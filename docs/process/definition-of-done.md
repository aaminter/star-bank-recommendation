# Definition of Done

A task is complete when all applicable criteria are met:

- [ ] Acceptance criteria from the Issue are satisfied.
- [ ] Code follows the existing project conventions.
- [ ] No secrets, local database files, or personal data are committed.
- [ ] Relevant unit/integration tests are added or updated.
- [ ] Existing fixed recommendation scenarios remain unchanged unless the Issue explicitly changes them.
- [ ] `mvn clean verify` passes.
- [ ] Public contracts and configuration changes are documented.
- [ ] Pull Request describes the change, test evidence, and known limitations.
- [ ] At least one other team member has reviewed the change.
- [ ] Integration conflicts and follow-up work are recorded.

For infrastructure changes, include startup/configuration instructions and verify that the original read-only H2 data source remains unaffected.
