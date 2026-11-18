package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.model.CustomerMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleCreditRuleSetTest {

    private static final UUID RECOMMENDATION_ID =
            UUID.fromString("ab138afb-f3ba-4a93-b74f-0fcee86d447f");

    private SimpleCreditRuleSet ruleSet;

    @BeforeEach
    void setUp() {
        ruleSet = new SimpleCreditRuleSet();
    }

    @Test
    void shouldRecommendWhenAllRulesMatch() {
        var result = ruleSet.check(new CustomerMetrics(true, false, false, 10_000, 120_001, 100_001));
        assertTrue(result.isPresent());
        assertEquals("Простой кредит", result.get().name());
        assertEquals(RECOMMENDATION_ID, result.get().id());
    }

    @Test
    void shouldNotRecommendWhenCustomerAlreadyHasCredit() {
        assertFalse(ruleSet.check(new CustomerMetrics(true, false, true, 10_000, 150_000, 120_000)).isPresent());
    }

    @Test
    void shouldNotRecommendWhenDepositsDoNotExceedWithdrawals() {
        assertFalse(ruleSet.check(new CustomerMetrics(true, false, false, 10_000, 120_000, 120_000)).isPresent());
    }

    @Test
    void shouldNotRecommendWhenWithdrawalsEqualOneHundredThousand() {
        assertFalse(ruleSet.check(new CustomerMetrics(true, false, false, 10_000, 150_000, 100_000)).isPresent());
    }

    @Test
    void shouldNotRecommendWhenWithdrawalsAreBelowThreshold() {
        assertFalse(ruleSet.check(new CustomerMetrics(true, false, false, 10_000, 99_999, 99_998)).isPresent());
    }
}
