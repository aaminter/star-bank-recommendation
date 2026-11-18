# Roles and Responsibilities

- Status: Working agreement, pending confirmation by both contributors
- Updated: 2026-10-31

## Current task ownership

| Area | Owner | Scope |
|---|---|---|
| Separate PostgreSQL datasource, DynamicRule persistence, Liquibase migrations, CRUD REST API and tests | `tani9kuz-lang` | Issue #4 |
| Dynamic condition handlers, comparison operators, AND/negate semantics, Caffeine cache and engine tests | `aaminter` | Issue #8 |
| Integration of dynamic rules into `GET /recommendation/{user_id}` | `aaminter` | Issue #10 |
| End-to-end integration and joint acceptance | Both contributors | Issue #13 |

The current split reflects the consolidated GitHub Issues. Earlier task ranges (#5–#7 and #8–#12) are no longer used as the ownership reference.

## Coordination

The project coordinator maintains:
- dependency visibility and integration order;
- shared contracts and cross-module questions;
- CI/review readiness;
- blocker tracking and follow-up Issues.

Each module owner maintains:
- implementation decisions within the agreed contract;
- tests and documentation for the module;
- progress updates and early blocker escalation;
- responses to review comments.

The coordinator does not silently make implementation decisions inside another contributor's module. Contract changes are discussed with the affected owner.

## Review and integration

- Every feature change is proposed through a Pull Request.
- A contributor other than the author reviews the change before merge.
- Blocking correctness, security, compatibility, and required-test issues must be resolved before merge.
- The full integration is accepted jointly against Issue #13.

## Blocker escalation

When blocked, report the expected behavior, observed result, attempted steps, decision needed, and impact on dependent tasks. Update the relevant Issue rather than hiding schedule risk.

## Confirmation required

- [ ] Both contributors confirm the current ownership split.
- [ ] Agree review response expectations.
- [ ] Agree who merges PRs and handles urgent integration conflicts.
- [ ] Revisit ownership if dates or scope change.
