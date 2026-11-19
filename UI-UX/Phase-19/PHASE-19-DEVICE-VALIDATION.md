# Phase 19 — Android runtime validation

The owner confirmed startup on the TECNO Spark 10 Pro after the earlier DataStore singleton fix. The latest build still requires testing on that handset.

## Android 13 emulator

The local Phase 18 APK from `477ed104f7745db6854bea9e79445ece85fa45e6` installed on an x86-64 Android 13 test guest. The first launch reached the foreground and completed bootstrap. Android logged a focus-dispatch ANR under software emulation, with the captured main thread executing Compose accessibility processing. A later launch reported `Status: ok`. The app rendered Home with the retained test relationship, and tapping Notes opened the Notes screen. [Home](evidence/android-13-home.jpg) · [Notes](evidence/android-13-notes.jpg) · [Runtime observations](evidence/android-13-runtime.txt)

Startup logs also identified eager opening of encrypted PIN storage on the UI thread. PIN storage now initializes only when a PIN operation needs it. Saved lock state still determines the first screen. PIN creation and verification use their existing background call paths. An Android 13 Robolectric regression test exercises startup with the lock enabled and disabled and fails if either path opens the PIN verifier.

The 274 dp emulator layout exposed a duplicate floating button on empty Notes that covered the inline content. Final consolidation will use the inline action for that state.

## Packaging and coverage

- The deferred-verifier checkpoint passed all 131 tests in each Gradle variant and assembled the debug APK.
- The Phase 18 APK verified with APK Signature Scheme v2 and passed ZIP alignment checks. Android reports package `com.twohearts.app`, version `1.0.0` (code 1), minimum SDK 26, and target SDK 35. The APK includes the 21 Yuki poses and ARM and x86 binaries.
- GitHub Actions produced the Phase 18 artifact. The workspace’s artifact download returned HTTP 403, so emulator validation used the local APK from the same source checkpoint. [Actions run](https://github.com/https10011/2HEARTS2/actions/runs/44433100179) · [Debug APK](https://github.com/https10011/2HEARTS2/actions/runs/44433100179/artifacts/14488065523)
- The copied emulator was stopped after the workspace reached its memory limit during the test rerun. Runtime verification of the deferred-verifier build and physical-device verification are pending at this checkpoint.
- Hardware checks still pending include fresh and returning launch, app-lock reentry, TalkBack, font scaling and keyboard behavior, media selection and playback, notification delivery, and long-session memory and battery behavior.
