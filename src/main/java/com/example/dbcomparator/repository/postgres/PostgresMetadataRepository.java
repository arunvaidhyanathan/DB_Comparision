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
}