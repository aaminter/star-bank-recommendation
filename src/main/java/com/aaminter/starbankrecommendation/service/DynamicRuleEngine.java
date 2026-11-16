package com.aaminter.starbankrecommendation.service;

import com.aaminter.starbankrecommendation.model.CustomerMetrics;
import com.aaminter.starbankrecommendation.model.DynamicRuleCondition;
import com.aaminter.starbankrecommendation.model.ProductType;
import com.aaminter.starbankrecommendation.model.ProductTypeMetrics;
import com.aaminter.starbankrecommendation.model.TransactionType;
import com.aaminter.starbankrecommendation.repository.RecommendationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class DynamicRuleEngine {

    private final RecommendationRepository repository;

    public DynamicRuleEngine(RecommendationRepository repository) {
        this.repository = repository;
    }

    /**
     * Проверяет динамическое правило для пользователя.
     */
    public boolean matches(
            UUID userId,
            List<DynamicRuleCondition> conditions
    ) {
        Objects.requireNonNull(userId, "User ID must not be null");

        CustomerMetrics metrics =
                repository.getCustomerMetrics(userId);

        return matches(metrics, conditions);
    }

    /**
     * Проверяет все условия правила по принципу AND.
     */
    public boolean matches(
            CustomerMetrics metrics,
            List<DynamicRuleCondition> conditions
    ) {
        Objects.requireNonNull(metrics, "Metrics must not be null");
        Objects.requireNonNull(
                conditions,
                "Conditions must not be null"
        );

        // Пустое правило не считается выполненным.
        if (conditions.isEmpty()) {
            return false;
        }

        for (DynamicRuleCondition condition : conditions) {
            boolean result = evaluateCondition(metrics, condition);

            if (condition.negate()) {
                result = !result;
            }

            if (!result) {
                return false;
            }
        }

        return true;
    }

    /**
     * Выбирает обработчик по типу запроса.
     */
    private boolean evaluateCondition(
            CustomerMetrics metrics,
            DynamicRuleCondition condition
    ) {
        Objects.requireNonNull(
                condition,
                "Condition must not be null"
        );

        return switch (condition.query()) {
            case "USER_OF" ->
                    evaluateUserOf(metrics, condition);

            case "ACTIVE_USER_OF" ->
                    evaluateActiveUserOf(metrics, condition);

            case "TRANSACTION_SUM_COMPARE" ->
                    evaluateTransactionSumCompare(metrics, condition);

            case "TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW" ->
                    evaluateDepositWithdrawCompare(metrics, condition);

            default -> throw new IllegalArgumentException(
                    "Unsupported query type: " + condition.query()
            );
        };
    }

    /**
     * Проверяет наличие хотя бы одной транзакции
     * по продукту указанного типа.
     */
    private boolean evaluateUserOf(
            CustomerMetrics metrics,
            DynamicRuleCondition condition
    ) {
        validateArguments(condition, 1);

        ProductType productType =
                parseProductType(condition.arguments().get(0));

        return metrics.forProduct(productType)
                .transactionCount() > 0;
    }

    /**
     * Проверяет наличие минимум пяти транзакций
     * по продукту указанного типа.
     */
    private boolean evaluateActiveUserOf(
            CustomerMetrics metrics,
            DynamicRuleCondition condition
    ) {
        validateArguments(condition, 1);

        ProductType productType =
                parseProductType(condition.arguments().get(0));

        return metrics.forProduct(productType)
                .transactionCount() >= 5;
    }

    /**
     * Сравнивает сумму пополнений или трат
     * с заданной константой.
     *
     * arguments:
     * 0 - product type
     * 1 - transaction type
     * 2 - comparison operator
     * 3 - non-negative integer
     */
    private boolean evaluateTransactionSumCompare(
            CustomerMetrics metrics,
            DynamicRuleCondition condition
    ) {
        validateArguments(condition, 4);

        ProductType productType =
                parseProductType(condition.arguments().get(0));

        TransactionType transactionType =
                parseTransactionType(condition.arguments().get(1));

        String operator = condition.arguments().get(2);

        int threshold = parseThreshold(
                condition.arguments().get(3)
        );

        ProductTypeMetrics productMetrics =
                metrics.forProduct(productType);

        BigDecimal actualSum =
                productMetrics.sum(transactionType);

        return compare(
                actualSum,
                operator,
                BigDecimal.valueOf(threshold)
        );
    }

    /**
     * Сравнивает общую сумму пополнений
     * с общей суммой трат по продукту.
     *
     * arguments:
     * 0 - product type
     * 1 - comparison operator
     */
    private boolean evaluateDepositWithdrawCompare(
            CustomerMetrics metrics,
            DynamicRuleCondition condition
    ) {
        validateArguments(condition, 2);

        ProductType productType =
                parseProductType(condition.arguments().get(0));

        String operator = condition.arguments().get(1);

        ProductTypeMetrics productMetrics =
                metrics.forProduct(productType);

        return compare(
                productMetrics.depositSum(),
                operator,
                productMetrics.withdrawSum()
        );
    }

    /**
     * Выполняет сравнение BigDecimal без потери точности.
     */
    private boolean compare(
            BigDecimal left,
            String operator,
            BigDecimal right
    ) {
        int comparison = left.compareTo(right);

        return switch (operator) {
            case ">" -> comparison > 0;
            case "<" -> comparison < 0;
            case "=" -> comparison == 0;
            case ">=" -> comparison >= 0;
            case "<=" -> comparison <= 0;

            default -> throw new IllegalArgumentException(
                    "Unsupported comparison operator: " + operator
            );
        };
    }

    private ProductType parseProductType(String value) {
        try {
            return ProductType.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(
                    "Unsupported product type: " + value,
                    e
            );
        }
    }

    private TransactionType parseTransactionType(String value) {
        try {
            return TransactionType.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(
                    "Unsupported transaction type: " + value,
                    e
            );
        }
    }

    /**
     * Константа должна быть неотрицательным целым числом
     * в диапазоне Integer.
     */
    private int parseThreshold(String value) {
        if (value == null || !value.matches("\\d+")) {
            throw new IllegalArgumentException(
                    "Threshold must be a non-negative integer: " + value
            );
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Threshold exceeds the maximum integer value: " + value,
                    e
            );
        }
    }

    private void validateArguments(
            DynamicRuleCondition condition,
            int expectedCount
    ) {
        if (condition.arguments().size() != expectedCount) {
            throw new IllegalArgumentException(
                    "Query " + condition.query()
                            + " requires " + expectedCount
                            + " arguments, but received "
                            + condition.arguments().size()
            );
        }
    }
}