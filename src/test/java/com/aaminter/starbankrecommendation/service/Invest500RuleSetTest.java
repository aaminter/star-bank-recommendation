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
class Invest500RuleSetTest {

    private static final UUID USER_ID =
            UUID.fromString("cd515076-5d8a-44be-930e-8d4fcb79f42d");

    private static final UUID RECOMMENDATION_ID =
            UUID.fromString("147f6a0f-3b91-413b-ab99-87f081d60d5a");

    @Mock
    private RecommendationRepository repository;

    private Invest500RuleSet ruleSet;

    @BeforeEach
    void setUp() {
        ruleSet = new Invest500RuleSet(repository);
    }

    @Test
    void shouldRecommendWhenAllRulesMatch() {
        when(repository.getCustomerMetrics(USER_ID))
                .thenReturn(new CustomerMetrics(
                        true, false, false,
                        1_001, 0, 0
                ));

        var result = ruleSet.check(USER_ID);

        assertTrue(result.isPresent());
        assertEquals("Invest 500", result.get().name());
        assertEquals(RECOMMENDATION_ID, result.get().id());
    }

    @Test
    void shouldNotRecommendWhenCustomerHasNoDebit() {
        when(repository.getCustomerMetrics(USER_ID))
                .thenReturn(new CustomerMetrics(
                        false, false, false,
                        5_000, 0, 0
                ));

        var result = ruleSet.check(USER_ID);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldNotRecommendWhenCustomerAlreadyHasInvest() {
        when(repository.getCustomerMetrics(USER_ID))
                .thenReturn(new CustomerMetrics(
                        true, true, false,
                        5_000, 0, 0
                ));

        var result = ruleSet.check(USER_ID);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldNotRecommendWhenSavingDepositsEqualOneThousand() {
        when(repository.getCustomerMetrics(USER_ID))
                .thenReturn(new CustomerMetrics(
                        true, false, false,
                        1_000, 0, 0
                ));

        var result = ruleSet.check(USER_ID);

        assertFalse(result.isPresent());
    }
}