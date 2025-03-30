package com.example.dbcomparator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.dbcomparator.model.DatabaseObject;
import com.example.dbcomparator.model.oracle.OracleObject;
import com.example.dbcomparator.model.postgres.PostgresObject;
import com.example.dbcomparator.repository.oracle.OracleMetadataRepository;
import com.example.dbcomparator.repository.postgres.PostgresMetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for comparing database objects between Oracle and PostgreSQL
 */
@Service
@Slf4j
public class DatabaseComparisonService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseComparisonService.class);

    private final OracleMetadataRepository oracleRepository;
    private final PostgresMetadataRepository postgresRepository;
    private final JdbcTemplate oracleJdbcTemplate;
    private final JdbcTemplate supabaseJdbcTemplate;

    @Autowired
    public DatabaseComparisonService(OracleMetadataRepository oracleRepository,
                                     PostgresMetadataRepository postgresRepository,
                                     @Qualifier("oracleJdbcTemplate") JdbcTemplate oracleJdbcTemplate,
                                     @Qualifier("supabaseJdbcTemplate") JdbcTemplate supabaseJdbcTemplate) {
        this.oracleRepository = oracleRepository;
        this.postgresRepository = postgresRepository;
        this.oracleJdbcTemplate = oracleJdbcTemplate;
        this.supabaseJdbcTemplate = supabaseJdbcTemplate;
    }

    /**
     * Checks connectivity to both Oracle and PostgreSQL databases.
     * Throws an exception if either connection fails.
     */
    private void checkConnections() {
        log.info("Checking database connections...");
        try {
            oracleJdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class);
            log.info("Oracle connection successful.");
        } catch (DataAccessException e) {
            log.error("Oracle connection failed: {}", e.getMessage());
            throw new RuntimeException("Failed to connect to Oracle database.", e);
        }

        try {
            supabaseJdbcTemplate.queryForObject("SELECT 1", Integer.class);
            log.info("PostgreSQL connection successful.");
        } catch (DataAccessException e) {
            log.error("PostgreSQL connection failed: {}", e.getMessage());
            throw new RuntimeException("Failed to connect to PostgreSQL database.", e);
        }
        log.info("Database connections verified.");
    }

    /**
     * Compare database objects between Oracle and PostgreSQL
     *
     * @param oracleSchema  The Oracle schema name
     * @param postgresSchema The PostgreSQL schema name
     * @return Byte array containing the Excel report
     * @throws IOException If an error occurs during report generation
     */
    public byte[] generateComparisonReport(String oracleSchema, String postgresSchema) throws IOException {
        // 1. Check Connections
        checkConnections();

        // 2. Generate a unique ID for this comparison run
        UUID comparisonRunUuid = UUID.randomUUID();
        Timestamp runTimestamp = Timestamp.from(Instant.now());
        log.info("Starting comparison run ID: {}", comparisonRunUuid);

        // 3. Define comparison tasks
        Map<String, ComparisonTask> tasks = defineComparisonTasks(oracleSchema, postgresSchema);

        // 4. Execute comparisons, persist results, and prepare data for Excel
        Map<String, ComparisonResult> comparisonResults = new HashMap<>();
        for (Map.Entry<String, ComparisonTask> entry : tasks.entrySet()) {
            String objectType = entry.getKey();
            ComparisonTask task = entry.getValue();
            log.info("Comparing {}...", objectType);

            List<? extends DatabaseObject> oracleList = task.oracleFetcher.apply(oracleSchema);
            List<? extends DatabaseObject> postgresList = task.postgresFetcher.apply(postgresSchema);
            log.info("Found {} {} in Oracle, {} in PostgreSQL", oracleList.size(), objectType, postgresList.size());

            ComparisonResult result = compareObjectLists(oracleList, postgresList);
            comparisonResults.put(objectType, result);

            // Persist differences
            persistDifferences(comparisonRunUuid, runTimestamp, objectType, result.getOnlyInOracle(), "Only in Oracle", "Oracle");
            persistDifferences(comparisonRunUuid, runTimestamp, objectType, result.getOnlyInPostgres(), "Only in PostgreSQL", "PostgreSQL");
        }

        // 5. Generate Excel Report
        return createExcelReport(comparisonResults, tasks);
    }

    /**
     * Creates the multi-sheet Excel report from the comparison results.
     */
    private byte[] createExcelReport(Map<String, ComparisonResult> comparisonResults, Map<String, ComparisonTask> tasks) throws IOException {
         try (Workbook workbook = new XSSFWorkbook()) {
             // Create cell styles for headers
             CellStyle headerStyle = workbook.createCellStyle();
             headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
             headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
             Font headerFont = workbook.createFont();
             headerFont.setBold(true);
             headerStyle.setFont(headerFont);

             // Create sheets for each comparison type
             for (Map.Entry<String, ComparisonTask> taskEntry : tasks.entrySet()) {
                 String objectType = taskEntry.getKey();
                 String sheetName = taskEntry.getValue().sheetName;
                 ComparisonResult result = comparisonResults.get(objectType);
                 if (result != null) { // Ensure result exists before creating sheet
                    createDifferenceSheet(workbook, sheetName, result, headerStyle);
                 } else {
                    log.warn("No comparison result found for object type: {}", objectType);
                 }
             }

             // Write the workbook to a byte array
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             workbook.write(outputStream);
             log.info("Excel report generated successfully.");
             return outputStream.toByteArray();
         } catch (IOException e) {
             log.error("Error generating Excel report", e);
             throw e;
         }
     }


    /**
     * Helper method to create a single sheet showing differences for a specific object type.
     */
    private void createDifferenceSheet(Workbook workbook, String sheetName, ComparisonResult result, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet(sheetName);

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Name", "Type", "Schema", "Status"}; // Added Status column

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Create data rows
        int rowNum = 1;
        // Add objects only in Oracle
        for (DatabaseObject obj : result.getOnlyInOracle()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(obj.getName());
            row.createCell(1).setCellValue(obj.getType());
            row.createCell(2).setCellValue(obj.getSchema());
            row.createCell(3).setCellValue("Only in Oracle");
        }
        // Add objects only in PostgreSQL
        for (DatabaseObject obj : result.getOnlyInPostgres()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(obj.getName());
            row.createCell(1).setCellValue(obj.getType());
            row.createCell(2).setCellValue(obj.getSchema());
            row.createCell(3).setCellValue("Only in PostgreSQL");
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Compares two lists of DatabaseObjects based on name and type (case-insensitive).
     *
     * @param oracleList   List of objects from Oracle.
     * @param postgresList List of objects from PostgreSQL.
     * @return A ComparisonResult containing lists of objects unique to each database.
     */
    private ComparisonResult compareObjectLists(List<? extends DatabaseObject> oracleList, List<? extends DatabaseObject> postgresList) {
        Set<String> postgresNames = postgresList.stream()
                .map(obj -> obj.getName().toLowerCase()) // Compare names case-insensitively
                .collect(Collectors.toSet());

        Set<String> oracleNames = oracleList.stream()
                .map(obj -> obj.getName().toLowerCase()) // Compare names case-insensitively
                .collect(Collectors.toSet());

        List<DatabaseObject> missingInPostgres = oracleList.stream()
                .filter(oracle -> !postgresNames.contains(oracle.getName().toLowerCase()))
                .collect(Collectors.toList());

        List<DatabaseObject> missingInOracle = postgresList.stream()
                .filter(postgres -> !oracleNames.contains(postgres.getName().toLowerCase()))
                .collect(Collectors.toList());

        return new ComparisonResult(missingInPostgres, missingInOracle);
    }

    /**
     * Persists the differences found for a specific object type to the comparison_results table.
     */
    private void persistDifferences(UUID comparisonRunUuid, Timestamp runTimestamp, String objectType,
                                    List<DatabaseObject> differences, String status, String sourceDb) {
        if (differences.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO comparison_results (comparison_run_uuid, run_timestamp, object_type, object_name, schema_name, status, source_db) VALUES (?, ?, ?, ?, ?, ?, ?)";

        List<Object[]> batchArgs = differences.stream()
                .map(obj -> new Object[]{
                        comparisonRunUuid,
                        runTimestamp,
                        objectType, // Use the overall object type category
                        obj.getName(),
                        obj.getSchema(),
                        status,
                        sourceDb
                })
                .collect(Collectors.toList());

        try {
            supabaseJdbcTemplate.batchUpdate(sql, batchArgs);
            log.info("Persisted {} differences for type '{}' with status '{}'", differences.size(), objectType, status);
        } catch (DataAccessException e) {
            log.error("Failed to persist differences for type '{}' with status '{}': {}", objectType, status, e.getMessage());
            // Decide if you want to throw an exception or just log the error
            // throw new RuntimeException("Failed to persist comparison results.", e);
        }
    }

    /**
     * Defines the tasks for comparing different object types.
     * Each task includes the fetchers and the desired Excel sheet name.
     */
    private Map<String, ComparisonTask> defineComparisonTasks(String oracleSchema, String postgresSchema) {
        Map<String, ComparisonTask> tasks = new LinkedHashMap<>(); // Use LinkedHashMap to maintain order

        // Define fetchers using lambda expressions referencing repository methods
        Function<String, List<OracleObject>> oracleAllFetcher = owner -> oracleRepository.findAllObjectsByOwner(owner);
        Function<String, List<PostgresObject>> postgresAllFetcher = schema -> postgresRepository.findAllObjectsBySchema(schema);
        Function<String, List<OracleObject>> oracleTableFetcher = owner -> oracleRepository.findAllTablesByOwner(owner);
        Function<String, List<PostgresObject>> postgresTableFetcher = schema -> postgresRepository.findAllTablesBySchema(schema);
        Function<String, List<OracleObject>> oracleViewFetcher = owner -> oracleRepository.findAllViewsByOwner(owner);
        Function<String, List<PostgresObject>> postgresViewFetcher = schema -> postgresRepository.findAllViewsBySchema(schema);
        Function<String, List<OracleObject>> oracleProcedureFetcher = owner -> oracleRepository.findAllProceduresByOwner(owner);
        Function<String, List<PostgresObject>> postgresProcedureFetcher = schema -> postgresRepository.findAllProceduresBySchema(schema);
        Function<String, List<OracleObject>> oracleFunctionFetcher = owner -> oracleRepository.findAllFunctionsByOwner(owner);
        Function<String, List<PostgresObject>> postgresFunctionFetcher = schema -> postgresRepository.findAllFunctionsBySchema(schema);
        Function<String, List<OracleObject>> oracleSequenceFetcher = owner -> oracleRepository.findAllSequencesByOwner(owner);
        Function<String, List<PostgresObject>> postgresSequenceFetcher = schema -> postgresRepository.findAllSequencesBySchema(schema);
        Function<String, List<OracleObject>> oracleConstraintFetcher = owner -> oracleRepository.findAllConstraintsByOwner(owner);
        Function<String, List<PostgresObject>> postgresConstraintFetcher = schema -> postgresRepository.findAllConstraintsBySchema(schema);
        Function<String, List<OracleObject>> oracleIndexFetcher = owner -> oracleRepository.findAllIndexesByOwner(owner);
        Function<String, List<PostgresObject>> postgresIndexFetcher = schema -> postgresRepository.findAllIndexesBySchema(schema);


        // Explicitly cast fetchers to the expected type for the constructor
        tasks.put("ALL_OBJECTS", new ComparisonTask("Object Comparison",
                (Function<String, List<? extends DatabaseObject>>) (Function<?, ?>) oracleAllFetcher,
                (Function<String, List<? extends DatabaseObject>>) (Function<?, ?>) postgresAllFetcher));
        tasks.put("TABLE", new ComparisonTask("Table Comparison",
                (Function<String, List<? extends DatabaseObject>>) (Function<?, ?>) oracleTableFetcher,
                (Function<String, List<? extends DatabaseObject>>) (Function<?, ?>) postgresTableFetcher));
        tasks.put("VIEW", new ComparisonTask("View Comparison",
                (Function<String, List<? extends DatabaseObject>>) (Function<?, ?>) oracleViewFetcher,
                (Function<String, List<? extends DatabaseObject>>) (Function<?, ?>) postgresViewFetcher));
        tasks.put("PROCEDURE", new ComparisonTask("Procedure Comparison",
                (Function<String, List<? extends DatabaseObject>>) (Function<?, ?>) oracleProcedureFetcher,
                (Function<String, List<? extends DatabaseObject>>) (Function<?, ?>) postgresProcedureFetcher));
        tasks.put("FUNCTION", new ComparisonTask("Function Comparison",
                (Function<String, List<? extends DatabaseObject>>) (Function<?, ?>) oracleFunctionFetcher,
                (Function<String, List<? extends DatabaseObject>>) (Function<?, ?>) postgresFunctionFetcher));
        tasks.put("SEQUENCE", new ComparisonTask("Sequence Comparison",
                (Function<String, List<? extends DatabaseObject>>) (Function<?, ?>) oracleSequenceFetcher,
                (Function<String, List<? extends DatabaseObject>>) (Function<?, ?>) postgresSequenceFetcher));
        tasks.put("CONSTRAINT", new ComparisonTask("Constraint Comparison",
                (Function<String, List<? extends DatabaseObject>>) (Function<?, ?>) oracleConstraintFetcher,
                (Function<String, List<? extends DatabaseObject>>) (Function<?, ?>) postgresConstraintFetcher));
        tasks.put("INDEX", new ComparisonTask("Index Comparison",
                (Function<String, List<? extends DatabaseObject>>) (Function<?, ?>) oracleIndexFetcher,
                (Function<String, List<? extends DatabaseObject>>) (Function<?, ?>) postgresIndexFetcher));


        return tasks;
    }

    // --- Helper Classes ---

    /**
     * Holds the results of comparing two lists of objects.
     */
    @lombok.Getter
    @lombok.RequiredArgsConstructor
    private static class ComparisonResult {
        private final List<DatabaseObject> onlyInOracle;
        private final List<DatabaseObject> onlyInPostgres;
    }

    /**
     * Represents a comparison task for a specific object type.
     */
    // @lombok.RequiredArgsConstructor // Explicit constructor added below
    private static class ComparisonTask {
        final String sheetName;
        final Function<String, List<? extends DatabaseObject>> oracleFetcher;
        final Function<String, List<? extends DatabaseObject>> postgresFetcher;

        // Explicit constructor to avoid potential Lombok issues
        public ComparisonTask(String sheetName,
                              Function<String, List<? extends DatabaseObject>> oracleFetcher,
                              Function<String, List<? extends DatabaseObject>> postgresFetcher) {
            this.sheetName = sheetName;
            this.oracleFetcher = oracleFetcher;
            this.postgresFetcher = postgresFetcher;
        }
    }
}
