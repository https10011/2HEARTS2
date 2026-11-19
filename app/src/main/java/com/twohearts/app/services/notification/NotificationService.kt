package com.twohearts.app.services.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

/** Registers the app's existing Android notification channels. */
internal class NotificationService(private val context: Context) {
    fun initialize() {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(CHANNEL_REMINDERS, "Reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Scheduled reminders"
                },
                NotificationChannel(CHANNEL_ANNIVERSARIES, "Anniversaries", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Anniversary notifications"
                },
                NotificationChannel(CHANNEL_GENERAL, "General", NotificationManager.IMPORTANCE_DEFAULT),
            ),
        )
    }

    companion object {
        const val CHANNEL_REMINDERS = "reminders"
        const val CHANNEL_ANNIVERSARIES = "anniversaries"
        const val CHANNEL_GENERAL = "general"
    }
}
