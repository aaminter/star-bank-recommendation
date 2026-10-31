# Portfolio Case Study: Star Bank Recommendation

- Status date: 2026-10-31
- Project type: educational team backend project
- Role demonstrated: technical coordination / aspiring Java Team Lead
- Production status: not production software

## Problem

Extend an existing recommendation service with dynamic rules while preserving the behavior of existing fixed rules and protecting the supplied H2 transaction/product database from writes.

## Technical context

- Java 17 and Spring Boot 4.1.2
- Spring MVC and JDBC/JdbcTemplate in the existing baseline
- H2 as a read-only source for transaction/product data
- Planned extension: separate PostgreSQL storage, Liquibase migrations, dynamic rule evaluation, and Caffeine caching

The README and architecture overview distinguish the existing baseline from components that are still being implemented.

## Coordination and engineering work

The repository contains concrete artifacts for the following practices:

1. **Task decomposition:** GitHub Issues split the work into persistence/CRUD, rule evaluation, API integration, caching, tests, and joint integration.
2. **Parallel ownership:** CRUD/persistence and evaluation/integration work have separate owners, with a joint integration issue.
3. **Contract-first design:** proposed ADRs capture the read-only H2 boundary, first-version condition semantics, and cache invalidation concerns.
4. **Quality process:** Definition of Done, code review guidance, development workflow, PR template, and security policy are documented.
5. **Integration management:** a dependency-aware integration issue defines acceptance criteria and protects fixed-rule behavior.
6. **Automation:** GitHub Actions runs Maven `clean verify` on pushes and pull requests.

## Evidence

- [Project README](../../README.md)
- [Issues](https://github.com/aaminter/star-bank-recommendation/issues)
- [Pull Requests](https://github.com/aaminter/star-bank-recommendation/pulls)
- [Architecture overview](../architecture/overview.md)
- [Proposed ADRs](../adr/)
- [Definition of Done](../process/definition-of-done.md)
- [Code review guidelines](../process/code-review-guidelines.md)
- [Roles and responsibilities](../process/roles-and-responsibilities.md)
- [Integration and escalation](../process/integration-and-escalation.md)
- [CI workflow](../../.github/workflows/ci.yml)

## What this case demonstrates

The current repository demonstrates the ability to structure a small team development effort, make dependencies visible, define quality gates, document architectural trade-offs, and plan integration.

It does **not yet prove** production Team Lead experience, successful delivery of every planned feature, or a green CI history. Those claims require merged implementation PRs, passing CI runs, review evidence, and a completed retrospective.

## Evidence still to add

- [ ] Merged implementation PRs for the dynamic rule engine and CRUD modules.
- [ ] Green CI runs for the main branch and feature PRs.
- [ ] Tests proving fixed-rule compatibility and read-only H2 behavior.
- [ ] Accepted ADRs after team discussion.
- [ ] Joint integration result and completed retrospective.
- [ ] Measured test coverage and build status.
