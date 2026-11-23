package com.aaminter.starbankrecommendation.repository;

import com.aaminter.starbankrecommendation.model.TelegramUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TelegramUserRepositoryTest {

    private JdbcTemplate jdbcTemplate;
    private TelegramUserRepository repository;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(
                "jdbc:h2:mem:telegram_user_test_"
                        + UUID.randomUUID().toString().replace("-", "")
                        + ";DB_CLOSE_DELAY=-1"
        );
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("""
                CREATE TABLE USERS (
                    ID UUID PRIMARY KEY,
                    USERNAME VARCHAR(255) NOT NULL,
                    FIRST_NAME VARCHAR(255) NOT NULL,
                    LAST_NAME VARCHAR(255) NOT NULL
                )
                """);

        repository = new TelegramUserRepository(jdbcTemplate);
    }

    @Test
    void findsUserByUsernameCaseInsensitively() {
        UUID id = insertUser("ivan123", "Иван", "Иванов");

        Optional<TelegramUser> result =
                repository.findUniqueByUsernameOrFullName("IVAN123");

        assertTrue(result.isPresent());
        assertEquals(id, result.get().id());
    }

    @Test
    void findsUserByFullName() {
        UUID id = insertUser("ivan123", "Иван", "Иванов");

        Optional<TelegramUser> result =
                repository.findUniqueByUsernameOrFullName("Иван Иванов");

        assertTrue(result.isPresent());
        assertEquals(id, result.get().id());
    }

    @Test
    void returnsEmptyWhenUserDoesNotExist() {
        assertTrue(
                repository.findUniqueByUsernameOrFullName("unknown").isEmpty()
        );
    }

    @Test
    void returnsEmptyWhenFullNameIsAmbiguous() {
        insertUser("ivan123", "Иван", "Иванов");
        insertUser("ivan456", "Иван", "Иванов");

        assertTrue(
                repository.findUniqueByUsernameOrFullName("Иван Иванов").isEmpty()
        );
    }

    private UUID insertUser(String username, String firstName, String lastName) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO USERS (ID, USERNAME, FIRST_NAME, LAST_NAME) VALUES (?, ?, ?, ?)",
                id, username, firstName, lastName
        );
        return id;
    }
}
