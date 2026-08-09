package com.example.core.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.core.database.AppDatabase
import com.example.core.repository.MottoPicker
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.MainActivity
import com.example.ui.screens.AlarmActivity
import com.example.R

class ReminderReceiver : BroadcastReceiver() {

    private var receiverWakeLock: PowerManager.WakeLock? = null

    private fun acquireWakeLock(context: Context) {
        releaseWakeLock()
        receiverWakeLock = try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyPlanner:ReminderReceiver")
                .also { it.acquire(10000L) }
        } catch (e: Exception) { null }
    }

    private fun releaseWakeLock() {
        try {
            receiverWakeLock?.let { if (it.isHeld) it.release() }
        } catch (_: Exception) { }
        receiverWakeLock = null
    }

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
            acquireWakeLock(context)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    com.example.core.manager.ReminderManager.rescheduleAllAlarms(context)
                    com.example.core.manager.ReminderManager.scheduleAutoBackup(context)

                    val prefs = context.getSharedPreferences("bulletcoach_prefs", Context.MODE_PRIVATE)
                    val reminders = listOf(
                        ReminderConfig("review_reminder_enabled", "review_reminder_time", "21:00", "com.example.action.DAY_REVIEW", 5000),
                        ReminderConfig("sleep_reminder_enabled", "sleep_reminder_time", "09:00", "com.example.action.SLEEP_REMINDER", 6000),
                        ReminderConfig("diary_reminder_enabled", "diary_reminder_time", "20:00", "com.example.action.DIARY_REMINDER", 7000),
                        ReminderConfig("planner_reminder_enabled", "planner_reminder_time", "07:00", "com.example.action.PLANNER_REMINDER", 8000),
                        ReminderConfig("habits_reminder_enabled", "habits_reminder_time", "21:00", "com.example.action.HABITS_REMINDER", 9000),
                        ReminderConfig("tomorrow_planner_reminder_enabled", "tomorrow_planner_reminder_time", "20:00", "com.example.action.TOMORROW_PLANNER_REMINDER", 10000),
                        ReminderConfig("learn_review_reminder_enabled", "learn_review_reminder_time", "19:00", "com.example.action.LEARN_REVIEW_REMINDER", 11000),
                        ReminderConfig("motto_reminder_enabled", "motto_reminder_time", "08:00", "com.example.action.MOTTO_REMINDER", 12000)
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
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    calendar.timeInMillis,
                                    remPendingIntent
                                )
                            } else {
                                alarmManager.setExact(
                                    AlarmManager.RTC_WAKEUP,
                                    calendar.timeInMillis,
                                    remPendingIntent
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    releaseWakeLock()
                    releaseWakeLock()
                        releaseWakeLock()
                        pendingResult.finish()
                }
            }
            return
        }

        when (action) {
            "com.example.action.DAY_REVIEW" -> {
                handleDayReview(context)
                rescheduleDailyReminder(context, action, 5000)
                return
            }
            "com.example.action.SLEEP_REMINDER" -> {
                handleSleepReminder(context)
                rescheduleDailyReminder(context, action, 6000)
                return
            }
            "com.example.action.DIARY_REMINDER" -> {
                handleDiaryReminder(context)
                rescheduleDailyReminder(context, action, 7000)
                return
            }
            "com.example.action.PLANNER_REMINDER" -> {
                handlePlannerReminder(context)
                rescheduleDailyReminder(context, action, 8000)
                return
            }
            "com.example.action.HABITS_REMINDER" -> {
                handleHabitsReminder(context)
                rescheduleDailyReminder(context, action, 9000)
                return
            }
            "com.example.action.TOMORROW_PLANNER_REMINDER" -> {
                handleTomorrowPlannerReminder(context)
                rescheduleDailyReminder(context, action, 10000)
                return
            }
            "com.example.action.LEARN_REVIEW_REMINDER" -> {
                handleLearnReviewReminder(context)
                rescheduleDailyReminder(context, action, 11000)
                return
            }
            "com.example.action.MOTTO_REMINDER" -> {
                handleMottoReminder(context)
                rescheduleDailyReminder(context, action, 12000)
                return
            }
            "com.example.action.SNOOZE_ALARM" -> { showSnoozedAlarm(context, intent); return }
            com.example.core.manager.ReminderManager.AUTO_BACKUP_ACTION -> {
                handleAutoBackup(context)
                return
            }
        }

        val title = intent.getStringExtra("title") ?: "Event Reminder"
        val message = intent.getStringExtra("message") ?: "You have an event coming up."
        val vibrate = intent.getBooleanExtra("vibrate", true)
        val sound = intent.getBooleanExtra("sound", true)
        val taskId = intent.getLongExtra("taskId", 0L)
        val isNightBefore = intent.getBooleanExtra("isNightBefore", false)

        if (isNightBefore) {
            val pendingResult = goAsync()
            acquireWakeLock(context)
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
                    releaseWakeLock()
                    releaseWakeLock()
                        releaseWakeLock()
                        pendingResult.finish()
                }
            }
        } else {
            showNotification(context, title, message, vibrate, sound, taskId, true)
            if (title == "Habit Reminder") {
                val pendingResult = goAsync()
                acquireWakeLock(context)
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
                        releaseWakeLock()
                    releaseWakeLock()
                        releaseWakeLock()
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    private fun handleDayReview(context: Context) {
        val pendingResult = goAsync()
        acquireWakeLock(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return@launch
                }
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
                releaseWakeLock()
                    releaseWakeLock()
                        releaseWakeLock()
                        pendingResult.finish()
            }
        }
    }

    private fun handleSleepReminder(context: Context) {
        val pendingResult = goAsync()
        acquireWakeLock(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return@launch
                }
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
                releaseWakeLock()
                    releaseWakeLock()
                        releaseWakeLock()
                        pendingResult.finish()
            }
        }
    }

    private fun handleDiaryReminder(context: Context) {
        val pendingResult = goAsync()
        acquireWakeLock(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return@launch
                }
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
                releaseWakeLock()
                    releaseWakeLock()
                        releaseWakeLock()
                        pendingResult.finish()
            }
        }
    }

    private fun handlePlannerReminder(context: Context) {
        val pendingResult = goAsync()
        acquireWakeLock(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return@launch
                }
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
                releaseWakeLock()
                    releaseWakeLock()
                        releaseWakeLock()
                        pendingResult.finish()
            }
        }
    }

    private fun handleHabitsReminder(context: Context) {
        val pendingResult = goAsync()
        acquireWakeLock(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return@launch
                }
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = sdf.format(Calendar.getInstance().time)
                val database = AppDatabase.getDatabase(context)
                val allHabits = database.habitDao().getAllHabits().first()
                if (allHabits.isEmpty()) return@launch
                val logs = database.habitDao().getLogsForDate(todayStr).first()
                val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                val missed = allHabits.filter { habit ->
                    val applicable = when (habit.recurrenceMode) {
                        "ALWAYS" -> true
                        "WEEKLY" -> {
                            val days = habit.recurrenceDaysOfWeek
                                .split(",")
                                .mapNotNull { it.trim().toIntOrNull() }
                                .toSet()
                            dayOfWeek in days
                        }
                        else -> false
                    }
                    if (!applicable) return@filter false
                    val beforeEnd = if (habit.recurrenceEndDate != null) {
                        try {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val endDate = sdf.parse(habit.recurrenceEndDate)
                            !sdf.parse(todayStr).after(endDate)
                        } catch (_: Exception) { true }
                    } else true
                    if (!beforeEnd) return@filter false
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
                releaseWakeLock()
                    releaseWakeLock()
                        releaseWakeLock()
                        pendingResult.finish()
            }
        }
    }

    private fun handleTomorrowPlannerReminder(context: Context) {
        val pendingResult = goAsync()
        acquireWakeLock(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return@launch
                }
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
                releaseWakeLock()
                    releaseWakeLock()
                        releaseWakeLock()
                        pendingResult.finish()
            }
        }
    }

    private fun handleLearnReviewReminder(context: Context) {
        val pendingResult = goAsync()
        acquireWakeLock(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return@launch
                }
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = sdf.format(Calendar.getInstance().time)
                val database = AppDatabase.getDatabase(context)
                val tasks = database.taskDao().getTasksForDateSync(todayStr)
                val learnReviewCount = tasks.count { it.linkedLearnSectionId != null && it.label == "Review" }
                val learnStudyCount = tasks.count { it.linkedLearnSectionId != null && it.label == "Study" }
                if (learnReviewCount == 0 && learnStudyCount == 0) return@launch

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        "learn_review_reminder", "Learn Review Reminder",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply { description = "Daily reminder for pending learn reviews" }
                    (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
                }

                val body = buildString {
                    append("You have ")
                    if (learnStudyCount > 0) {
                        append("$learnStudyCount study session")
                        if (learnStudyCount != 1) append("s")
                    }
                    if (learnStudyCount > 0 && learnReviewCount > 0) append(" and ")
                    if (learnReviewCount > 0) {
                        append("$learnReviewCount review")
                        if (learnReviewCount != 1) append("s")
                    }
                    append(" due today")
                }

                val openIntent = Intent(context, com.example.MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("navigate_to_tab", 0)
                }
                val pendingIntent = PendingIntent.getActivity(context, 11000, openIntent, PendingIntent.FLAG_IMMUTABLE)

                val notification = NotificationCompat.Builder(context, "learn_review_reminder")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Learn Reviews Due")
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(11000, notification)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                releaseWakeLock()
                    releaseWakeLock()
                        releaseWakeLock()
                        pendingResult.finish()
            }
        }
    }

    private fun handleMottoReminder(context: Context) {
        val pendingResult = goAsync()
        acquireWakeLock(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return@launch
                }
                val prefs = context.getSharedPreferences("bulletcoach_prefs", Context.MODE_PRIVATE)
                val database = AppDatabase.getDatabase(context)
                val motto = MottoPicker.pickNext(database, prefs) ?: return@launch

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        "motto_reminder", "Daily Motto",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply { description = "Daily motivational quote" }
                    (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
                }

                val body = if (motto.author.isBlank()) motto.text else "${motto.text}\n\n— ${motto.author}"

                val openIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("navigate_to_tab", 4)
                    putExtra("open_more_screen", "Mottos")
                }
                val pendingIntent = PendingIntent.getActivity(context, 12000, openIntent, PendingIntent.FLAG_IMMUTABLE)

                val notification = NotificationCompat.Builder(context, "motto_reminder")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Daily Motto")
                    .setContentText(motto.text)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(12000, notification)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                releaseWakeLock()
                    releaseWakeLock()
                        releaseWakeLock()
                        pendingResult.finish()
            }
        }
    }

    private fun handleAutoBackup(context: Context) {
        val pendingResult = goAsync()
        acquireWakeLock(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                com.example.core.manager.ReminderManager.scheduleAutoBackup(context)
                val request = OneTimeWorkRequestBuilder<com.example.core.manager.BackupWorker>().build()
                WorkManager.getInstance(context).enqueue(request)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                releaseWakeLock()
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, title: String, message: String, vibrate: Boolean, sound: Boolean, taskId: Long, isAlarm: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "event_reminders"
        val prefs = context.getSharedPreferences("bulletcoach_prefs", Context.MODE_PRIVATE)
        val patternName = prefs.getString("pomodoro_vibrate_pattern", "heartbeat") ?: "heartbeat"
        val vibePattern = com.example.ui.viewmodel.MainViewModel.getVibrationPattern(patternName)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(channelId)
            val channel = NotificationChannel(
                channelId,
                "Event Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming events"
                setBypassDnd(true)
                if (vibrate) {
                    enableVibration(true)
                    vibrationPattern = vibePattern
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
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (isAlarm) {
            val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("title", title)
                putExtra("message", message)
                putExtra("vibrate", vibrate)
                putExtra("sound", sound)
            }
            val alarmPendingIntent = PendingIntent.getActivity(context, taskId.toInt() + 1000, alarmIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            builder.setFullScreenIntent(alarmPendingIntent, true)
        }

        if (vibrate && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setVibrate(vibePattern)
        }
        if (sound && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        }

        notificationManager.notify(taskId.toInt(), builder.build())
    }

    private fun showSnoozedAlarm(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Event Reminder"
        val message = intent.getStringExtra("message") ?: "Starting soon"
        val vibrate = intent.getBooleanExtra("vibrate", true)
        val sound = intent.getBooleanExtra("sound", true)

        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("title", title)
            putExtra("message", message)
            putExtra("vibrate", vibrate)
            putExtra("sound", sound)
        }
        val pendingIntent = PendingIntent.getActivity(context, 9999, alarmIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        try {
            pendingIntent.send()
        } catch (e: PendingIntent.CanceledException) {
            e.printStackTrace()
        }
    }

    private fun rescheduleDailyReminder(context: Context, action: String, requestCode: Int) {
        try {
            val prefs = context.getSharedPreferences("bulletcoach_prefs", Context.MODE_PRIVATE)
            val configMap = mapOf(
                "com.example.action.DAY_REVIEW" to Triple("review_reminder_enabled", "review_reminder_time", "21:00"),
                "com.example.action.SLEEP_REMINDER" to Triple("sleep_reminder_enabled", "sleep_reminder_time", "09:00"),
                "com.example.action.DIARY_REMINDER" to Triple("diary_reminder_enabled", "diary_reminder_time", "20:00"),
                "com.example.action.PLANNER_REMINDER" to Triple("planner_reminder_enabled", "planner_reminder_time", "07:00"),
                "com.example.action.HABITS_REMINDER" to Triple("habits_reminder_enabled", "habits_reminder_time", "21:00"),
                "com.example.action.TOMORROW_PLANNER_REMINDER" to Triple("tomorrow_planner_reminder_enabled", "tomorrow_planner_reminder_time", "20:00"),
                "com.example.action.LEARN_REVIEW_REMINDER" to Triple("learn_review_reminder_enabled", "learn_review_reminder_time", "19:00"),
                "com.example.action.MOTTO_REMINDER" to Triple("motto_reminder_enabled", "motto_reminder_time", "08:00")
            )
            val (enabledKey, timeKey, defaultTime) = configMap[action] ?: return
            if (!prefs.getBoolean(enabledKey, false)) return

            val timeStr = prefs.getString(timeKey, defaultTime) ?: defaultTime
            val timeParts = timeStr.split(":")
            val hour = timeParts[0].toIntOrNull() ?: 9
            val minute = timeParts[1].toIntOrNull() ?: 0
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, 1)
            }
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val remIntent = Intent(context, ReminderReceiver::class.java).apply { this.action = action }
            val remPendingIntent = PendingIntent.getBroadcast(
                context, requestCode, remIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, calendar.timeInMillis, remPendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP, calendar.timeInMillis, remPendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
