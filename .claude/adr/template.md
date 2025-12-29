# ADR-NNN: [Short title of solved problem and solution]

## Status

[Proposed | Accepted | Deprecated | Superseded by [ADR-XXX](XXX-title.md)]

Date: YYYY-MM-DD

## Context

> What is the issue we're seeing that is motivating this decision or change?

Describe the context and problem statement. Include:

- **Background**: What led to this decision?
- **Problem**: What issue needs to be solved?
- **Requirements**: What must the solution provide?
- **Constraints**: What limitations exist?
- **Stakeholders**: Who is affected by this decision?

Example:
```
We need to choose an architecture pattern for our Android application.
The app is growing in complexity with multiple developers contributing.
We need a pattern that provides clear separation of concerns, is testable,
and is familiar to Android developers.
```

## Decision

> What is the change that we're proposing and/or doing?

State the decision clearly and concisely. Include:

- **What**: What are we deciding to do?
- **Why**: Why is this the best option?
- **How**: How will this be implemented?

Example:
```
We will adopt the MVP (Model-View-Presenter) pattern for all new UI components.

- View: Implemented by Fragments/Activities, handles UI only
- Presenter: Coordinates business logic, no Android dependencies
- Model: Data and business logic, accessed via Repository pattern

All new fragments will follow the *FragmentRefactored naming convention
and implement a corresponding *Contract interface.
```

## Alternatives Considered

Document other options that were evaluated:

### Alternative 1: [Name]
- **Pros**:
- **Cons**:
- **Why rejected**:

### Alternative 2: [Name]
- **Pros**:
- **Cons**:
- **Why rejected**:

Example:
```
### MVVM (Model-View-ViewModel)
- Pros: Data binding, less boilerplate for simple views
- Cons: ViewModels can become too complex, harder to test data binding
- Why rejected: Team is more familiar with MVP, better testability

### MVI (Model-View-Intent)
- Pros: Unidirectional data flow, predictable state
- Cons: Steeper learning curve, more boilerplate
- Why rejected: Too complex for our current needs
```

## Consequences

> What becomes easier or more difficult to do because of this change?

Document both positive and negative consequences:

### Positive Consequences
- What benefits does this decision provide?
- What problems does it solve?
- What becomes easier?

### Negative Consequences
- What trade-offs are we making?
- What becomes more difficult?
- What technical debt might this create?

### Neutral Consequences
- Other impacts that are neither clearly positive nor negative

Example:
```
### Positive
- Clear separation of concerns improves testability
- Presenters can be tested without Android framework
- Familiar pattern for most Android developers
- Easy to mock dependencies for testing

### Negative
- More boilerplate code (Contract, Presenter, View)
- Need to manually manage presenter lifecycle
- Some developers prefer ViewModel's lifecycle handling

### Neutral
- Need to establish clear guidelines for Contract definitions
- Existing legacy fragments need gradual migration
```

## Implementation Notes

Optional section for implementation-specific guidance:

- Code examples
- Migration strategy
- Timeline
- Related tasks
- Documentation to update

Example:
```
1. Create templates in .claude/templates/
2. Migrate one fragment as example
3. Document pattern in CLAUDE.md
4. Gradually refactor existing fragments
5. All new features must use this pattern

See templates:
- mvp-contract-template.kt
- presenter-template.kt
- fragment-refactored-template.kt
```

## References

Links to related resources:

- Related ADRs
- External articles
- Documentation
- Discussion threads
- Code examples

Example:
```
- [Clean Architecture by Uncle Bob](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Android MVP Guide](https://github.com/android10/Android-CleanArchitecture)
- ToneForge CLAUDE.md Section: Architecture
```

## Notes

Any additional notes, future considerations, or follow-up items:

Example:
```
- Revisit this decision if we add Jetpack Compose
- Consider ViewModel for simple read-only screens
- May need to refine Contract definitions based on experience
```

---

## Metadata

- **Author**: [Name]
- **Reviewers**: [Names]
- **Discussion**: [Link to PR or discussion]
- **Implementation**: [Link to implementation PR]
