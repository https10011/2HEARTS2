# Phase 9 — Reminders and important dates

Reminders now emphasize what is coming up without resembling a productivity dashboard. Pending and completed views use gentle language, overdue items receive clear restrained urgency, and each row exposes completion directly. Detail supports completion reversal, editing, notification status, and confirmed deletion. Creation validates title, date, and optional 24-hour time, explains local notifications, and lets the user enable or disable them.

Important Dates is now fully functional. The previous add action was an empty callback; users can now create, edit, and delete meaningful dates in place. Recurring dates are ordered by their next occurrence and surface a human countdown, while one-time dates retain their original calendar meaning. All writes run away from the main thread.

## Verification

- `Phase9DatesAndRemindersTest` verifies recurring-date calculations and real Room completion plus optional-field clearing.
- Native Compose renders cover empty and populated reminders, reminder detail and creation, and the populated Important Dates surface. The review confirmed urgency, completion, notification controls, and narrow-screen form fit. [Compose review](evidence/compose-review.png)
- `./gradlew :app:testDebugUnitTest :app:testReleaseUnitTest :app:assembleDebug` passed. All 94 tests passed in each variant.
- Physical-device verification of this phase remains pending.
