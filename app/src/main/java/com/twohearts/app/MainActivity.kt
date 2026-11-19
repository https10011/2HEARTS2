package com.twohearts.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.twohearts.app.ui.navigation.AppRouter
import com.twohearts.app.ui.theme.TwoHeartsTheme
import com.twohearts.app.ui.theme.TextScalingLevel
import com.twohearts.app.ui.components.ThLoadingState
import com.twohearts.app.ui.components.ThEmptyState
import com.twohearts.app.services.bootstrap.BootstrapService
import com.twohearts.app.services.appstate.AppStateService
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.services.security.AppLockService
import com.twohearts.app.services.search.SearchEngine
import com.twohearts.app.services.datamanagement.DataManagementService
import com.twohearts.app.services.media.MediaStorage
import com.twohearts.app.services.media.FileService
import com.twohearts.app.data.repository.NotificationCenterRepository
import com.twohearts.app.data.settings.SettingsStorage
import com.twohearts.app.data.repository.VaultItemRepository
import com.twohearts.app.services.media.VaultStorage
import com.twohearts.app.services.notification.OPEN_ALERTS
import com.twohearts.app.services.notification.ReminderScheduler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var bootstrapService: BootstrapService
    private var openAlerts by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize bootstrap service
        bootstrapService = BootstrapService(this)
        openAlerts = intent.action == OPEN_ALERTS
        // Created before setContent so the theme can observe persisted
        // appearance settings from the very first frame.
        val settingsStorage = SettingsStorage.getInstance(this)
        val appStateService = AppStateService(settingsStorage)

        setContent {
            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()

            // Appearance settings are read from AppStateService rather than
            // held in local state. Phase 1: these previously lived as
            // `remember { mutableStateOf(...) }` in this composable and were
            // never connected to SettingsStorage, so the Appearance screen's
            // theme / text-size / reduce-motion controls had no visible
            // effect on the running app.
            val themeMode by appStateService.themeMode.collectAsState()
            val textSize by appStateService.textSize.collectAsState()
            val reduceMotion by appStateService.reduceMotion.collectAsState()

            val darkMode = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemDark
            }

            var isInitialized by remember { mutableStateOf(false) }
            var startupFailed by remember { mutableStateOf(false) }
            var startupAttempt by remember { mutableIntStateOf(0) }

            // Initialize services
            LaunchedEffect(startupAttempt) {
                startupFailed = false
                try {
                    check(bootstrapService.bootstrap())
                    appStateService.initialize()
                    isInitialized = true
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    bootstrapService.stop()
                    startupFailed = true
                }
            }

            TwoHeartsTheme(
                darkMode = darkMode,
                textSize = textSize,
                reduceMotion = reduceMotion
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isInitialized) {
                        val relationshipService = remember {
                            RelationshipService(
                                profileRepository = com.twohearts.app.data.repository.ProfileRepository(
                                    bootstrapService.databaseInitializer.profileDao()
                                ),
                                relationshipRepository = com.twohearts.app.data.repository.CoupleRelationshipRepository(
                                    bootstrapService.databaseInitializer.coupleRelationshipDao()
                                )
                            )
                        }
                        val appLockService = remember {
                            bootstrapService.appLockService
                        }

                        // Create repositories for AppRouter
                        val noteRepository = remember {
                            com.twohearts.app.data.repository.NoteRepository(
                                bootstrapService.databaseInitializer.noteDao()
                            )
                        }
                        val memoryRepository = remember {
                            com.twohearts.app.data.repository.MemoryRepository(
                                dao = bootstrapService.databaseInitializer.memoryDao(),
                                memoryMediaDao = bootstrapService.databaseInitializer.memoryMediaDao()
                            )
                        }
                        val timelineEventRepository = remember {
                            com.twohearts.app.data.repository.TimelineEventRepository(
                                bootstrapService.databaseInitializer.timelineEventDao()
                            )
                        }
                        val reminderRepository = remember {
                            com.twohearts.app.data.repository.ReminderRepository(
                                bootstrapService.databaseInitializer.reminderDao()
                            )
                        }
                        LaunchedEffect(reminderRepository) {
                            val flags = settingsStorage.settings.map { it.notificationsEnabled && it.remindersEnabled }
                                .distinctUntilChanged()
                            val scheduler = ReminderScheduler(applicationContext)
                            reminderRepository.observeAll().combine(flags) { reminders, enabled -> reminders to enabled }
                                .collect { (reminders, enabled) ->
                                    withContext(Dispatchers.IO) {
                                        runCatching { scheduler.reconcile(reminders, enabled) }
                                    }
                                }
                        }
                        val placeRepository = remember {
                            com.twohearts.app.data.repository.PlaceRepository(
                                bootstrapService.databaseInitializer.placeDao()
                            )
                        }
                        val moodEntryRepository = remember {
                            com.twohearts.app.data.repository.MoodEntryRepository(
                                bootstrapService.databaseInitializer.moodEntryDao()
                            )
                        }
                        val periodEntryRepository = remember {
                            com.twohearts.app.data.repository.PeriodEntryRepository(
                                bootstrapService.databaseInitializer.periodEntryDao()
                            )
                        }
                        val periodSettingsRepository = remember {
                            com.twohearts.app.data.repository.PeriodSettingsRepository(
                                bootstrapService.databaseInitializer.periodSettingsDao()
                            )
                        }
                        val importantDateRepository = remember {
                            com.twohearts.app.data.repository.ImportantDateRepository(
                                bootstrapService.databaseInitializer.importantDateDao()
                            )
                        }

                        // Create services required by AppRouter
                        val searchEngine = remember { SearchEngine() }
                        val notificationCenterRepository = remember {
                            NotificationCenterRepository(
                                bootstrapService.databaseInitializer.notificationCenterDao()
                            )
                        }

                        val fileService = remember {
                            FileService(this@MainActivity)
                        }
                        val mediaStorage = remember {
                            MediaStorage(this@MainActivity)
                        }
                        val vaultRepository = remember { VaultItemRepository(bootstrapService.databaseInitializer.vaultItemDao()) }
                        val vaultStorage = remember {
                            VaultStorage(applicationContext, checkNotNull(bootstrapService.databaseInitializer.getDatabase()), mediaStorage, vaultRepository)
                        }
                        val dataManagementService = remember {
                            DataManagementService(
                                context = this@MainActivity,
                                databaseInitializer = bootstrapService.databaseInitializer,
                                mediaStorage = mediaStorage,
                                fileService = fileService,
                            )
                        }

                        // App router
                        AppRouter(
                            appStateService = appStateService,
                            relationshipService = relationshipService,
                            appLockService = appLockService,
                            noteRepository = noteRepository,
                            memoryRepository = memoryRepository,
                            timelineEventRepository = timelineEventRepository,
                            reminderRepository = reminderRepository,
                            placeRepository = placeRepository,
                            moodEntryRepository = moodEntryRepository,
                            periodEntryRepository = periodEntryRepository,
                            periodSettingsRepository = periodSettingsRepository,
                            importantDateRepository = importantDateRepository,
                            dataManagementService = dataManagementService,
                            searchEngine = searchEngine,
                            notificationCenterRepository = notificationCenterRepository,
                            settingsStorage = settingsStorage,
                            mediaAssetDao = bootstrapService.databaseInitializer.mediaAssetDao(),
                            mediaStorage = mediaStorage,
                            vaultRepository = vaultRepository,
                            vaultStorage = vaultStorage,
                            openAlerts = openAlerts,
                            onAlertsOpened = { openAlerts = false; intent.action = null },
                        )
                    } else if (startupFailed) {
                        ThEmptyState(
                            title = "Couldn’t open TwoHearts",
                            description = "Try starting the app again.",
                            modifier = Modifier.fillMaxSize(),
                            actionLabel = "Try again",
                            onAction = { startupAttempt++ },
                        )
                    } else {
                        // Loading state
                        ThLoadingState(
                            label = "Loading..."
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openAlerts = intent.action == OPEN_ALERTS
    }

    override fun onDestroy() {
        if (::bootstrapService.isInitialized) bootstrapService.stop()
        super.onDestroy()
    }
}
