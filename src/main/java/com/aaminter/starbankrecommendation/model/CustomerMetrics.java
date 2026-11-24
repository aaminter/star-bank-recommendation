package com.aaminter.starbankrecommendation.model;

import java.util.Map;

/**
 * Aggregated customer activity metrics used to evaluate recommendation rules.
 * The product metrics map is stored as an immutable snapshot.
 *
 * @param hasDebit whether the customer has a debit product
 * @param hasInvest whether the customer has an investment product
 * @param hasCredit whether the customer has a credit product
 * @param savingDeposits total savings deposits
 * @param debitDeposits total debit deposits
 * @param debitWithdrawals total debit withdrawals
 * @param productMetrics immutable metrics grouped by product type
 */
public record CustomerMetrics(
        boolean hasDebit,
        boolean hasInvest,
        boolean hasCredit,
        long savingDeposits,
        long debitDeposits,
        long debitWithdrawals,
        Map<ProductType, ProductTypeMetrics> productMetrics
) {
    public CustomerMetrics {
        productMetrics = productMetrics == null
                ? Map.of()
                : Map.copyOf(productMetrics);
    }

    // Совместимость с существующими фиксированными правилами и тестами.
    /**
     * Creates metrics without per-product aggregates for compatibility with fixed rules.
     *
     * @param hasDebit whether the customer has a debit product
     * @param hasInvest whether the customer has an investment product
     * @param hasCredit whether the customer has a credit product
     * @param savingDeposits total savings deposits
     * @param debitDeposits total debit deposits
     * @param debitWithdrawals total debit withdrawals
     */
    public CustomerMetrics(
            boolean hasDebit,
            boolean hasInvest,
            boolean hasCredit,
            long savingDeposits,
            long debitDeposits,
            long debitWithdrawals
    ) {
        this(
                hasDebit,
                hasInvest,
                hasCredit,
                savingDeposits,
                debitDeposits,
                debitWithdrawals,
                Map.of()
        );
    }

    /**
     * Returns metrics for the requested product type, or zero-valued metrics when absent.
     *
     * @param type product type to look up
     * @return metrics for the product type
     */
    public ProductTypeMetrics forProduct(ProductType type) {
        return productMetrics.getOrDefault(
                type,
                ProductTypeMetrics.empty()
        );
    }

    /**
     * Creates an instance with all metrics set to zero or false.
     *
     * @return empty customer metrics
     */
    public static CustomerMetrics empty() {
        return new CustomerMetrics(
                false,
                false,
                false,
                0,
                0,
                0,
                Map.of()
        );
    }
}