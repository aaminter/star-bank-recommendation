# ADR-002: Define composition and negation semantics for dynamic rule conditions

- Status: Proposed
- Date: 2026-10-31
- Participants: Project contributors; pending confirmation in Pull Request

## Context

Dynamic rules may contain more than one condition. The evaluator needs deterministic semantics for combining conditions and for expressing negative matches. Without an explicit contract, CRUD, evaluator, tests, and API clients can interpret the same JSON differently.

## Options considered

1. Treat all conditions as OR.
   - Benefit: broad matching and simple implementation.
   - Drawback: can produce recommendations when only one of several required constraints is true.
2. Treat all conditions as AND.
   - Benefit: a rule matches only when every declared condition is satisfied.
   - Drawback: users need separate rules for alternative paths.
3. Support an explicit composition operator such as AND/OR.
   - Benefit: flexible rule expressions.
   - Drawback: increases schema and evaluator complexity.

## Proposed decision

For the first version, treat the condition list as AND: every condition must evaluate to true. Add a boolean `negate` flag to an individual condition; when true, invert that condition's result before applying the AND composition.

This is a first-version proposal, not a claim that arbitrary nested boolean expressions are supported.

## Consequences

### Positive
- The first version has deterministic and testable behavior.
- CRUD validation and evaluator implementation can share one contract.
- Boundary and negative cases can be expressed explicitly.

### Trade-offs
- OR and nested expressions are not supported in the first version.
- A future schema extension may be required if business rules need more complex logic.

### Follow-up
- [ ] Confirm the contract with the CRUD module owner.
- [ ] Add examples to the API contract.
- [ ] Test empty condition lists, one condition, multiple conditions, negate true/false, and mixed results.
- [ ] Decide explicitly whether an empty list is invalid or matches by identity.
