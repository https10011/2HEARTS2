# Stage 20 — Cross-app visual, accessibility, and performance audit

## Starting point

Started from verified `4f0cb76f9b5e34f2e4966f5c2316560bda737a51`. Compared the current app with the Stage 1 route inventory and signed debug APK. Reviewed `MainActivity`, `AppRouter`, the onboarding flow, route and intent mappings, shared components and theme, record editors and readers, Settings, App Lock, Vault, bitmap loaders, and the Yuki art and motion paths. Traced the production callbacks to the relevant Room, DataStore, WorkManager, and local file tests. [Baseline and full inventory](../Stage-1/REPORT.md) · [Starting checkpoint](../Stage-19/REPORT.md)

## Findings and changes

- The Material confirmation window dropped the app’s font-scale override. In several native previews, a small title and small actions appeared at the top of a large blank window. The shared confirmation now uses the app’s modal sheet. It announces the pane and heading, scrolls the explanation independently, and keeps the primary and cancel actions visible. The busy guard prevents closing or invoking either action again. Vault export and deletion reuse the same sheet and retain their existing callbacks.
- Opaque cards inherited the surrounding content color, which could leave dark text on dark surfaces. Each filled tone now supplies its foreground. Transparent outlines keep the underlying surface’s foreground. Shared section headers expose heading semantics.
- The 320 dp navigation review found that Notes and More read like one label at 200%. The first gutter treatment clipped Home and Notes. The final layout makes more of the bar available to the tabs and uses the compact label style, which still follows the system scale. All four labels fit at 320 dp and 200%, with the original routes and keyboard behavior.
- The recorded Yuki play sequence briefly showed a double silhouette while blending from the jump to the raised paw. Inserted an existing registered landing pose before the reach. The motion keeps its original three-second duration and arrivals at the same floor anchor. The source artwork and care saves remain intact.

Changed `ConfirmDialog.kt`, `SurfaceCard.kt`, `BottomNav.kt`, `VaultContentViewer.kt`, `YukiMotion.kt`, the Yuki sprite layout and generated atlas, and the focused accessibility, navigation, and motion tests.

## Route audit

The overview sheets contain 128 native Compose renders with isolated test records. The review covered light and dark, empty and populated states, long names and records, form errors, missing content, and standard and narrow widths at default and 200% effective text size. The tests exercise the app’s data paths separately from the render fixtures. OS-level checks are tracked in the release rehearsal.

| Stage 1 inventory | Review and evidence |
|---|---|
| Startup loading, recovery and retry | Rechecked startup and lock sequencing, settings-store singleton coverage, and Room upgrade tests. The current APK still needs an Android process-level cold and warm walkthrough. [Startup and upgrade](../Stage-2/REPORT.md) |
| Onboarding Welcome, Profile, Relationship, Personalization, Device access, App Lock, Complete | Reviewed all seven screens, optional portraits, denial/skip, and pinned actions on short screens. DataStore draft restoration and Room commit rollback remain covered. [Native renders](evidence/onboarding.jpg) · [Persistence and permissions](../Stage-8/REPORT.md) |
| Onboarding dates and commit-error retry | Reviewed the common wheel picker at large type, its selection and clear actions, balance of scrollable vs pinned onboarding content, and safe save retry in the domain tests. A process-level interrupted setup check remains for release. [Date controls](evidence/utility-and-local-files.jpg) · [Setup retry](../Stage-8/REPORT.md) |
| Home | Reviewed no photos, one photo, both photos, long names, dynamic story and task sections, and the saved Yuki entry. [Native renders](evidence/shell-and-home.jpg) |
| Us | Reviewed empty and populated identity, date setup, long names, story sections, and private destinations. [Native renders](evidence/relationship-and-more.jpg) · [Routes and refresh](../Stage-10/REPORT.md) |
| More | Reviewed search, alerts, settings, profiles and utilities. Verified primary tab selection, reading order, and target sizes on the narrow shell. [Native renders](evidence/relationship-and-more.jpg) · [Navigation tests](../../app/src/test/java/com/twohearts/app/TransformationNavigationUiTest.kt) |
| Notes | Reviewed empty, category filter, editor, long detail, missing detail and removal. Existing save and search tests cover stored notes. [Native renders](evidence/notes-and-timeline.jpg) · [Records and persistence](../Stage-12/REPORT.md) |
| Memories | Reviewed both gallery states, ordered photo/video attachments, editor validation, photo preview, unavailable-video recovery, and deletion. Live picker and codec playback remain release checks. [Native renders](evidence/memories.jpg) · [Local media tests](../Stage-11/REPORT.md) |
| Timeline | Reviewed empty and grouped entries, invalid date, new/edit form, detail, and deletion. [Native renders](evidence/notes-and-timeline.jpg) · [Record tests](../Stage-12/REPORT.md) |
| Reminders | Reviewed pending and completed states, schedule fields, recurrence, date validation, and deletion. Existing tests cover WorkManager scheduling and completion. [Native renders](evidence/calendar-and-tasks.jpg) · [Scheduling tests](../Stage-13/REPORT.md) |
| Important Dates | Reviewed fallback, chronology, next date, long names, and edit/recurrence controls. Existing tests cover calendar boundaries and saving. [Native renders](evidence/calendar-and-tasks.jpg) |
| Notifications | Reviewed empty and populated alerts, allowed and denied capabilities, notification preferences, and target routing and deduplication. Android delivery and tapping a system notification remain release checks. [Native renders](evidence/notifications-and-search.jpg) · [Delivery and routing tests](../Stage-13/REPORT.md) |
| Places | Reviewed empty and populated categories, long detail, local photo, editor, and deletion. [Native renders](evidence/places-and-mood.jpg) · [Local media and persistence](../Stage-14/REPORT.md) |
| Mood | Reviewed empty and saved check-ins, all choices, editor and long notes, and date-grouped history. [Native renders](evidence/places-and-mood.jpg) |
| Period | Reviewed home, log, edit, calendar, history, and settings with invalid values. Calendar labels announce the full day and status. Date cells convey information and have no tap actions. [Native renders](evidence/period.jpg) |
| Vault | Reviewed empty/list, note, photo, video and file states, missing attachment, separate copy, and deletion. Real document-provider interaction and media playback remain release checks. [Native renders](evidence/vault.jpg) · [Privacy and reference checks](../Stage-16/REPORT.md) |
| Yuki | Reviewed the light and dark room, narrow controls, keepsakes, Home portrait, and native interaction recording. Automated tests cover idle variation, care, reduced motion, and foreground return. [Room and Home](../Stage-19/evidence/room-and-home.jpg) · [Keepsakes](../Stage-19/evidence/keepsakes-and-return.jpg) · [Revised play sequence](evidence/yuki-landing.jpg) |
| Legacy game routes | All eleven recorded entry points redirect to the same Yuki room. The navigation test checks the old alias is replaced and Back preserves the origin. [Navigation tests](../../app/src/test/java/com/twohearts/app/TransformationNavigationTest.kt) |
| Search | Reviewed empty, accent-insensitive match, and no-results states. Tests cover opening records, Back to search, and excluding protected content. [Native renders](evidence/notifications-and-search.jpg) · [Search tests](../Stage-15/REPORT.md) |
| Settings, Profile, Relationship | Reviewed overview, name editing, portrait controls, date picking, and pinned save. Room and file tests cover both partner portraits and retained data. [Native renders](evidence/relationship-and-more.jpg) · [Photo persistence](../Stage-7/REPORT.md) |
| Appearance and notification settings | Reviewed theme, text scale, reduced motion, denied system alerts, and local preferences. [Appearance](evidence/utility-and-local-files.jpg) · [Alerts](evidence/notifications-and-search.jpg) |
| Security and app-lock gate | Reviewed cold gate, wrong PIN, PIN setup/change/disable, and large-text form actions. Confirmed lock state before protected content in the routing test. [Native renders](evidence/access-and-recovery.jpg) · [Access tests](../Stage-16/REPORT.md) |
| Storage, Import/Export, About | Reviewed counts, cache cleanup and reset confirmations, transfer format and scope, preview, result, failure, and About. File-provider interaction remains a release check. [Native renders](evidence/utility-and-local-files.jpg) · [Round-trip tests](../Stage-15/REPORT.md) |
| Shared loading/error states and overlays | Reviewed record missing/error, success and failure feedback, and modal actions. Tests check heading semantics, tab order, modal pane title, keyboard-inset response, button dimensions, scroll and busy Back handling. Live TalkBack, gesture insets, and system keyboard behavior remain device checks. [State renders](evidence/access-and-recovery.jpg) · [Shared controls](evidence/shared-controls.jpg) |

## Performance comparison

- The local debug APK is 28,062,156 bytes, 40.4% smaller than the 47,117,983-byte Stage 1 APK. Compressed Yuki entries fell from 22,125,542 to 1,611,234 bytes. The pipeline packages 26 aligned 416 × 324 tiles and an atlas. The Yuki process cache is bounded to 5 MiB. The renderer holds the current clip and its fallback, decodes changes off the main thread, and samples at most once every 33 ms while active. The handset’s frame time still needs measurement.
- The Home companion portrait loads one saved pose and runs no animation clock. Images for local records use bucketed sampled sizes, a memory-scaled thumbnail cache capped at 16 MiB, and two concurrent background decodes. Both portraits save a square image up to 1,024 px and share the display path.
- There is no comparable Stage 1 hardware startup or frame trace. The suite rechecks cold lock state, retained records, and returning view state. Full-runtime startup timing, live screen-reader traversal, and extended memory measurements remain release or handset checks.

## Verification and checkpoint

- Debug and release each passed 258 tests with zero failures, errors, or skips. The reproducible art check verified all 26 tiles and the atlas. Inspected the rerendered confirmation, dark surfaces, Vault decisions, full navigation labels, and the landing sequence.
- `:app:assembleDebug` passed. The APK passed archive integrity, alignment, and v2 signature verification. It reports `com.twohearts.app` 1.0.0 (1), min API 26, target API 35, and four ABIs. Local SHA-256 `017f91cef2c63f794e4ee0bae20a4d6fdc3c5420b8d501358d12023a566c583c`.
- ADB showed no devices. The cross-app review used native Compose and existing process and lifecycle tests. Stage 22 verifies release behavior in available Android runtimes. The exact remote SHA, CI run, and artifact belong in the next verified progress update.

## Completion checklist

- [x] Record the starting commit, audit findings and inventory, implementation decisions, test inputs, and device limitations.
- [x] Review first-use, normal, empty, error, and long-content states across the route inventory. Rerender changes in dark and light and check the affected narrow, short, and 200% layouts.
- [x] Verify control targets, labels, modal boundaries, busy state, card text contrast, route reachability, and frame continuity through the affected sequence.
- [x] Run both full test variants, regenerate Yuki art deterministically, check the APK, and review the Git diff.
- [ ] Verify this checkpoint on `origin/master` and its exact CI and artifact, then begin repository cleanup.
