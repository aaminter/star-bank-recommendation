# Architecture Overview

## Current baseline

The application is a Spring Boot service that evaluates recommendation rule sets using customer transaction/product data.

The existing data access path uses JDBC/JdbcTemplate against a local H2 database. The configured H2 datasource is read-only. It is a source of existing transaction and product data and must not be modified by the dynamic-rule feature.

The recommendation service works with a collection of rule-set implementations. Existing fixed rule sets are part of the baseline and must remain operational.

## Target extension

The project is extending the baseline with dynamic rules. The intended separation is:

- H2: existing transaction and product source, read-only.
- PostgreSQL: storage for dynamic rule definitions.
- Rule evaluation: interpret supported condition descriptions against customer metrics.
- Cache: reduce repeated reads/computations where correctness can be preserved.
- Recommendation API: combine fixed and dynamic recommendations without breaking existing contracts.

The PostgreSQL/JPA/Liquibase and Caffeine components are target capabilities; mark each as implemented only after code, tests, and merge confirm it.

## Invariants

1. Do not write to the supplied H2 database.
2. Do not remove or silently change fixed recommendation rules.
3. Keep credentials outside version control.
4. Validate dynamic rule definitions before persistence/evaluation.
5. Test positive, negative, boundary, and malformed-condition cases.
6. Define cache invalidation behavior before enabling caching.

## Open design decisions

Record final decisions as ADRs in `docs/adr/`, including JSON condition schema, AND/negate semantics, PostgreSQL datasource configuration, transaction boundaries, and cache invalidation.
