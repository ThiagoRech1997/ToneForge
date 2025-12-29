# ToneForge Troubleshooting Guide

Comprehensive troubleshooting guide for common issues in ToneForge development and runtime.

## Table of Contents

1. [Build Issues](#build-issues)
2. [Audio Pipeline Problems](#audio-pipeline-problems)
3. [Native Code Issues](#native-code-issues)
4. [Testing Problems](#testing-problems)
5. [UI/Fragment Issues](#uifragment-issues)
6. [MIDI Issues](#midi-issues)
7. [Preset/File Operations](#presetfile-operations)
8. [Performance Issues](#performance-issues)
9. [Development Environment](#development-environment)

---

## Build Issues

### Gradle Build Fails

**Symptoms:**
- Build errors in Gradle output
- "Could not resolve dependencies"
- CMake configuration errors

**Solutions:**

1. **Clean and Rebuild**
   ```bash
   ./gradlew clean
   ./gradlew build --refresh-dependencies
   ```

2. **Check Gradle Version**
   - Ensure Gradle wrapper version matches project requirements
   - Check `gradle/wrapper/gradle-wrapper.properties`
   - Current required version: 8.11+

3. **Verify NDK Installation**
   ```bash
   # Check NDK is installed
   ls $ANDROID_HOME/ndk/

   # If missing, install via Android Studio SDK Manager
   # Or set NDK path in local.properties:
   # ndk.dir=/path/to/ndk
   ```

4. **CMake Issues**
   ```bash
   # Check CMakeLists.txt syntax
   # Verify native-lib and audio_engine are listed
   # Check minimum CMake version: 3.22.1
   ```

5. **Dependency Conflicts**
   ```bash
   # Show dependency tree
   ./gradlew :app:dependencies

   # Look for version conflicts
   # Update libs.versions.toml if needed
   ```

### JNI Compilation Errors

**Symptoms:**
- "undefined reference to..." in native code
- CMake configuration failures
- Missing header files

**Solutions:**

1. **Check CMakeLists.txt**
   - Verify all .cpp files are listed in `add_library()`
   - Check include directories are correct
   - Ensure Oboe library is properly linked

2. **Verify File Names**
   ```bash
   # JNI function names must match package structure
   # Format: Java_com_example_toneforge_ClassName_methodName

   # Check with:
   grep -r "JNIEXPORT" cpp/
   ```

3. **Clean Native Build**
   ```bash
   rm -rf app/.cxx
   ./gradlew clean
   ./gradlew build
   ```

---

## Audio Pipeline Problems

### Audio Pipeline Won't Start

**Symptoms:**
- No audio processing
- "Pipeline failed to initialize" log message
- App doesn't respond to audio input

**Diagnostic Steps:**

1. **Check Permissions**
   ```bash
   adb shell dumpsys package com.example.toneforge | grep -A 1 "RECORD_AUDIO"
   # Should show: granted=true
   ```

2. **View Logs**
   ```bash
   adb logcat | grep -E "ToneForge|AudioEngine|AudioTrack|AudioRecord"
   ```

3. **Common Issues:**

   **Missing Audio Permission:**
   - Verify `RECORD_AUDIO` permission in AndroidManifest.xml
   - Check runtime permission is requested and granted
   - Test: Go to Settings → Permissions → Microphone

   **Audio in Use by Another App:**
   - Close other audio apps
   - Restart device if needed
   - Check with: `adb shell dumpsys media.audio_flinger`

   **Invalid Sample Rate:**
   - Logs should show sample rate 8000-192000 Hz
   - Check AudioEngine initialization logs
   - Device might not support requested rate

   **Buffer Size Issues:**
   - Check logs for buffer size
   - Try different buffer sizes: 128, 256, 512, 1024
   - Smaller = lower latency but more CPU

4. **Check AudioStateManager**
   ```kotlin
   // In fragment/presenter
   val state = audioRepository.getAudioState()
   Log.d(TAG, "Audio state: $state")
   // Should be RUNNING or STARTED
   ```

5. **Verify Native Initialization**
   ```bash
   adb logcat | grep "AudioEngine"
   # Look for: "AudioEngine initialized successfully"
   # Or errors: "Failed to initialize audio engine"
   ```

**Solutions:**

```kotlin
// Restart pipeline
audioRepository.stopAudioPipeline()
delay(100) // Let resources release
audioRepository.startAudioPipeline()
```

### Crackling or Distorted Audio

**Symptoms:**
- Audio has clicks, pops, or glitches
- Intermittent distortion
- "Buffer underrun" in logs

**Diagnostic Steps:**

1. **Check CPU Usage**
   ```bash
   adb shell top | grep toneforge
   # CPU usage should be < 50% for stable audio
   ```

2. **Check Buffer Underruns**
   ```bash
   adb logcat | grep -i "underrun"
   ```

3. **Check Thread Priority**
   - Audio callback should run on high-priority thread
   - Verify in logs: "Audio callback thread priority: -19"

**Solutions:**

1. **Increase Buffer Size**
   - Larger buffer = more latency but fewer glitches
   - Try doubling current buffer size
   - Settings: Audio → Buffer Size

2. **Reduce Effect Load**
   - Disable some effects temporarily
   - Check if specific effect causes issue
   - Optimize effect processing if needed

3. **Check Background Apps**
   ```bash
   adb shell ps | wc -l  # Total processes
   # Close unnecessary apps
   ```

4. **Verify Sample Rate Consistency**
   - Input and output should match
   - Check logs for sample rate mismatches
   - Force specific rate if needed

5. **Check for Blocking Operations**
   - No blocking I/O in audio callback
   - No mutex locks in audio thread
   - Review audio_engine.cpp for blocking calls

### Effects Not Working

**Symptoms:**
- Effect toggle has no impact on sound
- Parameters don't change audio
- Some effects work, others don't

**Diagnostic Steps:**

1. **Check Effect State**
   ```bash
   adb logcat | grep "Effect"
   # Look for: "Effect enabled: DISTORTION"
   ```

2. **Verify Parameter Values**
   ```kotlin
   val params = audioRepository.getEffectParameters(PedalEffect.DISTORTION)
   Log.d(TAG, "Distortion params: $params")
   ```

3. **Test Individual Effects**
   - Enable only one effect at a time
   - Identify which effect has issues
   - Check effect order in chain

**Solutions:**

1. **Check Dry/Wet Mix**
   - Effect might be at 0% mix
   - Try 100% wet to hear effect clearly
   - UI might not reflect actual value

2. **Verify Effect Order**
   - Some effects interact poorly in certain orders
   - Try reordering effects
   - Check effect chain logs

3. **Reset Effect Parameters**
   ```kotlin
   audioRepository.resetEffect(PedalEffect.DISTORTION)
   ```

4. **Check Native Implementation**
   - Review audio_engine.cpp for effect
   - Verify parameter scaling (0-100 to actual range)
   - Check effect is actually being called

---

## Native Code Issues

### App Crashes in Native Code

**Symptoms:**
- "Fatal signal 11 (SIGSEGV)" in logs
- Immediate crash when audio starts
- Crash in libnative-lib.so

**Diagnostic Steps:**

1. **Get Stack Trace**
   ```bash
   adb logcat | grep -A 20 "SIGSEGV"
   # Or use ndk-stack:
   adb logcat | ndk-stack -sym app/build/intermediates/cmake/debug/obj
   ```

2. **Common Causes:**
   - **Buffer overflow**: Accessing beyond array bounds
   - **Null pointer**: GetFloatArrayElements returned null
   - **Invalid memory**: Using freed memory
   - **Integer overflow**: Buffer size calculation overflow

**Solutions:**

1. **Review Buffer Validation**
   - Check all GetArrayLength() calls
   - Verify bounds checking in loops
   - See ADR-003 for security practices

2. **Add Defensive Checks**
   ```cpp
   jfloat* buffer = env->GetFloatArrayElements(input, nullptr);
   if (buffer == nullptr) {
       __android_log_print(ANDROID_LOG_ERROR, TAG, "Null buffer");
       return;
   }
   ```

3. **Check Memory Management**
   - All GetXXXArrayElements matched with ReleaseXXXArrayElements
   - No use-after-free
   - Proper cleanup in error paths

4. **Enable Address Sanitizer** (Debug only)
   ```gradle
   // In app/build.gradle
   android {
       defaultConfig {
           externalNativeBuild {
               cmake {
                   arguments "-DANDROID_STL=c++_shared"
                   cppFlags "-fsanitize=address"
               }
           }
       }
   }
   ```

### Memory Leaks in Native Code

**Symptoms:**
- Memory usage grows over time
- App becomes slow after extended use
- "Out of memory" after long session

**Diagnostic Steps:**

1. **Monitor Memory**
   ```bash
   # Watch memory usage
   adb shell dumpsys meminfo com.example.toneforge

   # Continuous monitoring
   watch -n 1 'adb shell dumpsys meminfo com.example.toneforge | grep "Native Heap"'
   ```

2. **Check for Leaked References**
   ```cpp
   // Look for:
   // - Local refs not deleted (DeleteLocalRef)
   // - Global refs not released (DeleteGlobalRef)
   // - Array elements not released
   ```

**Solutions:**

1. **Ensure Resource Cleanup**
   ```cpp
   // Always release in reverse order
   jfloat* buffer = env->GetFloatArrayElements(array, nullptr);
   // Use buffer...
   env->ReleaseFloatArrayElements(array, buffer, 0); // Don't forget!
   ```

2. **Use RAII Pattern**
   ```cpp
   class ScopedFloatArray {
       JNIEnv* env;
       jfloatArray array;
       jfloat* elements;
   public:
       ScopedFloatArray(JNIEnv* e, jfloatArray a)
           : env(e), array(a), elements(e->GetFloatArrayElements(a, nullptr)) {}
       ~ScopedFloatArray() {
           if (elements) env->ReleaseFloatArrayElements(array, elements, 0);
       }
       jfloat* get() { return elements; }
   };
   ```

---

## Testing Problems

### Unit Tests Failing

**Symptoms:**
- Tests pass locally but fail in CI
- Intermittent test failures
- "Unresolved reference" in tests

**Solutions:**

1. **Clean Test Environment**
   ```bash
   ./gradlew cleanTest
   ./gradlew test
   ```

2. **Check Test Dependencies**
   - Verify mockito-kotlin version
   - Check JUnit version compatibility
   - Ensure test coroutines dispatcher set

3. **Fix Coroutine Tests**
   ```kotlin
   @OptIn(ExperimentalCoroutinesApi::class)
   class YourPresenterTest {
       private val testDispatcher = StandardTestDispatcher()

       @Before
       fun setup() {
           Dispatchers.setMain(testDispatcher)
       }

       @After
       fun tearDown() {
           Dispatchers.resetMain()
       }

       @Test
       fun `test with coroutines`() = runTest {
           presenter.loadData()
           advanceUntilIdle() // Important!
           verify(mockView).displayData(any())
       }
   }
   ```

4. **Fix Flaky Tests**
   - Add proper delays for async operations
   - Use `advanceUntilIdle()` in coroutine tests
   - Mock all external dependencies
   - Don't depend on test execution order

### Low Test Coverage

**Symptoms:**
- Jacoco report shows low coverage
- Critical code not tested
- Coverage dropping with new features

**Solutions:**

1. **Generate Coverage Report**
   ```bash
   ./gradlew test jacocoTestReport
   open app/build/reports/jacoco/test/html/index.html
   ```

2. **Prioritize Critical Code**
   - Test presenters (target: 85%+)
   - Test use cases (target: 90%+)
   - Test business logic
   - Less focus on simple getters/setters

3. **Use Coverage Commands**
   ```bash
   # Use Claude Code command
   /test-coverage
   ```

4. **Add Missing Tests**
   - Use test templates from `.claude/templates/`
   - Cover happy path, error cases, edge cases
   - Test parameter validation
   - Test error handling

---

## UI/Fragment Issues

### Fragment Not Displaying

**Symptoms:**
- Blank screen where fragment should be
- Fragment lifecycle not called
- "Fragment not attached" error

**Solutions:**

1. **Check Fragment Transaction**
   ```kotlin
   supportFragmentManager.beginTransaction()
       .replace(R.id.fragment_container, YourFragment.newInstance())
       .commit()
   ```

2. **Verify Container ID**
   - Check R.id.fragment_container exists
   - Verify it's a FrameLayout or similar
   - Check visibility is VISIBLE

3. **Check Fragment Lifecycle**
   ```kotlin
   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
       super.onViewCreated(view, savedInstanceState)
       Log.d(TAG, "onViewCreated called") // Should see this
   }
   ```

4. **Verify Navigation**
   ```kotlin
   // Use NavigationController
   navigationController.navigateTo(YourFragmentRefactored.newInstance())
   ```

### View Binding Issues

**Symptoms:**
- "Unresolved reference: binding"
- NullPointerException accessing views
- Views showing wrong values

**Solutions:**

1. **Check View Binding Setup**
   ```kotlin
   private var _binding: FragmentYourBinding? = null
   private val binding get() = _binding!!

   override fun onCreateView(...): View {
       _binding = FragmentYourBinding.inflate(inflater, container, false)
       return binding.root
   }

   override fun onDestroyView() {
       super.onDestroyView()
       _binding = null // Important!
   }
   ```

2. **Enable View Binding**
   ```gradle
   // In app/build.gradle
   android {
       buildFeatures {
           viewBinding = true
       }
   }
   ```

3. **Check Layout File Name**
   - `fragment_your.xml` → `FragmentYourBinding`
   - Rebuild project after layout changes
   - Clean and rebuild if binding not generated

### Presenter/View Not Updating

**Symptoms:**
- UI doesn't reflect data changes
- View methods not called
- Presenter detached errors

**Solutions:**

1. **Verify View Attached**
   ```kotlin
   // In presenter
   private fun updateView() {
       if (view == null) {
           Log.w(TAG, "View not attached!")
           return
       }
       view?.updateUI()
   }
   ```

2. **Check Lifecycle Management**
   ```kotlin
   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
       super.onViewCreated(view, savedInstanceState)
       presenter.attachView(this) // Don't forget!
       presenter.initialize()
   }

   override fun onDestroyView() {
       presenter.detachView() // Important!
       super.onDestroyView()
   }
   ```

3. **Ensure Main Thread for UI Updates**
   ```kotlin
   presenterScope.launch {
       val data = withContext(Dispatchers.IO) {
           // Background work
       }
       // Back on Main dispatcher automatically
       view?.displayData(data)
   }
   ```

---

## MIDI Issues

### MIDI Device Not Detected

**Symptoms:**
- MIDI controller not showing up
- No MIDI events received
- "No MIDI devices found"

**Solutions:**

1. **Check USB Connection**
   - Use USB OTG adapter if needed
   - Try different USB cable
   - Check device is powered (some require external power)

2. **Verify Permissions**
   - USB device permission dialog should appear
   - Grant permission when prompted
   - Check Settings → Apps → ToneForge → Permissions

3. **Check MIDI Device Support**
   ```bash
   adb shell dumpsys media.midi
   # Should list connected devices
   ```

4. **Restart MIDI Manager**
   ```kotlin
   midiManager.closeAllDevices()
   midiManager.scanForDevices()
   ```

### MIDI Learn Not Working

**Symptoms:**
- MIDI Learn mode active but not learning
- Wrong parameter mapped
- MIDI Learn doesn't exit

**Solutions:**

1. **Check MIDI Events Received**
   ```bash
   adb logcat | grep "MIDI"
   # Should see: "MIDI event received: CC..."
   ```

2. **Verify Learn Mode State**
   ```kotlin
   Log.d(TAG, "MIDI Learn active: ${midiManager.isLearnMode()}")
   ```

3. **Check Controller Message Type**
   - ToneForge expects Control Change (CC) messages
   - Program Change won't work
   - Note On/Off won't work
   - Verify controller sends CC messages

4. **Clear Existing Mappings**
   ```kotlin
   midiManager.clearMapping(paramId)
   ```

---

## Preset/File Operations

### Presets Won't Load

**Symptoms:**
- "Failed to load preset" error
- Presets load but parameters wrong
- App crashes when loading preset

**Solutions:**

1. **Check Preset File Format**
   - Should be valid JSON
   - Check with: `adb shell cat /sdcard/ToneForge/presets/preset.json`
   - Validate JSON syntax

2. **Verify File Permissions**
   ```bash
   adb shell ls -l /sdcard/ToneForge/presets/
   # Should be readable
   ```

3. **Check Preset Version**
   - Old presets might not be compatible
   - Check version field in JSON
   - Migrate old presets if needed

4. **Review PresetManager Logs**
   ```bash
   adb logcat | grep "PresetManager"
   ```

### File Export/Import Issues

**Symptoms:**
- "Failed to export" error
- Import crashes app
- Can't find exported files

**Solutions:**

1. **Check Storage Permission**
   ```bash
   adb shell dumpsys package com.example.toneforge | grep -A 1 "WRITE_EXTERNAL"
   ```

2. **Verify FileProvider Setup**
   - Check res/xml/file_paths.xml
   - Verify paths are correct
   - Check AndroidManifest.xml has FileProvider

3. **Check File Validation**
   - WAV files validated by LoopLoadUtil
   - Size limits enforced
   - Format must be valid

4. **Use SAF (Storage Access Framework)**
   ```kotlin
   // Prefer SAF over direct file access
   val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
       type = "application/json"
       putExtra(Intent.EXTRA_TITLE, "preset.json")
   }
   startActivityForResult(intent, CREATE_FILE_REQUEST)
   ```

---

## Performance Issues

### High CPU Usage

**Symptoms:**
- Device gets hot
- Battery drains quickly
- App becomes laggy
- "CPU usage: 80%+" in logs

**Diagnostic Steps:**

```bash
# Monitor CPU
adb shell top | grep toneforge

# Profile with systrace
python $ANDROID_HOME/platform-tools/systrace/systrace.py -a com.example.toneforge
```

**Solutions:**

1. **Reduce Effect Load**
   - Check which effects use most CPU
   - Optimize effect algorithms
   - Consider fixed-point math instead of float

2. **Optimize Audio Callback**
   - Minimize work in audio thread
   - Pre-calculate values outside callback
   - Use lookup tables for expensive functions

3. **Check for Unnecessary Recomposition**
   - UI might be updating too frequently
   - Debounce parameter changes
   - Batch UI updates

4. **Profile Native Code**
   - Use Android Studio Profiler
   - Add timing logs to identify hot spots
   - Consider SIMD optimizations (NEON)

### Memory Issues

**Symptoms:**
- "Out of memory" error
- App killed by system
- Slow performance over time

**Solutions:**

1. **Check Memory Usage**
   ```bash
   adb shell dumpsys meminfo com.example.toneforge
   ```

2. **Look for Leaks**
   - Fragment/Activity leaks
   - Presenter not detached
   - Native memory not freed
   - Bitmap not recycled

3. **Use LeakCanary** (Debug builds)
   ```gradle
   debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.12'
   ```

4. **Reduce Allocations**
   - Reuse audio buffers
   - Pool objects instead of allocating
   - Avoid creating objects in audio callback

---

## Development Environment

### Android Studio Issues

**Symptoms:**
- IDE slow or unresponsive
- Gradle sync fails
- NDK not recognized

**Solutions:**

1. **Invalidate Caches**
   - File → Invalidate Caches and Restart
   - Choose "Invalidate and Restart"

2. **Check NDK Configuration**
   - File → Project Structure → SDK Location
   - Verify NDK location is set
   - Download NDK if missing

3. **Increase Memory**
   ```properties
   # In gradle.properties
   org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=1024m
   ```

4. **Check Plugin Versions**
   - Update Android Gradle Plugin
   - Update Kotlin plugin
   - Check libs.versions.toml

### ADB Connection Issues

**Symptoms:**
- "Device not found"
- ADB not recognizing device
- "Unauthorized" device

**Solutions:**

1. **Restart ADB**
   ```bash
   adb kill-server
   adb start-server
   adb devices
   ```

2. **Check USB Debugging**
   - Enable Developer Options (tap Build Number 7 times)
   - Enable USB Debugging
   - Accept RSA fingerprint on device

3. **Try Wireless ADB**
   ```bash
   # On device in Developer Options, enable Wireless Debugging
   adb tcpip 5555
   adb connect DEVICE_IP:5555
   ```

---

## Getting Help

### Logging Best Practices

```kotlin
// Use appropriate log levels
Log.v(TAG, "Verbose: detailed info")
Log.d(TAG, "Debug: debugging info")
Log.i(TAG, "Info: general info")
Log.w(TAG, "Warning: potential issues")
Log.e(TAG, "Error: something failed", exception)
```

### Useful Log Filters

```bash
# Audio related
adb logcat | grep -E "ToneForge|AudioEngine|AudioTrack"

# Crashes
adb logcat | grep -A 20 "FATAL EXCEPTION"

# Native crashes
adb logcat | grep -A 20 "SIGSEGV"

# Performance
adb logcat | grep -E "ANR|GC_|lowmemorykiller"
```

### Report a Bug

When reporting bugs, include:

1. **Steps to reproduce**
2. **Expected vs actual behavior**
3. **Logcat output** (filtered for relevant logs)
4. **Device info** (model, Android version)
5. **App version**
6. **Screenshots/videos** if UI issue

### Use Claude Code Commands

```bash
# Comprehensive validation
/validate

# Test coverage analysis
/test-coverage

# Debug audio issues
/debug-audio

# Review code
/review-pr
```

---

## Quick Reference

### Common Commands

```bash
# Build and install
./gradlew installDebug

# Run tests
./gradlew test

# Generate coverage
./gradlew jacocoTestReport

# Lint check
./gradlew lint

# Full validation
./scripts/functional-validation.sh

# View logs
adb logcat | grep ToneForge

# Clear app data
adb shell pm clear com.example.toneforge
```

### Important Files

- [CLAUDE.md](../CLAUDE.md) - Project overview
- [ADRs](.claude/adr/) - Architecture decisions
- [Checklists](.claude/checklists/) - Review checklists
- [Templates](.claude/templates/) - Code templates
- Scripts: `./scripts/`

### Get More Help

- Create an issue: [GitHub Issues](https://github.com/your-repo/issues)
- Check documentation: `.claude/` directory
- Review ADRs for architectural context
- Use Claude Code with `/debug-audio` or `/validate`

---

**Last Updated:** 2024-11-07
**Version:** 1.0.0
