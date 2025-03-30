package com.example.dbcomparator.repository.postgres;

import com.example.dbcomparator.model.postgres.PostgresObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for querying PostgreSQL database metadata
 */
@Repository
public interface PostgresMetadataRepository extends JpaRepository<PostgresObject, String> {
    
    /**
     * Find all objects in a specific schema
     * 
     * @param schemaName The schema name
     * @return List of database objects
     */
    @Query(nativeQuery = true, value = 
        "SELECT c.relname as name, " +
        "CASE c.relkind " +
        "  WHEN 'r' THEN 'TABLE' " +
        "  WHEN 'v' THEN 'VIEW' " +
        "  WHEN 'i' THEN 'INDEX' " +
        "  WHEN 'S' THEN 'SEQUENCE' " +
        "  WHEN 'f' THEN 'FOREIGN TABLE' " +
        "  ELSE c.relkind::text " +
        "END as type, " +
        "n.nspname as schema, " +
        "n.nspname || '.' || c.relname as id, " +
        "n.nspname as schemaName, " +
        "CASE c.relkind " +
        "  WHEN 'r' THEN 'TABLE' " +
        "  WHEN 'v' THEN 'VIEW' " +
        "  WHEN 'i' THEN 'INDEX' " +
        "  WHEN 'S' THEN 'SEQUENCE' " +
        "  WHEN 'f' THEN 'FOREIGN TABLE' " +
        "  ELSE c.relkind::text " +
        "END as objectType, " +
        "to_char(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS') as createdAt, " +
        "to_char(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS') as updatedAt " +
        "FROM pg_class c " +
        "JOIN pg_namespace n ON n.oid = c.relnamespace " +
        "WHERE n.nspname = :schemaName " +
        "AND c.relkind IN ('r', 'v', 'i', 'S', 'f') " +
        "ORDER BY c.relkind, c.relname")
    List<PostgresObject> findAllObjectsBySchema(@Param("schemaName") String schemaName);
    
    /**
     * Find all tables in a specific schema
     * 
     * @param schemaName The schema name
     * @return List of tables
     */
    @Query(nativeQuery = true, value = 
        "SELECT c.relname as name, 'TABLE' as type, n.nspname as schema, " +
        "n.nspname || '.' || c.relname as id, " +
        "n.nspname as schemaName, 'TABLE' as objectType, " +
        "to_char(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS') as createdAt, " +
        "to_char(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS') as updatedAt " +
        "FROM pg_class c " +
        "JOIN pg_namespace n ON n.oid = c.relnamespace " +
        "WHERE n.nspname = :schemaName " +
        "AND c.relkind = 'r' " +
        "ORDER BY c.relname")
    List<PostgresObject> findAllTablesBySchema(@Param("schemaName") String schemaName);
    
    /**
     * Find all views in a specific schema
     * 
     * @param schemaName The schema name
     * @return List of views
     */
    @Query(nativeQuery = true, value = 
        "SELECT c.relname as name, 'VIEW' as type, n.nspname as schema, " +
        "n.nspname || '.' || c.relname as id, " +
        "n.nspname as schemaName, 'VIEW' as objectType, " +
        "to_char(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS') as createdAt, " +
        "to_char(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS') as updatedAt " +
        "FROM pg_class c " +
        "JOIN pg_namespace n ON n.oid = c.relnamespace " +
        "WHERE n.nspname = :schemaName " +
        "AND c.relkind = 'v' " +
        "ORDER BY c.relname")
    List<PostgresObject> findAllViewsBySchema(@Param("schemaName") String schemaName);
    
    /**
     * Find all sequences in a specific schema
     * 
     * @param schemaName The schema name
     * @return List of sequences
     */
    @Query(nativeQuery = true, value = 
        "SELECT c.relname as name, 'SEQUENCE' as type, n.nspname as schema, " +
        "n.nspname || '.' || c.relname as id, " +
        "n.nspname as schemaName, 'SEQUENCE' as objectType, " +
        "to_char(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS') as createdAt, " +
        "to_char(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS') as updatedAt " +
        "FROM pg_class c " +
        "JOIN pg_namespace n ON n.oid = c.relnamespace " +
        "WHERE n.nspname = :schemaName " +
        "AND c.relkind = 'S' " +
        "ORDER BY c.relname")
    List<PostgresObject> findAllSequencesBySchema(@Param("schemaName") String schemaName);
    
    /**
     * Find all functions in a specific schema
     * 
     * @param schemaName The schema name
     * @return List of functions
     */
    @Query(nativeQuery = true, value = 
        "SELECT p.proname as name, 'FUNCTION' as type, n.nspname as schema, " +
        "n.nspname || '.' || p.proname as id, " +
        "n.nspname as schemaName, 'FUNCTION' as objectType, " +
        "to_char(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS') as createdAt, " +
        "to_char(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS') as updatedAt " +
        "FROM pg_proc p " +
        "JOIN pg_namespace n ON n.oid = p.pronamespace " +
        "WHERE n.nspname = :schemaName " +
        "ORDER BY p.proname")
    List<PostgresObject> findAllFunctionsBySchema(@Param("schemaName") String schemaName);
    
    /**
     * Find all indexes in a specific schema
     * 
     * @param schemaName The schema name
     * @return List of indexes
     */
    @Query(nativeQuery = true, value = 
        "SELECT c.relname as name, 'INDEX' as type, n.nspname as schema, " +
        "n.nspname || '.' || c.relname as id, " +
        "n.nspname as schemaName, 'INDEX' as objectType, " +
        "to_char(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS') as createdAt, " +
        "to_char(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS') as updatedAt " +
        "FROM pg_class c " +
        "JOIN pg_namespace n ON n.oid = c.relnamespace " +
        "WHERE n.nspname = :schemaName " +
        "AND c.relkind = 'i' " +
        "ORDER BY c.relname")
    List<PostgresObject> findAllIndexesBySchema(@Param("schemaName") String schemaName);

    /**
     * Find all procedures in a specific schema
     *
     * @param schemaName The schema name
     * @return List of procedures
     */
    @Query(nativeQuery = true, value =
        "SELECT r.routine_name as name, 'PROCEDURE' as type, r.routine_schema as schema, " +
        "r.routine_schema || '.' || r.routine_name as id, " +
        "r.routine_schema as schemaName, 'PROCEDURE' as objectType, " +
        "to_char(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS') as createdAt, " + // Information schema doesn't easily provide creation time
        "to_char(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS') as updatedAt " +
        "FROM information_schema.routines r " +
        "WHERE r.routine_schema = :schemaName " +
        "AND r.routine_type = 'PROCEDURE' " +
        "ORDER BY r.routine_name")
    List<PostgresObject> findAllProceduresBySchema(@Param("schemaName") String schemaName);

    /**
     * Find all constraints in a specific schema
     *
     * @param schemaName The schema name
     * @return List of constraints
     */
    @Query(nativeQuery = true, value =
        "SELECT tc.constraint_name as name, tc.constraint_type as type, tc.constraint_schema as schema, " +
        "tc.constraint_schema || '.' || tc.constraint_name as id, " +
        "tc.constraint_schema as schemaName, tc.constraint_type as objectType, " +
        "to_char(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS') as createdAt, " + // Information schema doesn't easily provide creation time
        "to_char(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS') as updatedAt " +
        "FROM information_schema.table_constraints tc " +
        "WHERE tc.constraint_schema = :schemaName " +
        "ORDER BY tc.constraint_name")
    List<PostgresObject> findAllConstraintsBySchema(@Param("schemaName") String schemaName);
}
