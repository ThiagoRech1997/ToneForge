# Legacy Android app — DEPRECATED

This directory hosts the original Java/Android frontend of ToneForge. It is
**deprecated** as of the migration to a Flutter-based frontend that shares
the same C++ engine (`engine/`).

## Why this is still here

- It's the **production-validated fallback** for the audio pipeline. Until
  the Flutter iOS target is compiled and validated (Fase 4.7 of the migration
  plan), the Java app is the only guaranteed-working frontend.
- It still **builds and runs intact**. Engine source code has been moved to
  `engine/`, but `app/src/main/cpp/CMakeLists.txt` still links it via
  `add_subdirectory`, and `app/src/main/cpp/native-lib.cpp` still provides
  the JNI shim.
- The **feature flag** in
  [`AudioRepository.setUseCppPipeline`](src/main/java/com/thiagofernendorech/toneforge/data/repository/AudioRepository.java)
  lets the app use either the legacy `AudioRecord`/`AudioTrack` pipeline or
  the new C++/Oboe pipeline (same one the Flutter app uses). This coexistence
  is intentional and is how Fase 0 validation was done.

## Policy for this directory

1. **Do not add new features here.** New features belong in `flutter_app/`.
   If a new feature must exist in this app for a specific reason (e.g. a
   legacy user requesting something while Flutter iOS is not ready), document
   the reason in the commit message.
2. **Bug fixes are allowed** only when the same bug does not exist in the
   Flutter app, or when the legacy app is serving as a fallback for users
   whose device hasn't been validated against the Flutter path.
3. **Do not remove code from here** until Fase 4.7 (iOS build validation)
   has passed. Removing the Java app without an iOS fallback locks us into
   a broken state if CoreAudio needs invasive changes.
4. **Refactors inside `app/` are frozen.** No MVP tidy-ups, no Clean
   Architecture polishing, no new abstractions. Every hour spent refactoring
   legacy is an hour not spent porting features forward.

## What's already ported to the Flutter app

All 9 audio effects, tuner, metronome, recorder, looper MVP, presets,
loop library, settings placeholder, automation and MIDI learn. See
[`docs/migration-status.md`](../docs/migration-status.md) for the live
status and what's deferred.

## What's NOT ported yet

Features that exist in this legacy app but are not in `flutter_app/`:

- **Drag-and-drop effect chain reordering** (`setEffectOrder` native exists,
  Flutter UI pending)
- **Multi-track looper** (engine supports 8 tracks, Flutter uses only the mix)
- **Looper advanced**: reverse, speed, pitch shift, stutter, slicing, cut,
  fades, auto-compression/normalization, filters, reverb tail, quantization,
  auto fade in/out
- **MIDI mapping persistence** (Flutter mappings live in memory only)
- **Preset export/import via share intent**
- **Latency mode / advanced audio settings UI**
- **Custom drawable assets for the HILAVA visual identity** (the Flutter app
  uses Material 3 defaults for now)

When you port one of these to `flutter_app/`, tick it off in
`docs/migration-status.md`.

## When will this directory disappear

When all of the following are true, `app/` gets archived to a tag and
deleted in a follow-up commit:

1. **Fase 4.7 passed** — Flutter app builds and runs on a physical iPhone
   with acceptable latency (see exit criteria in
   [`docs/ios-build.md`](../docs/ios-build.md)).
2. **Feature backfill complete** — either all items in "What's NOT ported
   yet" above are done, or the product owner explicitly signs off on
   shipping the Flutter app without them.
3. **Beta rollout stable** — the Flutter app ran in a closed beta for 2-3
   weeks across enough devices that we're confident there's no device-specific
   audio regression (the single-device Fase 0.6 validation doesn't cover
   OEM variability).
4. **Release pipeline ported** — fastlane / signing / Play Store publishing
   scripts point at `flutter_app/` instead of `app/`.

Expected target: when items 1-4 are ticked, a commit with message
`chore: archive legacy Android app (see tag legacy-android-final)` removes
this directory. The tag preserves the last buildable state so we can still
cherry-pick from it in emergencies.

## If you're reading this after the removal

If you arrived here from git history looking for the legacy Java code,
check the `legacy-android-final` tag:

```bash
git checkout legacy-android-final -- app/
```
