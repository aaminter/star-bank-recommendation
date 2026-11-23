package com.aaminter.starbankrecommendation.repository;

import com.aaminter.starbankrecommendation.model.CustomerMetrics;
import com.aaminter.starbankrecommendation.model.ProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.github.benmanes.caffeine.cache.Ticker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationRepositoryTest {

    private JdbcTemplate jdbcTemplate;
    private RecommendationRepository repository;

    private UUID debitProductId;
    private UUID savingProductId;
    private UUID investProductId;
    private UUID creditProductId;

    private static final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource =
                new DriverManagerDataSource();

        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(
                "jdbc:h2:mem:recommendation_test_"
                        + UUID.randomUUID().toString().replace("-", "")
                        + ";DB_CLOSE_DELAY=-1"
        );
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.execute("""
                CREATE TABLE PRODUCTS (
                    ID UUID PRIMARY KEY,
                    TYPE VARCHAR(50),
                    NAME VARCHAR(100)
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE TRANSACTIONS (
                    ID UUID PRIMARY KEY,
                    PRODUCT_ID UUID,
                    USER_ID UUID,
                    TYPE VARCHAR(50),
                    AMOUNT DECIMAL(19, 2)
                )
                """);

        debitProductId = insertProduct("DEBIT", "Debit");
        savingProductId = insertProduct("SAVING", "Saving");
        investProductId = insertProduct("INVEST", "Invest");
        creditProductId = insertProduct("CREDIT", "Credit");

        repository = new RecommendationRepository(jdbcTemplate);
    }

    @Test
    void shouldCalculateMetricsByUserAndProductType() {
        insertTransaction(debitProductId, USER_ID, "DEPOSIT", 100_000);
        insertTransaction(debitProductId, USER_ID, "WITHDRAW", 40_000);
        insertTransaction(savingProductId, USER_ID, "DEPOSIT", 1_200);
        insertTransaction(investProductId, USER_ID, "WITHDRAW", 5_000);

        CustomerMetrics metrics = repository.getCustomerMetrics(USER_ID);

        assertTrue(metrics.hasDebit());
        assertTrue(metrics.hasInvest());
        assertFalse(metrics.hasCredit());

        assertEquals(1_200, metrics.savingDeposits());
        assertEquals(100_000, metrics.debitDeposits());
        assertEquals(40_000, metrics.debitWithdrawals());

        assertEquals(
                new BigDecimal("100000.00"),
                metrics.forProduct(ProductType.DEBIT).depositSum()
        );
        assertEquals(
                new BigDecimal("40000.00"),
                metrics.forProduct(ProductType.DEBIT).withdrawSum()
        );
        assertEquals(
                new BigDecimal("5000.00"),
                metrics.forProduct(ProductType.INVEST).withdrawSum()
        );
    }

    @Test
    void shouldReturnZeroMetricsWhenUserHasNoTransactions() {
        CustomerMetrics metrics =
                repository.getCustomerMetrics(UUID.randomUUID());

        assertFalse(metrics.hasDebit());
        assertFalse(metrics.hasInvest());
        assertFalse(metrics.hasCredit());

        assertEquals(0, metrics.savingDeposits());
        assertEquals(0, metrics.debitDeposits());
        assertEquals(0, metrics.debitWithdrawals());

        assertEquals(
                BigDecimal.ZERO,
                metrics.forProduct(ProductType.DEBIT).depositSum()
        );
    }

    @Test
    void shouldDetectCreditProductWhenTransactionExists() {
        insertTransaction(creditProductId, USER_ID, "WITHDRAW", 100);

        CustomerMetrics metrics = repository.getCustomerMetrics(USER_ID);

        assertTrue(metrics.hasCredit());
        assertFalse(metrics.hasDebit());
    }

    @Test
    void shouldSeparateDepositsAndWithdrawalsByProductType() {
        insertTransaction(savingProductId, USER_ID, "DEPOSIT", 1_200);
        insertTransaction(savingProductId, USER_ID, "WITHDRAW", 900);
        insertTransaction(debitProductId, USER_ID, "DEPOSIT", 300);
        insertTransaction(debitProductId, USER_ID, "WITHDRAW", 100);

        CustomerMetrics metrics = repository.getCustomerMetrics(USER_ID);

        assertEquals(
                new BigDecimal("1200.00"),
                metrics.forProduct(ProductType.SAVING).depositSum()
        );
        assertEquals(
                new BigDecimal("900.00"),
                metrics.forProduct(ProductType.SAVING).withdrawSum()
        );
        assertEquals(
                new BigDecimal("300.00"),
                metrics.forProduct(ProductType.DEBIT).depositSum()
        );
        assertEquals(
                new BigDecimal("100.00"),
                metrics.forProduct(ProductType.DEBIT).withdrawSum()
        );
    }

    @Test
    void shouldIgnoreTransactionsOfOtherUsers() {
        UUID anotherUserId = UUID.randomUUID();

        insertTransaction(debitProductId, USER_ID, "DEPOSIT", 1_000);
        insertTransaction(debitProductId, anotherUserId, "DEPOSIT", 100_000);
        insertTransaction(debitProductId, anotherUserId, "WITHDRAW", 50_000);

        CustomerMetrics metrics = repository.getCustomerMetrics(USER_ID);

        assertTrue(metrics.hasDebit());
        assertEquals(1_000, metrics.debitDeposits());
        assertEquals(0, metrics.debitWithdrawals());
    }

    @Test
    void shouldReuseCachedMetricsForSameUser() {
        insertTransaction(debitProductId, USER_ID, "DEPOSIT", 10_000);

        CustomerMetrics first = repository.getCustomerMetrics(USER_ID);

        assertEquals(
                new BigDecimal("10000.00"),
                first.forProduct(ProductType.DEBIT).depositSum()
        );

        insertTransaction(debitProductId, USER_ID, "DEPOSIT", 5_000);

        CustomerMetrics cached = repository.getCustomerMetrics(USER_ID);

        assertEquals(
                new BigDecimal("10000.00"),
                cached.forProduct(ProductType.DEBIT).depositSum()
        );

        repository.clearCache();

        CustomerMetrics refreshed = repository.getCustomerMetrics(USER_ID);

        assertEquals(
                new BigDecimal("15000.00"),
                refreshed.forProduct(ProductType.DEBIT).depositSum()
        );
    }

    @Test
    void shouldKeepSeparateCacheEntriesForDifferentUsers() {
        UUID firstUser = UUID.randomUUID();
        UUID secondUser = UUID.randomUUID();

        insertTransaction(debitProductId, firstUser, "DEPOSIT", 1_000);
        insertTransaction(debitProductId, secondUser, "DEPOSIT", 9_000);

        CustomerMetrics firstMetrics =
                repository.getCustomerMetrics(firstUser);

        CustomerMetrics secondMetrics =
                repository.getCustomerMetrics(secondUser);

        assertEquals(
                new BigDecimal("1000.00"),
                firstMetrics.forProduct(ProductType.DEBIT).depositSum()
        );

        assertEquals(
                new BigDecimal("9000.00"),
                secondMetrics.forProduct(ProductType.DEBIT).depositSum()
        );
    }


    @Test
    void shouldReloadMetricsAfterCacheEntryExpires() {
        FakeTicker ticker = new FakeTicker();
        repository = new RecommendationRepository(jdbcTemplate, ticker);

        insertTransaction(debitProductId, USER_ID, "DEPOSIT", 10_000);

        CustomerMetrics initial = repository.getCustomerMetrics(USER_ID);
        assertEquals(
                new BigDecimal("10000.00"),
                initial.forProduct(ProductType.DEBIT).depositSum()
        );

        insertTransaction(debitProductId, USER_ID, "DEPOSIT", 5_000);

        CustomerMetrics cached = repository.getCustomerMetrics(USER_ID);
        assertEquals(
                new BigDecimal("10000.00"),
                cached.forProduct(ProductType.DEBIT).depositSum()
        );

        ticker.advance(Duration.ofMinutes(11));

        CustomerMetrics refreshed = repository.getCustomerMetrics(USER_ID);
        assertEquals(
                new BigDecimal("15000.00"),
                refreshed.forProduct(ProductType.DEBIT).depositSum()
        );
    }

    private static final class FakeTicker implements Ticker {

        private final AtomicLong nanos = new AtomicLong();

        @Override
        public long read() {
            return nanos.get();
        }

        void advance(Duration duration) {
            nanos.addAndGet(duration.toNanos());
        }
    }

    private UUID insertProduct(String type, String name) {
        UUID productId = UUID.randomUUID();

        jdbcTemplate.update(
                "INSERT INTO PRODUCTS (ID, TYPE, NAME) VALUES (?, ?, ?)",
                productId,
                type,
                name
        );

        return productId;
    }

    private void insertTransaction(
            UUID productId,
            UUID userId,
            String type,
            int amount
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO TRANSACTIONS
                (ID, PRODUCT_ID, USER_ID, TYPE, AMOUNT)
                VALUES (?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                productId,
                userId,
                type,
                amount
        );
    }
}
