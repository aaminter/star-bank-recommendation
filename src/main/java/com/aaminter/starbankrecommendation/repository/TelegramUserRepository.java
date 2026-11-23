package com.aaminter.starbankrecommendation.repository;

import com.aaminter.starbankrecommendation.model.TelegramUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Performs read-only lookups in the source customer database.
 */
@Repository
public class TelegramUserRepository {

    private static final String FIND_USER_SQL = """
            SELECT ID, USERNAME, FIRST_NAME, LAST_NAME
            FROM USERS
            WHERE LOWER(USERNAME) = LOWER(?)
               OR LOWER(FIRST_NAME || ' ' || LAST_NAME) = LOWER(?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public TelegramUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<TelegramUser> findUniqueByUsernameOrFullName(String value) {
        List<TelegramUser> users = jdbcTemplate.query(
                FIND_USER_SQL,
                (rs, rowNum) -> new TelegramUser(
                        rs.getObject("ID", java.util.UUID.class),
                        rs.getString("USERNAME"),
                        rs.getString("FIRST_NAME"),
                        rs.getString("LAST_NAME")
                ),
                value,
                value
        );

        return users.size() == 1
                ? Optional.of(users.get(0))
                : Optional.empty();
    }
}
