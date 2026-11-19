# Phase 15 — Interaction feedback

Notes, Memories, Timeline, Reminders, Places, and Mood now report the result of local writes. Successful changes confirm after persistence and stay visible when the app returns to the previous screen. A failed write keeps its editor open. Repeated submissions are rejected while the first write is pending. Memory saves distinguish files that could not be copied, and a place photo that fails to copy leaves the editor available to retry.

Period entries, cycle settings, Important Dates, and profile and relationship settings also confirm successful writes. The date editor keeps the form and shows an inline error on a failed save or deletion. Reminder completion and reopening get distinct confirmations because both change how the item is listed. Import and export preserve their existing result screens and record counts.

Feedback uses the shared theme status tones, icons, and live-region announcements. The host sits above the navigation area, accounts for window insets, and honors Android’s recommended accessibility timeout. Reduced motion removes its fade and the shared button’s press scale. Presses retain a visual ripple, and loading buttons show a still clock when reduced motion is enabled.

Yuki keeps the room’s saved care reactions and progression messages. Newly reached levels can give a single Android confirmation haptic on API 30 and later. The haptic uses the system preference and does not replay on room entry.

## Verification

- Unit cases check that persistence precedes confirmation and navigation. They also cover writer failure, cancellation, partial success, repeated notices, and duplicate taps.
- Reviewed actual Compose captures of success above the shell navigation and failure on a dark background at 320 dp with extra-large text and reduced motion. [Compose review](evidence/compose-review.jpg)
- The full debug and release unit suites passed with 118 tests each. The debug APK assembled. The haptic feel and keyboard-inset behavior on physical hardware remain pending.
