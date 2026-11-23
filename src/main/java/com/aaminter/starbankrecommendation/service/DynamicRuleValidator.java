package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.dto.RuleQueryDto;
import com.aaminter.starbankrecommendation.model.ProductType;
import com.aaminter.starbankrecommendation.model.TransactionType;

import java.util.List;
import java.util.Set;

/**
 * Validates dynamic-rule condition types and their argument contracts
 * before a rule is persisted.
 */
final class DynamicRuleValidator {

    private static final Set<String> COMPARISON_OPERATORS =
            Set.of(">", "<", "=", ">=", "<=");

    private DynamicRuleValidator() {
    }

    static void validate(List<RuleQueryDto> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            throw new IllegalArgumentException(
                    "A dynamic rule must contain at least one condition"
            );
        }

        for (RuleQueryDto condition : conditions) {
            if (condition == null) {
                throw new IllegalArgumentException(
                        "A dynamic rule condition must not be null"
                );
            }

            List<String> arguments = condition.arguments();
            if (arguments == null) {
                throw new IllegalArgumentException(
                        "Condition arguments must not be null"
                );
            }

            switch (condition.query()) {
                case "USER_OF", "ACTIVE_USER_OF" -> {
                    requireArgumentCount(condition, 1);
                    parseProductType(arguments.get(0));
                }
                case "TRANSACTION_SUM_COMPARE" -> {
                    requireArgumentCount(condition, 4);
                    parseProductType(arguments.get(0));
                    parseTransactionType(arguments.get(1));
                    validateOperator(arguments.get(2));
                    validateThreshold(arguments.get(3));
                }
                case "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW" -> {
                    requireArgumentCount(condition, 2);
                    parseProductType(arguments.get(0));
                    validateOperator(arguments.get(1));
                }
                default -> throw new IllegalArgumentException(
                        "Unsupported query type: " + condition.query()
                );
            }
        }
    }

    private static void requireArgumentCount(
            RuleQueryDto condition,
            int expected
    ) {
        int actual = condition.arguments().size();
        if (actual != expected) {
            throw new IllegalArgumentException(
                    "Query " + condition.query() + " requires "
                            + expected + " arguments, but received " + actual
            );
        }
    }

    private static void parseProductType(String value) {
        try {
            ProductType.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException(
                    "Unsupported product type: " + value,
                    exception
            );
        }
    }

    private static void parseTransactionType(String value) {
        try {
            TransactionType.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException(
                    "Unsupported transaction type: " + value,
                    exception
            );
        }
    }

    private static void validateOperator(String operator) {
        if (!COMPARISON_OPERATORS.contains(operator)) {
            throw new IllegalArgumentException(
                    "Unsupported comparison operator: " + operator
            );
        }
    }

    private static void validateThreshold(String value) {
        if (value == null || !value.matches("\\d+")) {
            throw new IllegalArgumentException(
                    "Threshold must be a non-negative integer: " + value
            );
        }

        try {
            Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Threshold exceeds the maximum integer value: " + value,
                    exception
            );
        }
    }
}
