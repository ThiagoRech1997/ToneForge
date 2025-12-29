# Release Preparation Skill

## Description
Comprehensive release preparation checklist and automation for ToneForge releases.

## When to Use
- Before creating a new release
- After completing a major feature
- For bug fix releases
- For beta/RC releases

## Instructions

1. **Version Management**
   - Update version in `app/build.gradle.kts`
   - Update versionCode (increment by 1)
   - Update versionName (follow semantic versioning)
   - Update CHANGELOG.md with release notes

2. **Code Quality Checks**
   - Run lint: `./gradlew lint`
   - Fix all critical lint issues
   - Review lint report
   - Run static analysis
   - Check for TODOs and FIXMEs

3. **Testing**
   - Run all unit tests: `./gradlew test`
   - Run instrumented tests: `./gradlew connectedAndroidTest`
   - Generate coverage report: `./gradlew jacocoTestReport`
   - Run functional validation: `./scripts/functional-validation.sh`
   - Manual testing on multiple devices
   - Test on different Android versions
   - Test edge cases and error scenarios

4. **Security Review**
   - Run security audit (use security-audit skill)
   - Check for exposed API keys
   - Review ProGuard rules
   - Verify FileProvider configuration
   - Check permission usage
   - Review dependency vulnerabilities

5. **Performance Testing**
   - Test audio latency
   - Measure CPU usage
   - Check memory consumption
   - Test battery impact
   - Profile critical paths
   - Test on low-end devices

6. **UI/UX Review**
   - Test all UI flows
   - Verify responsive layouts
   - Check dark mode support
   - Test accessibility features
   - Verify material design compliance
   - Test on different screen sizes

7. **Documentation**
   - Update README.md
   - Update CLAUDE.md if needed
   - Update API documentation
   - Update user guide
   - Review inline comments
   - Generate KDoc documentation

8. **Build Preparation**
   - Clean project: `./gradlew clean`
   - Update dependencies if needed
   - Review build configuration
   - Set up signing configuration
   - Prepare release keystore

9. **Build Release**
   - Build release APK: `./gradlew assembleRelease`
   - Build release bundle: `./gradlew bundleRelease`
   - Verify ProGuard/R8 shrinking
   - Test release build thoroughly
   - Check APK size

10. **Create Git Release**
    - Commit version changes
    - Create release tag
    - Run: `./scripts/create-release.sh X.Y.Z "Release message"`
    - Push tag: `git push origin vX.Y.Z`
    - Create GitHub release

11. **Release Notes**
    - List new features
    - List bug fixes
    - List improvements
    - List known issues
    - Add upgrade instructions
    - Thank contributors

12. **Post-Release**
    - Monitor crash reports
    - Monitor user feedback
    - Prepare hotfix plan if needed
    - Update project board
    - Plan next release

## Pre-Release Checklist

### Critical
- [ ] All tests passing
- [ ] No critical bugs
- [ ] Version updated
- [ ] CHANGELOG updated
- [ ] Security audit completed
- [ ] Release build successful

### Important
- [ ] Performance tested
- [ ] UI/UX reviewed
- [ ] Documentation updated
- [ ] Backward compatibility verified
- [ ] Migration guide (if needed)

### Nice to Have
- [ ] Beta testing completed
- [ ] User feedback incorporated
- [ ] Screenshots updated
- [ ] Demo video created

## Semantic Versioning

- **MAJOR** (X.0.0): Breaking changes, major new features
- **MINOR** (0.X.0): New features, backward compatible
- **PATCH** (0.0.X): Bug fixes, minor improvements

## Release Types

- **Stable**: Production-ready release
- **Beta**: Feature-complete, needs testing
- **RC**: Release candidate, final testing
- **Hotfix**: Critical bug fix

## Expected Deliverables
- Updated version numbers
- Comprehensive test results
- Release build artifacts (APK/AAB)
- Git tag and GitHub release
- Release notes
- Performance metrics
- Post-release monitoring plan
