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

```
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
```

## Setup and Configuration

### Prerequisites

- Java 17 or higher
- Maven
- Access to Oracle and PostgreSQL databases

### Configuration

Edit the `application.yml` file to configure your database connections:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        format_sql: true
  
  # Oracle Database Configuration
  datasource:
    oracle:
      url: jdbc:oracle:thin:@localhost:1521:xe
      username: your_oracle_username
      password: your_oracle_password
      driver-class-name: oracle.jdbc.OracleDriver
    
    # PostgreSQL Database Configuration
    postgres:
      url: jdbc:postgresql://localhost:5432/postgres
      username: your_postgres_username
      password: your_postgres_password
      driver-class-name: org.postgresql.Driver
```

## Building and Running

### Build the application

```bash
mvn clean package
```

### Run the application

```bash
java -jar target/db-comparator-0.0.1-SNAPSHOT.jar
```

Or using Maven:

```bash
mvn spring-boot:run
```

## API Usage

### Compare Schemas

To compare database schemas and get JSON results:

```
GET /api/compare/schemas?oracleSchema=YOUR_ORACLE_SCHEMA&postgresSchema=YOUR_POSTGRES_SCHEMA
```

Example response:

```json
{
  "oracleObjects": [...],
  "postgresObjects": [...],
  "missingInPostgres": [...],
  "missingInOracle": [...]
}
```

### Generate Excel Report

To generate and download an Excel report:

```
GET /api/compare/report?oracleSchema=YOUR_ORACLE_SCHEMA&postgresSchema=YOUR_POSTGRES_SCHEMA
```

This endpoint returns an Excel file with four tabs:
1. Oracle Objects - All objects in the Oracle schema
2. PostgreSQL Objects - All objects in the PostgreSQL schema
3. Missing in PostgreSQL - Objects present in Oracle but missing in PostgreSQL
4. Missing in Oracle - Objects present in PostgreSQL but missing in Oracle

## Implementation Details

### Multiple DataSource Configuration

The application uses Spring's `@ConfigurationProperties` to configure multiple data sources:

```java
@Bean(name = "oracleDataSource")
@ConfigurationProperties(prefix = "spring.datasource.oracle")
public DataSource oracleDataSource() {
    return DataSourceBuilder.create().build();
}

@Bean(name = "postgresDataSource")
@ConfigurationProperties(prefix = "spring.datasource.postgres")
public DataSource postgresDataSource() {
    return DataSourceBuilder.create().build();
}
```

### Database Metadata Extraction

The application uses native SQL queries to extract metadata from both databases:

- For Oracle: Queries the `ALL_OBJECTS` view
- For PostgreSQL: Queries the `pg_class`, `pg_namespace`, and other system catalogs

### Comparison Logic

The comparison is performed in the `DatabaseComparisonService` class:

```java
// Find objects missing in PostgreSQL
List<OracleObject> missingInPostgres = oracleObjects.stream()
        .filter(oracle -> postgresObjects.stream()
                .noneMatch(postgres -> 
                    postgres.getName().equalsIgnoreCase(oracle.getName()) && 
                    postgres.getType().equalsIgnoreCase(oracle.getType())))
        .collect(Collectors.toList());
```

### Report Generation

The application uses Apache POI to generate Excel reports with multiple tabs for different comparison results.

## Extending the Application

### Adding Support for More Object Types

To add support for additional database object types:

1. Add new query methods in the repository interfaces
2. Update the comparison logic in the service class if needed

### Adding More Detailed Comparison

To compare object structures (e.g., table columns, view definitions):

1. Create additional model classes for the detailed structures
2. Add repository methods to fetch the detailed information
3. Enhance the comparison service to compare these details

## License

This project is licensed under the MIT License - see the LICENSE file for details.