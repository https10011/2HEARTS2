# Phase 18 — Full product visual QA

The walkthrough reviewed onboarding, Home, Us, More, the shell, core record lists and editors, Places, Mood, Period, Settings, Search, Notifications, Vault, and Yuki. Native Compose renders covered empty and saved-record states, dark surfaces, narrow layouts, and enlarged text. Populated views used isolated test fixtures.

Vault now lists Room records, copies selected photos, videos, and documents into app-private storage, saves private notes, opens details, and confirms deletions. Photos use bounded previews. Saved videos open with Android playback in Vault and Memories. A copy of a Vault document can be saved through the system picker. Vault remains excluded from regular search and the Notes and Reminders transfer.

Privacy settings support enabling app lock, changing the PIN, and disabling it after confirming the current PIN. When enabled, the PIN gate precedes app content on cold launch. It reappears after the configured time away. The verifier remains in Android Keystore-backed encrypted preferences. Vault content uses Android app-private storage.

Timed reminders are scheduled with WorkManager and rechecked against the saved reminder and preferences at delivery. Notification Center records a due occurrence once. Android notification permission, global and reminder preferences, completed or deleted status, and the actual schedule determine delivery. Date-only reminders stay in the list and explain how to add an alert time. Android may defer delivery in the background.

The review also corrected recurring dates across short months and leap years, space beneath the reminder list, live recent mood previews, the dark lock-screen text, and startup failure recovery.

## Verification

- The full debug and release unit suites passed with 130 tests each. The debug APK assembled.
- Regression tests cover recurring calendar dates, local reminder time zones and delivery conditions, Vault note persistence and isolation, notification occurrence deduplication, and the foreground relock timeout.
- Reviewed native Compose renders including default and large text, narrow layouts, empty and populated lists, validation and recovery states, and Yuki activity poses. [Core screens](evidence/core.jpg) · [Shared features](evidence/shared.jpg) · [Settings and utilities](evidence/settings.jpg) · [Onboarding and shell](evidence/shell.jpg) · [Private flows](evidence/privacy.jpg) · [Detailed states](evidence/detail.jpg)
- Android system permission prompts, notification delivery under background restrictions, playback, and the device document picker remain pending interactive verification.
