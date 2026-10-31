# ADR-003: Use a local cache for dynamic rule reads

- Status: Proposed
- Date: 2026-10-31
- Participants: Project contributors; pending confirmation in Pull Request

## Context

The recommendation flow may repeatedly read and evaluate the same dynamic rule definitions. A cache can reduce database reads and repeated work, but stale rules could cause incorrect recommendations after create, update, or delete operations.

## Options considered

1. No cache.
   - Benefit: simplest correctness model and always-current reads.
   - Drawback: repeated database reads and evaluation work.
2. Local in-memory cache with Caffeine.
   - Benefit: lightweight integration and low-latency reads for a single application instance.
   - Drawback: cache is process-local and needs explicit invalidation.
3. Distributed cache.
   - Benefit: shared state across application instances.
   - Drawback: adds infrastructure and operational complexity that is not yet justified by demonstrated scale requirements.

## Proposed decision

Evaluate Caffeine as the first cache implementation, provided the application remains a single-instance educational service. Cache only data whose invalidation behavior is defined. Successful create, update, and delete operations must invalidate or refresh the affected cache entry before the updated rule set is used by subsequent requests.

Do not introduce distributed caching without a measured need.

## Consequences

### Positive
- Avoids introducing a separate cache service.
- Provides a bounded, testable first implementation.

### Trade-offs
- Multiple application instances would have independent caches.
- Incorrect invalidation can return stale recommendation rules.
- Cache behavior must be covered by deterministic tests.

### Follow-up
- [ ] Confirm whether the first release targets a single application instance.
- [ ] Agree cache key and value model.
- [ ] Define TTL and maximum size.
- [ ] Test invalidation after create, update, and delete.
- [ ] Document behavior after process restart.
