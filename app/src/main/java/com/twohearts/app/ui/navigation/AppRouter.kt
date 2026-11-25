package com.twohearts.app.ui.navigation

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.twohearts.app.ui.screens.mood.belongsToOwner
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
import com.twohearts.app.services.security.AppLockState
import com.twohearts.app.data.repository.VaultItemRepository
import com.twohearts.app.services.media.VaultStorage
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
import com.twohearts.app.services.media.PlaceWriter
import com.twohearts.app.data.database.TwoHeartsDatabase
import com.twohearts.app.data.entity.Memory
import com.twohearts.app.services.media.MemoryWriter
import com.twohearts.app.services.media.ProfilePhotoStaging
import com.twohearts.app.services.media.ProfilePhotoWriter

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
internal fun AppRouter(
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
    vaultRepository: VaultItemRepository,
    vaultStorage: VaultStorage,
    openAlerts: Boolean = false,
    onAlertsOpened: () -> Unit = {},
) {
    val appContext = LocalContext.current.applicationContext
    fun memoryWriter() = MemoryWriter(
        TwoHeartsDatabase.getDatabase(appContext), memoryRepository, mediaStorage, appContext.contentResolver,
    )

    fun placeWriter() = PlaceWriter(
        TwoHeartsDatabase.getDatabase(appContext), placeRepository, mediaStorage, appContext.contentResolver,
    )

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
    val lockState by appLockService.lockState.collectAsStateWithLifecycle()

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
    } else if (lockState == AppLockState.LOCKED) {
        AppLockGate(onUnlock = { pin -> withContext(Dispatchers.IO) { appLockService.unlock(pin) } })
    } else {
        LaunchedEffect(openAlerts) {
            if (openAlerts) {
                navController.openNotifications()
                onAlertsOpened()
            }
        }
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
            onNavigate = navController::selectPrimaryRoute,
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
                        onOpenSettings = { navController.navigate(RoutePath.APP_SETTINGS_NOTIFICATIONS) },
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
                        onResultClick = navController::openSearchResult,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                // Notes
                composable(RoutePath.APP_NOTES) {
                    val notes by remember(noteRepository) { noteRepository.observeAll() }
                        .collectAsStateWithLifecycle(initialValue = emptyList())
                    NotesHome(
                        notes = notes,
                        onSearch = { navController.navigate(RoutePath.APP_SEARCH) },
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
                        saving = action.running,
                        errorMessage = action.errorMessage,
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
                            deleting = action.running,
                            errorMessage = action.errorMessage,
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
                            saving = action.running,
                            errorMessage = action.errorMessage,
                            noteId = n.id,
                            initialTitle = n.title,
                            initialContent = n.content,
                            initialCategory = NoteCategory.fromString(n.category),
                            onSave = { title, content, category ->
                                action.run("Note saved.", "Couldn’t save this note.", afterWrite = { navController.popBackStack() }) {
                                    checkNotNull(noteRepository.update(n.id, mapOf(
                                        "title" to title,
                                        "content" to content,
                                        "category" to if (category == NoteCategory.fromString(n.category)) n.category else category.name.lowercase()
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
                    val action = rememberRecordAction()
                    AddMemory(
                        saving = action.running,
                        errorMessage = action.errorMessage,
                        onSave = { title, caption, memoryDate, attachments ->
                            val id = generateId()
                            action.run(
                                "Memory saved.", "Couldn’t save the memory. Check its attachments and try again.",
                                afterWrite = { navController.popBackStack() },
                            ) {
                                val now = DateTimeHelper.nowUtc()
                                memoryWriter().save(
                                    Memory(title = title, caption = caption, memoryDate = memoryDate,
                                        id = id, createdAt = now, updatedAt = now),
                                    original = null, originalAttachmentIds = emptyList(), attachments = attachments,
                                )
                            }
                        },
                        onBack = { if (!action.running) navController.popBackStack() },
                    )
                }

                composable("/app/memories/{memoryId}") { backStackEntry ->
                    val memoryId = backStackEntry.arguments?.getString("memoryId") ?: return@composable
                    val action = rememberRecordAction()
                    val lookup = rememberRecordLookup(memoryId) {
                        memoryRepository.getById(memoryId)?.let { saved -> saved to memoryRepository.getAssets(memoryId) }
                    }
                    lookup.record?.let { (memory, assets) ->
                        MemoryDetail(
                            memory = memory,
                            media = assets,
                            deleting = action.running,
                            errorMessage = action.errorMessage,
                            onEdit = { if (!action.running) navController.navigate("/app/memories/${android.net.Uri.encode(memoryId)}/edit") },
                            onDelete = {
                                action.run("Memory deleted.", "Couldn’t delete this memory.", afterWrite = { navController.popBackStack() }) {
                                    memoryWriter().delete(memory.id)
                                }
                            },
                            onBack = { if (!action.running) navController.popBackStack() },
                        )
                    } ?: RecordLookupScreen("Memory", lookup) { navController.popBackStack() }
                }

                composable("/app/memories/{memoryId}/edit") { backStackEntry ->
                    val memoryId = backStackEntry.arguments?.getString("memoryId") ?: return@composable
                    val action = rememberRecordAction()
                    val lookup = rememberRecordLookup(memoryId) {
                        memoryRepository.getById(memoryId)?.let { saved -> saved to memoryRepository.getAssets(memoryId) }
                    }
                    lookup.record?.let { loaded ->
                        val (original, originalAssets) = remember(memoryId) { loaded }
                        AddMemory(
                            memoryId = original.id,
                            initialTitle = original.title,
                            initialCaption = original.caption.orEmpty(),
                            initialDate = original.memoryDate,
                            initialMedia = originalAssets,
                            saving = action.running,
                            errorMessage = action.errorMessage,
                            onSave = { title, caption, memoryDate, attachments ->
                                action.run(
                                    "Memory saved.", "Couldn’t save the changes. Check the attachments and try again.",
                                    afterWrite = { navController.popBackStack() },
                                ) {
                                    memoryWriter().save(
                                        original.copy(title = title, caption = caption, memoryDate = memoryDate),
                                        original, originalAssets.map { it.id }, attachments,
                                    )
                                }
                            },
                            onBack = { if (!action.running) navController.popBackStack() },
                        )
                    } ?: RecordLookupScreen("Memory", lookup) { navController.popBackStack() }
                }

                // Timeline
                composable(RoutePath.APP_TIMELINE) {
                    val events by remember(timelineEventRepository) { timelineEventRepository.observeAll() }
                        .collectAsStateWithLifecycle(initialValue = emptyList())
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
                        saving = action.running,
                        errorMessage = action.errorMessage,
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
                            deleting = action.running,
                            errorMessage = action.errorMessage,
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
                            saving = action.running,
                            errorMessage = action.errorMessage,
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
                    val reminders by remember { reminderRepository.observeAll() }.collectAsStateWithLifecycle(emptyList())
                    val action = rememberRecordAction()
                    RemindersHome(
                        reminders = reminders,
                        onReminderClick = { reminderId ->
                            navController.navigate("${RoutePath.APP_REMINDERS}/${Uri.encode(reminderId)}")
                        },
                        onAddReminder = {
                            navController.navigate(RoutePath.APP_REMINDERS_ADD)
                        },
                        onBack = {
                            navController.popBackStack()
                        },
                        onToggleCompleted = { action.toggleReminder(reminderRepository, it) },
                        updating = action.running,
                        errorMessage = action.errorMessage,
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
                        },
                        saving = action.running,
                        errorMessage = action.errorMessage,
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
                                if (!action.running) navController.navigate("${RoutePath.APP_REMINDERS}/${Uri.encode(reminderId)}/edit")
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
                            updating = action.running,
                            errorMessage = action.errorMessage,
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
                            initialDate = r.scheduledDate,
                            initialTime = r.scheduledTime ?: "",
                            initialRecurrence = r.recurrence,
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
                            },
                            saving = action.running,
                            errorMessage = action.errorMessage,
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
                        onPlaceClick = { id -> navController.navigate("${RoutePath.APP_PLACES}/${Uri.encode(id)}") },
                        onAddPlace = { navController.navigate(RoutePath.APP_PLACES_ADD) },
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(RoutePath.APP_PLACES_ADD) {
                    val action = rememberRecordAction()
                    CreatePlace(
                        onSave = { name, address, city, notes, category, photo ->
                            action.run("Place saved.", "Couldn’t save this place or its photo.", afterWrite = { navController.popBackStack() }) {
                                val now = DateTimeHelper.nowUtc()
                                placeWriter().save(com.twohearts.app.data.entity.Place(
                                    name = name, address = address, city = city,
                                    notes = notes, category = category, id = generateId(), createdAt = now, updatedAt = now,
                                ), original = null, photo = photo)
                            }
                        },
                        onBack = { if (!action.running) navController.popBackStack() },
                        saving = action.running,
                        errorMessage = action.errorMessage,
                    )
                }

                composable("${RoutePath.APP_PLACES}/{placeId}") { backStackEntry ->
                    val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
                    val action = rememberRecordAction()
                    val lookup = rememberRecordLookup(placeId) {
                        placeRepository.getById(placeId)?.let { saved ->
                            saved to saved.photoRef?.let { mediaAssetDao.getById(it) }
                        }
                    }
                    lookup.record?.let { (place, asset) ->
                        PlaceDetail(
                            place = place, media = asset,
                            onEdit = {
                                if (!action.running) navController.navigate("${RoutePath.APP_PLACES}/${Uri.encode(placeId)}/edit")
                            },
                            onDelete = {
                                action.run("Place deleted.", "Couldn’t delete this place.", afterWrite = { navController.popBackStack() }) {
                                    placeRepository.softDelete(place.id)
                                }
                            },
                            onBack = { if (!action.running) navController.popBackStack() },
                            deleting = action.running,
                            errorMessage = action.errorMessage,
                        )
                    } ?: RecordLookupScreen("Place", lookup) { navController.popBackStack() }
                }

                composable("${RoutePath.APP_PLACES}/{placeId}/edit") { backStackEntry ->
                    val placeId = backStackEntry.arguments?.getString("placeId") ?: return@composable
                    val action = rememberRecordAction()
                    val lookup = rememberRecordLookup(placeId) {
                        placeRepository.getById(placeId)?.let { saved ->
                            saved to saved.photoRef?.let { mediaAssetDao.getById(it) }
                        }
                    }
                    lookup.record?.let { (place, asset) ->
                        CreatePlace(
                            placeId = place.id, initialName = place.name, initialAddress = place.address.orEmpty(),
                            initialCity = place.city.orEmpty(), initialNotes = place.notes.orEmpty(), initialCategory = place.category,
                            initialMedia = asset, hasSavedPhoto = place.photoRef != null,
                            onSave = { name, address, city, notes, category, photo ->
                                action.run("Place saved.", "Couldn’t save this place or its photo.", afterWrite = { navController.popBackStack() }) {
                                    placeWriter().save(place.copy(
                                        name = name, address = address, city = city, notes = notes, category = category,
                                    ), original = place, photo = photo)
                                }
                            },
                            onBack = { if (!action.running) navController.popBackStack() },
                            saving = action.running,
                            errorMessage = action.errorMessage,
                        )
                    } ?: RecordLookupScreen("Place", lookup) { navController.popBackStack() }
                }

                // Mood
                composable(RoutePath.APP_MOOD) {
                    val moods by remember { moodEntryRepository.observeAll() }.collectAsStateWithLifecycle(emptyList())
                    val owner by remember { relationshipService.observeOwner() }.collectAsStateWithLifecycle(initialValue = null)
                    val today = DateTimeHelper.todayLocal()
                    val todayMood = moods.firstOrNull { it.entryDate == today && it.profileId == owner?.id }
                        ?: moods.firstOrNull { it.entryDate == today && it.belongsToOwner(owner?.id) }
                    val action = rememberRecordAction()
                    MoodHome(
                        todayMood = todayMood,
                        recentMoods = moods.filter { it.belongsToOwner(owner?.id) },
                        onAddMood = {
                            navController.navigate(todayMood?.let { "${RoutePath.APP_MOOD}/${Uri.encode(it.id)}/edit" }
                                ?: RoutePath.APP_MOOD_ADD)
                        },
                        onViewHistory = { navController.navigate(RoutePath.APP_MOOD_HISTORY) },
                        onBack = { navController.popBackStack() },
                        onQuickMood = { mood ->
                            action.run("Mood logged.", "Couldn’t log your mood.") {
                                val profileId = checkNotNull(relationshipService.getOwner()).id
                                moodEntryRepository.saveOwnerCheckIn(
                                    profileId, DateTimeHelper.todayLocal(), mood.value, mood.emoji, keepNote = true,
                                )
                            }
                        },
                        updating = action.running,
                        errorMessage = action.errorMessage,
                    )
                }

                composable(RoutePath.APP_MOOD_ADD) {
                    val action = rememberRecordAction()
                    val day = rememberSaveable { DateTimeHelper.todayLocal() }
                    MoodEntryScreen(
                        entryDate = day,
                        onSave = { moodValue, moodEmoji, note ->
                            action.run("Mood logged.", "Couldn’t log your mood.", afterWrite = { navController.popBackStack() }) {
                                val profileId = checkNotNull(relationshipService.getOwner()).id
                                moodEntryRepository.saveOwnerCheckIn(profileId, day, moodValue, moodEmoji, note)
                            }
                        },
                        onBack = { if (!action.running) navController.popBackStack() },
                        saving = action.running,
                        errorMessage = action.errorMessage,
                    )
                }

                composable(RoutePath.APP_MOOD_HISTORY) {
                    val moods by remember { moodEntryRepository.observeAll() }.collectAsStateWithLifecycle(emptyList())
                    val owner by remember { relationshipService.observeOwner() }.collectAsStateWithLifecycle(initialValue = null)
                    MoodHistory(
                        moods = moods, ownerId = owner?.id,
                        onBack = { navController.popBackStack() },
                        onEdit = { navController.navigate("${RoutePath.APP_MOOD}/${Uri.encode(it.id)}/edit") },
                    )
                }

                composable("${RoutePath.APP_MOOD}/{entryId}/edit") { backStackEntry ->
                    val entryId = backStackEntry.arguments?.getString("entryId") ?: return@composable
                    val action = rememberRecordAction()
                    val lookup = rememberRecordLookup(entryId) {
                        val ownerId = relationshipService.getOwner()?.id
                        moodEntryRepository.getById(entryId)?.takeIf { it.belongsToOwner(ownerId) }
                    }
                    lookup.record?.let { entry ->
                        MoodEntryScreen(
                            entryId = entry.id, entryDate = entry.entryDate,
                            initialMood = entry.moodValue, initialEmoji = entry.moodEmoji, initialNote = entry.note.orEmpty(),
                            onSave = { moodValue, moodEmoji, note ->
                                action.run("Mood saved.", "Couldn’t save this mood.", afterWrite = { navController.popBackStack() }) {
                                    checkNotNull(moodEntryRepository.update(entry.id, mapOf(
                                        "moodValue" to moodValue, "moodEmoji" to moodEmoji, "note" to note,
                                    )))
                                }
                            },
                            onDelete = {
                                action.run("Check-in deleted.", "Couldn’t delete this check-in.", afterWrite = { navController.popBackStack() }) {
                                    moodEntryRepository.softDelete(entry.id)
                                }
                            },
                            onBack = { if (!action.running) navController.popBackStack() },
                            saving = action.running,
                            errorMessage = action.errorMessage,
                        )
                    } ?: RecordLookupScreen("Mood entry", lookup) { navController.popBackStack() }
                }

                periodRoutes(navController, relationshipService, periodEntryRepository, periodSettingsRepository)

                // Vault
                composable(RoutePath.APP_VAULT) {
                    val items by remember { vaultRepository.observeAll() }.collectAsStateWithLifecycle(emptyList())
                    VaultHome(
                        items = items,
                        onItemClick = { item ->
                            navController.navigate(RoutePath.APP_VAULT_CONTENT.replace(":contentId", android.net.Uri.encode(item.id)))
                        },
                        onAddClick = { navController.navigate(RoutePath.APP_VAULT_ADD) },
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(RoutePath.APP_VAULT_ADD) {
                    val action = rememberRecordAction()
                    AddVaultContent(
                        saving = action.running,
                        onBack = { if (!action.running) navController.popBackStack() },
                        onContentAdded = { title, type, payload ->
                            action.run("Saved to Vault.", "Couldn’t save this vault item.", afterWrite = { navController.popBackStack() }) {
                                vaultStorage.add(title, type.name.lowercase(), payload)
                            }
                        },
                    )
                }

                composable(RoutePath.APP_VAULT_CONTENT.replace(":contentId", "{contentId}")) { route ->
                    val id = route.arguments?.getString("contentId") ?: return@composable
                    val lookup = rememberRecordLookup(id) {
                        vaultRepository.getById(id)?.let { it to it.mediaRef?.let { ref -> mediaAssetDao.getById(ref) } }
                    }
                    val action = rememberRecordAction()
                    val item = lookup.record
                    if (item != null) {
                        VaultContentViewer(
                            item = item.first,
                            asset = item.second,
                            onBack = { if (!action.running) navController.popBackStack() },
                            onDelete = {
                                action.run("Vault item deleted.", "Couldn’t delete this vault item.", afterWrite = { navController.popBackStack() }) {
                                    vaultRepository.softDelete(it.id)
                                }
                            },
                            onExport = { uri -> vaultStorage.export(checkNotNull(item.second), uri) },
                        )
                    } else RecordLookupScreen("Vault item", lookup) { navController.popBackStack() }
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

                legacyGameRedirects(navController)

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
                                        val db = com.twohearts.app.data.database.TwoHeartsDatabase.getDatabase(context)
                                        val writer = ProfilePhotoWriter(db, mediaStorage, ProfilePhotoStaging(context))
                                        writer.commit(photo) { replacement ->
                                            val profile = checkNotNull(relationshipService.getOwner())
                                            val ref = photo.reference(profile.photoRef, replacement)
                                            relationshipService.saveProfileDetails(profile.id, name, birthday, ref)
                                            profile.photoRef?.takeUnless { it == ref }
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
                        onSave = { name, date, photo ->
                            if (!saving) {
                                saving = true
                                error = null
                                scope.launch {
                                    try {
                                        val db = com.twohearts.app.data.database.TwoHeartsDatabase.getDatabase(context)
                                        val writer = ProfilePhotoWriter(db, mediaStorage, ProfilePhotoStaging(context))
                                        writer.commit(photo) { replacement ->
                                            val owner = checkNotNull(relationshipService.getOwner())
                                            val previous = relationshipService.getPartner()
                                            val ref = photo.reference(previous?.photoRef, replacement)
                                            val partner = if (previous == null) {
                                                relationshipService.createPartner(name, null, ref)
                                            } else {
                                                relationshipService.saveProfileDetails(previous.id, name, previous.birthday, ref)
                                            }
                                            val saved = relationshipService.getRelationship()
                                            if (saved == null) relationshipService.createRelationship(owner.id, partner.id, date)
                                            else relationshipService.updateRelationship(saved.id, date)
                                            previous?.photoRef?.takeUnless { it == ref }
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
                    val securityState by appLockService.lockState.collectAsStateWithLifecycle()
                    SecuritySettingsScreen(
                        isAppLockEnabled = securityState != AppLockState.DISABLED,
                        onCreatePin = appLockService::createPin,
                        onChangePin = { old, new ->
                            withContext(Dispatchers.IO) { appLockService.changePin(old, new) }
                        },
                        onDisableLock = { pin ->
                            val verified = withContext(Dispatchers.IO) { appLockService.verifyPin(pin) }
                            if (verified) appLockService.disable()
                            verified
                        },
                        onBack = { navController.popBackStack() },
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
                    val dates by remember { importantDateRepository.observeAll() }.collectAsStateWithLifecycle(emptyList())
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
