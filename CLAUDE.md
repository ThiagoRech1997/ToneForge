# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ToneForge is a digital multi-effects pedalboard for Android featuring real-time audio processing with native C++ code. The app allows guitarists to process their instrument through various effects like distortion, delay, reverb, chorus, and more, with a modern interface and professional-grade audio processing.

## Development Commands

### Build and Testing
```bash
# Build the app
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumentation tests (requires connected device)
./gradlew connectedAndroidTest

# Run all tests and generate coverage report
./gradlew jacocoTestReport

# Functional validation (comprehensive project check)
./scripts/functional-validation.sh

# Create a release
./scripts/create-release.sh 1.0.0 "Release message"

# Install APK on connected device
adb install app/build/outputs/apk/debug/app-debug.apk

# View logs
adb logcat | grep ToneForge
```

### Project Structure
```bash
# Clean build files
./gradlew clean

# Check lint
./gradlew lint

# Clean logs
./scripts/clean-logs.sh
```

## Architecture

The project follows **Clean Architecture** principles with **MVP pattern** and is currently undergoing refactoring from a monolithic structure to a well-organized, layered architecture.

### Current Architecture Layers

1. **Domain Layer** (`domain/`)
   - `interfaces/` - AudioEngineInterface, PermissionInterface
   - `models/` - AudioState, EffectParameters, PedalEffect
   - `usecases/` - StartAudioPipelineUseCase

2. **Infrastructure Layer** (`infrastructure/`)
   - `adapters/` - AudioEngineAdapter, PermissionManagerAdapter
   - `audio/` - AudioEngine, PipelineManager, AudioStateManager
   - `midi/` - ToneForgeMidiManager
   - `presets/` - PresetManager, FavoritesManager
   - `permissions/` - PermissionManager
   - `services/` - AudioBackgroundService

3. **UI Layer** (`ui/`)
   - `activities/` - MainActivity, BaseActivity
   - `base/` - BaseFragment, BasePresenter, BaseView
   - `fragments/` - Feature-specific MVP fragments (refactored and legacy)
   - `components/` - SystemStatusController, AudioInitializer
   - `navigation/` - NavigationController

### Fragment Refactoring Status

The project is migrating from large monolithic fragments to MVP-based refactored fragments:

**✅ Refactored (MVP):**
- `HomeFragmentRefactored` - Main navigation
- `EffectsFragmentRefactored` - Complete effects system
- `LooperFragmentRefactored` - Complete looper system  
- `TunerFragmentRefactored` - Complete tuner system
- `MetronomeFragmentRefactored` - Metronome
- `RecorderFragmentRefactored` - Recorder
- `SettingsFragmentRefactored` - Settings
- `LoopLibraryFragmentRefactored` - Loop library

**Legacy (being phased out):**
- Original fragments still exist for compatibility

### Core Components

- **AudioRepository**: Centralized audio operations interface
- **NavigationController**: Manages navigation between fragments
- **AudioEngine**: JNI interface to native C++ audio processing
- **PipelineManager**: Audio pipeline lifecycle management
- **BaseActivity**: Common activity functionality (permissions, fragments)
- **SystemStatusController**: System status UI management

## Native C++ Integration

The audio processing is handled by native C++ code via JNI:

- `cpp/audio_engine.cpp` - Core audio effects implementation
- `cpp/native-lib.cpp` - JNI interface methods
- `cpp/CMakeLists.txt` - CMake build configuration

Key native features:
- Real-time audio processing
- Multiple effects (gain, distortion, delay, reverb, chorus, flanger, phaser, EQ, compressor)
- Low-latency audio pipeline
- Buffer management
- Sample rate handling

## Key Features

### Audio Effects
- **9 effects**: Gain, Distortion (4 types), Delay, Reverb, Chorus, Flanger, Phaser, 3-band EQ, Compressor
- **Customizable effect order**: Drag-and-drop reordering
- **Real-time parameter control**: Immediate audio feedback
- **Dry/wet mix controls**: For all effects

### Advanced Features
- **Preset system**: Save/load/export/import configurations
- **Favorites system**: Mark presets as favorites
- **MIDI Learn**: Map external MIDI controllers to parameters
- **Automation system**: Record and playback parameter changes
- **Background processing**: Continue audio processing with screen off
- **State recovery**: Restore settings after app backgrounding
- **Oversampling**: Improve quality for distortion and delay

### Tools
- **Real-time tuner**: Pitch detection with visual feedback
- **Metronome**: BPM control with audio integration
- **Looper**: Basic loop recording and playback
- **Recorder**: Audio recording functionality

## Important Development Notes

### Security Considerations
The recent commits show security improvements:
- Buffer size validation in JNI functions
- WAV file validation in LoopLoadUtil
- Secure URI permission handling in LoopShareUtil
- FileProvider path restrictions

### Testing Strategy
- Unit tests for presenters and use cases
- Integration tests for repositories and managers
- UI tests with Espresso
- Functional validation script for comprehensive checking
- Test coverage reporting with Jacoco

### Code Conventions
- Use existing MVP patterns for new fragments
- Follow Clean Architecture principles for new components
- Prefer refactored fragments over legacy ones
- Use dependency injection via constructors
- Implement proper error handling and logging
- Follow naming conventions: `*Contract`, `*Presenter`, `*FragmentRefactored`

### Common Tasks
- When adding new effects: Update native audio_engine.cpp, add UI controls to EffectsFragment
- When adding fragments: Create Contract, Presenter, and RefactoredFragment following MVP pattern
- When modifying audio pipeline: Update AudioRepository and test thoroughly
- Always run functional validation script before major changes
- Use NavigationController for navigation between fragments

## Dependencies

Key dependencies in `libs.versions.toml`:
- Android Gradle Plugin 8.11.0
- AppCompat 1.6.1
- Material Design 1.11.0
- ConstraintLayout 2.1.4
- Navigation Component 2.7.7
- JUnit 4.13.2, Mockito 5.8.0 for testing
- Espresso 3.5.1 for UI testing