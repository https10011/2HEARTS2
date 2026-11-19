# Stage 8 — Onboarding and first-launch permissions

## Starting checkpoint

Stage 7 was verified at `a8529f8468c3ff4afd9be4d46d3da8872fc5f739`. Onboarding saved names, dates, and appearance across four numbered steps. PIN input lived in the current composition. The photo picker and Android notification controls lived in Settings. Reminder delivery used WorkManager. [Stage 7 report](../Stage-7/REPORT.md)

Reviewed the onboarding forms and route guard, AppStateService, the DataStore draft and completion flags, Room profiles and relationships, the photo staging and writer services, secure PIN storage, Notification Settings, the manifest, and the native rendering and persistence tests.

## Implementation

- The owner and partner forms share the Settings photo picker, circular crop and fallback avatar. Selected drafts live in private cache. DataStore stores the accepted draft ID before removing an older accepted image. Canceled picks and crops preserve the last accepted choice. A missing cached draft falls back to the stored photo or initials.
- The new **Device access** step follows Appearance and precedes App Lock. It explains optional photo selection and approximate reminder timing. It shows notification status, requests the Android 13+ grant after an explicit tap, and offers Android settings after denial. Skipping alerts still opens the optional PIN screen. Returning users find the same controls in Notification Settings.
- Existing stage keys remain valid. DataStore saves the stage and form draft in one edit. Invalid or incomplete restored drafts return to the first incomplete form. The welcome artwork scales with the viewport, while Begin and the Device access actions stay pinned outside the scroll area.
- Both profiles, their photos and the relationship save together in Room. Setup reuses the rows on retry and preserves the original relationship ID. The PIN uses existing secure storage. Completion flags and draft cleanup share one DataStore edit after the domain and PIN writes. The summary observes committed photos and the actual app-lock state.

## Verification

- Followed the production onboarding and settings wiring and exercised the shared controls in native Compose. Tests cover legacy draft decoding, stage restoration, DataStore completion flags, notification policy and denial/skip actions, transaction rollback on a corrupt second photo, and retry without duplicate rows. Existing photo and onboarding regression tests also ran.
- Inspected six native renders across light and dark themes. The short 360 × 640 dp dark renders at 200% keep Begin and both capability actions visible. Longer explanations scroll behind the pinned action area. Owner and partner photo-entry forms, unrequested and enabled alerts, and disabled alerts with the Android settings action are shown in the evidence. [Native Compose onboarding renders](evidence/setup-and-device-access.jpg)
- Each full unit suite passed 170 tests with zero failures, errors, or skips. The debug APK passed v2 signing, alignment, archive integrity, and package metadata checks. It retains `com.twohearts.app` 1.0.0 (1), min API 26 and target API 35. The manifest declares notifications and the existing background-work permissions. Photo selection uses the picker with no broad media or storage grant. Local APK SHA-256 `6a262c8de9872cc6f51d1ce63dbbdd794e78131f2930eeeff3105a1c8f74f062`.
- A live Photo Picker, notification grant and first-launch walkthrough on the TECNO handset remains part of release validation. The native tests exercise the flows with isolated data and controlled permission state.

- Git verified `4b5161a2704ce105b6ed101e08bcc4e43a76183a` on `origin/master`. Its Android workflow passed and published the stage APK. [Commit](https://github.com/https10011/2HEARTS2/commit/4b5161a2704ce105b6ed101e08bcc4e43a76183a) · [CI](https://github.com/https10011/2HEARTS2/actions/runs/44566311574) · [APK](https://github.com/https10011/2HEARTS2/actions/runs/44566311574/artifacts/14550704939)

## Completion checklist

- [x] Record the starting commit, source reviewed, initial behavior, choices, and stage dependencies.
- [x] Verify draft restoration, transaction rollback and retry, alert policy, Back and skip.
- [x] Render the changed Compose screens, inspect the responsive states, and record device limits.
- [x] Run both full test variants, verify the assembled APK, and review the final diff.
- [x] Commit, push, and verify the exact remote SHA and Android workflow.
- [x] Record the verified checkpoint and update the progress tracker.

## Next stage

Stage 9 redesigns Home around the two saved portraits, names and relationship duration, then gives real records and first-use actions a clear order.
