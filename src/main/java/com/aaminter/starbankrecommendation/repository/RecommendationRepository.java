package com.aaminter.starbankrecommendation.repository;

import com.aaminter.starbankrecommendation.model.CustomerMetrics;
import com.aaminter.starbankrecommendation.model.ProductType;
import com.aaminter.starbankrecommendation.model.ProductTypeMetrics;
import com.aaminter.starbankrecommendation.service.cache.CacheClearable;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Loads aggregated customer transaction metrics from the source database.
 *
 * <p>Results are cached by customer UUID in a local Caffeine cache with a
 * maximum of 10,000 entries and a 10-minute expire-after-write policy.
 * The cache can be invalidated through {@link #clearCache()}.</p>
 */
@Repository
public class RecommendationRepository implements CacheClearable {

    private static final String FIND_CUSTOMER_METRICS_SQL = """
            SELECT
                COUNT(CASE WHEN p.TYPE = 'DEBIT' THEN 1 END)
                    AS DEBIT_COUNT,
                COUNT(CASE WHEN p.TYPE = 'CREDIT' THEN 1 END)
                    AS CREDIT_COUNT,
                COUNT(CASE WHEN p.TYPE = 'INVEST' THEN 1 END)
                    AS INVEST_COUNT,
                COUNT(CASE WHEN p.TYPE = 'SAVING' THEN 1 END)
                    AS SAVING_COUNT,

                COALESCE(SUM(CASE
                    WHEN p.TYPE = 'DEBIT' AND t.TYPE = 'DEPOSIT'
                    THEN t.AMOUNT ELSE 0 END), 0) AS DEBIT_DEPOSITS,

                COALESCE(SUM(CASE
                    WHEN p.TYPE = 'DEBIT' AND t.TYPE = 'WITHDRAW'
                    THEN t.AMOUNT ELSE 0 END), 0) AS DEBIT_WITHDRAWALS,

                COALESCE(SUM(CASE
                    WHEN p.TYPE = 'CREDIT' AND t.TYPE = 'DEPOSIT'
                    THEN t.AMOUNT ELSE 0 END), 0) AS CREDIT_DEPOSITS,

                COALESCE(SUM(CASE
                    WHEN p.TYPE = 'CREDIT' AND t.TYPE = 'WITHDRAW'
                    THEN t.AMOUNT ELSE 0 END), 0) AS CREDIT_WITHDRAWALS,

                COALESCE(SUM(CASE
                    WHEN p.TYPE = 'INVEST' AND t.TYPE = 'DEPOSIT'
                    THEN t.AMOUNT ELSE 0 END), 0) AS INVEST_DEPOSITS,

                COALESCE(SUM(CASE
                    WHEN p.TYPE = 'INVEST' AND t.TYPE = 'WITHDRAW'
                    THEN t.AMOUNT ELSE 0 END), 0) AS INVEST_WITHDRAWALS,

                COALESCE(SUM(CASE
                    WHEN p.TYPE = 'SAVING' AND t.TYPE = 'DEPOSIT'
                    THEN t.AMOUNT ELSE 0 END), 0) AS SAVING_DEPOSITS,

                COALESCE(SUM(CASE
                    WHEN p.TYPE = 'SAVING' AND t.TYPE = 'WITHDRAW'
                    THEN t.AMOUNT ELSE 0 END), 0) AS SAVING_WITHDRAWALS

            FROM TRANSACTIONS t
            JOIN PRODUCTS p ON p.ID = t.PRODUCT_ID
            WHERE t.USER_ID = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    private final Cache<UUID, CustomerMetrics> customerMetricsCache =
            Caffeine.newBuilder()
                    .maximumSize(10_000)
                    .expireAfterWrite(Duration.ofMinutes(10))
                    .build();

    /**
     * Creates the repository using the JDBC template for source database queries.
     *
     * @param jdbcTemplate JDBC access to the source customer database
     */
    public RecommendationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Returns aggregated metrics for a customer, using the local cache when possible.
     *
     * <p>A cache miss triggers one aggregate query for transaction counts,
     * deposit totals and withdrawal totals across the customer's product types.</p>
     *
     * @param userId UUID of the customer
     * @return aggregated metrics, or empty metrics when the query returns no row
     * @throws NullPointerException if {@code userId} is null
     */
    public CustomerMetrics getCustomerMetrics(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");

        return customerMetricsCache.get(
                userId,
                this::loadCustomerMetrics
        );
    }

    private CustomerMetrics loadCustomerMetrics(UUID userId) {
        List<CustomerMetrics> results = jdbcTemplate.query(
                FIND_CUSTOMER_METRICS_SQL,
                (rs, rowNum) -> {
                    ProductTypeMetrics debit = new ProductTypeMetrics(
                            rs.getLong("DEBIT_COUNT"),
                            rs.getBigDecimal("DEBIT_DEPOSITS"),
                            rs.getBigDecimal("DEBIT_WITHDRAWALS")
                    );

                    ProductTypeMetrics credit = new ProductTypeMetrics(
                            rs.getLong("CREDIT_COUNT"),
                            rs.getBigDecimal("CREDIT_DEPOSITS"),
                            rs.getBigDecimal("CREDIT_WITHDRAWALS")
                    );

                    ProductTypeMetrics invest = new ProductTypeMetrics(
                            rs.getLong("INVEST_COUNT"),
                            rs.getBigDecimal("INVEST_DEPOSITS"),
                            rs.getBigDecimal("INVEST_WITHDRAWALS")
                    );

                    ProductTypeMetrics saving = new ProductTypeMetrics(
                            rs.getLong("SAVING_COUNT"),
                            rs.getBigDecimal("SAVING_DEPOSITS"),
                            rs.getBigDecimal("SAVING_WITHDRAWALS")
                    );

                    Map<ProductType, ProductTypeMetrics> metrics = Map.of(
                            ProductType.DEBIT, debit,
                            ProductType.CREDIT, credit,
                            ProductType.INVEST, invest,
                            ProductType.SAVING, saving
                    );

                    return new CustomerMetrics(
                            debit.transactionCount() > 0,
                            invest.transactionCount() > 0,
                            credit.transactionCount() > 0,
                            saving.depositSum().longValue(),
                            debit.depositSum().longValue(),
                            debit.withdrawSum().longValue(),
                            metrics
                    );
                },
                userId
        );

        return results.stream()
                .findFirst()
                .orElseGet(CustomerMetrics::empty);
    }

    /**
     * Invalidates all cached customer metrics.
     *
     * <p>The next lookup for a customer reloads metrics from the source database.</p>
     */
    @Override
    public void clearCache() {
        customerMetricsCache.invalidateAll();
    }
}
