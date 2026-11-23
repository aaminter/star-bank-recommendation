# Team Lead Evidence Sprint: 4 Weeks

**Project:** Star Bank Recommendation  
**Purpose:** produce verifiable evidence of technical leadership through a real collaborative delivery cycle.  
**Participants:** project coordinator/reviewer and contributor `tani9kuz-lang`.  
**Status:** completed on 2026-11-23; evidence and measured outcomes are summarized below.

## Definition of 5/5

The sprint is successful only when the contributor independently delivers an agreed module through a Pull Request, the reviewer performs a substantive review, defects are corrected, architecture and integration decisions are documented, the change is merged after CI passes, and the team completes a retrospective using repository evidence.

A task assignment, draft ADR, README statement, or self-merged PR does not count as proof.

## Week 1: Align and prepare

**Contributor**
- Confirm the module boundary and acceptance criteria.
- Review the existing code and tests.
- Create a feature branch and implement the agreed contract.
- Open a draft PR early, with a short implementation plan and known risks.

**Coordinator / reviewer**
- Agree the API and persistence contracts before implementation.
- Review the task breakdown and identify dependencies.
- Discuss ADR proposals with the contributor; record decisions only after agreement.
- Avoid implementing the contributor's module on their behalf.

**Evidence**
- Confirmed Issue and acceptance checklist.
- ADR decision record with participants, alternatives, decision and consequences.
- Draft PR linked to the Issue.

## Week 2: Independent implementation and first review

**Contributor**
- Complete the module implementation and focused tests.
- Update the PR description and provide test evidence.
- Respond to each review comment with a code change or reasoned discussion.

**Coordinator / reviewer**
- Review correctness, boundaries, validation, error handling, test quality and maintainability.
- Leave actionable inline comments tied to code.
- Separate blocking defects from suggestions.
- Do not approve while blocking comments remain unresolved.

**Evidence**
- PR authored by the contributor.
- Review with inline comments.
- Follow-up commits by the contributor addressing review feedback.
- Resolved review threads.

## Week 3: Integration and regression control

**Contributor**
- Rebase or merge the current integration branch as agreed.
- Resolve module-level conflicts and update tests.
- Demonstrate the module through its public contract.

**Coordinator / reviewer**
- Coordinate integration with the existing recommendation engine.
- Resolve cross-module conflicts without taking ownership away from the contributor.
- Verify fixed recommendation rules remain unchanged.
- Check read-only access to the H2 source.
- Ensure one metrics snapshot is reused per request instead of repeating the same aggregate query in each rule.
- Require PostgreSQL/Liquibase integration tests for the dynamic-rule storage path before treating that capability as delivered.

**Evidence**
- Integration PR and commit history.
- CI run URL and green test result.
- Regression tests for fixed rules and read-only source behavior.
- Documented integration decisions.

## Week 4: Delivery, review and retrospective

**Contributor**
- Complete agreed fixes and documentation.
- Confirm the final behavior against acceptance criteria.
- Present the delivered module and remaining limitations.

**Coordinator / reviewer**
- Perform final review and approve only after blockers are resolved.
- Confirm merge after required checks pass.
- Facilitate a blameless retrospective based on Issues, PR timestamps, review iterations, CI and defects.

**Evidence**
- Merged contributor-authored PR.
- Green CI run on the target branch.
- Accepted ADRs.
- Completed retrospective with 3–5 actions, owners and verification dates.
- Updated case study containing links to evidence, not unsupported claims.

## Scorecard

| Criterion | 0 | 1 |
|---|---|---|
| Contributor ownership | No independent delivery | Contributor delivers agreed module |
| Code review | No substantive review | Review comments and fixes are visible |
| Architecture | Decisions remain informal/proposed | ADRs discussed and accepted by team |
| Integration | No integrated delivery | Integration completed and regression-tested |
| Quality and learning | No verified CI or retrospective | Green CI, tests and measured retrospective |

**5/5 requires all five criteria to score 1.** Partial completion must be reported as a partial score.

## Metrics to capture

- Lead time: Issue start to merged PR.
- Review latency: PR ready-for-review to first substantive review.
- Review iterations: number of review/fix cycles.
- CI: first-run result and final result.
- Defects: blocking issues found in review and after merge.
- Scope: acceptance criteria completed / agreed.
- Ownership: contributor-authored commits and reviewer-authored review comments.

Do not set arbitrary targets before the first baseline is measured. Record actual values and compare them with the next cycle.

## Risks and controls

- **Overtaking the contributor's work:** reviewer coaches and reviews; contributor owns implementation.
- **Late contract changes:** agree interfaces in Week 1 and record changes in ADR/PR.
- **Green CI without meaningful tests:** inspect test assertions and coverage of failure paths.
- **False leadership claims:** count only merged work and visible review/integration evidence.
- **Insufficient scope:** reduce module scope rather than simulate completion.

## Final assessment

The completed repository evidence supports a 5/5 result against the five criteria for this educational project:

1. **Contributor ownership: 1/1.** Tatyana authored [PR #25](https://github.com/aaminter/star-bank-recommendation/pull/25), delivering the dynamic-rule persistence and CRUD module.
2. **Code review: 1/1.** PR #25 contains two formal `CHANGES_REQUESTED` reviews, contributor fixes and resolved review threads, followed by `APPROVED`.
3. **Architecture: 1/1.** ADR-001, ADR-002 and ADR-003 have status Accepted; the contributor confirmed agreement in [Issue #15](https://github.com/aaminter/star-bank-recommendation/issues/15).
4. **Integration: 1/1.** Cross-module acceptance is recorded in [Issue #13](https://github.com/aaminter/star-bank-recommendation/issues/13); integration changes were merged through PRs #29, #30 and #31.
5. **Quality and learning: 1/1.** CI checks passed; [PR #33](https://github.com/aaminter/star-bank-recommendation/pull/33) was merged with the expanded target-branch workflow; [Issue #17](https://github.com/aaminter/star-bank-recommendation/issues/17) and [PR #35](https://github.com/aaminter/star-bank-recommendation/pull/35) document the retrospective and its five follow-up actions.

### Measured examples

- Issue #4 lead time: 24 days, 1 hour, 56 minutes, 53 seconds.
- PR #25 time to first substantive review: 17 hours, 5 minutes, 48 seconds.
- PR #25 time from opening to approval: 5 days, 13 hours, 17 minutes, 22 seconds.
- PR #25 review outcomes: two `CHANGES_REQUESTED` submissions followed by one `APPROVED`.
- PR #25 final recorded CI result: 34 tests, no errors.
- PR #33 recorded result: 71 tests, zero errors, zero failures and two skipped tests; GitHub Actions checks passed.

These measurements are specific to the cited Issues and PRs. They are not presented as project-wide averages.

**Scope limitation:** this sprint demonstrates technical coordination and early Team Lead practices in one educational project. It does not, by itself, prove production Team Lead experience.
