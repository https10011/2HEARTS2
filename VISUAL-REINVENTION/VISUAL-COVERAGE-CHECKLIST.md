# Visual coverage checklist

Source root is app/src/main/java/com/twohearts/app/ui. Status **A** means inventoried at baseline. **I** means implementation exists, **T** tests passed, **V** named renders inspected, and **C** accepted across all applicable checks. Each open row requires default, empty/partial/populated, loading, failure/retry, pressed/focused/selected/disabled, light/dark, narrow/standard, 200% type, semantics and functional review where applicable. Record inapplicable states with a reason. A baseline image does not close a redesign row.

| Phase | Experience | Source | States and interactions | Design treatment | Implementation / verification |
|---|---|---|---|---|---|
| 01 | Startup | MainActivity | System splash → bootstrap / retry → first destination | Dedicated vector field; hydrate settings without a delay | T/V · Phase-01/evidence |
| 01 | Welcome | onboarding/WelcomeScreen | Fresh / resumed / large type | Book cover and a single Begin action | T/V · Phase-01/evidence |
| 03 | Setup | onboarding | Owner, partner, relationship date, personalization, capabilities, PIN, completion, draft restore | Page by page inscription, inline validation, optional photo and skip | A · open |
| 02 | Shell | navigation | Five roots, system Back, keyboard, notifications, search, long-label and selected state | Flush dock with a round center seal and measured safe area | I/T · inspected samples in Phase-02/evidence |
| 02 | Home | screens/home | Missing / one / two portraits, long names, upcoming, notes, mood, Yuki | Portrait frontispiece and timely actions | I/T · inspected samples in Phase-02/evidence |
| 03 | Us | screens/us | First visit, dated, populated, missing records and feature links | Relationship contents with date gutter and grouped chapter links | A · open |
| 03 | More | screens/more | Search, notification, settings and feature entry points | Scannable functional index | A · open |
| 04 | Memories | screens/memories | Gallery, text only, detail, add, edit, photo / video preview, delete, failed media | Contact sheet, captioned photo plate and media-first editor | A · open |
| 04 | Notes | screens/notes | List, favorite/shared/category, search, add/edit, reader, delete, missing | Letter index, writing surface and quiet reader | A · open |
| 04 | Timeline | screens/timeline | Month groups, empty, add/edit/detail, unknown date, deletion | Date gutter with a continuous chronology | A · open |
| 04 | Places | screens/places | Category selector, new/edit/detail, missing photo, delete | Travel folio, local photo and location caption | A · open |
| 05 | Important dates | screens/importantdates | Upcoming, repeat, add/edit, date picker, deletion, empty | Calendar leaf and subsequent entries | A · open |
| 05 | Reminders | screens/reminders | Pending/completed, overdue, detail, editor, repeat, opt out, delete | Agenda with due column, explicit completion action | A · open |
| 05 | Notifications | screens/notifications | Read/unread, empty, clear, open target, settings | Grouped delivery log with accessible status | A · open |
| 05 | Mood | screens/mood | Quick check-in, choices, edit, private note, history, remove | Emotion chooser and dated journal | A · open |
| 05 | Period | screens/period | Home, calendar estimated/logged, history, new/edit, settings, invalid range | Calendar first with native accessible day cells | A · open |
| 06 | Search | screens/search | Idle, loading, empty query, no hit, results, failure, clear | Search field plus grouped results | A · open |
| 06 | Preferences | screens/settings | Index, profile/partner photo, birthday, relationship date, appearance, permissions, busy/error | Personal details and live appearance specimen | A · open |
| 06 | Data transfer | screens/settings | Storage, counts, remove temp, erase, import preview, invalid/partial file, export cancelled/saved | Explicit file summary and distinct danger region | A · open |
| 06 | About | screens/about | Version, local privacy and credits | Colophon with official mark | A · open |
| 06 | Security | screens/security | Locked, rejected PIN, enable/change/disable, cancel, reopening | Single-focus private access prompt | A · open |
| 06 | Vault | screens/vault | List, add, note/photo/video/document, preview, export warning, deletion, missing file | Private archive with explicit reveal/save controls | A · open |
| 07 | Yuki | screens/yuki | Loading/retry, room, idle, play, sleep, feed, drink, pet, accessory, toy, refusal, background | Room fills stage, care controls at its foot and keepsake drawer | A · open |
| 06 | Shared states | components | Date picker, crop, sheets, confirmation, toast, banner, fields, switch, tabs, video, loading, invalid record | Contextual action hierarchy, safe scrolling, clear recovery | A · open |

## Route-level ledger

AppRouter and PeriodRoutes bind these destinations. The onboarding paths name flow steps. Capabilities is an additional persisted onboarding step. Eleven /app/games aliases redirect to Yuki and require navigation regression checks. Source names below identify screen implementations within each family.

| Destination | Implementation | Status and evidence |
|---|---|---|
| `/onboarding/welcome` | OnboardingFlow / WelcomeScreen | T/V · Phase-01/evidence |
| `/onboarding/profile` | OnboardingFlow / ProfileSetupScreen | A |
| `/onboarding/relationship` | OnboardingFlow / RelationshipSetupScreen | A |
| `/onboarding/personalization` | OnboardingFlow / PersonalizationSetupScreen | A |
| `/onboarding/app-lock` | OnboardingFlow / AppLockSetupScreen | A |
| `/onboarding/complete` | OnboardingFlow / SetupCompleteScreen | A |
| `/app/home` | HomeScreen | A |
| `/app/us` | UsScreen | A |
| `/app/more` | MoreScreen | A |
| `/app/notifications` | NotificationCenterScreen | A |
| `/app/search` | SearchScreen | A |
| `/app/notes` | NotesHome NoteEditor NoteDetail | A |
| `/app/notes/add` | NotesHome NoteEditor NoteDetail | A |
| `/app/notes/:noteId/edit` | NotesHome NoteEditor NoteDetail | A |
| `/app/notes/:noteId` | NotesHome NoteEditor NoteDetail | A |
| `/app/memories` | MemoriesHome AddMemory MemoryDetail | A |
| `/app/memories/add` | MemoriesHome AddMemory MemoryDetail | A |
| `/app/memories/:memoryId/edit` | MemoriesHome AddMemory MemoryDetail | A |
| `/app/memories/:memoryId` | MemoriesHome AddMemory MemoryDetail | A |
| `/app/timeline` | TimelineHome AddEvent EventDetail | A |
| `/app/timeline/add` | TimelineHome AddEvent EventDetail | A |
| `/app/timeline/:eventId/edit` | TimelineHome AddEvent EventDetail | A |
| `/app/timeline/:eventId` | TimelineHome AddEvent EventDetail | A |
| `/app/reminders` | RemindersHome CreateReminder ReminderDetail | A |
| `/app/reminders/add` | RemindersHome CreateReminder ReminderDetail | A |
| `/app/reminders/:reminderId/edit` | RemindersHome CreateReminder ReminderDetail | A |
| `/app/reminders/:reminderId` | RemindersHome CreateReminder ReminderDetail | A |
| `/app/places` | PlacesHome CreatePlace PlaceDetail | A |
| `/app/places/add` | PlacesHome CreatePlace PlaceDetail | A |
| `/app/places/:placeId/edit` | PlacesHome CreatePlace PlaceDetail | A |
| `/app/places/:placeId` | PlacesHome CreatePlace PlaceDetail | A |
| `/app/mood` | MoodHome MoodEntry MoodHistory | A |
| `/app/mood/add` | MoodHome MoodEntry MoodHistory | A |
| `/app/mood/:entryId/edit` | MoodHome MoodEntry MoodHistory | A |
| `/app/mood/history` | MoodHome MoodEntry MoodHistory | A |
| `/app/period` | PeriodHome LogPeriod PeriodCalendarScreen PeriodHistoryScreen PeriodSettingsScreen | A |
| `/app/period/log` | PeriodHome LogPeriod PeriodCalendarScreen PeriodHistoryScreen PeriodSettingsScreen | A |
| `/app/period/:entryId/edit` | PeriodHome LogPeriod PeriodCalendarScreen PeriodHistoryScreen PeriodSettingsScreen | A |
| `/app/period/calendar` | PeriodHome LogPeriod PeriodCalendarScreen PeriodHistoryScreen PeriodSettingsScreen | A |
| `/app/period/history` | PeriodHome LogPeriod PeriodCalendarScreen PeriodHistoryScreen PeriodSettingsScreen | A |
| `/app/period/settings` | PeriodHome LogPeriod PeriodCalendarScreen PeriodHistoryScreen PeriodSettingsScreen | A |
| `/app/vault` | VaultHome AddVaultContent VaultContentViewer | A |
| `/app/vault/add` | VaultHome AddVaultContent VaultContentViewer | A |
| `/app/vault/content/:contentId` | VaultHome AddVaultContent VaultContentViewer | A |
| `/app/yuki` | YukiScreen | A |
| `/app/games` | NavigationActions → Yuki | A |
| `/app/games/memory-match` | NavigationActions → Yuki | A |
| `/app/games/word-scramble` | NavigationActions → Yuki | A |
| `/app/games/couple-trivia` | NavigationActions → Yuki | A |
| `/app/games/who-knows-who-better` | NavigationActions → Yuki | A |
| `/app/games/would-you-rather` | NavigationActions → Yuki | A |
| `/app/games/this-or-that` | NavigationActions → Yuki | A |
| `/app/games/guess-my-answer` | NavigationActions → Yuki | A |
| `/app/games/casual-trivia` | NavigationActions → Yuki | A |
| `/app/games/riddle-room` | NavigationActions → Yuki | A |
| `/app/games/results` | NavigationActions → Yuki | A |
| `/app/settings` | SettingsHomeScreen | A |
| `/app/settings/profile` | ProfileSettingsScreen | A |
| `/app/settings/relationship` | RelationshipSettingsScreen | A |
| `/app/settings/appearance` | AppearanceSettingsScreen | A |
| `/app/settings/notifications` | NotificationSettingsScreen | A |
| `/app/settings/security` | SecuritySettingsScreen | A |
| `/app/settings/storage` | StorageSettingsScreen | A |
| `/app/settings/import` | ImportScreen | A |
| `/app/about` | AboutScreen | A |
| `/app/us/important-dates` | ImportantDatesScreen | A |
| Onboarding capabilities step | CapabilitiesSetupScreen | A |
| Returning-user PIN gate | AppLockGate | A |

## Shared components and overlay owners

Each entry lists the actual component functions to check in their callers. Interactive controls require a touch, focus, disabled, busy and recovery pass as applicable.

| Source file | UI functions | Status |
|---|---|---|
| Aliases.kt | Header, EmptyState, Input, Card, ConfirmDialog, TextThButton | A |
| BrandLogo.kt | BrandLogo | A |
| Button.kt | ThButton, ThQuietButton | A |
| ChoiceChip.kt | ThChoiceChip | A |
| ConfirmDialog.kt | ThConfirmDialog | A |
| CouplePortrait.kt | CouplePortrait, PortraitColumn, PortraitRow, PortraitAvatar, PortraitName | A |
| DatePicker.kt | ThDatePicker, DatePickerSheet, PickerColumn, ThDateValue | A |
| Divider.kt | ThDivider | A |
| EmptyState.kt | ThEmptyState | A |
| Header.kt | ThHeader, HeaderSlot | A |
| IconButton.kt | ThIconButton | A |
| Input.kt | ThInput | A |
| LoadingState.kt | ThLoadingState | A |
| Modal.kt | ThModal | A |
| NotificationPermissionControl.kt | rememberNotificationPermissionControl | A |
| ProfileAvatar.kt | ProfileAvatar | A |
| ProfilePhotoEditor.kt | profilePhotoBackAction, ProfilePhotoEditor, ProfilePhotoCropSheet, ProfilePhotoCropContent, CropSlider | A |
| RecordEditorLayout.kt | RecordEditorLayout | A |
| RecordReadingLayout.kt | RecordReadingLayout | A |
| RecurrencePicker.kt | RecurrencePicker | A |
| Screen.kt | ThScreen | A |
| ScreenTopBar.kt | ThTopBarBackAction, ThTopBar | A |
| SectionTabs.kt | ThSectionTabs | A |
| StatusBanner.kt | ThStatusBanner | A |
| SurfaceCard.kt | ThSurfaceCard, ThSectionHeader, ThEyebrow, ThCard, ThTintedBand | A |
| Switch.kt | ThSwitch | A |
| Toast.kt | ToastProvider, FeedbackBanner, useToast | A |
| UtilityIndex.kt | UtilityGroup, UtilityItem, UtilityPage | A |
| VideoPreviewDialog.kt | VideoPreviewDialog, VideoPreviewDialog | A |

## Evidence and performance

Baseline evidence is in Phase-00/evidence and the baseline report. Attach revised screenshots and commit references to each accepted group. Measure decoded media and visible room lifecycle for photo/video and Yuki. Check long-list scroll and update latency for lists. Measure cold/warm entry and packaged size across the whole application. Track handset-only checks separately.
