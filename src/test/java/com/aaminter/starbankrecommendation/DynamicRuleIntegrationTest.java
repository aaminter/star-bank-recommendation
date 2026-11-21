package com.aaminter.starbankrecommendation;

import com.aaminter.starbankrecommendation.dto.DynamicRuleDto;
import com.aaminter.starbankrecommendation.dto.RuleQueryDto;
import com.aaminter.starbankrecommendation.repository.recommendations.DynamicRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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
    private ObjectMapper objectMapper;

    @Autowired
    private DynamicRuleRepository repository;

    @BeforeEach
    void cleanUp() {
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
                "Простой кредит",
                productId,
                "Выгодный кредит на любые цели",
                conditions
        );

        String responseJson = mockMvc.perform(post("/rule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.product_name").value("Простой кредит"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        DynamicRuleDto created = objectMapper.readValue(responseJson, DynamicRuleDto.class);

        assertEquals(1, repository.count());
        var savedEntity = repository.findById(created.id()).orElseThrow();
        assertTrue(savedEntity.getRule().contains("TRANSACTION_SUM_COMPARE"));
        assertTrue(savedEntity.getRule().contains("100000"));

        mockMvc.perform(get("/rule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(created.id()))
                .andExpect(jsonPath("$.data[0].product_name").value("Простой кредит"))
                .andExpect(jsonPath("$.data[0].product_id").value(productId.toString()))
                .andExpect(jsonPath("$.data[0].product_text").value("Выгодный кредит на любые цели"))
                .andExpect(jsonPath("$.data[0].rule.length()").value(2))
                .andExpect(jsonPath("$.data[0].rule[0].query").value("USER_OF"))
                .andExpect(jsonPath("$.data[0].rule[0].arguments[0]").value("CREDIT"))
                .andExpect(jsonPath("$.data[0].rule[0].negate").value(true))
                .andExpect(jsonPath("$.data[0].rule[1].query").value("TRANSACTION_SUM_COMPARE"))
                .andExpect(jsonPath("$.data[0].rule[1].arguments[3]").value("100000"))
                .andExpect(jsonPath("$.data[0].rule[1].negate").value(false));


        mockMvc.perform(delete("/rule/{id}", created.id()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/rule/{id}", created.id()))
                .andExpect(status().isNotFound());
    }
}
