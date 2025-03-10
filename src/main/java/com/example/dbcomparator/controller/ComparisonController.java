package com.example.dbcomparator.controller;

import com.example.dbcomparator.model.DatabaseObject;
import com.example.dbcomparator.service.DatabaseComparisonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling database comparison requests
 */
@RestController
@RequestMapping("/api/compare")
@Slf4j
public class ComparisonController {

    @Autowired
    private DatabaseComparisonService comparisonService;

    /**
     * Compare database objects between Oracle and PostgreSQL
     *
     * @param oracleSchema   The Oracle schema name
     * @param postgresSchema The PostgreSQL schema name
     * @return Map containing comparison results
     */
    @GetMapping("/schemas")
    public ResponseEntity<Map<String, List<? extends DatabaseObject>>> compareSchemas(
            @RequestParam("oracleSchema") String oracleSchema,
            @RequestParam("postgresSchema") String postgresSchema) {
        
        log.info("Received request to compare Oracle schema '{}' with PostgreSQL schema '{}'", 
                oracleSchema, postgresSchema);
        
        Map<String, List<? extends DatabaseObject>> result = 
                comparisonService.compareSchemas(oracleSchema, postgresSchema);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Generate an Excel report with comparison results
     *
     * @param oracleSchema   The Oracle schema name
     * @param postgresSchema The PostgreSQL schema name
     * @return Excel file as byte array
     */
    @GetMapping("/report")
    public ResponseEntity<byte[]> generateReport(
            @RequestParam("oracleSchema") String oracleSchema,
            @RequestParam("postgresSchema") String postgresSchema) {
        
        log.info("Received request to generate report for Oracle schema '{}' and PostgreSQL schema '{}'", 
                oracleSchema, postgresSchema);
        
        try {
            byte[] reportBytes = comparisonService.generateComparisonReport(oracleSchema, postgresSchema);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                    "database-comparison-" + oracleSchema + "-" + postgresSchema + ".xlsx");
            
            return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("Error generating report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}