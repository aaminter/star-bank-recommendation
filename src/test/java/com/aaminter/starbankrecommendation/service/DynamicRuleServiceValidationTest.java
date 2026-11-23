package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.DynamicRuleDto;
import com.aaminter.starbankrecommendation.dto.RuleQueryDto;
import com.aaminter.starbankrecommendation.repository.recommendations.DynamicRuleRepository;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class DynamicRuleServiceValidationTest {

    @Test
    void createRuleRejectsUnsupportedConditionBeforePersistence() {
        DynamicRuleRepository repository = mock(DynamicRuleRepository.class);
        DynamicRuleService service = new DynamicRuleService(
                repository,
                new ObjectMapper()
        );

        DynamicRuleDto invalidRule = new DynamicRuleDto(
                null,
                "Invalid recommendation",
                UUID.randomUUID(),
                "This rule must not be stored",
                List.of(new RuleQueryDto(
                        "UNKNOWN_QUERY",
                        List.of("DEBIT"),
                        false
                ))
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createRule(invalidRule)
        );
        verifyNoInteractions(repository);
    }
}
