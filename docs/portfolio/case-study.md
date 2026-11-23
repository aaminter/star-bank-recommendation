# Portfolio Case Study: Star Bank Recommendation

- Status date: 2026-11-23
- Project type: educational team backend project
- Role demonstrated: technical coordination, task decomposition, architecture facilitation, code review and integration
- Contributors: `aaminter`, `tani9kuz-lang`
- Production status: educational project, not a production system

## Project outcome

The team extended a recommendation backend with independently developed dynamic-rule storage and evaluation, integrated the modules into the recommendation API, added operational endpoints and Telegram integration, and established a reviewed Pull Request workflow with automated CI.

The supplied H2 transaction/product database remains read-only. Dynamic rules and their statistics are stored separately in PostgreSQL, with Liquibase-managed schema changes.

## Technical result

- Java 17 and Spring Boot 4.1.2.
- Spring MVC REST API.
- H2 as a read-only source of customer, product and transaction data.
- Separate PostgreSQL storage for dynamic rules and statistics.
- Spring Data JPA and Liquibase for persistence and migrations.
- Fixed recommendation rules retained alongside dynamic rules.
- Dynamic condition evaluation with AND composition and per-condition `negate`.
- Caffeine caching of customer metrics and cache management.
- Rule-hit statistics.
- Telegram operator flow using the shared recommendation service and private-chat restrictions.
- Management endpoints for cache clearing and build information.
- Unit, MVC and integration tests; GitHub Actions CI on Java 17.

## Ownership and delivery

| Contributor | Delivered scope | Evidence |
|---|---|---|
| `tani9kuz-lang` | PostgreSQL/JPA persistence, Liquibase, dynamic-rule CRUD and validation; statistics and management-info work | [Issue #4](https://github.com/aaminter/star-bank-recommendation/issues/4), [Issue #20](https://github.com/aaminter/star-bank-recommendation/issues/20), [Issue #24](https://github.com/aaminter/star-bank-recommendation/issues/24), [PR #25](https://github.com/aaminter/star-bank-recommendation/pull/25) |
| `aaminter` | Dynamic-rule engine, metrics caching, recommendation integration, statistics integration, Telegram and management endpoints, CI | [Issue #8](https://github.com/aaminter/star-bank-recommendation/issues/8), [Issue #10](https://github.com/aaminter/star-bank-recommendation/issues/10), [Issue #21](https://github.com/aaminter/star-bank-recommendation/issues/21), [Issue #22](https://github.com/aaminter/star-bank-recommendation/issues/22), [Issue #23](https://github.com/aaminter/star-bank-recommendation/issues/23), [Issue #16](https://github.com/aaminter/star-bank-recommendation/issues/16), [PR #26](https://github.com/aaminter/star-bank-recommendation/pull/26), [PR #29](https://github.com/aaminter/star-bank-recommendation/pull/29), [PR #30](https://github.com/aaminter/star-bank-recommendation/pull/30), [PR #31](https://github.com/aaminter/star-bank-recommendation/pull/31), [PR #33](https://github.com/aaminter/star-bank-recommendation/pull/33) |
| Both contributors | Task ownership, architecture decisions, end-to-end integration and retrospective | [Issue #14](https://github.com/aaminter/star-bank-recommendation/issues/14), [Issue #15](https://github.com/aaminter/star-bank-recommendation/issues/15), [Issue #13](https://github.com/aaminter/star-bank-recommendation/issues/13), [Issue #17](https://github.com/aaminter/star-bank-recommendation/issues/17) |

## Code review evidence

[PR #25](https://github.com/aaminter/star-bank-recommendation/pull/25) is the primary contributor-owned review example:

- The PR was authored by `tani9kuz-lang`.
- The reviewer submitted two formal `CHANGES_REQUESTED` reviews with actionable findings, including credential handling, request validation, deletion semantics, PostgreSQL integration tests and CI configuration.
- The author responded with implementation fixes; the related review threads were resolved.
- The reviewer then submitted `APPROVED`; the PR was merged after the checks passed.
- The final recorded CI result for this PR was 34 tests with no errors.

This provides evidence of contributor ownership, substantive review, feedback resolution and acceptance after verification. It does not imply that every PR in the repository had the same review depth.

## Architecture and team process

- [ADR-001](../adr/001-read-only-source-and-dynamic-rule-storage.md): preserve read-only H2 and isolate dynamic rules in PostgreSQL.
- [ADR-002](../adr/002-dynamic-rule-condition-semantics.md): define AND and `negate` semantics.
- [ADR-003](../adr/003-local-cache-and-invalidation.md): use bounded local caching with explicit invalidation.
- The contributor's agreement with all three ADRs is recorded in [Issue #15](https://github.com/aaminter/star-bank-recommendation/issues/15).
- Roles and ownership were confirmed in [Issue #14](https://github.com/aaminter/star-bank-recommendation/issues/14).
- Cross-module integration and regression checks were recorded in [Issue #13](https://github.com/aaminter/star-bank-recommendation/issues/13).

## Quality and integration evidence

- [PR #29](https://github.com/aaminter/star-bank-recommendation/pull/29) integrated the recommendation flow and dynamic-rule engine.
- [PR #30](https://github.com/aaminter/star-bank-recommendation/pull/30) delivered management endpoints and Telegram integration.
- [PR #31](https://github.com/aaminter/star-bank-recommendation/pull/31) restricted Telegram recommendation requests to private chats.
- [PR #33](https://github.com/aaminter/star-bank-recommendation/pull/33) was merged into `main`; the CI workflow now runs for PRs targeting both `main` and `feature/recommendation-rules`.
- The PR #33 description records 71 tests, zero errors, zero failures and two skipped tests; GitHub Actions checks passed.
- [PR #35](https://github.com/aaminter/star-bank-recommendation/pull/35) added traceable evidence to the retrospective.

## Measured process evidence

The following values are calculated from GitHub timestamps and review records. They describe this project cycle, not a general team-performance benchmark.

| Metric | Observed result | Basis |
|---|---:|---|
| Issue #4 lead time | 24 days, 1 hour, 56 minutes, 53 seconds | Created 2026-10-30 15:30:04 UTC; closed 2026-11-23 17:26:57 UTC |
| PR #25 time to first substantive review | 17 hours, 5 minutes, 48 seconds | PR opened 2026-11-15 17:06:57 UTC; first review 2026-11-16 10:12:45 UTC |
| PR #25 time from opening to approval | 5 days, 13 hours, 17 minutes, 22 seconds | PR opened 2026-11-15 17:06:57 UTC; approved 2026-11-21 06:24:19 UTC |
| PR #25 formal review outcomes | 2 `CHANGES_REQUESTED`, followed by 1 `APPROVED` | GitHub review submissions |
| PR #25 final recorded test result | 34 tests, no errors | Final review/CI evidence |
| PR #33 recorded test result | 71 tests, 0 errors, 0 failures, 2 skipped | PR description and successful checks |

The available evidence does not provide a consistent project-wide measure for every task's review latency, post-merge defects or acceptance-criteria completion ratio. Those values are not estimated.

## Team Lead evidence assessment

The repository provides evidence for all five criteria defined in [Issue #18](https://github.com/aaminter/star-bank-recommendation/issues/18):

1. Contributor-owned implementation: demonstrated by the contributor-authored and merged PR #25.
2. Substantive review and fixes: demonstrated by review submissions, author responses and resolved threads in PR #25.
3. Agreed architecture: demonstrated by Accepted ADRs and the contributor's explicit confirmation in Issue #15.
4. Integration and regression control: demonstrated by Issue #13 and merged integration PRs.
5. Quality and learning: demonstrated by successful CI, protected-branch workflow, and the retrospective with five follow-up actions.

**Assessment boundary:** this is evidence of technical leadership practices in one educational team project. It is not evidence of production Team Lead employment or long-term leadership across multiple teams.

## Project documentation

- [README](../../README.md)
- [Architecture and diagrams](../architecture.md)
- [Requirements and Issue traceability](../requirements.md)
- [Deployment guide](../deployment.md)
- [OpenAPI specification](../openapi.yaml)
- [Retrospective](../retrospective.md)
- [Team Lead evidence sprint](team-lead-evidence-sprint.md)
