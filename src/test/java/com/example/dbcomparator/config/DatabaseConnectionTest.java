package com.example.dbcomparator.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles; // Optional: if you have test-specific properties

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
// @ActiveProfiles("test") // Uncomment if you have a specific test profile in application-test.properties
public class DatabaseConnectionTest {

    @Autowired
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    @Autowired
    @Qualifier("supabaseJdbcTemplate")
    private JdbcTemplate supabaseJdbcTemplate;

    @Test
    @DisplayName("Should connect to Oracle database successfully")
    void testOracleConnection() {
        assertDoesNotThrow(() -> {
            try {
                oracleJdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class);
                System.out.println("Successfully connected to Oracle.");
            } catch (DataAccessException e) {
                fail("Failed to connect to Oracle database: " + e.getMessage(), e);
            }
        }, "Database connection threw an unexpected exception.");
    }

    @Test
    @DisplayName("Should connect to PostgreSQL database successfully")
    void testPostgresConnection() {
        assertDoesNotThrow(() -> {
            try {
                supabaseJdbcTemplate.queryForObject("SELECT 1", Integer.class);
                System.out.println("Successfully connected to PostgreSQL.");
            } catch (DataAccessException e) {
                fail("Failed to connect to PostgreSQL database: " + e.getMessage(), e);
            }
        }, "Database connection threw an unexpected exception.");
    }
}
