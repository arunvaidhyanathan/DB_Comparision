package com.example.dbcomparator.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement // Required for transaction management
@EnableJpaRepositories(
    basePackages = "com.example.dbcomparator.repository.postgres", // No need for curly braces for single package
    entityManagerFactoryRef = "postgresEntityManagerFactory", // Matches the bean name below
    transactionManagerRef = "postgresTransactionManager" // Matches the bean name below
)
public class PostgresRepositoryConfig {

    @Primary // Mark this as the primary EntityManagerFactory if needed (e.g., if some default JPA behavior relies on one)
    @Bean(name = "postgresEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean postgresEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("supabaseDataSource") DataSource dataSource) {

        // Set JPA properties specifically for PostgreSQL
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none"); // Or "validate"
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        // Add other PostgreSQL-specific properties if needed

        return builder
                .dataSource(dataSource)
                .packages("com.example.dbcomparator.model.postgres") // Package containing PostgreSQL entities
                .persistenceUnit("postgres") // Unique name for this persistence unit
                .properties(properties)
                .build();
    }

    @Primary // Mark this as the primary TransactionManager if needed
    @Bean(name = "postgresTransactionManager")
    public PlatformTransactionManager postgresTransactionManager(
            @Qualifier("postgresEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
