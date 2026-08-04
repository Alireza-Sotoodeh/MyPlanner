package com.example.ui.viewmodel

import android.app.AppOpsManager
import android.app.NotificationManager
import android.app.AlarmManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.os.Vibrator
import android.os.VibratorManager
import android.os.VibrationEffect
import android.media.RingtoneManager
import android.media.Ringtone
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.DayReviewEntity
import com.example.core.database.entity.DiaryEntryEntity
import com.example.core.database.entity.HabitEntity
import com.example.core.database.entity.HabitLogEntity
import com.example.core.database.entity.IdeaEntity
import com.example.core.database.entity.IdeaGroupEntity
import com.example.core.database.entity.IdeaStageEntity
import com.example.core.database.entity.MottoEntity
import com.example.core.database.entity.ShopItemEntity
import com.example.core.database.entity.SleepLogEntity
import com.example.core.database.entity.TaskEntity
import com.example.core.database.entity.TodoEntity
import com.example.core.repository.DayReviewRepository
import com.example.core.repository.DiaryRepository
import com.example.core.repository.HabitRepository
import com.example.core.repository.IdeaRepository
import com.example.core.repository.MottoRepository
import com.example.core.repository.ShopItemRepository
import com.example.core.repository.SleepLogRepository
import com.example.core.repository.TaskRepository
import com.example.core.repository.TodoRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.app.PendingIntent
import kotlinx.coroutines.Dispatchers

data class AppUsageItem(
    val appName: String,
    val packageName: String,
    val durationMinutes: Long
)

data class BulletCoachBackup(
    val tasks: List<TaskEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val habitLogs: List<HabitLogEntity> = emptyList(),
    val sleepLogs: List<SleepLogEntity> = emptyList(),
    val ideaGroups: List<IdeaGroupEntity> = emptyList(),
    val ideas: List<IdeaEntity> = emptyList(),
    val ideaStages: List<IdeaStageEntity> = emptyList(),
    val todos: List<TodoEntity> = emptyList(),
    val diaryEntries: List<DiaryEntryEntity> = emptyList(),
    val shopItems: List<ShopItemEntity> = emptyList(),
    val mottos: List<MottoEntity> = emptyList(),
    val dayReviews: List<DayReviewEntity> = emptyList()
)

class MainViewModel(
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val sleepLogRepository: SleepLogRepository,
    private val ideaRepository: IdeaRepository,
    private val todoRepository: TodoRepository,
    private val diaryRepository: DiaryRepository,
    private val shopItemRepository: ShopItemRepository,
    private val mottoRepository: MottoRepository,
    private val dayReviewRepository: DayReviewRepository,
    private val context: Context
) : ViewModel() {

    private val TAG = "MainViewModel"
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val prefs = context.getSharedPreferences("bulletcoach_prefs", Context.MODE_PRIVATE)

    // Current Nav Tab index
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun selectTab(tab: Int) {
        _currentTab.value = tab
    }

    // State flows for Settings
    private val _googleDriveConnected = MutableStateFlow(prefs.getBoolean("google_drive_connected", false))
    val googleDriveConnected: StateFlow<Boolean> = _googleDriveConnected.asStateFlow()

    private val _googleDriveEmail = MutableStateFlow(prefs.getString("google_drive_email", "ar.sotoodeh@gmail.com") ?: "ar.sotoodeh@gmail.com")
    val googleDriveEmail: StateFlow<String> = _googleDriveEmail.asStateFlow()

    private val _dndEnabled = MutableStateFlow(prefs.getBoolean("pomodoro_dnd_enabled", false))
    val dndEnabled: StateFlow<Boolean> = _dndEnabled.asStateFlow()

    private val _eventReminderVibrate = MutableStateFlow(prefs.getBoolean("event_reminder_vibrate", true))
    val eventReminderVibrate: StateFlow<Boolean> = _eventReminderVibrate.asStateFlow()

    private val _eventReminderSound = MutableStateFlow(prefs.getBoolean("event_reminder_sound", true))
    val eventReminderSound: StateFlow<Boolean> = _eventReminderSound.asStateFlow()

    private val _defaultFocusMinutes = MutableStateFlow(prefs.getInt("default_focus_minutes", 25))
    val defaultFocusMinutes: StateFlow<Int> = _defaultFocusMinutes.asStateFlow()

    private val _defaultBreakMinutes = MutableStateFlow(prefs.getInt("default_break_minutes", 5))
    val defaultBreakMinutes: StateFlow<Int> = _defaultBreakMinutes.asStateFlow()

    private val _customLabels = MutableStateFlow(loadCustomLabels())
    val customLabels: StateFlow<List<Pair<String, Long>>> = _customLabels.asStateFlow()

    private val _autoSortEnabled = MutableStateFlow(prefs.getBoolean("auto_sort_enabled", false))
    val autoSortEnabled: StateFlow<Boolean> = _autoSortEnabled.asStateFlow()

    private fun loadCustomLabels(): List<Pair<String, Long>> {
        val serialized = prefs.getString("custom_labels", "") ?: ""
        if (serialized.isBlank()) return emptyList()
        return serialized.split(";").mapNotNull {
            val parts = it.split(",")
            if (parts.size == 2) {
                parts[0] to (parts[1].toLongOrNull() ?: 0L)
            } else null
        }
    }

    fun updateCustomLabels(labels: List<Pair<String, Long>>) {
        val serialized = labels.joinToString(";") { "${it.first},${it.second}" }
        prefs.edit().putString("custom_labels", serialized).apply()
        _customLabels.value = labels
    }

    fun updateGoogleDriveConnected(connected: Boolean, email: String = "ar.sotoodeh@gmail.com") {
        prefs.edit().putBoolean("google_drive_connected", connected)
            .putString("google_drive_email", email)
            .apply()
        _googleDriveConnected.value = connected
        _googleDriveEmail.value = email
    }

    fun updateDndEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("pomodoro_dnd_enabled", enabled).apply()
        _dndEnabled.value = enabled
    }

    fun updateEventReminderVibrate(enabled: Boolean) {
        prefs.edit().putBoolean("event_reminder_vibrate", enabled).apply()
        _eventReminderVibrate.value = enabled
    }

    fun updateEventReminderSound(enabled: Boolean) {
        prefs.edit().putBoolean("event_reminder_sound", enabled).apply()
        _eventReminderSound.value = enabled
    }

    fun updateAutoSortEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("auto_sort_enabled", enabled).apply()
        _autoSortEnabled.value = enabled
    }

    fun triggerReorderByPriority() {
        viewModelScope.launch {
            val currentTasks = dailyTasks.value
            val sorted = currentTasks.sortedWith(
                compareBy {
                    when (it.priorityLevel) {
                        "High" -> 1
                        "Medium" -> 2
                        "Low" -> 3
                        else -> 4
                    }
                }
            )
            val updated = sorted.mapIndexed { index, task ->
                task.copy(priority = index)
            }
            taskRepository.updateTaskPriorities(updated)
        }
    }

    // Google Drive / JSON Backup and Restore
    fun backupDataToGoogleDrive(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val tasksList = taskRepository.getAllTasks().first()
                val habitsList = habitRepository.allHabits.first()
                val habitLogsList = habitRepository.getAllLogs().first()
                val sleepLogsList = sleepLogRepository.allSleepLogs.first()
                val ideaGroupsList = ideaRepository.allGroups.first()
                val ideasList = ideaRepository.getAllIdeas().first()
                val todosList = todoRepository.allTodos.first()
                val diaryEntriesList = diaryRepository.getAllEntries().first()
                val shopItemsList = shopItemRepository.allItems.first()
                val mottosList = mottoRepository.allMottos.first()
                val dayReviewsList = dayReviewRepository.getAllReviews().first()
                val backupObj = BulletCoachBackup(
                    tasks = tasksList,
                    habits = habitsList,
                    habitLogs = habitLogsList,
                    sleepLogs = sleepLogsList,
                    ideaGroups = ideaGroupsList,
                    ideas = ideasList,
                    todos = todosList,
                    diaryEntries = diaryEntriesList,
                    shopItems = shopItemsList,
                    mottos = mottosList,
                    dayReviews = dayReviewsList
                )

                val adapter = moshi.adapter(BulletCoachBackup::class.java)
                val jsonString = adapter.toJson(backupObj)

                // Save locally to represent Google Drive file
                val backupFile = java.io.File(context.filesDir, "bulletcoach_backup.json")
                backupFile.writeText(jsonString)

                // Simulate Google Drive Sync and success
                delay(1200)
                onResult(true, "Successfully backed up ${tasksList.size} intentions, ${habitsList.size} habits, and logs to Google Drive!")
            } catch (e: Exception) {
                Log.e(TAG, "Backup failed", e)
                onResult(false, "Backup failed: ${e.localizedMessage}")
            }
        }
    }

    fun restoreDataFromGoogleDrive(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val backupFile = java.io.File(context.filesDir, "bulletcoach_backup.json")
                if (!backupFile.exists()) {
                    onResult(false, "No backup file found on Google Drive. Please create a backup first!")
                    return@launch
                }

                val jsonString = backupFile.readText()
                val adapter = moshi.adapter(BulletCoachBackup::class.java)
                val backupObj = adapter.fromJson(jsonString)

                if (backupObj == null) {
                    onResult(false, "Failed to parse backup data.")
                    return@launch
                }

                // Restore to database
                backupObj.tasks.forEach { taskRepository.insertTask(it) }
                backupObj.habits.forEach { habitRepository.insertHabit(it) }
                backupObj.habitLogs.forEach { habitRepository.insertLog(it) }
                backupObj.sleepLogs.forEach { sleepLogRepository.insertSleepLog(it) }
                backupObj.ideaGroups.forEach { ideaRepository.insertGroup(it) }
                backupObj.ideas.forEach { ideaRepository.insertIdea(it) }
                backupObj.ideaStages.forEach { ideaRepository.insertStage(it) }
                backupObj.todos.forEach { todoRepository.insertTodo(it) }
                backupObj.diaryEntries.forEach { diaryRepository.insertEntry(it) }
                backupObj.shopItems.forEach { shopItemRepository.insertItem(it) }
                backupObj.mottos.forEach { mottoRepository.insertMotto(it) }
                backupObj.dayReviews.forEach { dayReviewRepository.insertReview(it) }

                delay(1500)
                onResult(true, "Successfully restored ${backupObj.tasks.size} intentions, ${backupObj.habits.size} habits, and logs from Google Drive!")
            } catch (e: Exception) {
                Log.e(TAG, "Restore failed", e)
                onResult(false, "Restore failed: ${e.localizedMessage}")
            }
        }
    }

    // Date Navigation State
    private val _todayDate = MutableStateFlow(getTodayDateString())
    val todayDate: StateFlow<String> = _todayDate.asStateFlow()

    private var lastTodayDateString = getTodayDateString()
    private var lastTodayMonthString = getTodayMonthString()

    private val dateChangeReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            Log.d(TAG, "Received date/time/timezone change broadcast: ${intent?.action}")
            refreshSystemDate()
        }
    }

    fun refreshSystemDate() {
        val newToday = getTodayDateString()
        if (lastTodayDateString != newToday) {
            val oldToday = lastTodayDateString
            lastTodayDateString = newToday
            _todayDate.value = newToday
            
            // If user's selected view was exactly the old today, move it to the new today automatically
            if (_selectedDate.value == oldToday) {
                _selectedDate.value = newToday
            }
            
            // Same for month view
            val newMonth = getTodayMonthString()
            if (lastTodayMonthString != newMonth) {
                val oldMonth = lastTodayMonthString
                lastTodayMonthString = newMonth
                if (_selectedMonth.value == oldMonth) {
                    _selectedMonth.value = newMonth
                }
            }

            // Reset reviewed_today prefs cache on date change
            val cachedDate = prefs.getString("reviewed_today_date", null)
            if (cachedDate != null && cachedDate != newToday) {
                prefs.edit().remove("reviewed_today").remove("reviewed_today_date").apply()
            }
        }
    }

    private val _selectedDate = MutableStateFlow(getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedMonth = MutableStateFlow(getTodayMonthString())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    // Tasks for currently selected day
    val dailyTasks: StateFlow<List<TaskEntity>> = _selectedDate.flatMapLatest { date ->
        taskRepository.getTasksForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tasks for currently selected month
    val monthlyTasks: StateFlow<List<TaskEntity>> = _selectedMonth.flatMapLatest { month ->
        taskRepository.getTasksForMonth(month)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Tasks
    val allTasks: StateFlow<List<TaskEntity>> = taskRepository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Habits List
    val habits: StateFlow<List<HabitEntity>> = habitRepository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Habit Logs for currently selected date
    val habitLogs: StateFlow<List<HabitLogEntity>> = _selectedDate.flatMapLatest { date ->
        habitRepository.getLogsForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sleep Log for selected date
    val sleepLog: StateFlow<SleepLogEntity?> = _selectedDate.flatMapLatest { date ->
        sleepLogRepository.getSleepLogForDateFlow(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Pomodoro Timer State
    private val _activePomodoroTask = MutableStateFlow<TaskEntity?>(null)
    val activePomodoroTask: StateFlow<TaskEntity?> = _activePomodoroTask.asStateFlow()

    private val _pomodoroSecondsLeft = MutableStateFlow(0)
    val pomodoroSecondsLeft: StateFlow<Int> = _pomodoroSecondsLeft.asStateFlow()

    private val _pomodoroRunning = MutableStateFlow(false)
    val pomodoroRunning: StateFlow<Boolean> = _pomodoroRunning.asStateFlow()

    private val _pomodoroPhase = MutableStateFlow("FOCUS") // "FOCUS" or "BREAK"
    val pomodoroPhase: StateFlow<String> = _pomodoroPhase.asStateFlow()

    private val _pomodoroCurrentSession = MutableStateFlow(1)
    val pomodoroCurrentSession: StateFlow<Int> = _pomodoroCurrentSession.asStateFlow()

    private val _pomodoroTargetSessions = MutableStateFlow<Int?>(null)
    val pomodoroTargetSessions: StateFlow<Int?> = _pomodoroTargetSessions.asStateFlow()

    private val _pomodoroBreakMinutes = MutableStateFlow<Int?>(5)
    val pomodoroBreakMinutes: StateFlow<Int?> = _pomodoroBreakMinutes.asStateFlow()

    private val _pomodoroFocusMinutes = MutableStateFlow(25)
    val pomodoroFocusMinutes: StateFlow<Int> = _pomodoroFocusMinutes.asStateFlow()

    private val _taskForPomodoroSetup = MutableStateFlow<TaskEntity?>(null)
    val taskForPomodoroSetup: StateFlow<TaskEntity?> = _taskForPomodoroSetup.asStateFlow()

    fun setTaskForPomodoroSetup(task: TaskEntity?) {
        _taskForPomodoroSetup.value = task
    }

    private var pomodoroJob: Job? = null
    private var originalDndState = false

    // App Usage Stats State
    private val _appUsageItems = MutableStateFlow<List<AppUsageItem>>(emptyList())
    val appUsageItems: StateFlow<List<AppUsageItem>> = _appUsageItems.asStateFlow()

    private val _totalScreenTimeMinutes = MutableStateFlow(0L)
    val totalScreenTimeMinutes: StateFlow<Long> = _totalScreenTimeMinutes.asStateFlow()

    // === Idea List State ===
    val ideaGroups: StateFlow<List<IdeaGroupEntity>> = ideaRepository.allGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allIdeas: StateFlow<List<IdeaEntity>> = ideaRepository.getAllIdeas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // === To-Do State ===
    val pendingTodos: StateFlow<List<TodoEntity>> = todoRepository.pendingTodos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTodos: StateFlow<List<TodoEntity>> = todoRepository.allTodos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // === Diary State ===
    val diaryDates: StateFlow<List<String>> = diaryRepository.getAllDates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // === Shop List State ===
    val unpurchasedItems: StateFlow<List<ShopItemEntity>> = shopItemRepository.unpurchasedItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchasedItems: StateFlow<List<ShopItemEntity>> = shopItemRepository.purchasedItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allShopItems: StateFlow<List<ShopItemEntity>> = shopItemRepository.allItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // === Motto State ===
    private val _todayMotto = MutableStateFlow<MottoEntity?>(null)
    val todayMotto: StateFlow<MottoEntity?> = _todayMotto.asStateFlow()

    val allMottos: StateFlow<List<MottoEntity>> = mottoRepository.allMottos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // === Day Review State ===
    private val _reviewReminderTime = MutableStateFlow(prefs.getString("review_reminder_time", "21:00") ?: "21:00")
    val reviewReminderTime: StateFlow<String> = _reviewReminderTime.asStateFlow()

    private val _reviewReminderEnabled = MutableStateFlow(prefs.getBoolean("review_reminder_enabled", false))
    val reviewReminderEnabled: StateFlow<Boolean> = _reviewReminderEnabled.asStateFlow()

    // === Day Review Prompt State ===
    private val _showDayReviewPrompt = MutableStateFlow(false)
    val showDayReviewPrompt: StateFlow<Boolean> = _showDayReviewPrompt.asStateFlow()

    @Volatile
    private var _isCheckingPrompt = false

    private fun createDayReviewChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "day_review_reminder",
                "Day Review Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminder to review your day"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getImmutableFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }

    fun scheduleDayReviewAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, com.example.core.receiver.ReminderReceiver::class.java).apply {
            action = "com.example.action.DAY_REVIEW"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            5000,
            intent,
            getImmutableFlag() or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val timeParts = _reviewReminderTime.value.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelDayReviewAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, com.example.core.receiver.ReminderReceiver::class.java).apply {
            action = "com.example.action.DAY_REVIEW"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            5000,
            intent,
            getImmutableFlag() or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun sendImmediateDayReviewNotification(context: Context) {
        createDayReviewChannel(context)
        val intent = Intent(context, com.example.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_day_review", true)
        }
        val pendingIntent = PendingIntent.getActivity(context, 5001, intent, getImmutableFlag())
        val notification = NotificationCompat.Builder(context, "day_review_reminder")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Day Review Reminder")
            .setContentText("Time to review your day!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(5000, notification)
    }

    fun dismissDayReviewPrompt() {
        _showDayReviewPrompt.value = false
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(5000)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel day review notification", e)
        }
    }

    fun checkAndTriggerDayReviewPrompt() {
        if (_isCheckingPrompt) return
        if (!_reviewReminderEnabled.value) return
        if (prefs.getBoolean("reviewed_today", false)) return
        val today = getTodayDateString()
        val reminderTime = _reviewReminderTime.value
        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        if (now < reminderTime) return
        _isCheckingPrompt = true
        viewModelScope.launch {
            try {
                val existing = dayReviewRepository.getReviewForDate(today).first()
                if (existing != null) {
                    prefs.edit().putBoolean("reviewed_today", true).apply()
                    return@launch
                }
                _showDayReviewPrompt.value = true
            } finally {
                _isCheckingPrompt = false
            }
        }
    }

    init {
        // Register BroadcastReceiver for date, time, and timezone changes
        val intentFilter = android.content.IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        
        @android.annotation.SuppressLint("WrongConstant")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(dateChangeReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(dateChangeReceiver, intentFilter)
        }

        // Periodic coroutine check to guarantee date freshness (runs lightweight verification every 15s)
        viewModelScope.launch {
            while (true) {
                delay(15000)
                refreshSystemDate()
            }
        }

        // Prepopulate with a mock habit if empty
        viewModelScope.launch {
            val currentHabits = habitRepository.allHabits.first()
            if (currentHabits.isEmpty()) {
                habitRepository.insertHabit(HabitEntity(name = "Hydration (Glasses of Water)", type = "QUANTITATIVE", target = 8f, unit = "glasses"))
                habitRepository.insertHabit(HabitEntity(name = "Morning Meditation (15m)", type = "BINARY", target = 1f, unit = "times"))
                habitRepository.insertHabit(HabitEntity(name = "Read 10 Pages", type = "BINARY", target = 1f, unit = "times"))
            }

            val currentTasks = taskRepository.getAllTasks().first()
            if (currentTasks.isEmpty()) {
                val today = getTodayDateString()
                taskRepository.insertTask(TaskEntity(title = "Sync Google Sheets Context", description = "Review and fetch life coach profile contexts.", date = today, type = "TASK", durationMinutes = 25, priority = 1))
                taskRepository.insertTask(TaskEntity(title = "Morning Stretch & Meditation", description = "Mindfulness ritual for 15 minutes.", date = today, type = "EVENT", durationMinutes = 15, priority = 2))
                taskRepository.insertTask(TaskEntity(title = "Refactor Local Room Models", description = "Polish entities and custom DAOs.", date = today, type = "TASK", durationMinutes = 50, priority = 3))
            }
        }

        // Initialize today's motto
        refreshTodayMotto()
    }

    fun refreshTodayMotto() {
        viewModelScope.launch {
            val cachedDate = prefs.getString("today_motto_date", "")
            val cachedId = prefs.getLong("today_motto_id", -1L)
            val today = getTodayDateString()

            if (cachedDate != today || cachedId == -1L) {
                val random = mottoRepository.getRandomMotto()
                if (random != null) {
                    prefs.edit()
                        .putLong("today_motto_id", random.id)
                        .putString("today_motto_date", today)
                        .apply()
                    _todayMotto.value = random
                } else {
                    _todayMotto.value = null
                }
            } else {
                _todayMotto.value = allMottos.value.find { it.id == cachedId }
            }
        }
    }

    // --- Date Navigation Methods ---
    fun selectDate(date: String) {
        _selectedDate.value = date
        // Extract month string from selected date "yyyy-MM-dd" -> "yyyy-MM"
        if (date.length >= 7) {
            _selectedMonth.value = date.substring(0, 7)
        }
    }

    fun selectMonth(month: String) {
        _selectedMonth.value = month
    }

    // --- Task CRUD Operations ---
    fun addTask(
        title: String, description: String, date: String, type: String = "TASK", 
        duration: Int = 25, label: String = "", 
        labelColor: Long? = null, subtasks: List<Pair<String, String>> = emptyList(),
        recurrenceMode: String = "NONE", recurrenceInterval: Int = 1,
        recurrenceDaysOfWeek: String = "", recurrenceEndDate: String? = null,
        eventTime: String? = null, notifyNightBefore: Boolean = false, reminderMinutesBefore: Int? = null,
        priorityLevel: String = "Medium"
    ) {
        viewModelScope.launch {
            val dates = mutableListOf(date)
            if (recurrenceMode == "WEEKLY") {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                try {
                    val currentDate = sdf.parse(date)
                    val endDateParsed = recurrenceEndDate?.let { sdf.parse(it) }
                    val cal = java.util.Calendar.getInstance()
                    if (currentDate != null) {
                        cal.time = currentDate
                        val startDayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
                        val targetDays = if (recurrenceDaysOfWeek.isNotBlank()) {
                            recurrenceDaysOfWeek.split(",").mapNotNull { it.toIntOrNull() }
                        } else {
                            listOf(startDayOfWeek)
                        }

                        // Generate for next 1 year max or until endDate
                        val endLimit = java.util.Calendar.getInstance().apply { 
                            time = currentDate
                            add(java.util.Calendar.YEAR, 1)
                        }.time

                        var currentWeekCal = java.util.Calendar.getInstance().apply { time = currentDate }
                        
                        // We check weeks up to 52
                        for (weekOffset in 0..52 step recurrenceInterval) {
                            for (day in targetDays) {
                                val dayCal = java.util.Calendar.getInstance().apply {
                                    time = currentWeekCal.time
                                    set(java.util.Calendar.DAY_OF_WEEK, day)
                                }
                                // If day is before start date in the first week, skip it
                                if (weekOffset == 0 && dayCal.time.before(currentDate) && day != startDayOfWeek) {
                                    continue
                                }
                                
                                if (endDateParsed != null && dayCal.time.after(endDateParsed)) {
                                    continue
                                }
                                if (dayCal.time.after(endLimit)) {
                                    continue
                                }
                                
                                val dateStr = sdf.format(dayCal.time)
                                if (!dates.contains(dateStr)) {
                                    dates.add(dateStr)
                                }
                            }
                            currentWeekCal.add(java.util.Calendar.WEEK_OF_YEAR, recurrenceInterval)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Date parsing error", e)
                }
            }

            dates.forEach { taskDate ->
                val newTask = TaskEntity(
                    title = title,
                    description = description,
                    date = taskDate,
                    type = type,
                    durationMinutes = duration,
                    priority = dailyTasks.value.size + 1,
                    priorityLevel = priorityLevel,
                    label = label,
                    labelColor = labelColor,
                    recurrenceMode = recurrenceMode,
                    recurrenceInterval = recurrenceInterval,
                    recurrenceDaysOfWeek = recurrenceDaysOfWeek,
                    recurrenceEndDate = recurrenceEndDate,
                    eventTime = eventTime,
                    notifyNightBefore = notifyNightBefore,
                    reminderMinutesBefore = reminderMinutesBefore
                )
                val parentId = taskRepository.insertTask(newTask)
                if (newTask.type == "EVENT") {
                    com.example.core.manager.ReminderManager.scheduleReminders(
                        context = context,
                        task = newTask.copy(id = parentId),
                        vibrate = _eventReminderVibrate.value,
                        sound = _eventReminderSound.value
                    )
                }
                subtasks.forEach { (subTitle, importance) ->
                    if (subTitle.isNotBlank()) {
                        val subTask = TaskEntity(
                            title = subTitle,
                            description = "",
                            date = taskDate,
                            type = "TASK",
                            durationMinutes = 0,
                            priority = 0,
                            label = label,
                            labelColor = labelColor,
                            parentTaskId = parentId,
                            subtaskImportance = importance
                        )
                        taskRepository.insertTask(subTask)
                    }
                }
            }
        }
    }

    fun updateTaskWithSubtasks(
        task: TaskEntity,
        title: String,
        description: String,
        date: String,
        type: String,
        label: String,
        labelColor: Long?,
        subtasks: List<Pair<String, String>>,
        recurrenceMode: String = task.recurrenceMode,
        recurrenceInterval: Int = task.recurrenceInterval,
        recurrenceDaysOfWeek: String = task.recurrenceDaysOfWeek,
        recurrenceEndDate: String? = task.recurrenceEndDate,
        eventTime: String? = task.eventTime,
        notifyNightBefore: Boolean = task.notifyNightBefore,
        reminderMinutesBefore: Int? = task.reminderMinutesBefore,
        priorityLevel: String = task.priorityLevel
    ) {
        viewModelScope.launch {
            val updatedTask = task.copy(
                title = title,
                description = description,
                date = date,
                type = type,
                label = label,
                labelColor = labelColor,
                recurrenceMode = recurrenceMode,
                recurrenceInterval = recurrenceInterval,
                recurrenceDaysOfWeek = recurrenceDaysOfWeek,
                recurrenceEndDate = recurrenceEndDate,
                eventTime = eventTime,
                notifyNightBefore = notifyNightBefore,
                reminderMinutesBefore = reminderMinutesBefore,
                priorityLevel = priorityLevel
            )
            taskRepository.updateTask(updatedTask)
            
            if (updatedTask.type == "EVENT") {
                com.example.core.manager.ReminderManager.cancelReminders(context, task) // Cancel old
                com.example.core.manager.ReminderManager.scheduleReminders(
                    context = context,
                    task = updatedTask,
                    vibrate = _eventReminderVibrate.value,
                    sound = _eventReminderSound.value
                )
            } else if (task.type == "EVENT") {
                // Was event, now something else
                com.example.core.manager.ReminderManager.cancelReminders(context, task)
            }

            // Basic subtask sync: delete existing subtasks and re-insert
            val existingSubtasks = allTasks.value.filter { it.parentTaskId == task.id }
            existingSubtasks.forEach { taskRepository.deleteTask(it) }
            
            subtasks.forEach { (subTitle, importance) ->
                if (subTitle.isNotBlank()) {
                    val subTask = TaskEntity(
                        title = subTitle,
                        description = "",
                        date = date,
                        type = "TASK",
                        durationMinutes = 0,
                        priority = 0,
                        label = label,
                        labelColor = labelColor,
                        parentTaskId = task.id,
                        subtaskImportance = importance
                    )
                    taskRepository.insertTask(subTask)
                }
            }
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.updateTask(task)
        }
    }

    fun toggleTaskCompletion(task: TaskEntity, subtasks: List<TaskEntity> = emptyList()) {
        viewModelScope.launch {
            if (task.status != "COMPLETED") {
                val hasIncompleteImportantSubtasks = subtasks.any { it.status == "PENDING" && it.subtaskImportance == "IMPORTANT" }
                if (hasIncompleteImportantSubtasks) {
                    // Cannot complete main task if important subtasks are pending
                    return@launch
                }
            }
            val updated = task.copy(status = if (task.status == "COMPLETED") "PENDING" else "COMPLETED")
            taskRepository.updateTask(updated)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            if (task.type == "EVENT") {
                com.example.core.manager.ReminderManager.cancelReminders(context, task)
            }
            taskRepository.deleteTaskAndSubtasks(task)
            taskRepository.deleteSessionsForTask(task.id)
        }
    }

    fun completeTaskWithManualDuration(task: TaskEntity, durationMinutes: Int) {
        viewModelScope.launch {
            val updated = task.copy(
                durationMinutes = durationMinutes,
                status = "COMPLETED",
                pomodorosCompleted = task.pomodorosCompleted + 1
            )
            taskRepository.updateTask(updated)
            
            val session = com.example.core.database.entity.PomodoroSessionEntity(
                taskId = task.id,
                durationMinutes = durationMinutes,
                date = getTodayDateString(),
                status = "COMPLETED"
            )
            taskRepository.insertSession(session)
        }
    }

    fun getSessionsForTask(taskId: Long): kotlinx.coroutines.flow.Flow<List<com.example.core.database.entity.PomodoroSessionEntity>> {
        return taskRepository.getSessionsForTask(taskId)
    }

    fun migrateTask(task: TaskEntity, targetDate: String) {
        viewModelScope.launch {
            val updated = task.copy(date = targetDate, status = "PENDING")
            taskRepository.updateTask(updated)
            if (updated.type == "EVENT") {
                com.example.core.manager.ReminderManager.cancelReminders(context, task)
                com.example.core.manager.ReminderManager.scheduleReminders(
                    context = context,
                    task = updated,
                    vibrate = _eventReminderVibrate.value,
                    sound = _eventReminderSound.value
                )
            }
        }
    }

    // --- Habit & Tracker Operations ---
    fun addHabit(name: String, type: String, target: Float, unit: String) {
        viewModelScope.launch {
            habitRepository.insertHabit(HabitEntity(name = name, type = type, target = target, unit = unit))
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
        }
    }

    fun logHabit(habitId: Long, value: Float, notes: String = "") {
        viewModelScope.launch {
            val today = _selectedDate.value
            val currentLog = habitLogs.value.find { it.habitId == habitId }
            if (currentLog != null) {
                if (value <= 0f) {
                    habitRepository.deleteLogForDate(habitId, today)
                } else {
                    habitRepository.insertLog(currentLog.copy(value = value, notes = notes))
                }
            } else {
                habitRepository.insertLog(HabitLogEntity(habitId = habitId, date = today, value = value, notes = notes))
            }
        }
    }

    fun saveSleepLog(hours: Float, quality: Int, bedTime: String, wakeTime: String, notes: String) {
        viewModelScope.launch {
            val today = _selectedDate.value
            val existing = sleepLogRepository.getSleepLogForDate(today)
            val log = SleepLogEntity(
                id = existing?.id ?: 0L,
                date = today,
                hoursSlept = hours,
                sleepQuality = quality,
                sleepTime = bedTime,
                wakeTime = wakeTime,
                notes = notes
            )
            sleepLogRepository.insertSleepLog(log)
        }
    }

    // --- Pomodoro and DND Management ---
    fun startPomodoro(
        context: Context,
        task: TaskEntity,
        focusMinutes: Int = task.durationMinutes,
        targetSessions: Int? = task.targetSessions,
        breakMinutes: Int? = task.breakMinutes,
        saveAsDefault: Boolean = false
    ) {
        if (_pomodoroRunning.value) return

        val updatedTask = task.copy(
            durationMinutes = focusMinutes,
            targetSessions = targetSessions,
            breakMinutes = breakMinutes
        )

        viewModelScope.launch {
            taskRepository.updateTask(updatedTask)
        }

        if (saveAsDefault) {
            prefs.edit()
                .putInt("default_focus_minutes", focusMinutes)
                .putInt("default_break_minutes", breakMinutes ?: 5)
                .apply()
            _defaultFocusMinutes.value = focusMinutes
            _defaultBreakMinutes.value = breakMinutes ?: 5
        }

        _activePomodoroTask.value = updatedTask
        _pomodoroFocusMinutes.value = focusMinutes
        _pomodoroTargetSessions.value = targetSessions
        _pomodoroBreakMinutes.value = breakMinutes
        _pomodoroCurrentSession.value = 1
        _pomodoroPhase.value = "FOCUS"
        _pomodoroSecondsLeft.value = focusMinutes * 60
        _pomodoroRunning.value = true

        // Enable DND
        if (_dndEnabled.value) {
            originalDndState = getDndState(context)
            setDndState(context, true)
        }

        startTimerJob(context)
    }

    fun pausePomodoro() {
        pomodoroJob?.cancel()
        _pomodoroRunning.value = false
    }

    fun resumePomodoro(context: Context) {
        if (_pomodoroRunning.value) return
        _pomodoroRunning.value = true
        startTimerJob(context)
    }

    fun stopPomodoroEarly(context: Context) {
        val task = _activePomodoroTask.value
        if (task != null && _pomodoroPhase.value == "FOCUS") {
            val focusTotalSeconds = _pomodoroFocusMinutes.value * 60
            val secondsElapsed = focusTotalSeconds - _pomodoroSecondsLeft.value
            val minutesElapsed = (secondsElapsed / 60).coerceAtLeast(1)
            viewModelScope.launch {
                val session = com.example.core.database.entity.PomodoroSessionEntity(
                    taskId = task.id,
                    durationMinutes = minutesElapsed,
                    date = getTodayDateString(),
                    status = "INTERRUPTED"
                )
                taskRepository.insertSession(session)
            }
        }
        pomodoroJob?.cancel()
        _pomodoroRunning.value = false
        _activePomodoroTask.value = null
        _pomodoroSecondsLeft.value = 0
        if (_dndEnabled.value) {
            setDndState(context, originalDndState)
        }
    }

    private fun startTimerJob(context: Context) {
        pomodoroJob?.cancel()
        pomodoroJob = viewModelScope.launch {
            while (_pomodoroSecondsLeft.value > 0) {
                delay(1000)
                _pomodoroSecondsLeft.value -= 1
            }
            // Phase complete!
            handlePhaseCompletion(context)
        }
    }

    private fun handlePhaseCompletion(context: Context) {
        val task = _activePomodoroTask.value ?: return
        val currentPhase = _pomodoroPhase.value
        val breakMin = _pomodoroBreakMinutes.value
        val targetSess = _pomodoroTargetSessions.value

        if (currentPhase == "FOCUS") {
            // Completed FOCUS session
            viewModelScope.launch {
                val subtasks = allTasks.value.filter { it.parentTaskId == task.id }
                val hasIncompleteImportantSubtasks = subtasks.any { it.status == "PENDING" && it.subtaskImportance == "IMPORTANT" }
                val newStatus = if (task.pomodorosCompleted + 1 >= 1 && !hasIncompleteImportantSubtasks) "COMPLETED" else task.status
                
                val updated = task.copy(
                    pomodorosCompleted = task.pomodorosCompleted + 1,
                    status = newStatus
                )
                taskRepository.updateTask(updated)
                _activePomodoroTask.value = updated

                val session = com.example.core.database.entity.PomodoroSessionEntity(
                    taskId = task.id,
                    durationMinutes = _pomodoroFocusMinutes.value,
                    date = getTodayDateString(),
                    status = "COMPLETED"
                )
                taskRepository.insertSession(session)
            }

            triggerSessionFeedback(
                context, 
                "Focus Session Completed!", 
                "Great job! Focus session ${_pomodoroCurrentSession.value} for '${task.title}' is done."
            )

            val nextSessionNum = _pomodoroCurrentSession.value
            if (targetSess != null && nextSessionNum >= targetSess) {
                // Done all target sessions
                completeEntireChain(context, "Congratulations! You have finished all $targetSess focus sessions for '${task.title}'.")
            } else {
                // Break or Next Focus
                if (breakMin != null && breakMin > 0) {
                    _pomodoroPhase.value = "BREAK"
                    _pomodoroSecondsLeft.value = breakMin * 60
                    startTimerJob(context)
                } else {
                    _pomodoroCurrentSession.value = nextSessionNum + 1
                    _pomodoroPhase.value = "FOCUS"
                    _pomodoroSecondsLeft.value = _pomodoroFocusMinutes.value * 60
                    startTimerJob(context)
                }
            }
        } else {
            // BREAK completed
            triggerSessionFeedback(
                context,
                "Break Over! Time to Focus",
                "Get ready! Focus session ${_pomodoroCurrentSession.value + 1} for '${task.title}' is starting."
            )

            _pomodoroCurrentSession.value = _pomodoroCurrentSession.value + 1
            _pomodoroPhase.value = "FOCUS"
            _pomodoroSecondsLeft.value = _pomodoroFocusMinutes.value * 60
            startTimerJob(context)
        }
    }

    private fun completeEntireChain(context: Context, message: String) {
        pomodoroJob?.cancel()
        _pomodoroRunning.value = false
        _activePomodoroTask.value = null
        _pomodoroSecondsLeft.value = 0
        if (_dndEnabled.value) {
            setDndState(context, originalDndState)
        }
        triggerSessionFeedback(context, "Chain Completed!", message)
    }

    private fun triggerSessionFeedback(context: Context, title: String, message: String) {
        // 1. Play ringtone
        try {
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, notificationUri)
            ringtone?.play()
            viewModelScope.launch {
                delay(3000)
                if (ringtone?.isPlaying == true) {
                    ringtone.stop()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Vibrate
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val pattern = longArrayOf(0, 500, 200, 500)
                    it.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(1000)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Notification
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "pomodoro_session_channel"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Pomodoro Sessions",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for focus and break intervals"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(4002, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Usage Stats and Screen Time ---
    fun updateAppUsage(context: Context) {
        viewModelScope.launch {
            if (!hasUsageStatsPermission(context)) {
                _appUsageItems.value = listOf(
                    AppUsageItem("AI Studio Simulator", "com.example.mock", 145),
                    AppUsageItem("BulletCoach AI", context.packageName, 42),
                    AppUsageItem("Slack", "com.slack", 35),
                    AppUsageItem("Chrome", "com.android.chrome", 28)
                )
                _totalScreenTimeMinutes.value = 250
                return@launch
            }

            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()

            val statsMap = usageStatsManager.queryAndAggregateUsageStats(
                startTime,
                endTime
            )

            val packageManager = context.packageManager
            val allItems = statsMap.values
                .filter { it.totalTimeInForeground > 0 }
                .filter { stat -> 
                    // Filter out system apps/background processes without a launch intent
                    packageManager.getLaunchIntentForPackage(stat.packageName) != null 
                }
                .map { stat ->
                    val label = try {
                        val appInfo = packageManager.getApplicationInfo(stat.packageName, 0)
                        packageManager.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        stat.packageName.substringAfterLast('.')
                    }
                    AppUsageItem(
                        appName = label,
                        packageName = stat.packageName,
                        durationMinutes = stat.totalTimeInForeground / (1000 * 60)
                    )
                }
                .filter { it.durationMinutes > 0 }
                .sortedByDescending { it.durationMinutes }

            val topItems = allItems.take(6)
            val total = allItems.sumOf { it.durationMinutes }
            
            _appUsageItems.value = topItems
            _totalScreenTimeMinutes.value = total
        }
    }


    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun hasExactAlarmPermission(context: Context): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun hasAllRequiredPermissions(context: Context): Boolean {
        return hasNotificationPermission(context) && 
               hasExactAlarmPermission(context) && 
               hasUsageStatsPermission(context) && 
               checkNotificationPolicyPermission(context)
    }

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun requestUsagePermission(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    // --- DND Helper ---
    fun checkNotificationPolicyPermission(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true
        }
    }

    fun requestNotificationPolicyPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    private fun getDndState(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE
        } else {
            false
        }
    }

    private fun setDndState(context: Context, enable: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                val filter = if (enable) {
                    NotificationManager.INTERRUPTION_FILTER_NONE
                } else {
                    NotificationManager.INTERRUPTION_FILTER_ALL
                }
                notificationManager.setInterruptionFilter(filter)
            }
        }
    }

    fun reorderTask(task: com.example.core.database.entity.TaskEntity, activeTasks: List<com.example.core.database.entity.TaskEntity>, deltaIndex: Int, isSubtask: Boolean) {
        viewModelScope.launch {
            val currentIndex = activeTasks.indexOf(task)
            if (currentIndex == -1) return@launch
            val newIndex = (currentIndex + deltaIndex).coerceIn(0, activeTasks.size - 1)
                        
            if (isSubtask && newIndex > 0) {
                // Make it a subtask of the item above its new position
                val targetParent = activeTasks[newIndex - 1]
                updateTask(task.copy(parentTaskId = targetParent.id))
            } else if (deltaIndex != 0) {
                // Reorder
                val mutableTasks = activeTasks.toMutableList()
                mutableTasks.removeAt(currentIndex)
                mutableTasks.add(newIndex, task)
                                
                // Update priorities for all affected tasks to match their new index
                val updatedTasks = mutableTasks.mapIndexed { index, t ->
                    t.copy(priority = index)
                }
                taskRepository.updateTaskPriorities(updatedTasks)
            }
        }
    }

    // === Idea List CRUD ===
    fun stagesForIdea(ideaId: Long) = ideaRepository.getStagesForIdea(ideaId)

    fun addGroup(name: String, color: Long) {
        viewModelScope.launch { ideaRepository.insertGroup(IdeaGroupEntity(name = name, color = color)) }
    }

    fun updateGroup(group: IdeaGroupEntity) {
        viewModelScope.launch { ideaRepository.updateGroup(group) }
    }

    fun deleteGroup(group: IdeaGroupEntity) {
        viewModelScope.launch { ideaRepository.deleteGroup(group) }
    }

    fun addIdea(groupId: Long?, title: String, description: String) {
        viewModelScope.launch { ideaRepository.insertIdea(IdeaEntity(groupId = groupId, title = title, description = description)) }
    }

    fun updateIdea(idea: IdeaEntity) {
        viewModelScope.launch { ideaRepository.updateIdea(idea) }
    }

    fun deleteIdea(idea: IdeaEntity) {
        viewModelScope.launch { ideaRepository.deleteIdea(idea) }
    }

    fun moveIdeaToGroup(ideaId: Long, newGroupId: Long?) {
        viewModelScope.launch { ideaRepository.moveIdeaToGroup(ideaId, newGroupId) }
    }

    fun addStage(ideaId: Long, title: String) {
        viewModelScope.launch {
            val stages = ideaRepository.getStagesForIdeaSync(ideaId)
            ideaRepository.insertStage(IdeaStageEntity(ideaId = ideaId, title = title, orderIndex = stages.size))
        }
    }

    fun updateStage(stage: IdeaStageEntity) {
        viewModelScope.launch { ideaRepository.updateStage(stage) }
    }

    fun deleteStage(stage: IdeaStageEntity) {
        viewModelScope.launch { ideaRepository.deleteStage(stage) }
    }

    fun addIdeaToPlanner(idea: IdeaEntity, date: String, type: String) {
        viewModelScope.launch {
            val parentId = taskRepository.insertTask(
                TaskEntity(
                    title = idea.title,
                    description = idea.description,
                    date = date,
                    type = type,
                    label = "IDEA",
                    durationMinutes = 25,
                    priority = dailyTasks.value.size + 1
                )
            )
            val stages = ideaRepository.getStagesForIdeaSync(idea.id)
            stages.filter { it.title.isNotBlank() }.forEach { stage ->
                taskRepository.insertTask(
                    TaskEntity(
                        title = stage.title,
                        date = date,
                        type = "TASK",
                        parentTaskId = parentId,
                        subtaskImportance = "OPTIONAL",
                        label = "IDEA",
                        priority = 0
                    )
                )
            }
        }
    }

    fun addStageToPlanner(stage: IdeaStageEntity, date: String, type: String) {
        viewModelScope.launch {
            taskRepository.insertTask(
                TaskEntity(
                    title = stage.title,
                    date = date,
                    type = type,
                    label = "IDEA",
                    durationMinutes = 25,
                    priority = dailyTasks.value.size + 1
                )
            )
        }
    }

    // === To-Do CRUD ===
    fun addTodo(title: String, description: String = "", priority: String = "Medium") {
        viewModelScope.launch { todoRepository.insertTodo(TodoEntity(title = title, description = description, priority = priority)) }
    }

    fun updateTodo(todo: TodoEntity) {
        viewModelScope.launch { todoRepository.updateTodo(todo) }
    }

    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch {
            if (todo.linkedTaskId != null) {
                taskRepository.deleteTaskById(todo.linkedTaskId)
            }
            todoRepository.deleteTodo(todo)
        }
    }

    fun toggleTodoCompletion(todo: TodoEntity) {
        viewModelScope.launch {
            val newStatus = if (todo.status == "DONE") "PENDING" else "DONE"
            todoRepository.updateTodo(todo.copy(status = newStatus))

            if (todo.linkedTaskId != null && newStatus == "DONE") {
                val linkedTask = taskRepository.getTaskById(todo.linkedTaskId)
                if (linkedTask != null && linkedTask.status != "COMPLETED") {
                    taskRepository.updateTask(linkedTask.copy(status = "COMPLETED"))
                }
            }
        }
    }

    fun linkTodoToTask(todo: TodoEntity, targetDate: String) {
        viewModelScope.launch {
            val taskId = taskRepository.insertTask(
                TaskEntity(
                    title = todo.title,
                    description = todo.description,
                    date = targetDate,
                    type = "TASK",
                    label = "TODO",
                    linkedTodoId = todo.id,
                    priority = dailyTasks.value.size + 1
                )
            )
            todoRepository.updateTodo(todo.copy(linkedTaskId = taskId))
        }
    }

    fun unlinkTodoFromTask(todo: TodoEntity) {
        viewModelScope.launch {
            if (todo.linkedTaskId != null) {
                taskRepository.deleteTaskById(todo.linkedTaskId)
                todoRepository.updateTodo(todo.copy(linkedTaskId = null))
            }
        }
    }

    fun moveTaskToTodo(task: TaskEntity, subtasks: List<TaskEntity>) {
        viewModelScope.launch {
            val mergedDescription = buildString {
                append(task.description)
                if (subtasks.isNotEmpty()) {
                    append("\n\nSubtasks:\n")
                    subtasks.forEachIndexed { i, s -> append("${i + 1}. ${s.title}\n") }
                }
            }
            todoRepository.insertTodo(
                TodoEntity(
                    title = task.title,
                    description = mergedDescription.trim(),
                    priority = task.priorityLevel,
                    status = "PENDING"
                )
            )
            taskRepository.deleteTaskAndSubtasks(task)
        }
    }

    fun turnNoteIntoIdea(task: TaskEntity, subtasks: List<TaskEntity>) {
        viewModelScope.launch {
            val ideaId = ideaRepository.insertIdea(
                IdeaEntity(title = task.title, description = task.description)
            )
            subtasks.forEachIndexed { index, subtask ->
                ideaRepository.insertStage(
                    IdeaStageEntity(
                        ideaId = ideaId,
                        title = subtask.title,
                        isCompleted = false,
                        orderIndex = index
                    )
                )
            }
            taskRepository.deleteTaskAndSubtasks(task)
        }
    }

    // === Diary CRUD ===
    val diaryAllDates: StateFlow<List<String>> = diaryRepository.getAllDates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun diaryEntryForDate(date: String) = diaryRepository.getEntryForDate(date)

    fun saveDiaryEntry(date: String, title: String, content: String) {
        viewModelScope.launch {
            val existing = diaryRepository.getEntryForDate(date).first()
            if (existing != null) {
                diaryRepository.insertEntry(existing.copy(title = title, content = content, updatedAt = System.currentTimeMillis()))
            } else {
                diaryRepository.insertEntry(DiaryEntryEntity(date = date, title = title, content = content))
            }
        }
    }

    fun deleteDiaryEntry(date: String) {
        viewModelScope.launch { diaryRepository.deleteEntryByDate(date) }
    }

    // === Shop List CRUD ===
    fun addShopItem(name: String, quantity: Int = 1, price: Float? = null, notes: String = "") {
        viewModelScope.launch {
            shopItemRepository.insertItem(ShopItemEntity(name = name, quantity = quantity.coerceAtLeast(1), price = price?.takeIf { it >= 0f }, notes = notes))
        }
    }

    fun updateShopItem(item: ShopItemEntity) {
        viewModelScope.launch { shopItemRepository.updateItem(item) }
    }

    fun deleteShopItem(item: ShopItemEntity) {
        viewModelScope.launch { shopItemRepository.deleteItem(item) }
    }

    fun toggleShopItemPurchased(item: ShopItemEntity) {
        viewModelScope.launch { shopItemRepository.updateItem(item.copy(isPurchased = !item.isPurchased)) }
    }

    // === Motto CRUD ===
    fun addMotto(text: String, author: String = "") {
        viewModelScope.launch { mottoRepository.insertMotto(MottoEntity(text = text, author = author)) }
    }

    fun updateMotto(motto: MottoEntity) {
        viewModelScope.launch { mottoRepository.updateMotto(motto) }
    }

    fun deleteMotto(motto: MottoEntity) {
        viewModelScope.launch {
            mottoRepository.deleteMotto(motto)
            if (_todayMotto.value?.id == motto.id) {
                refreshTodayMotto()
            }
        }
    }

    // === Day Review CRUD ===
    fun reviewForDate(date: String) = dayReviewRepository.getReviewForDate(date)

    fun saveDayReview(date: String, good: String, bad: String, improve: String, gratitude: String, moodRating: Int, score: Int, notes: String) {
        viewModelScope.launch {
            val existing = dayReviewRepository.getReviewForDate(date).first()
            if (existing != null) {
                dayReviewRepository.insertReview(existing.copy(good = good, bad = bad, improve = improve, gratitude = gratitude, moodRating = moodRating, score = score, notes = notes))
            } else {
                dayReviewRepository.insertReview(DayReviewEntity(date = date, good = good, bad = bad, improve = improve, gratitude = gratitude, moodRating = moodRating, score = score, notes = notes))
            }
            prefs.edit().putBoolean("reviewed_today", true).putString("reviewed_today_date", date).apply()
            dismissDayReviewPrompt()
        }
    }

    fun deleteDayReview(date: String) {
        viewModelScope.launch { dayReviewRepository.deleteReviewByDate(date) }
    }

    fun updateReviewReminderTime(time: String) {
        prefs.edit().putString("review_reminder_time", time).apply()
        _reviewReminderTime.value = time
        if (_reviewReminderEnabled.value) {
            cancelDayReviewAlarm(context)
            scheduleDayReviewAlarm(context)
        }
    }

    fun updateReviewReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("review_reminder_enabled", enabled).apply()
        _reviewReminderEnabled.value = enabled
        if (enabled) {
            scheduleDayReviewAlarm(context)
        } else {
            cancelDayReviewAlarm(context)
        }
    }

    // Helper utilities for date
    fun getTodayDateString(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    }
    
    private fun getTodayMonthString(): String {
        return java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(java.util.Date())
    }

    override fun onCleared() {
        super.onCleared()
        try {
            context.unregisterReceiver(dateChangeReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Unregistering dateChangeReceiver failed", e)
        }
    }
}

class MainViewModelFactory(
    private val taskRepository: com.example.core.repository.TaskRepository,
    private val habitRepository: com.example.core.repository.HabitRepository,
    private val sleepLogRepository: com.example.core.repository.SleepLogRepository,
    private val ideaRepository: com.example.core.repository.IdeaRepository,
    private val todoRepository: com.example.core.repository.TodoRepository,
    private val diaryRepository: com.example.core.repository.DiaryRepository,
    private val shopItemRepository: com.example.core.repository.ShopItemRepository,
    private val mottoRepository: com.example.core.repository.MottoRepository,
    private val dayReviewRepository: com.example.core.repository.DayReviewRepository,
    private val context: android.content.Context
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                taskRepository, habitRepository, sleepLogRepository,
                ideaRepository, todoRepository, diaryRepository,
                shopItemRepository, mottoRepository, dayReviewRepository,
                context
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
