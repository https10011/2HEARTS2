# Stage 13 — Reminders, Important Dates, and Notifications

## Starting checkpoint

Stage 12 passed on `2bd93c06959e66e0440e9f2151c5c44e5c428198`. Inspected the active reminder and date screens and Room repositories, notification feed, notification settings, date arithmetic, WorkManager scheduler, and alert navigation in `MainActivity` and `AppRouter`. Reviewed the original native renders of the reminder list, detail, editor, date list, and empty notification feed. [Stage 12 report](../Stage-12/REPORT.md)

The reminder list omitted saved missed items. The date list labeled invalid restored values as today and showed the original date beside next-occurrence copy. Calendar calculations diverged across Home and the schedule. Changing time zones could generate a second notification receipt for the same local occurrence. An early worker invocation and disabled reminder channel could also produce misleading delivery state.

## Implementation

- Pending reminders are grouped into Needs attention and Scheduled, with a separate Completed tab, independent completion and detail controls, and one persistent add action. The editor retains its form in saved state and validates trimmed local dates and times. A schedule section displays the time, recurrence, and alert setting. Edit, completion, deletion, loading, and error paths use the existing repository and navigation.
- Important Dates has a featured next date with a calendar count, with past and malformed restored dates in separate groups. Edit retains the original saved date. The new sheet supports add, edit, recoverable validation, and confirmed deletion.
- Notifications are grouped by local delivery day. The screen shows unread state and opens the originating reminder, date, or mood destination. Existing alert settings are one action away, and Android denial is visible.
- Home and Important Dates share calendar arithmetic anchored to the original date. WorkManager recalculates future jobs after a clock or time-zone change. Delivery uses a local-date-and-time receipt key and still honors stored UTC keys for the same scheduled instant. An early job retries, a blocked system channel or permission skips the receipt, and a rejected system post rolls back its database transaction. The existing alert PendingIntent opens the Notifications feed.

## Verification

- Twelve focused tests passed. The Android API 34 worker tests validated notification denial, an early retry, a single positive post through Android’s notification manager shadow, and the attached PendingIntent. The WorkManager database test replaced a future job after a clock refresh, then canceled its old UTC key and queued a new one for the same local time in Lagos. Room tests verified concurrent deduplication, legacy receipts, rollback when posting is rejected, retained read state, and reminder and important-date CRUD across reopen.
- Native Compose tests exercised date repair, creation and deletion, reminder completion and validation, the empty list, notification denial, and the encoded deep link to a saved reminder. The initial post-save render exposed that a new date could lie above the old list position. The list now returns to its first item after adding a date. The next render showed the new date in the featured slot.
- Inspected API 34 native renders at 411 × 891, 360 × 740 and 320 × 618 dp in light and dark, with 200% text in the narrow cases. The reminder editor retains its Save action as the recurrence controls scroll. Long featured dates and explicit unread and denial labels wrap. Sampled label and action colors cleared 4.5:1 on their backgrounds. [Reminders](evidence/reminders.jpg) · [Reminder editor](evidence/reminder-editor.jpg) · [Important dates](evidence/important-dates.jpg) · [Notifications](evidence/notifications.jpg)
- Both full unit variants passed 205 tests with zero failures, errors, or skips. The debug APK passed v2 signing, alignment, archive integrity, and manifest checks. It declares `com.twohearts.app` 1.0.0 (1), min API 26, target API 35, and a non-exported receiver for clock and time-zone changes. The local APK SHA-256 is `b7a39b180607dfc8c40f0d47fcbbc3f6840443d57ac6f5cbae86f42fb1461f4b`.
- Scheduled delivery uses Android WorkManager, whose delivery time can vary with device power state. Legacy receipts suppress repeats at their original UTC instant. New receipts retain their local slot across a zone change.
- No handset is connected. The Android 13 software guest ran earlier checkpoints with System UI limitations. A read-only Android 13 guest smoke test was started for this build. The handset delivery, timing, and power-management walkthrough remains part of release validation.

## Completion checklist

- [x] Record the starting SHA, objectives, inspected routes, baseline, and implementation.
- [x] Run focused tests, then both full unit suites and the debug build.
- [x] Inspect the native screenshots in both palettes, standard and small sizes, and at 200% text.
- [x] Verify recurrence, wall-clock and time-zone changes, legacy receipts, Android denial, local CRUD, reopen, notification routes, and missing or invalid records.
- [x] Verify APK metadata and signing. Review the final diff.
- [ ] Verify exact CI and artifact after pushing the checkpoint.
- [ ] Record the verified result and continue to Stage 14 Places, Mood, and Period.
