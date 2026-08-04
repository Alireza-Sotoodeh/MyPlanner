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
import com.example.core.database.entity.TimerSessionEntity
import com.example.core.database.entity.TimerTemplateEntity
import com.example.core.database.entity.TodoEntity
import com.example.core.repository.DayReviewRepository
import com.example.core.repository.DiaryRepository
import com.example.core.repository.HabitRepository
import com.example.core.repository.IdeaRepository
import com.example.core.repository.MottoRepository
import com.example.core.repository.ShopItemRepository
import com.example.core.repository.SleepLogRepository
import com.example.core.repository.TaskRepository
import com.example.core.repository.TimerRepository
import com.example.core.repository.TodoRepository
import com.example.core.utils.PersianCalendarHelper
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

sealed class UndoSnapshot(val typeLabel: String) {
    data class TaskSnapshot(
        val task: TaskEntity,
        val subtasks: List<TaskEntity>,
        val linkedTodoId: Long?,
    ) : UndoSnapshot(
        when (task.type) {
            "EVENT" -> "Event"
            "NOTE" -> "Note"
            else -> "Task"
        }
    )
    data class TodoSnapshot(
        val todo: TodoEntity,
        val linkedTask: TaskEntity?,
        val linkedSubtasks: List<TaskEntity>,
    ) : UndoSnapshot("To-Do")
    data class IdeaSnapshot(
        val idea: IdeaEntity,
        val stages: List<IdeaStageEntity>,
    ) : UndoSnapshot("Idea")
}

data class UndoEntry(
    val id: Long,
    val snapshot: UndoSnapshot,
    val message: String,
    val expiryTime: Long,
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
    private val timerRepository: TimerRepository,
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

    // Undo stack for deleted items
    private val _undoStack = MutableStateFlow<List<UndoEntry>>(emptyList())
    val undoStack: StateFlow<List<UndoEntry>> = _undoStack.asStateFlow()

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

    private val _mottoEnabled = MutableStateFlow(prefs.getBoolean("motto_enabled", true))
    val mottoEnabled: StateFlow<Boolean> = _mottoEnabled.asStateFlow()

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

    fun updateMottoEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("motto_enabled", enabled).apply()
        _mottoEnabled.value = enabled
    }

    fun triggerReorderByPriority() {
        viewModelScope.launch {
            val currentTasks = dailyTasks.value
            val sorted = currentTasks.sortedWith(
                compareBy(
                    { if (it.postponed) 0 else 1 },
                    {
                        when (it.priorityLevel) {
                            "High" -> 1
                            "Medium" -> 2
                            "Low" -> 3
                            else -> 4
                        }
                    }
                )
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
                val ideaStagesList = allIdeas.value.flatMap { ideaRepository.getStagesForIdeaSync(it.id) }
                val backupObj = BulletCoachBackup(
                    tasks = tasksList,
                    habits = habitsList,
                    habitLogs = habitLogsList,
                    sleepLogs = sleepLogsList,
                    ideaGroups = ideaGroupsList,
                    ideas = ideasList,
                    ideaStages = ideaStagesList,
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
    private var lastTodayYearString = getTodayYearString()
    private var lastTodayPersianYear = PersianCalendarHelper.getCurrentPersianYear()
    private var lastTodayPersianMonth = PersianCalendarHelper.getCurrentPersianMonth()

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

            // Same for year view
            val newYear = getTodayYearString()
            if (lastTodayYearString != newYear) {
                val oldYear = lastTodayYearString
                lastTodayYearString = newYear
                if (_selectedYear.value == oldYear) {
                    _selectedYear.value = newYear
                }
            }

            // Same for Persian calendar
            val newPersianParts = PersianCalendarHelper.getPersianDateParts(newToday)
            if (_persianYear.value == lastTodayPersianYear && _persianMonth.value == lastTodayPersianMonth) {
                _persianYear.value = newPersianParts.first
                _persianMonth.value = newPersianParts.second
            }
            lastTodayPersianYear = newPersianParts.first
            lastTodayPersianMonth = newPersianParts.second

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

    private val _selectedYear = MutableStateFlow(getTodayYearString())
    val selectedYear: StateFlow<String> = _selectedYear.asStateFlow()

    // Persian Calendar Toggle
    private val _usePersianCalendar = MutableStateFlow(prefs.getBoolean("use_persian_calendar", false))
    val usePersianCalendar: StateFlow<Boolean> = _usePersianCalendar.asStateFlow()

    private val _persianYear = MutableStateFlow(PersianCalendarHelper.getCurrentPersianYear())
    val persianYear: StateFlow<Int> = _persianYear.asStateFlow()

    private val _persianMonth = MutableStateFlow(PersianCalendarHelper.getCurrentPersianMonth())
    val persianMonth: StateFlow<Int> = _persianMonth.asStateFlow()

    // Tasks for currently selected day
    val dailyTasks: StateFlow<List<TaskEntity>> = _selectedDate.flatMapLatest { date ->
        taskRepository.getTasksForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tasks for currently selected month
    val monthlyTasks: StateFlow<List<TaskEntity>> = _selectedMonth.flatMapLatest { month ->
        taskRepository.getTasksForMonth(month)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tasks for currently selected year (used in YearOverviewView)
    val yearTasks: StateFlow<List<TaskEntity>> = _selectedYear.flatMapLatest { year ->
        taskRepository.getTasksForYear(year)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Persian month tasks — queries by Persian month's Gregorian date range
    val persianMonthTasks: StateFlow<List<TaskEntity>> = combine(_persianYear, _persianMonth) { year, month ->
        PersianCalendarHelper.getGregorianDateRange(year, month)
    }.flatMapLatest { (start, end) ->
        taskRepository.getTasksForDateRange(start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Persian year tasks — full Gregorian range of the Persian year
    val persianYearTasks: StateFlow<List<TaskEntity>> = _persianYear.flatMapLatest { year ->
        val start = PersianCalendarHelper.getGregorianDateString(year, 1, 1)
        val end = PersianCalendarHelper.getGregorianDateString(year + 1, 1, 1)
        taskRepository.getTasksForDateRange(start, end)
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

    private val _pomodoroMarkCompleteOnFinish = MutableStateFlow(false)
    val pomodoroMarkCompleteOnFinish: StateFlow<Boolean> = _pomodoroMarkCompleteOnFinish.asStateFlow()

    private val _pomodoroShortBreakMinutes = MutableStateFlow<Int?>(5)
    val pomodoroShortBreakMinutes: StateFlow<Int?> = _pomodoroShortBreakMinutes.asStateFlow()

    private val _pomodoroLongBreakMinutes = MutableStateFlow<Int?>(null)
    val pomodoroLongBreakMinutes: StateFlow<Int?> = _pomodoroLongBreakMinutes.asStateFlow()

    // Chronometer State
    private val _chronoElapsed = MutableStateFlow(0L)
    val chronoElapsed: StateFlow<Long> = _chronoElapsed.asStateFlow()

    private val _chronoRunning = MutableStateFlow(false)
    val chronoRunning: StateFlow<Boolean> = _chronoRunning.asStateFlow()

    private val _chronoPaused = MutableStateFlow(false)
    val chronoPaused: StateFlow<Boolean> = _chronoPaused.asStateFlow()

    private var chronoJob: Job? = null

    private var _chronoSelectedTaskId = MutableStateFlow<Long?>(null)
    val chronoSelectedTaskId: StateFlow<Long?> = _chronoSelectedTaskId.asStateFlow()

    // Timer Templates
    val timerTemplates: StateFlow<List<TimerTemplateEntity>> = timerRepository.getAllTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTimerSessions: StateFlow<List<TimerSessionEntity>> = timerRepository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Timer Sessions (history)
    fun timerSessionsForDateRange(startDate: String, endDate: String): StateFlow<List<TimerSessionEntity>> =
        timerRepository.getSessionsForDateRange(startDate, endDate)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pre-selected task for Timer (from Planner)
    private val _preSelectedTaskForTimer = MutableStateFlow<Long?>(null)
    val preSelectedTaskForTimer: StateFlow<Long?> = _preSelectedTaskForTimer.asStateFlow()

    fun setPreSelectedTaskForTimer(taskId: Long?) {
        _preSelectedTaskForTimer.value = taskId
        if (taskId != null) {
            _currentTab.value = 2
        }
    }

    fun consumePreSelectedTask(): Long? {
        val id = _preSelectedTaskForTimer.value
        _preSelectedTaskForTimer.value = null
        return id
    }

    private val _preferredTimerTab = MutableStateFlow<Int?>(null)
    val preferredTimerTab: StateFlow<Int?> = _preferredTimerTab.asStateFlow()

    fun setPreferredTimerTab(tab: Int?) {
        _preferredTimerTab.value = tab
    }

    fun consumePreferredTimerTab(): Int? {
        val tab = _preferredTimerTab.value
        _preferredTimerTab.value = null
        return tab
    }

    private var pomodoroJob: Job? = null
    private var originalDndState = false

    // App Usage Stats State
    private val _appUsageItems = MutableStateFlow<List<AppUsageItem>>(emptyList())
    val appUsageItems: StateFlow<List<AppUsageItem>> = _appUsageItems.asStateFlow()

    private val _totalScreenTimeMinutes = MutableStateFlow(0L)
    val totalScreenTimeMinutes: StateFlow<Long> = _totalScreenTimeMinutes.asStateFlow()

    private val _screenTimeError = MutableStateFlow<String?>(null)
    val screenTimeError: StateFlow<String?> = _screenTimeError.asStateFlow()

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

    fun refreshRandomMotto() {
        viewModelScope.launch {
            _todayMotto.value = mottoRepository.getRandomMotto()
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

    fun selectYear(year: String) {
        _selectedYear.value = year
    }

    fun toggleUsePersianCalendar() {
        val newValue = !_usePersianCalendar.value
        if (newValue) {
            _persianYear.value = PersianCalendarHelper.getCurrentPersianYear()
            _persianMonth.value = PersianCalendarHelper.getCurrentPersianMonth()
        } else {
            _selectedMonth.value = getTodayMonthString()
        }
        _usePersianCalendar.value = newValue
        prefs.edit().putBoolean("use_persian_calendar", newValue).apply()
    }

    fun navigateMonth(delta: Int) {
        if (_usePersianCalendar.value) {
            val (newYear, newMonth) = PersianCalendarHelper.getOffsetPersianMonth(_persianYear.value, _persianMonth.value, delta)
            _persianYear.value = newYear
            _persianMonth.value = newMonth
        } else {
            _selectedMonth.value = getOffsetMonthString(_selectedMonth.value, delta)
        }
    }

    fun selectPersianMonth(year: Int, month: Int) {
        _persianYear.value = year
        _persianMonth.value = month
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

            updatedTask.linkedTodoId?.let { todoId ->
                todoRepository.getTodoById(todoId)?.let { linkedTodo ->
                    if (linkedTodo.title != title || linkedTodo.description != description) {
                        todoRepository.updateTodo(linkedTodo.copy(title = title, description = description))
                    }
                }
            }

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

        updated.linkedTodoId?.let { todoId ->
            val linkedTodo = todoRepository.getTodoById(todoId)
            if (linkedTodo != null) {
                val newTodoStatus = if (updated.status == "COMPLETED") "DONE" else "PENDING"
                if (linkedTodo.status != newTodoStatus) {
                    todoRepository.updateTodo(linkedTodo.copy(status = newTodoStatus))
                }
            }
        }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            try {
                if (task.type == "EVENT") {
                    com.example.core.manager.ReminderManager.cancelReminders(context, task)
                }
                task.linkedTodoId?.let { todoId ->
                    todoRepository.getTodoById(todoId)?.let { linkedTodo ->
                        todoRepository.updateTodo(linkedTodo.copy(linkedTaskId = null))
                    }
                }
                taskRepository.deleteTaskAndSubtasks(task)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete task", e)
            }
        }
    }

    fun deleteTaskWithUndo(task: TaskEntity) {
        viewModelScope.launch {
            try {
                val subtasks = taskRepository.getSubtasks(task.id)
                val linkedTodoId = task.linkedTodoId
                deleteTask(task)
                pushUndo(
                    UndoSnapshot.TaskSnapshot(task, subtasks, linkedTodoId),
                    task.title
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete task with undo", e)
            }
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

            updated.linkedTodoId?.let { todoId ->
                val linkedTodo = todoRepository.getTodoById(todoId)
                if (linkedTodo != null && linkedTodo.status != "DONE") {
                    todoRepository.updateTodo(linkedTodo.copy(status = "DONE"))
                }
            }

            timerRepository.insertSession(
                TimerSessionEntity(
                    type = "POMODORO",
                    taskId = task.id,
                    label = task.label,
                    durationSeconds = durationMinutes * 60,
                    date = getTodayDateString()
                )
            )
        }
    }

    fun migrateTask(task: TaskEntity, targetDate: String, postpone: Boolean = true) {
        viewModelScope.launch {
            val updated = if (postpone) {
                task.copy(date = targetDate, status = "PENDING", postponed = true)
            } else {
                task.copy(date = targetDate, status = "PENDING")
            }
            taskRepository.updateTask(updated)
            val subtasks = taskRepository.getSubtasks(task.id)
            subtasks.forEach { sub ->
                taskRepository.updateTask(sub.copy(date = targetDate, status = "PENDING"))
            }
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
    fun addHabit(
        name: String, type: String, target: Float, unit: String,
        recurrenceMode: String = "ALWAYS", recurrenceInterval: Int = 1,
        recurrenceDaysOfWeek: String = "", recurrenceEndDate: String? = null,
        habitTime: String? = null, reminderEnabled: Boolean = false
    ) {
        viewModelScope.launch {
            val newId = habitRepository.insertHabit(
                HabitEntity(
                    name = name, type = type, target = target, unit = unit,
                    recurrenceMode = recurrenceMode, recurrenceInterval = recurrenceInterval,
                    recurrenceDaysOfWeek = recurrenceDaysOfWeek, recurrenceEndDate = recurrenceEndDate,
                    habitTime = habitTime, reminderEnabled = reminderEnabled
                )
            )
            if (reminderEnabled && !habitTime.isNullOrBlank()) {
                com.example.core.manager.ReminderManager.scheduleHabitReminder(
                    context = context,
                    habit = HabitEntity(
                        id = newId, name = name, type = type, target = target, unit = unit,
                        recurrenceMode = recurrenceMode, recurrenceInterval = recurrenceInterval,
                        recurrenceDaysOfWeek = recurrenceDaysOfWeek, recurrenceEndDate = recurrenceEndDate,
                        habitTime = habitTime, reminderEnabled = reminderEnabled
                    ),
                    vibrate = _eventReminderVibrate.value,
                    sound = _eventReminderSound.value
                )
            }
        }
    }

    fun updateHabit(habit: HabitEntity) {
        viewModelScope.launch {
            habitRepository.updateHabit(habit)
            com.example.core.manager.ReminderManager.cancelHabitReminder(context, habit)
            if (habit.reminderEnabled && !habit.habitTime.isNullOrBlank()) {
                com.example.core.manager.ReminderManager.scheduleHabitReminder(
                    context = context, habit = habit,
                    vibrate = _eventReminderVibrate.value,
                    sound = _eventReminderSound.value
                )
            }
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            com.example.core.manager.ReminderManager.cancelHabitReminder(context, habit)
            habitRepository.deleteLogsForHabit(habit.id)
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

    // --- Pomodoro, Chronometer and DND Management ---
    fun startPomodoro(
        context: Context,
        task: TaskEntity,
        focusMinutes: Int = task.durationMinutes,
        targetSessions: Int? = 1,
        shortBreakMinutes: Int? = 5,
        longBreakMinutes: Int? = null,
        markCompleteOnFinish: Boolean = false,
        templateName: String? = null
    ) {
        if (_pomodoroRunning.value) return

        val updatedTask = task.copy(
            durationMinutes = focusMinutes,
            targetSessions = targetSessions,
            breakMinutes = shortBreakMinutes
        )

        viewModelScope.launch {
            taskRepository.updateTask(updatedTask)
        }

        _activePomodoroTask.value = updatedTask
        _pomodoroFocusMinutes.value = focusMinutes
        _pomodoroTargetSessions.value = targetSessions
        _pomodoroShortBreakMinutes.value = shortBreakMinutes
        _pomodoroLongBreakMinutes.value = longBreakMinutes
        _pomodoroMarkCompleteOnFinish.value = markCompleteOnFinish
        _pomodoroCurrentSession.value = 1
        _pomodoroPhase.value = "FOCUS"
        _pomodoroSecondsLeft.value = focusMinutes * 60
        _pomodoroRunning.value = true

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
                val relatedTask = taskRepository.getTaskById(task.id)
                timerRepository.insertSession(
                    TimerSessionEntity(
                        type = "POMODORO",
                        taskId = task.id,
                        label = relatedTask?.label ?: task.label,
                        durationSeconds = minutesElapsed * 60,
                        date = getTodayDateString()
                    )
                )
            }
        }
        resetPomodoroState(context)
    }

    fun discardPomodoro(context: Context) {
        pomodoroJob?.cancel()
        resetPomodoroState(context)
    }

    private fun resetPomodoroState(context: Context) {
        pomodoroJob?.cancel()
        _pomodoroRunning.value = false
        _activePomodoroTask.value = null
        _pomodoroSecondsLeft.value = 0
        if (_dndEnabled.value) {
            setDndState(context, originalDndState)
        }
    }

    fun resetPomodoro() {
        _pomodoroSecondsLeft.value = _pomodoroFocusMinutes.value * 60
        _pomodoroCurrentSession.value = 1
        _pomodoroPhase.value = "FOCUS"
    }

    fun adjustPomodoroMinusOne() {
        val current = _pomodoroSecondsLeft.value
        if (current > 0) {
            _pomodoroSecondsLeft.value = (current - 60).coerceAtLeast(0)
        }
    }

    private fun startTimerJob(context: Context) {
        pomodoroJob?.cancel()
        pomodoroJob = viewModelScope.launch {
            while (_pomodoroSecondsLeft.value > 0) {
                delay(1000)
                _pomodoroSecondsLeft.value -= 1
            }
            handlePhaseCompletion(context)
        }
    }

    private fun handlePhaseCompletion(context: Context) {
        val task = _activePomodoroTask.value ?: return
        val currentPhase = _pomodoroPhase.value
        val shortBreakMin = _pomodoroShortBreakMinutes.value
        val longBreakMin = _pomodoroLongBreakMinutes.value
        val targetSess = _pomodoroTargetSessions.value
        val currentSess = _pomodoroCurrentSession.value

        if (currentPhase == "FOCUS") {
            viewModelScope.launch {
                val relatedTask = taskRepository.getTaskById(task.id)
                timerRepository.insertSession(
                    TimerSessionEntity(
                        type = "POMODORO",
                        taskId = task.id,
                        label = relatedTask?.label ?: task.label,
                        durationSeconds = _pomodoroFocusMinutes.value * 60,
                        date = getTodayDateString()
                    )
                )

                if (_pomodoroMarkCompleteOnFinish.value) {
                    val subtasks = allTasks.value.filter { it.parentTaskId == task.id }
                    val hasIncompleteImportant = subtasks.any { it.status == "PENDING" && it.subtaskImportance == "IMPORTANT" }
                    val newStatus = if (!hasIncompleteImportant) "COMPLETED" else task.status
                    val updated = task.copy(
                        pomodorosCompleted = task.pomodorosCompleted + 1,
                        status = newStatus
                    )
                    taskRepository.updateTask(updated)
                    _activePomodoroTask.value = updated
                    if (newStatus == "COMPLETED") {
                        updated.linkedTodoId?.let { todoId ->
                            val linkedTodo = todoRepository.getTodoById(todoId)
                            if (linkedTodo != null && linkedTodo.status != "DONE") {
                                todoRepository.updateTodo(linkedTodo.copy(status = "DONE"))
                            }
                        }
                    }
                } else {
                    taskRepository.updateTask(task.copy(pomodorosCompleted = task.pomodorosCompleted + 1))
                }
            }

            triggerSessionFeedback(
                context,
                "Focus Session Completed!",
                "Great job! Focus session $currentSess for '${task.title}' is done."
            )

            if (targetSess != null && currentSess >= targetSess) {
                completeEntireChain(context, "Congratulations! You have finished all $targetSess focus sessions for '${task.title}'.")
            } else {
                val isLongBreak = currentSess % 4 == 0
                val breakDuration = if (isLongBreak) longBreakMin else shortBreakMin
                if (breakDuration != null && breakDuration > 0) {
                    _pomodoroPhase.value = "BREAK"
                    _pomodoroSecondsLeft.value = breakDuration * 60
                    startTimerJob(context)
                } else {
                    _pomodoroCurrentSession.value = currentSess + 1
                    _pomodoroPhase.value = "FOCUS"
                    _pomodoroSecondsLeft.value = _pomodoroFocusMinutes.value * 60
                    startTimerJob(context)
                }
            }
        } else {
            triggerSessionFeedback(
                context,
                "Break Over! Time to Focus",
                "Get ready! Focus session ${currentSess + 1} for '${task.title}' is starting."
            )
            _pomodoroCurrentSession.value = currentSess + 1
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

    // --- Chronometer ---
    fun startChronometer(taskId: Long? = null) {
        if (_chronoRunning.value) return
        if (pomodoroJob != null && _pomodoroRunning.value) return
        _chronoSelectedTaskId.value = taskId
        _chronoElapsed.value = 0L
        _chronoPaused.value = false
        _chronoRunning.value = true
        if (_dndEnabled.value) {
            originalDndState = getDndState(context)
            setDndState(context, true)
        }
        chronoJob?.cancel()
        chronoJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_chronoPaused.value) {
                    _chronoElapsed.value += 1
                }
            }
        }
    }

    fun pauseChronometer() {
        _chronoPaused.value = !_chronoPaused.value
    }

    fun stopChronometer() {
        chronoJob?.cancel()
        _chronoRunning.value = false
    }

    fun saveChronometerSession(durationSeconds: Int, taskId: Long?, note: String) {
        viewModelScope.launch {
            val relatedTask = if (taskId != null) taskRepository.getTaskById(taskId) else null
            timerRepository.insertSession(
                TimerSessionEntity(
                    type = "CHRONOMETER",
                    taskId = taskId,
                    label = relatedTask?.label ?: "",
                    durationSeconds = durationSeconds,
                    date = getTodayDateString(),
                    note = note
                )
            )
        }
        _chronoElapsed.value = 0L
        _chronoSelectedTaskId.value = null
        if (_dndEnabled.value) {
            setDndState(context, originalDndState)
        }
    }

    fun discardChronometer() {
        chronoJob?.cancel()
        _chronoRunning.value = false
        _chronoPaused.value = false
        _chronoElapsed.value = 0L
        _chronoSelectedTaskId.value = null
        if (_dndEnabled.value) {
            setDndState(context, originalDndState)
        }
    }

    fun resetChronometer() {
        _chronoElapsed.value = 0L
    }

    fun adjustChronoMinusOne() {
        val current = _chronoElapsed.value
        if (current > 0) {
            _chronoElapsed.value = (current - 60).coerceAtLeast(0)
        }
    }

    // --- Timer Templates ---
    fun createTemplate(name: String, focusMinutes: Int, shortBreakMinutes: Int?, longBreakMinutes: Int?, targetSessions: Int?) {
        viewModelScope.launch {
            timerRepository.insertTemplate(
                TimerTemplateEntity(
                    name = name,
                    focusMinutes = focusMinutes,
                    shortBreakMinutes = shortBreakMinutes,
                    longBreakMinutes = longBreakMinutes,
                    targetSessions = targetSessions
                )
            )
        }
    }

    fun updateTemplate(id: Long, name: String, focusMinutes: Int, shortBreakMinutes: Int?, longBreakMinutes: Int?, targetSessions: Int?) {
        viewModelScope.launch {
            timerRepository.updateTemplate(id, name, focusMinutes, shortBreakMinutes, longBreakMinutes, targetSessions)
        }
    }

    fun deleteTemplate(id: Long) {
        viewModelScope.launch {
            timerRepository.deleteTemplate(id)
        }
    }

    // --- Timer Session History ---
    fun updateTimerSession(id: Long, durationSeconds: Int, note: String, date: String) {
        viewModelScope.launch {
            timerRepository.updateSession(id, durationSeconds, note, date)
        }
    }

    fun deleteTimerSession(id: Long) {
        viewModelScope.launch {
            timerRepository.deleteSession(id)
        }
    }

    fun addManualTimerSession(type: String, taskId: Long?, durationSeconds: Int, date: String, note: String) {
        viewModelScope.launch {
            val relatedTask = if (taskId != null) taskRepository.getTaskById(taskId) else null
            timerRepository.insertSession(
                TimerSessionEntity(
                    type = type,
                    taskId = taskId,
                    label = relatedTask?.label ?: "",
                    durationSeconds = durationSeconds,
                    date = date,
                    note = note
                )
            )
        }
    }

    // --- Mark task complete from Timer screen ---
    fun markTaskCompleteFromTimer(taskId: Long) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(taskId) ?: return@launch
            val updated = task.copy(status = "COMPLETED")
            taskRepository.updateTask(updated)
            updated.linkedTodoId?.let { todoId ->
                val linkedTodo = todoRepository.getTodoById(todoId)
                if (linkedTodo != null && linkedTodo.status != "DONE") {
                    todoRepository.updateTodo(linkedTodo.copy(status = "DONE"))
                }
            }
        }
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
        viewModelScope.launch(Dispatchers.IO) {
            _screenTimeError.value = null

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                _appUsageItems.value = emptyList()
                _totalScreenTimeMinutes.value = 0
                _screenTimeError.value = "Screen time tracking requires Android 5.0+"
                return@launch
            }

            if (!hasUsageStatsPermission(context)) {
                _appUsageItems.value = emptyList()
                _totalScreenTimeMinutes.value = 0
                return@launch
            }

            try {
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startTime = calendar.timeInMillis
                val endTime = System.currentTimeMillis()

                val statsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)

                val packageManager = context.packageManager
                val allItems = statsMap.values
                    .filter { it.totalTimeInForeground > 0 }
                    .mapNotNull { stat ->
                        val mins = stat.totalTimeInForeground / (1000 * 60)
                        if (mins <= 0) return@mapNotNull null
                        val label = try {
                            val appInfo = packageManager.getApplicationInfo(stat.packageName, 0)
                            packageManager.getApplicationLabel(appInfo).toString()
                        } catch (_: Exception) {
                            return@mapNotNull null
                        }
                        AppUsageItem(
                            appName = label,
                            packageName = stat.packageName,
                            durationMinutes = mins
                        )
                    }
                    .sortedByDescending { it.durationMinutes }

                val topItems = allItems.take(6)
                val total = allItems.sumOf { it.durationMinutes }

                _appUsageItems.value = topItems
                _totalScreenTimeMinutes.value = total
            } catch (e: Exception) {
                _appUsageItems.value = emptyList()
                _totalScreenTimeMinutes.value = 0
                _screenTimeError.value = "Unable to load screen time"
            }
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
        viewModelScope.launch {
            try { ideaRepository.deleteGroup(group) } catch (e: Exception) { Log.e(TAG, "Failed to delete group", e) }
        }
    }

    fun addIdea(groupId: Long?, title: String, description: String, stages: List<IdeaStageEntity> = emptyList(), priority: String = "Medium") {
        if (title.isBlank()) return
        viewModelScope.launch {
            try {
                val allIdeas = ideaRepository.getAllIdeasSync()
                val nextOrder = (allIdeas.maxOfOrNull { it.sortOrder } ?: -1) + 1
                val ideaId = ideaRepository.insertIdea(IdeaEntity(groupId = groupId, title = title.trim(), description = description.trim(), sortOrder = nextOrder, priority = priority))
                stages.filter { it.title.isNotBlank() }.forEachIndexed { i, s ->
                    ideaRepository.insertStage(s.copy(ideaId = ideaId, orderIndex = i))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add idea", e)
            }
        }
    }

    fun triggerReorderIdeasByPriority() {
        viewModelScope.launch {
            val currentIdeas = ideaRepository.getAllIdeasSync()
            val sorted = currentIdeas.sortedWith(
                compareBy<IdeaEntity> {
                    when (it.priority) {
                        "High" -> 1
                        "Medium" -> 2
                        "Low" -> 3
                        else -> 4
                    }
                }.thenBy { it.sortOrder }
            )
            val updated = sorted.mapIndexed { index, idea ->
                idea.copy(sortOrder = index)
            }
            ideaRepository.updateIdeaSortOrders(updated)
        }
    }

    fun reorderIdea(idea: IdeaEntity, activeIdeas: List<IdeaEntity>, deltaIndex: Int) {
        viewModelScope.launch {
            val currentIndex = activeIdeas.indexOf(idea)
            if (currentIndex == -1) return@launch
            val newIndex = (currentIndex + deltaIndex).coerceIn(0, activeIdeas.size - 1)
            if (deltaIndex != 0) {
                val mutableIdeas = activeIdeas.toMutableList()
                mutableIdeas.removeAt(currentIndex)
                mutableIdeas.add(newIndex, idea)
                val updatedIdeas = mutableIdeas.mapIndexed { index, t ->
                    t.copy(sortOrder = index)
                }
                ideaRepository.updateIdeaSortOrders(updatedIdeas)
            }
        }
    }

    fun updateIdea(idea: IdeaEntity, stages: List<IdeaStageEntity> = emptyList()) {
        viewModelScope.launch {
            try {
                ideaRepository.updateIdea(idea)
                val existingStages = ideaRepository.getStagesForIdeaSync(idea.id)
                existingStages.forEach { existing ->
                    if (stages.none { it.id == existing.id }) {
                        ideaRepository.deleteStage(existing)
                    }
                }
                stages.filter { it.title.isNotBlank() }.forEachIndexed { i, stage ->
                    if (stage.id == 0L) {
                        ideaRepository.insertStage(stage.copy(ideaId = idea.id, orderIndex = i))
                    } else {
                        ideaRepository.updateStage(stage.copy(orderIndex = i))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update idea", e)
            }
        }
    }

    fun deleteIdea(idea: IdeaEntity) {
        viewModelScope.launch {
            try { ideaRepository.deleteIdea(idea) } catch (e: Exception) { Log.e(TAG, "Failed to delete idea", e) }
        }
    }

    fun deleteIdeaWithUndo(idea: IdeaEntity) {
        viewModelScope.launch {
            try {
                val stages = ideaRepository.getStagesForIdeaSync(idea.id)
                deleteIdea(idea)
                pushUndo(UndoSnapshot.IdeaSnapshot(idea, stages), idea.title)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete idea with undo", e)
            }
        }
    }

    fun moveIdeaToGroup(ideaId: Long, newGroupId: Long?) {
        viewModelScope.launch {
            try { ideaRepository.moveIdeaToGroup(ideaId, newGroupId) } catch (e: Exception) { Log.e(TAG, "Failed to move idea", e) }
        }
    }

    fun addStage(ideaId: Long, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            try {
                val stages = ideaRepository.getStagesForIdeaSync(ideaId)
                ideaRepository.insertStage(IdeaStageEntity(ideaId = ideaId, title = title.trim(), orderIndex = stages.size))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add stage", e)
            }
        }
    }

    fun updateStage(stage: IdeaStageEntity) {
        viewModelScope.launch {
            try { ideaRepository.updateStage(stage) } catch (e: Exception) { Log.e(TAG, "Failed to update stage", e) }
        }
    }

    fun deleteStage(stage: IdeaStageEntity) {
        viewModelScope.launch {
            try { ideaRepository.deleteStage(stage) } catch (e: Exception) { Log.e(TAG, "Failed to delete stage", e) }
        }
    }

    fun addIdeaToPlanner(idea: IdeaEntity, date: String, type: String) {
        viewModelScope.launch {
            try {
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
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add idea to planner", e)
            }
        }
    }

    fun addStageToPlanner(stage: IdeaStageEntity, date: String, type: String) {
        viewModelScope.launch {
            try {
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
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add stage to planner", e)
            }
        }
    }

    // === To-Do CRUD ===
    fun addTodo(title: String, description: String = "", priority: String = "Medium") {
        if (title.isBlank()) return
        viewModelScope.launch {
            try {
                val allTodos = todoRepository.getAllTodosSync()
                val nextOrder = (allTodos.maxOfOrNull { it.sortOrder } ?: -1) + 1
                todoRepository.insertTodo(TodoEntity(title = title.trim(), description = description.trim(), priority = priority, sortOrder = nextOrder))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add todo", e)
            }
        }
    }

    fun reorderTodo(todo: TodoEntity, activeTodos: List<TodoEntity>, deltaIndex: Int) {
        viewModelScope.launch {
            val currentIndex = activeTodos.indexOf(todo)
            if (currentIndex == -1) return@launch
            val newIndex = (currentIndex + deltaIndex).coerceIn(0, activeTodos.size - 1)
            if (deltaIndex != 0) {
                val mutableTodos = activeTodos.toMutableList()
                mutableTodos.removeAt(currentIndex)
                mutableTodos.add(newIndex, todo)
                val updatedTodos = mutableTodos.mapIndexed { index, t ->
                    t.copy(sortOrder = index)
                }
                todoRepository.updateTodoSortOrders(updatedTodos)
            }
        }
    }

    fun triggerReorderTodosByPriority() {
        viewModelScope.launch {
            val currentTodos = todoRepository.getAllTodosSync()
            val sorted = currentTodos.sortedWith(
                compareBy<TodoEntity> {
                    when (it.priority) {
                        "High" -> 1
                        "Medium" -> 2
                        "Low" -> 3
                        else -> 4
                    }
                }.thenBy { it.sortOrder }
            )
            val updated = sorted.mapIndexed { index, todo ->
                todo.copy(sortOrder = index)
            }
            todoRepository.updateTodoSortOrders(updated)
        }
    }

    fun updateTodo(todo: TodoEntity) {
        viewModelScope.launch {
            try {
                todoRepository.updateTodo(todo)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update todo", e)
            }
        }
    }

    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch {
            try {
                if (todo.linkedTaskId != null) {
                    taskRepository.getTaskById(todo.linkedTaskId)?.let { linkedTask ->
                        taskRepository.deleteTaskAndSubtasks(linkedTask)
                    }
                }
                todoRepository.deleteTodo(todo)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete todo", e)
            }
        }
    }

    fun deleteTodoWithUndo(todo: TodoEntity) {
        viewModelScope.launch {
            try {
                var linkedTask: TaskEntity? = null
                var linkedSubtasks: List<TaskEntity> = emptyList()
                if (todo.linkedTaskId != null) {
                    linkedTask = taskRepository.getTaskById(todo.linkedTaskId)
                    if (linkedTask != null) {
                        linkedSubtasks = taskRepository.getSubtasks(linkedTask.id)
                    }
                }
                deleteTodo(todo)
                pushUndo(
                    UndoSnapshot.TodoSnapshot(todo, linkedTask, linkedSubtasks),
                    todo.title
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete todo with undo", e)
            }
        }
    }

    fun unlinkAndDeleteTodoWithUndo(todo: TodoEntity) {
        viewModelScope.launch {
            try {
                var linkedTask: TaskEntity? = null
                var linkedSubtasks: List<TaskEntity> = emptyList()
                if (todo.linkedTaskId != null) {
                    linkedTask = taskRepository.getTaskById(todo.linkedTaskId)
                    if (linkedTask != null) {
                        linkedSubtasks = taskRepository.getSubtasks(linkedTask.id)
                    }
                }
                unlinkTodoFromTask(todo)
                todoRepository.deleteTodo(todo)
                pushUndo(
                    UndoSnapshot.TodoSnapshot(todo, linkedTask, linkedSubtasks),
                    todo.title
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unlink and delete todo with undo", e)
            }
        }
    }

    fun toggleTodoCompletion(todo: TodoEntity) {
        viewModelScope.launch {
            try {
                val newStatus = if (todo.status == "DONE") "PENDING" else "DONE"
                todoRepository.updateTodo(todo.copy(status = newStatus))

                if (todo.linkedTaskId != null && newStatus == "DONE") {
                    val linkedTask = taskRepository.getTaskById(todo.linkedTaskId)
                    if (linkedTask != null && linkedTask.status != "COMPLETED") {
                        taskRepository.updateTask(linkedTask.copy(status = "COMPLETED"))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle todo completion", e)
            }
        }
    }

    fun linkTodoToTask(todo: TodoEntity, targetDate: String) {
        viewModelScope.launch {
            try {
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
            } catch (e: Exception) {
                Log.e(TAG, "Failed to link todo to task", e)
            }
        }
    }

    fun unlinkTodoFromTask(todo: TodoEntity) {
        viewModelScope.launch {
            try {
                if (todo.linkedTaskId != null) {
                    taskRepository.deleteTaskById(todo.linkedTaskId)
                    todoRepository.updateTodo(todo.copy(linkedTaskId = null))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unlink todo from task", e)
            }
        }
    }

    fun moveTaskToTodo(task: TaskEntity, subtasks: List<TaskEntity>) {
        viewModelScope.launch {
            try {
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
                task.linkedTodoId?.let { todoId ->
                    todoRepository.getTodoById(todoId)?.let { linkedTodo ->
                        todoRepository.updateTodo(linkedTodo.copy(linkedTaskId = null))
                    }
                }
                taskRepository.deleteTaskAndSubtasks(task)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to move task to todo", e)
            }
        }
    }

    fun turnNoteIntoIdea(task: TaskEntity, subtasks: List<TaskEntity>) {
        viewModelScope.launch {
            try {
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
                task.linkedTodoId?.let { todoId ->
                    todoRepository.getTodoById(todoId)?.let { linkedTodo ->
                        todoRepository.updateTodo(linkedTodo.copy(linkedTaskId = null))
                    }
                }
                taskRepository.deleteTaskAndSubtasks(task)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to turn note into idea", e)
            }
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

    private fun getOffsetMonthString(monthStr: String, offsetMonths: Int): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
        val date = sdf.parse(monthStr) ?: java.util.Date()
        val cal = java.util.Calendar.getInstance()
        cal.time = date
        cal.add(java.util.Calendar.MONTH, offsetMonths)
        return sdf.format(cal.time)
    }

    // Helper utilities for date
    fun getTodayDateString(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    }
    
    private fun getTodayMonthString(): String {
        return java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(java.util.Date())
    }

    private fun getTodayYearString(): String {
        return java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault()).format(java.util.Date())
    }

    fun dismissUndo(entryId: Long) {
        _undoStack.value = _undoStack.value.filter { it.id != entryId }
    }

    fun restoreFromUndo(entryId: Long) {
        viewModelScope.launch {
            val entry = _undoStack.value.find { it.id == entryId } ?: return@launch
            dismissUndo(entryId)
            try {
                when (val snap = entry.snapshot) {
                    is UndoSnapshot.TaskSnapshot -> {
                        val newTaskId = taskRepository.insertTask(snap.task.copy(id = 0))
                        for (subtask in snap.subtasks) {
                            taskRepository.insertTask(subtask.copy(id = 0, parentTaskId = newTaskId))
                        }
                        snap.linkedTodoId?.let { linkedTodoId ->
                            todoRepository.getTodoById(linkedTodoId)?.let { linkedTodo ->
                                todoRepository.updateTodo(linkedTodo.copy(linkedTaskId = newTaskId))
                            }
                        }
                        if (snap.task.type == "EVENT") {
                            com.example.core.manager.ReminderManager.scheduleReminders(
                                context = context,
                                task = snap.task.copy(id = newTaskId),
                                vibrate = _eventReminderVibrate.value,
                                sound = _eventReminderSound.value
                            )
                        }
                    }
                    is UndoSnapshot.TodoSnapshot -> {
                        val newTodoId: Long
                        if (snap.linkedTask != null) {
                            val newTaskId = taskRepository.insertTask(snap.linkedTask.copy(id = 0, linkedTodoId = null))
                            for (subtask in snap.linkedSubtasks) {
                                taskRepository.insertTask(subtask.copy(id = 0, parentTaskId = newTaskId))
                            }
                            newTodoId = todoRepository.insertTodo(snap.todo.copy(id = 0, linkedTaskId = newTaskId))
                            taskRepository.getTaskById(newTaskId)?.let { insertedTask ->
                                taskRepository.updateTask(insertedTask.copy(linkedTodoId = newTodoId))
                            }
                        } else {
                            newTodoId = todoRepository.insertTodo(snap.todo.copy(id = 0))
                        }
                    }
                    is UndoSnapshot.IdeaSnapshot -> {
                        val newIdeaId = ideaRepository.insertIdea(snap.idea.copy(id = 0))
                        for (stage in snap.stages) {
                            ideaRepository.insertStage(stage.copy(id = 0, ideaId = newIdeaId))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore from undo", e)
            }
        }
    }

    private fun pushUndo(snapshot: UndoSnapshot, title: String) {
        val label = snapshot.typeLabel
        val shortTitle = if (title.length > 40) title.take(37) + "..." else title
        val entry = UndoEntry(
            id = System.nanoTime(),
            snapshot = snapshot,
            message = "$label \"$shortTitle\" deleted",
            expiryTime = System.currentTimeMillis() + 5000
        )
        _undoStack.value = _undoStack.value + entry
        viewModelScope.launch {
            delay(5000)
            _undoStack.value = _undoStack.value.filter { it.id != entry.id }
        }
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
    private val timerRepository: com.example.core.repository.TimerRepository,
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
                taskRepository, timerRepository, habitRepository, sleepLogRepository,
                ideaRepository, todoRepository, diaryRepository,
                shopItemRepository, mottoRepository, dayReviewRepository,
                context
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
