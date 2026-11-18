# Contributing

## Before starting

1. Select an open GitHub Issue or create one with an outcome and acceptance criteria.
2. Check dependencies and agree on API/data contracts with affected contributors.
3. Confirm the task owner and integration sequence.
4. Do not start parallel work on the same files without coordination.

## Branches and commits

Use descriptive branches:

- `feature/<purpose>`
- `fix/<purpose>`
- `docs/<purpose>`

Keep commits focused. Commit messages should describe the change, not the activity.

## Implementation rules

- Preserve the supplied H2 database as read-only.
- Keep dynamic rule storage separate from source transaction data.
- Preserve existing fixed recommendation behavior.
- Never commit credentials, database files, tokens, or personal data.
- Validate external input at the API boundary.
- Add tests for normal, negative, boundary, and invalid-input cases.

## Pull Requests

- Link the GitHub Issue.
- Describe the design and externally visible behavior.
- List tests and their results.
- Call out configuration, migration, and compatibility changes.
- Request review from a contributor familiar with the affected module.
- Resolve blocking review comments before merge.

## Definition of Done

Use [Definition of Done](docs/process/definition-of-done.md). A task is not complete merely because the code is pushed: required checks, review, and integration evidence must be available.
