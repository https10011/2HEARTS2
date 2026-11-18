# TwoHearts autonomous execution progress

## Current checkpoint

- **Active work** — Phase 6 (Memories). The owner confirmed that TwoHearts now runs on their TECNO Spark 10 Pro. The startup gate is clear.
- **Completed UI/UX phases at intake** — Phase 0 reconnaissance, Phase 1 visual language, Phase 2 shell and navigation, Phase 3 onboarding, and Phase 4 Home. Their implementation and reports are in the repository history.
- **Completed since intake** — Phase 5 replaced the Us directory with a live relationship summary and feature previews. All 87 local tests and the APK build passed. Compose and Android 13 emulator checks verified the hub and retained local data. [Phase 5 report](Phase-5/PHASE-5-US-RELATIONSHIP-HUB.md)
- **Pending UI/UX phases** — Phase 6 through Phase 20 in the [master directive](MASTER-UI-UX-DIRECTIVE.md). Phase 6 starts with restoring the Memories media flow and redesigning browsing, creation, and detail.

## Repository and startup audit

On 2026-11-18, the checkout at `/workspace/2HEARTS2` was clean on `master`. Both local `HEAD` and fetched `origin/master` resolved to `722ed471d39b6f84104e99363883008fd69bf409`. GitHub reported push permission and an unprotected `master` branch. The checkout contains one native `app` module, the migration and phase reports, the archived Capacitor implementation and 77 historical screen references, the official brand resources, and 21 supplied Yuki poses. The Yuki directive is tracked as [`Yuki Assets/Yuki_Game_Engine.md`](../Yuki%20Assets/Yuki_Game_Engine.md).

The startup audit traced the packaged manifest and AndroidX initializers through `MainActivity`, `BootstrapService`, Room, notification channels, Android Keystore and DataStore, appearance settings, Compose setup, `AppRouter`, onboarding, and the repository-backed Home route. The active app uses Kotlin and Compose with Room schema 13 and process-wide Preferences DataStore. `722ed47` makes both startup settings consumers share one store. The earlier DataStore exception and Android 10 comparison are recorded in the [previous launch investigation](REAL-DEVICE-LAUNCH-FIX.md).

## Priority Zero build and visual baseline

| Check | Result |
| --- | --- |
| Last application source commit tested | `722ed471d39b6f84104e99363883008fd69bf409` |
| Local Gradle | `:app:testDebugUnitTest :app:assembleDebug` succeeded with JDK 17 and Android SDK 35. All 84 tests passed, including four settings-store regression tests. |
| APK | Local `app-debug.apk` verified with `apksigner` and `zipalign`. Package `com.twohearts.app`, version `1.0.0` (code 1), min SDK 26, target SDK 35, launcher `MainActivity`, and bundled ARM and x86 native libraries. |
| Compose visual inspection | Re-rendered the existing Android/Robolectric harnesses and reviewed Welcome and Home in default, dark, and 360 dp extra-large text layouts. Populated cases used the existing test fixtures. [Compose renders](PRIORITY-ZERO-evidence/compose-startup-baseline.jpg) |
| GitHub Actions | The original run passed and its historical artifact is gone. A diagnostic dispatch rebuilt the same commit and passed its packaging and test steps. GitHub lists `twohearts-debug-apk` through 2026-12-18. Artifact download in the workspace returned HTTP 403, so runtime testing used locally built APKs. [Original run](https://github.com/https10011/2HEARTS2/actions/runs/35688677934) · [Diagnostic run](https://github.com/https10011/2HEARTS2/actions/runs/44213685130) · [Baseline artifact](https://github.com/https10011/2HEARTS2/actions/runs/44213685130/artifacts/14387373616) |

## Priority Zero runtime verification

| Android 13 launch case | Observation |
| --- | --- |
| `722ed47`, fresh install | `MainActivity` stayed alive, Android Keystore-backed app-lock initialization completed, and the Welcome screen rendered. The AOSP guest's own System UI timed out during first boot. TwoHearts remained the resumed app after the system dialog was dismissed. [Welcome screenshot](PRIORITY-ZERO-evidence/welcome-api33.png) |
| `722ed47`, onboarding to Home | Completed setup with two test profiles and a relationship date entered through the form, skipped the optional PIN, and opened Home. Home displayed the persisted names, duration, and anniversary. The date picker's confirmation was below the fold on this configuration and became reachable after scrolling the form. |
| `961c4e7`, pre-fix comparison | Reinstalled with the same signing key and app data. `FATAL EXCEPTION: main` reported `IllegalStateException: There are multiple DataStores active for the same file`. The process exited. |
| `722ed47`, returning launch with retained data | Reinstalled on the same device without clearing app data. `MainActivity` resumed, Logcat showed no TwoHearts fatal entry, and UI assertions confirmed both persisted profiles, the 365-day count, and the anniversary on Home. [Returning Home screenshot](PRIORITY-ZERO-evidence/returning-home-api33.png) |
| TECNO Spark 10 Pro, Android 13 | The owner subsequently confirmed normal launch on their phone. This confirmation covers startup. The owner did not identify the APK by hash or run. |

The runtime comparison confirms the existing singleton fix for the reproduced DataStore failure on Android 13. The owner's launch confirmation clears the startup gate for Phase 5. [Android 13 launch logs](PRIORITY-ZERO-evidence/android-13-launch.txt)

## Startup diagnostic reference

If the handset failure recurs, capture its `com.twohearts.app` exception immediately after launch with `adb logcat -b crash -d -v threadtime` and identify the APK by Actions run URL or SHA-256. If the crash buffer has no entry, capture `adb shell dumpsys activity exit-info com.twohearts.app`. Preserve the handset’s app data for that comparison.
