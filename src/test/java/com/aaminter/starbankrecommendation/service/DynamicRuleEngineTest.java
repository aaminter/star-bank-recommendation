package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.model.CustomerMetrics;
import com.aaminter.starbankrecommendation.model.DynamicRuleCondition;
import com.aaminter.starbankrecommendation.model.ProductType;
import com.aaminter.starbankrecommendation.model.ProductTypeMetrics;
import com.aaminter.starbankrecommendation.model.TransactionType;
import com.aaminter.starbankrecommendation.repository.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DynamicRuleEngineTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private RecommendationRepository repository;

    private DynamicRuleEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DynamicRuleEngine(repository);
    }

    private CustomerMetrics metrics(long debitCount) {
        return new CustomerMetrics(
                true,
                false,
                false,
                1_200,
                150_000,
                100_000,
                Map.of(
                        ProductType.DEBIT,
                        new ProductTypeMetrics(
                                debitCount,
                                new BigDecimal("150000"),
                                new BigDecimal("100000")
                        ),
                        ProductType.CREDIT,
                        ProductTypeMetrics.empty(),
                        ProductType.INVEST,
                        ProductTypeMetrics.empty(),
                        ProductType.SAVING,
                        new ProductTypeMetrics(
                                1,
                                new BigDecimal("1200"),
                                BigDecimal.ZERO
                        )
                )
        );
    }

    private DynamicRuleCondition condition(
            String query,
            List<String> arguments,
            boolean negate
    ) {
        return new DynamicRuleCondition(
                query,
                arguments,
                negate
        );
    }

    @Test
    void shouldEvaluateUserOf() {
        assertTrue(engine.matches(
                metrics(5),
                List.of(condition(
                        "USER_OF",
                        List.of("DEBIT"),
                        false
                ))
        ));

        assertFalse(engine.matches(
                metrics(5),
                List.of(condition(
                        "USER_OF",
                        List.of("CREDIT"),
                        false
                ))
        ));
    }

    @Test
    void shouldEvaluateActiveUserOf() {
        assertTrue(engine.matches(
                metrics(5),
                List.of(condition(
                        "ACTIVE_USER_OF",
                        List.of("DEBIT"),
                        false
                ))
        ));

        assertFalse(engine.matches(
                metrics(4),
                List.of(condition(
                        "ACTIVE_USER_OF",
                        List.of("DEBIT"),
                        false
                ))
        ));
    }

    @Test
    void shouldSupportAllComparisonOperators() {
        CustomerMetrics metrics = metrics(6);

        assertComparison(metrics, ">", "149999", true);
        assertComparison(metrics, ">", "150000", false);

        assertComparison(metrics, "<", "150001", true);
        assertComparison(metrics, "<", "150000", false);

        assertComparison(metrics, "=", "150000", true);
        assertComparison(metrics, "=", "149999", false);

        assertComparison(metrics, ">=", "150000", true);
        assertComparison(metrics, ">=", "150001", false);

        assertComparison(metrics, "<=", "150000", true);
        assertComparison(metrics, "<=", "149999", false);
    }

    private void assertComparison(
            CustomerMetrics metrics,
            String operator,
            String threshold,
            boolean expected
    ) {
        boolean result = engine.matches(
                metrics,
                List.of(condition(
                        "TRANSACTION_SUM_COMPARE",
                        List.of(
                                "DEBIT",
                                "DEPOSIT",
                                operator,
                                threshold
                        ),
                        false
                ))
        );

        assertEquals(
                expected,
                result,
                "Operator " + operator + ", threshold " + threshold
        );
    }

    @Test
    void shouldCompareDepositsWithWithdrawals() {
        CustomerMetrics metrics = metrics(6);

        assertTrue(engine.matches(
                metrics,
                List.of(condition(
                        "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW",
                        List.of("DEBIT", ">"),
                        false
                ))
        ));

        assertFalse(engine.matches(
                metrics,
                List.of(condition(
                        "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW",
                        List.of("DEBIT", "<"),
                        false
                ))
        ));

        assertFalse(engine.matches(
                metrics,
                List.of(condition(
                        "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW",
                        List.of("DEBIT", "="),
                        false
                ))
        ));
    }

    @Test
    void shouldApplyNegateAndAndLogic() {
        CustomerMetrics metrics = metrics(6);

        List<DynamicRuleCondition> conditions = List.of(
                condition("USER_OF", List.of("DEBIT"), false),
                condition("USER_OF", List.of("CREDIT"), true),
                condition("ACTIVE_USER_OF", List.of("DEBIT"), false)
        );

        assertTrue(engine.matches(metrics, conditions));

        // Отрицание истинного условия делает всё правило ложным.
        assertFalse(engine.matches(
                metrics,
                List.of(condition(
                        "USER_OF",
                        List.of("DEBIT"),
                        true
                ))
        ));

        // Все условия должны выполниться одновременно.
        assertFalse(engine.matches(
                metrics,
                List.of(
                        condition("USER_OF", List.of("DEBIT"), false),
                        condition("USER_OF", List.of("CREDIT"), false)
                )
        ));
    }

    @Test
    void shouldReturnFalseForEmptyRule() {
        assertFalse(engine.matches(
                metrics(6),
                List.of()
        ));
    }

    @Test
    void shouldRejectInvalidConditions() {
        CustomerMetrics metrics = metrics(6);

        assertThrows(
                IllegalArgumentException.class,
                () -> engine.matches(
                        metrics,
                        List.of(condition(
                                "UNKNOWN_QUERY",
                                List.of("DEBIT"),
                                false
                        ))
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> engine.matches(
                        metrics,
                        List.of(condition(
                                "USER_OF",
                                List.of("DEBIT", "CREDIT"),
                                false
                        ))
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> engine.matches(
                        metrics,
                        List.of(condition(
                                "TRANSACTION_SUM_COMPARE",
                                List.of(
                                        "DEBIT",
                                        "DEPOSIT",
                                        ">",
                                        "-100"
                                ),
                                false
                        ))
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> engine.matches(
                        metrics,
                        List.of(condition(
                                "TRANSACTION_SUM_COMPARE",
                                List.of(
                                        "DEBIT",
                                        "DEPOSIT",
                                        ">",
                                        "2147483648"
                                ),
                                false
                        ))
                )
        );
    }

    @Test
    void shouldLoadMetricsFromRepository() {
        when(repository.getCustomerMetrics(USER_ID))
                .thenReturn(metrics(6));

        boolean result = engine.matches(
                USER_ID,
                List.of(condition(
                        "ACTIVE_USER_OF",
                        List.of("DEBIT"),
                        false
                ))
        );

        assertTrue(result);

        verify(repository, times(1))
                .getCustomerMetrics(USER_ID);
    }
}