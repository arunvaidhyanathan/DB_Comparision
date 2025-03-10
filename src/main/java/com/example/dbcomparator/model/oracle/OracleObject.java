package com.example.dbcomparator.model.oracle;

import com.example.dbcomparator.model.DatabaseObject;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents an Oracle database object
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OracleObject extends DatabaseObject {
    @Id
    private String id;
    
    private String owner;
    private String status;
    private String created;
    private String lastDdlTime;
    
    // Additional Oracle-specific properties can be added here
}