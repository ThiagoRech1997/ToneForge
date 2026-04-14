# ToneForge migration status

Live dashboard of the Java → Flutter migration. Updated as things change.

## Phases

| # | Phase | Status | Notes |
|---|---|---|---|
| 0 | De-risk Oboe in-place on Android | ✅ code, ✅ conditionally validated | Moto G9 Play passed 12m19s stress with 0 xruns at 5ms burst. 3-device requirement relaxed to 1 (solo dev, no device lab). See `phase0_validation_decision` memory entry. |
| 1 | Extract `engine/` as standalone lib | ✅ | Android builds via `add_subdirectory`; Oboe backend only compiled on `if(ANDROID)`. |
| 2 | Flutter app + FFI bootstrap | ✅ | `flutter_app/` with ffigen-generated bindings; Android debug APK ships `libtoneforge_engine.so`. |
| 3 | Feature-by-feature port | ✅ 10/10 core features | All features below are working in the Flutter app. |
| 4 | iOS CoreAudio backend + Xcode build | 🟡 code written, **never compiled** | `audio_io_coreaudio.mm` + `toneforge_engine.podspec` + `Podfile` + `Info.plist` all in repo. First build has to happen on macOS. See [`ios-build.md`](ios-build.md). |
| 5 | Deprecate legacy Java app | ✅ done (risk accepted) | `app/` and the root gradle infrastructure were removed despite Fase 4.7 not having validated yet. User accepted the risk consciously. Last legacy state preserved under `legacy-android-final` git tag. |

## Feature port status (Flutter app)

| Feature | Ported | Notes / limitations |
|---|---|---|
| **Tuner** | ✅ | Reads from the same Oboe input stream via a hook in `audio_io_oboe.cpp`. |
| **Metronome** | ✅ | Fixed palette; no tap tempo, no persistence (parity with legacy). |
| **Effects** | ✅ | All 9 effects with full parameter coverage. No drag-and-drop reorder yet. |
| **Recorder** | ✅ | Implemented for the first time (legacy stubs were empty). Pre-allocated buffer, 10-minute cap. |
| **Looper** | ✅ MVP | Single track only. No reverse / speed / pitch shift / slicing / quantization — all supported by the engine, UI pending. |
| **Presets** | ✅ | JSON schema v1. No export/share. No chain order (add when reorder lands). |
| **Loop Library** | ✅ | Save/load WAV mono PCM 16-bit. No import via file picker. No sample rate metadata. |
| **Settings** | ✅ placeholder | Info-only card (pipeline state, SR, latency, backend). Real configuration pending. |
| **Home** | ✅ | Static card grid. Will need revisit when more features land. |
| **Automation** | ✅ MVP | Self-contained screen with 4-parameter palette. No global effects integration, no persistence. |
| **MIDI Learn** | ✅ MVP | USB/BLE via `flutter_midi_command`. Same 4-parameter palette as Automation. No persistence. |
| **Benchmark** | ✅ | Debug screen with start/stop and live latency/xrun readout. |

## Features NOT ported (backlog)

These exist in the legacy Java app or in the engine native API but have no
Flutter UI yet. None are blockers for Fase 4.7 or Fase 5.

### Effects
- [ ] Drag-and-drop effect chain reordering (`setEffectOrder` native exists)
- [ ] Oversampling UI toggles

### Looper (engine already supports all of these)
- [ ] Multi-track (up to 8 tracks with volume/mute/solo/remove)
- [ ] Reverse
- [ ] Speed (0.5× – 2×)
- [ ] Pitch shift (±12 semitones)
- [ ] Stutter
- [ ] Slicing (set points, reorder, randomize, reverse slices)
- [ ] Cut region / fade in / fade out
- [ ] Auto compression, auto normalization
- [ ] Low-pass / high-pass filters
- [ ] Reverb tail between loops
- [ ] Quantization to BPM grid
- [ ] Auto fade in/out
- [ ] BPM sync

### MIDI
- [ ] Mapping persistence between runs (JSON like Presets)
- [ ] Program Change / Note On / Pitch Bend handling (only CC for now)
- [ ] Bidirectional feedback to motorised controllers

### Files
- [ ] Share/export presets via share_plus
- [ ] Share/export loops and recordings
- [ ] WAV file picker import for loops

### Settings
- [ ] Latency mode / buffer size selection (needs new engine API)
- [ ] Theme selector (Material 3 color seed swapping)
- [ ] Toggle for C++/Oboe vs `PipelineManager` in the legacy app (currently
      only exposed in DebugBenchmarkActivity via adb)

## Gates pending

### Fase 4.7 — first iOS build on macOS

**Owner:** requires physical macOS machine with Xcode and an iPhone with an
audio interface. Not achievable from the Linux development environment.

**Procedure:** `docs/ios-build.md` step by step.

**Expected issues on first build:**
1. Minor fixups in `audio_io_coreaudio.mm` — `AudioUnitSetProperty` call
   ordering or scope constants.
2. Possible `HEADER_SEARCH_PATHS` tweak in the podspec.
3. Missing `<algorithm>` or similar includes that clang-Linux tolerated.

**Exit criteria:**
- iOS debug build passes without blocking warnings
- Round-trip latency on iPhone ≤ Moto G9 Play result (20-40ms round-trip)
- 10-minute stress test with all effects, 0 xruns, 0 glitches
- Background audio functional (home button, lock screen, incoming call)

### Beta rollout mitigations (pending)

These compensate for the single-device Fase 0.6 validation. They're no
longer blocking anything now that Fase 5 has been executed, but they still
matter before any public release:

- [ ] Device telemetry: on each engine start, log device model, manufacturer,
      effective framesPerBurst, final xrun count — local file export, no
      network
- [ ] Remote config flag to disable the C++/Oboe backend if production
      telemetry flags device-specific issues

The "expose C++/Oboe toggle in legacy Settings" mitigation was done before
the removal (commit f8f18eb) and the resulting code is preserved under the
`legacy-android-final` tag for reference.

## Fase 5 removal — done

Executed on 2026-04-14 at commit TBD (the commit containing this doc
update). Actions taken:

- Tagged `legacy-android-final` on the last buildable legacy state
- `git rm -r app/`
- Removed root gradle infrastructure (`gradlew`, `gradle/`,
  `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`)
- Removed legacy-only scripts (`test-app-device.sh`, `create-release.sh`)
- Rewrote `CLAUDE.md` and `README.md` to drop all Java/JNI references
- Rewrote `scripts/functional-validation.sh` to validate only
  engine/ + flutter_app/

**Accepted risk:** Fase 4.7 was NOT validated before this removal. If the
iOS build turns out to require invasive changes to the engine or the
CoreAudio backend, the only fallback is the `legacy-android-final` tag.
The user accepted this consciously to unblock the migration.

## Key commits

All on branch `flutter-migration`:

```
f8f18eb feat(legacy,settings): expose C++/Oboe pipeline toggle (last legacy change)
1335679 fix(debug): capture benchmark report before stopping the pipeline
a595ab3 feat(engine,ios): add CoreAudio backend and iOS pod glue for phase 4
fd3867d feat(flutter): add MIDI Learn screen with flutter_midi_command
ca5041e feat(flutter): add Settings placeholder and Automation MVP
d9a2b24 feat(engine,flutter): add Loop Library with WAV save/load
8b50c12 feat(flutter): add preset save/load to Effects screen
9b83da4 feat(flutter): port Looper MVP with FFI waveform snapshot
1ce019c feat(flutter): port full Effects screen with all 9 effects
f1f9892 feat(flutter): port Metronome to Flutter with Cubit
8afb9d2 feat(flutter,engine): port Tuner to Flutter
8a56e60 feat(engine,flutter): implement audio recorder in C++
d89bf95 feat(flutter): bootstrap flutter_app with FFI bindings
8240437 refactor(engine): extract DSP + Oboe backend into standalone engine/
a7e5cd4 feat(audio): add Oboe C++ pipeline behind feature flag (phase 0)
```
