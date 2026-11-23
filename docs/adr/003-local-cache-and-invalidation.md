# ADR-003: Local caching and invalidation strategy

- Status: Accepted; implementation deviation documented
- Date: 2026-10-31
- Participants: `aaminter`, `tani9kuz-lang`; agreement recorded in [Issue #15](https://github.com/aaminter/star-bank-recommendation/issues/15).

## Context

The recommendation flow repeatedly reads customer transaction and product data from the read-only H2 source. Caching can reduce repeated database aggregation, but cached values must be bounded and have an explicit invalidation mechanism.

Dynamic rule definitions are stored separately in PostgreSQL. Caching those definitions would require invalidation when the rule set changes.

## Options considered

1. No cache.
   - Benefit: simplest correctness model and always-current reads.
   - Drawback: repeated aggregation queries against H2.
2. Local in-memory cache with Caffeine.
   - Benefit: lightweight integration and low latency for a single application instance.
   - Drawback: process-local state requires an explicit expiry and invalidation policy.
3. Distributed cache.
   - Benefit: shared state across application instances.
   - Drawback: adds infrastructure and operational complexity not justified by the current project scale.

## Accepted decision

Use Caffeine for local in-process caching. Keep the cache bounded and provide an application-level cache invalidation mechanism. Do not introduce distributed caching without a measured need.

The original decision discussion also considered caching dynamic rule definitions and invalidating affected entries after rule changes. The current implementation does not cache dynamic rule definitions; that part of the originally discussed design has not been implemented.

## Current implementation

The implemented cache is owned by `RecommendationRepository` and stores aggregated `CustomerMetrics`, keyed by customer UUID:

- maximum size: 10,000 entries;
- expiry: 10 minutes after write;
- cache miss: metrics are loaded from the read-only H2 database through `JdbcTemplate`;
- cache clear: `RecommendationRepository` implements `CacheClearable`, and `CacheManagementService` invokes all registered cache owners through `POST /management/clear-caches`.

`DynamicRuleService` reads dynamic rule definitions from PostgreSQL through `DynamicRuleRepository`; it does not currently maintain a Caffeine cache. Therefore, create/delete operations do not invalidate a dynamic-rule cache in the current implementation.

## Consequences

### Positive

- Repeated customer-metrics aggregation can be avoided while a cache entry is valid.
- Cache memory use is bounded.
- The management endpoint provides a common invalidation mechanism for registered cache owners.
- No separate cache infrastructure is required.

### Trade-offs and limitations

- Each application instance has its own cache.
- Cached customer metrics can remain stale for up to the configured expiry period unless the management endpoint is called.
- Dynamic rule definitions are not cached, so each recommendation flow loads them from PostgreSQL.
- The accepted design discussion about caching dynamic rule definitions is not fully reflected in the implementation.

## Follow-up

- [ ] Decide whether dynamic rule definitions should be cached. If yes, implement cache ownership and invalidation on every rule mutation; if no, explicitly supersede that part of the original decision.
- [ ] Add or retain tests proving customer-metrics cache expiry, reuse, and management-endpoint invalidation.
- [ ] Document the operational procedure for invoking cache invalidation after source-data changes.
- [ ] Revisit the local-cache decision if the application is deployed with multiple instances.
