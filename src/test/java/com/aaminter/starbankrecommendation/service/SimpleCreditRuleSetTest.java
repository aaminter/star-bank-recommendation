package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.model.CustomerMetrics;
import com.aaminter.starbankrecommendation.repository.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleCreditRuleSetTest {

    private static final UUID USER_ID =
            UUID.fromString("1f9b149c-6577-448a-bc94-16bea229b71a");

    private static final UUID RECOMMENDATION_ID =
            UUID.fromString("ab138afb-f3ba-4a93-b74f-0fcee86d447f");

    @Mock
    private RecommendationRepository repository;

    private SimpleCreditRuleSet ruleSet;

    @BeforeEach
    void setUp() {
        ruleSet = new SimpleCreditRuleSet(repository);
    }

    @Test
    void shouldRecommendWhenAllRulesMatch() {
        when(repository.getCustomerMetrics(USER_ID))
                .thenReturn(new CustomerMetrics(
                        true, false, false,
                        10_000, 120_001, 100_001
                ));

        var result = ruleSet.check(USER_ID);

        assertTrue(result.isPresent());
        assertEquals("Простой кредит", result.get().name());
        assertEquals(RECOMMENDATION_ID, result.get().id());
    }

    @Test
    void shouldNotRecommendWhenCustomerAlreadyHasCredit() {
        when(repository.getCustomerMetrics(USER_ID))
                .thenReturn(new CustomerMetrics(
                        true, false, true,
                        10_000, 150_000, 120_000
                ));

        var result = ruleSet.check(USER_ID);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldNotRecommendWhenDepositsDoNotExceedWithdrawals() {
        when(repository.getCustomerMetrics(USER_ID))
                .thenReturn(new CustomerMetrics(
                        true, false, false,
                        10_000, 120_000, 120_000
                ));

        var result = ruleSet.check(USER_ID);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldNotRecommendWhenWithdrawalsEqualOneHundredThousand() {
        when(repository.getCustomerMetrics(USER_ID))
                .thenReturn(new CustomerMetrics(
                        true, false, false,
                        10_000, 150_000, 100_000
                ));

        var result = ruleSet.check(USER_ID);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldNotRecommendWhenWithdrawalsAreBelowThreshold() {
        when(repository.getCustomerMetrics(USER_ID))
                .thenReturn(new CustomerMetrics(
                        true, false, false,
                        10_000, 99_999, 99_998
                ));

        var result = ruleSet.check(USER_ID);

        assertFalse(result.isPresent());
    }
}