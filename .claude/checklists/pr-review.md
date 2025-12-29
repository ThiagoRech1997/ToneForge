# Pull Request Review Checklist

Use this checklist when reviewing pull requests to ensure code quality, architectural compliance, and security standards.

## 📐 Architecture Compliance

### MVP Pattern
- [ ] Contract interface properly defines View, Presenter, and Model interfaces
- [ ] Presenter doesn't hold View reference beyond lifecycle (use weak references or detach)
- [ ] View implementation only handles UI updates, no business logic
- [ ] Model/Repository handles data operations
- [ ] Proper separation between layers maintained
- [ ] No direct Android framework dependencies in Presenter

### Clean Architecture
- [ ] Dependencies point inward: UI → Domain ← Infrastructure
- [ ] Domain layer is pure Kotlin (no Android dependencies)
- [ ] Use cases have single responsibility
- [ ] Interfaces defined in domain layer
- [ ] Implementations in infrastructure layer
- [ ] No layer bypassing (e.g., UI directly calling Infrastructure)

### Code Organization
- [ ] Files placed in correct layer directories (domain/infrastructure/ui)
- [ ] Naming conventions followed: `*Contract`, `*Presenter`, `*FragmentRefactored`
- [ ] No legacy patterns introduced in new code
- [ ] Package structure follows established patterns
- [ ] Refactored fragments preferred over legacy ones

## 🔒 Security Review

### JNI Security
- [ ] Buffer size validation before native calls
- [ ] Array bounds checking in C++ code
- [ ] No potential buffer overflows
- [ ] Proper null checking for JNI objects
- [ ] GetArrayLength() used before accessing arrays
- [ ] Proper exception handling in JNI calls

### File Operations
- [ ] WAV file format validation if loading audio files
- [ ] File size limits enforced
- [ ] URI permissions handled correctly with takePersistableUriPermission
- [ ] FileProvider paths properly restricted (see res/xml/file_paths.xml)
- [ ] No path traversal vulnerabilities
- [ ] Proper file extension validation

### Input Validation
- [ ] All user input properly validated
- [ ] Parameter ranges checked (e.g., 0-100 for percentages)
- [ ] No SQL/command injection vulnerabilities
- [ ] Proper escaping of special characters if needed
- [ ] Error messages don't leak sensitive information
- [ ] Input length limits enforced

### Permissions & Privacy
- [ ] Only necessary permissions requested
- [ ] Runtime permissions handled properly
- [ ] No sensitive data logged
- [ ] Secure storage for sensitive data if applicable

## 💎 Code Quality

### General Quality
- [ ] No code duplication (DRY principle)
- [ ] Complex logic extracted into well-named functions
- [ ] Proper error handling throughout (try-catch where appropriate)
- [ ] Logging at appropriate levels (ERROR, WARN, INFO, DEBUG)
- [ ] No commented-out code (remove or explain why it's there)
- [ ] Clear, descriptive variable and method names
- [ ] Constants defined for magic numbers/strings

### Performance
- [ ] No blocking operations on main/UI thread
- [ ] Coroutines or background threads used for heavy operations
- [ ] Efficient algorithms (consider time/space complexity)
- [ ] Proper resource management (close streams, cursors, etc.)
- [ ] No obvious memory leaks
- [ ] Bitmaps/large objects properly recycled
- [ ] Database queries optimized if applicable

### Android Best Practices
- [ ] Proper Activity/Fragment lifecycle management
- [ ] Configuration changes handled (screen rotation, etc.)
- [ ] Background processing uses appropriate mechanisms (WorkManager, Service)
- [ ] No context leaks in async operations
- [ ] Permissions requested at appropriate times
- [ ] UI state preserved and restored correctly
- [ ] Resources properly released in onDestroy/onDetach

### Kotlin Best Practices
- [ ] Null safety properly utilized (?, !!, ?:, let, etc.)
- [ ] Data classes used where appropriate
- [ ] Sealed classes for state management
- [ ] Extension functions for utility methods
- [ ] Scope functions used appropriately (let, apply, run, with, also)
- [ ] Coroutines used instead of callbacks where possible

## 🧪 Testing

### Test Coverage
- [ ] Unit tests added for new Presenters
- [ ] Unit tests added for new Use Cases
- [ ] Unit tests added for new business logic
- [ ] Integration tests added if crossing layer boundaries
- [ ] UI tests added for critical user flows if applicable
- [ ] Overall coverage maintained or improved

### Test Quality
- [ ] Tests are meaningful, not just for coverage numbers
- [ ] Edge cases and error scenarios covered
- [ ] Mocking used appropriately (mock external dependencies)
- [ ] Test names clearly describe what they test (given_when_then format)
- [ ] Tests are independent and can run in any order
- [ ] No flaky tests (tests that randomly fail)
- [ ] Assertions are specific and clear

### Native Code Testing
- [ ] C++ code changes have corresponding validation
- [ ] Buffer handling tested with edge cases
- [ ] Audio processing verified with test signals if applicable

## 📚 Documentation

### Code Documentation
- [ ] Complex algorithms explained with comments
- [ ] Public APIs have KDoc/Javadoc comments
- [ ] Non-obvious decisions explained in comments
- [ ] TODOs have associated tickets/issues if applicable

### Project Documentation
- [ ] CLAUDE.md updated if architecture changed
- [ ] README.md updated if user-facing features added
- [ ] Migration guide provided if breaking changes
- [ ] Skills/agents updated if workflow changed

## 🚀 Build & Validation

### Build Success
- [ ] Code compiles without errors
- [ ] No new compiler warnings introduced
- [ ] Lint checks pass
- [ ] Proguard rules updated if using reflection/JNI

### Functional Validation
- [ ] Run `./gradlew test` - all tests pass
- [ ] Run `./gradlew lint` - no critical issues
- [ ] Run `./scripts/functional-validation.sh` - passes
- [ ] Test coverage report shows adequate coverage
- [ ] Manual testing on device confirms functionality

## 🎨 UI/UX (if applicable)

- [ ] UI follows Material Design guidelines
- [ ] Consistent with existing app design
- [ ] Responsive on different screen sizes
- [ ] Accessibility considerations (content descriptions, colors)
- [ ] Loading states handled gracefully
- [ ] Error states provide clear user feedback
- [ ] Animations are smooth (60 fps)

## 🔄 Backwards Compatibility

- [ ] No breaking changes to public APIs without migration path
- [ ] Database migrations provided if schema changed
- [ ] Preferences/settings migration handled
- [ ] Existing presets still load correctly

## Review Decision

After completing the checklist, decide on approval status:

### ✅ Approved
All checks pass, ready to merge immediately.

### ✅ Approved with Minor Comments
Minor suggestions that don't block merge. Author can address in follow-up PR.

### ⚠️ Changes Requested
Issues found that must be addressed before merge:
- List specific issues with file:line references
- Explain why each issue needs to be fixed
- Suggest concrete solutions

### ❌ Rejected
Major architectural or security issues requiring redesign.

---

## Notes for Reviewers

- Focus on logic, architecture, and security rather than style (use linters for style)
- Be constructive and specific in feedback
- Suggest alternatives, don't just criticize
- Consider the context and constraints of the project
- Ask questions if something is unclear rather than assuming
- Remember: code review is a conversation, not a judgment
