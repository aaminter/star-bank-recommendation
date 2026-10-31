package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.model.CustomerMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Invest500RuleSetTest {

    private static final UUID RECOMMENDATION_ID =
            UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a");

    private Invest500RuleSet ruleSet;

    @BeforeEach
    void setUp() {
        ruleSet = new Invest500RuleSet();
    }

    @Test
    void shouldRecommendWhenAllRulesMatch() {
        var result = ruleSet.check(new CustomerMetrics(true, false, false, 1_001, 0, 0));
        assertTrue(result.isPresent());
        assertEquals("Invest 500", result.get().name());
        assertEquals(RECOMMENDATION_ID, result.get().id());
    }

    @Test
    void shouldNotRecommendWhenCustomerHasNoDebit() {
        assertFalse(ruleSet.check(new CustomerMetrics(false, false, false, 5_000, 0, 0)).isPresent());
    }

    @Test
    void shouldNotRecommendWhenCustomerAlreadyHasInvest() {
        assertFalse(ruleSet.check(new CustomerMetrics(true, true, false, 5_000, 0, 0)).isPresent());
    }

    @Test
    void shouldNotRecommendWhenSavingDepositsEqualOneThousand() {
        assertFalse(ruleSet.check(new CustomerMetrics(true, false, false, 1_000, 0, 0)).isPresent());
    }
}
