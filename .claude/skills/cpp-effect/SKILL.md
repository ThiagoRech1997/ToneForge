# Add C++ Audio Effect Skill

## Description
Add a new audio effect to ToneForge's native audio engine with full integration into the Android UI.

## When to Use
- Adding a new audio effect (e.g., tremolo, wah-wah, ring modulator)
- Enhancing existing effects
- Implementing new DSP algorithms

## Instructions

1. **Research Audio Effect**
   - Study the DSP algorithm
   - Identify required parameters
   - Research optimal parameter ranges
   - Find reference implementations
   - Document the theory

2. **Implement C++ Effect**
   - Add effect class/struct to `cpp/audio_engine.cpp`
   - Implement audio processing function
   - Add parameter variables
   - Implement parameter setters
   - Handle sample rate dependencies
   - Add initialization code
   - Implement cleanup/reset

3. **Add JNI Interface**
   - Add JNI method definitions in `cpp/native-lib.cpp`
   - Export parameter setter methods
   - Export enable/disable methods
   - Add effect to pipeline routing
   - Update CMakeLists.txt if needed

4. **Update AudioEngine.kt**
   - Add external function declarations
   - Create wrapper methods
   - Add parameter validation
   - Implement proper error handling

5. **Update Domain Layer**
   - Add effect to PedalEffect enum
   - Create EffectParameter data class
   - Update EffectParameters model
   - Add to supported effects list

6. **Update AudioRepository**
   - Add methods for new effect control
   - Implement parameter update methods
   - Add effect enable/disable
   - Update effect chain management

7. **Update EffectsFragmentRefactored**
   - Add UI controls (SeekBars, Switches)
   - Add labels and descriptions
   - Implement parameter listeners
   - Add visual feedback
   - Update layout XML

8. **Update EffectsPresenter**
   - Add effect control logic
   - Handle parameter changes
   - Update state management
   - Add preset integration

9. **Update PresetManager**
   - Add effect to preset serialization
   - Update preset load/save logic
   - Ensure backward compatibility

10. **Testing**
    - Test effect in isolation
    - Test with other effects (effect chain)
    - Test parameter ranges
    - Test enable/disable
    - Check for audio artifacts
    - Measure CPU usage
    - Test preset save/load
    - Check memory usage

11. **Optimization**
    - Profile performance
    - Optimize hot code paths
    - Consider SIMD/NEON optimizations
    - Reduce memory allocations
    - Test on low-end devices

12. **Documentation**
    - Document DSP algorithm
    - Add parameter descriptions
    - Document CPU requirements
    - Add usage examples
    - Update user-facing documentation

## Effect Implementation Template

```cpp
// In audio_engine.cpp

// Effect state
struct NewEffect {
    float parameter1;
    float parameter2;
    bool enabled;
    // Internal state buffers
    float buffer[MAX_BUFFER_SIZE];

    void init(int sampleRate) {
        // Initialize
    }

    void process(float* input, float* output, int numFrames) {
        if (!enabled) {
            memcpy(output, input, numFrames * sizeof(float));
            return;
        }
        // Processing logic
    }

    void reset() {
        // Clear state
    }
};

// JNI methods in native-lib.cpp
extern "C" JNIEXPORT void JNICALL
Java_..._setNewEffectParameter1(JNIEnv* env, jobject, jfloat value) {
    // Validate and set parameter
}
```

## Testing Checklist
- [ ] Effect processes audio correctly
- [ ] All parameters work as expected
- [ ] No audio glitches or clicks
- [ ] No buffer overflows
- [ ] No memory leaks
- [ ] CPU usage acceptable
- [ ] Works in effect chain
- [ ] Presets save/load correctly
- [ ] UI responsive
- [ ] Works on different sample rates

## Expected Deliverables
- Working C++ implementation
- JNI interface methods
- Kotlin/Java integration
- UI controls and layout
- Unit tests (where applicable)
- Performance metrics
- Documentation
- Demo preset showcasing the effect
