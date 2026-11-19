# Phase 20 — Final UI and release consolidation

The final pass revisited the native shell, empty and populated destinations, shared controls, appearance tokens, Yuki’s room and Home presence, and local storage. The product walkthrough and its detailed Compose evidence are recorded in Phase 18. [Product QA](../Phase-18/PHASE-18-PRODUCT-VISUAL-QA.md)

Empty Notes, Memories, Timeline, Places, and Important Dates use their inline first-item action. These empty views can scroll on short screens. Notes uses more compact spacing when height is limited. The first-note button and bottom navigation are fully visible in the native 274 × 488 dp render with Large text. Lists retain the floating creation action once they contain records. Places also keeps the selected category aligned with the displayed results and gives the final row room above its add button. [Compose render](evidence/short-notes.jpg)

Room opens and checks the database during startup on a background dispatcher. Existing version 13 data survives a reopen. When a file has an unsupported schema, the app shows a retry state and leaves the stored data intact. A process-wide Room instance serves both the activity and the reminder worker.

The active PIN gate and WorkManager delivery paths replace the obsolete Vault lock screen and alarm receiver code. The established Android notification channels, app data models, and original Yuki artwork remain part of the app.

## Release verification

- `:app:testDebugUnitTest`, `:app:testReleaseUnitTest`, and `:app:assembleDebug` succeeded. Both variants passed all 134 tests. They include on-disk database reopen and unsupported-schema retention checks and the short Notes render.
- The local APK passed APK Signature Scheme v2, ZIP alignment, and archive integrity checks. It is `com.twohearts.app` version `1.0.0` (code 1), minimum SDK 26 and target SDK 35, and contains four Android ABIs and all 21 original Yuki poses. The local APK SHA-256 is `42afb4df8653209dd3d4ac40bbe405d5c534654573b0acd6a5f1f8e288f79d60`.
- An in-place update of the local APK succeeded on an isolated x86-64 Android 13 guest. The first `am start -W` timed out under software emulation. The activity stayed in the foreground, Room opened, bootstrap completed, and Home rendered with the existing test profiles and date. A tap on Notes opened its empty screen with the new first-note action fully visible. The crash buffer was empty, and the event buffer had no TwoHearts crash or ANR entry during this check. [Returning Home](evidence/android-13-home.jpg) · [Notes](evidence/android-13-notes.jpg)
- The source checkpoint is the commit containing this report. The existing Build Android APK workflow packages `twohearts-debug-apk` from `master`. [Workflow history](https://github.com/https10011/2HEARTS2/actions/workflows/build-android.yml?query=branch%3Amaster)
- Physical verification of the final APK on the TECNO Spark 10 Pro remains pending. The Phase 19 report records the earlier Android 13 emulator runs, the captured ANR, and the handset test cases. [Device validation](../Phase-19/PHASE-19-DEVICE-VALIDATION.md)
