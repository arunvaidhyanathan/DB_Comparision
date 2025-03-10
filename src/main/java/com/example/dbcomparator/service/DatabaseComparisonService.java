package com.example.dbcomparator.service;

import com.example.dbcomparator.model.DatabaseObject;
import com.example.dbcomparator.model.oracle.OracleObject;
import com.example.dbcomparator.model.postgres.PostgresObject;
import com.example.dbcomparator.repository.oracle.OracleMetadataRepository;
import com.example.dbcomparator.repository.postgres.PostgresMetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for comparing database objects between Oracle and PostgreSQL
 */
@Service
@Slf4j
public class DatabaseComparisonService {

    @Autowired
    private OracleMetadataRepository oracleRepository;

    @Autowired
    private PostgresMetadataRepository postgresRepository;

    /**
     * Compare database objects between Oracle and PostgreSQL
     *
     * @param oracleSchema  The Oracle schema name
     * @param postgresSchema The PostgreSQL schema name
     * @return Map containing comparison results
     */
    public Map<String, List<? extends DatabaseObject>> compareSchemas(String oracleSchema, String postgresSchema) {
        log.info("Starting comparison between Oracle schema '{}' and PostgreSQL schema '{}'", oracleSchema, postgresSchema);
        
        // Fetch all objects from both databases
        List<OracleObject> oracleObjects = oracleRepository.findAllObjectsByOwner(oracleSchema);
        List<PostgresObject> postgresObjects = postgresRepository.findAllObjectsBySchema(postgresSchema);
        
        log.info("Found {} objects in Oracle schema", oracleObjects.size());
        log.info("Found {} objects in PostgreSQL schema", postgresObjects.size());

        // Find objects missing in PostgreSQL
        List<OracleObject> missingInPostgres = oracleObjects.stream()
                .filter(oracle -> postgresObjects.stream()
                        .noneMatch(postgres -> 
                            postgres.getName().equalsIgnoreCase(oracle.getName()) && 
                            postgres.getType().equalsIgnoreCase(oracle.getType())))
                .collect(Collectors.toList());

        // Find objects missing in Oracle
        List<PostgresObject> missingInOracle = postgresObjects.stream()
                .filter(postgres -> oracleObjects.stream()
                        .noneMatch(oracle -> 
                            oracle.getName().equalsIgnoreCase(postgres.getName()) && 
                            oracle.getType().equalsIgnoreCase(postgres.getType())))
                .collect(Collectors.toList());

        log.info("Found {} objects missing in PostgreSQL", missingInPostgres.size());
        log.info("Found {} objects missing in Oracle", missingInOracle.size());

        Map<String, List<? extends DatabaseObject>> result = new HashMap<>();
        result.put("oracleObjects", oracleObjects);
        result.put("postgresObjects", postgresObjects);
        result.put("missingInPostgres", missingInPostgres);
        result.put("missingInOracle", missingInOracle);
        
        return result;
    }

    /**
     * Generate an Excel report with the comparison results
     *
     * @param oracleSchema  The Oracle schema name
     * @param postgresSchema The PostgreSQL schema name
     * @return Byte array containing the Excel report
     * @throws IOException If an error occurs during report generation
     */
    public byte[] generateComparisonReport(String oracleSchema, String postgresSchema) throws IOException {
        Map<String, List<? extends DatabaseObject>> comparisonResults = compareSchemas(oracleSchema, postgresSchema);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create cell styles for headers
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // Create Oracle Objects sheet
            createObjectSheet(workbook, "Oracle Objects", comparisonResults.get("oracleObjects"), headerStyle);
            
            // Create PostgreSQL Objects sheet
            createObjectSheet(workbook, "PostgreSQL Objects", comparisonResults.get("postgresObjects"), headerStyle);
            
            // Create Missing in PostgreSQL sheet
            createObjectSheet(workbook, "Missing in PostgreSQL", comparisonResults.get("missingInPostgres"), headerStyle);
            
            // Create Missing in Oracle sheet
            createObjectSheet(workbook, "Missing in Oracle", comparisonResults.get("missingInOracle"), headerStyle);
            
            // Write the workbook to a byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    /**
     * Helper method to create a sheet with database objects
     *
     * @param workbook    The Excel workbook
     * @param sheetName   The name of the sheet
     * @param objects     The list of database objects
     * @param headerStyle The cell style for headers
     */
    private void createObjectSheet(Workbook workbook, String sheetName, List<? extends DatabaseObject> objects, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet(sheetName);
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Name", "Type", "Schema"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Create data rows
        int rowNum = 1;
        for (DatabaseObject obj : objects) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(obj.getName());
            row.createCell(1).setCellValue(obj.getType());
            row.createCell(2).setCellValue(obj.getSchema());
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}