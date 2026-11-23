# Architecture Overview

## Current architecture

Star Bank Recommendation is a Spring Boot backend that returns product recommendations using customer transaction metrics and two groups of recommendation rules:

- fixed rule sets implemented in the application;
- dynamic rule definitions stored in PostgreSQL and evaluated at runtime.

The REST API and Telegram bot use the same recommendation service. The application reads customer, product, and transaction data from the supplied H2 database through `JdbcTemplate`. The H2 datasource is read-only.

## Implemented components and data stores

- **H2:** source of customer, product, and transaction data. The application reads from it and does not modify it.
- **PostgreSQL:** stores dynamic rule definitions and rule-hit statistics. The schema is managed by Liquibase; access to dynamic rules uses JPA.
- **Fixed rule sets:** `Invest500RuleSet`, `TopSavingRuleSet`, and `SimpleCreditRuleSet` remain part of the recommendation flow.
- **Dynamic rule engine:** evaluates supported conditions, combines conditions with AND semantics, and applies the per-condition `negate` flag.
- **Caffeine:** caches aggregated customer metrics in the `RecommendationRepository`. The cache is bounded to 10,000 entries and expires entries 10 minutes after write. It does not cache dynamic rule definitions.
- **Cache management:** `CacheManagementService` invokes all registered `CacheClearable` components. The current cache owner is `RecommendationRepository`.
- **Telegram integration:** the bot receives updates from Telegram Bot API and sends recommendation responses. Access is restricted by the configured sender-ID allowlist.
- **Management API:** provides dynamic-rule CRUD, rule statistics, cache invalidation, and application build information.

## Recommendation request flow

1. A request arrives through the recommendation REST endpoint or Telegram handler.
2. `RecommendationService` obtains customer metrics from `RecommendationRepository`.
3. The repository returns cached metrics when available; otherwise it aggregates the source data from read-only H2 and stores the result in Caffeine.
4. The service evaluates the fixed rule sets.
5. The service loads dynamic rule definitions from PostgreSQL and evaluates their conditions using AND semantics and each condition's `negate` flag.
6. Matching recommendations are returned. For a matching dynamic rule, the rule-hit statistic is incremented.

## Invariants

1. The supplied H2 database is a read-only source and must not be modified.
2. All fixed recommendation rule sets must remain operational.
3. Dynamic rule definitions and their statistics are stored in PostgreSQL.
4. Dynamic rules are validated before persistence and evaluated using the documented condition semantics.
5. Customer metrics are cached in-process by customer ID; cache entries expire after 10 minutes and the cache is limited to 10,000 entries.
6. The management cache-clear endpoint invalidates caches registered through `CacheClearable`.
7. Telegram access is restricted to sender IDs in `TELEGRAM_BOT_ALLOWED_USER_IDS`.

## Related architecture decisions

- [ADR-001: Read-only source and dynamic rule storage](../adr/001-read-only-source-and-dynamic-rule-storage.md)
- [ADR-002: Dynamic rule condition semantics](../adr/002-dynamic-rule-condition-semantics.md)
- [ADR-003: Local cache and invalidation](../adr/003-local-cache-and-invalidation.md)

For the component and activity diagrams, see [Architecture](../architecture.md).
