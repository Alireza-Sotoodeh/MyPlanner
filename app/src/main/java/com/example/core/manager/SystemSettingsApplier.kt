package com.example.core.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object SystemSettingsApplier {

    fun reapplyAfterRestore(context: Context) {
        createNotificationChannels(context)
    }

    private fun createNotificationChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channels = listOf(
            NotificationChannel("pomodoro", "Pomodoro Timer", NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel("event_reminder", "Event Reminders", NotificationManager.IMPORTANCE_HIGH),
            NotificationChannel("habits", "Habit Reminders", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel("task_reminder", "Task Reminders", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel("learn_review", "Learn Review", NotificationManager.IMPORTANCE_DEFAULT),
            NotificationChannel("reminders", "Daily Reminders", NotificationManager.IMPORTANCE_DEFAULT)
        )
        channels.forEach { manager.createNotificationChannel(it) }
    }
}
