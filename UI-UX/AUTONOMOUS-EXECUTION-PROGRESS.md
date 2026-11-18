# TwoHearts autonomous execution progress

## Current checkpoint

- **Active work** — Phase 12 remote gate. Phase 13 covers Yuki after the gate passes. The startup confirmation on the owner’s TECNO Spark 10 Pro applies to the earlier launch fix.
- **Completed UI/UX phases at intake** — Phase 0 reconnaissance, Phase 1 visual language, Phase 2 shell and navigation, Phase 3 onboarding, and Phase 4 Home. Their implementation and reports are in the repository history.
- **Completed since intake** — Phase 5 replaced the Us directory with a live relationship summary and feature previews. Phase 6 redesigned Memories and connected private photo/video selection, persistence, browsing, editing, and deletion. Phase 7 transformed Notes into a searchable, category-aware private writing experience. Phase 8 rebuilt Timeline around a continuous chronological story. [Phase 5 report](Phase-5/PHASE-5-US-RELATIONSHIP-HUB.md) · [Phase 6 report](Phase-6/PHASE-6-MEMORIES-EXPERIENCE.md) · [Phase 7 report](Phase-7/PHASE-7-NOTES-EXPERIENCE.md) · [Phase 8 report](Phase-8/PHASE-8-TIMELINE-EXPERIENCE.md)
- **Completed since intake** — Phases 5–9 now cover Us, Memories, Notes, Timeline, Reminders, and Important Dates. Phase 9 adds reminder completion and notification control plus complete Important Date CRUD and recurrence ordering. [Phase 9 report](Phase-9/PHASE-9-REMINDERS-AND-IMPORTANT-DATES.md)
- **Completed since intake** — Phases 5–10 now include Us, Memories, Notes, Timeline, Reminders, Important Dates, Places, and Mood. [Phase 10 report](Phase-10/PHASE-10-PLACES-AND-MOOD.md)
- **Pending UI/UX phases** — Phase 13 through Phase 20 in the [master directive](MASTER-UI-UX-DIRECTIVE.md).
- **Phase 7 remote checkpoint** — `ee23b153370d8534f43cd4d835111be528d152d1`. GitHub Actions run [44261918124](https://github.com/https10011/2HEARTS2/actions/runs/44261918124) passed its APK build, tests, and artifact upload.
- **Phase 8 remote checkpoint** — `77ef7157fd37fe97cf7fdebff15402726fc6a5a7`. GitHub Actions run [44265458516](https://github.com/https10011/2HEARTS2/actions/runs/44265458516) passed its APK build, tests, and artifact upload.
- **Phase 9 remote checkpoint** — `f23a4b21d2dbea5bad788b30ff5ce61dcfc54a91`. GitHub Actions run [44266837411](https://github.com/https10011/2HEARTS2/actions/runs/44266837411) passed its APK build, tests, and artifact upload.
- **Phase 10 remote checkpoint** — `41ab33dabead543aa93a6d0e975c82b2a4ab2399`. The 96 tests in each variant and the local debug build passed. [GitHub Actions](https://github.com/https10011/2HEARTS2/actions/runs/44304791227) · [APK](https://github.com/https10011/2HEARTS2/actions/runs/44304791227/artifacts/14428725240)
- **Latest verified remote checkpoint** — Phase 11, `690a82429ab8c95bd20f77e1704f66f57fa8fe8d`. Period logging, settings, calendar, and history passed 100 tests per variant and the APK build. [Phase 11 report](Phase-11/PHASE-11-PERIOD-EXPERIENCE.md) · [GitHub Actions](https://github.com/https10011/2HEARTS2/actions/runs/44306875311) · [APK](https://github.com/https10011/2HEARTS2/actions/runs/44306875311/artifacts/14427604682)
- **Phase 12 local status** — Settings and utilities passed 105 tests per variant, the debug APK build, and Compose review. Verify the Phase 12 workflow before starting Yuki. [Phase 12 report](Phase-12/PHASE-12-SETTINGS-AND-UTILITIES.md)
- **Runtime limitations** — The reused API 33 guest installed the APK and then lost Android’s activity service during repeated system process crashes. The fresh guest’s ADB connection stayed offline. Device launch and interactive validation for the new APKs are pending.

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
