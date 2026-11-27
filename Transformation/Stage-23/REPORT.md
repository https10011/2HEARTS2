# Stage 23 — Product acceptance and Android delivery

## Baseline and audit

The 23-stage transformation began from `7a44bccfa596585d37c5f5572efd4717510485d6`, after the owner confirmed that the previous APK installed, launched, and ran on the TECNO Spark 10 Pro with Android 13. The earlier duplicate DataStore startup failure was already addressed. The transformation retained the singleton regression and corrected Room’s upgrade from the version-12 and version-13 profile schemas. The final audit started from the successful Android release rehearsal at `616b1f29b69cb48a226a839ccc1b727afb7688a4`. [Baseline inventory](../Stage-1/REPORT.md) · [Startup and Room](../Stage-2/REPORT.md) · [Android rehearsal](../Stage-22/REPORT.md)

| Area | Accepted outcome and evidence |
|---|---|
| Identity, brand, and controls | Bundled fonts and light/dark colors support the editorial hierarchy. Shared controls use visible focus, meaningful motion, and an accessible confirmation sheet. The official mark has an adaptive icon layer and light/dark splash assets. [Design system](../Stage-3/REPORT.md) · [Controls](../Stage-4/REPORT.md) · [Android branding](../Stage-5/REPORT.md) |
| App shell, first use, and profiles | The four-destination shell centers Us and keeps the legacy routes, alerts, and search reachable. First use supports optional images for both people, crop, skippable permission explanations, and safe draft persistence. Both photos are stored privately and can be changed in Settings. [Navigation](../Stage-6/REPORT.md) · [Profile photos](../Stage-7/REPORT.md) · [Onboarding](../Stage-8/REPORT.md) |
| Home, Us, and private journals | Home and Us share portrait and relationship treatments with live milestones and records. The gallery and editors for Memories, Notes, and Timeline were checked through save, validation, reader, deletion, and restart paths. [Home](../Stage-9/REPORT.md) · [Us](../Stage-10/REPORT.md) · [Memories](../Stage-11/REPORT.md) · [Notes and Timeline](../Stage-12/REPORT.md) |
| Tasks, calendars, and wellness | Reminders, Important Dates, and Notifications use distinct layouts and real local data. Places, Mood, and Period cover their list, detail, form, history, picker, and private states. WorkManager delivery and the stored receipt passed on Android 13. [Scheduling and alerts](../Stage-13/REPORT.md) · [Places, Mood, Period](../Stage-14/REPORT.md) |
| Search, utilities, and security | More and Settings link to profiles, appearance, permission controls, transfer, storage, About, and app lock. Search excludes protected data. Vault note, photo, video, and document paths are covered. JSON transfer covers Notes and Reminders. [Settings and transfer](../Stage-15/REPORT.md) · [Lock and Vault](../Stage-16/REPORT.md) |
| Yuki | The reproducible art pipeline registers 26 runtime sprites from 21 original orange-tabby poses. Kotlin motion supports autonomous cues, the care actions and sleep, saved progression, apparel, and a lightweight Home portrait. Native motion and the Android background return were inspected. [Art](../Stage-17/REPORT.md) · [Engine](../Stage-18/REPORT.md) · [Room and motion](../Stage-19/REPORT.md) |
| Cross-app verification | Stage 20 closes the 24 inventory groups with native state and accessibility evidence, including 128 cross-app screens, 200% text, short and narrow layouts, and corrected confirmation and tab states. Stage 21 archives superseded plans and verifies the same packaged contents from a clean checkout. [Route and accessibility audit](../Stage-20/REPORT.md) · [Cleanup](../Stage-21/REPORT.md) |

## Final delivery

The existing **Build Android APK** workflow builds and tests the package, publishes `twohearts-debug-apk`, and runs the uploaded APK on API 26, 33, and 35. The Android 13 job additionally checks real PIN storage, retained notes, the system picker, video playback, scheduled alert delivery, Yuki’s foreground return, and a matching-signature upgrade. The final verification job downloads the APK, matches its checksum and source SHA against all three emulator receipts, and records its identity, signing certificate, and artifact link in the run summary and `twohearts-delivery-receipt`. The receipt serves as the exact-commit remote acceptance record. [Build Android APK](https://github.com/https10011/2HEARTS2/actions/workflows/build-android.yml)

- Forced fresh runs of the debug and release unit suites. Each passed 258 tests with zero failures, errors, or skips. Both debug and Android test APK builds passed.
- The local debug APK passed ZIP integrity, alignment, and APK v2 signature validation. It reports `com.twohearts.app` 1.0.0 (1), min API 26, target API 35, and four native ABIs. It contains 26 registered Yuki sprites and their atlas. The test video stays in the Android test APK.
- The local APK is 28,062,156 bytes, compared with 47,117,983 bytes at Stage 1. Its SHA-256 is `017f91cef2c63f794e4ee0bae20a4d6fdc3c5420b8d501358d12023a566c583c`. The delivery receipt gives the hosted artifact’s own checksum and certificate.
- Verified all 23 stage reports, 24 audit entries, the acceptance report’s local links, the reproducible sprites, the workflow and shell syntax, and the receipt’s package metadata and signing validation against the local APK.

## Remaining physical verification

The transformed APK has hosted Android runtime coverage. A TECNO Spark 10 Pro was unavailable for this build. Its full walkthrough, live TalkBack and gesture behavior, user-selected photo flow, frame timing, and prolonged memory use remain physical verification. The same-signer upgrade test uses an isolated fixture key. The published debug APK can update an existing installation only when Android sees a matching certificate.

## Completion checklist

- [x] Review the initial baseline and every route group, with linked native and Android evidence across all product areas.
- [x] Confirm supported architecture, local persistence, privacy boundaries, and reproducible Yuki art.
- [x] Rerun the debug and release unit suites, build both APKs, and verify package metadata and contents.
- [x] Define and test the exact-commit delivery verification. It requires the committed source SHA, successful packaging and emulator jobs, matching APK bytes, a valid signer, and an available named artifact.

Remote acceptance is recorded by the **Verify Android delivery** job for the final `master` commit. The APK and delivery receipt use the same workflow run. [Delivery workflow](https://github.com/https10011/2HEARTS2/actions/workflows/build-android.yml)
