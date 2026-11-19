package com.twohearts.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.room.withTransaction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import com.twohearts.app.ui.theme.LocalTwoHeartsMotion
import com.twohearts.app.ui.components.useToast
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.twohearts.app.ui.onboarding.OnboardingFlow
import com.twohearts.app.ui.onboarding.OnboardingGate
import com.twohearts.app.ui.onboarding.shouldShowApp
import com.twohearts.app.ui.screens.home.HomeScreen
import com.twohearts.app.ui.screens.home.YukiPresence
import com.twohearts.app.ui.screens.home.yukiPresenceFor
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
import com.twohearts.app.data.repository.PeriodSettingsRepository
import com.twohearts.app.services.datetime.DateTimeHelper
import com.twohearts.app.data.repository.generateId
import com.twohearts.app.data.dao.MediaAssetDao
import com.twohearts.app.services.media.MediaStorage

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
    periodSettingsRepository: PeriodSettingsRepository,
    importantDateRepository: ImportantDateRepository,
    dataManagementService: DataManagementService,
    searchEngine: SearchEngine,
    notificationCenterRepository: NotificationCenterRepository,
    settingsStorage: SettingsStorage,
    mediaAssetDao: MediaAssetDao,
    mediaStorage: MediaStorage,
) {
    val navController = rememberNavController()
    val currentRoute by navController.currentBackStackEntryAsState()

    // Determine current route
    val currentRoutePath = currentRoute?.destination?.route ?: RoutePath.APP_HOME

    // Phase 2: transitions respect the user's reduce-motion preference.
    // Previously the durations were literal 300s, so the in-app "reduce
    // motion" setting and the OS animator scale had no effect on navigation
    // at all.
    val motion = LocalTwoHeartsMotion.current

    // Check if onboarding is complete
    val isOnboarded by appStateService.isOnboarded.collectAsState()
    val onboardingStage by appStateService.onboardingStage.collectAsState()

    // Phase 3: the shell is eligible once the commit has persisted, but the
    // completion screen still has to be shown. `acknowledged` is seeded from
    // the persisted state on first composition — so a cold start of an
    // already-complete install goes straight to the app — and is only cleared
    // again when setup becomes incomplete (a data reset).
    var onboardingAcknowledged by remember { mutableStateOf(
        shouldShowApp(isOnboarded, onboardingStage, acknowledged = true)
    ) }
    LaunchedEffect(isOnboarded, onboardingStage) {
        if (!shouldShowApp(isOnboarded, onboardingStage, acknowledged = true)) {
            onboardingAcknowledged = false
        }
    }

    // Show onboarding or app
    if (!shouldShowApp(isOnboarded, onboardingStage, onboardingAcknowledged)) {
        OnboardingFlow(
            appStateService = appStateService,
            relationshipService = relationshipService,
            appLockService = appLockService,
            settingsStorage = settingsStorage,
            onComplete = {
                // The person has seen the completion screen and tapped through
                // it. The domain and the "complete" stage were already
                // persisted by the commit, so this only drops the gate — the
                // shell's NavHost starts at Home.
                onboardingAcknowledged = true
            }
        )
    } else {
        // App shell with navigation
        //
        // Phase 2: "can we go back" is derived from the actual back stack.
        // The migrated shell could not answer that question — it kept an
        // unconditional BackHandler that no-oped at the root — which left
        // system back unable to leave the app, and let the header decide
        // independently whether to draw a back button. One source of truth
        // now feeds both.
        val canNavigateBack = navController.previousBackStackEntry != null
        AppShell(
            currentRoute = currentRoutePath,
            canNavigateBack = canNavigateBack,
            onNavigate = { route ->
                // Phase 2: a destination already showing is not re-pushed.
                // Previously selecting the current area still ran the full
                // navigate() dance (and its animation), which made the bar
                // feel unresponsive — the screen appeared to reload for no
                // reason.
                if (route != currentRoutePath) {
                    navController.navigate(route) {
                        // Pop up to the start destination rather than letting
                        // the stack grow with every area switch.
                        popUpTo(RoutePath.APP_HOME) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            onBack = {
                // Only reached while canNavigateBack is true, so this always
                // has an effect.
                navController.popBackStack()
            }
        ) { paddingValues ->
            val feedback = useToast()
            NavHost(
                navController = navController,
                startDestination = RoutePath.APP_HOME,
                modifier = Modifier.padding(paddingValues),
                enterTransition = {
                    thForward(
                        targetRoute = targetState.destination.route,
                        reduced = motion.reduced,
                    )
                },
                exitTransition = {
                    thForwardExit(
                        targetRoute = targetState.destination.route,
                        reduced = motion.reduced,
                    )
                },
                popEnterTransition = {
                    thBackEnter(
                        fromRoute = initialState.destination.route,
                        reduced = motion.reduced,
                    )
                },
                popExitTransition = {
                    thBackExit(
                        fromRoute = initialState.destination.route,
                        reduced = motion.reduced,
                    )
                }
            ) {
                // Home
                composable(RoutePath.APP_HOME) {
                    // Phase 4: Home now reads the same repositories the feature
                    // screens do, so it reflects real local state instead of a
                    // fixed set of buttons. Yuki's presence comes from the
                    // persisted companion state; it is optional because a
                    // brand-new install has no Yuki record yet, and Home must
                    // not invent one.
                    val context = LocalContext.current.applicationContext
                    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
                    val yukiPresence by produceState<YukiPresence?>(null, context, lifecycle) {
                        if (lifecycle == Lifecycle.State.RESUMED) {
                            value = withContext(Dispatchers.IO) {
                                runCatching { yukiPresenceFor(context) }.getOrNull()
                            }
                        }
                    }
                    HomeScreen(
                        relationshipService = relationshipService,
                        noteRepository = noteRepository,
                        reminderRepository = reminderRepository,
                        memoryRepository = memoryRepository,
                        moodEntryRepository = moodEntryRepository,
                        importantDateRepository = importantDateRepository,
                        onNavigate = { route ->
                            navController.navigate(route)
                        },
                        yukiPresence = yukiPresence,
                    )
                }

                // Us / Relationship Hub
                composable(RoutePath.APP_US) {
                    UsScreen(
                        relationshipService = relationshipService,
                        importantDateRepository = importantDateRepository,
                        memoryRepository = memoryRepository,
                        timelineEventRepository = timelineEventRepository,
                        placeRepository = placeRepository,
                        moodEntryRepository = moodEntryRepository,
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
                        onOpenDestination = { navController.navigate(it) },
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
                            val encodedId = android.net.Uri.encode(id)
                            when (type) {
                                "note" -> navController.navigate("${RoutePath.APP_NOTES}/$encodedId")
                                "memory" -> navController.navigate("${RoutePath.APP_MEMORIES}/$encodedId")
                                "reminder" -> navController.navigate("${RoutePath.APP_REMINDERS}/$encodedId")
                                "place" -> navController.navigate("${RoutePath.APP_PLACES}/$encodedId")
                                "timeline" -> navController.navigate("${RoutePath.APP_TIMELINE}/$encodedId")
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
                    val action = rememberRecordAction()
                    NoteEditor(
                        onSave = { title, content, category ->
                            action.run("Note saved.", "Couldn’t save this note.", afterWrite = { navController.popBackStack() }) {
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
                        },
                        onBack = {
                            if (!action.running) navController.popBackStack()
                        }
                    )
                }

                composable("/app/notes/{noteId}") { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
                    val action = rememberRecordAction()
                    val lookup = rememberRecordLookup(noteId) {
                        noteRepository.getById(noteId)
                    }
                    val note = lookup.record
                    note?.let { n ->
                        NoteDetail(
                            note = n,
                            onEdit = {
                                if (!action.running) navController.navigate("/app/notes/$noteId/edit")
                            },
                            onDelete = {
                                action.run("Note deleted.", "Couldn’t delete this note.", afterWrite = { navController.popBackStack() }) {
                                    noteRepository.softDelete(n.id)
                                }
                            },
                            onBack = {
                                if (!action.running) navController.popBackStack()
                            },
                        )
                    } ?: RecordLookupScreen("Note", lookup) { navController.popBackStack() }
                }

                composable("/app/notes/{noteId}/edit") { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
                    val action = rememberRecordAction()
                    val lookup = rememberRecordLookup(noteId) {
                        noteRepository.getById(noteId)
                    }
                    val note = lookup.record
                    note?.let { n ->
                        NoteEditor(
                            noteId = n.id,
                            initialTitle = n.title,
                            initialContent = n.content,
                            initialCategory = NoteCategory.fromString(n.category),
                            onSave = { title, content, category ->
                                action.run("Note saved.", "Couldn’t save this note.", afterWrite = { navController.popBackStack() }) {
                                    checkNotNull(noteRepository.update(n.id, mapOf(
                                        "title" to title,
                                        "content" to content,
                                        "category" to category.name.lowercase()
                                    )))
                                }
                            },
                            onBack = {
                                if (!action.running) navController.popBackStack()
                            },
                        )
                    } ?: RecordLookupScreen("Note", lookup) { navController.popBackStack() }
                }

                // Memories
                composable(RoutePath.APP_MEMORIES) {
                    val memories by remember { memoryRepository.observeAll() }.collectAsStateWithLifecycle(emptyList())
                    val attachments by remember { memoryRepository.observeAttachments() }.collectAsStateWithLifecycle(emptyList())
                    val memoryMedia = remember(attachments) { attachments.groupBy({ it.memoryId }, { it.asset }) }
                    MemoriesHome(
                        memories = memories,
                        media = memoryMedia,
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
                    val context = LocalContext.current
                    val action = rememberRecordAction()
                    AddMemory(
                        saving = action.running,
                        onSave = { title, caption, memoryDate, selectedMedia ->
                            var missingAttachment = false
                            action.run(
                                "Memory saved.", "Couldn’t save this memory.",
                                afterWrite = { navController.popBackStack() },
                                warning = { if (missingAttachment) "Memory saved. Some attachments couldn’t be added." else null },
                            ) {
                                val memoryId = generateId()
                                memoryRepository.create(com.twohearts.app.data.entity.Memory(
                                    title = title,
                                    caption = caption,
                                    memoryDate = memoryDate,
                                    id = memoryId,
                                    createdAt = DateTimeHelper.nowUtc(),
                                    updatedAt = DateTimeHelper.nowUtc()
                                ))
                                selectedMedia.forEachIndexed { index, uri ->
                                    val mime = context.contentResolver.getType(uri)
                                    val stored = mime?.let { mediaStorage.store(uri, it) }
                                    if (stored == null) {
                                        missingAttachment = true
                                        return@forEachIndexed
                                    }
                                    mediaAssetDao.insert(com.twohearts.app.data.entity.MediaAsset(stored.filePath, stored.mimeType, stored.fileSize, stored.width, stored.height, id = stored.id, createdAt = DateTimeHelper.nowUtc(), updatedAt = DateTimeHelper.nowUtc()))
                                    memoryRepository.addMedia(memoryId, stored.id, index)
                                }
                            }
                        },
                        onBack = {
                            if (!action.running) navController.popBackStack()
                        }
                    )
                }

                composable("/app/memories/{memoryId}") { backStackEntry ->
                    val memoryId = backStackEntry.arguments?.getString("memoryId") ?: return@composable
                    val action = rememberRecordAction()
                    val lookup = rememberRecordLookup(memoryId) {
                        memoryRepository.getById(memoryId)?.let { saved ->
                            saved to memoryRepository.getAssets(memoryId)
                        }
                    }
                    val memory = lookup.record?.first
                    memory?.let { m ->
                        MemoryDetail(
                            memory = m,
                            media = lookup.record?.second.orEmpty(),
                            onEdit = {
                                if (!action.running) navController.navigate("/app/memories/$memoryId/edit")
                            },
                            onDelete = {
                                action.run("Memory deleted.", "Couldn’t delete this memory.", afterWrite = { navController.popBackStack() }) {
                                    memoryRepository.softDelete(m.id)
                                }
                            },
                            onBack = {
                                if (!action.running) navController.popBackStack()
                            }
                        )
                    } ?: RecordLookupScreen("Memory", lookup) { navController.popBackStack() }
                }

                composable("/app/memories/{memoryId}/edit") { backStackEntry ->
                    val memoryId = backStackEntry.arguments?.getString("memoryId") ?: return@composable
                    val context = LocalContext.current
                    val action = rememberRecordAction()
                    val lookup = rememberRecordLookup(memoryId) {
                        memoryRepository.getById(memoryId)
                    }
                    val memory = lookup.record
                    memory?.let { m ->
                        AddMemory(
                            memoryId = m.id,
                            initialTitle = m.title,
                            initialCaption = m.caption ?: "",
                            initialDate = m.memoryDate,
                            saving = action.running,
                            onSave = { title, caption, memoryDate, selectedMedia ->
                                var missingAttachment = false
                                action.run(
                                    "Memory saved.", "Couldn’t save this memory.",
                                    afterWrite = { navController.popBackStack() },
                                    warning = { if (missingAttachment) "Memory saved. Some attachments couldn’t be added." else null },
                                ) {
                                    checkNotNull(memoryRepository.update(m.id, mapOf("title" to title, "caption" to caption, "memoryDate" to memoryDate)))
                                    val offset = memoryRepository.getMedia(m.id).size
                                    selectedMedia.forEachIndexed { index, uri ->
                                        val mime = context.contentResolver.getType(uri)
                                        val stored = mime?.let { mediaStorage.store(uri, it) }
                                        if (stored == null) {
                                            missingAttachment = true
                                            return@forEachIndexed
                                        }
                                        mediaAssetDao.insert(com.twohearts.app.data.entity.MediaAsset(stored.filePath, stored.mimeType, stored.fileSize, stored.width, stored.height, id = stored.id, createdAt = DateTimeHelper.nowUtc(), updatedAt = DateTimeHelper.nowUtc()))
                                        memoryRepository.addMedia(m.id, stored.id, offset + index)
                                    }
                                }
                            },
                            onBack = {
                                if (!action.running) navController.popBackStack()
                            }
                        )
                    } ?: RecordLookupScreen("Memory", lookup) { navController.popBackStack() }
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
                    val action = rememberRecordAction()
                    AddEvent(
                        onSave = { title, eventDate, description ->
                            action.run("Moment added to your timeline.", "Couldn’t save this moment.", afterWrite = { navController.popBackStack() }) {
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
                        },
                        onBack = {
                            if (!action.running) navController.popBackStack()
                        }
                    )
                }

                composable("/app/timeline/{eventId}") { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
                    val action = rememberRecordAction()
                    val lookup = rememberRecordLookup(eventId) {
                        timelineEventRepository.getById(eventId)
                    }
                    val event = lookup.record
                    event?.let { e ->
                        EventDetail(
                            event = e,
                            onEdit = {
                                if (!action.running) navController.navigate("/app/timeline/$eventId/edit")
                            },
                            onDelete = {
                                action.run("Moment deleted.", "Couldn’t delete this moment.", afterWrite = { navController.popBackStack() }) {
                                    timelineEventRepository.softDelete(e.id)
                                }
                            },
                            onBack = {
                                if (!action.running) navController.popBackStack()
                            }
                        )
                    } ?: RecordLookupScreen("Timeline moment", lookup) { navController.popBackStack() }
                }

                composable("/app/timeline/{eventId}/edit") { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
                    val action = rememberRecordAction()
                    val lookup = rememberRecordLookup(eventId) {
                        timelineEventRepository.getById(eventId)
                    }
                    val event = lookup.record
                    event?.let { e ->
                        AddEvent(
                            eventId = e.id,
                            initialTitle = e.title,
                            initialDescription = e.description ?: "",
                            initialDate = e.eventDate,
                            onSave = { title, eventDate, description ->
                                action.run("Moment saved.", "Couldn’t save this moment.", afterWrite = { navController.popBackStack() }) {
                                    checkNotNull(timelineEventRepository.update(e.id, mapOf(
                                        "title" to title,
                                        "eventDate" to eventDate,
                                        "description" to description
                                    )))
                                }
                            },
                            onBack = {
                                if (!action.running) navController.popBackStack()
                            }
                        )
                    } ?: RecordLookupScreen("Timeline moment", lookup) { navController.popBackStack() }
                }

                // Reminders
                composable(RoutePath.APP_REMINDERS) {
                    val reminders by reminderRepository.observeAll().collectAsState(initial = emptyList())
                    val action = rememberRecordAction()
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
                        },
                        onToggleCompleted = { action.toggleReminder(reminderRepository, it) },
                    )
                }

                composable(RoutePath.APP_REMINDERS_ADD) {
                    val action = rememberRecordAction()
                    CreateReminder(
                        onSave = { title, description, scheduledDate, scheduledTime, recurrence, notificationEnabled ->
                            action.run("Reminder saved.", "Couldn’t save this reminder.", afterWrite = { navController.popBackStack() }) {
                                reminderRepository.create(
                                    com.twohearts.app.data.entity.Reminder(
                                        title = title,
                                        description = description,
                                        scheduledDate = scheduledDate,
                                        scheduledTime = scheduledTime,
                                        recurrence = recurrence,
                                        notificationEnabled = notificationEnabled,
                                        id = generateId(),
                                        createdAt = DateTimeHelper.nowUtc(),
                                        updatedAt = DateTimeHelper.nowUtc()
                                    )
                                )
                            }
                        },
                        onBack = {
                            if (!action.running) navController.popBackStack()
                        }
                    )
                }

                composable("/app/reminders/{reminderId}") { backStackEntry ->
                    val reminderId = backStackEntry.arguments?.getString("reminderId") ?: return@composable
                    val action = rememberRecordAction()
                    val lookup = rememberRecordLookup(reminderId) {
                        reminderRepository.getById(reminderId)
                    }
                    val reminder = lookup.record
                    reminder?.let { r ->
                        ReminderDetail(
                            reminder = r,
                            onEdit = {
                                if (!action.running) navController.navigate("/app/reminders/$reminderId/edit")
                            },
                            onDelete = {
                                action.run("Reminder deleted.", "Couldn’t delete this reminder.", afterWrite = { navController.popBackStack() }) {
                                    reminderRepository.softDelete(r.id)
                                }
                            },
                            onBack = {
                                if (!action.running) navController.popBackStack()
                            },
                            onToggleCompleted = { action.toggleReminder(reminderRepository, r, lookup::refresh) },
                        )
                    } ?: RecordLookupScreen("Reminder", lookup) { navController.popBackStack() }
                }

                composable("/app/reminders/{reminderId}/edit") { backStackEntry ->
                    val reminderId = backStackEntry.arguments?.getString("reminderId") ?: return@composable
                    val action = rememberRecordAction()
                    val lookup = rememberRecordLookup(reminderId) {
                        reminderRepository.getById(reminderId)
                    }
                    val reminder = lookup.record
                    reminder?.let { r ->
                        CreateReminder(
                            reminderId = r.id,
                            initialTitle = r.title,
                            initialDescription = r.description ?: "",
                            initialDate = r.scheduledDate ?: "",
                            initialTime = r.scheduledTime ?: "",
                            initialRecurrence = r.recurrence ?: "none",
                            initialNotificationEnabled = r.notificationEnabled,
                            onSave = { title, description, scheduledDate, scheduledTime, recurrence, notificationEnabled ->
                                action.run("Reminder saved.", "Couldn’t save this reminder.", afterWrite = { navController.popBackStack() }) {
                                    checkNotNull(reminderRepository.update(r.id, mapOf(
                                        "title" to title,
                                        "description" to description,
                                        "scheduledDate" to scheduledDate,
                                        "scheduledTime" to scheduledTime,
                                        "recurrence" to recurrence,
                                        "notificationEnabled" to notificationEnabled,
                                    )))
                                }
                            },
                            onBack = {
                                if (!action.running) navController.popBackStack()
                            }
                        )
                    } ?: RecordLookupScreen("Reminder", lookup) { navController.popBackStack() }
                }

                // Places
                composable(RoutePath.APP_PLACES) {
                    val places by remember { placeRepository.observeAll() }.collectAsStateWithLifecycle(emptyList())
                    val attachments by remember { mediaAssetDao.observePlaceAttachments() }.collectAsStateWithLifecycle(emptyList())
                    val placeMedia = remember(attachments) { attachments.associate { it.placeId to it.asset } }
                    PlacesHome(
                        places = places,
                        media = placeMedia,
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
                    val context = LocalContext.current
                    val action = rememberRecordAction()
                    CreatePlace(
                        onSave = { name, address, city, notes, category, photo ->
                            action.run("Place saved.", "Couldn’t save this place or its photo.", afterWrite = { navController.popBackStack() }) {
                                val stored = photo?.let { uri ->
                                    val mime = checkNotNull(context.contentResolver.getType(uri))
                                    checkNotNull(mediaStorage.store(uri, mime))
                                }
                                stored?.let { mediaAssetDao.insert(com.twohearts.app.data.entity.MediaAsset(it.filePath, it.mimeType, it.fileSize, it.width, it.height, id = it.id, createdAt = DateTimeHelper.nowUtc(), updatedAt = DateTimeHelper.nowUtc())) }
                                placeRepository.create(com.twohearts.app.data.entity.Place(
                                    name = name,
                                    address = address,
                                    city = city,
                                    notes = notes,
                                    category = category,
                                    photoRef = stored?.id,
                                    id = generateId(),
                                    createdAt = DateTimeHelper.nowUtc(),
                                    updatedAt = DateTimeHelper.nowUtc()
                                ))
                            }
                        },
                        onBack = {
                            if (!action.running) navController.popBackStack()
                        }
                    )
                }

                composable("/app/places/{placeId}") { backStackEntry ->
                    val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
                    val action = rememberRecordAction()
                    val lookup = rememberRecordLookup(placeId) {
                        placeRepository.getById(placeId)?.let { saved ->
                            saved to saved.photoRef?.let { mediaAssetDao.getById(it) }
                        }
                    }
                    val place = lookup.record?.first
                    place?.let { p ->
                        PlaceDetail(
                            place = p,
                            media = lookup.record?.second,
                            onEdit = {
                                if (!action.running) navController.navigate("/app/places/$placeId/edit")
                            },
                            onDelete = {
                                action.run("Place deleted.", "Couldn’t delete this place.", afterWrite = { navController.popBackStack() }) {
                                    placeRepository.softDelete(p.id)
                                }
                            },
                            onBack = {
                                if (!action.running) navController.popBackStack()
                            }
                        )
                    } ?: RecordLookupScreen("Place", lookup) { navController.popBackStack() }
                }

                composable("/app/places/{placeId}/edit") { backStackEntry ->
                    val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
                    val context = LocalContext.current
                    val action = rememberRecordAction()
                    val lookup = rememberRecordLookup(placeId) {
                        placeRepository.getById(placeId)
                    }
                    val place = lookup.record
                    place?.let { p ->
                        CreatePlace(
                            placeId = p.id,
                            initialName = p.name,
                            initialAddress = p.address ?: "",
                            initialCity = p.city ?: "",
                            initialNotes = p.notes ?: "",
                            initialCategory = p.category ?: "",
                            onSave = { name, address, city, notes, category, photo ->
                                action.run("Place saved.", "Couldn’t save this place or its photo.", afterWrite = { navController.popBackStack() }) {
                                    val stored = photo?.let { uri ->
                                        val mime = checkNotNull(context.contentResolver.getType(uri))
                                        checkNotNull(mediaStorage.store(uri, mime))
                                    }
                                    stored?.let { mediaAssetDao.insert(com.twohearts.app.data.entity.MediaAsset(it.filePath, it.mimeType, it.fileSize, it.width, it.height, id = it.id, createdAt = DateTimeHelper.nowUtc(), updatedAt = DateTimeHelper.nowUtc())) }
                                    checkNotNull(placeRepository.update(p.id, mapOf(
                                        "name" to name,
                                        "address" to address,
                                        "city" to city,
                                        "notes" to notes,
                                        "category" to category,
                                        "photoRef" to (stored?.id ?: p.photoRef),
                                    )))
                                }
                            },
                            onBack = {
                                if (!action.running) navController.popBackStack()
                            }
                        )
                    } ?: RecordLookupScreen("Place", lookup) { navController.popBackStack() }
                }

                // Mood
                composable(RoutePath.APP_MOOD) {
                    val moods by moodEntryRepository.observeAll().collectAsState(initial = emptyList())
                    val owner by relationshipService.observeOwner().collectAsState(initial = null)
                    val today = DateTimeHelper.todayLocal()
                    val todayMood = moods.firstOrNull { it.entryDate == today && it.profileId == owner?.id }
                        ?: moods.firstOrNull { it.entryDate == today && it.profileId == "owner" }
                    val action = rememberRecordAction()
                    MoodHome(
                        todayMood = todayMood,
                        recentMoods = moods.take(7),
                        onAddMood = {
                            navController.navigate(todayMood?.let { "/app/mood/${android.net.Uri.encode(it.id)}/edit" }
                                ?: RoutePath.APP_MOOD_ADD)
                        },
                        onViewHistory = {
                            navController.navigate(RoutePath.APP_MOOD_HISTORY)
                        },
                        onBack = {
                            navController.popBackStack()
                        },
                        onQuickMood = { mood ->
                            action.run("Mood logged.", "Couldn’t log your mood.") {
                                val profileId = checkNotNull(relationshipService.getOwner()).id
                                moodEntryRepository.saveOwnerCheckIn(
                                    profileId, DateTimeHelper.todayLocal(), mood.value, mood.emoji, keepNote = true,
                                )
                            }
                        },
                    )
                }

                composable(RoutePath.APP_MOOD_ADD) {
                    val action = rememberRecordAction()
                    MoodEntryScreen(
                        onSave = { moodValue, moodEmoji, note ->
                            action.run("Mood logged.", "Couldn’t log your mood.", afterWrite = { navController.popBackStack() }) {
                                val profileId = checkNotNull(relationshipService.getOwner()).id
                                moodEntryRepository.saveOwnerCheckIn(
                                    profileId, DateTimeHelper.todayLocal(), moodValue, moodEmoji, note,
                                )
                            }
                        },
                        onBack = {
                            if (!action.running) navController.popBackStack()
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
                    val action = rememberRecordAction()
                    val lookup = rememberRecordLookup(entryId) {
                        moodEntryRepository.getById(entryId)
                    }
                    val entry = lookup.record
                    entry?.let { e ->
                        MoodEntryScreen(
                            initialMood = e.moodValue,
                            initialNote = e.note ?: "",
                            onSave = { moodValue, moodEmoji, note ->
                                action.run("Mood saved.", "Couldn’t save this mood.", afterWrite = { navController.popBackStack() }) {
                                    checkNotNull(moodEntryRepository.update(e.id, mapOf(
                                        "moodValue" to moodValue,
                                        "moodEmoji" to moodEmoji,
                                        "note" to note
                                    )))
                                }
                            },
                            onBack = {
                                if (!action.running) navController.popBackStack()
                            }
                        )
                    } ?: RecordLookupScreen("Mood entry", lookup) { navController.popBackStack() }
                }

                periodRoutes(navController, relationshipService, periodEntryRepository, periodSettingsRepository)

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
                    val context = LocalContext.current.applicationContext
                    val owner by remember { relationshipService.observeOwner() }
                        .collectAsStateWithLifecycle(initialValue = null)
                    val scope = rememberCoroutineScope()
                    var saving by remember { mutableStateOf(false) }
                    var error by remember { mutableStateOf<String?>(null) }
                    androidx.activity.compose.BackHandler(saving) {}
                    ProfileSettingsScreen(
                        profile = owner,
                        saving = saving,
                        error = error,
                        onBack = { if (!saving) navController.popBackStack() },
                        onSave = { name, birthday, photo ->
                            if (!saving) {
                                saving = true
                                error = null
                                scope.launch {
                                    try {
                                        val profile = checkNotNull(relationshipService.getOwner())
                                        val stored = photo?.let { uri ->
                                            withContext(Dispatchers.IO) {
                                                val mime = context.contentResolver.getType(uri)
                                                mime?.takeIf { it.startsWith("image/") }?.let { mediaStorage.store(uri, it) }
                                            } ?: error("Could not read selected photo")
                                        }
                                        val db = com.twohearts.app.data.database.TwoHeartsDatabase.getDatabase(context)
                                        db.withTransaction {
                                            if (stored != null) {
                                                val now = DateTimeHelper.nowUtc()
                                                mediaAssetDao.insert(com.twohearts.app.data.entity.MediaAsset(
                                                    filePath = stored.filePath, mimeType = stored.mimeType,
                                                    fileSize = stored.fileSize, width = stored.width, height = stored.height,
                                                    id = stored.id, createdAt = now, updatedAt = now,
                                                ))
                                            }
                                            relationshipService.saveOwnerDetails(profile.id, name, birthday, stored?.id)
                                        }
                                        feedback.success("Profile saved.")
                                        navController.popBackStack()
                                    } catch (cancelled: CancellationException) {
                                        throw cancelled
                                    } catch (_: Exception) {
                                        error = "Couldn’t save this profile. Check the selected photo and try again."
                                    } finally {
                                        saving = false
                                    }
                                }
                            }
                        },
                    )
                }

                composable(RoutePath.APP_SETTINGS_RELATIONSHIP) {
                    val context = LocalContext.current.applicationContext
                    val partner by remember { relationshipService.observePartner() }
                        .collectAsStateWithLifecycle(initialValue = null)
                    val relationship by remember { relationshipService.observeRelationship() }
                        .collectAsStateWithLifecycle(initialValue = null)
                    val scope = rememberCoroutineScope()
                    var saving by remember { mutableStateOf(false) }
                    var error by remember { mutableStateOf<String?>(null) }
                    androidx.activity.compose.BackHandler(saving) {}
                    RelationshipSettingsScreen(
                        partner = partner,
                        relationship = relationship,
                        saving = saving,
                        error = error,
                        onBack = { if (!saving) navController.popBackStack() },
                        onSave = { name, date ->
                            if (!saving) {
                                saving = true
                                error = null
                                scope.launch {
                                    try {
                                        val db = com.twohearts.app.data.database.TwoHeartsDatabase.getDatabase(context)
                                        db.withTransaction {
                                            val owner = checkNotNull(relationshipService.getOwner())
                                            val savedPartner = relationshipService.getPartner()
                                            val partnerId = if (savedPartner == null) {
                                                relationshipService.createPartner(name, null).id
                                            } else {
                                                relationshipService.updatePartner(savedPartner.id, name, null)
                                                savedPartner.id
                                            }
                                            val saved = relationshipService.getRelationship()
                                            if (saved == null) relationshipService.createRelationship(owner.id, partnerId, date)
                                            else relationshipService.updateRelationship(saved.id, date)
                                        }
                                        feedback.success("Relationship saved.")
                                        navController.popBackStack()
                                    } catch (cancelled: CancellationException) {
                                        throw cancelled
                                    } catch (_: Exception) {
                                        error = "Couldn’t save these changes. Try again."
                                    } finally {
                                        saving = false
                                    }
                                }
                            }
                        },
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
                        onSaveDate = { existing, title, date, recurrence ->
                            if (existing == null) {
                                val now = DateTimeHelper.nowUtc()
                                importantDateRepository.create(com.twohearts.app.data.entity.ImportantDate(
                                    title, date, recurrence, id = generateId(), createdAt = now, updatedAt = now,
                                ))
                            } else {
                                checkNotNull(importantDateRepository.update(
                                    existing.id, mapOf("title" to title, "date" to date, "recurrence" to recurrence),
                                ))
                            }
                        },
                        onDeleteDate = { item -> importantDateRepository.softDelete(item.id) },
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

private fun RecordAction.toggleReminder(
    repository: ReminderRepository,
    reminder: com.twohearts.app.data.entity.Reminder,
    refresh: () -> Unit = {},
) {
    val completing = reminder.status != "completed"
    run(
        if (completing) "Reminder completed." else "Reminder marked as pending.",
        "Couldn’t change this reminder.", afterWrite = refresh,
    ) {
        checkNotNull(repository.update(reminder.id, mapOf("status" to if (completing) "completed" else "pending")))
    }
}
