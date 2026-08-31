package com.twohearts.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.padding
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
import com.twohearts.app.ui.screens.notes.NoteDetail
import com.twohearts.app.ui.screens.notes.NoteCategory
import com.twohearts.app.ui.screens.memories.MemoriesHome
import com.twohearts.app.ui.screens.memories.AddMemory
import com.twohearts.app.ui.screens.memories.MemoryDetail
import com.twohearts.app.ui.screens.timeline.TimelineHome
import com.twohearts.app.ui.screens.timeline.AddEvent
import com.twohearts.app.ui.screens.timeline.EventDetail
import com.twohearts.app.ui.screens.reminders.RemindersHome
import com.twohearts.app.ui.screens.reminders.CreateReminder
import com.twohearts.app.ui.screens.reminders.ReminderDetail
import com.twohearts.app.ui.screens.places.PlacesHome
import com.twohearts.app.ui.screens.places.CreatePlace
import com.twohearts.app.ui.screens.places.PlaceDetail
import com.twohearts.app.ui.screens.mood.MoodHome
import com.twohearts.app.ui.screens.mood.MoodEntryScreen
import com.twohearts.app.ui.screens.mood.MoodHistory
import com.twohearts.app.ui.screens.period.PeriodHome
import com.twohearts.app.ui.screens.period.LogPeriod
import com.twohearts.app.ui.screens.period.PeriodCalendarScreen
import com.twohearts.app.ui.screens.period.PeriodHistoryScreen
import com.twohearts.app.ui.screens.period.PeriodSettingsScreen
import com.twohearts.app.ui.screens.importantdates.ImportantDatesScreen
import com.twohearts.app.ui.screens.vault.*
import com.twohearts.app.ui.screens.security.*
import com.twohearts.app.ui.screens.settings.SettingsHomeScreen
import com.twohearts.app.ui.screens.settings.AppearanceSettingsScreen
import com.twohearts.app.ui.screens.settings.NotificationSettingsScreen
import com.twohearts.app.ui.screens.settings.StorageSettingsScreen
import com.twohearts.app.ui.screens.settings.ImportScreen
import com.twohearts.app.ui.screens.settings.ProfileSettingsScreen
import com.twohearts.app.ui.screens.settings.RelationshipSettingsScreen
import com.twohearts.app.ui.screens.search.SearchScreen
import com.twohearts.app.ui.screens.notifications.NotificationCenterScreen
import com.twohearts.app.ui.screens.about.AboutScreen
import com.twohearts.app.ui.screens.yuki.YukiScreen
import com.twohearts.app.ui.screens.yuki.YukiViewModel
import com.twohearts.app.services.datamanagement.DataManagementService
import com.twohearts.app.services.search.SearchEngine
import com.twohearts.app.data.repository.NotificationCenterRepository
import com.twohearts.app.data.settings.SettingsStorage
import androidx.lifecycle.viewmodel.compose.viewModel
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
    importantDateRepository: ImportantDateRepository,
    dataManagementService: DataManagementService,
    searchEngine: SearchEngine,
    notificationCenterRepository: NotificationCenterRepository,
    settingsStorage: SettingsStorage
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
                    NotificationCenterScreen(
                        notificationCenterRepository = notificationCenterRepository,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                // Search
                composable(RoutePath.APP_SEARCH) {
                    SearchScreen(
                        searchEngine = searchEngine,
                        noteRepository = noteRepository,
                        memoryRepository = memoryRepository,
                        reminderRepository = reminderRepository,
                        placeRepository = placeRepository,
                        timelineEventRepository = timelineEventRepository,
                        onResultClick = { type, id ->
                            when (type) {
                                "note" -> navController.navigate("/app/notes/$id")
                                "memory" -> navController.navigate("/app/memories/$id")
                                "reminder" -> navController.navigate("/app/reminders/$id")
                                "place" -> navController.navigate("/app/places/$id")
                                "timeline" -> navController.navigate("/app/timeline/$id")
                            }
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
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

                composable("/app/notes/{noteId}") { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
                    var note by remember { mutableStateOf<com.twohearts.app.data.entity.Note?>(null) }
                    LaunchedEffect(noteId) {
                        note = noteRepository.getById(noteId)
                    }
                    note?.let { n ->
                        NoteDetail(
                            note = n,
                            onEdit = {
                                navController.navigate("/app/notes/$noteId/edit")
                            },
                            onDelete = {
                                kotlinx.coroutines.runBlocking {
                                    noteRepository.softDelete(n.id)
                                }
                                navController.popBackStack()
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    } ?: Text("Loading...")
                }

                composable("/app/notes/{noteId}/edit") { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
                    var note by remember { mutableStateOf<com.twohearts.app.data.entity.Note?>(null) }
                    LaunchedEffect(noteId) {
                        note = noteRepository.getById(noteId)
                    }
                    note?.let { n ->
                        NoteEditor(
                            noteId = n.id,
                            initialTitle = n.title,
                            initialContent = n.content,
                            initialCategory = NoteCategory.fromString(n.category),
                            onSave = { title, content, category ->
                                kotlinx.coroutines.runBlocking {
                                    noteRepository.update(n.copy(
                                        title = title,
                                        content = content,
                                        category = category.name.lowercase(),
                                        updatedAt = DateTimeHelper.nowUtc()
                                    ))
                                }
                                navController.popBackStack()
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    } ?: Text("Loading...")
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

                composable("/app/memories/{memoryId}") { backStackEntry ->
                    val memoryId = backStackEntry.arguments?.getString("memoryId") ?: return@composable
                    var memory by remember { mutableStateOf<com.twohearts.app.data.entity.Memory?>(null) }
                    LaunchedEffect(memoryId) {
                        memory = memoryRepository.getById(memoryId)
                    }
                    memory?.let { m ->
                        MemoryDetail(
                            memory = m,
                            onEdit = {
                                navController.navigate("/app/memories/$memoryId/edit")
                            },
                            onDelete = {
                                kotlinx.coroutines.runBlocking {
                                    memoryRepository.softDelete(m.id)
                                }
                                navController.popBackStack()
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    } ?: Text("Loading...")
                }

                composable("/app/memories/{memoryId}/edit") { backStackEntry ->
                    val memoryId = backStackEntry.arguments?.getString("memoryId") ?: return@composable
                    var memory by remember { mutableStateOf<com.twohearts.app.data.entity.Memory?>(null) }
                    LaunchedEffect(memoryId) {
                        memory = memoryRepository.getById(memoryId)
                    }
                    memory?.let { m ->
                        AddMemory(
                            memoryId = m.id,
                            initialTitle = m.title,
                            initialCaption = m.caption ?: "",
                            initialDate = m.memoryDate,
                            onSave = { title, caption, memoryDate ->
                                kotlinx.coroutines.runBlocking {
                                    memoryRepository.update(m.copy(
                                        title = title,
                                        caption = caption,
                                        memoryDate = memoryDate,
                                        updatedAt = DateTimeHelper.nowUtc()
                                    ))
                                }
                                navController.popBackStack()
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    } ?: Text("Loading...")
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

                composable("/app/timeline/{eventId}") { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
                    var event by remember { mutableStateOf<com.twohearts.app.data.entity.TimelineEvent?>(null) }
                    LaunchedEffect(eventId) {
                        event = timelineEventRepository.getById(eventId)
                    }
                    event?.let { e ->
                        EventDetail(
                            event = e,
                            onEdit = {
                                navController.navigate("/app/timeline/$eventId/edit")
                            },
                            onDelete = {
                                kotlinx.coroutines.runBlocking {
                                    timelineEventRepository.softDelete(e.id)
                                }
                                navController.popBackStack()
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    } ?: Text("Loading...")
                }

                composable("/app/timeline/{eventId}/edit") { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
                    var event by remember { mutableStateOf<com.twohearts.app.data.entity.TimelineEvent?>(null) }
                    LaunchedEffect(eventId) {
                        event = timelineEventRepository.getById(eventId)
                    }
                    event?.let { e ->
                        AddEvent(
                            eventId = e.id,
                            initialTitle = e.title,
                            initialDescription = e.description ?: "",
                            initialDate = e.eventDate,
                            onSave = { title, eventDate, description ->
                                kotlinx.coroutines.runBlocking {
                                    timelineEventRepository.update(e.copy(
                                        title = title,
                                        eventDate = eventDate,
                                        description = description,
                                        updatedAt = DateTimeHelper.nowUtc()
                                    ))
                                }
                                navController.popBackStack()
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    } ?: Text("Loading...")
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

                composable("/app/reminders/{reminderId}") { backStackEntry ->
                    val reminderId = backStackEntry.arguments?.getString("reminderId") ?: return@composable
                    var reminder by remember { mutableStateOf<com.twohearts.app.data.entity.Reminder?>(null) }
                    LaunchedEffect(reminderId) {
                        reminder = reminderRepository.getById(reminderId)
                    }
                    reminder?.let { r ->
                        ReminderDetail(
                            reminder = r,
                            onEdit = {
                                navController.navigate("/app/reminders/$reminderId/edit")
                            },
                            onDelete = {
                                kotlinx.coroutines.runBlocking {
                                    reminderRepository.softDelete(r.id)
                                }
                                navController.popBackStack()
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    } ?: Text("Loading...")
                }

                composable("/app/reminders/{reminderId}/edit") { backStackEntry ->
                    val reminderId = backStackEntry.arguments?.getString("reminderId") ?: return@composable
                    var reminder by remember { mutableStateOf<com.twohearts.app.data.entity.Reminder?>(null) }
                    LaunchedEffect(reminderId) {
                        reminder = reminderRepository.getById(reminderId)
                    }
                    reminder?.let { r ->
                        CreateReminder(
                            reminderId = r.id,
                            initialTitle = r.title,
                            initialDescription = r.description ?: "",
                            initialDate = r.scheduledDate ?: "",
                            initialTime = r.scheduledTime ?: "",
                            initialRecurrence = r.recurrence ?: "none",
                            onSave = { title, description, scheduledDate, scheduledTime, recurrence ->
                                kotlinx.coroutines.runBlocking {
                                    reminderRepository.update(r.copy(
                                        title = title,
                                        description = description,
                                        scheduledDate = scheduledDate,
                                        scheduledTime = scheduledTime,
                                        recurrence = recurrence,
                                        updatedAt = DateTimeHelper.nowUtc()
                                    ))
                                }
                                navController.popBackStack()
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    } ?: Text("Loading...")
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

                composable("/app/places/{placeId}") { backStackEntry ->
                    val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
                    var place by remember { mutableStateOf<com.twohearts.app.data.entity.Place?>(null) }
                    LaunchedEffect(placeId) {
                        place = placeRepository.getById(placeId)
                    }
                    place?.let { p ->
                        PlaceDetail(
                            place = p,
                            onEdit = {
                                navController.navigate("/app/places/$placeId/edit")
                            },
                            onDelete = {
                                kotlinx.coroutines.runBlocking {
                                    placeRepository.softDelete(p.id)
                                }
                                navController.popBackStack()
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    } ?: Text("Loading...")
                }

                composable("/app/places/{placeId}/edit") { backStackEntry ->
                    val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
                    var place by remember { mutableStateOf<com.twohearts.app.data.entity.Place?>(null) }
                    LaunchedEffect(placeId) {
                        place = placeRepository.getById(placeId)
                    }
                    place?.let { p ->
                        CreatePlace(
                            placeId = p.id,
                            initialName = p.name,
                            initialAddress = p.address ?: "",
                            initialCity = p.city ?: "",
                            initialNotes = p.notes ?: "",
                            initialCategory = p.category ?: "",
                            onSave = { name, address, city, notes, category ->
                                kotlinx.coroutines.runBlocking {
                                    placeRepository.update(p.copy(
                                        name = name,
                                        address = address,
                                        city = city,
                                        notes = notes,
                                        category = category,
                                        updatedAt = DateTimeHelper.nowUtc()
                                    ))
                                }
                                navController.popBackStack()
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    } ?: Text("Loading...")
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

                composable("/app/mood/{entryId}/edit") { backStackEntry ->
                    val entryId = backStackEntry.arguments?.getString("entryId") ?: return@composable
                    var entry by remember { mutableStateOf<com.twohearts.app.data.entity.MoodEntry?>(null) }
                    LaunchedEffect(entryId) {
                        entry = moodEntryRepository.getById(entryId)
                    }
                    entry?.let { e ->
                        MoodEntryScreen(
                            initialMood = e.moodValue,
                            initialNote = e.note ?: "",
                            onSave = { moodValue, moodEmoji, note ->
                                kotlinx.coroutines.runBlocking {
                                    moodEntryRepository.update(e.copy(
                                        moodValue = moodValue,
                                        moodEmoji = moodEmoji,
                                        note = note,
                                        updatedAt = DateTimeHelper.nowUtc()
                                    ))
                                }
                                navController.popBackStack()
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    } ?: Text("Loading...")
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
                    val entries by periodEntryRepository.observeAll().collectAsState(initial = emptyList())
                    PeriodCalendarScreen(
                        entries = entries,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_PERIOD_HISTORY) {
                    val entries by periodEntryRepository.observeAll().collectAsState(initial = emptyList())
                    PeriodHistoryScreen(
                        entries = entries,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_PERIOD_SETTINGS) {
                    PeriodSettingsScreen(
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable("/app/period/{entryId}/edit") { backStackEntry ->
                    val entryId = backStackEntry.arguments?.getString("entryId") ?: return@composable
                    var entry by remember { mutableStateOf<com.twohearts.app.data.entity.PeriodEntry?>(null) }
                    LaunchedEffect(entryId) {
                        entry = periodEntryRepository.getById(entryId)
                    }
                    entry?.let { e ->
                        LogPeriod(
                            entryId = e.id,
                            initialStartDate = e.startDate,
                            initialEndDate = e.endDate ?: "",
                            initialFlowLevel = e.flowLevel,
                            initialNote = e.note ?: "",
                            onSave = { startDate, endDate, flowLevel, note ->
                                kotlinx.coroutines.runBlocking {
                                    periodEntryRepository.update(e.copy(
                                        startDate = startDate,
                                        endDate = endDate,
                                        flowLevel = flowLevel,
                                        note = note,
                                        updatedAt = DateTimeHelper.nowUtc()
                                    ))
                                }
                                navController.popBackStack()
                            },
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    } ?: Text("Loading...")
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
                    val yukiViewModel: YukiViewModel = viewModel()
                    YukiScreen(
                        viewModel = yukiViewModel,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                // Games — archived (Stage 12).
                // Legacy games are preserved in Archive/Legacy-React-Vite-Capacitor/
                // Yuki replaced games as the primary engagement feature.
                // All game routes redirect to Yuki companion.
                composable(RoutePath.APP_GAMES) {
                    LaunchedEffect(Unit) {
                        navController.navigate(RoutePath.APP_YUKI) {
                            popUpTo(RoutePath.APP_HOME) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                }

                composable(RoutePath.APP_GAMES_MEMORY_MATCH) {
                    LaunchedEffect(Unit) {
                        navController.navigate(RoutePath.APP_YUKI) {
                            popUpTo(RoutePath.APP_HOME) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                }

                composable(RoutePath.APP_GAMES_WORD_SCRAMBLE) {
                    LaunchedEffect(Unit) {
                        navController.navigate(RoutePath.APP_YUKI) {
                            popUpTo(RoutePath.APP_HOME) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                }

                composable(RoutePath.APP_GAMES_COUPLE_TRIVIA) {
                    LaunchedEffect(Unit) {
                        navController.navigate(RoutePath.APP_YUKI) {
                            popUpTo(RoutePath.APP_HOME) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                }

                composable(RoutePath.APP_GAMES_WHO_KNOWS) {
                    LaunchedEffect(Unit) {
                        navController.navigate(RoutePath.APP_YUKI) {
                            popUpTo(RoutePath.APP_HOME) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                }

                composable(RoutePath.APP_GAMES_WOULD_YOU_RATHER) {
                    LaunchedEffect(Unit) {
                        navController.navigate(RoutePath.APP_YUKI) {
                            popUpTo(RoutePath.APP_HOME) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                }

                composable(RoutePath.APP_GAMES_THIS_OR_THAT) {
                    LaunchedEffect(Unit) {
                        navController.navigate(RoutePath.APP_YUKI) {
                            popUpTo(RoutePath.APP_HOME) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                }

                composable(RoutePath.APP_GAMES_GUESS_MY_ANSWER) {
                    LaunchedEffect(Unit) {
                        navController.navigate(RoutePath.APP_YUKI) {
                            popUpTo(RoutePath.APP_HOME) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                }

                composable(RoutePath.APP_GAMES_CASUAL_TRIVIA) {
                    LaunchedEffect(Unit) {
                        navController.navigate(RoutePath.APP_YUKI) {
                            popUpTo(RoutePath.APP_HOME) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                }

                composable(RoutePath.APP_GAMES_RIDDLE_ROOM) {
                    LaunchedEffect(Unit) {
                        navController.navigate(RoutePath.APP_YUKI) {
                            popUpTo(RoutePath.APP_HOME) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                }

                composable(RoutePath.APP_GAMES_RESULTS) {
                    LaunchedEffect(Unit) {
                        navController.navigate(RoutePath.APP_YUKI) {
                            popUpTo(RoutePath.APP_HOME) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                }

                // Settings
                composable(RoutePath.APP_SETTINGS) {
                    SettingsHomeScreen(
                        onNavigate = { route ->
                            navController.navigate(route)
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_SETTINGS_PROFILE) {
                    ProfileSettingsScreen(
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_SETTINGS_RELATIONSHIP) {
                    RelationshipSettingsScreen(
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_SETTINGS_APPEARANCE) {
                    AppearanceSettingsScreen(
                        appStateService = appStateService,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_SETTINGS_NOTIFICATIONS) {
                    NotificationSettingsScreen(
                        settingsStorage = settingsStorage,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
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
                    StorageSettingsScreen(
                        dataManagementService = dataManagementService,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_SETTINGS_IMPORT) {
                    ImportScreen(
                        noteRepository = noteRepository,
                        reminderRepository = reminderRepository,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(RoutePath.APP_ABOUT) {
                    AboutScreen(
                        onBack = {
                            navController.popBackStack()
                        }
                    )
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
