# 🎸 ToneForge — Digital Pedalboard

ToneForge is a real-time digital multi-effects pedalboard for guitar, built on a
portable C++ DSP engine. The project is currently **mid-migration** from a
native Android Java app to a Flutter-based cross-platform app, keeping the same
C++ audio core on both sides.

## ⚠️ Migration status (Fase 5)

The repository hosts two frontends that share one engine:

| Path | Role | Status |
|---|---|---|
| [`engine/`](engine/) | Portable C++17 library — DSP + I/O backends | Active, single source of truth |
| [`app/`](app/) | Legacy Android (Java + JNI) | **Deprecated**, see [`app/DEPRECATED.md`](app/DEPRECATED.md) |
| [`flutter_app/`](flutter_app/) | New Flutter app consuming the engine via Dart FFI | Active, feature parity reached on Android |

The legacy `app/` still builds and runs — it's intentionally kept as a fallback
until the Flutter side is validated on iOS. Do not start new work there.

Full migration dashboard with per-feature status, gates and pending items:
**[`docs/migration-status.md`](docs/migration-status.md)**.

## 🧠 Why the migration

The core motivation is **iOS support**. The legacy app is Android-only. By
extracting the DSP into `engine/` (~2k lines of portable C++) and wrapping it
in either JNI (Android legacy) or Dart FFI (Flutter), the same audio core can
run on Android **and** iOS without duplicating DSP code.

The architectural plan is documented in the internal plan file that guided
the work. Summary: de-risk audio first on Android with Oboe (Fase 0), extract
the engine (Fase 1), bootstrap Flutter with FFI bindings (Fase 2), port feature
by feature (Fase 3), bring up iOS with a CoreAudio backend (Fase 4), then
deprecate the Java project (Fase 5 — **we are here**).

## 🚀 Quick start (legacy Android)

```bash
./gradlew assembleDebug
./gradlew installDebug
```

Requires Android Studio, SDK API 27+, NDK and a device with microphone input.

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
- [`docs/QUICKSTART.md`](docs/QUICKSTART.md) — legacy Android quick start
- [`CLAUDE.md`](CLAUDE.md) — architecture and development guide for AI assistants
- [`app/DEPRECATED.md`](app/DEPRECATED.md) — status of the legacy Android app
- [`scripts/README.md`](scripts/README.md) — development scripts

## 📊 Project structure

```
ToneForge/
├── engine/                   # Portable C++ audio engine (canonical)
│   ├── include/toneforge/    # Public C API headers
│   ├── src/                  # DSP + Oboe backend + CoreAudio backend
│   ├── CMakeLists.txt        # Cross-platform build
│   └── toneforge_engine.podspec  # CocoaPods spec for iOS
├── app/                      # Legacy Android Java app (deprecated)
│   └── src/main/
│       ├── cpp/              # JNI shim only — engine is now in /engine
│       └── java/             # MVP fragments + AudioRepository + JNI bindings
├── flutter_app/              # Flutter cross-platform app (canonical frontend)
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

- `scripts/functional-validation.sh` — full validation of both frontends
- `scripts/setup/setup-dev-environment.sh` — initial env setup
- `scripts/verify-environment.sh` — verify Android/Flutter toolchains
- `scripts/create-release.sh` — legacy release build

## 📄 License

This project is open source.

## 👨‍💻 Author

Thiago Fernando Rech — Android / Flutter Developer, Audio Enthusiast

---

**ToneForge** — Real-time guitar effects, now running on a portable engine. 🎸
