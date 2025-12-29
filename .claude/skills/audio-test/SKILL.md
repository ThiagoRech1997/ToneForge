# Audio Test Skill

## Description
Run comprehensive audio tests for ToneForge, including native C++ tests, JNI interface validation, and audio processing verification.

## When to Use
- After modifying native audio processing code
- When adding new audio effects
- Before creating a release
- When debugging audio issues
- After updating audio dependencies

## Instructions

1. **Run Native Audio Tests**
   - Build the native library with debug symbols
   - Check for memory leaks in audio buffers
   - Validate audio processing algorithms

2. **Test JNI Interface**
   - Verify all JNI methods are properly linked
   - Test parameter passing between Java/Kotlin and C++
   - Check for JNI exceptions and crashes

3. **Audio Pipeline Validation**
   - Test audio routing and effect chain
   - Verify buffer sizes and sample rates
   - Check latency measurements
   - Test effect parameter changes in real-time

4. **Integration Tests**
   - Run unit tests: `./gradlew test`
   - Run instrumented tests: `./gradlew connectedAndroidTest`
   - Generate coverage report: `./gradlew jacocoTestReport`

5. **Manual Verification**
   - Test each effect individually
   - Test effect combinations
   - Verify audio doesn't clip or distort
   - Check CPU usage and battery impact

6. **Report Results**
   - Summarize test results
   - Highlight any failures or warnings
   - Suggest fixes for identified issues
   - Document performance metrics

## Expected Deliverables
- Test execution summary
- Coverage report analysis
- Performance metrics (latency, CPU usage)
- List of issues found with severity
- Recommendations for improvements
