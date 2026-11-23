# ADR-001: Keep source transactions read-only and store dynamic rules separately

- Status: Accepted
- Date: 2026-10-31
- Participants: `aaminter`, `tani9kuz-lang`; agreement recorded in [Issue #15](https://github.com/aaminter/star-bank-recommendation/issues/15).

## Context

The service reads existing customer transaction and product data from the supplied H2 database. The project is adding dynamic recommendation rules that require persistence and CRUD operations.

Writing dynamic configuration into the supplied H2 database would mix operational source data with application-managed configuration and would violate the read-only boundary already established in the project configuration.

## Options considered

1. Store dynamic rules in the existing H2 database.
   - Benefit: fewer infrastructure components.
   - Drawback: violates the read-only source boundary and couples new configuration to the supplied data store.
2. Store dynamic rules in a separate PostgreSQL database.
   - Benefit: isolates application-managed configuration and allows independent schema migrations.
   - Drawback: adds a datasource, credentials, migrations, and operational setup.

## Accepted decision

Keep the supplied H2 datasource read-only and use a separate PostgreSQL datasource for dynamic rule definitions and rule statistics. Existing fixed rules and H2-backed transaction/product reads remain unchanged.

The application configures the H2 connection with `spring.datasource.hikari.read-only=true` and a file JDBC URL containing `ACCESS_MODE_DATA=r;IFEXISTS=TRUE`. Dynamic-rule JPA repositories use the dedicated `recommendationsDataSource`, `recommendationsEntityManagerFactory`, and `recommendationsTransactionManager`. Liquibase is configured against the PostgreSQL datasource.

## Current implementation and verification

- `application.properties` configures the source H2 datasource as read-only and points it to an existing database file; the default URL does not create a missing source database.
- `DataSourceConfig` creates a separate datasource from `custom.datasource.recommendations` properties.
- `RecommendationsJpaConfig` binds the dynamic-rule repositories and their transaction manager to that PostgreSQL datasource.
- Liquibase uses the PostgreSQL JDBC URL and credentials.
- `DynamicRuleIntegrationTest` exercises rule creation, retrieval, deletion, and statistics through the application and checks the PostgreSQL repository state.
- The integration test overrides `spring.datasource.hikari.read-only=false` and creates/populates H2 tables as test fixtures. It therefore does not prove that writes through the application-bound H2 datasource are rejected.
- `SourceDataSourceReadOnlyTest` separately verifies the write-protection mechanism: it prepares a temporary H2 database, reconnects using `ACCESS_MODE_DATA=r;IFEXISTS=TRUE` and a Hikari datasource with `readOnly=true`, attempts an `INSERT`, expects a `DataAccessException`, and verifies that no row was written. This test verifies the read-only JDBC URL and Hikari setting together; it does not load the application's Spring datasource properties or prove their binding.

## Consequences

### Positive
- Protects the original transaction/product dataset.
- Separates source data from application-managed rule configuration.
- Allows independent Liquibase migrations for dynamic rules.

### Trade-offs
- The application must configure and test two data sources.
- Local development and CI need reproducible PostgreSQL setup.
- Integration tests must prove that dynamic CRUD does not write to H2.

### Follow-up
- [x] Document datasource properties and ownership in `application.properties` and `docs/deployment.md`.
- [x] Add integration coverage for PostgreSQL CRUD and rule statistics.
- [x] Add a dedicated negative test verifying that H2 rejects writes when connected with the read-only JDBC URL and Hikari `readOnly=true`; the test is isolated from Spring property binding.
