# Phase 12 — Settings, profile, and utilities

- Settings groups personal details, preferences, data, and app information. About uses the supplied TwoHearts mark and reports the installed app version and build.
- Profile and relationship settings load and save existing Room records. The profile accepts a local photo through Android’s picker and shows it on profile avatars. Editing a name or birthday preserves the existing photo. Profile changes use the original record ID.
- Appearance persists theme, text size, and reduced motion. Notifications show app preferences alongside the current Android notification status. Notification Center identifies unread entries and opens their corresponding feature when a destination is known.
- Search observes notes, memories, reminders, places, and timeline events from Room. Results use the existing accent-insensitive ranking and show their feature. Private period and Vault records stay outside the search sources.
- Import/export uses the existing versioned Notes and Reminders document. Import retains support for the earlier unlabelled document shape. The chooser explicitly discloses the export scope. The app validates input and reports partial imports.
- Storage refreshes usage after cleanup. The confirmed reset delegates package-local data removal to Android. The app leaves exported files in the chosen destination.

## Verification

- All 105 tests passed per variant and the debug APK assembled. Tests cover ranking, transfer compatibility, profile field clearing with photo retention, and the reset request against Robolectric’s Android service. The reset was not invoked on a handset.
- Compose review includes profile with a long saved name, Settings, Relationship, Appearance, Notifications, Storage loading, Search landing, empty Notification Center, About, and import/export. Dark 360 dp captures use extra-large type. [Compose review](evidence/compose-review.jpg)
- Delivery of a scheduled system notification and Android document-picker interactions remain pending runtime validation. The fresh Android 13 guest stayed offline. The notification scheduler lacks production callers, which needs follow-up during the end-to-end review.
