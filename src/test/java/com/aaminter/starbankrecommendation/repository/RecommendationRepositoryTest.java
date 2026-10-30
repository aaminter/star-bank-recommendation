package com.aaminter.starbankrecommendation.repository;

import com.aaminter.starbankrecommendation.model.CustomerMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.UUID;

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

    @BeforeEach
    void setUp() {
        String databaseName = "recommendation_test_"
                + UUID.randomUUID().toString().replace("-", "");

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.execute("""
                CREATE TABLE PRODUCTS (
                    ID UUID PRIMARY KEY,
                    TYPE VARCHAR(255) NOT NULL,
                    NAME VARCHAR(255) NOT NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE TRANSACTIONS (
                    ID UUID PRIMARY KEY,
                    PRODUCT_ID UUID NOT NULL,
                    USER_ID UUID NOT NULL,
                    TYPE VARCHAR(255) NOT NULL,
                    AMOUNT INTEGER NOT NULL
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
        UUID userId = UUID.randomUUID();

        insertTransaction(debitProductId, userId, "DEPOSIT", 100_000);
        insertTransaction(debitProductId, userId, "WITHDRAW", 40_000);
        insertTransaction(savingProductId, userId, "DEPOSIT", 1_200);
        insertTransaction(investProductId, userId, "WITHDRAW", 5_000);

        CustomerMetrics metrics = repository.getCustomerMetrics(userId);

        assertTrue(metrics.hasDebit());
        assertTrue(metrics.hasInvest());
        assertFalse(metrics.hasCredit());
        assertEquals(1_200, metrics.savingDeposits());
        assertEquals(100_000, metrics.debitDeposits());
        assertEquals(40_000, metrics.debitWithdrawals());
    }

    @Test
    void shouldReturnZeroMetricsWhenUserHasNoTransactions() {
        UUID userIdWithoutTransactions = UUID.randomUUID();

        CustomerMetrics metrics =
                repository.getCustomerMetrics(userIdWithoutTransactions);

        assertFalse(metrics.hasDebit());
        assertFalse(metrics.hasInvest());
        assertFalse(metrics.hasCredit());
        assertEquals(0, metrics.savingDeposits());
        assertEquals(0, metrics.debitDeposits());
        assertEquals(0, metrics.debitWithdrawals());
    }

    @Test
    void shouldDetectCreditProductWhenTransactionExists() {
        UUID userId = UUID.randomUUID();

        insertTransaction(creditProductId, userId, "WITHDRAW", 100);

        CustomerMetrics metrics = repository.getCustomerMetrics(userId);

        assertTrue(metrics.hasCredit());
        assertFalse(metrics.hasDebit());
    }

    @Test
    void shouldSeparateDepositsAndWithdrawalsByProductType() {
        UUID userId = UUID.randomUUID();

        insertTransaction(savingProductId, userId, "DEPOSIT", 1_200);
        insertTransaction(savingProductId, userId, "WITHDRAW", 900);
        insertTransaction(debitProductId, userId, "DEPOSIT", 300);
        insertTransaction(debitProductId, userId, "WITHDRAW", 100);

        CustomerMetrics metrics = repository.getCustomerMetrics(userId);

        assertEquals(1_200, metrics.savingDeposits());
        assertEquals(300, metrics.debitDeposits());
        assertEquals(100, metrics.debitWithdrawals());
    }

    @Test
    void shouldIgnoreTransactionsOfOtherUsers() {
        UUID requestedUserId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();

        insertTransaction(debitProductId, requestedUserId, "DEPOSIT", 1_000);
        insertTransaction(debitProductId, anotherUserId, "DEPOSIT", 100_000);
        insertTransaction(debitProductId, anotherUserId, "WITHDRAW", 50_000);

        CustomerMetrics metrics =
                repository.getCustomerMetrics(requestedUserId);

        assertTrue(metrics.hasDebit());
        assertEquals(1_000, metrics.debitDeposits());
        assertEquals(0, metrics.debitWithdrawals());
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
                INSERT INTO TRANSACTIONS (ID, PRODUCT_ID, USER_ID, TYPE, AMOUNT)
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