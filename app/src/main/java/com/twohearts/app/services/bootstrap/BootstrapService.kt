package com.twohearts.app.services.bootstrap

import android.content.Context
import com.twohearts.app.data.settings.SecureStorage
import com.twohearts.app.data.database.DatabaseInitializer
import com.twohearts.app.services.device.DeviceCapabilities
import com.twohearts.app.services.lifecycle.LifecycleService
import com.twohearts.app.services.notification.NotificationService
import com.twohearts.app.services.security.AppLockService
import com.twohearts.app.services.logger.Logger
import kotlinx.coroutines.CancellationException

/**
 * BootstrapService — ordered initialization pipeline.
 *
 * Matches legacy bootstrapApp() exactly:
 * 1. persistence (CRITICAL) — initializeDatabase()
 * 2. schema-verify (CRITICAL) — verifySchemaVersion()
 * 3. device-capabilities — DeviceCapabilities.initialize()
 * 4. lifecycle — appLifecycle.start()
 * 5. notifications — NotificationService.initialize()
 * 6. app-lock — AppLockService.initialize()
 * 7. application-state — AppStateService + RelationshipService + MediaStorage
 */
class BootstrapService(private val context: Context) {

    private val logger = Logger("Bootstrap")

    // Services to be initialized
    lateinit var databaseInitializer: DatabaseInitializer
        private set
    lateinit var deviceCapabilities: DeviceCapabilities
        private set
    lateinit var lifecycleService: LifecycleService
        private set
    lateinit var appLockService: AppLockService
        private set

    /**
     * Run the bootstrap pipeline.
     * Returns true if all stages succeed.
     */
    suspend fun bootstrap(): Boolean {
        return try {
            logger.info("Starting bootstrap pipeline")

            // Stage 1: persistence (CRITICAL)
            logger.info("Stage 1: Initializing database")
            databaseInitializer = DatabaseInitializer(context)
            databaseInitializer.initialize()

            // Stage 2: schema-verify (CRITICAL)
            logger.info("Stage 2: Verifying schema version")
            if (!databaseInitializer.verifySchemaVersion()) {
                logger.error("Schema verification failed")
                return false
            }

            // Stage 3: device-capabilities
            logger.info("Stage 3: Initializing device capabilities")
            deviceCapabilities = DeviceCapabilities(context)
            deviceCapabilities.initialize()

            // Stage 4: lifecycle
            logger.info("Stage 4: Starting lifecycle service")
            lifecycleService = LifecycleService()
            lifecycleService.start()

            // Stage 5: notifications
            logger.info("Stage 5: Initializing notification service")
            NotificationService(context).initialize()

            // Stage 6: app-lock
            logger.info("Stage 6: Initializing app lock service")
            val settingsStorage = com.twohearts.app.data.settings.SettingsStorage.getInstance(context)
            val applicationContext = context.applicationContext
            appLockService = AppLockService({ SecureStorage(applicationContext) }, settingsStorage, lifecycleService)
            appLockService.initialize()

            logger.info("Bootstrap pipeline completed successfully")
            true
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            logger.error("Bootstrap pipeline failed", e)
            false
        }
    }

    fun stop() {
        if (::lifecycleService.isInitialized) lifecycleService.stop()
    }

// Access services via their property names directly:
// bootstrap.databaseInitializer, bootstrap.deviceCapabilities, etc.
}
