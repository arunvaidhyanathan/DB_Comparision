package com.example.dbcomparator.repository.oracle;

import com.example.dbcomparator.model.oracle.OracleObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for querying Oracle database metadata
 */
@Repository
public interface OracleMetadataRepository extends JpaRepository<OracleObject, String> {
    
    /**
     * Find all objects in a specific schema
     * 
     * @param owner The schema/owner name
     * @return List of database objects
     */
    @Query(nativeQuery = true, value = 
        "SELECT OBJECT_NAME as name, OBJECT_TYPE as type, OWNER as schema, " +
        "OWNER || '.' || OBJECT_NAME as id, " +
        "OWNER as owner, STATUS as status, " +
        "TO_CHAR(CREATED, 'YYYY-MM-DD HH24:MI:SS') as created, " +
        "TO_CHAR(LAST_DDL_TIME, 'YYYY-MM-DD HH24:MI:SS') as lastDdlTime " +
        "FROM ALL_OBJECTS " +
        "WHERE OWNER = :owner " +
        "ORDER BY OBJECT_TYPE, OBJECT_NAME")
    List<OracleObject> findAllObjectsByOwner(@Param("owner") String owner);
    
    /**
     * Find all tables in a specific schema
     * 
     * @param owner The schema/owner name
     * @return List of tables
     */
    @Query(nativeQuery = true, value = 
        "SELECT OBJECT_NAME as name, 'TABLE' as type, OWNER as schema, " +
        "OWNER || '.' || OBJECT_NAME as id, " +
        "OWNER as owner, STATUS as status, " +
        "TO_CHAR(CREATED, 'YYYY-MM-DD HH24:MI:SS') as created, " +
        "TO_CHAR(LAST_DDL_TIME, 'YYYY-MM-DD HH24:MI:SS') as lastDdlTime " +
        "FROM ALL_OBJECTS " +
        "WHERE OWNER = :owner AND OBJECT_TYPE = 'TABLE' " +
        "ORDER BY OBJECT_NAME")
    List<OracleObject> findAllTablesByOwner(@Param("owner") String owner);
    
    /**
     * Find all views in a specific schema
     * 
     * @param owner The schema/owner name
     * @return List of views
     */
    @Query(nativeQuery = true, value = 
        "SELECT OBJECT_NAME as name, 'VIEW' as type, OWNER as schema, " +
        "OWNER || '.' || OBJECT_NAME as id, " +
        "OWNER as owner, STATUS as status, " +
        "TO_CHAR(CREATED, 'YYYY-MM-DD HH24:MI:SS') as created, " +
        "TO_CHAR(LAST_DDL_TIME, 'YYYY-MM-DD HH24:MI:SS') as lastDdlTime " +
        "FROM ALL_OBJECTS " +
        "WHERE OWNER = :owner AND OBJECT_TYPE = 'VIEW' " +
        "ORDER BY OBJECT_NAME")
    List<OracleObject> findAllViewsByOwner(@Param("owner") String owner);
    
    /**
     * Find all procedures in a specific schema
     * 
     * @param owner The schema/owner name
     * @return List of procedures
     */
    @Query(nativeQuery = true, value = 
        "SELECT OBJECT_NAME as name, 'PROCEDURE' as type, OWNER as schema, " +
        "OWNER || '.' || OBJECT_NAME as id, " +
        "OWNER as owner, STATUS as status, " +
        "TO_CHAR(CREATED, 'YYYY-MM-DD HH24:MI:SS') as created, " +
        "TO_CHAR(LAST_DDL_TIME, 'YYYY-MM-DD HH24:MI:SS') as lastDdlTime " +
        "FROM ALL_OBJECTS " +
        "WHERE OWNER = :owner AND OBJECT_TYPE = 'PROCEDURE' " +
        "ORDER BY OBJECT_NAME")
    List<OracleObject> findAllProceduresByOwner(@Param("owner") String owner);
    
    /**
     * Find all functions in a specific schema
     * 
     * @param owner The schema/owner name
     * @return List of functions
     */
    @Query(nativeQuery = true, value = 
        "SELECT OBJECT_NAME as name, 'FUNCTION' as type, OWNER as schema, " +
        "OWNER || '.' || OBJECT_NAME as id, " +
        "OWNER as owner, STATUS as status, " +
        "TO_CHAR(CREATED, 'YYYY-MM-DD HH24:MI:SS') as created, " +
        "TO_CHAR(LAST_DDL_TIME, 'YYYY-MM-DD HH24:MI:SS') as lastDdlTime " +
        "FROM ALL_OBJECTS " +
        "WHERE OWNER = :owner AND OBJECT_TYPE = 'FUNCTION' " +
        "ORDER BY OBJECT_NAME")
    List<OracleObject> findAllFunctionsByOwner(@Param("owner") String owner);
    
    /**
     * Find all sequences in a specific schema
     * 
     * @param owner The schema/owner name
     * @return List of sequences
     */
    @Query(nativeQuery = true, value = 
        "SELECT OBJECT_NAME as name, 'SEQUENCE' as type, OWNER as schema, " +
        "OWNER || '.' || OBJECT_NAME as id, " +
        "OWNER as owner, STATUS as status, " +
        "TO_CHAR(CREATED, 'YYYY-MM-DD HH24:MI:SS') as created, " +
        "TO_CHAR(LAST_DDL_TIME, 'YYYY-MM-DD HH24:MI:SS') as lastDdlTime " +
        "FROM ALL_OBJECTS " +
        "WHERE OWNER = :owner AND OBJECT_TYPE = 'SEQUENCE' " +
        "ORDER BY OBJECT_NAME")
    List<OracleObject> findAllSequencesByOwner(@Param("owner") String owner);

    /**
     * Find all indexes in a specific schema
     *
     * @param owner The schema/owner name
     * @return List of indexes
     */
    @Query(nativeQuery = true, value =
        "SELECT INDEX_NAME as name, 'INDEX' as type, OWNER as schema, " +
        "OWNER || '.' || INDEX_NAME as id, " +
        "OWNER as owner, STATUS as status, " +
        "NULL as created, " + // ALL_INDEXES doesn't have created/last_ddl_time
        "NULL as lastDdlTime " +
        "FROM ALL_INDEXES " +
        "WHERE OWNER = :owner " +
        "ORDER BY INDEX_NAME")
    List<OracleObject> findAllIndexesByOwner(@Param("owner") String owner);

    /**
     * Find all constraints in a specific schema
     *
     * @param owner The schema/owner name
     * @return List of constraints
     */
    @Query(nativeQuery = true, value =
        "SELECT CONSTRAINT_NAME as name, " +
        "CASE CONSTRAINT_TYPE " +
        "  WHEN 'P' THEN 'PRIMARY KEY' " +
        "  WHEN 'U' THEN 'UNIQUE' " +
        "  WHEN 'C' THEN 'CHECK' " +
        "  WHEN 'R' THEN 'FOREIGN KEY' " +
        "  ELSE 'CONSTRAINT' " + // Default type
        "END as type, " +
        "OWNER as schema, " +
        "OWNER || '.' || CONSTRAINT_NAME as id, " +
        "OWNER as owner, STATUS as status, " +
        "NULL as created, " + // ALL_CONSTRAINTS doesn't have created/last_ddl_time
        "NULL as lastDdlTime " +
        "FROM ALL_CONSTRAINTS " +
        "WHERE OWNER = :owner " +
        "ORDER BY CONSTRAINT_NAME")
    List<OracleObject> findAllConstraintsByOwner(@Param("owner") String owner);
}
