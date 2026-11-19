# Stage 2 — Startup, storage, and upgrade

## Starting state

Stage 1 completed at `08088016ba27acda8a672c459dc70700eb7a42e1`, with 134 tests passing per variant, verified local APK packaging, inspected native renders, and successful CI. The owner has confirmed normal operation of their baseline Android 13 APK. This stage checks startup regressions and retained data on supported native upgrades. [Stage 1](../Stage-1/REPORT.md) · [CI](https://github.com/https10011/2HEARTS2/actions/runs/44474694306)

## Investigation

- The native Room 12-to-13 migration had no schema operation. Earlier native migrations create `photo_ref` directly. The archived frontend used its own profile schema and added its photo column at version 13. The regression fixtures model the native schema with and without the optional column. They exercise the full Room open and preserve an existing profile and its photo reference where present.
- The startup tests cover the shared preferences-store lifetime and PIN verifier initialization. The new Android smoke checks use the baseline and candidate APKs on an isolated API 33 guest. GitHub artifacts from consecutive runs have different debug certificates. Both valid signatures were verified during Stage 1.

## Findings and changes

The two version 12 fixtures failed before the fix. Room rejected the profiles table without `photo_ref`. When that column was already present, Room rejected the unique notification owner-reference index because the native SQLite database contained it and the entity omitted it. Earlier native migrations also created memory attachment indexes that the entity omitted.

- `TwoHeartsDatabase.kt` now checks for the photo column during the 12-to-13 migration and adds the nullable column only when absent.
- The database moves to schema 14 through an index-only 13-to-14 step. `NotificationRegistry.kt` and `MemoryMedia.kt` declare the existing index definitions. Fresh databases create those indexes through Room. An upgrade retains the notification owner constraint and creates any missing attachment indexes without rewriting the affected tables or rows.
- `Phase20DataRetentionTest.kt` exercises both version 12 profile shapes, the shipped version 13 index shape, and preserved rows. It also keeps unsupported-schema recovery coverage, using a future version 15 file.

The app keeps the same package ID and continues to use the process-wide Room and DataStore instances, deferred PIN verifier, and existing startup routes. The Room schema change is independent of APK signing.

## Verification

| Check | Result |
|---|---|
| Pre-fix reproduction | Two added Room fixtures failed on the native version 12 paths. The error identified `profiles` and `notification_registry` respectively. |
| Debug and release | `./gradlew :app:testDebugUnitTest :app:testReleaseUnitTest :app:assembleDebug --no-daemon --max-workers=1 -Pkotlin.compiler.execution.strategy=in-process` passed. Each variant ran 137 tests with zero failures, errors, or skips. |
| Migration | The Room tests passed from version 12 with and without the photo column and from a version 13 database with its shipped index shape. They verify retained records, including the existing photo reference, note and notification where applicable. An unsupported version 15 database kept its original file and row. |
| Early schema review | Replayed the declared native 1-to-14 SQL in SQLite. The resulting 17 tables, column types, nullability, primary keys, and explicit indexes matched the generated Room schema. |
| Existing startup coverage | DataStore lifetime, persisted onboarding and app-lock state, deferred PIN verifier, and notification behavior regression tests passed. |
| Visual | No composables changed. The native Compose suite rendered again, and the 274 × 488 dp Notes screen retained its visible creation action and navigation. [Stage 1 comparison](../Stage-1/evidence/records-and-utilities.jpg) |
| Local APK | APK v2 signatures, ZIP alignment, and archive integrity passed. Candidate SHA-256 `64d434e2e4d37adac8ab2e5b04dd05f8e360e7979b1884e81afaf85d98ce2c21`. The local prior APK and candidate share certificate SHA-256 `422b889111ac2bbc0ef39a264bd07bf57550e82f96ca8adcbb1ef6c6b2da3e75`. |
| Android runtime | The isolated Android 13 x86-64 guest installed the local Stage 1 APK and accepted the same-signer Stage 2 replacement. The first launch wait timed out while guest System UI reported ANRs. After a cold restart, TwoHearts completed bootstrap, its activity resumed, and Room reached schema 14 with the test note retained. The crash buffer had no TwoHearts entry. The returning Home, locked handset, and complete first-launch candidate walkthrough remain part of release validation. [Android upgrade evidence](evidence/android-13-upgrade.txt) |
| Published APK | The Actions artifact downloaded and passed APK v2 signature, ZIP alignment, archive integrity, and package checks. APK SHA-256 `cb83199cb6214fe3a82c8095abd4cbae397447736fd11f394ca1d2a52a7d876a`. Certificate SHA-256 `83c962ff8bc2687b05463e591953240b0698aeec495210c055328caf26e2cce9`. |
| Remote | Git verified `bd278225e2f54cbe6c2e83da71723ecbc0dcd353` on `master`. The Android workflow passed and published `twohearts-debug-apk`. [CI](https://github.com/https10011/2HEARTS2/actions/runs/44476385339) · [APK](https://github.com/https10011/2HEARTS2/actions/runs/44476385339/artifacts/14505789721) |

The existing GitHub workflow generates per-run debug certificates. The confirmed certificate mismatch remains a release concern for in-place updates from a prior CI APK. Verify the installed APK's certificate and use a matching signing key or platform-supported lineage before claiming preserved handset upgrades. The owner's running installation and data are untouched.

## Completion checklist

- [x] Record starting commit, inspected files, two reproduced migration errors, and the implementation.
- [x] Verify supported native Room upgrades, unsupported-schema retention, and existing startup suites.
- [x] Run both unit-test suites, assemble the APK, inspect its package and signature, and review the native render baseline.
- [x] Verify on Android 13 that the same-signer update launches and retains test data. Record the guest System UI limitation and the remaining handset checks.
- [x] Verify the exact published migration commit and the successful workflow.
- [x] Record signing compatibility findings, limitations, and the next required work.

## Next stage

Stage 3 establishes the premium visual tokens, bundled typography and contrast reference. The owner’s working APK remains the handset baseline. Stage 22 repeats release validation on an identified candidate.
