package com.aaminter.starbankrecommendation.repository;

import com.aaminter.starbankrecommendation.model.CustomerMetrics;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class RecommendationRepository {

    private static final String FIND_CUSTOMER_METRICS_SQL = """
            SELECT
                COUNT(CASE WHEN p.TYPE = 'DEBIT' THEN 1 END) > 0
                    AS HAS_DEBIT,

                COUNT(CASE WHEN p.TYPE = 'INVEST' THEN 1 END) > 0
                    AS HAS_INVEST,

                COUNT(CASE WHEN p.TYPE = 'CREDIT' THEN 1 END) > 0
                    AS HAS_CREDIT,

                COALESCE(SUM(
                    CASE
                        WHEN p.TYPE = 'SAVING'
                         AND t.TYPE = 'DEPOSIT'
                        THEN t.AMOUNT
                        ELSE 0
                    END
                ), 0) AS SAVING_DEPOSITS,

                COALESCE(SUM(
                    CASE
                        WHEN p.TYPE = 'DEBIT'
                         AND t.TYPE = 'DEPOSIT'
                        THEN t.AMOUNT
                        ELSE 0
                    END
                ), 0) AS DEBIT_DEPOSITS,

                COALESCE(SUM(
                    CASE
                        WHEN p.TYPE = 'DEBIT'
                         AND t.TYPE = 'WITHDRAW'
                        THEN t.AMOUNT
                        ELSE 0
                    END
                ), 0) AS DEBIT_WITHDRAWALS

            FROM TRANSACTIONS t
            JOIN PRODUCTS p ON p.ID = t.PRODUCT_ID
            WHERE t.USER_ID = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public RecommendationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public CustomerMetrics getCustomerMetrics(UUID userId) {
        List<CustomerMetrics> results = jdbcTemplate.query(
                FIND_CUSTOMER_METRICS_SQL,
                (resultSet, rowNum) -> new CustomerMetrics(
                        resultSet.getBoolean("HAS_DEBIT"),
                        resultSet.getBoolean("HAS_INVEST"),
                        resultSet.getBoolean("HAS_CREDIT"),
                        resultSet.getLong("SAVING_DEPOSITS"),
                        resultSet.getLong("DEBIT_DEPOSITS"),
                        resultSet.getLong("DEBIT_WITHDRAWALS")
                ),
                userId
        );

        return results.stream()
                .findFirst()
                .orElse(CustomerMetrics.empty());
    }
}