package com.example.core.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.core.database.entity.TaskEntity
import com.example.core.receiver.ReminderReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ReminderManager {
    fun scheduleReminders(context: Context, task: TaskEntity, vibrate: Boolean, sound: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (task.type != "EVENT" || task.eventTime.isNullOrBlank()) return

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val eventDateTimeStr = "${task.date} ${task.eventTime}"
        
        try {
            val eventDate = sdf.parse(eventDateTimeStr)
            if (eventDate == null || eventDate.before(Calendar.getInstance().time)) {
                return // Event is in the past
            }

            // 1. Remind night before (e.g. 20:00)
            if (task.notifyNightBefore) {
                val cal = Calendar.getInstance().apply { time = eventDate }
                cal.add(Calendar.DAY_OF_YEAR, -1)
                cal.set(Calendar.HOUR_OF_DAY, 20)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)

                if (cal.time.after(Calendar.getInstance().time)) {
                    scheduleAlarm(
                        context = context,
                        alarmManager = alarmManager,
                        timeInMillis = cal.timeInMillis,
                        taskId = task.id * 10, // unique id for night before
                        isNightBefore = true,
                        title = "Tomorrow: ${task.title}",
                        message = "Your event is tomorrow at ${task.eventTime}",
                        vibrate = vibrate,
                        sound = sound
                    )
                }
            }

            // 2. Remind X minutes before
            task.reminderMinutesBefore?.let { minutes ->
                if (minutes > 0) {
                    val cal = Calendar.getInstance().apply { time = eventDate }
                    cal.add(Calendar.MINUTE, -minutes)
                    
                    if (cal.time.after(Calendar.getInstance().time)) {
                        scheduleAlarm(
                            context = context,
                            alarmManager = alarmManager,
                            timeInMillis = cal.timeInMillis,
                            taskId = task.id * 10 + 1, // unique id for X mins before
                            isNightBefore = false,
                            title = "Upcoming Event: ${task.title}",
                            message = "Starting in $minutes minutes",
                            vibrate = vibrate,
                            sound = sound
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ReminderManager", "Failed to parse event time", e)
        }
    }

    fun cancelReminders(context: Context, task: TaskEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intentNightBefore = Intent(context, ReminderReceiver::class.java)
        val pendingIntentNightBefore = PendingIntent.getBroadcast(
            context, (task.id * 10).toInt(), intentNightBefore, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntentNightBefore)

        val intentMinsBefore = Intent(context, ReminderReceiver::class.java)
        val pendingIntentMinsBefore = PendingIntent.getBroadcast(
            context, (task.id * 10 + 1).toInt(), intentMinsBefore, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntentMinsBefore)
    }

    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        timeInMillis: Long,
        taskId: Long,
        isNightBefore: Boolean = false,
        title: String,
        message: String,
        vibrate: Boolean,
        sound: Boolean
    ) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("vibrate", vibrate)
            putExtra("sound", sound)
            putExtra("taskId", taskId)
            putExtra("isNightBefore", isNightBefore)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.e("ReminderManager", "Exact alarm permission missing", e)
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    }

    suspend fun rescheduleAllAlarms(context: Context) {
        try {
            val database = com.example.core.database.AppDatabase.getDatabase(context)
            val events = database.taskDao().getUpcomingEventsSync()
            
            val prefs = context.getSharedPreferences("bulletcoach_prefs", Context.MODE_PRIVATE)
            val vibrate = prefs.getBoolean("event_reminder_vibrate", true)
            val sound = prefs.getBoolean("event_reminder_sound", true)
            
            events.forEach { event ->
                scheduleReminders(context, event, vibrate, sound)
            }
            Log.d("ReminderManager", "Successfully rescheduled ${events.size} event alarms.")
        } catch (e: Exception) {
            Log.e("ReminderManager", "Failed to reschedule alarms", e)
        }
    }
}
