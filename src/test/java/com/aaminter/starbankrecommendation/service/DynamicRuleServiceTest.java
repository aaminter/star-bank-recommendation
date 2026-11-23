
package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.DynamicRuleDto;
import com.aaminter.starbankrecommendation.dto.RuleQueryDto;
import com.aaminter.starbankrecommendation.model.recommendations.DynamicRule;
import com.aaminter.starbankrecommendation.repository.recommendations.DynamicRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DynamicRuleServiceTest {

    @Mock
    private DynamicRuleRepository repository;

    private ObjectMapper objectMapper;
    private DynamicRuleService ruleService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        ruleService = new DynamicRuleService(repository, objectMapper);
    }

    @Test
    void createRule_ShouldSaveAndReturnDtoWithGeneratedId() {
        UUID productId = UUID.randomUUID();

        List<RuleQueryDto> queries = List.of(
                new RuleQueryDto("USER_OF", List.of("CREDIT"), true),
                new RuleQueryDto(
                        "TRANSACTION_SUM_COMPARE",
                        List.of("DEBIT", "DEPOSIT", ">", "100000"),
                        false
                )
        );

        DynamicRuleDto inputDto = new DynamicRuleDto(
                null,
                "РџСЂРѕСЃС‚РѕР№ РєСЂРµРґРёС‚",
                productId,
                "РўРµРєСЃС‚ СЂРµРєРѕРјРµРЅРґР°С†РёРё",
                queries
        );

        when(repository.save(any(DynamicRule.class))).thenAnswer(invocation -> {
            DynamicRule entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        DynamicRuleDto result = ruleService.createRule(inputDto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("РџСЂРѕСЃС‚РѕР№ РєСЂРµРґРёС‚", result.productName());
        assertEquals(productId, result.productId());
        assertEquals("РўРµРєСЃС‚ СЂРµРєРѕРјРµРЅРґР°С†РёРё", result.productText());
        assertEquals(2, result.rule().size());

        verify(repository, times(1)).save(any(DynamicRule.class));
    }

    @Test
    void getAllRules_ShouldReturnMappedListOfDtos() {
        UUID productId = UUID.randomUUID();

        String jsonRule =
                "[{\"query\":\"USER_OF\",\"arguments\":[\"DEBIT\"],\"negate\":false}]";

        DynamicRule entity = new DynamicRule(
                productId,
                "Р”РµР±РµС‚РѕРІР°СЏ РєР°СЂС‚Р°",
                "РўРµРєСЃС‚",
                jsonRule
        );
        entity.setId(10L);

        when(repository.findAll()).thenReturn(List.of(entity));

        List<DynamicRuleDto> rules = ruleService.getAllRules();

        assertEquals(1, rules.size());

        DynamicRuleDto dto = rules.get(0);

        assertEquals(10L, dto.id());
        assertEquals("Р”РµР±РµС‚РѕРІР°СЏ РєР°СЂС‚Р°", dto.productName());
        assertEquals(1, dto.rule().size());
        assertEquals("USER_OF", dto.rule().get(0).query());
        assertFalse(dto.rule().get(0).negate());

        verify(repository, times(1)).findAll();
    }

    @Test
    void getAllRules_ShouldSkipInvalidJsonAndReturnValidRules() {
        UUID validProductId = UUID.randomUUID();

        DynamicRule invalidRule = new DynamicRule(
                UUID.randomUUID(),
                "Invalid rule",
                "Invalid rule text",
                "not-valid-json"
        );
        invalidRule.setId(20L);

        DynamicRule validRule = new DynamicRule(
                validProductId,
                "Valid rule",
                "Valid rule text",
                "[{\"query\":\"USER_OF\",\"arguments\":[\"DEBIT\"],\"negate\":false}]"
        );
        validRule.setId(21L);

        when(repository.findAll()).thenReturn(List.of(invalidRule, validRule));

        List<DynamicRuleDto> rules = ruleService.getAllRules();

        assertEquals(1, rules.size());
        assertEquals(21L, rules.get(0).id());
        assertEquals("Valid rule", rules.get(0).productName());
        assertEquals("USER_OF", rules.get(0).rule().get(0).query());

        verify(repository, times(1)).findAll();
    }

    @Test
    void deleteRule_ShouldCallRepositoryDeleteById() {
        Long ruleId = 5L;

        when(repository.existsById(ruleId)).thenReturn(true);

        ruleService.deleteRule(ruleId);

        verify(repository, times(1)).existsById(ruleId);
        verify(repository, times(1)).deleteById(ruleId);
    }

    @Test
    void deleteRule_WhenNotFound_ShouldThrowException() {
        Long ruleId = 999L;

        when(repository.existsById(ruleId)).thenReturn(false);

        assertThrows(
                jakarta.persistence.EntityNotFoundException.class,
                () -> ruleService.deleteRule(ruleId)
        );

        verify(repository, never()).deleteById(any());
    }
}
