# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project layout

The project has exactly two first-class trees:

- **`engine/`** — portable C++17 library. Single source of truth for DSP and
  audio I/O backends (Oboe on Android, AudioUnit/AVAudioSession on iOS).
- **`flutter_app/`** — Flutter cross-platform frontend. Consumes the engine
  via Dart FFI and ships the same binary to Android (gradle) and iOS
  (CocoaPods). All new features belong here.

The legacy Android Java app that lived in `app/` was removed in Fase 5 of
the migration. Its last buildable state is preserved under the
`legacy-android-final` git tag — cherry-pick from there in emergencies:

```bash
git checkout legacy-android-final -- app/
```

Fase 4.7 (the first iOS build on macOS) has **not been validated yet** at
the time of the Fase 5 removal — see `docs/migration-status.md` and the
`phase0_validation_decision` memory entry for the accepted risk rationale.

## Project Overview

ToneForge is a real-time digital multi-effects pedalboard for guitar. The
audio engine is written in portable C++17 (`engine/`) and supports low-latency
I/O on Android (Oboe) and iOS (AudioUnit/AVAudioSession). The Flutter app in
`flutter_app/` is the canonical (and only) frontend; it consumes the engine
via Dart FFI on both platforms.

## Development Commands

### Flutter app (canonical frontend)

```bash
# All Flutter commands run from flutter_app/
cd flutter_app
export PATH="$HOME/fvm/bin:$PATH"

fvm flutter pub get
fvm flutter analyze
fvm flutter build apk --debug
fvm flutter install              # to a connected device
fvm flutter run                  # hot reload

# Regenerate FFI bindings after changing engine/ headers
fvm flutter pub run ffigen --config ffigen.yaml

# iOS — only on macOS, see docs/ios-build.md
cd ios && pod install && cd ..
fvm flutter build ios --debug --no-codesign
```

### Validation and utilities

```bash
# Comprehensive functional validation (engine headers + flutter_app analyze + build)
./scripts/functional-validation.sh

# Setup development environment (Android SDK, NDK, Flutter)
./scripts/setup/setup-dev-environment.sh

# Verify environment configuration
./scripts/verify-environment.sh

# Clean logs
./scripts/clean-logs.sh
```

### Viewing logs on a connected device

```bash
# Flutter app logs (legacy Android tags no longer exist)
adb logcat -s ToneForgeAudioIO:* flutter:*
```

## Architecture Overview

ToneForge follows a two-layer architecture split cleanly along the Dart FFI boundary.

### Layer Structure

```
engine/                          # Portable C++17 audio library
├── include/toneforge/           # Public C API headers
│   ├── audio_engine.h           # DSP and effects (extern "C")
│   ├── audio_io.h               # Lifecycle: start/stop/latency/xruns
│   ├── recorder.h               # Post-FX recorder
│   └── loop_io.h                # Looper WAV save
└── src/
    ├── audio_engine.cpp         # ~1.9k lines of platform-agnostic DSP
    ├── audio_io_oboe.cpp        # Android backend (Oboe)
    ├── audio_io_coreaudio.mm    # iOS backend (AudioUnit/CoreAudio/AVAudioSession)
    ├── recorder.cpp             # Lock-free post-FX buffer → WAV writer
    ├── loop_io.cpp              # Looper mix → WAV
    └── wav_writer.{h,cpp}       # Shared WAV writer helper

flutter_app/lib/                 # Flutter frontend
├── engine/
│   ├── bindings.dart            # ffigen-generated FFI bindings (~1.7k lines)
│   └── engine.dart              # ToneforgeEngine singleton wrapper
├── features/                    # One subdir per feature (Cubit + Screen)
│   ├── tuner/
│   ├── metronome/
│   ├── effects/                 # Includes presets/
│   ├── recorder/
│   ├── looper/
│   ├── loop_library/
│   ├── automation/
│   ├── midi/
│   ├── settings/
│   └── benchmark/
└── main.dart                    # HomeScreen + navigation
```

### Key architectural components

**engine (C++)**
- Only source of truth for DSP and audio I/O. Same code compiled both on
  Android (Oboe backend, CMake) and iOS (CoreAudio backend, CocoaPods via
  `engine/toneforge_engine.podspec`).
- Public API is pure C with `extern "C"` guards + `<stdbool.h>` so ffigen
  can parse it. Never use C++ types in public headers.
- Audio callback is the only place that must remain lock-free; lifecycle
  (start/stop) uses a mutex.
- Backends chain input samples to `processTunerBuffer` (if tuner active)
  and output samples to `recorder_feed` (if recording active) — same
  pattern in both Oboe and CoreAudio backends.

**ToneforgeEngine (Dart)**
- Singleton wrapping `bindings.dart`. Loads `libtoneforge_engine.so`
  on Android and falls back to `DynamicLibrary.process()` on iOS (where
  the engine is statically linked into the Runner via CocoaPods).
- Exposes idiomatic Dart methods; calls through to the FFI bindings
  without holding any intermediate state — everything lives in C++.
- `snapshotLooperMix` / `loadLooperFromFloats` handle the `Pointer<Float>`
  marshaling explicitly, freeing any allocations in `finally`.

**Cubits (flutter_bloc)**
- One Cubit per feature in `lib/features/<feature>/<feature>_cubit.dart`.
- Each Cubit owns the engine lifecycle for its feature (`_ensurePipeline`
  → `_engine.start()` if not running → feature-specific setup).
- State classes are immutable with `copyWith`. Cubits emit new instances,
  never mutate.
- Pollers (position, waveform, latency) use `Timer.periodic` and get
  cancelled in `close()`.

## Audio System Architecture

### Audio Pipeline Flow

```
Input (Microphone/Line-in)
    ↓
Oboe input stream (audio_io_oboe.cpp on Android)
CoreAudio/RemoteIO input (audio_io_coreaudio.mm on iOS)
    ↓
Render callback (onAudioReady / renderCallback)
    ├─ processTunerBuffer(in, n) — if tuner active
    ├─ processBuffer(in, out, n) — DSP chain
    │   ├─ Gain
    │   ├─ Distortion (4 types)
    │   ├─ Delay, Reverb (3 types)
    │   ├─ Chorus, Flanger, Phaser
    │   ├─ EQ (3-band), Compressor
    │   ├─ Pitch Shift (granular, ±12 semitones)
    │   └─ (effect order configurable via setEffectOrder)
    └─ recorder_feed(out, n) — if recording active
    ↓
Oboe output stream / CoreAudio AudioBufferList
    ↓
Output (speaker / headphones / interface)
```

### Effects (10 audio effects)

1. **Gain** — level
2. **Distortion** — amount, type (Soft Clip / Hard Clip / Fuzz / Overdrive), mix
3. **Delay** — time (ms), feedback, mix
4. **Reverb** — room size, damping, type (Hall / Plate / Spring), mix
5. **Chorus** — depth, rate (Hz), mix
6. **Flanger** — depth, rate (Hz), feedback, mix
7. **Phaser** — depth, rate (Hz), feedback, mix
8. **EQ (3-band)** — low/mid/high (±12 dB), mix
9. **Compressor** — threshold (dB), ratio, attack (ms), release (ms), mix
10. **Pitch Shift** — semitones (±12), mix. Granular, ~40 ms latency added to chain.

Every effect is controlled via the same pair of calls: `setXXXEnabled(bool)`
and one or more parameter setters. See
`flutter_app/lib/features/effects/effects_cubit.dart` for the full wiring
pattern.

### Features available in the Flutter app

| Feature | Source | Notes |
|---|---|---|
| Tuner | `features/tuner/` | Chromatic, fed by the Oboe/CoreAudio input hook |
| Metronome | `features/metronome/` | BPM 40-240, time sig 1-16 |
| Effects | `features/effects/` | Full 9-effect chain, no reorder yet |
| Recorder | `features/recorder/` | Post-FX WAV capture (10-min buffer) |
| Looper | `features/looper/` | MVP single-track with waveform |
| Loop Library | `features/loop_library/` | Save/load WAV mono PCM 16-bit |
| Presets | `features/effects/preset_manager.dart` | JSON schema v1 |
| Automation | `features/automation/` | Self-contained 4-param palette |
| MIDI Learn | `features/midi/` | USB/BLE via `flutter_midi_command` |
| Settings | `features/settings/` | Placeholder (info only) |
| Benchmark | `features/benchmark/` | Debug-only start/stop + live telemetry |

See `docs/migration-status.md` for per-feature status and the deferred
backlog (drag-and-drop reorder, multi-track looper, pitch shift, slicing,
etc. — all already supported by the engine C++ API, only the UI side pending).

## Development Guidelines

### When adding a new effect

1. **Engine (C++):**
   - Add the DSP implementation in `engine/src/audio_engine.cpp`
   - Declare the public API in `engine/include/toneforge/audio_engine.h`
     inside the `extern "C"` block with `setXXXEnabled(bool)` +
     parameter setters
2. **Bindings:**
   - Regenerate FFI bindings with
     `fvm flutter pub run ffigen --config ffigen.yaml` from `flutter_app/`
3. **Dart wrapper:**
   - Add idiomatic wrappers in `flutter_app/lib/engine/engine.dart`
4. **Feature:**
   - Add the new effect to `flutter_app/lib/features/effects/models.dart`
     (new `Config` class with `copyWith`, `toJson`, `fromJson`)
   - Add setters to `EffectsCubit` and push the state in `_pushAll()`
   - Add a new `_EffectCard` in `effects_screen.dart`

### When adding a new feature screen

1. Create `flutter_app/lib/features/<feature>/`
2. `<feature>_cubit.dart` with an immutable state class, `copyWith`, and
   engine lifecycle handling
3. `<feature>_screen.dart` wraps the Cubit in a `BlocProvider`
4. Add a card in `flutter_app/lib/main.dart` pointing at the new screen
5. `fvm flutter analyze` should return 0 issues before committing

### When modifying the audio pipeline

1. Update the relevant backend (`audio_io_oboe.cpp` / `audio_io_coreaudio.mm`)
   keeping both in sync — they implement the same C ABI
2. **Test on a physical device**; Android emulators and the iOS simulator
   don't have real input audio
3. Watch for xruns via `DebugBenchmarkActivity` (adb launches it manually)
   or by adding telemetry to the Flutter app
4. Run `./scripts/functional-validation.sh` for a full engine + Flutter
   build sanity check

### Code conventions

**Flutter (Dart):**
- Cubits are pure logic. UI widgets pull state via `BlocBuilder`.
- State classes are immutable. Prefer `copyWith` over mutation.
- Timers/subscriptions must be cancelled in `Cubit.close()`.
- Never call blocking FFI from the main isolate — wrap with `Isolate.run`
  if a call ever starts blocking (current engine calls are all
  non-blocking atomics or short locks).

**Engine (C++):**
- Public headers are pure C with `__cplusplus` guards and `<stdbool.h>`
  (ffigen parses them as C).
- Audio callbacks must be lock-free. Use `std::atomic` for state flags.
- Memory allocated in callbacks = allocation-free path. Pre-allocate in
  `start()`.
- Lifecycle functions (`audio_engine_start` / `audio_engine_stop`) can
  take a mutex; never hold it across an audio frame.

## Project Structure

```
ToneForge/
├── engine/                 # Portable C++ audio engine (canonical)
│   ├── include/toneforge/  # Public C API headers (audio_engine, audio_io,
│   │                       #   recorder, loop_io)
│   ├── src/
│   │   ├── audio_engine.cpp     # DSP (~1.9k lines, platform-agnostic)
│   │   ├── audio_io_oboe.cpp    # Android backend (Oboe)
│   │   ├── audio_io_coreaudio.mm  # iOS backend (AudioUnit/CoreAudio)
│   │   ├── recorder.cpp         # Post-FX WAV recorder
│   │   ├── loop_io.cpp          # Looper WAV save
│   │   └── wav_writer.{h,cpp}   # Shared WAV writer
│   ├── CMakeLists.txt       # Cross-platform (Android builds via this)
│   └── toneforge_engine.podspec  # iOS build via CocoaPods
│
├── flutter_app/             # Canonical Flutter frontend
│   ├── lib/
│   │   ├── engine/          # Dart FFI bindings + ToneforgeEngine wrapper
│   │   ├── features/        # One subdir per feature: tuner, metronome,
│   │   │                    #   effects, recorder, looper, loop_library,
│   │   │                    #   presets (inside effects), automation, midi,
│   │   │                    #   settings, benchmark
│   │   └── main.dart        # HomeScreen + navigation
│   ├── android/             # Flutter Android project (consumes engine/)
│   ├── ios/                 # Flutter iOS project (consumes engine/ via pod)
│   ├── ffigen.yaml          # Bindings config
│   └── pubspec.yaml
│
├── docs/                   # Documentation
│   ├── migration-status.md # Live dashboard of the migration
│   ├── ios-build.md        # First-build guide on macOS
│   └── QUICKSTART.md
├── scripts/                # Development scripts
│   ├── functional-validation.sh
│   ├── verify-environment.sh
│   ├── clean-logs.sh
│   └── setup/
├── CLAUDE.md               # This file
└── README.md               # Project overview
```

The legacy Java app that lived in `app/` was removed in Fase 5. Its last
state is preserved at the `legacy-android-final` git tag.

## Dependencies

**Engine (C++):** C++17 standard library only. No external deps beyond the
platform audio frameworks (Oboe 1.9.3 on Android via prefab, AVFoundation /
AudioToolbox / CoreAudio on iOS via system frameworks).

**Flutter:** see `flutter_app/pubspec.yaml`. Key packages:
- `ffi` / `ffigen` — FFI bindings to the engine
- `flutter_bloc` — state management
- `permission_handler` — microphone + Bluetooth permissions
- `path_provider` — app documents directory
- `flutter_midi_command` — USB/BLE MIDI I/O

## Common Issues

**Audio:**
- Always test on a physical device. Emulators and simulators don't have
  real input audio.
- Xruns under stress usually mean the effective `framesPerBurst` / buffer
  size isn't the low-latency path. Check
  `adb logcat -s ToneForgeAudioIO:*` for the "Oboe engine iniciado" line
  — `framesPerBurst` should be 128-256 on a modern device, not 960.

**FFI / Native library:**
- `DynamicLibrary.open('libtoneforge_engine.so')` failing on Android
  means the .so wasn't bundled. Check
  `flutter_app/android/app/build/intermediates/cxx/` for CMake errors.
- On iOS the engine is statically linked into the Runner via the
  `toneforge_engine` pod, so `DynamicLibrary.process()` is used. If
  `Unable to find symbol` errors appear, re-run `pod install` and
  double-check `engine/toneforge_engine.podspec` source globs.

**Build:**
- After changing engine/ headers, regenerate the Dart bindings:
  `cd flutter_app && fvm flutter pub run ffigen --config ffigen.yaml`
- NDK version must match what `flutter_app/android/app/build.gradle.kts`
  declares (currently 27.1.12297006).

## Additional Resources

- [`docs/migration-status.md`](docs/migration-status.md) — feature-level
  port status, deferred backlog, pending gates
- [`docs/ios-build.md`](docs/ios-build.md) — first-build guide on macOS
- [`docs/QUICKSTART.md`](docs/QUICKSTART.md) — getting started
- [`scripts/README.md`](scripts/README.md) — script documentation
