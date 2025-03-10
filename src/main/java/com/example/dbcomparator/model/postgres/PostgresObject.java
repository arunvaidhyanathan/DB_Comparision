package com.example.dbcomparator.model.postgres;

import com.example.dbcomparator.model.DatabaseObject;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a PostgreSQL database object
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostgresObject extends DatabaseObject {
    @Id
    private String id;
    
    private String schemaName;
    private String objectType;
    private String createdAt;
    private String updatedAt;
    
    // Additional PostgreSQL-specific properties can be added here
}