package com.twohearts.app

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ApplicationProvider
import com.twohearts.app.data.settings.SettingsStorage
import com.twohearts.app.services.lifecycle.LifecycleService
import com.twohearts.app.services.security.AppLockService
import com.twohearts.app.services.security.AppLockState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class Phase19StartupTest {
    @Test
    fun startupRestoresTheLockGateWithoutOpeningSecureStorage() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val settings = SettingsStorage.getInstance(context)
        val owner = object : LifecycleOwner {
            override val lifecycle = LifecycleRegistry(this)
        }
        val lifecycle = LifecycleService(owner.lifecycle)
        for ((enabled, expected) in listOf(false to AppLockState.DISABLED, true to AppLockState.LOCKED)) {
            settings.setAppLockEnabled(enabled)
            val lock = AppLockService(
                openSecureStorage = { error("Startup should defer opening the PIN verifier") },
                settingsStorage = settings,
                lifecycleService = lifecycle,
            )
            lock.initialize()
            assertEquals(expected, lock.lockState.value)
        }
    }
}
