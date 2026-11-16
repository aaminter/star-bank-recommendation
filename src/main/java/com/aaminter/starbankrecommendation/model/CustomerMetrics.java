package com.aaminter.starbankrecommendation.model;

import java.util.Map;

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

    public ProductTypeMetrics forProduct(ProductType type) {
        return productMetrics.getOrDefault(
                type,
                ProductTypeMetrics.empty()
        );
    }

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