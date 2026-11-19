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
| Existing startup coverage | DataStore lifetime, persisted onboarding and app-lock state, deferred PIN verifier, and notification behavior regression tests passed. |
| Visual | No composables changed. The native Compose suite rendered again, and the 274 × 488 dp Notes screen retained its visible creation action and navigation. [Stage 1 comparison](../Stage-1/evidence/records-and-utilities.jpg) |
| Local APK | APK v2 signatures, ZIP alignment, and archive integrity passed. Candidate SHA-256 `64d434e2e4d37adac8ab2e5b04dd05f8e360e7979b1884e81afaf85d98ce2c21`. The local prior APK and candidate share certificate SHA-256 `422b889111ac2bbc0ef39a264bd07bf57550e82f96ca8adcbb1ef6c6b2da3e75`. |
| Android runtime | The software guest initially reached Android system services. A retry using its partial userdata stayed offline and reported an ext4 resize error. A clean sparse userdata image is being used for the next check. Fresh candidate launch, same-signer reinstall with local data, and the TECNO handset walkthrough remain open. |
| Remote | This report accompanies the tested Stage 2 migration commit. Its exact remote SHA and CI result will be recorded once the workflow finishes. |

The existing GitHub workflow generates per-run debug certificates. The confirmed certificate mismatch remains a release concern for in-place updates from a prior CI APK. Verify the installed APK's certificate and use a matching signing key or platform-supported lineage before claiming preserved handset upgrades. The owner's running installation and data are untouched.

## Completion checklist

- [x] Record starting commit, inspected files, two reproduced migration errors, and the implementation.
- [x] Verify supported native Room upgrades, unsupported-schema retention, and existing startup suites.
- [x] Run both unit-test suites, assemble the APK, inspect its package and signature, and review the native render baseline.
- [ ] Capture candidate Android launch and retained-data runtime evidence where available.
- [ ] Verify the exact published commit and successful CI before advancing.
- [x] Record signing compatibility findings, limitations, and the next required work.

## Next stage

Verify the Stage 2 remote and runtime results. Stage 3 then establishes the premium visual tokens, typography and contrast reference. The existing signed APK remains the owner's functional baseline.
