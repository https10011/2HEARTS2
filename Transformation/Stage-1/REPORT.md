# Stage 1 — Reproducible baseline

## Starting state and objectives

The first transformation commit, `7fc2f65eecea9f709a97ea0f54d39614bfd02b05`, contained only the roadmap and progress tracker. Both documents were present at that exact remote SHA and the Android workflow passed. This stage inventories the native application and reproduces the existing build, tests, and renders before any product redesign. The owner's successful Android 13 handset launch is the starting functional baseline. [Roadmap](../TWOHEARTS-MASTER-TRANSFORMATION-ROADMAP.md) · [Checkpoint CI](https://github.com/https10011/2HEARTS2/actions/runs/44471969961)

## Active source, assets, and constraints

| Area | Source inspected and baseline |
|---|---|
| Launch and lifecycle | `AndroidManifest.xml`, `MainActivity.kt`, `BootstrapService`, `LifecycleService`, `AppStateService`, and `AppRouter`. A single exported launcher activity observes appearance before drawing. Bootstrap opens Room on a background dispatcher. The router gates onboarding, app lock, then the shell. Retain loading and retry states and the internal notification-intent path. |
| Local persistence | `data/database`, entities, DAOs, repositories, `SettingsStorage`, `OnboardingDraft`, and `SecureStorage`. Room schema 13 has 17 entities and 16 DAOs, with migrations from earlier versions. DataStore persists onboarding and appearance. Both user profiles have a media reference. Room schema export is disabled in the current app. Existing on-disk tests cover reopen and preservation when a schema is unsupported. |
| Media, transfer, and privacy | `MediaStorage`, `MediaThumbnails`, `VaultStorage`, `DataManagementService`, `TransferDocument`, `SearchEngine`, and `AppLockService`. Selected files are copied into private storage. Thumbnail loads are bounded. Export and import cover Notes and Reminders, and regular search excludes Vault. The PIN verifier uses encrypted preferences and initializes lazily. |
| Reminders and capabilities | `ReminderScheduler`, `NotificationService`, `PermissionService`, and feature settings. WorkManager handles background delivery and the active manifest declares post-notification permission. Existing image flows use the system picker; document flows use Android content providers. Preserve the platform prompts and test API-specific outcomes. |
| Navigation and interface | `ui/navigation`, `ui/onboarding`, `ui/screens`, `ui/components`, and `ui/theme`. Five primary destinations share a bottom bar. Most detail/editor routes use common headers, lookup states, and feedback. The router contains 54 destination registrations and adds six Period destinations from `PeriodRoutes`. Eleven registrations are legacy game redirects. The onboarding and lock gates render outside this graph. |
| Branding and assets | `app/src/main/assets`, `app/src/main/res`, and `tools`. The official SVG lockup and mark feed the generated branding PNGs. The adaptive foreground resource has a legacy XML variant and a generated PNG variant. The repository also holds onboarding imagery, 20 floral SVGs, and five launcher density buckets. Stage 5 must check compiled resource selection and masks. |
| Yuki | `Yuki Assets`, `YukiService`, `YukiTypes`, `YukiViewModel`, `YukiCharacter`, and `YukiArt`. Gradle packages all 21 source poses in the debug APK. The pose files account for about 21 MiB before packaging. Whole-pose changes, background removal, and inconsistent attachments are the primary animation risks. Preserve local care, toys, accessories, and progression. |
| Build and tests | Gradle 8.11 wrapper, AGP 8.7.3, Kotlin 2.1.0, compile and target SDK 35, min SDK 26, application ID `com.twohearts.app`. The active APK workflow builds debug and runs the unit suite on JDK 17. There are 27 Kotlin test and fixture files under `app/src/test`, including four native screenshot harness source files. The app declares an instrumentation runner and dependencies and currently has no tracked `app/src/androidTest` suite. |
| History | `Migration/`, `UI-UX/`, and `Archive/` contain migration reports, the completed UI/UX program, and the archived Capacitor implementation and references. The Android Gradle settings select the native `:app` module. Keep the earlier evidence and official source art. |

### Product risks to verify during implementation

- Home displays initials as the default relationship centerpiece and repeats the couple’s names. Preserve data-driven dates and actions while making the portraits and names the focal point.
- Shared surfaces flatten the distinction between browsing, reading, and editing. Review empty, populated, long-text, and narrow states as each feature moves to the new design.
- The five-tab shell gives Us a prominent position but leaves narrow slots for labels. Evaluate both shells in Stage 6 and retain the option with clearer first-use journeys.
- Photo selection currently exists for the owner settings route. There is no shared, explicit replace/remove contract covering both roles or onboarding.
- The archived version 13 migration adds the profile photo column. The native migration from version 12 to 13 is a no-op because its earlier native schema already included that column. Stage 2 should exercise both version 12 input shapes with retained records.
- A successful build alone leaves launcher resource resolution, system pickers, upgrade signatures, notification delivery, and device performance open. Keep genuine Android runtime evidence with the relevant stages.

## Route and interaction inventory

Relative routes below are under `/app` unless specified. Each group lists the implementation stages and the evidence to capture. Add the final evidence link or an explicit environment limitation for each row in Stage 20.

| Surface or route group | Stages | Required evidence |
|---|---|---|
| Startup loading, recovery and retry | 2, 4, 22 | Fresh and returning launch, recoverable startup state, retry, Logcat on failure |
| Onboarding Welcome, Profile, Relationship, Personalization, App Lock, and Complete outside NavHost | 7, 8, 22 | Six existing views and the new capabilities step, invalid inputs, draft recreation, optional PIN, denial and skip, owner and partner crop |
| Onboarding date/month/year chooser, layout and commit-error retry modal | 4, 8 | Large text, short screen, confirm/cancel, validation and save retry |
| Home `/home`, relationship duration, next date, notes/reminders, memory and mood previews, Yuki entry | 6, 9, 19 | Light/dark, narrow/large text, both/one/no photo, empty and populated, actionable navigation |
| Us `/us`, relationship hero and links, recent memory and milestone | 6, 10 | Live data load, pair fallbacks, empty and populated, all entry points |
| More `/more`, search and utility shortcuts | 6, 15 | Discoverability, label wrapping, settings and utilities reachable |
| Notes `/notes`, `/notes/add`, `/notes/{id}`, `/notes/{id}/edit` | 12 | Empty/list, filter and search, editor and long detail, soft deletion confirmation, restart |
| Memories `/memories`, `/memories/add`, `/memories/{id}`, `/memories/{id}/edit` | 11 | Empty gallery and populated gallery, multiple photo/video picker, media preview, playback dialog, editor errors and deletion |
| Timeline `/timeline`, `/timeline/add`, `/timeline/{id}`, `/timeline/{id}/edit` | 12 | Empty/populated chronological list, date chooser, editor, detail and delete dialog |
| Reminders `/reminders`, `/reminders/add`, `/reminders/{id}`, `/reminders/{id}/edit` | 13 | Due and completed states, time and date controls, recurrence fields, edit/delete, scheduled and denied notifications |
| Important Dates `/us/important-dates` | 13 | Empty/list, add/edit form dialogs, date picker, recurrence and deletion |
| Notifications `/notifications`, notification intent and settings permission prompt | 6, 8, 13, 15 | Empty/list, notification deep entry, permission grant/denial and system settings, deduplication |
| Places `/places`, `/places/add`, `/places/{id}`, `/places/{id}/edit` | 14 | Category and empty states, image picker, date control, detail/edit and delete confirmation |
| Mood `/mood`, `/mood/add`, `/mood/history`, `/mood/{id}/edit` | 14 | Mood choice semantics, editor, empty/history and update/deletion |
| Period `/period`, `/period/log`, `/period/{id}/edit`, `/period/calendar`, `/period/history`, `/period/settings` | 14 | All six destinations, private entry and deletion, calendar dates, history and configuration |
| Vault `/vault`, `/vault/add`, `/vault/content/{id}` | 16 | Empty/list, note/photo/video/file creation and viewer, content picker, video dialog, document export and deletion |
| Yuki `/yuki`, room and toy/accessory/keepsake dialog | 17–19 | Light and dark room, idle and action clips, accessibility, saved progress, lifecycle and performance |
| Legacy `/games`, `/games/memory-match`, `/games/word-scramble`, `/games/couple-trivia`, `/games/who-knows-who-better`, `/games/would-you-rather`, `/games/this-or-that`, `/games/guess-my-answer`, `/games/casual-trivia`, `/games/riddle-room`, `/games/results` | 6, 19 | Each redirect resolves to Yuki, and Back restores the origin without a redirect loop |
| Search `/search` | 6, 15 | Empty/search/no-results, valid record deep link, removed result, exclusion of Vault and sensitive data |
| Settings `/settings`, Profile `/settings/profile`, Relationship `/settings/relationship` | 7, 15 | Overview, both profile saves and photo edits, date picker, error and restart, Home refresh |
| Appearance `/settings/appearance`, notification settings `/settings/notifications` | 3, 4, 8, 15 | Light/dark/system, font and motion controls, device permission and preference states |
| Security `/settings/security`, app lock gate and change/disable PIN dialog | 2, 16, 22 | Cold and background lock, failed and successful unlock, confirm old PIN, cancel |
| Storage `/settings/storage`, Import/Export `/settings/import`, About `/about` | 15 | Cache/reset confirmation, read/write provider sheets, format errors, transfer scope, About and app metadata |
| Shared route states and overlays | 4, 6, 20 | Record lookup loading/missing, status and feedback banners, cancellation, modal focus, empty and error states, system Back and IME insets |

## Local build, tests, and evidence

| Check | Result |
|---|---|
| Toolchain | Eclipse Adoptium Temurin 17.0.21.1, checked-in Gradle 8.11, Android SDK Platform 35 revision 2, AGP-selected Build Tools 34.0.0, and Build Tools 35.0.0 for APK inspection. The workspace proxy and trusted CA were configured outside the checkout. |
| Gradle | `./gradlew :app:testDebugUnitTest :app:testReleaseUnitTest :app:assembleDebug --no-daemon --max-workers=3 --console=plain` completed successfully. The JUnit XML recorded 134 debug and 134 release tests, with zero failures, errors, or skips. |
| Debug APK | `com.twohearts.app` 1.0.0, version code 1, minimum SDK 26, target SDK 35, `MainActivity`, four Android ABIs, and all 21 Yuki poses. Signature verification, ZIP alignment, and archive integrity passed. SHA-256 `472499725be0754741ff161419c4035f281a92ae707bf1bc8fe8873d16a6ab82`. |
| Native Compose | The local harnesses generated 149 reference PNGs using test-owned data. The review includes light and dark, 360 dp layouts, the existing in-app Large and Extra Large settings, and the short 274 × 488 dp Notes state. These in-app sizes are 112% and 128%. The 200% Android system font-scale pass remains part of the later UI gates. |
| Android runtime | Set up an isolated API 33 x86-64 software guest. The original image extraction exceeded the workspace disk capacity. Sparse extraction and dedicated userdata let the guest reach Android system services. Running it alongside Gradle saturated the host memory limit, so the guest was stopped before a clean APK launch was observed. Run the emulator and Gradle sequentially in Stage 2. The owner already confirmed their baseline APK on the TECNO handset. |
| CI checkpoints | The pre-transformation run and roadmap-only run succeeded. The Stage 1 commit is the commit introducing this report and its evidence. The next progress update records that commit's exact remote and workflow verification. [Baseline CI](https://github.com/https10011/2HEARTS2/actions/runs/44439439508) · [Roadmap CI](https://github.com/https10011/2HEARTS2/actions/runs/44471969961) |

The render review confirmed that Home spends its opening space on small initials, repeated names, and a separate duration card. The current Us entry remains prominent in the short shell, and the Notes creation action fits above it. Record and settings layouts reuse similar visual weights even when the tasks differ. Dark Home's outlined brand mark has limited separation from the background. The Yuki images confirm the current character design and care controls. They capture individual poses, so Stage 19 will need motion evidence for transitions and idle behavior. [Identity and shell](evidence/identity-and-shell.jpg) · [Records and utilities](evidence/records-and-utilities.jpg) · [Yuki poses](evidence/yuki-poses.jpg)

### Signing evidence

Both CI APKs passed `apksigner` verification. Their certificate subjects identify Android Debug, and their SHA-256 fingerprints differ. These two CI APKs cannot update one another in place. The already installed handset APK remains the functional baseline and its certificate has yet to be identified.

| APK | Certificate SHA-256 |
|---|---|
| Pre-transformation artifact 14489748983 | `e57ce614260bf5fb422c710833d6c425dd090b0ca624c29b789a5b6b481155c2` |
| Roadmap checkpoint artifact 14503404924 | `543ef33e0d7f036627f6ea4fd4fd442b965049a30f2de07e33b253ca052c90c1` |
| Local Stage 1 debug APK | `422b889111ac2bbc0ef39a264bd07bf57550e82f96ca8adcbb1ef6c6b2da3e75` |

## Completion checklist

- [x] Record objectives, starting SHA, inspected areas, findings, changed files, dependencies, and acceptance results. This stage changes the progress tracker and adds this report and three sheets of native render evidence.
- [x] Run the debug and release unit suites and build the debug APK. Existing test coverage is preserved.
- [x] Render and inspect the native baseline in light and dark, narrow and standard sizes, and at the existing enlarged in-app scale. Record the additional Android font-scale and runtime coverage required later.
- [x] Map production entry points and persistence boundaries. Existing tests cover the current save, failure, navigation, and data-retention paths. Stage 2 owns fresh runtime and version-upgrade checks.
- [x] Record local results, evidence, limitations, and the next stage in the report and progress tracker.
- [ ] Verify the published Stage 1 commit and its own successful CI run. Record both in the next progress update.

## Next stage

Stage 2 verifies cold and warm startup, onboarding restart, returning Home, locked launch, process recreation, worker startup, Room migrations and unsupported-schema recovery. Use a test-owned emulator and matching signing certificates for retained-data upgrades. Investigate the version 12 profile schema with and without the optional photo column. The historical DataStore crash is closed, and its regression tests stay active.
