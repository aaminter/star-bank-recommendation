package com.aaminter.starbankrecommendation.model;

import java.math.BigDecimal;

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

    public static ProductTypeMetrics empty() {
        return new ProductTypeMetrics(
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

    public BigDecimal sum(TransactionType type) {
        return switch (type) {
            case DEPOSIT -> depositSum;
            case WITHDRAW -> withdrawSum;
        };
    }
}