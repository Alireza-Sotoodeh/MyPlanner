package com.example.core.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.core.database.entity.HabitEntity
import com.example.core.database.entity.TaskEntity
import com.example.core.receiver.ReminderReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ReminderManager {

    const val AUTO_BACKUP_ACTION = "com.example.action.AUTO_BACKUP"
    private const val AUTO_BACKUP_REQUEST_CODE = 13000

    fun scheduleAutoBackup(context: Context) {
        val prefs = context.getSharedPreferences("bulletcoach_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("backup_enabled", true)) return

        val timeStr = prefs.getString("backup_time", "23:00") ?: "23:00"
        val hour = timeStr.substringBefore(":").toIntOrNull() ?: 23
        val minute = timeStr.substringAfter(":").toIntOrNull() ?: 0

        val now = Calendar.getInstance()
        val scheduled = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply { action = AUTO_BACKUP_ACTION }
        val pendingIntent = PendingIntent.getBroadcast(
            context, AUTO_BACKUP_REQUEST_CODE, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduled.timeInMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduled.timeInMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduled.timeInMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.e("ReminderManager", "Exact alarm permission missing for auto backup", e)
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduled.timeInMillis, pendingIntent)
        }
    }

    fun cancelAutoBackup(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply { action = AUTO_BACKUP_ACTION }
        val pendingIntent = PendingIntent.getBroadcast(
            context, AUTO_BACKUP_REQUEST_CODE, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleReminders(context: Context, task: TaskEntity, vibrate: Boolean, sound: Boolean) {
        val prefs = context.getSharedPreferences("bulletcoach_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("event_reminder_enabled", true)) return

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

    fun scheduleHabitReminder(context: Context, habit: HabitEntity, vibrate: Boolean, sound: Boolean) {
        if (!habit.reminderEnabled || habit.habitTime.isNullOrBlank()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        val hour = habit.habitTime!!.substringBefore(":").toIntOrNull() ?: return
        val minute = habit.habitTime!!.substringAfter(":").toIntOrNull() ?: return

        if (habit.recurrenceMode == "ALWAYS") {
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            if (calendar.timeInMillis <= now) calendar.add(Calendar.DAY_OF_YEAR, 1)

            scheduleAlarm(
                context = context,
                alarmManager = alarmManager,
                timeInMillis = calendar.timeInMillis,
                taskId = habit.id * 100,
                isNightBefore = false,
                title = "Habit Reminder",
                message = "Time to ${habit.name}!",
                vibrate = vibrate,
                sound = sound
            )
        } else if (habit.recurrenceMode == "WEEKLY") {
            val daysOfWeek = habit.recurrenceDaysOfWeek.split(",").filter { it.isNotBlank() }.mapNotNull { it.toIntOrNull() }
            if (daysOfWeek.isEmpty()) return

            var nextAlarmTime: Long? = null
            for (i in 0..7) {
                val checkCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, i)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val dayOfWeek = when (checkCal.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.SUNDAY -> 1
                    Calendar.MONDAY -> 2
                    Calendar.TUESDAY -> 3
                    Calendar.WEDNESDAY -> 4
                    Calendar.THURSDAY -> 5
                    Calendar.FRIDAY -> 6
                    Calendar.SATURDAY -> 7
                    else -> -1
                }
                if (dayOfWeek in daysOfWeek && checkCal.timeInMillis > now) {
                    val endDate = habit.recurrenceEndDate
                    if (endDate != null) {
                        try {
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            val parsedDate = sdf.parse(endDate)
                            if (parsedDate == null) continue
                            val endCal = Calendar.getInstance().apply { time = parsedDate; add(Calendar.DAY_OF_YEAR, 1) }
                            if (checkCal.timeInMillis > endCal.timeInMillis) continue
                        } catch (_: Exception) {}
                    }
                    nextAlarmTime = checkCal.timeInMillis
                    break
                }
            }

            nextAlarmTime?.let { time ->
                scheduleAlarm(
                    context = context,
                    alarmManager = alarmManager,
                    timeInMillis = time,
                    taskId = habit.id * 100,
                    isNightBefore = false,
                    title = "Habit Reminder",
                    message = "Time to ${habit.name}!",
                    vibrate = vibrate,
                    sound = sound
                )
            }
        }
    }

    fun cancelHabitReminder(context: Context, habit: HabitEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, (habit.id * 100).toInt(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }

    suspend fun rescheduleAllAlarms(context: Context) {
        try {
            val database = com.example.core.database.AppDatabase.getDatabase(context)
            val events = database.taskDao().getUpcomingEventsSync()

            val prefs = context.getSharedPreferences("bulletcoach_prefs", Context.MODE_PRIVATE)
            val vibrate = prefs.getBoolean("event_reminder_vibrate", true)
            val sound = prefs.getBoolean("event_reminder_sound", true)

            events.forEach { event ->
                cancelReminders(context, event)
                scheduleReminders(context, event, vibrate, sound)
            }

            val habits = database.habitDao().getAllHabitsSync()
            habits.forEach { habit ->
                cancelHabitReminder(context, habit)
                scheduleHabitReminder(context, habit, vibrate, sound)
            }

            Log.d("ReminderManager", "Successfully rescheduled ${events.size} event alarms and ${habits.size} habit alarms.")
        } catch (e: Exception) {
            Log.e("ReminderManager", "Failed to reschedule alarms", e)
        }
    }
}
