package com.example.dbcomparator.service;

import com.example.dbcomparator.model.oracle.OracleObject;
import com.example.dbcomparator.model.postgres.PostgresObject;
import com.example.dbcomparator.repository.oracle.OracleMetadataRepository;
import com.example.dbcomparator.repository.postgres.PostgresMetadataRepository;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier; // Added import
import org.springframework.dao.DataAccessException; // Added import
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseComparisonServiceTest {

    @Mock
    private OracleMetadataRepository oracleRepository;

    @Mock
    private PostgresMetadataRepository postgresRepository;

    @Mock
    @Qualifier("oracleJdbcTemplate")
    private JdbcTemplate oracleJdbcTemplate;

    @Mock
    @Qualifier("supabaseJdbcTemplate")
    private JdbcTemplate supabaseJdbcTemplate;

    @InjectMocks // Automatically injects mocks into the service
    private DatabaseComparisonService comparisonService;

    @Captor
    private ArgumentCaptor<String> sqlCaptor;
    @Captor
    private ArgumentCaptor<List<Object[]>> batchArgsCaptor;

    private final String ORACLE_SCHEMA = "TEST_ORA";
    private final String POSTGRES_SCHEMA = "test_pg";

    @BeforeEach
    void setUp() {
        // Mock connection checks to always succeed
        when(oracleJdbcTemplate.queryForObject(eq("SELECT 1 FROM DUAL"), eq(Integer.class))).thenReturn(1);
        when(supabaseJdbcTemplate.queryForObject(eq("SELECT 1"), eq(Integer.class))).thenReturn(1);
    }

    private OracleObject createOracleObject(String name, String type) {
        OracleObject obj = new OracleObject();
        obj.setName(name);
        obj.setType(type);
        obj.setSchema(ORACLE_SCHEMA);
        return obj;
    }

    private PostgresObject createPostgresObject(String name, String type) {
        PostgresObject obj = new PostgresObject();
        obj.setName(name);
        obj.setType(type); // Use the mapped type
        obj.setSchema(POSTGRES_SCHEMA);
        obj.setObjectType(type); // Set objectType as well
        return obj;
    }

    @Test
    @DisplayName("Should generate report, persist differences, and call correct methods")
    void generateComparisonReport_Success() throws IOException {
        // --- Arrange ---
        // Mock repository responses
        OracleObject oraTable1 = createOracleObject("TABLE_A", "TABLE");
        OracleObject oraTableOnly = createOracleObject("TABLE_ORA_ONLY", "TABLE");
        PostgresObject pgTable1 = createPostgresObject("TABLE_A", "TABLE"); // Match name/type case-insensitively
        PostgresObject pgTableOnly = createPostgresObject("TABLE_PG_ONLY", "TABLE");

        when(oracleRepository.findAllTablesByOwner(ORACLE_SCHEMA)).thenReturn(Arrays.asList(oraTable1, oraTableOnly));
        when(postgresRepository.findAllTablesBySchema(POSTGRES_SCHEMA)).thenReturn(Arrays.asList(pgTable1, pgTableOnly));
        // Mock other repository methods to return empty lists for simplicity in this test
        when(oracleRepository.findAllObjectsByOwner(anyString())).thenReturn(Collections.emptyList());
        when(postgresRepository.findAllObjectsBySchema(anyString())).thenReturn(Collections.emptyList());
        when(oracleRepository.findAllViewsByOwner(anyString())).thenReturn(Collections.emptyList());
        when(postgresRepository.findAllViewsBySchema(anyString())).thenReturn(Collections.emptyList());
        when(oracleRepository.findAllProceduresByOwner(anyString())).thenReturn(Collections.emptyList());
        when(postgresRepository.findAllProceduresBySchema(anyString())).thenReturn(Collections.emptyList());
        when(oracleRepository.findAllFunctionsByOwner(anyString())).thenReturn(Collections.emptyList());
        when(postgresRepository.findAllFunctionsBySchema(anyString())).thenReturn(Collections.emptyList());
        when(oracleRepository.findAllSequencesByOwner(anyString())).thenReturn(Collections.emptyList());
        when(postgresRepository.findAllSequencesBySchema(anyString())).thenReturn(Collections.emptyList());
        when(oracleRepository.findAllConstraintsByOwner(anyString())).thenReturn(Collections.emptyList());
        when(postgresRepository.findAllConstraintsBySchema(anyString())).thenReturn(Collections.emptyList());
        when(oracleRepository.findAllIndexesByOwner(anyString())).thenReturn(Collections.emptyList());
        when(postgresRepository.findAllIndexesBySchema(anyString())).thenReturn(Collections.emptyList());


        // --- Act ---
        byte[] reportBytes = comparisonService.generateComparisonReport(ORACLE_SCHEMA, POSTGRES_SCHEMA);

        // --- Assert ---
        assertNotNull(reportBytes);
        assertTrue(reportBytes.length > 0, "Report byte array should not be empty");

        // Verify connection checks were called
        verify(oracleJdbcTemplate, times(1)).queryForObject(eq("SELECT 1 FROM DUAL"), eq(Integer.class));
        verify(supabaseJdbcTemplate, times(1)).queryForObject(eq("SELECT 1"), eq(Integer.class));

        // Verify repository methods were called (at least the table ones)
        verify(oracleRepository, times(1)).findAllTablesByOwner(ORACLE_SCHEMA);
        verify(postgresRepository, times(1)).findAllTablesBySchema(POSTGRES_SCHEMA);
        // Verify others were called too
        verify(oracleRepository, times(1)).findAllViewsByOwner(ORACLE_SCHEMA);
        verify(postgresRepository, times(1)).findAllViewsBySchema(POSTGRES_SCHEMA);
        // ... verify calls for all other object types ...
        verify(oracleRepository, times(1)).findAllIndexesByOwner(ORACLE_SCHEMA);
        verify(postgresRepository, times(1)).findAllIndexesBySchema(POSTGRES_SCHEMA);


        // Verify persistence calls (should be called twice for TABLE type: once for Oracle-only, once for PG-only)
        verify(supabaseJdbcTemplate, times(2)).batchUpdate(sqlCaptor.capture(), batchArgsCaptor.capture());

        // Examine the captured arguments for the TABLE persistence
        List<String> capturedSql = sqlCaptor.getAllValues();
        List<List<Object[]>> capturedArgs = batchArgsCaptor.getAllValues();

        // Check persistence for Oracle-only table
        boolean foundOraPersist = false;
        for (int i = 0; i < capturedSql.size(); i++) {
            if (capturedArgs.get(i).size() == 1 && capturedArgs.get(i).get(0)[2].equals("TABLE") && capturedArgs.get(i).get(0)[5].equals("Only in Oracle")) {
                assertEquals("TABLE_ORA_ONLY", capturedArgs.get(i).get(0)[3]); // Check name
                assertEquals(ORACLE_SCHEMA, capturedArgs.get(i).get(0)[4]); // Check schema
                assertEquals("Oracle", capturedArgs.get(i).get(0)[6]); // Check source DB
                foundOraPersist = true;
                break;
            }
        }
        assertTrue(foundOraPersist, "Persistence call for Oracle-only table not found or incorrect.");

         // Check persistence for PostgreSQL-only table
        boolean foundPgPersist = false;
        for (int i = 0; i < capturedSql.size(); i++) {
             if (capturedArgs.get(i).size() == 1 && capturedArgs.get(i).get(0)[2].equals("TABLE") && capturedArgs.get(i).get(0)[5].equals("Only in PostgreSQL")) {
                 assertEquals("TABLE_PG_ONLY", capturedArgs.get(i).get(0)[3]); // Check name
                 assertEquals(POSTGRES_SCHEMA, capturedArgs.get(i).get(0)[4]); // Check schema
                 assertEquals("PostgreSQL", capturedArgs.get(i).get(0)[6]); // Check source DB
                 foundPgPersist = true;
                 break;
             }
         }
        assertTrue(foundPgPersist, "Persistence call for PostgreSQL-only table not found or incorrect.");

    }

    @Test
    @DisplayName("Should throw exception if Oracle connection fails")
    void generateComparisonReport_OracleConnectionFails() {
        // Arrange
        reset(oracleJdbcTemplate); // Reset previous mock setup for this test
        // Use RuntimeException for mocking as DataAccessException is abstract
        when(oracleJdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
                .thenThrow(new RuntimeException("Simulated Oracle connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            comparisonService.generateComparisonReport(ORACLE_SCHEMA, POSTGRES_SCHEMA);
        });
        assertTrue(exception.getMessage().contains("Failed to connect to Oracle database"));
        verify(supabaseJdbcTemplate, never()).queryForObject(anyString(), eq(Integer.class)); // Ensure PG check wasn't even attempted
    }

     @Test
     @DisplayName("Should throw exception if PostgreSQL connection fails")
     void generateComparisonReport_PostgresConnectionFails() {
         // Arrange
         reset(supabaseJdbcTemplate); // Reset previous mock setup
         // Use RuntimeException for mocking as DataAccessException is abstract
         when(supabaseJdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
                 .thenThrow(new RuntimeException("Simulated PG connection error"));

         // Act & Assert
         RuntimeException exception = assertThrows(RuntimeException.class, () -> {
             comparisonService.generateComparisonReport(ORACLE_SCHEMA, POSTGRES_SCHEMA);
         });
         assertTrue(exception.getMessage().contains("Failed to connect to PostgreSQL database"));
         verify(oracleJdbcTemplate, times(1)).queryForObject(anyString(), eq(Integer.class)); // Oracle check should have passed
     }
}
