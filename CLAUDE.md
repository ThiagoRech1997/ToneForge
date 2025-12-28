# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ToneForge is a digital multi-effects pedalboard for Android featuring real-time audio processing with native C++ code. The app allows guitarists to process their instrument through various effects like distortion, delay, reverb, chorus, and more, with a modern interface and professional-grade audio processing.

## Development Commands

### Build and Testing
```bash
# Build debug APK
./gradlew assembleDebug

# Build and install on device
./gradlew installDebug

# Clean build
./gradlew clean assembleDebug

# Run unit tests
./gradlew test

# Run instrumentation tests (requires connected device)
./gradlew connectedAndroidTest

# Generate coverage report
./gradlew jacocoTestReport

# Run functional validation (comprehensive check)
./scripts/functional-validation.sh

# Install APK manually
adb install app/build/outputs/apk/debug/app-debug.apk

# View logs filtered by ToneForge
adb logcat -s ToneForge:* AudioEngine:* PipelineManager:* AudioRepository:*
```

### Testing Individual Components
```bash
# Run specific test class
./gradlew test --tests "com.thiagofernendorech.toneforge.SpecificTestClass"

# Run tests with pattern
./gradlew test --tests "*Presenter*"

# Run instrumented tests for specific class
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.thiagofernendorech.toneforge.YourTestClass
```

### Environment Setup
```bash
# Setup development environment
./scripts/setup/setup-dev-environment.sh

# Verify environment configuration
./scripts/verify-environment.sh

# Test on connected device
./scripts/test-app-device.sh
```

### Other Utilities
```bash
# Check lint
./gradlew lint

# Create release
./scripts/create-release.sh 1.0.0 "Release message"

# Clean logs
./scripts/clean-logs.sh
```

## Architecture Overview

ToneForge follows **Clean Architecture** principles with **MVP (Model-View-Presenter)** pattern. The project is organized into three main layers:

### Layer Structure

```
app/src/main/java/com/thiagofernendorech/toneforge/
├── domain/              # Business logic (framework-independent)
│   ├── interfaces/      # Port definitions
│   ├── models/          # Domain models
│   └── usecases/        # Use cases
├── data/                # Data layer
│   └── repository/      # AudioRepository (centralized audio operations)
├── infrastructure/      # Framework implementations
│   ├── adapters/        # Adapter implementations
│   ├── audio/           # AudioEngine, PipelineManager, AudioStateManager
│   ├── loops/           # Loop management utilities
│   ├── midi/            # MIDI integration
│   ├── permissions/     # Permission handling
│   ├── presets/         # Preset management
│   ├── services/        # Background services
│   ├── state/           # State management
│   └── ui/              # UI utilities
└── ui/                  # Presentation layer
    ├── activities/      # MainActivity, BaseActivity
    ├── base/            # BaseFragment, BasePresenter, BaseView
    ├── fragments/       # Feature fragments (MVP pattern)
    ├── components/      # Reusable components
    ├── navigation/      # NavigationController
    └── widgets/         # Custom widgets
```

### Key Architectural Components

**AudioRepository (Data Layer)**
- Centralized singleton for all audio operations
- Abstracts complexity of audio managers
- Manages: AudioEngine, PipelineManager, StateManager, LatencyManager, PresetManager, AutomationManager, MidiManager
- Located at: `data/repository/AudioRepository.java`

**PipelineManager (Infrastructure)**
- Manages audio pipeline lifecycle (initialization, start, stop)
- Handles AudioRecord and AudioTrack
- Manages real-time audio thread
- Detects optimal sample rate
- Located at: `infrastructure/audio/PipelineManager.java`

**AudioEngine (Infrastructure)**
- JNI bridge to native C++ audio processing
- Singleton pattern with native library loading
- Wrapper methods for all effects with error handling
- Located at: `infrastructure/audio/AudioEngine.java`

**NavigationController (UI)**
- Manages fragment navigation throughout the app
- Handles fragment transactions and back stack
- Located at: `ui/navigation/NavigationController.java`

### Native C++ Layer

```
app/src/main/cpp/
├── audio_engine.cpp     # Core audio DSP implementation
├── native-lib.cpp       # JNI interface (Java ↔ C++)
└── CMakeLists.txt       # CMake build configuration
```

**JNI Naming Convention:**
- All JNI functions use `Native` suffix for compatibility (e.g., `setGainEnabledNative`)
- Wrapper methods in Java call these native functions with error handling
- Effects control: `setXXXEnabled()`, `setXXXLevel()`, `setXXXMix()`

### MVP Pattern Implementation

All refactored fragments follow this structure:

```
fragments/<feature>/
├── <Feature>Contract.java        # Interface definitions (View, Presenter)
├── <Feature>Presenter.java       # Business logic
└── <Feature>FragmentRefactored.java  # UI implementation
```

**✅ Refactored Fragments (MVP):**
- `HomeFragmentRefactored` - Main navigation hub
- `EffectsFragmentRefactored` - Complete effects system with drag-and-drop reordering
- `LooperFragmentRefactored` - Multi-track loop recorder with advanced features
- `TunerFragmentRefactored` - Real-time pitch detection tuner
- `MetronomeFragmentRefactored` - BPM-synchronized metronome
- `RecorderFragmentRefactored` - Audio recording functionality
- `SettingsFragmentRefactored` - App settings and preferences
- `LoopLibraryFragmentRefactored` - Loop file management

**Legacy Fragments:** Original fragments still exist for compatibility but should not be modified.

## Audio System Architecture

### Audio Pipeline Flow

```
Input (Microphone/Line-in)
    ↓
AudioRecord (PipelineManager)
    ↓
Native C++ Processing (audio_engine.cpp)
    ├─ Effect 1 (customizable order)
    ├─ Effect 2
    ├─ Effect N
    └─ Mix & Output Buffer
    ↓
AudioTrack (PipelineManager)
    ↓
Output (Speaker/Headphones)
```

### Effects System

**9 Audio Effects with full parameter control:**
1. **Gain** - Volume control
2. **Distortion** - 4 types (Soft Clip, Hard Clip, Fuzz, Overdrive)
3. **Delay** - Time, feedback, mix, BPM sync
4. **Reverb** - Room size, damping, type (Hall/Plate/Spring)
5. **Chorus** - Depth, rate, mix
6. **Flanger** - Depth, rate, feedback, mix
7. **Phaser** - Depth, rate, feedback, mix
8. **EQ (3-band)** - Low, mid, high gain controls
9. **Compressor** - Threshold, ratio, attack, release

**Effect Management:**
- Each effect has `setXXXEnabled()` method in AudioRepository
- Calls `updateEffectsActiveStatus()` to notify PipelineManager
- Supports drag-and-drop reordering via `setEffectOrder()`
- Dry/wet mix control for all effects

### State Management

**AudioStateManager:**
- Tracks pipeline state: STOPPED → INITIALIZING → RUNNING
- Notifies observers of state changes
- Integrated with AudioRepository

**State Recovery:**
- App saves effect parameters and pipeline state
- Restores settings after backgrounding
- Handles lifecycle events properly

## Key Features & Systems

### Preset System
- Save/load effect configurations
- Export/import presets (JSON format)
- Favorites marking
- Managed by PresetManager in infrastructure layer

### MIDI Integration
- MIDI Learn for parameter mapping
- External controller support
- CC message processing
- Managed by ToneForgeMidiManager

### Automation System
- Record parameter changes over time
- Playback automation sequences
- Sync with metronome BPM
- Managed by AutomationManager

### Background Processing
- AudioBackgroundService keeps pipeline running
- Foreground notification with controls
- Handles audio focus changes
- Battery optimization compatibility

### Looper System
- Multi-track recording
- Loop playback with volume/mute/solo per track
- Advanced features: reverse, speed, pitch shift, slicing
- Auto-compression and normalization
- BPM sync and quantization

## Development Guidelines

### When Adding New Effects

1. **Native Layer (C++):**
   - Add effect implementation in `audio_engine.cpp`
   - Add JNI wrapper in `native-lib.cpp` with `Native` suffix
   - Example: `Java_com_thiagofernendorech_toneforge_AudioEngine_setNewEffectEnabledNative()`

2. **Java Layer:**
   - Declare native method in `AudioEngine.java`
   - Add wrapper method with error handling in `AudioEngine.java`
   - Add `setNewEffectEnabled()` in `AudioRepository.java`
   - Call `updateEffectsActiveStatus()` in the repository method

3. **UI Layer:**
   - Add controls to `EffectsFragmentRefactored`
   - Update effect parameters model
   - Add to preset system

### When Adding New Fragments

1. Create MVP structure:
   ```java
   // Contract
   public interface NewFeatureContract {
       interface View extends BaseView<Presenter> { }
       interface Presenter extends BasePresenter { }
   }

   // Presenter
   public class NewFeaturePresenter implements NewFeatureContract.Presenter { }

   // Fragment
   public class NewFeatureFragmentRefactored extends BaseFragment
       implements NewFeatureContract.View { }
   ```

2. Register in NavigationController
3. Add navigation from HomeFragment
4. Follow existing patterns in refactored fragments

### When Modifying Audio Pipeline

1. Update `AudioRepository` interface
2. Test with `PipelineManager` integration
3. Verify state management with `AudioStateManager`
4. Test on physical device (emulators don't support real-time audio well)
5. Check logs for buffer underruns or latency issues
6. Run functional validation: `./scripts/functional-validation.sh`

### Code Conventions

**Naming:**
- Contracts: `*Contract.java`
- Presenters: `*Presenter.java`
- Refactored Fragments: `*FragmentRefactored.java`
- JNI methods: `*Native()` suffix

**Architecture:**
- Use dependency injection via constructors
- Follow Clean Architecture layer boundaries
- Domain layer should have no Android dependencies
- Infrastructure adapts framework to domain interfaces

**Error Handling:**
- All JNI calls wrapped in try-catch with UnsatisfiedLinkError
- Proper logging with appropriate log levels
- Check `AudioEngine.isNativeLibraryLoaded()` before native calls

**Testing:**
- Unit tests for presenters (mock views)
- Integration tests for repositories
- UI tests with Espresso for fragments
- Always check Jacoco coverage report

## Project Structure

```
ToneForge/
├── app/                    # Android application source
│   ├── src/main/
│   │   ├── cpp/           # Native C++ audio processing
│   │   ├── java/          # Java/Kotlin source (Clean Architecture layers)
│   │   └── res/           # Android resources
│   └── build.gradle.kts
├── docs/                   # Complete documentation
│   ├── setup/             # Environment setup guides
│   ├── testing/           # Test reports and validation
│   └── README.md          # Documentation index
├── scripts/               # Development scripts
│   ├── setup/             # Setup automation
│   ├── functional-validation.sh
│   └── test-app-device.sh
├── logs/                  # Build and test logs (git ignored)
├── CLAUDE.md             # This file
└── README.md             # Project overview
```

## Security Considerations

Recent security improvements include:
- Buffer size validation in all JNI functions
- WAV file validation in LoopLoadUtil
- Secure URI permission handling in LoopShareUtil
- FileProvider path restrictions
- Input sanitization for native calls

## Dependencies

Key dependencies (see `libs.versions.toml`):
- Android Gradle Plugin 8.11.0
- AppCompat 1.6.1
- Material Design 1.11.0
- ConstraintLayout 2.1.4
- Navigation Component 2.7.7
- JUnit 4.13.2, Mockito 5.8.0
- Espresso 3.5.1
- Jacoco for coverage reporting

## Common Issues

**Audio Pipeline:**
- Always test on physical device (emulators have poor audio support)
- Check logcat for "PipelineManager" tags to debug pipeline issues
- Buffer underruns indicate sample rate mismatch or processing overload
- Use `AudioStateManager` to track pipeline state

**JNI:**
- Native library load failures: check CMakeLists.txt and ABI support
- UnsatisfiedLinkError: verify JNI method signatures match exactly
- Use `Native` suffix for all JNI functions

**Build:**
- Clean build if CMake changes: `./gradlew clean`
- NDK version compatibility check in `build.gradle.kts`
- Gradle sync required after CMakeLists.txt changes

## Additional Resources

- **Quick Start:** See [docs/QUICKSTART.md](docs/QUICKSTART.md)
- **Setup Guides:** See [docs/setup/](docs/setup/)
- **Testing Reports:** See [docs/testing/](docs/testing/)
- **Scripts Documentation:** See [scripts/README.md](scripts/README.md)
- **Architecture Docs:** See [docs/](docs/) for detailed architecture documentation
