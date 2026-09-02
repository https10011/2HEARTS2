package com.twohearts.app

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
import com.twohearts.app.services.bootstrap.BootstrapService
import com.twohearts.app.services.appstate.AppStateService
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.services.security.AppLockService
import com.twohearts.app.services.search.SearchEngine
import com.twohearts.app.services.datamanagement.DataManagementService
import com.twohearts.app.services.media.MediaStorage
import com.twohearts.app.services.media.FileService
import com.twohearts.app.data.repository NotificationCenterRepository
import com.twohearts.app.data.settings.SettingsStorage
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize bootstrap service
        val bootstrapService = BootstrapService(this)

        setContent {
            var darkMode by remember { mutableStateOf(false) }
            var textScaling by remember { mutableStateOf(TextScalingLevel.DEFAULT) }
            var isInitialized by remember { mutableStateOf(false) }

            // Initialize services
            LaunchedEffect(Unit) {
                val success = bootstrapService.bootstrap()
                if (success) {
                    isInitialized = true
                }
            }

            TwoHeartsTheme(
                darkMode = darkMode,
                textScalingLevel = textScaling
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isInitialized) {
                        // Create services
                        val appStateService = remember {
                            AppStateService(
                                com.twohearts.app.data.settings.SettingsStorage(this@MainActivity)
                            )
                        }
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
                        val importantDateRepository = remember {
                            com.twohearts.app.data.repository.ImportantDateRepository(
                                bootstrapService.databaseInitializer.importantDateDao()
                            )
                        }

                        // Initialize app state
                        LaunchedEffect(Unit) {
                            appStateService.initialize()
                        }

                        // Create services required by AppRouter
                        val settingsStorage = remember {
                            SettingsStorage(this@MainActivity)
                        }
                        val searchEngine = remember { SearchEngine() }
                        val notificationCenterRepository = remember {
                            NotificationCenterRepository(
                                bootstrapService.databaseInitializer.notificationCenterDao()
                            )
                        }

                        // MediaStorage + FileService stubs (placeholder until full integration)
                        val fileService = remember {
                            FileService(this@MainActivity)
                        }
                        val mediaStorage = remember {
                            MediaStorage(this@MainActivity)
                        }
                        val dataManagementService = remember {
                            DataManagementService(
                                context = this@MainActivity,
                                databaseInitializer = bootstrapService.databaseInitializer,
                                mediaStorage = mediaStorage,
                                fileService = fileService,
                                appLockService = appLockService,
                                settingsStorage = settingsStorage
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
                            importantDateRepository = importantDateRepository,
                            dataManagementService = dataManagementService,
                            searchEngine = searchEngine,
                            notificationCenterRepository = notificationCenterRepository,
                            settingsStorage = settingsStorage
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
}
