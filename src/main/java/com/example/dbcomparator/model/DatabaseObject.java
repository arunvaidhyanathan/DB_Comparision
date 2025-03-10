package com.example.dbcomparator.model;

import lombok.Data;

/**
 * Base interface for database objects from both Oracle and PostgreSQL
 */
@Data
public class DatabaseObject {
    private String name;
    private String type;
    private String schema;
    
    // For comparison purposes
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        DatabaseObject that = (DatabaseObject) obj;
        
        if (!name.equalsIgnoreCase(that.name)) return false;
        return type.equalsIgnoreCase(that.type);
    }
    
    @Override
    public int hashCode() {
        int result = name.toLowerCase().hashCode();
        result = 31 * result + type.toLowerCase().hashCode();
        return result;
    }
}