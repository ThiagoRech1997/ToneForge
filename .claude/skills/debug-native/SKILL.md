# Debug Native Code Skill

## Description
Debug and troubleshoot issues in ToneForge's native C++ audio processing code using Android NDK debugging tools.

## When to Use
- Native crashes or segfaults
- Audio glitches or artifacts
- Performance issues in audio processing
- Memory leaks in native code
- JNI errors or crashes

## Instructions

1. **Prepare Debug Build**
   - Build with debug symbols
   - Enable ASAN (Address Sanitizer) if needed
   - Set NDK debug flags in build.gradle:
     ```kotlin
     android {
         defaultConfig {
             ndk {
                 debugSymbolLevel = 'FULL'
             }
         }
         buildTypes {
             debug {
                 debuggable = true
                 jniDebuggable = true
             }
         }
     }
     ```

2. **Collect Crash Information**
   - Get logcat output: `adb logcat -d > crash.log`
   - Look for native stack traces
   - Note crash address and signal
   - Identify the problematic function
   - Check for JNI exceptions

3. **Analyze Stack Trace**
   - Use ndk-stack to symbolicate:
     ```bash
     adb logcat | ndk-stack -sym app/build/intermediates/cmake/debug/obj
     ```
   - Identify the crash location
   - Review nearby code
   - Check for common issues:
     - Buffer overflows
     - Null pointer dereferences
     - Array out of bounds
     - Use after free
     - Integer overflow

4. **Memory Analysis**
   - Check for memory leaks
   - Use AddressSanitizer (ASAN)
   - Look for buffer overflows
   - Verify proper cleanup
   - Check for dangling pointers

5. **Audio Processing Issues**
   - Check buffer sizes
   - Verify sample rate handling
   - Look for division by zero
   - Check for NaN or Inf values
   - Verify parameter ranges
   - Check for audio clipping

6. **JNI Debugging**
   - Verify JNI method signatures
   - Check for JNI exceptions
   - Use CheckJNI mode:
     ```bash
     adb shell setprop debug.checkjni 1
     ```
   - Verify global/local references
   - Check for reference leaks

7. **Use Native Debugger**
   - Attach LLDB debugger
   - Set breakpoints in C++ code
   - Inspect variables
   - Step through code
   - Watch memory addresses

8. **Add Debug Logging**
   - Add __android_log_print statements
   - Log function entry/exit
   - Log parameter values
   - Log buffer states
   - Use different log levels

9. **Reproduce Issue**
   - Create minimal test case
   - Isolate the problem
   - Test with different inputs
   - Test on different devices
   - Check different Android versions

10. **Fix and Verify**
    - Implement fix
    - Add null checks
    - Add bounds checking
    - Add input validation
    - Test thoroughly
    - Verify fix on multiple devices

11. **Prevent Future Issues**
    - Add assertions
    - Add unit tests for native code
    - Document assumptions
    - Add input validation
    - Use safer APIs

## Common Native Issues

### Buffer Overflow
```cpp
// BAD
float buffer[256];
for (int i = 0; i <= 256; i++) { // Off by one!
    buffer[i] = 0.0f;
}

// GOOD
float buffer[256];
for (int i = 0; i < 256; i++) {
    buffer[i] = 0.0f;
}
```

### Null Pointer
```cpp
// BAD
void processAudio(float* input) {
    float value = input[0]; // Crash if input is null
}

// GOOD
void processAudio(float* input) {
    if (input == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "Audio", "Null input");
        return;
    }
    float value = input[0];
}
```

### Memory Leak
```cpp
// BAD
float* allocateBuffer() {
    return new float[1024]; // Never freed!
}

// GOOD
class AudioBuffer {
    float* data;
public:
    AudioBuffer() : data(new float[1024]) {}
    ~AudioBuffer() { delete[] data; }
};
```

## Debugging Commands

```bash
# Get device ABI
adb shell getprop ro.product.cpu.abi

# Enable CheckJNI
adb shell setprop debug.checkjni 1

# Get native crash tombstone
adb pull /data/tombstones/

# Symbolicate stack trace
ndk-stack -sym app/build/intermediates/cmake/debug/obj < crash.log

# Run with ASAN
# Add to build.gradle:
# externalNativeBuild.cmake.arguments += "-DANDROID_STL=c++_shared"
# externalNativeBuild.cmake.cFlags += "-fsanitize=address"

# Monitor memory
adb shell dumpsys meminfo com.thiagofernendorech.toneforge
```

## Expected Deliverables
- Root cause analysis
- Symbolicated stack trace
- Fix implementation
- Test cases to prevent regression
- Documentation of the issue
- Updated code with proper checks
