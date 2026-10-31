# ADR-001: Keep source transactions read-only and store dynamic rules separately

- Status: Proposed
- Date: 2026-10-31
- Participants: Project contributors; pending confirmation in Pull Request

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

## Proposed decision

Keep the supplied H2 datasource read-only and use a separate PostgreSQL datasource for dynamic rule definitions. Existing fixed rules and H2-backed transaction/product reads remain unchanged.

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
- [ ] Confirm the decision with all contributors.
- [ ] Document datasource properties and ownership.
- [ ] Add integration tests for PostgreSQL CRUD.
- [ ] Add a test proving the H2 source remains read-only.
