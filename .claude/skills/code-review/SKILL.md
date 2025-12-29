# Code Review Skill

Perform comprehensive code review for ToneForge following architectural, security, and quality standards.

## When to Use

Use this skill when:
- Reviewing pull requests
- Conducting code audits
- Evaluating new code before merge
- Checking architectural compliance
- Ensuring security best practices

## What This Skill Does

This skill provides a systematic code review process that:

1. **Analyzes Architecture** - Verifies Clean Architecture and MVP compliance
2. **Checks Security** - Identifies security vulnerabilities (especially JNI)
3. **Evaluates Quality** - Reviews code quality, patterns, and maintainability
4. **Reviews Tests** - Ensures adequate test coverage and quality
5. **Provides Feedback** - Gives actionable, constructive feedback

## Review Process

### Phase 1: Initial Assessment

**Understand the Change:**
- Read PR description and linked issues
- Understand the intent and scope
- Identify affected components and layers
- Note any breaking changes or migrations

**Review Checklist:**
Load the appropriate checklist:
- Pull Request: `.claude/checklists/pr-review.md`
- New Feature: `.claude/checklists/new-feature.md`
- Security: `.claude/checklists/security-audit.md`

### Phase 2: Architecture Review

**Clean Architecture Compliance:**

Check layer boundaries:
```
✓ UI depends on Domain and Infrastructure (via interfaces)
✓ Infrastructure depends on Domain only
✓ Domain has no dependencies (pure Kotlin)
✗ UI directly accessing Infrastructure without interfaces
✗ Infrastructure importing from UI
✗ Domain importing Android framework
```

**MVP Pattern Compliance:**

For UI components:
- [ ] Contract interface exists (*Contract.kt)
- [ ] View interface implemented by Fragment
- [ ] Presenter coordinates use cases
- [ ] No business logic in View
- [ ] Proper lifecycle management (attach/detach)
- [ ] Coroutines managed correctly

**Package Structure:**
- [ ] Files in correct layer directories
- [ ] Naming conventions followed
- [ ] No legacy patterns in new code
- [ ] Proper grouping by feature

### Phase 3: Security Review

**Critical Security Checks:**

**JNI Code (if present):**
```cpp
// ✓ REQUIRED: Buffer validation
jsize length = env->GetArrayLength(array);
if (length <= 0 || length > MAX_SIZE) {
    return; // Safe exit
}

// ✓ REQUIRED: Null checks
jfloat* buffer = env->GetFloatArrayElements(array, nullptr);
if (buffer == nullptr) {
    return; // Safe exit
}

// ✓ REQUIRED: Bounds checking in loops
for (int i = 0; i < length && i < bufferSize; i++) {
    // Safe access
}

// ✓ REQUIRED: Resource cleanup
env->ReleaseFloatArrayElements(array, buffer, 0);
```

**File Operations:**
- [ ] WAV file validation (LoopLoadUtil)
- [ ] File size limits enforced
- [ ] URI permissions handled correctly
- [ ] FileProvider used for sharing
- [ ] No path traversal vulnerabilities

**Input Validation:**
- [ ] All user input validated
- [ ] Parameter ranges checked
- [ ] No injection vulnerabilities
- [ ] Proper error messages (no sensitive data)

**Reference ADR-003** for complete JNI security practices.

### Phase 4: Code Quality Review

**Code Clarity:**
- [ ] Clear, descriptive names
- [ ] Appropriate comments for complex logic
- [ ] No commented-out code
- [ ] Consistent formatting
- [ ] Proper use of Kotlin idioms

**Error Handling:**
- [ ] Try-catch where appropriate
- [ ] Proper error logging
- [ ] Graceful degradation
- [ ] User-friendly error messages
- [ ] No swallowed exceptions

**Performance:**
- [ ] No blocking operations on main thread
- [ ] Efficient algorithms
- [ ] Proper resource management
- [ ] No obvious memory leaks
- [ ] Coroutines used appropriately

**Android Best Practices:**
- [ ] Proper lifecycle handling
- [ ] Configuration change support
- [ ] Background processing appropriate
- [ ] Permissions requested correctly
- [ ] View binding used (not findViewById)

### Phase 5: Testing Review

**Test Coverage:**

Check Jacoco report:
```bash
./gradlew jacocoTestReport
# Target: 75%+ overall, 85%+ for critical code
```

**Test Quality:**
- [ ] Tests are meaningful (not just for coverage)
- [ ] Edge cases covered
- [ ] Error scenarios tested
- [ ] Mocking used appropriately
- [ ] Tests follow Given-When-Then

**Test Structure:**
```kotlin
@Test
fun `test name clearly describes what is tested`() = runTest {
    // Given: Setup test conditions
    val mockData = createMockData()
    `when`(mockRepository.getData()).thenReturn(mockData)

    // When: Execute the action
    presenter.loadData()
    advanceUntilIdle()

    // Then: Verify expected outcome
    verify(mockView).displayData(mockData)
    verify(mockView, never()).showError(any())
}
```

### Phase 6: Documentation Review

**Code Documentation:**
- [ ] Complex logic explained
- [ ] Public APIs documented (KDoc)
- [ ] Non-obvious decisions explained
- [ ] TODOs have context/tickets

**Project Documentation:**
- [ ] CLAUDE.md updated if needed
- [ ] ADR created for significant decisions
- [ ] README updated for user-facing changes
- [ ] Migration guide if breaking changes

### Phase 7: Run Validation

**Execute Validation Commands:**

```bash
# Full validation
./scripts/functional-validation.sh

# Or individual checks
./gradlew clean test
./gradlew lint
./gradlew jacocoTestReport
```

**Or use Claude Code command:**
```
/validate
```

**Check for:**
- [ ] All tests pass
- [ ] No lint errors (warnings acceptable with justification)
- [ ] Build succeeds
- [ ] Coverage maintained or improved

## Providing Feedback

### Feedback Structure

**✅ Strengths**
List positive aspects:
- Good architectural decisions
- Clean code examples
- Excellent test coverage
- Security best practices followed

**⚠️ Issues Found**

For each issue, provide:

**[File:Line]** - Brief description
- **Severity**: Critical | High | Medium | Low
- **Issue**: Specific problem
- **Impact**: Why it matters
- **Recommendation**: How to fix

Example:
```
**[AudioEngine.kt:142]** - Buffer size not validated
- **Severity**: Critical
- **Issue**: JNI call made without validating array length
- **Impact**: Potential buffer overflow, app crash
- **Recommendation**: Add GetArrayLength() check before access (see ADR-003)
```

**💡 Suggestions**

Optional improvements:
- Performance optimizations
- Code simplifications
- Better patterns
- Refactoring opportunities

### Approval Status

Choose appropriate status:

**✅ Approved**
- All checks pass
- No critical issues
- Ready to merge immediately

**✅ Approved with Comments**
- Minor suggestions only
- Author can address in follow-up
- Safe to merge now

**⚠️ Changes Requested**
- Issues must be addressed
- Re-review needed after fixes
- Specific changes required

**❌ Rejected**
- Major architectural problems
- Security vulnerabilities
- Requires significant redesign

## Examples

### Example 1: Architecture Issue

```
**Issue**: UI layer directly accessing infrastructure

**File**: EffectsFragment.kt:89

**Problem**:
Fragment directly calls AudioEngine instead of using AudioRepository
through presenter.

**Why it matters**:
- Violates Clean Architecture dependency rule
- Makes testing difficult
- Bypasses business logic layer
- Creates tight coupling

**Recommendation**:
Move AudioEngine call to presenter, access via AudioRepository interface:

// In Presenter
override fun toggleEffect(effect: PedalEffect) {
    audioRepository.toggleEffect(effect)
}

// In Fragment
binding.effectToggle.setOnClickListener {
    presenter.toggleEffect(PedalEffect.DISTORTION)
}
```

### Example 2: Security Issue

```
**Issue**: Missing buffer validation in JNI

**File**: native-lib.cpp:234

**Problem**:
```cpp
// Current code
jfloat* buffer = env->GetFloatArrayElements(input, nullptr);
for (int i = 0; i < bufferSize; i++) {
    output[i] = process(buffer[i]); // UNSAFE!
}
```

**Why it matters**:
- Buffer overflow vulnerability
- Potential app crash
- Security risk
- Violates ADR-003

**Recommendation**:
```cpp
// Fixed code
jsize inputLength = env->GetArrayLength(input);
if (inputLength <= 0 || inputLength > MAX_BUFFER_SIZE) {
    __android_log_print(ANDROID_LOG_ERROR, TAG, "Invalid buffer size");
    return;
}

jfloat* buffer = env->GetFloatArrayElements(input, nullptr);
if (buffer == nullptr) {
    __android_log_print(ANDROID_LOG_ERROR, TAG, "Failed to get buffer");
    return;
}

// Safe loop
for (int i = 0; i < inputLength && i < bufferSize; i++) {
    output[i] = process(buffer[i]);
}

env->ReleaseFloatArrayElements(input, buffer, JNI_ABORT);
```

See ADR-003 for complete JNI security practices.
```

### Example 3: Testing Issue

```
**Issue**: Insufficient test coverage for error cases

**File**: EffectsPresenterTest.kt

**Problem**:
Only happy path tested. No tests for:
- Network errors
- Invalid parameters
- Null handling
- Concurrent operations

**Why it matters**:
- Bugs in error handling won't be caught
- Production crashes possible
- False confidence in code quality

**Recommendation**:
Add tests for error scenarios:

@Test
fun `loadEffects handles repository error gracefully`() = runTest {
    // Given: Repository throws exception
    `when`(mockRepository.getEffects())
        .thenThrow(RuntimeException("Network error"))

    // When: Loading effects
    presenter.loadEffects()
    advanceUntilIdle()

    // Then: Should show error to user
    verify(mockView).showError(any())
    verify(mockView, never()).displayEffects(any())
}
```

## Success Criteria

A successful code review:

✅ **Is Thorough**
- All checklist items reviewed
- Multiple layers checked
- Both code and tests reviewed

✅ **Is Constructive**
- Specific, actionable feedback
- Examples provided
- Alternative solutions suggested
- Positive aspects acknowledged

✅ **Is Educational**
- Explains why issues matter
- References documentation (ADRs, guides)
- Teaches better patterns
- Builds team knowledge

✅ **Is Balanced**
- Critical issues identified
- Minor issues noted but don't block
- Pragmatic about trade-offs
- Focuses on important things

## Tools and Resources

**Checklists:**
- `.claude/checklists/pr-review.md` - Comprehensive PR review
- `.claude/checklists/new-feature.md` - New feature checklist
- `.claude/checklists/security-audit.md` - Security checklist

**Architecture References:**
- `.claude/adr/001-mvp-pattern-adoption.md` - MVP guidelines
- `.claude/adr/002-clean-architecture-layers.md` - Layer structure
- `.claude/adr/003-jni-security-practices.md` - JNI security

**Templates:**
- `.claude/templates/` - Code templates for reference

**Commands:**
- `/review-pr` - Review pull request command
- `/validate` - Run comprehensive validation
- `/test-coverage` - Analyze test coverage

**Project Docs:**
- `CLAUDE.md` - Project overview
- `.claude/TROUBLESHOOTING.md` - Common issues

## Tips for Effective Reviews

1. **Understand Context First**
   - Read PR description thoroughly
   - Check linked issues
   - Understand the "why"

2. **Start with Architecture**
   - Big picture before details
   - Check layer boundaries
   - Verify patterns

3. **Prioritize Security**
   - JNI code gets extra scrutiny
   - File operations carefully reviewed
   - Input validation checked

4. **Be Specific**
   - Reference file and line numbers
   - Show concrete examples
   - Explain impact

5. **Be Constructive**
   - Suggest solutions, don't just criticize
   - Acknowledge good code
   - Focus on learning

6. **Use Tools**
   - Run validation scripts
   - Check test coverage
   - Review lint output

7. **Think Like a User**
   - How could this fail?
   - What edge cases exist?
   - Is error handling clear?

8. **Consider Maintenance**
   - Will this be understandable in 6 months?
   - Is it consistent with existing code?
   - Does it add technical debt?

## Final Checklist

Before completing review:

- [ ] Architecture reviewed
- [ ] Security checked (especially JNI)
- [ ] Code quality evaluated
- [ ] Tests reviewed
- [ ] Documentation checked
- [ ] Validation run
- [ ] Feedback structured
- [ ] Approval status decided
- [ ] Actionable recommendations provided

---

**Note**: This skill combines multiple specialized agents and checklists to provide comprehensive code review following ToneForge's standards and best practices.
