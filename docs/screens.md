# TwoHearts V1 — Screen Status (77 Approved References)

Authoritative mapping between the 77 approved PNG references in
`TwoHeart UI Reference Screens/` and the implemented V1 screens/routes.
Updated at Phase 22 (final build & release).

Status legend:
- **Implemented** — a real screen wired to the local persistence layer.
- **Covered** — the reference's content is part of another implemented
  screen (no dedicated route, by design).
- **Design-only** — approved Canva reference kept for future work; NOT
  implemented in V1 (roadmap games architecture marks these optional).
  These are not V2 online features — they are simply out of the built
  V1 game catalog.

## Onboarding (01–08)

| # | Reference | Route / Screen | Status |
|---|-----------|----------------|--------|
| 01 | SplashScreen | `SplashScreen` + bootstrap `SplashView` in `main.tsx` | Implemented |
| 02 | Welcome-FirstLaunch | `WelcomeScreen` | Implemented |
| 03 | Profile-Setup | `ProfileSetupScreen` | Implemented |
| 04 | Relationship-Setup | `RelationshipSetupScreen` | Implemented |
| 05 | Personalization-Setup | `PersonalizationSetupScreen` | Implemented |
| 06 | AppLock-Setup | `AppLockSetupScreen` | Implemented |
| 07 | Setup-Complete | `SetupCompleteScreen` | Implemented |
| 08 | StorySetup-Date | start-date step of `RelationshipSetupScreen` | Covered |

## Main shell (09–15)

| # | Reference | Route / Screen | Status |
|---|-----------|----------------|--------|
| 09 | Home | `/app/home` — `HomeScreen` | Implemented |
| 10 | Us-SharedSpace | `/app/us` — `UsScreen` | Implemented |
| 11 | More | `/app/more` — `MoreScreen` | Implemented |
| 12 | MainMemories | `/app/memories` — `MemoriesHome` | Implemented |
| 13 | IndividualMemories | `/app/memories/:memoryId` — `MemoryDetail` | Implemented |
| 14 | Add-Edit-Memory | `/app/memories/add`, `/app/memories/:memoryId/edit` — `AddMemory` | Implemented |
| 15 | Relationship-Counter | counter card on `HomeScreen` and `UsScreen`; `/app/us/important-dates` — `ImportantDatesScreen` | Covered |

## Notes / Reminders / Timeline (16–22)

| # | Reference | Route / Screen | Status |
|---|-----------|----------------|--------|
| 16 | Notes | `/app/notes` — `NotesHome` | Implemented |
| 17 | Reminders | `/app/reminders` — `RemindersHome` | Implemented |
| 18 | Add-Reminder | `/app/reminders/add` — `CreateReminder` | Implemented |
| 19 | Set-Edit-Reminder | `/app/reminders/:reminderId`, `/app/reminders/:reminderId/edit` — `ReminderDetail`, `CreateReminder` | Implemented |
| 20 | Timeline | `/app/timeline` — `TimelineHome` | Implemented |
| 21 | Add-Timeline | `/app/timeline/add` — `AddEvent` | Implemented |
| 22 | Set-Edit-Timeline | `/app/timeline/:eventId`, `/app/timeline/:eventId/edit` — `EventDetail`, `AddEvent` | Implemented |

## Games (23–45)

V1 ships 10 games (6 couple + 4 casual) on the shared game engine
(`GameService`, content in `customization/games/gameContent.ts`).

| # | Reference | Route / Screen | Status |
|---|-----------|----------------|--------|
| 23 | MiniGames | `/app/games` — `GamesHubScreen` | Implemented |
| 24 | Game-Menu | game list on `GamesHubScreen` | Covered |
| 25 | WhoKnowsWhoBetter | `GamePlayScreen` (`who-knows-who-better`) | Implemented |
| 26 | GameResults | `GameResultsScreen` | Implemented |
| 27 | GameStats | session results on `GameResultsScreen` (no persistent stats in V1) | Covered |
| 28 | WouldYouRather | `GamePlayScreen` (`would-you-rather`) | Implemented |
| 29 | TwentyQuestions | — | Design-only |
| 30 | HowWellDoYouKnowEachOther | `GamePlayScreen` (`guess-my-answer`) | Implemented |
| 31 | WordScramble | `WordScrambleScreen` | Implemented |
| 32 | GuessTheWord | — | Design-only |
| 33 | RiddleMeThis | `CasualGamePlayScreen` (`riddle-room`) | Implemented |
| 34 | 2TruthsAndALie | — | Design-only |
| 35 | EmojiGuess | — | Design-only |
| 36 | WouldYouRather | `GamePlayScreen` (`would-you-rather`) | Implemented |
| 37 | ThisOrThat | `GamePlayScreen` (`this-or-that`) | Implemented |
| 38 | MemoryMatch | `MemoryMatchScreen` | Implemented |
| 39 | Hangman | — | Design-only |
| 40 | WordScramble | `WordScrambleScreen` | Implemented |
| 41 | TriviaChallenge | `CasualGamePlayScreen` (`casual-trivia`), `GamePlayScreen` (`couple-trivia`) | Implemented |
| 42 | WordSearch | — | Design-only |
| 43 | Tic-Tac-Toe | — | Design-only |
| 44 | ConnectFour | — | Design-only |
| 45 | 2048 | — | Design-only |

## Places / Mood (46–51)

| # | Reference | Route / Screen | Status |
|---|-----------|----------------|--------|
| 46 | OurPlaces | `/app/places` — `PlacesHome` | Implemented |
| 47 | Add-Place | `/app/places/add` — `CreatePlace` | Implemented |
| 48 | Edit-Place | `/app/places/:placeId`, `/app/places/:placeId/edit` — `PlaceDetail`, `CreatePlace` | Implemented |
| 49 | Mood | `/app/mood` — `MoodHome` | Implemented |
| 50 | Add-Mood | `/app/mood/add` — `MoodEntryScreen` | Implemented |
| 51 | Mood-Home-History | `/app/mood/history` — `MoodHistory` | Implemented |

## Period Tracker (52–60)

| # | Reference | Route / Screen | Status |
|---|-----------|----------------|--------|
| 52 | Period-Tracker | `/app/period` — `PeriodHome` | Implemented |
| 53 | Period-Calendar | `/app/period/calendar` — `PeriodCalendarScreen` | Implemented (Phase 22) |
| 54 | Log-Period | `/app/period/log` — `LogPeriod` | Implemented |
| 55 | CycleDetails | cycle status card on `PeriodHome` | Covered |
| 56 | CycleHistory | `/app/period/history` — `CycleHistory` | Implemented |
| 57 | PeriodTrackerSettings | `/app/period/settings` — `PeriodSettingsScreen` | Implemented (Phase 22) |
| 58 | PeriodReminders | recurring reminders via the Reminders feature | Covered |
| 59 | PeriodTrackerPrivacy | local-only privacy statement on `PeriodSettingsScreen` | Covered |
| 60 | Setup-Period-Tracker | first-use empty state on `PeriodHome` | Covered |

## Vault / Settings / Search / Notifications (61–77)

| # | Reference | Route / Screen | Status |
|---|-----------|----------------|--------|
| 61 | PrivateVault-Locked | `VaultLocked` (inside `/app/vault` `VaultEntryRoute`) | Implemented |
| 62 | VaultHome | `/app/vault` — `VaultHome` | Implemented |
| 63 | Add-Vault-Content | `/app/vault/add` — `AddVaultContent` | Implemented |
| 64 | TwoHearts-SettingsHome | `/app/settings` — `SettingsHomeScreen` | Implemented |
| 65 | ProfileSettings | `/app/settings/profile` — `ProfileSettingsScreen` | Implemented |
| 66 | Relationship-Settings | `/app/settings/relationship` — `RelationshipSettingsScreen` | Implemented |
| 67 | Appearance-Settings | `/app/settings/appearance` — `AppearanceSettingsScreen` | Implemented |
| 68 | Notifications-Settings | `/app/settings/notifications` — `NotificationSettingsScreen` | Implemented |
| 69 | Security-AppLockSettings | `/app/settings/security` — `SecuritySettingsScreen` | Implemented |
| 70 | Storage-Settings | `/app/settings/storage` — `StorageSettingsScreen` | Implemented |
| 71 | About-TwoHearts | `/app/settings/about` — `AboutScreen` | Implemented |
| 72 | SearchResults | `/app/search` — `SearchScreen` | Implemented |
| 73 | Notification-Center | `/app/notifications` — `NotificationCenter` | Implemented |
| 74 | GlobalSearch | `/app/search` — `SearchScreen` | Implemented |
| 75 | Add-Note | `/app/notes/add` — `NoteEditor` | Implemented |
| 76 | Edit-Note | `/app/notes/:noteId/edit` — `NoteEditor` | Implemented |
| 77 | VaultContentView | `/app/vault/content/:contentId` — `VaultContentViewer` | Implemented |

## Summary

- 67 references implemented as real screens.
- 10 references covered by sections of implemented screens.
- 8 game references design-only (29, 32, 34, 35, 39, 42, 43, 44, 45 — nine
  PNGs across eight unique games) — intentionally not built in V1.
- V2-deferred items (Open When, online chat, cloud sync) have no screens
  and no code in V1 by design.
