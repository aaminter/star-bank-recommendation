package com.aaminter.starbankrecommendation;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Path;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SourceDataSourceReadOnlyTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldRejectWritesWhenSourceDataSourceIsReadOnly() throws Exception {
        String databasePath = temporaryDirectory.resolve("source-db")
                .toString()
                .replace("\\", "/");
        String writableUrl = "jdbc:h2:file:" + databasePath;
        String readOnlyUrl = writableUrl + ";ACCESS_MODE_DATA=r;IFEXISTS=TRUE";

        // Prepare the source database before opening it in read-only mode.
        try (var connection = DriverManager.getConnection(writableUrl, "sa", "");
             var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE SOURCE_DATA (
                        ID BIGINT PRIMARY KEY,
                        VALUE VARCHAR(100)
                    )
                    """);
        }

        try (HikariDataSource dataSource = new HikariDataSource()) {
            dataSource.setJdbcUrl(readOnlyUrl);
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            dataSource.setReadOnly(true);

            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            assertThrows(DataAccessException.class, () ->
                    jdbcTemplate.update(
                            "INSERT INTO SOURCE_DATA (ID, VALUE) VALUES (?, ?)",
                            1L,
                            "must not be stored"
                    )
            );

            assertEquals(
                    0,
                    jdbcTemplate.queryForObject(
                            "SELECT COUNT(*) FROM SOURCE_DATA",
                            Integer.class
                    )
            );
        }
    }
}
