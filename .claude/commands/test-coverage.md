---
description: Generate and analyze comprehensive test coverage report
---

# Test Coverage Analysis

Generate comprehensive test coverage report with Jacoco and identify coverage gaps that need attention.

## Tasks to Execute

1. **Clean Build**: Ensure fresh test execution
2. **Run All Tests**: Execute complete test suite
3. **Generate Coverage Report**: Create Jacoco HTML and XML reports
4. **Analyze Coverage**: Identify low-coverage areas
5. **Provide Recommendations**: Suggest tests for critical uncovered code

## Commands to Run

```bash
# Clean and run tests with coverage
./gradlew clean test jacocoTestReport

# Open coverage report (if in graphical environment)
# xdg-open app/build/reports/jacoco/test/html/index.html
```

## Analysis Focus Areas

### Priority Coverage Targets
1. **Domain Layer**: Use cases and business logic (target: 90%+)
2. **Presenters**: MVP presenters (target: 85%+)
3. **Infrastructure**: Adapters and managers (target: 80%+)
4. **Utilities**: Helper classes (target: 75%+)

### Critical Code Paths
- Audio pipeline initialization
- Effect parameter validation
- Preset save/load operations
- Security validations (JNI, file operations)
- MIDI Learn functionality

## Report Analysis

After generating the report:
1. Check overall project coverage percentage
2. Identify classes below target coverage
3. Focus on critical business logic with low coverage
4. Prioritize security-sensitive code
5. Suggest specific test cases for gaps

## Recommendations Format

Provide actionable recommendations:
- Class/method with low coverage
- Why it's important to test
- Suggested test scenarios
- Priority level (High/Medium/Low)
