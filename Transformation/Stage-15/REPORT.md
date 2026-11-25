# Stage 15 — Settings, Profile, Search, and utilities

## Starting checkpoint

Stage 14 passed on `6b7048d6fdc8cca42b759da7f5ff546ce6c7959e`. Inspected the baseline native renders, More and settings routes, profile photo and relationship writers, appearance and notification state, Room search providers, storage cleanup and reset, JSON import/export, and package metadata. [Stage 14 report](../Stage-14/REPORT.md)

## Initial findings

- More and Settings repeat similar menu markup and give utility actions different names. The More transfer entry mentions import alone.
- Profile and relationship already share local photo handling, but their primary actions scroll below the form. Appearance and notification settings have generic control layouts.
- Search recreates its data subscription for every query. Transfer previews and execution parse the source separately. The current storage view combines usage, cache clearing, and permanent reset in one dense stack.
- The full project suite can require more than 590 MB of temporary space. Use a RAM-backed JVM temporary directory for local full runs in this workspace.

## Implementation

- More and Settings share an accessible index. The transfer entry explicitly names Notes and Reminders. Profile and Relationship retain their avatar picker and staged photo writes and share a persistent Save action.
- Appearance uses a preview and wrapping controls. Notification settings distinguish Android access from the app and reminder switches. Storage separates counts, temporary-file cleanup, and permanent reset.
- Search listens to live Room data through the destination lifecycle and preserves the five existing search sources. The result and empty states fit the keyboard-adjusted area, with Clear available in the toolbar.
- Import parses a bounded JSON document into the exact batch shown in preview. The valid batch commits together inside a Room transaction, and invalid rows are counted and shown before confirmation. Export keeps the versioned Notes and Reminders format.
- About uses the official mark and reads installed app and Android package information. It explains which records can be exported.

## Verification

- Seventeen focused regressions passed. Native Compose exercised the index and About, both profile editors, appearance changes, denial and system settings, both storage confirmations, search navigation, and transfer. Exact Notes and Reminders contents shown in preview were retained after the provider changed its source. Export, picker cancellation and rejected JSON were exercised using the Android document contracts.
- The file-backed transfer test verified that a write conflict rolls back both record types. Legacy and versioned JSON preserve relevant fields, including old category spelling, accented text, a leap-day schedule, recurrence, completion and disabled alerts. Invalid files and the input size limit were exercised.
- Local settings tests reinitialized preference state after writes. Cache cleanup retained registered media and removed an orphan. The existing reset test uses Android’s package-data-clear shadow. Native search found an accented note, exposed its title and source as one actionable label, refreshed following Room deletion, and excluded a matching Vault item.
- Reviewed native renders at 411 × 891, 360 × 740, and 320 × 618 dp, including both palettes and 200% text. The result list scrolls within the keyboard-adjusted body, the profile Save actions remain visible, and long labels and transfer messages wrap. [Navigation and profiles](evidence/navigation-and-profiles.jpg) · [Appearance and alerts](evidence/appearance-and-alerts.jpg) · [Storage](evidence/storage.jpg) · [Search](evidence/search.jpg) · [Transfer](evidence/transfer.jpg)
- Debug and release each passed 228 tests with zero failures, errors or skips. The debug APK passed v2 signing, alignment, archive integrity and metadata checks. It reports `com.twohearts.app` 1.0.0 (1), min API 26 and target API 35. Local APK SHA-256 `4ef0c596bd41fbedac0bd874e23cf32906bc934335bd441cb32846f2a7e51473`.
- Native UI and platform contract checks use Android API 34 under Robolectric. Installed document-provider behavior and the TECNO walkthrough remain release checks.

## Acceptance and checkpoint

- [x] Redesign More, Settings, both profile editors, Appearance, Notifications, Search, Storage, Import/Export, and About while preserving the existing routes.
- [x] Verify local avatar and profile settings, appearance persistence, and permission denial and settings actions.
- [x] Verify live search, accessible result navigation and privacy scope.
- [x] Verify legacy and versioned Notes and Reminders transfer, invalid files, cancellation, cache cleanup, and confirmation for permanent reset.
- [x] Inspect native renders in both palettes at standard and large text, pass both full suites, and verify the debug APK.
- [x] Commit, push, verify exact CI and artifact, and continue to Stage 16.


## Remote verification

GitHub confirmed `d5ddbe54426247b0ab93b0fe44d0a4bc5b718296` on `master`. Build Android APK run `45522674260` passed and published `twohearts-debug-apk` artifact `14980207806` for that commit. [Commit](https://github.com/https10011/2HEARTS2/commit/d5ddbe54426247b0ab93b0fe44d0a4bc5b718296) · [CI](https://github.com/https10011/2HEARTS2/actions/runs/45522674260) · [APK](https://github.com/https10011/2HEARTS2/actions/runs/45522674260/artifacts/14980207806)
