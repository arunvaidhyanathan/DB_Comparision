package com.example.dbcomparator.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Map;
    
    @Configuration
    public class DatabaseConfig {
    
        // --- Supabase Data Source Configuration ---
        @Bean
        @ConfigurationProperties("spring.datasource.supabase")
        public DataSourceProperties supabaseDataSourceProperties() {
            return new DataSourceProperties();
        }
    
        @Bean
        public DataSource supabaseDataSource() {
            return supabaseDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
        }
    
        @Bean
        @Primary
        public JdbcTemplate supabaseJdbcTemplate(@Qualifier("supabaseDataSource") DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }
    
        // --- Oracle Autonomous Database Configuration ---
        @Bean
        @ConfigurationProperties("spring.datasource.oracle")
        public DataSourceProperties oracleDataSourceProperties() {
            return new DataSourceProperties();
        }
    
        @Bean
        public DataSource oracleDataSource() {
            return oracleDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
        }
    
        @Bean
        public JdbcTemplate oracleJdbcTemplate(@Qualifier("oracleDataSource") DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        // --- Explicitly define EntityManagerFactoryBuilder ---
        // This bean is usually auto-configured but might be missing due to manual DataSource/JPA setup.
        @Bean
        public EntityManagerFactoryBuilder entityManagerFactoryBuilder(JpaVendorAdapter jpaVendorAdapter, JpaProperties jpaProperties) {
            return new EntityManagerFactoryBuilder(jpaVendorAdapter, jpaProperties.getProperties(), null);
        }

        // --- Define JpaVendorAdapter (needed by the builder) ---
        @Bean
        public JpaVendorAdapter jpaVendorAdapter(JpaProperties jpaProperties) {
             HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
             // Configure adapter based on JpaProperties if needed
             // adapter.setShowSql(jpaProperties.isShowSql());
             // adapter.setDatabase(jpaProperties.getDatabase());
             // adapter.setDatabasePlatform(jpaProperties.getDatabasePlatform());
             // adapter.setGenerateDdl(jpaProperties.isGenerateDdl());
             return adapter;
        }

        // --- Define JpaProperties (needed by the builder and adapter) ---
        // This allows Spring to load JPA properties from application.properties
        @Bean
        @ConfigurationProperties(prefix = "spring.jpa")
        public JpaProperties jpaProperties() {
            return new JpaProperties();
        }
    }
