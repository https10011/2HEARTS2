# Stage 14 — Places, Mood, and Period

## Starting checkpoint

Stage 13 passed on `e2f686a8bbccffe1ad804cb96e3a02d6d2d7c0be`. Reviewed native Places, Mood, and Period reference renders, the corresponding layouts, their Room DAOs and repositories, the media store and reference checks, AppRouter, PeriodRoutes, and the baseline tests. [Stage 13 report](../Stage-13/REPORT.md)

## Initial findings

- Places allocates a large placeholder image to records without a photo. The editor shows neither saved nor selected images, and a failed record write can strand an imported asset.
- Mood repeats the add and quick-entry affordances. History has no edit entry point, uses a rolling date comparison that can include future records, and visually does not distinguish personal entries from other saved entries.
- Period already scopes entries to the owner and legacy owner ID. The tracker, calendar and history can better distinguish logged ranges, incomplete entries and estimates at large font sizes.

## Implementation

- Places now opens onto a saved-photo feature and compact rows for places without pictures. The category sheet recognizes spacing and case variants of old labels and preserves the saved spelling on an unchanged edit. The reader shows the whole story, locality, photo preview, and edit and delete actions. The editor restores its draft fields and previews the saved or newly selected picture.
- The place writer copies the photo in the same Room transaction that saves its metadata and record reference, which serializes the copy with the orphan-media sweep. A failed decode or stale edit leaves the previous record intact. Cleanup retains any file another record still references, and the existing soft-delete behavior is preserved. This uses Room schema 14.
- Mood shows today’s local check-in and a quick picker that preserves the saved note. History groups by real calendar month. The rolling windows exclude future dates, and All keeps malformed and future restored records accessible. History retains other saved profiles in read-only rows and lets the owner edit or delete their own and legacy owner entries. The check-in editor keeps its draft and writes to the date it displays.
- Period distinguishes the latest valid logged entry from the estimate. The calendar shows logged dates with filled markers and estimates with outlined markers and announces both with the local date. History keeps invalid records editable and labels incomplete entries. Logging and cycle settings use the shared persistent Save action. The shared date picker opens with the saved selection in view. The existing owner and legacy owner scope applies to list, history, calendar and edit routes.

## Verification

- Inspected native Android API 34 screenshots in both palettes at 411 × 891, 360 × 740, and 320 × 618 dp. At 200% text the long place title wrapped, both date-picker actions remained usable, and calendar day numbers and status markers remained legible. Sampled text and action contrast ranged from 4.80:1 to 9.99:1. [Places](evidence/places.jpg) · [Mood](evidence/mood.jpg) · [Period](evidence/period.jpg) · [Date picker and settings](evidence/date-picker-and-settings.jpg)
- Isolated Room tests used real JPEG bytes and verified add, detach, replacement, shared references, failed decoding, stale edits, retained place fields, and soft deletion across reopen. Journal persistence tests kept the legacy owner’s note on a quick check-in and left the other profile untouched. They also reopened the private cycle entry and settings, confirmed deletion, and retained the other profile’s row.
- Native interaction tests exercised category filtering, editable place fields, removal and confirmation, both Mood History windows, owner history edits and deletion, and end-date clearing and cycle settings validation. The calendar correctly announced Feb. 29 as logged and March 1 as estimated, and the picker kept its selected leap day and both actions visible at large text.
- Debug and release each passed 217 tests with zero failures, errors or skips. The debug APK passed v2 signature, alignment, archive integrity and metadata checks. It reports `com.twohearts.app` 1.0.0 (1), min API 26 and target API 35. Local APK SHA-256 `c3d018cf9cdd21d7db686bf462f45ccaea0a5336a5b6e24aa1fcc625ed4096a0`. The first full test attempt exhausted disk space during Robolectric extraction. The passing run used a RAM-backed JVM temporary directory.
- This checkpoint uses simulated Android API 34 execution for native composition and Room. Installed Photo Picker and the transformed APK’s TECNO handset walkthrough remain part of release validation.

## Acceptance and checkpoint

- [x] Distinguish the three experiences with real saved records, accessible actions, legible hierarchy, and appropriate empty and error states.
- [x] Preserve existing Places fields and categories, verify photo writes and cleanup, and exercise add, edit, and delete.
- [x] Retain quick mood check-in and note preservation, verify local history windows and entry ownership, and exercise historical edits and deletion.
- [x] Preserve private Period scope and all six routes. Verify date boundaries, estimates, change and deletion, saved lengths, and reopening.
- [x] Review native images in both palettes and at standard and large font sizes, then run both unit variants and assemble and inspect the APK.
- [x] Commit, push, verify exact CI and artifact, record results, and continue to Stage 15.


## Remote verification

GitHub confirmed `6b7048d6fdc8cca42b759da7f5ff546ce6c7959e` on `master`. Build Android APK run `45518395408` passed and published `twohearts-debug-apk` artifact `14978217858` for that commit. [Commit](https://github.com/https10011/2HEARTS2/commit/6b7048d6fdc8cca42b759da7f5ff546ce6c7959e) · [CI](https://github.com/https10011/2HEARTS2/actions/runs/45518395408) · [APK](https://github.com/https10011/2HEARTS2/actions/runs/45518395408/artifacts/14978217858)
