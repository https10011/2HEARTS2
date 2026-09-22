package com.twohearts.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.settings.SettingsStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Regression test for the real-device launch crash.
 *
 * ## The failure this pins
 *
 * `SettingsStorage` originally declared its `DataStore` as a *member* property
 * and was constructed twice during startup — once in `MainActivity` (which
 * subscribes to `settings` for the theme, so the DataStore is active) and again
 * inside `BootstrapService`'s app-lock stage. DataStore permits exactly one
 * active instance per file per process, so the second construction threw:
 *
 * ```
 * java.lang.IllegalStateException: There are multiple DataStores active for the
 * same file: .../datastore/twohearts_settings.preferences_pb
 * ```
 *
 * `BootstrapService.bootstrap()` catches `Exception` and returns `false`, so the
 * app did not surface that exception — it simply never left the loading state
 * and was reported as a launch crash.
 *
 * ## Why the existing 80 tests missed it
 *
 * Robolectric gives every test method a fresh data directory, and no existing
 * test constructed two storages in one process. A test suite can therefore be
 * entirely green while the app cannot start. These tests exist so that stays
 * impossible: they exercise the *real* startup shape — two independent call
 * sites acquiring storage in one process — rather than a single happy path.
 */
@RunWith(RobolectricTestRunner::class)
class SettingsStorageSingletonTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun repeatedAcquisitionReturnsTheSameInstance() {
        // MainActivity's acquisition and BootstrapService's must be the same
        // object, or the second one creates a competing DataStore.
        val fromActivity = SettingsStorage.getInstance(context)
        val fromBootstrap = SettingsStorage.getInstance(context)
        assertSame(fromActivity, fromBootstrap)
    }

    @Test
    fun twoCallSitesCanReadConcurrently() {
        // The exact startup sequence: the Activity subscribes for the theme,
        // then bootstrap reads settings for app-lock. Before the fix the second
        // read threw IllegalStateException.
        val activityStorage = SettingsStorage.getInstance(context)
        val bootstrapStorage = SettingsStorage.getInstance(context)

        runBlocking {
            val themeRead = async { activityStorage.settings.first() }
            val lockRead = async { bootstrapStorage.settings.first() }
            // Both complete; neither throws.
            assertEquals(themeRead.await().themeMode, lockRead.await().themeMode)
        }
    }

    @Test
    fun writeThroughOneReferenceIsVisibleThroughTheOther() {
        // Sharing one instance must not cost correctness: settings written by
        // one consumer are read by the next.
        val first = SettingsStorage.getInstance(context)
        val second = SettingsStorage.getInstance(context)

        runBlocking {
            first.updateThemeMode("dark")
            assertEquals("dark", second.settings.first().themeMode)
            first.updateThemeMode("system")
        }
    }

    @Test
    fun appLockInitializationDoesNotPoisonTheActivitySettingsAccess() {
        // The precise production crash path, end to end:
        //
        //   1. MainActivity acquires storage for the theme.
        //   2. BootstrapService runs; its app-lock stage acquires storage and
        //      activates the DataStore via AppLockService.initialize().
        //   3. Bootstrap returns true and MainActivity calls
        //      AppStateService.initialize() on the *Activity's* storage.
        //
        // Before the fix, step 3 threw "multiple DataStores active for the same
        // file" on the Activity's DataStore and, because that call sits outside
        // bootstrap's try/catch, it killed the process. Both storages must now
        // be one object so step 3 is a plain read.
        val activityStorage = SettingsStorage.getInstance(context)
        val bootstrapStorage = SettingsStorage.getInstance(context)
        assertSame(activityStorage, bootstrapStorage)

        runBlocking {
            val fromBootstrap = bootstrapStorage.settings.first()
            assertEquals(false, fromBootstrap.appLockEnabled)

            // Step 3 must not throw.
            val appState = com.twohearts.app.services.appstate.AppStateService(activityStorage)
            appState.initialize()
            assertEquals("system", appState.themeMode.value)
        }
    }
}
