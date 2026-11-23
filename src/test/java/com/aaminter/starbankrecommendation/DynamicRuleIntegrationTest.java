
package com.aaminter.starbankrecommendation;

import com.aaminter.starbankrecommendation.dto.DynamicRuleDto;
import com.aaminter.starbankrecommendation.dto.RuleQueryDto;
import com.aaminter.starbankrecommendation.repository.RecommendationRepository;
import com.aaminter.starbankrecommendation.repository.recommendations.DynamicRuleRepository;
import com.aaminter.starbankrecommendation.service.RuleStatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:integration_h2;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.hikari.read-only=false",
        "spring.liquibase.enabled=true"
})
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "REC_DB_PASSWORD", matches = ".+")
class DynamicRuleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DynamicRuleRepository repository;

    @Autowired
    private RuleStatsService ruleStatsService;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS PRODUCTS (
                    ID UUID PRIMARY KEY,
                    TYPE VARCHAR(50),
                    NAME VARCHAR(100)
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS TRANSACTIONS (
                    ID UUID PRIMARY KEY,
                    PRODUCT_ID UUID,
                    USER_ID UUID,
                    TYPE VARCHAR(50),
                    AMOUNT DECIMAL(19, 2)
                )
                """);

        jdbcTemplate.execute("DELETE FROM TRANSACTIONS");
        jdbcTemplate.execute("DELETE FROM PRODUCTS");

        recommendationRepository.clearCache();
        repository.deleteAll();
    }

    @Test
    void fullCycle_PostToPostgresAndGetWithJsonbConditions_ShouldWork() throws Exception {
        UUID productId = UUID.randomUUID();

        List<RuleQueryDto> conditions = List.of(
                new RuleQueryDto("USER_OF", List.of("CREDIT"), true),
                new RuleQueryDto("TRANSACTION_SUM_COMPARE", List.of("DEBIT", "DEPOSIT", ">", "100000"), false)
        );

        DynamicRuleDto requestDto = new DynamicRuleDto(
                null,
                "Simple credit",
                productId,
                "Credit for any purpose",
                conditions
        );

        String responseJson = mockMvc.perform(post("/rule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.product_name").value("Simple credit"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        DynamicRuleDto created = objectMapper.readValue(
                responseJson,
                DynamicRuleDto.class
        );

        assertEquals(1, repository.count());

        var savedEntity = repository.findById(created.id()).orElseThrow();

        assertTrue(savedEntity.getRule().contains("TRANSACTION_SUM_COMPARE"));
        assertTrue(savedEntity.getRule().contains("100000"));

        mockMvc.perform(get("/rule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(created.id()))
                .andExpect(jsonPath("$.data[0].product_name").value("Simple credit"))
                .andExpect(jsonPath("$.data[0].product_id").value(productId.toString()))
                .andExpect(jsonPath("$.data[0].product_text").value("Credit for any purpose"))
                .andExpect(jsonPath("$.data[0].rule.length()").value(2))
                .andExpect(jsonPath("$.data[0].rule[0].query").value("USER_OF"))
                .andExpect(jsonPath("$.data[0].rule[0].arguments[0]").value("CREDIT"))
                .andExpect(jsonPath("$.data[0].rule[0].negate").value(true))
                .andExpect(jsonPath("$.data[0].rule[1].query").value("TRANSACTION_SUM_COMPARE"))
                .andExpect(jsonPath("$.data[0].rule[1].arguments[3]").value("100000"))
                .andExpect(jsonPath("$.data[0].rule[1].negate").value(false));

        mockMvc.perform(get("/rule/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats.length()").value(1))
                .andExpect(jsonPath("$.stats[0].rule_id").value(String.valueOf(created.id())))
                .andExpect(jsonPath("$.stats[0].count").value("0"));

        ruleStatsService.increment(created.id());
        ruleStatsService.increment(created.id());

        mockMvc.perform(get("/rule/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats.length()").value(1))
                .andExpect(jsonPath("$.stats[0].rule_id").value(String.valueOf(created.id())))
                .andExpect(jsonPath("$.stats[0].count").value("2"));

        mockMvc.perform(delete("/rule/{id}", created.id()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/rule/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats.length()").value(0));

        mockMvc.perform(delete("/rule/{id}", created.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void recommendationRequest_ShouldIncrementStatsOnEveryRuleMatch() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        DynamicRuleDto requestDto = new DynamicRuleDto(
                null,
                "Automatic statistics test",
                productId,
                "Test product",
                List.of(
                        new RuleQueryDto("USER_OF", List.of("CREDIT"), true)
                )
        );

        String responseJson = mockMvc.perform(post("/rule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        DynamicRuleDto created = objectMapper.readValue(
                responseJson,
                DynamicRuleDto.class
        );

        mockMvc.perform(get("/rule/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats.length()").value(1))
                .andExpect(jsonPath("$.stats[0].rule_id").value(String.valueOf(created.id())))
                .andExpect(jsonPath("$.stats[0].count").value("0"));

        mockMvc.perform(get("/recommendation/{user_id}", userId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/rule/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats[0].rule_id").value(String.valueOf(created.id())))
                .andExpect(jsonPath("$.stats[0].count").value("1"));

        mockMvc.perform(get("/recommendation/{user_id}", userId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/rule/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats[0].rule_id").value(String.valueOf(created.id())))
                .andExpect(jsonPath("$.stats[0].count").value("2"));
    }
}
