# Portfolio Case Study: Star Bank Recommendation

- Status date: 2026-10-31
- Project type: educational team backend project
- Role demonstrated: technical coordination / aspiring Java Team Lead
- Production status: not production software

## Problem

Extend an existing recommendation service with dynamic rules while preserving fixed-rule behavior and protecting the supplied H2 transaction/product database from writes.

## Technical baseline

- Java 17, Spring Boot 4.1.2
- Spring MVC and JDBC/JdbcTemplate
- H2 as a read-only source for transaction/product data
- Three fixed recommendation rules
- One CustomerMetrics snapshot per request, shared by all fixed rules
- JUnit and Spring Boot Test

Dynamic rule storage, PostgreSQL, Liquibase, CRUD and Caffeine caching remain planned/in progress until delivered through merged and tested PRs.

## Engineering evidence

- [README](../../README.md)
- [PR #19: reuse one metrics query per request](https://github.com/aaminter/star-bank-recommendation/pull/19)
- [Issues](https://github.com/aaminter/star-bank-recommendation/issues)
- [Architecture overview](../architecture/overview.md)
- [ADR proposals](../adr/)
- [Definition of Done](../process/definition-of-done.md)
- [Code review guidelines](../process/code-review-guidelines.md)
- [Roles and responsibilities](../process/roles-and-responsibilities.md)
- [Integration and escalation](../process/integration-and-escalation.md)
- [CI workflow](../../.github/workflows/ci.yml)

## Team coordination evidence

The repository includes task decomposition, role definitions, review guidance, integration criteria and ADR proposals. These are process artifacts, not proof that a contributor-owned module has already been independently delivered and reviewed.

The four-week [Team Lead Evidence Sprint](team-lead-evidence-sprint.md) defines the evidence required for a 5/5 result. The tracking Issue is [#18](https://github.com/aaminter/star-bank-recommendation/issues/18).

## Current gaps

- [ ] Tatyana independently delivers an agreed module through a PR.
- [ ] A substantive review is completed with inline comments and contributor fixes.
- [ ] ADRs are discussed and accepted by the team.
- [ ] Dynamic-rule storage and CRUD are implemented with PostgreSQL and Liquibase.
- [ ] Engine integration is completed with regression tests.
- [ ] CI is green on the relevant PR and target branch.
- [ ] Retrospective is completed with measured delivery/review/quality metrics.

## Honest assessment

The current repository demonstrates backend implementation and preparation of a small-team workflow. It does not yet prove production Team Lead experience or the full 5/5 collaborative delivery cycle. Claims must be updated only when linked evidence exists.
