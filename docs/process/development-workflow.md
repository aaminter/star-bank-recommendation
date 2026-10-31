# Team Development Workflow

## Task lifecycle

1. Describe the outcome and acceptance criteria in a GitHub Issue.
2. Identify dependencies and agree on contracts before parallel implementation.
3. Assign an owner and create a feature branch.
4. Implement the smallest coherent change.
5. Run relevant tests and `mvn clean verify`.
6. Open a Pull Request referencing the Issue.
7. Request review and resolve blocking comments.
8. Merge only after checks pass and integration impact is understood.
9. Update the Issue and record follow-up work.

## Branch conventions

Use descriptive names such as:

- `feature/<short-purpose>`
- `fix/<short-purpose>`
- `docs/<short-purpose>`

Avoid committing directly to the integration branch for feature work.

## Coordination

The coordinator maintains visibility of dependencies, integration sequence, and risks. Each task owner remains responsible for implementation quality, tests, and communicating blockers. Technical contracts are agreed by the contributors affected by them, not imposed silently.

## Evidence

Use Issues, PR descriptions, review threads, commit history, and test results as the project record. Do not claim completion based only on a verbal status update.
