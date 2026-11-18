# Code Review Guidelines

## Review goals

Review for correctness, maintainability, compatibility, security, and test quality. A review is not a style contest.

## Checklist

- Does the implementation satisfy the Issue acceptance criteria?
- Are edge cases and invalid input handled?
- Are names, abstractions, and responsibilities clear?
- Are database changes backward-compatible and reproducible?
- Are secrets and sensitive data excluded?
- Are tests meaningful and deterministic?
- Does the change preserve fixed recommendation behavior?
- Are errors observable without leaking sensitive information?
- Is documentation updated where contracts or setup changed?

## Review comments

Use three categories:

- **Blocking**: correctness, security, data loss, broken contract, or missing required tests.
- **Suggestion**: a concrete improvement that is not required for merge.
- **Question**: clarification needed to understand the design.

Every blocking comment should explain the risk and, where possible, suggest a direction for correction.

## Author responsibilities

The author responds to review comments, updates the branch, and explains any rejected suggestion with technical reasoning. Resolve conversations only after the issue is addressed or explicitly agreed.
