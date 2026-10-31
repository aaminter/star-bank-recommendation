package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.model.CustomerMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopSavingRuleSetTest {

    private static final UUID RECOMMENDATION_ID =
            UUID.fromString("59efc529-2fff-41af-baff-90ccd7402925");

    private TopSavingRuleSet ruleSet;

    @BeforeEach
    void setUp() {
        ruleSet = new TopSavingRuleSet();
    }

    @Test
    void shouldRecommendWhenDebitDepositsReachThreshold() {
        var result = ruleSet.check(new CustomerMetrics(true, false, false, 10_000, 50_000, 20_000));
        assertTrue(result.isPresent());
        assertEquals("Top Saving", result.get().name());
        assertEquals(RECOMMENDATION_ID, result.get().id());
    }

    @Test
    void shouldRecommendWhenSavingDepositsReachThreshold() {
        assertTrue(ruleSet.check(new CustomerMetrics(true, false, false, 50_000, 10_000, 5_000)).isPresent());
    }

    @Test
    void shouldNotRecommendWhenCustomerHasNoDebit() {
        assertFalse(ruleSet.check(new CustomerMetrics(false, false, false, 100_000, 100_000, 10_000)).isPresent());
    }

    @Test
    void shouldNotRecommendWhenBothDepositAmountsBelowThreshold() {
        assertFalse(ruleSet.check(new CustomerMetrics(true, false, false, 49_999, 49_999, 10_000)).isPresent());
    }

    @Test
    void shouldNotRecommendWhenDepositsEqualWithdrawals() {
        assertFalse(ruleSet.check(new CustomerMetrics(true, false, false, 10_000, 50_000, 50_000)).isPresent());
    }
}
