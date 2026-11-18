# Integration and Escalation

## Module ownership

Each Issue has one directly responsible owner. The owner is accountable for implementation, tests, and raising blockers. The coordinator manages dependencies and integration visibility; this does not transfer implementation ownership from the task owner.

## Contract-first integration

Before parallel work begins, agree on:

- endpoint paths and HTTP status codes;
- request/response JSON and validation errors;
- identifiers and database ownership;
- condition names, operand types, and composition semantics;
- exception mapping and logging expectations;
- tests that prove compatibility.

Record material architectural decisions in `docs/adr/`. Proposed ADRs remain Proposed until contributors confirm them.

## Integration sequence

1. Agree on the contract and acceptance criteria.
2. Implement modules on separate feature branches.
3. Open focused PRs and run CI.
4. Review each module against its Issue.
5. Merge in dependency order.
6. Run the full verification suite after integration.
7. Record defects and follow-up work in Issues.

## Escalation

Raise a blocker when a dependency, contract, environment, or failing test prevents progress. A useful blocker report contains:

- what is blocked;
- the exact expected behavior;
- what was tried and the observed result;
- the decision or help needed;
- the impact on dependent work.

Do not silently change a shared contract or bypass a failing check to preserve a date. Re-plan the dependent work and update the Issue.

## Completion evidence

Use merged PRs, CI results, tests, and issue closure criteria as evidence. A target date in an Issue is a planning estimate, not proof of completion.
