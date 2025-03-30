package com.example.dbcomparator.controller;

import com.example.dbcomparator.service.DatabaseComparisonService;
import com.example.dbcomparator.service.DatabaseComparisonService;
// Removed unused imports
// import lombok.extern.slf4j.Slf4j; // Removed Slf4j import
import org.slf4j.Logger; // Added explicit logger import
import org.slf4j.LoggerFactory; // Added explicit logger import
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
// @Slf4j // Removed Slf4j annotation
public class ComparisonController {

    // Explicitly define the logger
    private static final Logger log = LoggerFactory.getLogger(ComparisonController.class);

    private final DatabaseComparisonService comparisonService;

    @Autowired
    public ComparisonController(DatabaseComparisonService comparisonService) {
        this.comparisonService = comparisonService;
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
            // Use standard XLSX MIME type
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment",
                    "database_comparison_" + oracleSchema + "_" + postgresSchema + ".xlsx"); // Use underscores for better compatibility

            return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.error("Error generating Excel report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating report.".getBytes());
        } catch (RuntimeException e) {
             // Catch potential connection errors from checkConnections()
             log.error("Error during comparison process (potentially connection issue): {}", e.getMessage(), e);
             return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(("Error during comparison: " + e.getMessage()).getBytes());
        }
    }
}
