package com.example.core.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.core.database.AppDatabase
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.MainActivity
import com.example.ui.screens.AlarmActivity
import com.example.R

class ReminderReceiver : BroadcastReceiver() {

    private data class ReminderConfig(
        val prefsEnabledKey: String,
        val prefsTimeKey: String,
        val defaultTime: String,
        val action: String,
        val requestCode: Int
    )

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || 
            action == Intent.ACTION_TIME_CHANGED || 
            action == Intent.ACTION_TIMEZONE_CHANGED || 
            action == "android.intent.action.TIME_SET" || 
            action == Intent.ACTION_LOCALE_CHANGED) {
            
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    com.example.core.manager.ReminderManager.rescheduleAllAlarms(context)

                    val prefs = context.getSharedPreferences("bulletcoach_prefs", Context.MODE_PRIVATE)
                    val reminders = listOf(
                        ReminderConfig("review_reminder_enabled", "review_reminder_time", "21:00", "com.example.action.DAY_REVIEW", 5000),
                        ReminderConfig("sleep_reminder_enabled", "sleep_reminder_time", "09:00", "com.example.action.SLEEP_REMINDER", 6000),
                        ReminderConfig("diary_reminder_enabled", "diary_reminder_time", "20:00", "com.example.action.DIARY_REMINDER", 7000),
                        ReminderConfig("planner_reminder_enabled", "planner_reminder_time", "07:00", "com.example.action.PLANNER_REMINDER", 8000),
                        ReminderConfig("habits_reminder_enabled", "habits_reminder_time", "21:00", "com.example.action.HABITS_REMINDER", 9000),
                        ReminderConfig("tomorrow_planner_reminder_enabled", "tomorrow_planner_reminder_time", "20:00", "com.example.action.TOMORROW_PLANNER_REMINDER", 10000)
                    )
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                    for (cfg in reminders) {
                        if (prefs.getBoolean(cfg.prefsEnabledKey, false)) {
                            val timeStr = prefs.getString(cfg.prefsTimeKey, cfg.defaultTime) ?: cfg.defaultTime
                            val timeParts = timeStr.split(":")
                            val hour = timeParts[0].toIntOrNull() ?: 9
                            val minute = timeParts[1].toIntOrNull() ?: 0
                            val calendar = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, hour)
                                set(Calendar.MINUTE, minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                                if (before(Calendar.getInstance())) {
                                    add(Calendar.DAY_OF_YEAR, 1)
                                }
                            }
                            val remIntent = Intent(context, ReminderReceiver::class.java).apply {
                                this.action = cfg.action
                            }
                            val remPendingIntent = PendingIntent.getBroadcast(
                                context, cfg.requestCode, remIntent,
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            )
                            alarmManager.setRepeating(
                                AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                AlarmManager.INTERVAL_DAY,
                                remPendingIntent
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        when (action) {
            "com.example.action.DAY_REVIEW" -> { handleDayReview(context); return }
            "com.example.action.SLEEP_REMINDER" -> { handleSleepReminder(context); return }
            "com.example.action.DIARY_REMINDER" -> { handleDiaryReminder(context); return }
            "com.example.action.PLANNER_REMINDER" -> { handlePlannerReminder(context); return }
            "com.example.action.HABITS_REMINDER" -> { handleHabitsReminder(context); return }
            "com.example.action.TOMORROW_PLANNER_REMINDER" -> { handleTomorrowPlannerReminder(context); return }
        }

        val title = intent.getStringExtra("title") ?: "Event Reminder"
        val message = intent.getStringExtra("message") ?: "You have an event coming up."
        val vibrate = intent.getBooleanExtra("vibrate", true)
        val sound = intent.getBooleanExtra("sound", true)
        val taskId = intent.getLongExtra("taskId", 0L)
        val isNightBefore = intent.getBooleanExtra("isNightBefore", false)

        if (isNightBefore) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    val tomorrowStr = sdf.format(cal.time)

                    val database = AppDatabase.getDatabase(context)
                    val tasks = database.taskDao().getTasksForDateSync(tomorrowStr)
                    val nightTasks = tasks.filter { it.type == "EVENT" && it.notifyNightBefore }

                    if (nightTasks.isNotEmpty()) {
                        val names = nightTasks.joinToString(", ") { it.title }
                        showNotification(context, "Tomorrow's Events", "You have: $names", vibrate, sound, 9999L, false)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        } else {
            showNotification(context, title, message, vibrate, sound, taskId, true)
            if (title == "Habit Reminder") {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val database = AppDatabase.getDatabase(context)
                        val habitId = taskId / 100
                        val habit = database.habitDao().getHabitById(habitId)
                        if (habit != null) {
                            com.example.core.manager.ReminderManager.cancelHabitReminder(context, habit)
                            com.example.core.manager.ReminderManager.scheduleHabitReminder(context, habit, vibrate, sound)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    private fun handleDayReview(context: Context) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        "day_review_reminder",
                        "Day Review Reminder",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Daily reminder to review your day"
                    }
                    notificationManager.createNotificationChannel(channel)
                }

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = sdf.format(Calendar.getInstance().time)
                val database = AppDatabase.getDatabase(context)
                val existing = database.dayReviewDao().getReviewForDate(todayStr).first()
                if (existing != null) return@launch

                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("navigate_to_tab", 4)
                    putExtra("open_more_screen", "DayReview")
                }
                val pendingIntent = PendingIntent.getActivity(context, 5000, openIntent, PendingIntent.FLAG_IMMUTABLE)

                val notification = NotificationCompat.Builder(context, "day_review_reminder")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Day Review Reminder")
                    .setContentText("Time to review your day!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(5000, notification)

                context.sendBroadcast(Intent("com.example.action.DAY_REVIEW_TRIGGERED"))
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleSleepReminder(context: Context) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = sdf.format(Calendar.getInstance().time)
                val database = AppDatabase.getDatabase(context)
                val existing = database.sleepLogDao().getSleepLogForDate(todayStr)
                if (existing != null) return@launch

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        "sleep_reminder", "Sleep Log Reminder",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply { description = "Daily reminder to log your sleep" }
                    (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
                }

                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("navigate_to_tab", 1)
                }
                val pendingIntent = PendingIntent.getActivity(context, 6000, openIntent, PendingIntent.FLAG_IMMUTABLE)

                val notification = NotificationCompat.Builder(context, "sleep_reminder")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Sleep Log Reminder")
                    .setContentText("Did you log your sleep last night?")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(6000, notification)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleDiaryReminder(context: Context) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = sdf.format(Calendar.getInstance().time)
                val database = AppDatabase.getDatabase(context)
                val existing = database.diaryDao().getEntryForDate(todayStr).first()
                if (existing != null) return@launch

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        "diary_reminder", "Diary Reminder",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply { description = "Daily reminder to write in your diary" }
                    (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
                }

                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("navigate_to_tab", 4)
                    putExtra("open_more_screen", "Diary")
                }
                val pendingIntent = PendingIntent.getActivity(context, 7000, openIntent, PendingIntent.FLAG_IMMUTABLE)

                val notification = NotificationCompat.Builder(context, "diary_reminder")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Diary Reminder")
                    .setContentText("Write about your day — capture your thoughts")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(7000, notification)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handlePlannerReminder(context: Context) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = sdf.format(Calendar.getInstance().time)
                val database = AppDatabase.getDatabase(context)
                val tasks = database.taskDao().getTasksForDateSync(todayStr)
                val taskCount = tasks.count { it.type == "TASK" }
                val eventCount = tasks.count { it.type == "EVENT" }
                val body = buildString {
                    append("Good morning! You have ")
                    if (tasks.isEmpty()) {
                        append("nothing planned today")
                    } else {
                        append("$taskCount task")
                        if (taskCount != 1) append("s")
                        append(" and $eventCount event")
                        if (eventCount != 1) append("s")
                        append(" today")
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        "planner_reminder", "Morning Planner Reminder",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply { description = "Daily morning summary of your day" }
                    (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
                }

                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("navigate_to_tab", 0)
                }
                val pendingIntent = PendingIntent.getActivity(context, 8000, openIntent, PendingIntent.FLAG_IMMUTABLE)

                val notification = NotificationCompat.Builder(context, "planner_reminder")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Morning Planner Summary")
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(8000, notification)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleHabitsReminder(context: Context) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = sdf.format(Calendar.getInstance().time)
                val database = AppDatabase.getDatabase(context)
                val allHabits = database.habitDao().getAllHabits().first()
                if (allHabits.isEmpty()) return@launch
                val logs = database.habitDao().getLogsForDate(todayStr).first()
                val missed = allHabits.filter { habit ->
                    val log = logs.find { it.habitId == habit.id }
                    log == null || log.value < habit.target
                }
                if (missed.isEmpty()) return@launch
                val body = buildString {
                    append("You missed:\n")
                    missed.take(5).forEachIndexed { i, h ->
                        val log = logs.find { it.habitId == h.id }
                        if (i > 0) append("\n")
                        append("• ${h.name}")
                        if (log != null && h.type == "QUANTITATIVE") {
                            append(" (${log.value.toInt()}/${h.target.toInt()})")
                        }
                    }
                    if (missed.size > 5) {
                        append("\n• and ${missed.size - 5} more")
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        "habits_reminder", "Habits Check-in Reminder",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply { description = "Daily reminder to check your habits" }
                    (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
                }

                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("navigate_to_tab", 1)
                }
                val pendingIntent = PendingIntent.getActivity(context, 9000, openIntent, PendingIntent.FLAG_IMMUTABLE)

                val notification = NotificationCompat.Builder(context, "habits_reminder")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Habits Check-in")
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(9000, notification)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleTomorrowPlannerReminder(context: Context) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, 1)
                val tomorrowStr = sdf.format(cal.time)
                val database = AppDatabase.getDatabase(context)
                val tasks = database.taskDao().getTasksForDateSync(tomorrowStr)
                val taskCount = tasks.count { it.type == "TASK" }
                val eventCount = tasks.count { it.type == "EVENT" }
                val body = buildString {
                    append("Plan tomorrow — ")
                    if (tasks.isEmpty()) {
                        append("nothing scheduled yet. Add your tasks!")
                    } else {
                        append("$taskCount task")
                        if (taskCount != 1) append("s")
                        append(" and $eventCount event")
                        if (eventCount != 1) append("s")
                        append(" coming up")
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        "tomorrow_planner_reminder", "Tomorrow Planner Reminder",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply { description = "Evening reminder to plan tomorrow" }
                    (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
                }

                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("navigate_to_tab", 0)
                    putExtra("open_date", tomorrowStr)
                }
                val pendingIntent = PendingIntent.getActivity(context, 10000, openIntent, PendingIntent.FLAG_IMMUTABLE)

                val notification = NotificationCompat.Builder(context, "tomorrow_planner_reminder")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Plan Tomorrow")
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(10000, notification)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, title: String, message: String, vibrate: Boolean, sound: Boolean, taskId: Long, isAlarm: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "event_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Event Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming events"
                if (vibrate) {
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 200, 500)
                }
                if (sound) {
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, taskId.toInt(), intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (isAlarm) {
            val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("title", title)
                putExtra("message", message)
            }
            val alarmPendingIntent = PendingIntent.getActivity(context, taskId.toInt() + 1000, alarmIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            builder.setFullScreenIntent(alarmPendingIntent, true)
        }

        if (vibrate && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setVibrate(longArrayOf(0, 500, 200, 500))
        }
        if (sound && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        }

        notificationManager.notify(taskId.toInt(), builder.build())
    }
}
