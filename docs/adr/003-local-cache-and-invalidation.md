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

Dynamic rule definitions stored in PostgreSQL are intentionally not cached. The original discussion considered caching them, but the current read path loads the rule set from PostgreSQL directly. No measured performance need currently justifies a second cache and its mutation-invalidation requirements. This supersedes the earlier proposal to cache dynamic rule definitions. If profiling later identifies PostgreSQL rule reads as a bottleneck, caching can be reconsidered with explicit invalidation on every successful rule mutation.

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
- Dynamic rule definitions are deliberately read from PostgreSQL on each retrieval; this avoids stale rule definitions and mutation-driven cache invalidation, at the cost of a database read.

## Follow-up

- [x] Decide whether dynamic rule definitions should be cached. They are intentionally not cached unless profiling demonstrates a material performance need; this supersedes the earlier proposal to cache them.
- [x] Add tests proving customer-metrics cache reuse, expiry, and invalidation through the management HTTP endpoint.
- [x] Document the operational procedure for invoking cache invalidation after source-data changes in [deployment instructions](../deployment.md#управление-кешем).
- [ ] Revisit the local-cache decision if the application is deployed with multiple instances. The cache is process-local, so each instance must be invalidated independently; distributed caching remains out of scope until there is a demonstrated need.
