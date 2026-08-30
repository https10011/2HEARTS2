package com.twohearts.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.twohearts.app.ui.onboarding.OnboardingFlow
import com.twohearts.app.ui.onboarding.OnboardingGate
import com.twohearts.app.ui.screens.home.HomeScreen
import com.twohearts.app.ui.screens.us.UsScreen
import com.twohearts.app.ui.screens.more.MoreScreen
import com.twohearts.app.ui.screens.notes.NotesHome
import com.twohearts.app.ui.screens.notes.NoteEditor
import com.twohearts.app.ui.screens.memories.MemoriesHome
import com.twohearts.app.ui.screens.memories.AddMemory
import com.twohearts.app.ui.screens.timeline.TimelineHome
import com.twohearts.app.ui.screens.timeline.AddEvent
import com.twohearts.app.ui.screens.reminders.RemindersHome
import com.twohearts.app.ui.screens.reminders.CreateReminder
import com.twohearts.app.ui.screens.places.PlacesHome
import com.twohearts.app.ui.screens.places.CreatePlace
import com.twohearts.app.ui.screens.mood.MoodHome
import com.twohearts.app.ui.screens.mood.MoodEntryScreen
import com.twohearts.app.ui.screens.mood.MoodHistory
import com.twohearts.app.ui.screens.period.PeriodHome
import com.twohearts.app.ui.screens.period.LogPeriod
import com.twohearts.app.ui.screens.importantdates.ImportantDatesScreen
import com.twohearts.app.ui.screens.vault.*
import com.twohearts.app.ui.screens.security.*
import com.twohearts.app.services.appstate.AppStateService
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.services.security.AppLockService
import com.twohearts.app.data.repository.NoteRepository
import com.twohearts.app.data.repository.MemoryRepository
import com.twohearts.app.data.repository.TimelineEventRepository
import com.twohearts.app.data.repository.ReminderRepository
import com.twohearts.app.data.repository.PlaceRepository
import com.twohearts.app.data.repository.MoodEntryRepository
import com.twohearts.app.data.repository.PeriodEntryRepository
import com.twohearts.app.data.repository.ImportantDateRepository
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.data.repository.generateId

/**
 * AppRouter — main application router.
 *
 * matches legacy AppRouter with:
 * - All routes defined
 * - Onboarding gate
 * - App shell
 * - Route transitions (fade + rise)
 */
@Composable
fun AppRouter(
    appStateService: AppStateService,
    relationshipService: RelationshipService,
    appLockService: AppLockService,
    noteRepository: NoteRepository,
    memoryRepository: MemoryRepository,
    timelineEventRepository: TimelineEventRepository,
    reminderRepository: ReminderRepository,
    placeRepository: PlaceRepository,
    moodEntryRepository: MoodEntryRepository,
    periodEntryRepository: PeriodEntryRepository,
    importantDateRepository: ImportantDateRepository
) {
    val navController = rememberNavController()
    val currentRoute by navController.currentBackStackEntryAsState()

    // Determine current route
    val currentRoutePath = currentRoute?.destination?.route ?: RoutePath.APP_HOME

    // Check if onboarding is complete
    val isOnboarded by appStateService.isOnboarded.collectAsState()
    val onboardingStage by appStateService.onboardingStage.collectAsState()

    // Show onboarding or app
    if (!isOnboarded || onboardingStage != "complete") {
        OnboardingFlow(
            appStateService = appStateService,
            relationshipService = relationshipService,
            appLockService = appLockService,
            onComplete = {
                // Navigate to home after onboarding
                navController.navigate(RoutePath.APP_HOME) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    } else {
        // App shell with navigation
        AppShell(
            currentRoute = currentRoutePath,
            onNavigate = { route ->
                navController.navigate(route) {
                    // Pop up to start destination to avoid building up large back stack
                    popUpTo(RoutePath.APP_HOME) {
                        saveState = true
                    }
                    // Avoid multiple copies of the same destination
                    launchSingleTop = true
                    // Restore state when re-selecting a tab
                    restoreState = true
                }
            },
            onBack = {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = RoutePath.APP_HOME,
                modifier = Modifier.padding(paddingValues),
                enterTransition = {
                    fadeIn(animationSpec = tween(300)) + slideInVertically(
                        initialOffsetY = { it / 20 },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(300)) + slideOutVertically(
                        targetOffsetY = { it / 20 },
                        animationSpec = tween(300)
                    )
                }
            ) {
                // Home
                composable(RoutePath.APP_HOME) {
                    HomeScreen(
                        relationshipService = relationshipService,
                        onNavigate = { route ->
                            navController.navigate(route)
                        }
                    )
                }

                // Us / Relationship Hub
                composable(RoutePath.APP_US) {
                    UsScreen(
                        relationshipService = relationshipService,
                        onNavigate = { route ->
                            navController.navigate(route)
                        }
                    )
                }

                // More
                composable(RoutePath.APP_MORE) {
                    MoreScreen(
                        onNavigate = { route ->
                            navController.navigate(route)
                        }
                    )
                }

                // Notifications
                composable(RoutePath.APP_NOTIFICATIONS) {
                    Text("Notifications Screen")
                }

                // Search
                composable(RoutePath.APP_SEARCH) {
                    Text("Search Screen")
                }

                // Notes
                composable(RoutePath.APP_NOTES) {
                    val notes by noteRepository.observeAll().collectAsState(initial = emptyList())
                    NotesHome(
                        notes = notes,
                        onNoteClick = { noteId ->
                            navController.navigate("/app/notes/$noteId")
                        },
                        onAddNote = {
                            navController.navigate(RoutePath.APP_NOTES_ADD)
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_NOTES_ADD) {
                    NoteEditor(
                        onSave = { title, content, category ->
                            kotlinx.coroutines.runBlocking {
                                noteRepository.create(
                                    com.twohearts.app.data.entity.Note(
                                        title = title,
                                        content = content,
                                        category = category.name.lowercase(),
                                        id = generateId(),
                                        createdAt = DateTimeHelper.nowUtc(),
                                        updatedAt = DateTimeHelper.nowUtc()
                                    )
                                )
                            }
                            navController.popBackStack()
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_NOTES_DETAIL) {
                    Text("Note Detail Screen")
                }

                composable(RoutePath.APP_NOTES_EDIT) {
                    Text("Edit Note Screen")
                }

                // Memories
                composable(RoutePath.APP_MEMORIES) {
                    val memories by memoryRepository.observeAll().collectAsState(initial = emptyList())
                    MemoriesHome(
                        memories = memories,
                        onMemoryClick = { memoryId ->
                            navController.navigate("/app/memories/$memoryId")
                        },
                        onAddMemory = {
                            navController.navigate(RoutePath.APP_MEMORIES_ADD)
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_MEMORIES_ADD) {
                    AddMemory(
                        onSave = { title, caption, memoryDate ->
                            kotlinx.coroutines.runBlocking {
                                memoryRepository.create(
                                    com.twohearts.app.data.entity.Memory(
                                        title = title,
                                        caption = caption,
                                        memoryDate = memoryDate,
                                        id = generateId(),
                                        createdAt = DateTimeHelper.nowUtc(),
                                        updatedAt = DateTimeHelper.nowUtc()
                                    )
                                )
                            }
                            navController.popBackStack()
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_MEMORIES_DETAIL) {
                    Text("Memory Detail Screen")
                }

                composable(RoutePath.APP_MEMORIES_EDIT) {
                    Text("Edit Memory Screen")
                }

                // Timeline
                composable(RoutePath.APP_TIMELINE) {
                    val events by timelineEventRepository.observeAll().collectAsState(initial = emptyList())
                    TimelineHome(
                        events = events,
                        onEventClick = { eventId ->
                            navController.navigate("/app/timeline/$eventId")
                        },
                        onAddEvent = {
                            navController.navigate(RoutePath.APP_TIMELINE_ADD)
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_TIMELINE_ADD) {
                    AddEvent(
                        onSave = { title, eventDate, description ->
                            kotlinx.coroutines.runBlocking {
                                timelineEventRepository.create(
                                    com.twohearts.app.data.entity.TimelineEvent(
                                        title = title,
                                        eventDate = eventDate,
                                        description = description,
                                        id = generateId(),
                                        createdAt = DateTimeHelper.nowUtc(),
                                        updatedAt = DateTimeHelper.nowUtc()
                                    )
                                )
                            }
                            navController.popBackStack()
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_TIMELINE_DETAIL) {
                    Text("Event Detail Screen")
                }

                composable(RoutePath.APP_TIMELINE_EDIT) {
                    Text("Edit Event Screen")
                }

                // Reminders
                composable(RoutePath.APP_REMINDERS) {
                    val reminders by reminderRepository.observeAll().collectAsState(initial = emptyList())
                    RemindersHome(
                        reminders = reminders,
                        onReminderClick = { reminderId ->
                            navController.navigate("/app/reminders/$reminderId")
                        },
                        onAddReminder = {
                            navController.navigate(RoutePath.APP_REMINDERS_ADD)
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_REMINDERS_ADD) {
                    CreateReminder(
                        onSave = { title, description, scheduledDate, scheduledTime, recurrence ->
                            kotlinx.coroutines.runBlocking {
                                reminderRepository.create(
                                    com.twohearts.app.data.entity.Reminder(
                                        title = title,
                                        description = description,
                                        scheduledDate = scheduledDate,
                                        scheduledTime = scheduledTime,
                                        recurrence = recurrence,
                                        id = generateId(),
                                        createdAt = DateTimeHelper.nowUtc(),
                                        updatedAt = DateTimeHelper.nowUtc()
                                    )
                                )
                            }
                            navController.popBackStack()
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_REMINDERS_DETAIL) {
                    Text("Reminder Detail Screen")
                }

                composable(RoutePath.APP_REMINDERS_EDIT) {
                    Text("Edit Reminder Screen")
                }

                // Places
                composable(RoutePath.APP_PLACES) {
                    val places by placeRepository.observeAll().collectAsState(initial = emptyList())
                    PlacesHome(
                        places = places,
                        onPlaceClick = { placeId ->
                            navController.navigate("/app/places/$placeId")
                        },
                        onAddPlace = {
                            navController.navigate(RoutePath.APP_PLACES_ADD)
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_PLACES_ADD) {
                    CreatePlace(
                        onSave = { name, address, city, notes, category ->
                            kotlinx.coroutines.runBlocking {
                                placeRepository.create(
                                    com.twohearts.app.data.entity.Place(
                                        name = name,
                                        address = address,
                                        city = city,
                                        notes = notes,
                                        category = category,
                                        id = generateId(),
                                        createdAt = DateTimeHelper.nowUtc(),
                                        updatedAt = DateTimeHelper.nowUtc()
                                    )
                                )
                            }
                            navController.popBackStack()
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_PLACES_DETAIL) {
                    Text("Place Detail Screen")
                }

                composable(RoutePath.APP_PLACES_EDIT) {
                    Text("Edit Place Screen")
                }

                // Mood
                composable(RoutePath.APP_MOOD) {
                    val moods by moodEntryRepository.observeAll().collectAsState(initial = emptyList())
                    val todayMood = moods.find { it.entryDate == DateTimeHelper.todayLocal() }
                    MoodHome(
                        todayMood = todayMood,
                        recentMoods = moods.take(7),
                        onAddMood = {
                            navController.navigate(RoutePath.APP_MOOD_ADD)
                        },
                        onViewHistory = {
                            navController.navigate(RoutePath.APP_MOOD_HISTORY)
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_MOOD_ADD) {
                    MoodEntryScreen(
                        onSave = { moodValue, moodEmoji, note ->
                            kotlinx.coroutines.runBlocking {
                                moodEntryRepository.create(
                                    com.twohearts.app.data.entity.MoodEntry(
                                        moodValue = moodValue,
                                        moodEmoji = moodEmoji,
                                        note = note,
                                        profileId = "owner", // Placeholder
                                        entryDate = DateTimeHelper.todayLocal(),
                                        id = generateId(),
                                        createdAt = DateTimeHelper.nowUtc(),
                                        updatedAt = DateTimeHelper.nowUtc()
                                    )
                                )
                            }
                            navController.popBackStack()
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_MOOD_HISTORY) {
                    val moods by moodEntryRepository.observeAll().collectAsState(initial = emptyList())
                    MoodHistory(
                        moods = moods,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_MOOD_EDIT) {
                    Text("Edit Mood Screen")
                }

                // Period
                composable(RoutePath.APP_PERIOD) {
                    val entries by periodEntryRepository.observeAll().collectAsState(initial = emptyList())
                    val latestEntry = entries.firstOrNull()
                    PeriodHome(
                        latestEntry = latestEntry,
                        onLogPeriod = {
                            navController.navigate(RoutePath.APP_PERIOD_LOG)
                        },
                        onViewCalendar = {
                            navController.navigate(RoutePath.APP_PERIOD_CALENDAR)
                        },
                        onViewHistory = {
                            navController.navigate(RoutePath.APP_PERIOD_HISTORY)
                        },
                        onSettings = {
                            navController.navigate(RoutePath.APP_PERIOD_SETTINGS)
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_PERIOD_LOG) {
                    LogPeriod(
                        onSave = { startDate, endDate, flowLevel, note ->
                            kotlinx.coroutines.runBlocking {
                                periodEntryRepository.create(
                                    com.twohearts.app.data.entity.PeriodEntry(
                                        startDate = startDate,
                                        endDate = endDate,
                                        flowLevel = flowLevel,
                                        note = note,
                                        profileId = "owner", // Placeholder
                                        id = generateId(),
                                        createdAt = DateTimeHelper.nowUtc(),
                                        updatedAt = DateTimeHelper.nowUtc()
                                    )
                                )
                            }
                            navController.popBackStack()
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_PERIOD_CALENDAR) {
                    Text("Period Calendar Screen")
                }

                composable(RoutePath.APP_PERIOD_HISTORY) {
                    Text("Period History Screen")
                }

                composable(RoutePath.APP_PERIOD_SETTINGS) {
                    Text("Period Settings Screen")
                }

                composable(RoutePath.APP_PERIOD_LOG_EDIT) {
                    Text("Edit Period Screen")
                }

                // Vault
                composable(RoutePath.APP_VAULT) {
                    val vaultItems by remember { mutableStateOf(emptyList<com.twohearts.app.data.entity.VaultItem>()) }
                    val isVaultLocked = appLockService.isLocked()
                    
                    VaultEntryRoute(
                        isVaultLocked = isVaultLocked,
                        vaultItems = vaultItems,
                        onPinEntered = { pin ->
                            appLockService.verifyPin(pin)
                        },
                        onPinError = { error ->
                            // Show error toast
                        },
                        onItemClicked = { item ->
                            navController.navigate("/app/vault/${item.id}")
                        },
                        onAddClicked = {
                            navController.navigate(RoutePath.APP_VAULT_ADD)
                        }
                    )
                }

                composable(RoutePath.APP_VAULT_ADD) {
                    AddVaultContent(
                        onBack = {
                            navController.popBackStack()
                        },
                        onContentAdded = { title, contentType, contentUri ->
                            // Will be implemented with repository
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_VAULT_CONTENT) {
                    // Placeholder - will be implemented with item loading
                    Text("Vault Content Viewer")
                }

                // Yuki
                composable(RoutePath.APP_YUKI) {
                    Text("Yuki Screen")
                }

                // Games
                composable(RoutePath.APP_GAMES) {
                    Text("Games Screen")
                }

                composable(RoutePath.APP_GAMES_MEMORY_MATCH) {
                    Text("Memory Match Screen")
                }

                composable(RoutePath.APP_GAMES_WORD_SCRAMBLE) {
                    Text("Word Scramble Screen")
                }

                composable(RoutePath.APP_GAMES_COUPLE_TRIVIA) {
                    Text("Couple Trivia Screen")
                }

                composable(RoutePath.APP_GAMES_WHO_KNOWS) {
                    Text("Who Knows Screen")
                }

                composable(RoutePath.APP_GAMES_WOULD_YOU_RATHER) {
                    Text("Would You Rather Screen")
                }

                composable(RoutePath.APP_GAMES_THIS_OR_THAT) {
                    Text("This Or That Screen")
                }

                composable(RoutePath.APP_GAMES_GUESS_MY_ANSWER) {
                    Text("Guess My Answer Screen")
                }

                composable(RoutePath.APP_GAMES_CASUAL_TRIVIA) {
                    Text("Casual Trivia Screen")
                }

                composable(RoutePath.APP_GAMES_RIDDLE_ROOM) {
                    Text("Riddle Room Screen")
                }

                composable(RoutePath.APP_GAMES_RESULTS) {
                    Text("Game Results Screen")
                }

                // Settings
                composable(RoutePath.APP_SETTINGS) {
                    Text("Settings Screen")
                }

                composable(RoutePath.APP_SETTINGS_PROFILE) {
                    Text("Profile Settings Screen")
                }

                composable(RoutePath.APP_SETTINGS_RELATIONSHIP) {
                    Text("Relationship Settings Screen")
                }

                composable(RoutePath.APP_SETTINGS_APPEARANCE) {
                    Text("Appearance Settings Screen")
                }

                composable(RoutePath.APP_SETTINGS_NOTIFICATIONS) {
                    Text("Notification Settings Screen")
                }

                composable(RoutePath.APP_SETTINGS_SECURITY) {
                    val isAppLockEnabled = appLockService.isEnabled()
                    
                    val scope = rememberCoroutineScope()
                    SecuritySettingsScreen(
                        isAppLockEnabled = isAppLockEnabled,
                        onAppLockToggle = { enabled ->
                            scope.launch {
                                if (enabled) {
                                    // Will prompt for PIN setup
                                } else {
                                    appLockService.disable()
                                }
                            }
                        },
                        onChangePin = { oldPin, newPin ->
                            scope.launch {
                                appLockService.changePin(oldPin, newPin)
                            }
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_SETTINGS_STORAGE) {
                    Text("Storage Settings Screen")
                }

                composable(RoutePath.APP_SETTINGS_IMPORT) {
                    Text("Import Screen")
                }

                composable(RoutePath.APP_ABOUT) {
                    Text("About Screen")
                }

                // Important dates
                composable(RoutePath.APP_IMPORTANT_DATES) {
                    val dates by importantDateRepository.observeAll().collectAsState(initial = emptyList())
                    ImportantDatesScreen(
                        dates = dates,
                        onAddDate = {
                            // Will be implemented
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
