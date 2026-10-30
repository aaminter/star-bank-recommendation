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
class TopSavingRuleSetTest {

    private static final UUID USER_ID =
            UUID.fromString("d4a4d619-9a0c-4fc5-b0cb-76c49409546b");

    private static final UUID RECOMMENDATION_ID =
            UUID.fromString("59efc529-2fff-41af-baff-90ccd7402925");

    @Mock
    private RecommendationRepository repository;

    private TopSavingRuleSet ruleSet;

    @BeforeEach
    void setUp() {
        ruleSet = new TopSavingRuleSet(repository);
    }

    @Test
    void shouldRecommendWhenDebitDepositsReachThreshold() {
        when(repository.getCustomerMetrics(USER_ID))
                .thenReturn(new CustomerMetrics(
                        true, false, false,
                        10_000, 50_000, 20_000
                ));

        var result = ruleSet.check(USER_ID);

        assertTrue(result.isPresent());
        assertEquals("Top Saving", result.get().name());
        assertEquals(RECOMMENDATION_ID, result.get().id());
    }

    @Test
    void shouldRecommendWhenSavingDepositsReachThreshold() {
        when(repository.getCustomerMetrics(USER_ID))
                .thenReturn(new CustomerMetrics(
                        true, false, false,
                        50_000, 10_000, 5_000
                ));

        var result = ruleSet.check(USER_ID);

        assertTrue(result.isPresent());
    }

    @Test
    void shouldNotRecommendWhenCustomerHasNoDebit() {
        when(repository.getCustomerMetrics(USER_ID))
                .thenReturn(new CustomerMetrics(
                        false, false, false,
                        100_000, 100_000, 10_000
                ));

        var result = ruleSet.check(USER_ID);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldNotRecommendWhenBothDepositAmountsBelowThreshold() {
        when(repository.getCustomerMetrics(USER_ID))
                .thenReturn(new CustomerMetrics(
                        true, false, false,
                        49_999, 49_999, 10_000
                ));

        var result = ruleSet.check(USER_ID);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldNotRecommendWhenDepositsEqualWithdrawals() {
        when(repository.getCustomerMetrics(USER_ID))
                .thenReturn(new CustomerMetrics(
                        true, false, false,
                        10_000, 50_000, 50_000
                ));

        var result = ruleSet.check(USER_ID);

        assertFalse(result.isPresent());
    }
}