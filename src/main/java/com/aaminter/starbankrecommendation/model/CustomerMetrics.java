package com.aaminter.starbankrecommendation.model;

public record CustomerMetrics(
        boolean hasDebit,
        boolean hasInvest,
        boolean hasCredit,
        long savingDeposits,
        long debitDeposits,
        long debitWithdrawals
) {
    public static CustomerMetrics empty() {
        return new CustomerMetrics(
                false,
                false,
                false,
                0,
                0,
                0
        );
    }
}