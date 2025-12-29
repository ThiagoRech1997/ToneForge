---
description: Run comprehensive validation checks before committing
---

# Validation Command

Run functional validation, tests, and lint checks before committing changes to ensure code quality and project integrity.

## Tasks to Execute

1. **Clean Build**: Remove any stale build artifacts
2. **Run Unit Tests**: Execute all unit tests with coverage
3. **Run Lint Checks**: Verify code quality and style
4. **Functional Validation**: Run comprehensive project validation script
5. **Generate Reports**: Create test coverage and lint reports

## Validation Steps

Execute the following commands in sequence:

```bash
# Clean previous builds
./gradlew clean

# Run unit tests with coverage
./gradlew test jacocoTestReport

# Run lint checks
./gradlew lint

# Run functional validation
./scripts/functional-validation.sh
```

## Success Criteria

- All unit tests pass
- Lint checks show no critical issues
- Functional validation script completes successfully
- Test coverage is maintained or improved

## Report Locations

After validation, review reports at:
- Test results: `app/build/reports/tests/`
- Coverage report: `app/build/reports/jacoco/`
- Lint results: `app/build/reports/lint/`

If any checks fail, analyze the reports and fix issues before committing.
