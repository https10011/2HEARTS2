# Phase 14 — Cross-app consistency

Notes, Memories, Timeline, Reminders, Places, Mood, and Period share loading, missing-record, and retryable storage-error states. Detail screens refresh when resumed so a saved edit or reminder status change appears when the user returns. The distinction between a deleted record and a failed read gives people a clear action in each case.

Header slots now fit multiple actions and inherit the current theme’s on-surface color. Shared button labels and badges use the foreground color paired with their background. Screen actions use the shared icon set. The loading state uses a still indicator when reduced motion is enabled.

Destructive confirmations use the same scrollable dialog and stacked actions. Important Dates now uses that confirmation and identifies an elapsed one-off date without a negative countdown.

## Verification

- The full debug and release unit suites passed with 114 tests each. The debug APK assembled. Added tests cover record lookup, retry, recovery, and cancellation.
- Reviewed actual Compose captures of a missing memory, failed timeline lookup, and two-action dark header. The dark cases use 320 dp with extra-large text. [Compose review](evidence/compose-review.jpg)
- The current Android 13 emulator failed at the system-process level. Handset verification of this checkpoint is pending.
