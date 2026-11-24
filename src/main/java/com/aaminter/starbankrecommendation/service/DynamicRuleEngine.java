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

/**
 * Evaluates dynamic rule conditions against aggregated customer metrics.
 *
 * <p>All conditions in a rule must match (AND semantics). The {@code negate}
 * flag is applied to each condition individually before the combined result
 * is determined.</p>
 */
@Service
public class DynamicRuleEngine {

    private final RecommendationRepository repository;

    /**
     * Creates the engine with the repository used to load customer metrics.
     *
     * @param repository repository providing aggregated customer metrics
     */
    public DynamicRuleEngine(RecommendationRepository repository) {
        this.repository = repository;
    }

    /**
     * Loads metrics for a customer and evaluates the supplied conditions.
     *
     * @param userId UUID of the customer whose metrics are evaluated
     * @param conditions conditions that form the dynamic rule
     * @return {@code true} only when the non-empty condition list matches
     * @throws NullPointerException if {@code userId} or {@code conditions} is null
     * @throws IllegalArgumentException if a condition type or argument is unsupported
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
     * Evaluates conditions against already loaded customer metrics.
     *
     * <p>Conditions are combined using AND. An individual condition is
     * inverted when its {@code negate} flag is true. An empty condition list
     * does not match.</p>
     *
     * @param metrics aggregated metrics used for evaluation
     * @param conditions conditions that form the dynamic rule
     * @return {@code true} if every condition matches after its optional negation
     * @throws NullPointerException if metrics, conditions, or a condition is null
     * @throws IllegalArgumentException if a query type, argument, or comparison operator is unsupported
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
     * Selects the evaluator associated with the condition's query type.
     *
     * @param metrics customer metrics used by the selected evaluator
     * @param condition condition containing the query type and arguments
     * @return result produced by the matching query evaluator
     * @throws NullPointerException if {@code condition} is null
     * @throws IllegalArgumentException if the query type is unsupported
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
     * Checks whether the customer has at least one transaction for a product type.
     *
     * @param metrics aggregated customer metrics
     * @param condition condition whose first argument is a product type
     * @return {@code true} when the customer has at least one transaction of that type
     * @throws IllegalArgumentException if the condition has an invalid argument count or product type
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
     * Checks whether the customer has at least five transactions for a product type.
     *
     * @param metrics aggregated customer metrics
     * @param condition condition whose first argument is a product type
     * @return {@code true} when the transaction count is at least five
     * @throws IllegalArgumentException if the condition has an invalid argument count or product type
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
     * Compares a product's deposit or withdrawal sum with a numeric threshold.
     *
     * <p>Arguments are the product type, transaction type, comparison operator,
     * and a non-negative integer threshold, in that order.</p>
     *
     * @param metrics aggregated customer metrics
     * @param condition condition containing the four comparison arguments
     * @return result of the requested comparison
     * @throws IllegalArgumentException if an argument, type, threshold, or operator is invalid
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
     * Compares the total deposits with the total withdrawals for a product type.
     *
     * @param metrics aggregated customer metrics
     * @param condition condition containing the product type and comparison operator
     * @return result of comparing the deposit and withdrawal totals
     * @throws IllegalArgumentException if the argument count, product type, or operator is invalid
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
     * Compares two decimal values without converting them to a less precise numeric type.
     *
     * @param left first value
     * @param operator supported comparison operator
     * @param right second value
     * @return result of applying the operator to the two values
     * @throws IllegalArgumentException if the operator is null or unsupported
     */
    private boolean compare(
            BigDecimal left,
            String operator,
            BigDecimal right
    ) {
        if (operator == null) {
            throw new IllegalArgumentException(
                    "Comparison operator must not be null"
            );
        }

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

    /**
     * Converts a condition argument to a supported product type.
     *
     * @param value product type name
     * @return parsed product type
     * @throws IllegalArgumentException if the value is null or is not a supported product type
     */
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

    /**
     * Converts a condition argument to a supported transaction type.
     *
     * @param value transaction type name
     * @return parsed transaction type
     * @throws IllegalArgumentException if the value is null or is not a supported transaction type
     */
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
     * Parses a non-negative integer threshold within the {@link Integer} range.
     *
     * @param value threshold supplied as a condition argument
     * @return parsed threshold
     * @throws IllegalArgumentException if the value is not a non-negative integer or exceeds the integer range
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

    /**
     * Ensures that a condition contains exactly the number of arguments required by its query.
     *
     * @param condition condition being validated
     * @param expectedCount required number of arguments
     * @throws IllegalArgumentException if the actual argument count differs from the expected count
     */
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