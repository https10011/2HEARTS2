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
import com.twohearts.app.services.bootstrap.BootstrapService
import com.twohearts.app.services.appstate.AppStateService
import com.twohearts.app.services.relationship.RelationshipService
import com.twohearts.app.services.security.AppLockService
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
                                    bootstrapService.getDatabaseInitializer().profileDao()
                                ),
                                relationshipRepository = com.twohearts.app.data.repository.CoupleRelationshipRepository(
                                    bootstrapService.getDatabaseInitializer().coupleRelationshipDao()
                                )
                            )
                        }
                        val appLockService = remember {
                            bootstrapService.getAppLockService()
                        }

                        // Initialize app state
                        LaunchedEffect(Unit) {
                            appStateService.initialize()
                        }

                        // App router
                        AppRouter(
                            appStateService = appStateService,
                            relationshipService = relationshipService,
                            appLockService = appLockService
                        )
                    } else {
                        // Loading state
                        com.twohearts.app.ui.components.LoadingState(
                            message = "Loading..."
                        )
                    }
                }
            }
        }
    }
}
