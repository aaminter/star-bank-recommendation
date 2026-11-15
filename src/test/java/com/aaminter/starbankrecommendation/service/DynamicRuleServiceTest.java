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
        // Подготовка данных
        UUID productId = UUID.randomUUID();
        List<RuleQueryDto> queries = List.of(
                new RuleQueryDto("USER_OF", List.of("CREDIT"), true),
                new RuleQueryDto("TRANSACTION_SUM_COMPARE", List.of("DEBIT", "DEPOSIT", ">", "100000"), false)
        );
        DynamicRuleDto inputDto = new DynamicRuleDto(
                null,
                "Простой кредит",
                productId,
                "Текст рекомендации",
                queries
        );

        // Настройка мока репозитория
        when(repository.save(any(DynamicRule.class))).thenAnswer(invocation -> {
            DynamicRule entity = invocation.getArgument(0);
            entity.setId(1L); // Имитируем автогенерацию ID в PostgreSQL
            return entity;
        });

        // Выполнение
        DynamicRuleDto result = ruleService.createRule(inputDto);

        // Проверка
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Простой кредит", result.productName());
        assertEquals(productId, result.productId());
        assertEquals("Текст рекомендации", result.productText());
        assertEquals(2, result.rule().size());
        verify(repository, times(1)).save(any(DynamicRule.class));
    }

    @Test
    void getAllRules_ShouldReturnMappedListOfDtos() {
        // Подготовка данных
        UUID productId = UUID.randomUUID();
        String jsonRule = "[{\"query\":\"USER_OF\",\"arguments\":[\"DEBIT\"],\"negate\":false}]";
        DynamicRule entity = new DynamicRule(productId, "Дебетовая карта", "Текст", jsonRule);
        entity.setId(10L);

        when(repository.findAll()).thenReturn(List.of(entity));

        // Выполнение
        List<DynamicRuleDto> rules = ruleService.getAllRules();

        // Проверка
        assertEquals(1, rules.size());
        DynamicRuleDto dto = rules.get(0);
        assertEquals(10L, dto.id());
        assertEquals("Дебетовая карта", dto.productName());
        assertEquals(1, dto.rule().size());
        assertEquals("USER_OF", dto.rule().get(0).query());
        assertFalse(dto.rule().get(0).negate());
        verify(repository, times(1)).findAll();
    }

    @Test
    void deleteRule_ShouldCallRepositoryDeleteById() {
        // Подготовка данных
        Long ruleId = 5L;

        // Выполнение
        ruleService.deleteRule(ruleId);

        // Проверка
        verify(repository, times(1)).deleteById(ruleId);
    }
}
