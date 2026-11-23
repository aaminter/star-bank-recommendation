package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.RuleQueryDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DynamicRuleValidatorTest {

    @Test
    void acceptsEverySupportedConditionType() {
        List<RuleQueryDto> conditions = List.of(
                new RuleQueryDto("USER_OF", List.of("DEBIT"), false),
                new RuleQueryDto("ACTIVE_USER_OF", List.of("CREDIT"), false),
                new RuleQueryDto(
                        "TRANSACTION_SUM_COMPARE",
                        List.of("INVEST", "DEPOSIT", ">=", "1000"),
                        false
                ),
                new RuleQueryDto(
                        "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW",
                        List.of("SAVING", "<"),
                        false
                )
        );

        assertDoesNotThrow(() -> DynamicRuleValidator.validate(conditions));
    }

    @Test
    void rejectsUnsupportedQuery() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DynamicRuleValidator.validate(List.of(
                        new RuleQueryDto("UNKNOWN_QUERY", List.of("DEBIT"), false)
                ))
        );
    }

    @Test
    void rejectsIncorrectArgumentCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DynamicRuleValidator.validate(List.of(
                        new RuleQueryDto("USER_OF", List.of("DEBIT", "CREDIT"), false)
                ))
        );
    }

    @Test
    void rejectsUnsupportedProductAndTransactionTypes() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DynamicRuleValidator.validate(List.of(
                        new RuleQueryDto("USER_OF", List.of("UNKNOWN_PRODUCT"), false)
                ))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> DynamicRuleValidator.validate(List.of(
                        new RuleQueryDto(
                                "TRANSACTION_SUM_COMPARE",
                                List.of("DEBIT", "UNKNOWN_TRANSACTION", ">", "100"),
                                false
                        )
                ))
        );
    }

    @Test
    void rejectsUnsupportedOperatorAndInvalidThreshold() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DynamicRuleValidator.validate(List.of(
                        new RuleQueryDto(
                                "TRANSACTION_SUM_COMPARE",
                                List.of("DEBIT", "DEPOSIT", "LIKE", "100"),
                                false
                        )
                ))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> DynamicRuleValidator.validate(List.of(
                        new RuleQueryDto(
                                "TRANSACTION_SUM_COMPARE",
                                List.of("DEBIT", "DEPOSIT", ">", "-1"),
                                false
                        )
                ))
        );
    }
}
