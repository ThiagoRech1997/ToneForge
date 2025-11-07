---
description: Review pull request for architecture, security, and quality
---

# Pull Request Review

Perform comprehensive pull request review checking architectural compliance, security issues, code quality, and test coverage.

## Review Checklist

### 1. Architecture Compliance

#### MVP Pattern
- [ ] Contract interface defines View, Presenter, and Model properly
- [ ] Presenter doesn't hold View reference beyond lifecycle
- [ ] View only handles UI updates, no business logic
- [ ] Proper separation of concerns maintained

#### Clean Architecture
- [ ] Dependencies point inward (UI → Domain ← Infrastructure)
- [ ] Domain layer has no Android dependencies
- [ ] Use cases are single-responsibility
- [ ] Interfaces defined in domain, implemented in infrastructure

#### Code Organization
- [ ] Files in correct layer directories
- [ ] Naming follows conventions (*Contract, *Presenter, *FragmentRefactored)
- [ ] No legacy patterns introduced
- [ ] Proper package structure maintained

### 2. Security Review

#### JNI Security
- [ ] Buffer size validation before native calls
- [ ] Bounds checking in C++ code
- [ ] No buffer overflows possible
- [ ] Proper null checking for JNI objects

#### File Operations
- [ ] WAV file validation if loading audio
- [ ] URI permissions handled correctly
- [ ] FileProvider paths properly restricted
- [ ] No path traversal vulnerabilities

#### Input Validation
- [ ] User input properly sanitized
- [ ] Parameter ranges validated
- [ ] No injection vulnerabilities
- [ ] Proper error messages (no sensitive data leaked)

### 3. Code Quality

#### General Quality
- [ ] No code duplication
- [ ] Proper error handling throughout
- [ ] Logging at appropriate levels
- [ ] No commented-out code
- [ ] Clear variable/method names

#### Performance
- [ ] No blocking operations on UI thread
- [ ] Efficient algorithms used
- [ ] Proper resource management (close streams, etc.)
- [ ] No memory leaks

#### Android Best Practices
- [ ] Proper lifecycle management
- [ ] Configuration changes handled
- [ ] Background processing appropriate
- [ ] Permissions requested properly

### 4. Testing

#### Test Coverage
- [ ] Unit tests for new Presenters
- [ ] Unit tests for new Use Cases
- [ ] Integration tests if needed
- [ ] Coverage maintained or improved

#### Test Quality
- [ ] Tests are meaningful (not just for coverage)
- [ ] Edge cases covered
- [ ] Mocking used appropriately
- [ ] Test names clearly describe what they test

### 5. Documentation

- [ ] CLAUDE.md updated if architecture changed
- [ ] Code comments for complex logic
- [ ] Public APIs documented
- [ ] README updated if needed

## Review Process

1. **Read PR Description**: Understand the intent and scope
2. **Check Diff Statistics**: Verify reasonable size and file changes
3. **Review Architecture**: Apply architecture checklist
4. **Security Scan**: Apply security checklist
5. **Code Quality Review**: Check for quality issues
6. **Test Review**: Verify adequate test coverage
7. **Run Validation**: Execute `/validate` command
8. **Provide Feedback**: Summarize findings with specific line references

## Feedback Format

Provide structured feedback:

### ✅ Strengths
- List positive aspects

### ⚠️ Issues Found
For each issue:
- **File:Line** - Brief description
- **Severity**: Critical/High/Medium/Low
- **Recommendation**: Specific fix suggestion

### 📋 Suggestions
- Optional improvements
- Performance optimizations
- Code simplifications

### ✓ Approval Status
- **Approved**: Ready to merge
- **Approved with Comments**: Minor issues, can merge
- **Changes Requested**: Issues must be addressed
