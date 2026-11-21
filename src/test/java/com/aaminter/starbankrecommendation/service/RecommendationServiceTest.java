
package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.DynamicRuleDto;
import com.aaminter.starbankrecommendation.dto.RecommendationDto;
import com.aaminter.starbankrecommendation.dto.RuleQueryDto;
import com.aaminter.starbankrecommendation.model.CustomerMetrics;
import com.aaminter.starbankrecommendation.model.DynamicRuleCondition;
import com.aaminter.starbankrecommendation.repository.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    private static final UUID USER_ID =
            UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");

    private static final UUID DYNAMIC_PRODUCT_ID =
            UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a");

    private static final CustomerMetrics METRICS =
            new CustomerMetrics(true, false, false, 10_000, 60_000, 20_000);

    @Mock
    private RecommendationRepository repository;

    @Mock
    private RecommendationRuleSet firstRule;

    @Mock
    private RecommendationRuleSet secondRule;

    @Mock
    private RecommendationRuleSet thirdRule;

    @Mock
    private DynamicRuleService dynamicRuleService;

    @Mock
    private DynamicRuleEngine dynamicRuleEngine;

    private RecommendationService service;

    @BeforeEach
    void setUp() {
        service = new RecommendationService(
                repository,
                List.of(firstRule, secondRule, thirdRule),
                dynamicRuleService,
                dynamicRuleEngine
        );
    }

    @Test
    void shouldCollectStaticAndDynamicRecommendationsAndLoadMetricsOnlyOnce() {
        RecommendationDto first = new RecommendationDto(
                "Invest 500",
                UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a"),
                "Investment description"
        );

        RecommendationDto second = new RecommendationDto(
                "Top Saving",
                UUID.fromString("59efc529-2fff-41af-baff-90ccd7402925"),
                "Saving description"
        );

        DynamicRuleDto dynamicRule = dynamicRule(
                "Dynamic Credit",
                "Credit recommendation",
                new RuleQueryDto("USER_OF", List.of("CREDIT"), true)
        );

        when(repository.getCustomerMetrics(USER_ID)).thenReturn(METRICS);
        when(firstRule.check(METRICS)).thenReturn(Optional.of(first));
        when(secondRule.check(METRICS)).thenReturn(Optional.empty());
        when(thirdRule.check(METRICS)).thenReturn(Optional.of(second));
        when(dynamicRuleService.getAllRules()).thenReturn(List.of(dynamicRule));
        when(dynamicRuleEngine.matches(
                METRICS,
                List.of(new DynamicRuleCondition("USER_OF", List.of("CREDIT"), true))
        )).thenReturn(true);

        var response = service.getRecommendations(USER_ID);

        RecommendationDto dynamicRecommendation = new RecommendationDto(
                "Dynamic Credit",
                DYNAMIC_PRODUCT_ID,
                "Credit recommendation"
        );

        assertEquals(USER_ID, response.userId());
        assertEquals(
                List.of(first, second, dynamicRecommendation),
                response.recommendations()
        );

        verify(repository).getCustomerMetrics(USER_ID);
        verify(firstRule).check(METRICS);
        verify(secondRule).check(METRICS);
        verify(thirdRule).check(METRICS);
        verify(dynamicRuleService).getAllRules();
        verify(dynamicRuleEngine).matches(
                METRICS,
                List.of(new DynamicRuleCondition("USER_OF", List.of("CREDIT"), true))
        );
    }

    @Test
    void shouldNotAddDynamicRecommendationWhenRuleDoesNotMatch() {
        DynamicRuleDto dynamicRule = dynamicRule(
                "Dynamic Credit",
                "Credit recommendation",
                new RuleQueryDto("USER_OF", List.of("CREDIT"), false)
        );

        when(repository.getCustomerMetrics(USER_ID)).thenReturn(METRICS);
        when(firstRule.check(METRICS)).thenReturn(Optional.empty());
        when(secondRule.check(METRICS)).thenReturn(Optional.empty());
        when(thirdRule.check(METRICS)).thenReturn(Optional.empty());
        when(dynamicRuleService.getAllRules()).thenReturn(List.of(dynamicRule));
        when(dynamicRuleEngine.matches(
                METRICS,
                List.of(new DynamicRuleCondition("USER_OF", List.of("CREDIT"), false))
        )).thenReturn(false);

        var response = service.getRecommendations(USER_ID);

        assertEquals(USER_ID, response.userId());
        assertEquals(List.of(), response.recommendations());

        verify(repository).getCustomerMetrics(USER_ID);
        verify(dynamicRuleService).getAllRules();
        verify(dynamicRuleEngine).matches(
                METRICS,
                List.of(new DynamicRuleCondition("USER_OF", List.of("CREDIT"), false))
        );
    }

    @Test
    void shouldHandleEmptyDynamicRulesAndPreserveStaticRecommendations() {
        RecommendationDto staticRecommendation = new RecommendationDto(
                "Invest 500",
                UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a"),
                "Investment description"
        );

        when(repository.getCustomerMetrics(USER_ID)).thenReturn(METRICS);
        when(firstRule.check(METRICS)).thenReturn(Optional.of(staticRecommendation));
        when(secondRule.check(METRICS)).thenReturn(Optional.empty());
        when(thirdRule.check(METRICS)).thenReturn(Optional.empty());
        when(dynamicRuleService.getAllRules()).thenReturn(List.of());

        var response = service.getRecommendations(USER_ID);

        assertEquals(List.of(staticRecommendation), response.recommendations());

        verify(repository).getCustomerMetrics(USER_ID);
        verify(dynamicRuleService).getAllRules();
        verifyNoInteractions(dynamicRuleEngine);
    }

    @Test
    void shouldSkipInvalidDynamicRuleAndContinueProcessingOtherRules() {
        RecommendationDto staticRecommendation = new RecommendationDto(
                "Invest 500",
                UUID.randomUUID(),
                "Investment description"
        );

        DynamicRuleDto invalidRule = new DynamicRuleDto(
                10L,
                "Invalid rule",
                UUID.randomUUID(),
                "Invalid rule description",
                List.of(new RuleQueryDto("UNKNOWN_QUERY", List.of("DEBIT"), false))
        );

        DynamicRuleDto validRule = new DynamicRuleDto(
                11L,
                "Valid dynamic rule",
                DYNAMIC_PRODUCT_ID,
                "Valid recommendation",
                List.of(new RuleQueryDto("USER_OF", List.of("DEBIT"), false))
        );

        when(repository.getCustomerMetrics(USER_ID)).thenReturn(METRICS);
        when(firstRule.check(METRICS)).thenReturn(Optional.of(staticRecommendation));
        when(secondRule.check(METRICS)).thenReturn(Optional.empty());
        when(thirdRule.check(METRICS)).thenReturn(Optional.empty());
        when(dynamicRuleService.getAllRules())
                .thenReturn(List.of(invalidRule, validRule));

        when(dynamicRuleEngine.matches(
                METRICS,
                List.of(new DynamicRuleCondition(
                        "UNKNOWN_QUERY",
                        List.of("DEBIT"),
                        false
                ))
        )).thenThrow(new IllegalArgumentException("Unknown query"));

        when(dynamicRuleEngine.matches(
                METRICS,
                List.of(new DynamicRuleCondition(
                        "USER_OF",
                        List.of("DEBIT"),
                        false
                ))
        )).thenReturn(true);

        var response = service.getRecommendations(USER_ID);

        RecommendationDto validRecommendation = new RecommendationDto(
                "Valid dynamic rule",
                DYNAMIC_PRODUCT_ID,
                "Valid recommendation"
        );

        assertEquals(
                List.of(staticRecommendation, validRecommendation),
                response.recommendations()
        );

        verify(dynamicRuleEngine).matches(
                METRICS,
                List.of(new DynamicRuleCondition(
                        "UNKNOWN_QUERY",
                        List.of("DEBIT"),
                        false
                ))
        );

        verify(dynamicRuleEngine).matches(
                METRICS,
                List.of(new DynamicRuleCondition(
                        "USER_OF",
                        List.of("DEBIT"),
                        false
                ))
        );
    }

    private DynamicRuleDto dynamicRule(
            String productName,
            String productText,
            RuleQueryDto condition
    ) {
        return new DynamicRuleDto(
                1L,
                productName,
                DYNAMIC_PRODUCT_ID,
                productText,
                List.of(condition)
        );
    }
}
