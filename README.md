# DB_Comparision
Database Comparison Tool for Oracle and PostgreSQL

This Spring Boot application allows you to compare database objects between Oracle and PostgreSQL databases. It extracts metadata from both databases and generates a comparison report highlighting the differences.

## Features

- Connect to both Oracle and PostgreSQL databases simultaneously
- Extract metadata about tables, views, sequences, functions, and other database objects
- Compare objects between databases and identify differences
- Generate Excel reports with separate tabs for:
  - Oracle objects
  - PostgreSQL objects
  - Objects missing in PostgreSQL
  - Objects missing in Oracle

## Technology Stack

- Java 17
- Spring Boot 3.1.5
- Spring Data JPA
- Oracle JDBC Driver
- PostgreSQL JDBC Driver
- Apache POI (for Excel report generation)
- Lombok

## Project Structure

database-comparator/
├── src/main/java/com/example/dbcomparator/
│   ├── config/             # Database configurations
│   │   └── DatabaseConfig.java  # Multiple datasource configuration
│   ├── model/              # Entity models for DB metadata
│   │   ├── DatabaseObject.java  # Base class for database objects
│   │   ├── oracle/         # Oracle-specific models
│   │   │   └── OracleObject.java
│   │   └── postgres/       # PostgreSQL-specific models
│   │       └── PostgresObject.java
│   ├── repository/         # JPA Repositories
│   │   ├── oracle/         # Oracle repositories
│   │   │   └── OracleMetadataRepository.java
│   │   └── postgres/       # PostgreSQL repositories
│   │       └── PostgresMetadataRepository.java
│   ├── service/            # Business logic for comparison
│   │   └── DatabaseComparisonService.java
│   ├── controller/         # REST API for triggering comparison
│   │   └── ComparisonController.java
│   └── DatabaseComparatorApplication.java  # Main Spring Boot App
├── src/main/resources/
│   └── application.yml     # Database configurations
└── pom.xml                 # Dependencies


1. Create additional model classes for the detailed structures
2. Add repository methods to fetch the detailed information
3. Enhance the comparison service to compare these details
