
package com.aaminter.starbankrecommendation.controller;

import com.aaminter.starbankrecommendation.dto.DynamicRuleDto;
import com.aaminter.starbankrecommendation.dto.RuleQueryDto;
import com.aaminter.starbankrecommendation.service.DynamicRuleService;
import com.aaminter.starbankrecommendation.service.RuleStatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DynamicRuleController.class)
class DynamicRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DynamicRuleService ruleService;

    @MockitoBean
    private RuleStatsService ruleStatsService;

    @Test
    void createRule_ShouldReturn200AndCreatedRule() throws Exception {
        UUID productId = UUID.randomUUID();
        List<RuleQueryDto> queries = List.of(
                new RuleQueryDto("USER_OF", List.of("CREDIT"), true)
        );
        DynamicRuleDto requestDto = new DynamicRuleDto(null, "Простой кредит", productId, "Описание", queries);
        DynamicRuleDto responseDto = new DynamicRuleDto(1L, "Простой кредит", productId, "Описание", queries);

        when(ruleService.createRule(any(DynamicRuleDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/rule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.product_name").value("Простой кредит"))
                .andExpect(jsonPath("$.product_id").value(productId.toString()))
                .andExpect(jsonPath("$.rule[0].query").value("USER_OF"))
                .andExpect(jsonPath("$.rule[0].negate").value(true));
    }

    @Test
    void getAllRules_ShouldReturn200AndDataArray() throws Exception {
        UUID productId = UUID.randomUUID();
        DynamicRuleDto rule = new DynamicRuleDto(1L, "Простой кредит", productId, "Описание", List.of());

        when(ruleService.getAllRules()).thenReturn(List.of(rule));

        mockMvc.perform(get("/rule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].product_name").value("Простой кредит"));
    }

    @Test
    void deleteRule_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/rule/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void getRuleStats_ShouldReturn200AndStatsArray() throws Exception {
        when(ruleStatsService.getAllStats()).thenReturn(List.of(
                Map.of("rule_id", "1", "count", "5"),
                Map.of("rule_id", "2", "count", "0")
        ));

        mockMvc.perform(get("/rule/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats").isArray())
                .andExpect(jsonPath("$.stats[0].rule_id").value("1"))
                .andExpect(jsonPath("$.stats[0].count").value("5"))
                .andExpect(jsonPath("$.stats[1].rule_id").value("2"))
                .andExpect(jsonPath("$.stats[1].count").value("0"));
    }
}
