package com.aaminter.starbankrecommendation.repository;

import com.aaminter.starbankrecommendation.model.CustomerMetrics;
import com.aaminter.starbankrecommendation.model.ProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecommendationRepositoryTest {

    private JdbcTemplate jdbcTemplate;
    private RecommendationRepository repository;

    private UUID debitProductId;

    private static final UUID USER_ID =
            UUID.randomUUID();

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource =
                new DriverManagerDataSource();

        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(
                "jdbc:h2:mem:cache_test_"
                        + UUID.randomUUID().toString().replace("-", "")
                        + ";DB_CLOSE_DELAY=-1"
        );
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

        debitProductId = UUID.randomUUID();

        jdbcTemplate.update(
                "INSERT INTO PRODUCTS (ID, TYPE, NAME) VALUES (?, ?, ?)",
                debitProductId,
                "DEBIT",
                "Debit"
        );

        repository = new RecommendationRepository(jdbcTemplate);
    }

    @Test
    void shouldReuseCachedMetricsForSameUser() {
        insertTransaction(USER_ID, 10_000);

        CustomerMetrics first =
                repository.getCustomerMetrics(USER_ID);

        assertEquals(
                new BigDecimal("10000"),
                first.forProduct(ProductType.DEBIT).depositSum()
        );

        // Добавляем транзакцию после первого получения метрик.
        insertTransaction(USER_ID, 5_000);

        // Повторное получение должно использовать кеш.
        CustomerMetrics cached =
                repository.getCustomerMetrics(USER_ID);

        assertEquals(
                new BigDecimal("10000"),
                cached.forProduct(ProductType.DEBIT).depositSum()
        );

        // Очищаем кеш.
        repository.clearCache();

        // Теперь запрос должен получить актуальные данные из H2.
        CustomerMetrics refreshed =
                repository.getCustomerMetrics(USER_ID);

        assertEquals(
                new BigDecimal("15000"),
                refreshed.forProduct(ProductType.DEBIT).depositSum()
        );
    }

    @Test
    void shouldKeepSeparateCacheEntriesForDifferentUsers() {
        UUID firstUser = UUID.randomUUID();
        UUID secondUser = UUID.randomUUID();

        insertTransaction(firstUser, 1_000);
        insertTransaction(secondUser, 9_000);

        CustomerMetrics firstMetrics =
                repository.getCustomerMetrics(firstUser);

        CustomerMetrics secondMetrics =
                repository.getCustomerMetrics(secondUser);

        assertEquals(
                new BigDecimal("1000"),
                firstMetrics.forProduct(ProductType.DEBIT).depositSum()
        );

        assertEquals(
                new BigDecimal("9000"),
                secondMetrics.forProduct(ProductType.DEBIT).depositSum()
        );
    }

    private void insertTransaction(UUID userId, int amount) {
        jdbcTemplate.update(
                """
                INSERT INTO TRANSACTIONS
                (ID, PRODUCT_ID, USER_ID, TYPE, AMOUNT)
                VALUES (?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                debitProductId,
                userId,
                "DEPOSIT",
                amount
        );
    }
}