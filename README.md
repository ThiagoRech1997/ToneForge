# 🎸 ToneForge — Digital Pedalboard

ToneForge is a real-time digital multi-effects pedalboard for guitar, built on
a portable C++ DSP engine consumed by a single Flutter frontend that targets
both Android and iOS.

## 🧭 Layout

| Path | Role |
|---|---|
| [`engine/`](engine/) | Portable C++17 library — DSP, Oboe backend (Android), CoreAudio backend (iOS) |
| [`flutter_app/`](flutter_app/) | Flutter cross-platform frontend consuming the engine via Dart FFI |
| [`docs/`](docs/) | Migration dashboard, iOS build guide, quick start |
| [`scripts/`](scripts/) | Environment setup and validation scripts |

The legacy Android Java app that lived in `app/` was removed in Fase 5 of the
migration. Its last buildable state is preserved under the
`legacy-android-final` git tag — cherry-pick from there if you ever need the
old Java frontend back:

```bash
git checkout legacy-android-final -- app/
```

Full migration dashboard with per-feature status, deferred backlog and
pending gates: **[`docs/migration-status.md`](docs/migration-status.md)**.

## 🧠 Why the migration happened

The original ToneForge was an Android-only Java app with ~31k lines of Java
around ~3.4k lines of C++ DSP behind JNI. The core motivation for migrating
to Flutter was **iOS support**. The DSP in `engine/audio_engine.cpp` was
already portable (no Android-specific headers, `extern "C"`, buffer-centric),
so the migration strategy was: extract the engine, share it between an Oboe
backend for Android and a CoreAudio backend for iOS, and consume it from
either a JNI shim (legacy Android) or Dart FFI (Flutter). After the Flutter
port reached feature parity with the legacy app and the Oboe backend was
validated on a low-end device, the Java frontend was retired.

## 🚀 Quick start (Flutter app)

```bash
cd flutter_app
export PATH="$HOME/fvm/bin:$PATH"   # or point to your Flutter install
fvm flutter pub get
fvm flutter build apk --debug
fvm flutter install
```

Requires Flutter stable (tested on 3.41.6), an Android device and the NDK
matching `ndkVersion = 27.1.12297006` in `flutter_app/android/app/build.gradle.kts`.

For iOS, see **[`docs/ios-build.md`](docs/ios-build.md)** — the first build must
happen on a macOS machine with Xcode; the CoreAudio backend has been written
blindly on Linux and expects ~1-3 minor fixups on first compile.

## 🧰 Main technologies

- **C++17** — DSP and I/O backends (`engine/`)
- **Oboe** — Android low-latency audio I/O (`engine/src/audio_io_oboe.cpp`)
- **AudioUnit / AVAudioSession** — iOS audio I/O (`engine/src/audio_io_coreaudio.mm`)
- **Dart FFI + ffigen** — Flutter → engine bindings (`flutter_app/lib/engine/`)
- **flutter_bloc** — state management in the Flutter app
- **Java + JNI** — legacy Android frontend (`app/`)
- **CMake** — Android/legacy build; **CocoaPods** — iOS build via
  `engine/toneforge_engine.podspec`

## ✨ Features (Flutter app)

All features below run against the same C++ engine via Dart FFI:

- **Real-time audio pipeline** with Oboe (Android) / CoreAudio (iOS)
- **9 effects**: Gain, Distortion (4 types), Delay, Reverb (3 types), Chorus,
  Flanger, Phaser, 3-band EQ, Compressor
- **Tuner** — chromatic, real-time pitch detection sharing the same input stream
- **Metronome** — BPM, time signature, synced visual pulse
- **Recorder** — WAV capture post-FX from the engine's output buffer
- **Looper** — record/play/clear with waveform visualization (MVP: single track)
- **Loop Library** — persist loops as WAV, reload into the engine
- **Presets** — save/load full effect chain state as JSON
- **Automation** — record and replay parameter changes over time
- **MIDI Learn** — map USB/BLE MIDI controllers to parameters

Deferred for later iterations (engine already supports them, UI side pending):
multi-track looper, effect chain drag-and-drop reorder, loop reverse / speed /
pitch shift / slicing, MIDI mapping persistence, share/export. See
[`docs/migration-status.md`](docs/migration-status.md) for the full list.

## 📱 Compatibility

- **Android**: 8.1+ (API 27), ARM64, ARM32, x86, x86_64
- **iOS**: 13.0+ (planned, first build on macOS pending)
- **Sample rate**: 48 kHz (device-preferred), mono float32 pipeline

## 📚 Documentation

- [`docs/migration-status.md`](docs/migration-status.md) — live dashboard of the migration
- [`docs/ios-build.md`](docs/ios-build.md) — how to do the first iOS build on macOS
- [`docs/QUICKSTART.md`](docs/QUICKSTART.md) — quick start
- [`CLAUDE.md`](CLAUDE.md) — architecture and development guide for AI assistants
- [`scripts/README.md`](scripts/README.md) — development scripts

## 📊 Project structure

```
ToneForge/
├── engine/                   # Portable C++ audio engine (single source of truth)
│   ├── include/toneforge/    # Public C API headers
│   ├── src/                  # DSP + Oboe backend + CoreAudio backend
│   ├── CMakeLists.txt        # Cross-platform build (consumed by Flutter Android)
│   └── toneforge_engine.podspec  # CocoaPods spec (consumed by Flutter iOS)
├── flutter_app/              # Flutter cross-platform frontend
│   ├── lib/
│   │   ├── engine/           # Dart FFI wrapper around the C engine
│   │   ├── features/         # Cubit + screen per feature (tuner, effects, ...)
│   │   └── main.dart         # Home + navigation
│   ├── android/              # Flutter Android project (consumes engine/)
│   └── ios/                  # Flutter iOS project (consumes engine/ via pod)
├── docs/                     # Documentation
├── scripts/                  # Dev and validation scripts
└── CLAUDE.md                 # Architecture guide
```

## 🔧 Development scripts

All scripts live in [`scripts/`](scripts/):

- `scripts/functional-validation.sh` — engine headers + Flutter analyze + Flutter build APK
- `scripts/setup/setup-dev-environment.sh` — initial env setup
- `scripts/verify-environment.sh` — verify Android/Flutter toolchains
- `scripts/clean-logs.sh` — clean build/test logs

## 📄 License

This project is open source.

## 👨‍💻 Author

Thiago Fernando Rech — Android / Flutter Developer, Audio Enthusiast

---

**ToneForge** — Real-time guitar effects, now running on a portable engine. 🎸
