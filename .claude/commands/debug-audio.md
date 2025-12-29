---
description: Debug audio pipeline issues using logs and native code analysis
---

# Audio Pipeline Debugging

Help debug audio pipeline issues by analyzing logs, native code, and audio state management.

## Debugging Approach

1. **Collect Information**: Gather current symptoms and error messages
2. **Analyze Logs**: Review Android and native audio logs
3. **Check State**: Verify AudioRepository and PipelineManager state
4. **Inspect Native Code**: Review C++ audio engine if needed
5. **Provide Solutions**: Suggest fixes based on findings

## Log Collection Commands

```bash
# Filter ToneForge logs
adb logcat | grep ToneForge

# Filter audio-related logs
adb logcat | grep -E "ToneForge|AudioEngine|AudioTrack|AudioRecord"

# Clear logs and start fresh
adb logcat -c && adb logcat | grep ToneForge
```

## Common Audio Issues Checklist

### Pipeline Won't Start
- [ ] Check audio permissions granted
- [ ] Verify AudioEngine initialization
- [ ] Check sample rate compatibility
- [ ] Verify buffer size configuration
- [ ] Check for concurrent audio apps

### Crackling/Distorted Audio
- [ ] Check buffer underruns in logs
- [ ] Verify CPU usage is acceptable
- [ ] Check for excessive effect processing
- [ ] Verify sample rate consistency
- [ ] Check for thread priority issues

### Effects Not Working
- [ ] Verify effect parameters are set correctly
- [ ] Check dry/wet mix values
- [ ] Verify effect order in chain
- [ ] Check native effect implementation
- [ ] Verify JNI calls are successful

### Latency Issues
- [ ] Check buffer size settings
- [ ] Verify using low-latency audio path
- [ ] Check device audio capabilities
- [ ] Review effect processing efficiency
- [ ] Check for blocking operations in audio thread

## Native Code Analysis

If issue appears to be in native code:

1. Review `cpp/audio_engine.cpp` for the relevant effect
2. Check buffer size validation in JNI methods
3. Verify proper error handling
4. Check for memory leaks or buffer overflows
5. Review sample processing logic

## Key Files to Inspect

- `AudioEngine.kt` - JNI interface
- `PipelineManager.kt` - Pipeline lifecycle
- `AudioRepository.kt` - Audio operations
- `cpp/audio_engine.cpp` - Native effects
- `cpp/native-lib.cpp` - JNI implementations

## Debugging Steps

1. Reproduce the issue consistently
2. Collect relevant logs during issue occurrence
3. Identify the layer where issue originates (UI/Domain/Infrastructure/Native)
4. Review relevant code for potential causes
5. Suggest specific fixes or additional diagnostics needed
