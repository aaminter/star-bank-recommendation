package com.aaminter.starbankrecommendation.model;

import java.math.BigDecimal;

/**
 * Transaction metrics aggregated for a single product type.
 */
public record ProductTypeMetrics(
        long transactionCount,
        BigDecimal depositSum,
        BigDecimal withdrawSum
) {
    public ProductTypeMetrics {
        if (depositSum == null || withdrawSum == null) {
            throw new IllegalArgumentException(
                    "Transaction sums must not be null"
            );
        }
    }

    /**
     * Creates zero-valued transaction metrics.
     *
     * @return empty product type metrics
     */
    public static ProductTypeMetrics empty() {
        return new ProductTypeMetrics(
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

    /**
     * Returns the aggregate amount for the specified transaction type.
     *
     * @param type transaction type
     * @return deposit or withdrawal sum
     */
    public BigDecimal sum(TransactionType type) {
        return switch (type) {
            case DEPOSIT -> depositSum;
            case WITHDRAW -> withdrawSum;
        };
    }
}