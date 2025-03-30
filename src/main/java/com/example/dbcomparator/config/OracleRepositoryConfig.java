package com.example.dbcomparator.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    basePackages = "com.example.dbcomparator.repository.oracle", // No need for curly braces for single package
    entityManagerFactoryRef = "oracleEntityManagerFactory", // Matches the bean name below
    transactionManagerRef = "oracleTransactionManager" // Matches the bean name below
)
public class OracleRepositoryConfig {

    @Bean(name = "oracleEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean oracleEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("oracleDataSource") DataSource dataSource) {

        // Set JPA properties specifically for Oracle
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none"); // Or "validate"
        properties.put("hibernate.dialect", "org.hibernate.dialect.OracleDialect");
        // Add other Oracle-specific properties if needed

        return builder
                .dataSource(dataSource)
                .packages("com.example.dbcomparator.model.oracle") // Package containing Oracle entities
                .persistenceUnit("oracle") // Unique name for this persistence unit
                .properties(properties)
                .build();
    }

    @Bean(name = "oracleTransactionManager")
    public PlatformTransactionManager oracleTransactionManager(
            @Qualifier("oracleEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
