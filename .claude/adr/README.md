# Architecture Decision Records (ADRs)

This directory contains Architecture Decision Records for ToneForge, documenting important architectural and technical decisions made during development.

## What are ADRs?

Architecture Decision Records capture important architectural decisions along with their context and consequences. They help:

- **Document why decisions were made** - Not just what was decided
- **Preserve historical context** - Understanding past constraints and reasoning
- **Facilitate onboarding** - Help new developers understand the codebase
- **Guide future decisions** - Learn from past choices
- **Enable discussion** - Provide a format for proposing and reviewing decisions

## ADR Format

Each ADR follows this structure:

```markdown
# ADR-NNN: Title

## Status
[Proposed | Accepted | Deprecated | Superseded by ADR-XXX]

## Context
What is the issue we're seeing that is motivating this decision or change?

## Decision
What is the change that we're proposing and/or doing?

## Consequences
What becomes easier or more difficult to do because of this change?
```

## Creating a New ADR

1. Copy `template.md` to a new file
2. Name it `NNN-descriptive-title.md` (e.g., `004-coroutine-error-handling.md`)
3. Fill in all sections
4. Submit for review via pull request
5. Update status when decision is made

## Existing ADRs

| Number | Title | Status | Date |
|--------|-------|--------|------|
| [001](001-mvp-pattern-adoption.md) | MVP Pattern Adoption | Accepted | 2024-01 |
| [002](002-clean-architecture-layers.md) | Clean Architecture Layers | Accepted | 2024-01 |
| [003](003-jni-security-practices.md) | JNI Security Practices | Accepted | 2024-02 |

## ADR Lifecycle

### Proposed
Decision is proposed and under discussion. Anyone can comment and suggest changes.

### Accepted
Decision has been reviewed, discussed, and approved. Implementation should follow this decision.

### Deprecated
Decision is no longer recommended but may still be in use in legacy code. New code should not follow this pattern.

### Superseded
Decision has been replaced by a newer ADR. Reference the superseding ADR.

## Guidelines for Writing ADRs

### Be Clear and Concise
- Use simple language
- Avoid unnecessary jargon
- Be specific about what is being decided

### Provide Context
- Explain the problem or need
- Describe constraints and requirements
- Reference relevant background information

### Document Alternatives
- List options considered
- Explain why they were rejected
- Show you've done your homework

### Be Honest About Trade-offs
- No decision is perfect
- Document both benefits and drawbacks
- Explain why benefits outweigh costs

### Make it Actionable
- Be specific enough to guide implementation
- Include examples if helpful
- Link to related documentation

## When to Create an ADR

Create an ADR for decisions that:

- **Affect architecture**: Layer structure, component organization
- **Impact multiple teams/developers**: Shared patterns and practices
- **Have significant consequences**: Hard to reverse later
- **Resolve debates**: Document the resolution and reasoning
- **Establish patterns**: Define how similar problems should be solved
- **Choose technologies**: Frameworks, libraries, tools

## When NOT to Create an ADR

Don't create ADRs for:

- Trivial decisions that can be easily changed
- Implementation details that don't affect architecture
- Personal coding style preferences
- Obvious choices with no alternatives
- Temporary workarounds or experiments

## ADR Numbering

- Use sequential numbers: 001, 002, 003, etc.
- Never reuse numbers, even if an ADR is deleted
- Gaps in numbering are okay (means ADRs were removed)

## Reviewing ADRs

When reviewing a proposed ADR, consider:

1. **Completeness**: Are all sections filled out?
2. **Clarity**: Is the decision clearly stated?
3. **Justification**: Is the reasoning sound?
4. **Alternatives**: Were other options considered?
5. **Consequences**: Are trade-offs honestly assessed?
6. **Feasibility**: Can this be implemented?

## Updating ADRs

ADRs should generally not be modified after being accepted, as they represent decisions at a point in time. Instead:

- **For minor corrections**: Update the existing ADR with a note
- **For significant changes**: Create a new ADR that supersedes the old one
- **For reversals**: Create a new ADR explaining why the decision was reversed

## Related Documentation

- [CLAUDE.md](../../CLAUDE.md) - Project overview and current architecture
- [Clean Architecture Guide](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [ADR Best Practices](https://github.com/joelparkerhenderson/architecture-decision-record)

## Examples from ToneForge

See existing ADRs for examples specific to this project:

- **001-mvp-pattern-adoption.md**: Why we chose MVP over MVVM
- **002-clean-architecture-layers.md**: How we structure our layers
- **003-jni-security-practices.md**: Security guidelines for native code

---

**Note**: ADRs are living documents for active decisions. Archive old ADRs in `archive/` when they're no longer relevant.
