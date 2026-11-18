# Phase 11 — Period experience

The overview shows the latest recorded dates, an estimate based on cycle settings when one is still current, and direct access to logging, calendar, history, and settings. A missing end date remains visible as missing. The calendar marks the recorded start on its own until an end is supplied. Completed ranges include both endpoints.

The logging form uses the shared date picker and validates date order and future dates. History opens the saved entry for editing or confirmed removal. Existing entries can clear an optional end date or note. Calendar and history show owner entries and preserve access to records stored under the legacy owner key. Cycle and period lengths persist in Room and refresh the tracker with the same stored settings.

The tracker remains app-local. The Android emulator accepted the Phase 10 APK but lost its activity service during repeated Android system process crashes. This prevented a useful launch check on that guest. The owner’s confirmation applies to the earlier startup baseline.

## Verification

- Regression tests cover leap-day ranges, incomplete entries, future and reversed dates, stale estimates, settings persistence, entry edits, and removal.
- The full debug and release unit suites passed with 100 tests each. The debug APK assembled successfully.
- Compose scenarios cover the empty dashboard, the populated tracker, the log form, and the calendar, history, edit, and settings screens at 360 dp with dark mode and extra-large text. [Compose review](evidence/compose-review.jpg)
