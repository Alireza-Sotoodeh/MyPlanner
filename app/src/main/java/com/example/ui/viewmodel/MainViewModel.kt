package com.example.ui.viewmodel

import android.app.AppOpsManager
import android.app.NotificationManager
import android.app.AlarmManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.ContentValues
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Environment
import android.os.Process
import android.os.Vibrator
import android.os.VibratorManager
import android.os.VibrationEffect
import android.media.RingtoneManager
import android.media.Ringtone
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.provider.Settings
import android.provider.MediaStore
import android.util.Log
import kotlin.math.ceil
import java.util.concurrent.ConcurrentHashMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.DayReviewEntity
import com.example.core.database.entity.DiaryEntryEntity
import com.example.core.database.entity.HabitEntity
import com.example.core.database.entity.HabitLogEntity
import com.example.core.database.entity.IdeaEntity
import com.example.core.database.entity.IdeaGroupEntity
import com.example.core.database.entity.LearnGroupEntity
import com.example.core.database.entity.LearnItemEntity
import com.example.core.database.entity.LearnSectionEntity
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
import com.example.core.repository.LearnRepository
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
import kotlinx.coroutines.Dispatchers
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
import java.nio.charset.StandardCharsets
import java.io.IOException
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import android.app.PendingIntent
import com.example.core.service.TimerForegroundService
import com.example.core.service.TimerMode

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
        val subTodos: List<TodoEntity> = emptyList(),
    ) : UndoSnapshot("To-Do")
    data class IdeaSnapshot(
        val idea: IdeaEntity,
        val stages: List<IdeaStageEntity>,
    ) : UndoSnapshot("Idea")
    data class IdeaToTaskSnapshot(
        val idea: IdeaEntity,
        val stages: List<IdeaStageEntity>,
        val parentTaskId: Long,
        val subtaskIds: List<Long>,
    ) : UndoSnapshot("Idea in Planner")
    data class HabitSnapshot(
        val habit: HabitEntity,
        val logs: List<HabitLogEntity>,
    ) : UndoSnapshot("Habit")
    data class DiarySnapshot(val entry: DiaryEntryEntity) : UndoSnapshot("Diary entry")
    data class DayReviewSnapshot(val review: DayReviewEntity) : UndoSnapshot("Day review")
    data class TimerTemplateSnapshot(val template: TimerTemplateEntity) : UndoSnapshot("Timer template")
    data class TimerSessionSnapshot(val session: TimerSessionEntity) : UndoSnapshot("Timer session")
    data class ShopItemSnapshot(val item: ShopItemEntity) : UndoSnapshot("Shop item")
    data class MottoSnapshot(val motto: MottoEntity) : UndoSnapshot("Motto")
    data class LearnItemSnapshot(
        val item: LearnItemEntity,
        val sections: List<LearnSectionEntity>,
        val studyTaskIds: List<Long>,
        val reviewTaskIds: List<Long>,
    ) : UndoSnapshot("Learn Item")
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
    val dayReviews: List<DayReviewEntity> = emptyList(),
    val learnGroups: List<LearnGroupEntity> = emptyList(),
    val learnItems: List<LearnItemEntity> = emptyList(),
    val learnSections: List<LearnSectionEntity> = emptyList(),
    val timerSessions: List<TimerSessionEntity> = emptyList(),
    val timerTemplates: List<TimerTemplateEntity> = emptyList()
)

data class PendingTaskCompletion(
    val task: TaskEntity,
    val subtasks: List<TaskEntity>,
    val durationMinutes: Int = 0,
    val todoId: Long? = null,
    val startHour: Int? = null,
    val startMinute: Int? = null,
    val endHour: Int? = null,
    val endMinute: Int? = null
)

data class PendingSubTodoCompletion(
    val todo: TodoEntity,
    val subTodos: List<TodoEntity>
)

data class PomodoroCompletionState(
    val phase: String,
    val sessionNumber: Int,
    val totalSessions: Int?,
    val taskTitle: String,
    val taskId: Long,
    val durationSeconds: Int,
    val nextActionLabel: String,
    val nextActionMinutes: Int,
    val canProceed: Boolean,
    val isFinal: Boolean,
    val breakDuration: Int?
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
    private val learnRepository: LearnRepository,
    private val context: Context
) : ViewModel() {

    private val TAG = "MainViewModel"
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val moshiStrict = moshi.adapter(BulletCoachBackup::class.java).failOnUnknown().lenient()
    private val prefs = context.getSharedPreferences("bulletcoach_prefs", Context.MODE_PRIVATE)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val database = com.example.core.database.AppDatabase.getDatabase(context)

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

    private val _googleDriveEmail = MutableStateFlow(prefs.getString("google_drive_email", "") ?: "")
    val googleDriveEmail: StateFlow<String> = _googleDriveEmail.asStateFlow()

    private val _dndEnabled = MutableStateFlow(prefs.getBoolean("pomodoro_dnd_enabled", false))
    val dndEnabled: StateFlow<Boolean> = _dndEnabled.asStateFlow()

    private val _eventReminderVibrate = MutableStateFlow(prefs.getBoolean("event_reminder_vibrate", true))
    val eventReminderVibrate: StateFlow<Boolean> = _eventReminderVibrate.asStateFlow()

    private val _eventReminderSound = MutableStateFlow(prefs.getBoolean("event_reminder_sound", true))
    val eventReminderSound: StateFlow<Boolean> = _eventReminderSound.asStateFlow()

    private val _eventReminderEnabled = MutableStateFlow(prefs.getBoolean("event_reminder_enabled", true))
    val eventReminderEnabled: StateFlow<Boolean> = _eventReminderEnabled.asStateFlow()

    private val _pomodoroRingtoneUri = MutableStateFlow(prefs.getString("pomodoro_ringtone_uri", "") ?: "")
    val pomodoroRingtoneUri: StateFlow<String> = _pomodoroRingtoneUri.asStateFlow()

    private val _pomodoroRingtoneEnabled = MutableStateFlow(prefs.getBoolean("pomodoro_ringtone_enabled", true))
    val pomodoroRingtoneEnabled: StateFlow<Boolean> = _pomodoroRingtoneEnabled.asStateFlow()

    private val _pomodoroVibrateEnabled = MutableStateFlow(prefs.getBoolean("pomodoro_vibrate_enabled", true))
    val pomodoroVibrateEnabled: StateFlow<Boolean> = _pomodoroVibrateEnabled.asStateFlow()

    companion object {
        val HEARTBEAT_PATTERN = longArrayOf(0, 300, 100, 300, 500, 300, 100, 300)
        val HEARTBEAT_PATTERN_SINGLE = longArrayOf(0, 300, 100, 300)
        private const val PREFS_KEY_ORIGINAL_DND_FILTER = "original_dnd_filter"
        val LEITNER_INTERVALS = intArrayOf(1, 3, 7, 16, 35, 90)
    }

    private fun parseDaysOfWeek(daysOfWeek: String): Set<Int> =
        daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()

    private fun parseWeeklyDate(dateStr: String): java.util.Calendar? {
        // Handle weekly format: "yyyy-Www" (e.g., "2026-W29")
        val weeklyRegex = "^\\d{4}-W\\d{2}$".toRegex()
        if (!weeklyRegex.matches(dateStr)) return null
        
        val parts = dateStr.split("-W")
        val year = parts[0].toIntOrNull() ?: return null
        val week = parts[1].toIntOrNull() ?: return null
        
        val cal = java.util.Calendar.getInstance()
        cal.clear()
        cal.set(java.util.Calendar.YEAR, year)
        cal.set(java.util.Calendar.WEEK_OF_YEAR, week)
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek())
        return cal
    }

    private fun isAllowedDay(dateStr: String, daysOfWeek: String): Boolean {
        if (daysOfWeek.isBlank()) return true
        val allowed = parseDaysOfWeek(daysOfWeek)
        if (allowed.isEmpty()) return true
        
        // Handle weekly format "yyyy-Www"
        val weeklyCal = parseWeeklyDate(dateStr)
        if (weeklyCal != null) {
            // For weekly dates, the dateStr represents a week.
            // Check if today's day of week is in allowed days.
            val todayCal = java.util.Calendar.getInstance()
            return todayCal.get(java.util.Calendar.DAY_OF_WEEK) in allowed
        }
        
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance()
        cal.time = fmt.parse(dateStr) ?: return true
        return cal.get(java.util.Calendar.DAY_OF_WEEK) in allowed
    }

    private fun nextAllowedDate(dateStr: String, daysOfWeek: String): String {
        if (daysOfWeek.isBlank()) return dateStr
        val allowed = parseDaysOfWeek(daysOfWeek)
        if (allowed.isEmpty()) return dateStr
        
        // Handle weekly format "yyyy-Www"
        val weeklyCal = parseWeeklyDate(dateStr)
        if (weeklyCal != null) {
            // Find the next allowed day of week from today
            val todayCal = java.util.Calendar.getInstance()
            val todayDOW = todayCal.get(java.util.Calendar.DAY_OF_WEEK)
            
            // Find the next allowed day
            for (i in 0..6) {
                val checkDOW = if (todayDOW + i > 7) todayDOW + i - 7 else todayDOW + i
                if (checkDOW in allowed) {
                    val resultCal = java.util.Calendar.getInstance()
                    resultCal.time = todayCal.time
                    resultCal.add(java.util.Calendar.DAY_OF_YEAR, i)
                    return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(resultCal.time)
                }
            }
            return dateStr
        }
        
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance()
        cal.time = fmt.parse(dateStr) ?: return dateStr
        for (i in 0..6) {
            if (cal.get(java.util.Calendar.DAY_OF_WEEK) in allowed) return fmt.format(cal.time)
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        return dateStr
    }

    private fun countAllowedDaysBetween(from: String, to: String, daysOfWeek: String): Int {
        if (daysOfWeek.isBlank()) return daysBetweenDates(from, to)
        val allowed = parseDaysOfWeek(daysOfWeek)
        if (allowed.isEmpty()) return daysBetweenDates(from, to)
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val startCal = java.util.Calendar.getInstance().apply { time = fmt.parse(from) ?: return 1 }
        val endCal = java.util.Calendar.getInstance().apply { time = fmt.parse(to) ?: return 1 }
        var count = 0
        while (!startCal.after(endCal)) {
            if (startCal.get(java.util.Calendar.DAY_OF_WEEK) in allowed) count++
            startCal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        return count.coerceAtLeast(1)
    }

    private val _pendingReviewTask = MutableStateFlow<TaskEntity?>(null)
    val pendingReviewTask: StateFlow<TaskEntity?> = _pendingReviewTask.asStateFlow()

    private val _pendingReviewSection = MutableStateFlow<LearnSectionEntity?>(null)
    val pendingReviewSection: StateFlow<LearnSectionEntity?> = _pendingReviewSection.asStateFlow()

    private val _pendingReviewLearnItem = MutableStateFlow<LearnItemEntity?>(null)
    val pendingReviewLearnItem: StateFlow<LearnItemEntity?> = _pendingReviewLearnItem.asStateFlow()

    private val _pomodoroCompletionState = MutableStateFlow<PomodoroCompletionState?>(null)
    val pomodoroCompletionState: StateFlow<PomodoroCompletionState?> = _pomodoroCompletionState.asStateFlow()

    private val _defaultBreakMinutes = MutableStateFlow(prefs.getInt("default_break_minutes", 5))
    val defaultBreakMinutes: StateFlow<Int> = _defaultBreakMinutes.asStateFlow()

    private val _customLabels = MutableStateFlow(loadCustomLabels())
    val customLabels: StateFlow<List<Pair<String, Long>>> = _customLabels.asStateFlow()

    private val _autoSortEnabled = MutableStateFlow(prefs.getBoolean("auto_sort_enabled", false))
    val autoSortEnabled: StateFlow<Boolean> = _autoSortEnabled.asStateFlow()

    private val _mottoEnabled = MutableStateFlow(prefs.getBoolean("motto_enabled", true))
    val mottoEnabled: StateFlow<Boolean> = _mottoEnabled.asStateFlow()

    private val _expandAllItems = MutableStateFlow(prefs.getBoolean("daily_expand_all_items", true))
    val expandAllItems: StateFlow<Boolean> = _expandAllItems.asStateFlow()

    private val _expandAllSubtasks = MutableStateFlow(prefs.getBoolean("daily_expand_all_subtasks", true))
    val expandAllSubtasks: StateFlow<Boolean> = _expandAllSubtasks.asStateFlow()

    private val _expandAllDescriptions = MutableStateFlow(prefs.getBoolean("todo_expand_all_descriptions", false))
    val expandAllDescriptions: StateFlow<Boolean> = _expandAllDescriptions.asStateFlow()

    private val _expandAllIdeas = MutableStateFlow(prefs.getBoolean("ideas_expand_all_ideas", true))
    val expandAllIdeas: StateFlow<Boolean> = _expandAllIdeas.asStateFlow()

    private val _expandAllLearnItems = MutableStateFlow(prefs.getBoolean("learn_expand_all_items", true))
    val expandAllLearnItems: StateFlow<Boolean> = _expandAllLearnItems.asStateFlow()

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

    fun updateDefaultBreakMinutes(minutes: Int) {
        prefs.edit().putInt("default_break_minutes", minutes).apply()
        _defaultBreakMinutes.value = minutes
    }

    fun updateGoogleDriveConnected(connected: Boolean, email: String = "") {
        if (connected) {
            trySilentSignIn(email)
        } else {
            com.example.core.manager.DriveManager.signOut(context)
            com.example.core.manager.DriveManager.invalidateToken()
            prefs.edit().putBoolean("google_drive_connected", false)
                .putString("google_drive_email", "")
                .apply()
            _googleDriveConnected.value = false
            _googleDriveEmail.value = ""
        }
    }

    private fun clearDriveConnected() {
        if (_googleDriveConnected.value) {
            com.example.core.manager.DriveManager.signOut(context)
            com.example.core.manager.DriveManager.invalidateToken()
            prefs.edit().putBoolean("google_drive_connected", false)
                .putString("google_drive_email", "")
                .apply()
            _googleDriveConnected.value = false
            _googleDriveEmail.value = ""
        }
    }

    private val _pendingDriveSignInIntent = MutableStateFlow<android.content.Intent?>(null)
    val pendingDriveSignInIntent: StateFlow<android.content.Intent?> = _pendingDriveSignInIntent.asStateFlow()

    private fun trySilentSignIn(email: String) {
        viewModelScope.launch {
            if (com.example.core.manager.DriveManager.isSignedIn(context)) {
                val account = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)
                val accEmail = account?.email ?: email
                prefs.edit().putBoolean("google_drive_connected", true)
                    .putString("google_drive_email", accEmail)
                    .apply()
                _googleDriveConnected.value = true
                _googleDriveEmail.value = accEmail
            } else {
                _pendingDriveSignInIntent.value = com.example.core.manager.DriveManager.getSignInIntent(context)
            }
        }
    }

    fun onDriveSignInResult(data: android.content.Intent?): Boolean {
        _pendingDriveSignInIntent.value = null
        val account = com.example.core.manager.DriveManager.handleSignInResult(data)
        if (account != null) {
            val email = account.email ?: ""
            prefs.edit().putBoolean("google_drive_connected", true)
                .putString("google_drive_email", email)
                .apply()
            _googleDriveConnected.value = true
            _googleDriveEmail.value = email
            return true
        }
        return false
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

    fun updateEventReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("event_reminder_enabled", enabled).apply()
        _eventReminderEnabled.value = enabled
    }

    fun updatePomodoroRingtoneUri(uri: String) {
        prefs.edit().putString("pomodoro_ringtone_uri", uri).apply()
        _pomodoroRingtoneUri.value = uri
    }

    fun updatePomodoroRingtoneEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("pomodoro_ringtone_enabled", enabled).apply()
        _pomodoroRingtoneEnabled.value = enabled
    }

    fun updatePomodoroVibrateEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("pomodoro_vibrate_enabled", enabled).apply()
        _pomodoroVibrateEnabled.value = enabled
    }

    fun getCurrentRingtoneName(context: Context): String {
        val uri = _pomodoroRingtoneUri.value
        if (uri.isBlank()) return "Default ringtone"
        return try {
            val ringtone = RingtoneManager.getRingtone(context, android.net.Uri.parse(uri))
            ringtone?.getTitle(context) ?: "Custom ringtone"
        } catch (e: Exception) {
            "Custom ringtone"
        }
    }

    fun updateAutoSortEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("auto_sort_enabled", enabled).apply()
        _autoSortEnabled.value = enabled
    }

    fun updateMottoEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("motto_enabled", enabled).apply()
        _mottoEnabled.value = enabled
    }

    fun toggleExpandAllItems() {
        val newValue = !_expandAllItems.value
        _expandAllItems.value = newValue
        prefs.edit().putBoolean("daily_expand_all_items", newValue).apply()
    }

    fun toggleExpandAllSubtasks() {
        val newValue = !_expandAllSubtasks.value
        _expandAllSubtasks.value = newValue
        prefs.edit().putBoolean("daily_expand_all_subtasks", newValue).apply()
    }

    fun toggleExpandAllDescriptions() {
        val newValue = !_expandAllDescriptions.value
        _expandAllDescriptions.value = newValue
        prefs.edit().putBoolean("todo_expand_all_descriptions", newValue).apply()
    }

    fun toggleExpandAllIdeas() {
        val newValue = !_expandAllIdeas.value
        _expandAllIdeas.value = newValue
        prefs.edit().putBoolean("ideas_expand_all_ideas", newValue).apply()
    }

    fun toggleExpandAllLearnItems() {
        val newValue = !_expandAllLearnItems.value
        _expandAllLearnItems.value = newValue
        prefs.edit().putBoolean("learn_expand_all_items", newValue).apply()
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
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            try {
                if (!com.example.core.manager.DriveManager.isSignedIn(context)) {
                    onResult(false, "Not signed in to Google Drive. Please reconnect.")
                    return@launch
                }

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
                val timerSessionsList = timerRepository.getAllSessions().first()
                val timerTemplatesList = timerRepository.getAllTemplates().first()
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
                    dayReviews = dayReviewsList,
                    learnGroups = learnRepository.getAllGroupsSync(),
                    learnItems = learnItems.value,
                    learnSections = learnItems.value.flatMap { learnRepository.getSectionsForItemSync(it.id) },
                    timerSessions = timerSessionsList,
                    timerTemplates = timerTemplatesList
                )

                val adapter = moshi.adapter(BulletCoachBackup::class.java)
                val jsonString = adapter.toJson(backupObj)

                // Save locally (offline fallback) — uncompressed
                val backupFile = java.io.File(context.filesDir, "bulletcoach_backup.json")
                backupFile.writeText(jsonString)

                // Gzip for Drive upload
                val jsonBytes = jsonString.toByteArray(StandardCharsets.UTF_8)
                val bos = java.io.ByteArrayOutputStream()
                java.util.zip.GZIPOutputStream(bos).use { it.write(jsonBytes) }
                val gzipBytes = bos.toByteArray()
                val dateStr = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                val filename = "bulletcoach_${dateStr}.json.gz"
                val fileId = com.example.core.manager.DriveManager.uploadBackup(context, gzipBytes, filename)

                if (fileId != null) {
                    val lastSync = System.currentTimeMillis()
                    prefs.edit().putLong("drive_last_sync_at", lastSync).apply()
                    onResult(true, "Successfully backed up ${tasksList.size} intentions, ${habitsList.size} habits, and logs to Google Drive!")
                } else {
                    if (!com.example.core.manager.DriveManager.isSignedIn(context)) clearDriveConnected()
                    onResult(true, "Saved locally (Drive upload failed). ${tasksList.size} intentions backed up.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Backup failed", e)
                onResult(false, "Backup failed: ${e.localizedMessage}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun restoreDataFromGoogleDrive(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.value = true
            try {
                var jsonString: String? = null
                var fromDrive = false

                // Try Drive first
                if (com.example.core.manager.DriveManager.isSignedIn(context)) {
                    try {
                        val driveBytes = com.example.core.manager.DriveManager.downloadLatest(context)
                        if (driveBytes != null) {
                            jsonString = try {
                                java.util.zip.GZIPInputStream(java.io.ByteArrayInputStream(driveBytes))
                                    .use { it.reader(StandardCharsets.UTF_8).readText() }
                            } catch (_: java.util.zip.ZipException) {
                                String(driveBytes, StandardCharsets.UTF_8)
                            }
                            fromDrive = jsonString != null
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Drive download failed, falling back to local", e)
                    }
                }

                // Fall back to local
                if (jsonString == null) {
                    val backupFile = java.io.File(context.filesDir, "bulletcoach_backup.json")
                    if (!backupFile.exists()) {
                        onResult(false, "No backup file found. Please create a backup first!")
                        return@launch
                    }
                    val MAX_BACKUP_SIZE = 50 * 1024 * 1024L
                    if (backupFile.length() > MAX_BACKUP_SIZE) {
                        onResult(false, "Backup file too large (${backupFile.length() / 1024 / 1024} MB). Max supported: 50 MB.")
                        return@launch
                    }
jsonString = backupFile.readText()
            }

            val backupObj = moshiStrict.fromJson(jsonString)

                if (backupObj == null) {
                    onResult(false, "Failed to parse backup data.")
                    return@launch
                }

                // Clear existing data
                val writableDb = database.openHelper.writableDatabase
                writableDb.beginTransaction()
                try {
                    writableDb.execSQL("DELETE FROM tasks")
                    writableDb.execSQL("DELETE FROM habits")
                    writableDb.execSQL("DELETE FROM habit_logs")
                    writableDb.execSQL("DELETE FROM sleep_logs")
                    writableDb.execSQL("DELETE FROM timer_sessions")
                    writableDb.execSQL("DELETE FROM timer_templates")
                    writableDb.execSQL("DELETE FROM idea_groups")
                    writableDb.execSQL("DELETE FROM ideas")
                    writableDb.execSQL("DELETE FROM idea_stages")
                    writableDb.execSQL("DELETE FROM todos")
                    writableDb.execSQL("DELETE FROM diary_entries")
                    writableDb.execSQL("DELETE FROM shop_items")
                    writableDb.execSQL("DELETE FROM mottos")
                    writableDb.execSQL("DELETE FROM day_reviews")
                    writableDb.execSQL("DELETE FROM learn_groups")
                    writableDb.execSQL("DELETE FROM learn_items")
                    writableDb.execSQL("DELETE FROM learn_sections")
                    writableDb.execSQL("DELETE FROM sqlite_sequence")
                    writableDb.setTransactionSuccessful()
                } finally {
                    writableDb.endTransaction()
                }

                // Restore in FK-safe order: groups before items, parents before children
                backupObj.learnGroups.forEach { learnRepository.insertGroup(it) }
                backupObj.learnItems.forEach { learnRepository.insertItem(it) }
                backupObj.learnSections.forEach { learnRepository.insertSection(it) }
                backupObj.ideaGroups.forEach { ideaRepository.insertGroup(it) }
                backupObj.ideas.forEach { ideaRepository.insertIdea(it) }
                backupObj.ideaStages.forEach { ideaRepository.insertStage(it) }
                backupObj.tasks.forEach { taskRepository.insertTask(it) }
                backupObj.habits.forEach { habitRepository.insertHabit(it) }
                backupObj.habitLogs.forEach { habitRepository.insertLog(it) }
                backupObj.sleepLogs.forEach { sleepLogRepository.insertSleepLog(it) }
                backupObj.timerSessions.forEach { timerRepository.insertSession(it) }
                backupObj.timerTemplates.forEach { timerRepository.insertTemplate(it) }
                backupObj.todos.forEach { todoRepository.insertTodo(it) }
                backupObj.diaryEntries.forEach { diaryRepository.insertEntry(it) }
                backupObj.shopItems.forEach { shopItemRepository.insertItem(it) }
                backupObj.mottos.forEach { mottoRepository.insertMotto(it) }
                backupObj.dayReviews.forEach { dayReviewRepository.insertReview(it) }

                // FK orphan nullification (runs outside transaction to avoid nested transaction in DAO calls)
                val taskIds = backupObj.tasks.map { it.id }.toSet()
                val todoIds = backupObj.todos.map { it.id }.toSet()
                val ideaIds = backupObj.ideas.map { it.id }.toSet()
                val learnSectionIds = backupObj.learnSections.map { it.id }.toSet()

                backupObj.tasks
                    .filter { it.linkedTodoId != null && it.linkedTodoId !in todoIds }
                    .forEach { taskRepository.updateTask(it.copy(linkedTodoId = null)) }
                backupObj.tasks
                    .filter { it.linkedIdeaId != null && it.linkedIdeaId !in ideaIds }
                    .forEach { taskRepository.updateTask(it.copy(linkedIdeaId = null)) }
                backupObj.tasks
                    .filter { it.linkedLearnSectionId != null && it.linkedLearnSectionId !in learnSectionIds }
                    .forEach { taskRepository.updateTask(it.copy(linkedLearnSectionId = null)) }
                backupObj.todos
                    .filter { it.linkedTaskId != null && it.linkedTaskId !in taskIds }
                    .forEach { todoRepository.updateTodo(it.copy(linkedTaskId = null)) }
                backupObj.ideas
                    .filter { it.linkedTaskId != null && it.linkedTaskId !in taskIds }
                    .forEach { ideaRepository.updateIdea(it.copy(linkedTaskId = null)) }
                backupObj.learnSections
                    .filter { it.studyTaskId != null && it.studyTaskId !in taskIds }
                    .forEach { learnRepository.updateSection(it.copy(studyTaskId = null)) }
                backupObj.learnSections
                    .filter { it.reviewTaskId != null && it.reviewTaskId !in taskIds }
                    .forEach { learnRepository.updateSection(it.copy(reviewTaskId = null)) }

                com.example.core.manager.SystemSettingsApplier.reapplyAfterRestore(context)

                // Re-schedule all alarms
                val eventVibrate = prefs.getBoolean("event_reminder_vibrate", true)
                val eventSound = prefs.getBoolean("event_reminder_sound", true)
                backupObj.tasks.forEach { task ->
                    if (task.type == "EVENT" && task.eventTime != null) {
                        com.example.core.manager.ReminderManager.scheduleReminders(context, task, eventVibrate, eventSound)
                    }
                }
                backupObj.habits.forEach { habit ->
                    if (habit.habitTime != null && habit.reminderEnabled) {
                        com.example.core.manager.ReminderManager.scheduleHabitReminder(context, habit, eventVibrate, eventSound)
                    }
                }
                com.example.core.manager.ReminderManager.rescheduleAllAlarms(context)

                delay(1500)
                val sourceLabel = if (fromDrive) "Google Drive" else "local backup"
                onResult(true, "Successfully restored ${backupObj.tasks.size} intentions, ${backupObj.habits.size} habits, ${backupObj.learnItems.size} learn items, and logs from $sourceLabel!")

            } catch (e: Exception) {
                Log.e(TAG, "Restore failed", e)
                onResult(false, "Restore failed: ${e.localizedMessage}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    // LLM Export
    fun exportForLlm(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val exportJson = buildLlmExportJson()
                writeToDownloads(exportJson)
                onResult(true, "Exported to Downloads/bulletcoach_llm_export.json")
            } catch (e: Exception) {
                Log.e(TAG, "LLM export failed", e)
                onResult(false, "Export failed: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun buildLlmExportJson(): String {
        val tasks = taskRepository.getAllTasks().first()
        val habits = habitRepository.allHabits.first()
        val habitLogs = habitRepository.getAllLogs().first()
        val sleepLogs = sleepLogRepository.allSleepLogs.first()
        val ideaGroups = ideaRepository.allGroups.first()
        val ideas = ideaRepository.getAllIdeas().first()
        val ideaStages = ideas.flatMap { ideaRepository.getStagesForIdeaSync(it.id) }
        val todos = todoRepository.allTodos.first()
        val diaryEntries = diaryRepository.getAllEntries().first()
        val shopItems = shopItemRepository.allItems.first()
        val mottos = mottoRepository.allMottos.first()
        val dayReviews = dayReviewRepository.getAllReviews().first()
        val learnGroups = learnRepository.getAllGroupsSync()
        val learnItemsList = learnItems.value
        val learnSections = learnItemsList.flatMap { learnRepository.getSectionsForItemSync(it.id) }
        val timerSessions = timerRepository.getAllSessions().first()

        val summary = buildSummary(tasks, habits, habitLogs, sleepLogs, dayReviews, learnItemsList, timerSessions, diaryEntries)
        val entities = buildEntitiesJson(tasks, habits, habitLogs, sleepLogs, ideaGroups, ideas, ideaStages, todos, diaryEntries, shopItems, mottos, dayReviews, learnGroups, learnItemsList, learnSections, timerSessions)
        val settings = buildStructuredSettings()

        val exportData = mapOf(
            "backupVersion" to 1,
            "createdAt" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()),
            "summary" to summary,
            "entities" to entities,
            "settings" to settings
        )

        val adapter = moshi.adapter(Map::class.java)
        return adapter.toJson(exportData)
    }

    private fun buildSummary(
        tasks: List<TaskEntity>,
        habits: List<HabitEntity>,
        habitLogs: List<HabitLogEntity>,
        sleepLogs: List<SleepLogEntity>,
        dayReviews: List<DayReviewEntity>,
        learnItems: List<LearnItemEntity>,
        timerSessions: List<TimerSessionEntity>,
        diaryEntries: List<DiaryEntryEntity>
    ): Map<String, Any> {
        val completedTasks = tasks.count { it.status == "COMPLETED" }
        val totalTasks = tasks.size
        val completionRate = if (totalTasks > 0) completedTasks.toDouble() / totalTasks else 0.0

        val longestStreak = computeLongestStreak(habits, habitLogs)
        val avgSleep = computeAvgSleep(sleepLogs)
        val avgMood = computeAvgMood(dayReviews)
        val learnInProgress = learnItems.count { it.status == "IN_PROGRESS" }
        val pomodorosCompleted = timerSessions.count { it.type == "POMODORO" }

        return mapOf(
            "totalTasks" to totalTasks,
            "completedTasks" to completedTasks,
            "completionRate" to completionRate,
            "activeHabits" to habits.size,
            "totalDiaryEntries" to diaryEntries.size,
            "habitStreakDays" to longestStreak,
            "averageSleepHours" to avgSleep,
            "averageMoodRating" to avgMood,
            "learnItemsInProgress" to learnInProgress,
            "totalPomodorosCompleted" to pomodorosCompleted
        )
    }

    private fun computeLongestStreak(habits: List<HabitEntity>, logs: List<HabitLogEntity>): Int {
        var maxStreak = 0
        for (habit in habits) {
            val habitLogs = logs.filter { it.habitId == habit.id }.map { it.date }.distinct().sorted()
            var currentStreak = 0
            var lastDate: String? = null
            for (date in habitLogs) {
                if (lastDate == null || isConsecutiveDay(lastDate, date)) {
                    currentStreak++
                    maxStreak = maxOf(maxStreak, currentStreak)
                } else {
                    currentStreak = 1
                }
                lastDate = date
            }
        }
        return maxStreak
    }

    private fun isConsecutiveDay(prev: String, curr: String): Boolean {
        try {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val prevDate = fmt.parse(prev)
            val currDate = fmt.parse(curr)
            val diff = (currDate?.time ?: 0) - (prevDate?.time ?: 0)
            return diff == 24L * 60 * 60 * 1000
        } catch (_: Exception) {
            return false
        }
    }

    private fun computeAvgSleep(logs: List<SleepLogEntity>): Double {
        if (logs.isEmpty()) return 0.0
        return logs.map { it.hoursSlept }.average()
    }

    private fun computeAvgMood(reviews: List<DayReviewEntity>): Double {
        if (reviews.isEmpty()) return 0.0
        return reviews.map { it.moodRating.toDouble() }.average()
    }

    private fun buildEntitiesJson(
        tasks: List<TaskEntity>,
        habits: List<HabitEntity>,
        habitLogs: List<HabitLogEntity>,
        sleepLogs: List<SleepLogEntity>,
        ideaGroups: List<IdeaGroupEntity>,
        ideas: List<IdeaEntity>,
        ideaStages: List<IdeaStageEntity>,
        todos: List<TodoEntity>,
        diaryEntries: List<DiaryEntryEntity>,
        shopItems: List<ShopItemEntity>,
        mottos: List<MottoEntity>,
        dayReviews: List<DayReviewEntity>,
        learnGroups: List<LearnGroupEntity>,
        learnItems: List<LearnItemEntity>,
        learnSections: List<LearnSectionEntity>,
        timerSessions: List<TimerSessionEntity>
    ): Map<String, Any> {
        val tasksWithSubtasks = tasks.map { task ->
            val subtasks = tasks.filter { it.parentTaskId == task.id }
            mapOf(
                "task" to task,
                "subtasks" to subtasks
            )
        }

        val ideasWithStages = ideas.map { idea ->
            val stages = ideaStages.filter { it.ideaId == idea.id }.sortedBy { it.orderIndex }
            mapOf(
                "idea" to idea,
                "stages" to stages
            )
        }

        val learnItemsWithSections = learnItems.map { item ->
            val sections = learnSections.filter { it.learnItemId == item.id }.sortedBy { it.orderIndex }
            mapOf(
                "learnItem" to item,
                "sections" to sections
            )
        }

        return mapOf(
            "tasks" to tasksWithSubtasks,
            "habits" to habits,
            "habitLogs" to habitLogs,
            "sleepLogs" to sleepLogs,
            "ideaGroups" to ideaGroups,
            "ideas" to ideasWithStages,
            "todos" to todos,
            "diaryEntries" to diaryEntries,
            "shopItems" to shopItems,
            "mottos" to mottos,
            "dayReviews" to dayReviews,
            "learnGroups" to learnGroups,
            "learnItems" to learnItemsWithSections,
            "timerSessions" to timerSessions
        )
    }

    private fun buildStructuredSettings(): Map<String, Any> {
        return mapOf(
            "usePersianCalendar" to _usePersianCalendar.value,
            "autoSortEnabled" to prefs.getBoolean("auto_sort_enabled", false),
            "reminders" to mapOf(
                "dayReview" to mapOf(
                    "enabled" to prefs.getBoolean("reminder_day_review_enabled", true),
                    "time" to prefs.getString("reminder_day_review_time", "20:00")
                ),
                "habitCheckin" to mapOf(
                    "enabled" to prefs.getBoolean("reminder_habit_checkin_enabled", false),
                    "time" to prefs.getString("reminder_habit_checkin_time", "09:00")
                ),
                "pomodoroBreak" to mapOf(
                    "enabled" to prefs.getBoolean("reminder_pomodoro_break_enabled", true),
                    "time" to prefs.getString("reminder_pomodoro_break_time", "")
                ),
                "sleepLog" to mapOf(
                    "enabled" to prefs.getBoolean("reminder_sleep_log_enabled", false),
                    "time" to prefs.getString("reminder_sleep_log_time", "22:00")
                ),
                "waterReminder" to mapOf(
                    "enabled" to prefs.getBoolean("reminder_water_enabled", false),
                    "intervalMinutes" to prefs.getInt("reminder_water_interval_minutes", 60)
                ),
                "postureReminder" to mapOf(
                    "enabled" to prefs.getBoolean("reminder_posture_enabled", false),
                    "intervalMinutes" to prefs.getInt("reminder_posture_interval_minutes", 60)
                ),
                "custom" to prefs.getString("reminder_custom_list", "[]")
            ),
            "pomodoro" to mapOf(
                "dndEnabled" to _dndEnabled.value,
                "ringtoneEnabled" to _pomodoroRingtoneEnabled.value,
                "vibrateEnabled" to _pomodoroVibrateEnabled.value,
                "defaultBreakMinutes" to _defaultBreakMinutes.value
            ),
            "eventReminders" to mapOf(
                "enabled" to prefs.getBoolean("event_reminders_enabled", true),
                "vibrate" to prefs.getBoolean("event_reminders_vibrate", true),
                "sound" to prefs.getString("event_reminders_sound", "default")
            )
        )
    }

    private fun writeToDownloads(jsonString: String) {
        val fileName = "bulletcoach_llm_export.json"
        val jsonBytes = jsonString.toByteArray(StandardCharsets.UTF_8)

        if (Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) { // API 33+
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { it.write(jsonBytes) }
            } else {
                throw IOException("Failed to create MediaStore entry for Downloads")
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { it.write(jsonBytes) }
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

    // Habit Logs for today (used in Stats screen)
    val todayHabitLogs: StateFlow<List<HabitLogEntity>> = _todayDate.flatMapLatest { date ->
        habitRepository.getLogsForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All habit logs (used for line graph in Stats screen)
    val allHabitLogs: StateFlow<List<HabitLogEntity>> = habitRepository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All sleep logs (for history tab)
    val allSleepLogs: StateFlow<List<SleepLogEntity>> = sleepLogRepository.allSleepLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    private val _pomodoroShortBreakMinutes = MutableStateFlow<Int?>(_defaultBreakMinutes.value)
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

    private var _chronoSelectedTaskId = MutableStateFlow<Long?>(null)
    val chronoSelectedTaskId: StateFlow<Long?> = _chronoSelectedTaskId.asStateFlow()

    private var timerServiceJob: Job? = null
    private var _pomodoroProcessedCompletion = false

    // Timer Templates
    val timerTemplates: StateFlow<List<TimerTemplateEntity>> = timerRepository.getAllTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTimerSessions: StateFlow<List<TimerSessionEntity>> = timerRepository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Timer Sessions (history)
    private val _historyDateRange = MutableStateFlow("today")
    private val _historySelectedDate = MutableStateFlow<String?>(null)

    val timerHistorySessions: StateFlow<List<TimerSessionEntity>> = combine(
        _historyDateRange,
        _historySelectedDate
    ) { range, specificDate ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        when (range) {
            "today" -> todayStr to todayStr
            "week" -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -7)
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time) to todayStr
            }
            "month" -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -1)
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time) to todayStr
            }
            "specific_date" -> {
                val date = specificDate ?: todayStr
                date to date
            }
            else -> todayStr to todayStr
        }
    }.flatMapLatest { (start, end) ->
        timerRepository.getSessionsForDateRange(start, end)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setHistoryDateRange(range: String) {
        if (range != "specific_date") {
            _historySelectedDate.value = null
        }
        _historyDateRange.value = range
    }

    fun selectHistoryDate(gregorianDate: String) {
        _historySelectedDate.value = gregorianDate
        _historyDateRange.value = "specific_date"
    }

    val historySelectedDate: StateFlow<String?> = _historySelectedDate.asStateFlow()

    fun clearHistoryDateSelection() {
        _historySelectedDate.value = null
        _historyDateRange.value = "today"
    }

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

    // Pending task completion with subtask confirmation
    private val _pendingTaskCompletion = MutableStateFlow<PendingTaskCompletion?>(null)
    val pendingTaskCompletion: StateFlow<PendingTaskCompletion?> = _pendingTaskCompletion.asStateFlow()

    private val _pendingSubTodoCompletion = MutableStateFlow<PendingSubTodoCompletion?>(null)
    val pendingSubTodoCompletion: StateFlow<PendingSubTodoCompletion?> = _pendingSubTodoCompletion.asStateFlow()

    fun confirmCompleteTask(completeChildren: Boolean) {
        val pending = _pendingTaskCompletion.value ?: return
        _pendingTaskCompletion.value = null
        viewModelScope.launch {
            val (task, subtasks, durationMinutes, todoId, startHour, startMinute, endHour, endMinute) = pending

            if (completeChildren) {
                subtasks.filter { it.status != "COMPLETED" }.forEach {
                    taskRepository.updateTask(it.copy(status = "COMPLETED"))
                }
            }

            val effectiveDuration = if (startHour != null && startMinute != null && endHour != null && endMinute != null) {
                (endHour * 60 + endMinute - startHour * 60 - startMinute).coerceAtLeast(0)
            } else {
                durationMinutes
            }

            val updated = if (effectiveDuration > 0) {
                task.copy(
                    status = "COMPLETED",
                    durationMinutes = effectiveDuration,
                    pomodorosCompleted = task.pomodorosCompleted + 1
                )
            } else {
                task.copy(status = "COMPLETED")
            }
            taskRepository.updateTask(updated)

            handleLearnTaskToggle(original = task, updated = updated)

            updated.linkedTodoId?.let { linkedId ->
                val linkedTodo = todoRepository.getTodoById(linkedId)
                if (linkedTodo != null && linkedTodo.status != "DONE") {
                    todoRepository.updateTodo(linkedTodo.copy(status = "DONE"))
                    if (completeChildren) {
                        val linkedSubTodos = todoRepository.getSubTodosSync(linkedTodo.id)
                        linkedSubTodos.filter { it.status != "DONE" }.forEach {
                            todoRepository.updateTodo(it.copy(status = "DONE"))
                        }
                    }
                }
            }

            if (effectiveDuration > 0) {
                val timestamp = computeStartTimestamp(startHour, startMinute)
                timerRepository.insertSession(
                    TimerSessionEntity(
                        type = "POMODORO",
                        taskId = task.id,
                        label = task.label,
                        durationSeconds = effectiveDuration * 60,
                        date = getTodayDateString(),
                        timestamp = timestamp
                    )
                )
            }

            todoId?.let { tid ->
                if (tid == updated.linkedTodoId) return@let
                val todo = todoRepository.getTodoById(tid)
                if (todo != null) {
                    todoRepository.updateTodo(todo.copy(status = "DONE"))
                }
            }
        }
    }

    fun cancelPendingTaskCompletion() {
        _pendingTaskCompletion.value = null
    }

    // === Sub-To-Do Operations ===

    fun confirmCompleteTodoWithSubtodos(completeChildren: Boolean) {
        val pending = _pendingSubTodoCompletion.value ?: return
        _pendingSubTodoCompletion.value = null
        viewModelScope.launch {
            try {
                val (todo, subTodos) = pending
                if (completeChildren) {
                    subTodos.filter { it.status != "DONE" }.forEach {
                        todoRepository.updateTodo(it.copy(status = "DONE"))
                    }
                }
                todoRepository.updateTodo(todo.copy(status = "DONE"))
                if (todo.linkedTaskId != null) {
                    val linkedTask = taskRepository.getTaskById(todo.linkedTaskId)
                    if (linkedTask != null && linkedTask.status != "COMPLETED") {
                        val subtasks = taskRepository.getSubtasks(linkedTask.id)
                        val incompleteSubtasks = subtasks.filter { it.status != "COMPLETED" }
                        if (incompleteSubtasks.isEmpty()) {
                            taskRepository.updateTask(linkedTask.copy(status = "COMPLETED"))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to confirm complete todo with subtodos", e)
            }
        }
    }

    fun cancelPendingSubTodoCompletion() {
        _pendingSubTodoCompletion.value = null
    }

    fun addSubTodo(parentTodo: TodoEntity, title: String, importance: String = "OPTIONAL") {
        if (title.isBlank()) return
        viewModelScope.launch {
            try {
                val allTodos = todoRepository.getAllTodosSync()
                val nextOrder = (allTodos.maxOfOrNull { it.sortOrder } ?: -1) + 1
                todoRepository.insertTodo(
                    TodoEntity(
                        title = title.trim(),
                        description = "",
                        priority = parentTodo.priority,
                        parentTodoId = parentTodo.id,
                        status = "PENDING",
                        subtaskImportance = importance,
                        sortOrder = nextOrder
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add subtodo", e)
            }
        }
    }

    fun toggleSubTodoCompletion(subTodo: TodoEntity) {
        viewModelScope.launch {
            try {
                val newStatus = if (subTodo.status == "DONE") "PENDING" else "DONE"
                todoRepository.updateTodo(subTodo.copy(status = newStatus))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle subtodo completion", e)
            }
        }
    }

    fun deleteSubTodo(subTodo: TodoEntity) {
        viewModelScope.launch {
            try {
                todoRepository.deleteTodo(subTodo)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete subtodo", e)
            }
        }
    }

    private var originalInterruptionFilter = NotificationManager.INTERRUPTION_FILTER_ALL

    // App Usage Stats State
    private val _appUsageItems = MutableStateFlow<List<AppUsageItem>>(emptyList())
    val appUsageItems: StateFlow<List<AppUsageItem>> = _appUsageItems.asStateFlow()

    private val _totalScreenTimeMinutes = MutableStateFlow(0L)
    val totalScreenTimeMinutes: StateFlow<Long> = _totalScreenTimeMinutes.asStateFlow()

    private val _screenTimeError = MutableStateFlow<String?>(null)
    val screenTimeError: StateFlow<String?> = _screenTimeError.asStateFlow()

    private val _screenTimeLoading = MutableStateFlow(false)
    val screenTimeLoading: StateFlow<Boolean> = _screenTimeLoading.asStateFlow()

    private val _screenTimeLastUpdated = MutableStateFlow<Long?>(null)
    val screenTimeLastUpdated: StateFlow<Long?> = _screenTimeLastUpdated.asStateFlow()

    private val appLabelCache = ConcurrentHashMap<String, String>()

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

    // === Learn State ===
    val learnItems: StateFlow<List<LearnItemEntity>> = learnRepository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val learnGroups: StateFlow<List<LearnGroupEntity>> = learnRepository.allGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addLearnGroup(name: String, color: Long) {
        viewModelScope.launch {
            try {
                val all = learnRepository.getAllGroupsSync()
                val nextOrder = (all.maxOfOrNull { it.sortOrder } ?: -1) + 1
                learnRepository.insertGroup(LearnGroupEntity(name = name.trim(), color = color, sortOrder = nextOrder))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add learn group", e)
            }
        }
    }

    fun updateLearnGroup(group: LearnGroupEntity) {
        viewModelScope.launch {
            try {
                learnRepository.updateGroup(group)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update learn group", e)
            }
        }
    }

    fun deleteLearnGroup(group: LearnGroupEntity) {
        viewModelScope.launch {
            try {
                learnRepository.ungroupItemsByGroupId(group.id)
                learnRepository.deleteGroup(group)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete learn group", e)
            }
        }
    }

    fun moveLearnItemToGroup(itemId: Long, newGroupId: Long?) {
        viewModelScope.launch {
            try {
                learnRepository.moveItemToGroup(itemId, newGroupId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to move learn item to group", e)
            }
        }
    }

    fun sectionsForLearnItem(itemId: Long) = learnRepository.getSectionsForItem(itemId)

    fun archiveLearnItem(item: LearnItemEntity) {
        viewModelScope.launch {
            try {
                learnRepository.updateItem(item.copy(status = "ARCHIVED"))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to archive learn item", e)
            }
        }
    }

    suspend fun getReviewStageForTaskId(taskId: Long): Int {
        val section = learnRepository.getSectionByReviewTaskId(taskId)
            ?: learnRepository.getSectionByStudyTaskId(taskId)
        return section?.reviewStage ?: -1
    }

    // === Day Review State ===
    private val _reviewReminderTime = MutableStateFlow(prefs.getString("review_reminder_time", "21:00") ?: "21:00")
    val reviewReminderTime: StateFlow<String> = _reviewReminderTime.asStateFlow()

    private val _reviewReminderEnabled = MutableStateFlow(prefs.getBoolean("review_reminder_enabled", false))
    val reviewReminderEnabled: StateFlow<Boolean> = _reviewReminderEnabled.asStateFlow()

    // === Sleep Reminder State ===
    private val _sleepReminderTime = MutableStateFlow(prefs.getString("sleep_reminder_time", "09:00") ?: "09:00")
    val sleepReminderTime: StateFlow<String> = _sleepReminderTime.asStateFlow()
    private val _sleepReminderEnabled = MutableStateFlow(prefs.getBoolean("sleep_reminder_enabled", false))
    val sleepReminderEnabled: StateFlow<Boolean> = _sleepReminderEnabled.asStateFlow()

    // === Diary Reminder State ===
    private val _diaryReminderTime = MutableStateFlow(prefs.getString("diary_reminder_time", "20:00") ?: "20:00")
    val diaryReminderTime: StateFlow<String> = _diaryReminderTime.asStateFlow()
    private val _diaryReminderEnabled = MutableStateFlow(prefs.getBoolean("diary_reminder_enabled", false))
    val diaryReminderEnabled: StateFlow<Boolean> = _diaryReminderEnabled.asStateFlow()

    // === Morning Planner Reminder State ===
    private val _plannerReminderTime = MutableStateFlow(prefs.getString("planner_reminder_time", "07:00") ?: "07:00")
    val plannerReminderTime: StateFlow<String> = _plannerReminderTime.asStateFlow()
    private val _plannerReminderEnabled = MutableStateFlow(prefs.getBoolean("planner_reminder_enabled", false))
    val plannerReminderEnabled: StateFlow<Boolean> = _plannerReminderEnabled.asStateFlow()

    // === Habits Check-in Reminder State ===
    private val _habitsReminderTime = MutableStateFlow(prefs.getString("habits_reminder_time", "21:00") ?: "21:00")
    val habitsReminderTime: StateFlow<String> = _habitsReminderTime.asStateFlow()
    private val _habitsReminderEnabled = MutableStateFlow(prefs.getBoolean("habits_reminder_enabled", false))
    val habitsReminderEnabled: StateFlow<Boolean> = _habitsReminderEnabled.asStateFlow()

    // === Tomorrow Planner Reminder State ===
    private val _tomorrowPlannerReminderTime = MutableStateFlow(prefs.getString("tomorrow_planner_reminder_time", "20:00") ?: "20:00")
    val tomorrowPlannerReminderTime: StateFlow<String> = _tomorrowPlannerReminderTime.asStateFlow()
    private val _tomorrowPlannerReminderEnabled = MutableStateFlow(prefs.getBoolean("tomorrow_planner_reminder_enabled", false))
    val tomorrowPlannerReminderEnabled: StateFlow<Boolean> = _tomorrowPlannerReminderEnabled.asStateFlow()

    // === Learn Review Reminder State ===
    private val _learnReviewReminderTime = MutableStateFlow(prefs.getString("learn_review_reminder_time", "19:00") ?: "19:00")
    val learnReviewReminderTime: StateFlow<String> = _learnReviewReminderTime.asStateFlow()
    private val _learnReviewReminderEnabled = MutableStateFlow(prefs.getBoolean("learn_review_reminder_enabled", false))
    val learnReviewReminderEnabled: StateFlow<Boolean> = _learnReviewReminderEnabled.asStateFlow()

    // === Deep-link More Screen State ===
    private val _pendingMoreScreen = MutableStateFlow<String?>(null)
    val pendingMoreScreen: StateFlow<String?> = _pendingMoreScreen.asStateFlow()

    fun setPendingMoreScreen(screen: String) {
        _pendingMoreScreen.value = screen
    }

    fun consumePendingMoreScreen() {
        _pendingMoreScreen.value = null
    }

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
        if (!hasNotificationPermission(context)) return
        createDayReviewChannel(context)
        val intent = Intent(context, com.example.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to_tab", 4)
            putExtra("open_more_screen", "DayReview")
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

    private fun createSleepReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "sleep_reminder", "Sleep Log Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Daily reminder to log your sleep" }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    private fun createDiaryReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "diary_reminder", "Diary Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Daily reminder to write in your diary" }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    private fun createPlannerReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "planner_reminder", "Morning Planner Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Daily morning summary of your day" }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    private fun createHabitsReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "habits_reminder", "Habits Check-in Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Daily reminder to check your habits" }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    private fun createTomorrowPlannerReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "tomorrow_planner_reminder", "Tomorrow Planner Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Evening reminder to plan tomorrow" }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    private fun scheduleReminderAlarm(context: Context, action: String, requestCode: Int, time: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, com.example.core.receiver.ReminderReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            getImmutableFlag() or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val timeParts = time.split(":")
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
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelReminderAlarm(context: Context, action: String, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, com.example.core.receiver.ReminderReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            getImmutableFlag() or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun scheduleSleepReminderAlarm(context: Context) {
        scheduleReminderAlarm(context, "com.example.action.SLEEP_REMINDER", 6000, _sleepReminderTime.value)
    }

    fun cancelSleepReminderAlarm(context: Context) {
        cancelReminderAlarm(context, "com.example.action.SLEEP_REMINDER", 6000)
    }

    fun scheduleDiaryReminderAlarm(context: Context) {
        scheduleReminderAlarm(context, "com.example.action.DIARY_REMINDER", 7000, _diaryReminderTime.value)
    }

    fun cancelDiaryReminderAlarm(context: Context) {
        cancelReminderAlarm(context, "com.example.action.DIARY_REMINDER", 7000)
    }

    fun schedulePlannerReminderAlarm(context: Context) {
        scheduleReminderAlarm(context, "com.example.action.PLANNER_REMINDER", 8000, _plannerReminderTime.value)
    }

    fun cancelPlannerReminderAlarm(context: Context) {
        cancelReminderAlarm(context, "com.example.action.PLANNER_REMINDER", 8000)
    }

    fun scheduleHabitsReminderAlarm(context: Context) {
        scheduleReminderAlarm(context, "com.example.action.HABITS_REMINDER", 9000, _habitsReminderTime.value)
    }

    fun cancelHabitsReminderAlarm(context: Context) {
        cancelReminderAlarm(context, "com.example.action.HABITS_REMINDER", 9000)
    }

    fun scheduleTomorrowPlannerReminderAlarm(context: Context) {
        scheduleReminderAlarm(context, "com.example.action.TOMORROW_PLANNER_REMINDER", 10000, _tomorrowPlannerReminderTime.value)
    }

    fun cancelTomorrowPlannerReminderAlarm(context: Context) {
        cancelReminderAlarm(context, "com.example.action.TOMORROW_PLANNER_REMINDER", 10000)
    }

    fun scheduleLearnReviewReminderAlarm(context: Context) {
        scheduleReminderAlarm(context, "com.example.action.LEARN_REVIEW_REMINDER", 11000, _learnReviewReminderTime.value)
    }

    fun cancelLearnReviewReminderAlarm(context: Context) {
        cancelReminderAlarm(context, "com.example.action.LEARN_REVIEW_REMINDER", 11000)
    }

    fun sendImmediateLearnReviewReminderNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        createLearnReviewReminderChannel(context)
        val intent = Intent(context, com.example.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to_tab", 0)
        }
        val pendingIntent = PendingIntent.getActivity(context, 11001, intent, getImmutableFlag())
        val notification = NotificationCompat.Builder(context, "learn_review_reminder")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Learn Reviews Due")
            .setContentText("You have learn reviews waiting — check your planner!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(11001, notification)
    }

    private fun createLearnReviewReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "learn_review_reminder",
                "Learn Review Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Daily reminder for pending learn reviews" }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    fun sendImmediateSleepReminderNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        createSleepReminderChannel(context)
        val intent = Intent(context, com.example.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to_tab", 1)
        }
        val pendingIntent = PendingIntent.getActivity(context, 6001, intent, getImmutableFlag())
        val notification = NotificationCompat.Builder(context, "sleep_reminder")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Sleep Log Reminder")
            .setContentText("Did you log your sleep last night?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(6000, notification)
    }

    fun sendImmediateDiaryReminderNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        createDiaryReminderChannel(context)
        val intent = Intent(context, com.example.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to_tab", 4)
            putExtra("open_more_screen", "Diary")
        }
        val pendingIntent = PendingIntent.getActivity(context, 7001, intent, getImmutableFlag())
        val notification = NotificationCompat.Builder(context, "diary_reminder")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Diary Reminder")
            .setContentText("Write about your day — capture your thoughts")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(7000, notification)
    }

    fun sendImmediatePlannerReminderNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        createPlannerReminderChannel(context)
        val todayStr = getTodayDateString()
        viewModelScope.launch {
            val tasks = taskRepository.getTasksForDate(todayStr).first()
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
            val intent = Intent(context, com.example.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to_tab", 0)
            }
            val pendingIntent = PendingIntent.getActivity(context, 8001, intent, getImmutableFlag())
            val notification = NotificationCompat.Builder(context, "planner_reminder")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Morning Planner Summary")
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(8000, notification)
        }
    }

    fun sendImmediateHabitsReminderNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        createHabitsReminderChannel(context)
        val todayStr = getTodayDateString()
        viewModelScope.launch {
            val allHabits = habitRepository.allHabits.first()
            if (allHabits.isEmpty()) return@launch
            val logs = habitRepository.getLogsForDate(todayStr).first()
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
            val intent = Intent(context, com.example.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to_tab", 1)
            }
            val pendingIntent = PendingIntent.getActivity(context, 9001, intent, getImmutableFlag())
            val notification = NotificationCompat.Builder(context, "habits_reminder")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Habits Check-in")
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(9000, notification)
        }
    }

    fun sendImmediateTomorrowPlannerReminderNotification(context: Context) {
        if (!hasNotificationPermission(context)) return
        createTomorrowPlannerReminderChannel(context)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrowStr = sdf.format(cal.time)
        viewModelScope.launch {
            val tasks = taskRepository.getTasksForDate(tomorrowStr).first()
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
            val intent = Intent(context, com.example.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to_tab", 0)
                putExtra("open_date", tomorrowStr)
            }
            val pendingIntent = PendingIntent.getActivity(context, 10001, intent, getImmutableFlag())
            val notification = NotificationCompat.Builder(context, "tomorrow_planner_reminder")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Plan Tomorrow")
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(10000, notification)
        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.registerReceiver(dateChangeReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
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

        collectTimerServiceState()
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
        duration: Int = 0, label: String = "", 
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

            // Sync linked todo's sub-todos with importance
            updatedTask.linkedTodoId?.let { todoId ->
                todoRepository.getTodoById(todoId)?.let { linkedTodo ->
                    val existingSubTodos = todoRepository.getSubTodosSync(todoId)
                    existingSubTodos.forEach { todoRepository.deleteTodo(it) }
                    subtasks.forEach { (subTitle, importance) ->
                        if (subTitle.isNotBlank()) {
                            todoRepository.insertTodo(
                                TodoEntity(
                                    title = subTitle.trim(),
                                    description = "",
                                    priority = linkedTodo.priority,
                                    parentTodoId = todoId,
                                    status = "PENDING",
                                    subtaskImportance = importance,
                                    sortOrder = (todoRepository.getAllTodosSync().maxOfOrNull { it.sortOrder } ?: -1) + 1
                                )
                            )
                        }
                    }
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
                val incompleteSubtasks = subtasks.filter { it.status != "COMPLETED" }
                if (incompleteSubtasks.isNotEmpty()) {
                    _pendingTaskCompletion.value = PendingTaskCompletion(
                        task = task, subtasks = subtasks
                    )
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
            handleLearnTaskToggle(original = task, updated = updated)
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

    fun completeTaskWithManualDuration(
        task: TaskEntity,
        durationMinutes: Int,
        startHour: Int? = null,
        startMinute: Int? = null,
        endHour: Int? = null,
        endMinute: Int? = null
    ) {
        viewModelScope.launch {
            val effectiveDuration = if (startHour != null && startMinute != null && endHour != null && endMinute != null) {
                (endHour * 60 + endMinute - startHour * 60 - startMinute).coerceAtLeast(0)
            } else {
                durationMinutes
            }

            val subtasks = taskRepository.getSubtasks(task.id)
            val incompleteSubtasks = subtasks.filter { it.status != "COMPLETED" }
            if (incompleteSubtasks.isNotEmpty()) {
                _pendingTaskCompletion.value = PendingTaskCompletion(
                    task = task, subtasks = subtasks,
                    durationMinutes = effectiveDuration,
                    startHour = startHour, startMinute = startMinute,
                    endHour = endHour, endMinute = endMinute
                )
                return@launch
            }
            val updated = task.copy(
                durationMinutes = effectiveDuration,
                status = "COMPLETED",
                pomodorosCompleted = task.pomodorosCompleted + 1
            )
            taskRepository.updateTask(updated)

            handleLearnTaskToggle(original = task, updated = updated)

            updated.linkedTodoId?.let { todoId ->
                val linkedTodo = todoRepository.getTodoById(todoId)
                if (linkedTodo != null && linkedTodo.status != "DONE") {
                    todoRepository.updateTodo(linkedTodo.copy(status = "DONE"))
                }
            }

            if (effectiveDuration > 0) {
                val timestamp = computeStartTimestamp(startHour, startMinute)
                timerRepository.insertSession(
                    TimerSessionEntity(
                        type = "POMODORO",
                        taskId = task.id,
                        label = task.label,
                        durationSeconds = effectiveDuration * 60,
                        date = getTodayDateString(),
                        timestamp = timestamp
                    )
                )
            }
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
            // Sync learn section state when migrating learn-linked tasks
            if (task.linkedLearnSectionId != null) {
                val section = learnRepository.getSectionById(task.linkedLearnSectionId)
                if (section != null && (section.studyTaskId == task.id || section.reviewTaskId == task.id)) {
                    if (task.status == "COMPLETED") {
                        handleLearnTaskToggle(original = task, updated = task.copy(status = "PENDING"))
                    }
                    if (task.label == "Review") {
                        learnRepository.updateSection(section.copy(nextReviewDate = targetDate))
                    }
                }
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

    fun deleteHabitWithUndo(habit: HabitEntity) {
        viewModelScope.launch {
            try {
                val logs = habitRepository.getLogsForHabitSync(habit.id)
                com.example.core.manager.ReminderManager.cancelHabitReminder(context, habit)
                habitRepository.deleteLogsForHabit(habit.id)
                habitRepository.deleteHabit(habit)
                pushUndo(UndoSnapshot.HabitSnapshot(habit, logs), habit.name)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete habit with undo", e)
            }
        }
    }

    fun logHabit(habitId: Long, value: Float, notes: String = "", date: String? = null) {
        viewModelScope.launch {
            val targetDate = date ?: _selectedDate.value
            val currentLog = if (date != null) {
                habitRepository.getLogsForDate(targetDate).first().find { it.habitId == habitId }
            } else {
                habitLogs.value.find { it.habitId == habitId }
            }
            if (currentLog != null) {
                if (value <= 0f) {
                    habitRepository.deleteLogForDate(habitId, targetDate)
                } else {
                    habitRepository.insertLog(currentLog.copy(value = value, notes = notes))
                }
            } else {
                habitRepository.insertLog(HabitLogEntity(habitId = habitId, date = targetDate, value = value, notes = notes))
            }
        }
    }

    fun deleteHabitLog(logId: Long) {
        viewModelScope.launch {
            habitRepository.deleteLogById(logId)
        }
    }

    fun saveSleepLog(hours: Float, quality: Int, bedTime: String, wakeTime: String, notes: String, date: String? = null) {
        viewModelScope.launch {
            val targetDate = date ?: _selectedDate.value
            val existing = sleepLogRepository.getSleepLogForDate(targetDate)
            val log = SleepLogEntity(
                id = existing?.id ?: 0L,
                date = targetDate,
                hoursSlept = hours,
                sleepQuality = quality,
                sleepTime = bedTime,
                wakeTime = wakeTime,
                notes = notes
            )
            sleepLogRepository.insertSleepLog(log)
        }
    }

    fun deleteSleepLog(date: String) {
        viewModelScope.launch {
            sleepLogRepository.deleteSleepLogByDate(date)
        }
    }

    // --- Pomodoro, Chronometer and DND Management ---
    fun startPomodoro(
        context: Context,
        task: TaskEntity,
        focusMinutes: Int = task.durationMinutes,
        targetSessions: Int? = 1,
        shortBreakMinutes: Int? = _defaultBreakMinutes.value,
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

        _pomodoroProcessedCompletion = false
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
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && nm.isNotificationPolicyAccessGranted) {
                originalInterruptionFilter = getCurrentInterruptionFilter(context)
                prefs.edit().putInt(PREFS_KEY_ORIGINAL_DND_FILTER, originalInterruptionFilter).apply()
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            }
        }

        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_START_POMODORO
            putExtra(TimerForegroundService.EXTRA_FOCUS_MINUTES, focusMinutes)
            putExtra(TimerForegroundService.EXTRA_TASK_TITLE, task.title)
            putExtra(TimerForegroundService.EXTRA_TASK_ID, task.id)
            putExtra(TimerForegroundService.EXTRA_SESSION_NUMBER, 1)
            if (shortBreakMinutes != null) putExtra(TimerForegroundService.EXTRA_SHORT_BREAK, shortBreakMinutes)
            if (longBreakMinutes != null) putExtra(TimerForegroundService.EXTRA_LONG_BREAK, longBreakMinutes)
            if (targetSessions != null) putExtra(TimerForegroundService.EXTRA_TARGET_SESSIONS, targetSessions)
            putExtra(TimerForegroundService.EXTRA_MARK_COMPLETE, markCompleteOnFinish)
        }
        context.startService(intent)
    }

    fun pausePomodoro() {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_TOGGLE_PAUSE
        }
        context.startService(intent)
    }

    fun resumePomodoro(context: Context) {
        pausePomodoro()
    }

    fun stopPomodoroEarly(context: Context) {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_STOP
        }
        context.startService(intent)
    }

    fun discardPomodoro(context: Context) {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_STOP
        }
        context.startService(intent)
        resetPomodoroState(context)
    }

    private fun resetPomodoroState(context: Context) {
        _pomodoroRunning.value = false
        _activePomodoroTask.value = null
        _pomodoroSecondsLeft.value = 0
        if (_dndEnabled.value) {
            restoreDndState(context)
        }
    }

    fun resetPomodoro() {
        _pomodoroSecondsLeft.value = _pomodoroFocusMinutes.value * 60
        _pomodoroCurrentSession.value = 1
        _pomodoroPhase.value = "FOCUS"
    }

    fun adjustPomodoroPlusOne() {
        _pomodoroSecondsLeft.value = (_pomodoroSecondsLeft.value + 60).coerceAtMost(7200)
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_ADJUST_POMODORO
            putExtra(TimerForegroundService.EXTRA_ADJUST_SECONDS, 60)
        }
        context.startService(intent)
    }

    private fun collectTimerServiceState() {
        timerServiceJob?.cancel()
        timerServiceJob = viewModelScope.launch {
            TimerForegroundService.state.collect { s ->
                when (s.mode) {
                    TimerMode.POMODORO -> {
                        _pomodoroSecondsLeft.value = s.secondsLeft
                        _pomodoroRunning.value = s.running && !s.paused
                        _pomodoroPhase.value = s.phase
                        _pomodoroCurrentSession.value = s.sessionNumber

                        // Restore settings and task after process death
                        _pomodoroFocusMinutes.value = s.focusMinutes
                        if (s.shortBreakMinutes != null) _pomodoroShortBreakMinutes.value = s.shortBreakMinutes
                        if (s.longBreakMinutes != null) _pomodoroLongBreakMinutes.value = s.longBreakMinutes
                        if (s.targetSessions != null) _pomodoroTargetSessions.value = s.targetSessions
                        _pomodoroMarkCompleteOnFinish.value = s.markCompleteOnFinish
                        if (s.taskId != -1L && _activePomodoroTask.value?.id != s.taskId) {
                            viewModelScope.launch {
                                val restored = taskRepository.getTaskById(s.taskId)
                                if (restored != null) {
                                    _activePomodoroTask.value = restored
                                }
                            }
                        }

                        if (s.completed && !_pomodoroProcessedCompletion) {
                            _pomodoroProcessedCompletion = true
                            handlePhaseCompletion(context)
                            TimerForegroundService.clearCompletedFlag()
                        }
                    }
                    TimerMode.CHRONOMETER -> {
                        _chronoElapsed.value = s.elapsedSeconds
                        _chronoRunning.value = s.running && !s.paused
                        _chronoPaused.value = s.paused

                        // Restore the selected task id after process death
                        if (s.taskId != -1L && _chronoSelectedTaskId.value != s.taskId) {
                            _chronoSelectedTaskId.value = s.taskId
                        }
                    }
                    null -> {
                        if (_pomodoroRunning.value || _activePomodoroTask.value != null) {
                            if (!_pomodoroProcessedCompletion) {
                                _activePomodoroTask.value = null
                                _pomodoroRunning.value = false
                                _pomodoroSecondsLeft.value = 0
                            }
                        }
                        if (_chronoRunning.value) {
                            _chronoRunning.value = false
                            _chronoElapsed.value = 0L
                            _chronoPaused.value = false
                            _chronoSelectedTaskId.value = null
                        }
                        restoreDndState(context)
                    }
                }
            }
        }
    }

    private fun handlePhaseCompletion(context: Context) {
        val task = _activePomodoroTask.value ?: return
        val currentPhase = _pomodoroPhase.value
        val shortBreakMin = _pomodoroShortBreakMinutes.value
        val longBreakMin = _pomodoroLongBreakMinutes.value
        val targetSess = _pomodoroTargetSessions.value
        val currentSess = _pomodoroCurrentSession.value

        _pomodoroRunning.value = false

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
                    val hasIncompleteSubtasks = subtasks.any { it.status != "COMPLETED" }
                    val updated = task.copy(
                        pomodorosCompleted = task.pomodorosCompleted + 1,
                        status = if (hasIncompleteSubtasks) task.status else "COMPLETED"
                    )
                    taskRepository.updateTask(updated)
                    _activePomodoroTask.value = updated
                    if (updated.status == "COMPLETED") {
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

            val isTargetReached = targetSess != null && currentSess >= targetSess
            val isLongBreak = currentSess % 4 == 0
            val breakDuration = if (isLongBreak) longBreakMin else shortBreakMin
            val hasBreak = breakDuration != null && breakDuration > 0

            if (isTargetReached) {
                setCompletionStateAndNotify(context, task, "FOCUS", currentSess, targetSess, _pomodoroFocusMinutes.value * 60, "", 0, false, true, null)
            } else {
                val label: String
                val minutes: Int
                if (hasBreak) {
                    label = "Start Break (${breakDuration}m)"
                    minutes = breakDuration
                } else {
                    label = "Next Focus (${_pomodoroFocusMinutes.value}m)"
                    minutes = _pomodoroFocusMinutes.value
                }
                setCompletionStateAndNotify(context, task, "FOCUS", currentSess, targetSess, _pomodoroFocusMinutes.value * 60, label, minutes, true, false, if (hasBreak) breakDuration else null)
            }
        } else {
            val isTargetReached = targetSess != null && currentSess >= targetSess
            if (isTargetReached) {
                setCompletionStateAndNotify(context, task, "BREAK", currentSess, targetSess, 0, "", 0, false, true, null)
            } else {
                setCompletionStateAndNotify(context, task, "BREAK", currentSess, targetSess, 0, "Next Focus (${_pomodoroFocusMinutes.value}m)", _pomodoroFocusMinutes.value, true, false, null)
            }
        }
    }

    private fun setCompletionStateAndNotify(context: Context, task: TaskEntity, phase: String, sessionNumber: Int, totalSessions: Int?, durationSeconds: Int, nextActionLabel: String, nextActionMinutes: Int, canProceed: Boolean, isFinal: Boolean, breakDuration: Int?) {
        val state = PomodoroCompletionState(
            phase = phase,
            sessionNumber = sessionNumber,
            totalSessions = totalSessions,
            taskTitle = task.title,
            taskId = task.id,
            durationSeconds = durationSeconds,
            nextActionLabel = nextActionLabel,
            nextActionMinutes = nextActionMinutes,
            canProceed = canProceed,
            isFinal = isFinal,
            breakDuration = breakDuration
        )
        _pomodoroCompletionState.value = state
    }

    fun continueFromPomodoroCompletion(context: Context) {
        val state = _pomodoroCompletionState.value ?: return
        val nextActionMinutes = state.nextActionMinutes
        if (nextActionMinutes <= 0) return

        _pomodoroProcessedCompletion = false

        if (state.phase == "FOCUS") {
            if (state.breakDuration != null && state.breakDuration > 0) {
                _pomodoroPhase.value = "BREAK"
                _pomodoroSecondsLeft.value = state.breakDuration * 60
            } else {
                _pomodoroCurrentSession.value = state.sessionNumber + 1
                _pomodoroPhase.value = "FOCUS"
                _pomodoroSecondsLeft.value = _pomodoroFocusMinutes.value * 60
            }
        } else {
            _pomodoroCurrentSession.value = state.sessionNumber + 1
            _pomodoroPhase.value = "FOCUS"
            _pomodoroSecondsLeft.value = _pomodoroFocusMinutes.value * 60
        }
        _pomodoroCompletionState.value = null
        _pomodoroRunning.value = true
        cancelPomodoroNotification(context)

        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_START_POMODORO
            putExtra(TimerForegroundService.EXTRA_FOCUS_MINUTES, _pomodoroFocusMinutes.value)
            putExtra(TimerForegroundService.EXTRA_TASK_TITLE, _activePomodoroTask.value?.title ?: "")
            putExtra(TimerForegroundService.EXTRA_TASK_ID, _activePomodoroTask.value?.id ?: -1L)
            putExtra(TimerForegroundService.EXTRA_SESSION_NUMBER, _pomodoroCurrentSession.value)
            val sb = _pomodoroShortBreakMinutes.value
            if (sb != null) putExtra(TimerForegroundService.EXTRA_SHORT_BREAK, sb)
            val lb = _pomodoroLongBreakMinutes.value
            if (lb != null) putExtra(TimerForegroundService.EXTRA_LONG_BREAK, lb)
            val ts = _pomodoroTargetSessions.value
            if (ts != null) putExtra(TimerForegroundService.EXTRA_TARGET_SESSIONS, ts)
            putExtra(TimerForegroundService.EXTRA_MARK_COMPLETE, _pomodoroMarkCompleteOnFinish.value)
        }
        context.startService(intent)
    }

    fun endPomodoroChain(context: Context) {
        _pomodoroRunning.value = false
        _activePomodoroTask.value = null
        _pomodoroSecondsLeft.value = 0
        _pomodoroCompletionState.value = null
        _pomodoroProcessedCompletion = false
        cancelPomodoroNotification(context)

        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_STOP
        }
        context.startService(intent)

        if (_dndEnabled.value) {
            restoreDndState(context)
        }
    }

    fun handlePomodoroAction(context: Context, action: String) {
        when (action) {
            "continue" -> continueFromPomodoroCompletion(context)
            "end" -> endPomodoroChain(context)
        }
    }

    fun testPomodoroAlarm(context: Context) {
        val intent = Intent(context, com.example.ui.screens.PomodoroFinishActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("phase", "TEST")
            putExtra("taskTitle", "Test Pomodoro")
            putExtra("sessionNumber", 1)
            putExtra("durationSeconds", 1500)
            putExtra("nextActionLabel", "")
            putExtra("canProceed", false)
            putExtra("isFinal", true)
            putExtra("ringtoneUri", _pomodoroRingtoneUri.value)
            putExtra("ringtoneEnabled", _pomodoroRingtoneEnabled.value)
            putExtra("vibrateEnabled", _pomodoroVibrateEnabled.value)
        }
        context.startActivity(intent)
    }

    // --- Chronometer ---
    fun startChronometer(taskId: Long? = null) {
        if (_chronoRunning.value) return
        if (_activePomodoroTask.value != null && _pomodoroRunning.value) return
        _chronoSelectedTaskId.value = taskId
        _chronoElapsed.value = 0L
        _chronoPaused.value = false
        _chronoRunning.value = true
        if (_dndEnabled.value) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && nm.isNotificationPolicyAccessGranted) {
                originalInterruptionFilter = getCurrentInterruptionFilter(context)
                prefs.edit().putInt(PREFS_KEY_ORIGINAL_DND_FILTER, originalInterruptionFilter).apply()
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            }
        }

        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_START_CHRONOMETER
            putExtra(TimerForegroundService.EXTRA_CHRONO_TASK_ID, taskId ?: -1L)
        }
        context.startService(intent)
    }

    fun pauseChronometer() {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_TOGGLE_PAUSE
        }
        context.startService(intent)
    }

    fun stopChronometer() {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_STOP
        }
        context.startService(intent)
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
            restoreDndState(context)
        }
    }

    fun discardChronometer() {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_STOP
        }
        context.startService(intent)
        _chronoRunning.value = false
        _chronoPaused.value = false
        _chronoElapsed.value = 0L
        _chronoSelectedTaskId.value = null
        if (_dndEnabled.value) {
            restoreDndState(context)
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

    fun deleteTemplateWithUndo(template: TimerTemplateEntity) {
        viewModelScope.launch {
            try {
                timerRepository.deleteTemplate(template.id)
                pushUndo(UndoSnapshot.TimerTemplateSnapshot(template), template.name)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete template with undo", e)
            }
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

    fun deleteTimerSessionWithUndo(session: TimerSessionEntity) {
        viewModelScope.launch {
            try {
                timerRepository.deleteSession(session.id)
                val label = if (session.label.isNotBlank()) session.label else session.type.lowercase()
                pushUndo(UndoSnapshot.TimerSessionSnapshot(session), "$label · ${session.date}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete timer session with undo", e)
            }
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
            val subtasks = taskRepository.getSubtasks(task.id)
            val incompleteSubtasks = subtasks.filter { it.status != "COMPLETED" }
            if (incompleteSubtasks.isNotEmpty()) {
                _pendingTaskCompletion.value = PendingTaskCompletion(
                    task = task, subtasks = subtasks
                )
                return@launch
            }
            val updated = task.copy(status = "COMPLETED")
            taskRepository.updateTask(updated)
            handleLearnTaskToggle(original = task, updated = updated)
            updated.linkedTodoId?.let { todoId ->
                val linkedTodo = todoRepository.getTodoById(todoId)
                if (linkedTodo != null && linkedTodo.status != "DONE") {
                    todoRepository.updateTodo(linkedTodo.copy(status = "DONE"))
                }
            }
        }
    }

    private fun firePomodoroCompletionNotification(context: Context, state: PomodoroCompletionState) {
        if (!hasNotificationPermission(context)) return
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "pomodoro_session_channel_fs"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.deleteNotificationChannel(channelId)
                val channel = NotificationChannel(
                    channelId,
                    "Pomodoro Alarms",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Full-screen Pomodoro session completion alerts"
                    enableVibration(true)
                    setBypassDnd(true)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            }

            val activityIntent = Intent(context, com.example.ui.screens.PomodoroFinishActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("phase", state.phase)
                putExtra("sessionNumber", state.sessionNumber)
                putExtra("totalSessions", state.totalSessions)
                putExtra("taskTitle", state.taskTitle)
                putExtra("taskId", state.taskId)
                putExtra("durationSeconds", state.durationSeconds)
                putExtra("nextActionLabel", state.nextActionLabel)
                putExtra("nextActionMinutes", state.nextActionMinutes)
                putExtra("canProceed", state.canProceed)
                putExtra("isFinal", state.isFinal)
                putExtra("breakDuration", state.breakDuration ?: -1)
                putExtra("ringtoneUri", _pomodoroRingtoneUri.value)
                putExtra("ringtoneEnabled", _pomodoroRingtoneEnabled.value)
                putExtra("vibrateEnabled", _pomodoroVibrateEnabled.value)
            }
            val pendingIntent = PendingIntent.getActivity(context, 4003, activityIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val title = if (state.phase == "FOCUS") "Focus Session Completed!" else "Break Over!"
            val message = "Session ${state.sessionNumber}${if (state.totalSessions != null) "/${state.totalSessions}" else ""} for '${state.taskTitle}' is done."

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(pendingIntent, true)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(4003, notification)
        } catch (e: Exception) {
            Log.e(TAG, "firePomodoroCompletionNotification failed", e)
        }
    }

    private fun cancelPomodoroNotification(context: Context) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(4003)
        } catch (e: Exception) {
            Log.e(TAG, "cancelPomodoroNotification failed", e)
        }
    }

    // --- Usage Stats and Screen Time ---
    fun updateAppUsage(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _screenTimeError.value = null
            _screenTimeLoading.value = true

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                _appUsageItems.value = emptyList()
                _totalScreenTimeMinutes.value = 0
                _screenTimeError.value = "Screen time tracking requires Android 5.0+"
                _screenTimeLoading.value = false
                return@launch
            }

            if (!hasUsageStatsPermission(context)) {
                _appUsageItems.value = emptyList()
                _totalScreenTimeMinutes.value = 0
                _screenTimeLoading.value = false
                return@launch
            }

            try {
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val midnight = calendar.timeInMillis
                val now = System.currentTimeMillis()

                val stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    midnight,
                    now
                )

                val packageManager = context.packageManager
                val allItems = stats
                    .filter { it.totalTimeInForeground > 0 }
                    .mapNotNull { stat ->
                        val mins = stat.totalTimeInForeground / (1000 * 60)
                        if (mins <= 0) return@mapNotNull null
                        val label = appLabelCache.getOrPut(stat.packageName) {
                            try {
                                val appInfo = packageManager.getApplicationInfo(stat.packageName, 0)
                                packageManager.getApplicationLabel(appInfo).toString()
                            } catch (_: Exception) {
                                stat.packageName
                            }
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
                _screenTimeLastUpdated.value = System.currentTimeMillis()
            } catch (e: Exception) {
                _appUsageItems.value = emptyList()
                _totalScreenTimeMinutes.value = 0
                _screenTimeError.value = "Unable to load screen time"
            } finally {
                _screenTimeLoading.value = false
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
               checkNotificationPolicyPermission(context) &&
               hasFullScreenIntentPermission(context)
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

    // --- Full-Screen Intent Permission ---
    fun hasFullScreenIntentPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= 34) {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.USE_FULL_SCREEN_INTENT
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun requestFullScreenIntentSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= 34) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    private fun getCurrentInterruptionFilter(context: Context): Int {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            nm.currentInterruptionFilter
        } else {
            NotificationManager.INTERRUPTION_FILTER_ALL
        }
    }

    private fun restoreDndState(context: Context) {
        val prefsFilter = prefs.getInt(PREFS_KEY_ORIGINAL_DND_FILTER, -1)
        val filter = if (prefsFilter != -1) prefsFilter else originalInterruptionFilter
        prefs.edit().remove(PREFS_KEY_ORIGINAL_DND_FILTER).apply()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && nm.isNotificationPolicyAccessGranted) {
            try {
                if (nm.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE) {
                    nm.setInterruptionFilter(filter)
                }
            } catch (_: SecurityException) { }
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
            try {
                ideaRepository.ungroupIdeasByGroupId(group.id)
                ideaRepository.deleteGroup(group)
            } catch (e: Exception) { Log.e(TAG, "Failed to delete group", e) }
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

    fun reorderLearnItem(item: LearnItemEntity, activeItems: List<LearnItemEntity>, deltaIndex: Int) {
        viewModelScope.launch {
            val currentIndex = activeItems.indexOf(item)
            if (currentIndex == -1) return@launch
            val newIndex = (currentIndex + deltaIndex).coerceIn(0, activeItems.size - 1)
            if (deltaIndex != 0) {
                val mutableItems = activeItems.toMutableList()
                mutableItems.removeAt(currentIndex)
                mutableItems.add(newIndex, item)
                val updatedItems = mutableItems.mapIndexed { index, t ->
                    t.copy(sortOrder = index)
                }
                learnRepository.updateLearnItemSortOrders(updatedItems)
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

    fun addStage(ideaId: Long, title: String, importance: String = "OPTIONAL") {
        if (title.isBlank()) return
        viewModelScope.launch {
            try {
                val stages = ideaRepository.getStagesForIdeaSync(ideaId)
                ideaRepository.insertStage(IdeaStageEntity(ideaId = ideaId, title = title.trim(), orderIndex = stages.size, importance = importance))
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
                        linkedIdeaId = idea.id,
                        priority = dailyTasks.value.size + 1,
                        priorityLevel = idea.priority,
                        createdAt = idea.createdAt
                    )
                )
                val stages = ideaRepository.getStagesForIdeaSync(idea.id)
                val subtaskIds = mutableListOf<Long>()
                stages.filter { it.title.isNotBlank() }.forEach { stage ->
                    val subtaskId = taskRepository.insertTask(
                        TaskEntity(
                            title = stage.title,
                            date = date,
                            type = type,
                            parentTaskId = parentId,
                            subtaskImportance = stage.importance,
                            label = "IDEA",
                            linkedIdeaId = idea.id,
                            priority = stage.orderIndex
                        )
                    )
                    subtaskIds.add(subtaskId)
                }
                deleteIdea(idea)
                pushUndo(
                    UndoSnapshot.IdeaToTaskSnapshot(idea, stages, parentId, subtaskIds),
                    idea.title
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add idea to planner", e)
            }
        }
    }

    fun addStageToPlanner(stage: IdeaStageEntity, date: String, type: String) {
        viewModelScope.launch {
            try {
                val taskId = taskRepository.insertTask(
                    TaskEntity(
                        title = stage.title,
                        date = date,
                        type = type,
                        label = "IDEA",
                        priority = dailyTasks.value.size + 1,
                        priorityLevel = "Medium"
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add stage to planner", e)
            }
        }
    }

    // === To-Do CRUD ===
    fun addTodo(title: String, description: String = "", priority: String = "Medium", subtasks: List<Pair<String, String>> = emptyList()) {
        if (title.isBlank()) return
        viewModelScope.launch {
            try {
                val allTodos = todoRepository.getAllTodosSync()
                val nextOrder = (allTodos.maxOfOrNull { it.sortOrder } ?: -1) + 1
                val todoId = todoRepository.insertTodo(TodoEntity(title = title.trim(), description = description.trim(), priority = priority, sortOrder = nextOrder))
                val filtered = subtasks.filter { it.first.isNotBlank() }
                filtered.forEachIndexed { index, (subTitle, importance) ->
                    todoRepository.insertTodo(
                        TodoEntity(
                            title = subTitle.trim(),
                            description = "",
                            priority = priority,
                            parentTodoId = todoId,
                            status = "PENDING",
                            subtaskImportance = importance,
                            sortOrder = nextOrder + 1 + index
                        )
                    )
                }
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

    fun updateTodoWithSubtodos(todo: TodoEntity, subtasks: List<Pair<String, String>>) {
        viewModelScope.launch {
            try {
                val allTodos = todoRepository.getAllTodosSync()
                todoRepository.updateTodo(todo)
                val existingSubTodos = todoRepository.getSubTodosSync(todo.id)
                existingSubTodos.forEach { todoRepository.deleteTodo(it) }
                val allTodoEntities = todoRepository.getAllTodosSync()
                val baseOrder = (allTodoEntities.maxOfOrNull { it.sortOrder } ?: -1) + 1
                val filtered = subtasks.filter { it.first.isNotBlank() }
                filtered.forEachIndexed { index, (subTitle, importance) ->
                    todoRepository.insertTodo(
                        TodoEntity(
                            title = subTitle.trim(),
                            description = "",
                            priority = todo.priority,
                            parentTodoId = todo.id,
                            status = "PENDING",
                            subtaskImportance = importance,
                            sortOrder = baseOrder + index
                        )
                    )
                }
                // Sync to linked task's subtasks
                todo.linkedTaskId?.let { taskId ->
                    taskRepository.getTaskById(taskId)?.let { linkedTask ->
                        val existingSubtasks = taskRepository.getSubtasks(taskId)
                        existingSubtasks.forEach { taskRepository.deleteTask(it) }
                        subtasks.filter { it.first.isNotBlank() }.forEach { (subTitle, importance) ->
                            taskRepository.insertTask(
                                TaskEntity(
                                    title = subTitle.trim(),
                                    description = "",
                                    date = linkedTask.date,
                                    type = "TASK",
                                    durationMinutes = 0,
                                    priority = 0,
                                    label = linkedTask.label,
                                    labelColor = linkedTask.labelColor,
                                    parentTaskId = taskId,
                                    subtaskImportance = importance
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update todo with subtodos", e)
            }
        }
    }

    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch {
            try {
                todoRepository.deleteTodoAndSubTodos(todo)
                if (todo.linkedTaskId != null) {
                    taskRepository.getTaskById(todo.linkedTaskId)?.let { linkedTask ->
                        taskRepository.deleteTaskAndSubtasks(linkedTask)
                    }
                }
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
                val subTodos = todoRepository.getSubTodosSync(todo.id)
                deleteTodo(todo)
                pushUndo(
                    UndoSnapshot.TodoSnapshot(todo, linkedTask, linkedSubtasks, subTodos),
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
                val subTodos = todoRepository.getSubTodosSync(todo.id)
                unlinkTodoFromTask(todo)
                todoRepository.deleteTodo(todo)
                pushUndo(
                    UndoSnapshot.TodoSnapshot(todo, linkedTask, linkedSubtasks, subTodos),
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

                if (newStatus == "DONE") {
                    val subTodos = allTodos.value.filter { it.parentTodoId == todo.id }
                    val incompleteSubTodos = subTodos.filter { it.status != "DONE" }
                    if (incompleteSubTodos.isNotEmpty()) {
                        _pendingSubTodoCompletion.value = PendingSubTodoCompletion(
                            todo = todo, subTodos = subTodos
                        )
                        return@launch
                    }
                }

                if (todo.linkedTaskId != null && newStatus == "DONE") {
                    val linkedTask = taskRepository.getTaskById(todo.linkedTaskId)
                    if (linkedTask != null && linkedTask.status != "COMPLETED") {
                        val subtasks = taskRepository.getSubtasks(linkedTask.id)
                        val incompleteSubtasks = subtasks.filter { it.status != "COMPLETED" }
                        if (incompleteSubtasks.isNotEmpty()) {
                            _pendingTaskCompletion.value = PendingTaskCompletion(
                                task = linkedTask, subtasks = subtasks, todoId = todo.id
                            )
                            return@launch
                        }
                        taskRepository.updateTask(linkedTask.copy(status = "COMPLETED"))
                    }
                }

                todoRepository.updateTodo(todo.copy(status = newStatus))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle todo completion", e)
            }
        }
    }

    fun linkTodoToTask(todo: TodoEntity, targetDate: String) {
        if (todo.linkedTaskId != null) {
            Log.w(TAG, "linkTodoToTask: todo ${todo.id} already linked to task ${todo.linkedTaskId}")
            return
        }
        viewModelScope.launch {
            try {
                val subTodos = allTodos.value.filter { it.parentTodoId == todo.id }

                val taskId = taskRepository.insertTask(
                    TaskEntity(
                        title = todo.title,
                        description = todo.description,
                        date = targetDate,
                        type = "TASK",
                        label = "TODO",
                        linkedTodoId = todo.id,
                        priorityLevel = todo.priority,
                        priority = dailyTasks.value.size + 1
                    )
                )
                subTodos.forEach { subTodo ->
                    taskRepository.insertTask(
                        TaskEntity(
                            title = subTodo.title,
                            description = subTodo.description,
                            date = targetDate,
                            type = "TASK",
                            label = "TODO",
                            parentTaskId = taskId,
                            subtaskImportance = subTodo.subtaskImportance,
                            priorityLevel = subTodo.priority,
                            priority = 0,
                            createdAt = subTodo.createdAt
                        )
                    )
                }
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
                    taskRepository.getTaskById(todo.linkedTaskId)?.let { linkedTask ->
                        taskRepository.deleteTaskAndSubtasks(linkedTask)
                    }
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
                val linkedTodoId = task.linkedTodoId

                if (linkedTodoId == null) {
                    val todoId = todoRepository.insertTodo(
                        TodoEntity(
                            title = task.title,
                            description = task.description,
                            priority = task.priorityLevel,
                            status = if (task.status == "COMPLETED") "DONE" else "PENDING",
                            createdAt = task.createdAt
                        )
                    )
                    subtasks.forEach { subtask ->
                        todoRepository.insertTodo(
                            TodoEntity(
                                title = subtask.title,
                                description = "",
                                priority = subtask.priorityLevel,
                                status = if (subtask.status == "COMPLETED") "DONE" else "PENDING",
                                parentTodoId = todoId,
                                subtaskImportance = subtask.subtaskImportance,
                                createdAt = subtask.createdAt
                            )
                        )
                    }
                } else {
                    todoRepository.getTodoById(linkedTodoId)?.let { linkedTodo ->
                        todoRepository.updateTodo(linkedTodo.copy(linkedTaskId = null))
                        subtasks.forEach { subtask ->
                            todoRepository.insertTodo(
                                TodoEntity(
                                    title = subtask.title,
                                    description = "",
                                    priority = linkedTodo.priority,
                                    status = if (subtask.status == "COMPLETED") "DONE" else "PENDING",
                                    parentTodoId = linkedTodo.id,
                                    subtaskImportance = subtask.subtaskImportance,
                                    createdAt = subtask.createdAt
                                )
                            )
                        }
                    }
                }

                if (task.type == "EVENT") {
                    com.example.core.manager.ReminderManager.cancelReminders(context, task)
                }
                taskRepository.deleteTaskAndSubtasks(task)

                pushUndo(
                    UndoSnapshot.TaskSnapshot(task, subtasks, linkedTodoId),
                    task.title
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to move task to todo", e)
            }
        }
    }

    fun turnNoteIntoIdea(task: TaskEntity, subtasks: List<TaskEntity>) {
        viewModelScope.launch {
            try {
                val linkedTodoId = task.linkedTodoId

                val ideaId = ideaRepository.insertIdea(
                    IdeaEntity(title = task.title, description = task.description)
                )
                subtasks.forEachIndexed { index, subtask ->
                    ideaRepository.insertStage(
                        IdeaStageEntity(
                            ideaId = ideaId,
                            title = subtask.title,
                            isCompleted = false,
                            orderIndex = index,
                            importance = subtask.subtaskImportance
                        )
                    )
                }
                task.linkedTodoId?.let { todoId ->
                    todoRepository.getTodoById(todoId)?.let { linkedTodo ->
                        todoRepository.updateTodo(linkedTodo.copy(linkedTaskId = null))
                    }
                }
                taskRepository.deleteTaskAndSubtasks(task)

                pushUndo(
                    UndoSnapshot.TaskSnapshot(task, subtasks, linkedTodoId),
                    task.title
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to turn note into idea", e)
            }
        }
    }

    fun moveTodoToTask(todo: TodoEntity, targetDate: String, subTodos: List<TodoEntity> = emptyList()) {
        if (todo.linkedTaskId != null) {
            Log.w(TAG, "moveTodoToTask: todo ${todo.id} already linked to task ${todo.linkedTaskId}")
            return
        }
        viewModelScope.launch {
            try {
                val taskId = taskRepository.insertTask(
                    TaskEntity(
                        title = todo.title,
                        description = todo.description,
                        date = targetDate,
                        type = "TASK",
                        label = "TODO",
                        priorityLevel = todo.priority,
                        priority = dailyTasks.value.size + 1,
                        createdAt = todo.createdAt
                    )
                )
                subTodos.forEach { subTodo ->
                    taskRepository.insertTask(
                        TaskEntity(
                            title = subTodo.title,
                            description = "",
                            date = targetDate,
                            type = "TASK",
                            label = "TODO",
                            parentTaskId = taskId,
                            priorityLevel = subTodo.priority,
                            subtaskImportance = subTodo.subtaskImportance,
                            priority = 0,
                            createdAt = subTodo.createdAt
                        )
                    )
                }
                todoRepository.deleteTodoAndSubTodos(todo)
                pushUndo(
                    UndoSnapshot.TodoSnapshot(todo, null, emptyList(), subTodos),
                    todo.title
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to move todo to task", e)
            }
        }
    }

    // === Learn CRUD ===
    fun addLearnItem(
        title: String,
        type: String,
        totalSections: Int,
        priorityLevel: String = "Medium",
        groupId: Long? = null
    ) {
        viewModelScope.launch {
            try {
                val itemId = learnRepository.insertItem(
                    LearnItemEntity(
                        title = title.trim(),
                        type = type,
                        totalSections = totalSections,
                        priorityLevel = priorityLevel,
                        groupId = groupId
                    )
                )
                for (i in 0 until totalSections) {
                    val chapNum = when (type) {
                        "BOOK" -> "Chapter"
                        "COURSE" -> "Lesson"
                        else -> "Section"
                    }
                    learnRepository.insertSection(
                        LearnSectionEntity(
                            learnItemId = itemId,
                            orderIndex = i,
                            title = "$chapNum ${i + 1}"
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add learn item", e)
            }
        }
    }

    fun updateLearnItem(
        item: LearnItemEntity,
        newTitle: String,
        newType: String,
        newTotalSections: Int,
        newPriorityLevel: String = "Medium",
        newGroupId: Long? = item.groupId
    ) {
        viewModelScope.launch {
            try {
                val existingSections = learnRepository.getSectionsForItemSync(item.id)
                val anyStarted = existingSections.any { it.status != "NOT_STARTED" }
                learnRepository.updateItem(
                    item.copy(
                        title = newTitle.trim(),
                        type = newType,
                        totalSections = newTotalSections,
                        priorityLevel = newPriorityLevel,
                        groupId = newGroupId
                    )
                )
                if (!anyStarted) {
                    for (section in existingSections) {
                        learnRepository.deleteSection(section)
                    }
                    for (i in 0 until newTotalSections) {
                        val chapNum = when (newType) {
                            "BOOK" -> "Chapter"
                            "COURSE" -> "Lesson"
                            else -> "Section"
                        }
                        learnRepository.insertSection(
                            LearnSectionEntity(
                                learnItemId = item.id,
                                orderIndex = i,
                                title = "$chapNum ${i + 1}"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update learn item", e)
            }
        }
    }

    fun deleteLearnItem(item: LearnItemEntity) {
        viewModelScope.launch {
            try {
                val sections = learnRepository.getSectionsForItemSync(item.id)
                for (section in sections) {
                    section.studyTaskId?.let { taskRepository.deleteTaskById(it) }
                    section.reviewTaskId?.let { taskRepository.deleteTaskById(it) }
                }
                learnRepository.deleteItem(item)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete learn item", e)
            }
        }
    }

    fun deleteLearnItemWithUndo(item: LearnItemEntity) {
        viewModelScope.launch {
            try {
                val sections = learnRepository.getSectionsForItemSync(item.id)
                val studyTaskIds = sections.mapNotNull { it.studyTaskId }
                val reviewTaskIds = sections.mapNotNull { it.reviewTaskId }
                for (section in sections) {
                    section.studyTaskId?.let { taskRepository.deleteTaskById(it) }
                    section.reviewTaskId?.let { taskRepository.deleteTaskById(it) }
                }
                learnRepository.deleteItem(item)
                pushUndo(
                    UndoSnapshot.LearnItemSnapshot(item, sections, studyTaskIds, reviewTaskIds),
                    item.title
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete learn item with undo", e)
            }
        }
    }

    fun applyLearningAlgorithm(
        itemId: Long,
        startDate: String,
        sectionsPerDay: Int,
        deadline: String? = null,
        scheduleMode: String = "CONTINUOUS",
        scheduleDaysOfWeek: String = ""
    ) {
        viewModelScope.launch {
            try {
                val item = learnRepository.getItemById(itemId) ?: return@launch
                val sections = learnRepository.getSectionsForItemSync(itemId)
                if (sections.isEmpty()) return@launch

                val effectivePerDay = if (deadline != null && deadline.isNotBlank()) {
                    if (scheduleMode == "WEEKLY" && scheduleDaysOfWeek.isNotBlank()) {
                        val allowedDays = countAllowedDaysBetween(startDate, deadline, scheduleDaysOfWeek)
                        ceil(sections.size.toFloat() / allowedDays.toFloat()).toInt()
                    } else {
                        val daysBetween = daysBetweenDates(startDate, deadline).coerceAtLeast(1)
                        ceil(sections.size.toFloat() / daysBetween.toFloat()).toInt()
                    }
                } else {
                    sectionsPerDay.coerceAtLeast(1)
                }

                learnRepository.updateItem(item.copy(
                    status = "ACTIVE",
                    sectionsPerDay = effectivePerDay,
                    scheduleMode = scheduleMode,
                    scheduleDaysOfWeek = scheduleDaysOfWeek
                ))

                val perDay = effectivePerDay

                if (scheduleMode == "WEEKLY" && scheduleDaysOfWeek.isNotBlank()) {
                    val firstDate = nextAllowedDate(startDate, scheduleDaysOfWeek)
                    val maxDate = if (deadline != null && deadline.isNotBlank()) deadline else null
                    val totalBatches = ceil(sections.size.toFloat() / perDay.toFloat()).toInt()
                    val allowedDates = generateSequence(firstDate) { addDays(it, 1) }
                        .filter { nextAllowedDate(it, scheduleDaysOfWeek) == it }
                        .takeWhile { maxDate == null || daysBetweenDates(startDate, it) <= daysBetweenDates(startDate, maxDate) }
                        .take(totalBatches)
                        .toList()

                    for (i in sections.indices) {
                        val section = sections[i]
                        val batchIndex = i / perDay
                        val taskDate = allowedDates[batchIndex]
                        val shortTitle = if (item.title.length > 30) item.title.take(27) + "..." else item.title
                        val task = TaskEntity(
                            title = "📖 $shortTitle — ${section.title}",
                            date = taskDate,
                            type = "TASK",
                            status = "PENDING",
                            label = "Study",
                            labelColor = 0xFFFFB300,
                            linkedLearnSectionId = section.id,
                            priorityLevel = item.priorityLevel
                        )
                        val taskId = taskRepository.insertTask(task)
                        learnRepository.updateSection(section.copy(studyTaskId = taskId))
                    }
                } else {
                    for (i in sections.indices) {
                        val section = sections[i]
                        val dayOffset = i / perDay
                        val taskDate = addDays(startDate, dayOffset)
                        val shortTitle = if (item.title.length > 30) item.title.take(27) + "..." else item.title
                        val task = TaskEntity(
                            title = "📖 $shortTitle — ${section.title}",
                            date = taskDate,
                            type = "TASK",
                            status = "PENDING",
                            label = "Study",
                            labelColor = 0xFFFFB300,
                            linkedLearnSectionId = section.id,
                            priorityLevel = item.priorityLevel
                        )
                        val taskId = taskRepository.insertTask(task)
                        learnRepository.updateSection(section.copy(studyTaskId = taskId))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to apply learning algorithm", e)
            }
        }
    }

    fun pauseLearnItem(item: LearnItemEntity) {
        viewModelScope.launch {
            try {
                val sections = learnRepository.getSectionsForItemSync(item.id)
                for (section in sections) {
                    var updated = section
                    if (section.studyTaskId != null) {
                        val task = taskRepository.getTaskById(section.studyTaskId)
                        if (task != null && task.status != "COMPLETED") {
                            taskRepository.deleteTaskById(section.studyTaskId)
                        }
                        updated = updated.copy(studyTaskId = null)
                    }
                    if (section.reviewTaskId != null) {
                        val task = taskRepository.getTaskById(section.reviewTaskId)
                        if (task != null && task.status != "COMPLETED") {
                            taskRepository.deleteTaskById(section.reviewTaskId)
                        }
                        updated = updated.copy(reviewTaskId = null)
                    }
                    if (updated != section) {
                        learnRepository.updateSection(updated)
                    }
                }
                if (_pendingReviewLearnItem.value?.id == item.id) {
                    _pendingReviewTask.value = null
                    _pendingReviewSection.value = null
                    _pendingReviewLearnItem.value = null
                }
                learnRepository.updateItem(item.copy(status = "PAUSED", pausedAt = System.currentTimeMillis()))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to pause learn item", e)
            }
        }
    }

    fun resumeLearnItem(item: LearnItemEntity) {
        viewModelScope.launch {
            try {
                val sections = learnRepository.getSectionsForItemSync(item.id)
                if (sections.isEmpty()) return@launch

                val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val todayStr = fmt.format(java.util.Calendar.getInstance().time)

                val cal = java.util.Calendar.getInstance().apply { timeInMillis = item.pausedAt }
                val pausedAtDateStr = fmt.format(cal.time)
                val gapDays = daysBetweenDates(pausedAtDateStr, todayStr)

                val perDay = item.sectionsPerDay.coerceAtLeast(1)
                val shortTitle = if (item.title.length > 30) item.title.take(27) + "..." else item.title

                // Pre-compute allowed dates for NOT_STARTED sections (handles WEEKLY mode)
                val notStartedCount = sections.count { it.status == "NOT_STARTED" }
                val totalBatches = ceil(notStartedCount.toFloat() / perDay.toFloat()).toInt()
                val notStartedAllowedDates = if (item.scheduleMode == "WEEKLY" && item.scheduleDaysOfWeek.isNotBlank()) {
                    val firstAllowed = nextAllowedDate(todayStr, item.scheduleDaysOfWeek)
                    generateSequence(firstAllowed) { addDays(it, 1) }
                        .filter { nextAllowedDate(it, item.scheduleDaysOfWeek) == it }
                        .take(totalBatches)
                        .toList()
                } else emptyList()

                var notStartedBatchIndex = 0
                for (section in sections) {
                    when (section.status) {
                        "NOT_STARTED" -> {
                            val taskDate = if (item.scheduleMode == "WEEKLY" && item.scheduleDaysOfWeek.isNotBlank()) {
                                notStartedAllowedDates[notStartedBatchIndex]
                            } else {
                                addDays(todayStr, notStartedBatchIndex)
                            }
                            val taskId = taskRepository.insertTask(TaskEntity(
                                title = "📖 $shortTitle — ${section.title}",
                                date = taskDate,
                                type = "TASK",
                                status = "PENDING",
                                label = "Study",
                                labelColor = 0xFFFFB300,
                                linkedLearnSectionId = section.id,
                                priorityLevel = item.priorityLevel
                            ))
                            learnRepository.updateSection(section.copy(studyTaskId = taskId))
                            notStartedBatchIndex++
                        }
                        "STUDIED" -> {
                            if (gapDays > LEITNER_INTERVALS[0]) {
                                val nextDate = addDays(todayStr, LEITNER_INTERVALS[0])
                                val adjustedDate = if (item.scheduleMode == "WEEKLY" && item.scheduleDaysOfWeek.isNotBlank())
                                    nextAllowedDate(nextDate, item.scheduleDaysOfWeek) else nextDate
                                val taskId = taskRepository.insertTask(TaskEntity(
                                    title = "🔄 $shortTitle — ${section.title}",
                                    date = adjustedDate,
                                    type = "TASK",
                                    status = "PENDING",
                                    label = "Review",
                                    labelColor = 0xFFFFB300,
                                    linkedLearnSectionId = section.id,
                                    priorityLevel = item.priorityLevel
                                ))
                                learnRepository.updateSection(section.copy(
                                    status = "IN_REVIEW",
                                    reviewStage = 0,
                                    lastReviewDate = null,
                                    nextReviewDate = adjustedDate,
                                    reviewTaskId = taskId
                                ))
                            } else {
                                val nextDate = addDays(todayStr, LEITNER_INTERVALS[0])
                                val adjustedDate = if (item.scheduleMode == "WEEKLY" && item.scheduleDaysOfWeek.isNotBlank())
                                    nextAllowedDate(nextDate, item.scheduleDaysOfWeek) else nextDate
                                val taskId = taskRepository.insertTask(TaskEntity(
                                    title = "🔄 $shortTitle — ${section.title}",
                                    date = adjustedDate,
                                    type = "TASK",
                                    status = "PENDING",
                                    label = "Review",
                                    labelColor = 0xFFFFB300,
                                    linkedLearnSectionId = section.id,
                                    priorityLevel = item.priorityLevel
                                ))
                                learnRepository.updateSection(section.copy(
                                    status = "IN_REVIEW",
                                    reviewStage = 0,
                                    lastReviewDate = null,
                                    nextReviewDate = adjustedDate,
                                    reviewTaskId = taskId
                                ))
                            }
                        }
                        "IN_REVIEW" -> {
                            val stage = section.reviewStage.coerceIn(0, 5)
                            if (gapDays > LEITNER_INTERVALS[stage]) {
                                val todayOrAllowed = if (item.scheduleMode == "WEEKLY" && item.scheduleDaysOfWeek.isNotBlank())
                                    nextAllowedDate(todayStr, item.scheduleDaysOfWeek) else todayStr
                                val taskId = taskRepository.insertTask(TaskEntity(
                                    title = "🔄 $shortTitle — ${section.title}",
                                    date = todayOrAllowed,
                                    type = "TASK",
                                    status = "PENDING",
                                    label = "Review",
                                    labelColor = 0xFFFFB300,
                                    linkedLearnSectionId = section.id,
                                    priorityLevel = item.priorityLevel
                                ))
                                val nextDate = addDays(todayOrAllowed, LEITNER_INTERVALS[stage])
                                val adjustedDate = if (item.scheduleMode == "WEEKLY" && item.scheduleDaysOfWeek.isNotBlank())
                                    nextAllowedDate(nextDate, item.scheduleDaysOfWeek) else nextDate
                                learnRepository.updateSection(section.copy(
                                    lastReviewDate = null,
                                    nextReviewDate = adjustedDate,
                                    reviewTaskId = taskId
                                ))
                            } else if (section.nextReviewDate != null) {
                                val taskId = taskRepository.insertTask(TaskEntity(
                                    title = "🔄 $shortTitle — ${section.title}",
                                    date = section.nextReviewDate,
                                    type = "TASK",
                                    status = "PENDING",
                                    label = "Review",
                                    labelColor = 0xFFFFB300,
                                    linkedLearnSectionId = section.id,
                                    priorityLevel = item.priorityLevel
                                ))
                                learnRepository.updateSection(section.copy(reviewTaskId = taskId))
                            }
                        }
                    }
                }
                learnRepository.updateItem(item.copy(status = "ACTIVE", pausedAt = 0))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to resume learn item", e)
            }
        }
    }

    fun completeReviewWithRating(taskId: Long, sectionId: Long, rating: String) {
        viewModelScope.launch {
            try {
                val section = learnRepository.getSectionById(sectionId) ?: return@launch
                val task = taskRepository.getTaskById(taskId) ?: return@launch
                val completionDate = task.date

                val newStage = when (rating) {
                    "HARD" -> (section.reviewStage - 1).coerceAtLeast(0)
                    "EASY" -> (section.reviewStage + 2).coerceAtMost(5)
                    else -> (section.reviewStage + 1).coerceAtMost(5) // MEDIUM
                }

                if (newStage >= 5) {
                    learnRepository.updateSection(section.copy(
                        status = "MASTERED",
                        reviewStage = 5,
                        lastReviewDate = completionDate,
                        nextReviewDate = null,
                        reviewTaskId = null
                    ))
                } else {
                    val nextDate = addDays(completionDate, LEITNER_INTERVALS[newStage])
                    val learnItem = learnRepository.getItemById(section.learnItemId)
                    val adjustedDate = learnItem?.let { li ->
                        if (li.scheduleMode == "WEEKLY" && li.scheduleDaysOfWeek.isNotBlank())
                            nextAllowedDate(nextDate, li.scheduleDaysOfWeek) else nextDate
                    } ?: nextDate
                    val newReviewTaskId = taskRepository.insertTask(TaskEntity(
                        title = task.title.replace("📖", "🔄"),
                        date = adjustedDate,
                        type = "TASK",
                        status = "PENDING",
                        label = "Review",
                        labelColor = 0xFFFFB300,
                        linkedLearnSectionId = section.id,
                        priorityLevel = task.priorityLevel
                    ))
                    learnRepository.updateSection(section.copy(
                        status = "IN_REVIEW",
                        reviewStage = newStage,
                        lastReviewDate = completionDate,
                        nextReviewDate = adjustedDate,
                        reviewTaskId = newReviewTaskId
                    ))
                }

                _pendingReviewTask.value = null
                _pendingReviewSection.value = null
                _pendingReviewLearnItem.value = null
                checkLearnItemCompletion(section.learnItemId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to complete review with rating", e)
            }
        }
    }

    fun dismissReviewRating() {
        _pendingReviewTask.value = null
        _pendingReviewSection.value = null
        _pendingReviewLearnItem.value = null
    }

    private suspend fun handleLearnTaskToggle(original: TaskEntity, updated: TaskEntity) {
        val sectionId = updated.linkedLearnSectionId ?: return
        val section = learnRepository.getSectionById(sectionId) ?: return

        if (original.status == "COMPLETED" && updated.status != "COMPLETED") {
            when (section.status) {
                "IN_REVIEW" -> {
                    section.reviewTaskId?.let { taskRepository.deleteTaskById(it) }
                    learnRepository.updateSection(section.copy(
                        status = "NOT_STARTED",
                        reviewStage = -1,
                        lastReviewDate = null,
                        nextReviewDate = null,
                        reviewTaskId = null
                    ))
                }
                "MASTERED" -> {
                    learnRepository.updateSection(section.copy(
                        status = "NOT_STARTED",
                        reviewStage = -1,
                        lastReviewDate = null,
                        nextReviewDate = null
                    ))
                }
            }
        } else if (original.status != "COMPLETED" && updated.status == "COMPLETED") {
            val completionDate = updated.date
            val learnItem = learnRepository.getItemById(section.learnItemId)
            when (updated.label) {
                "Study" -> {
                    val nextDate = addDays(completionDate, LEITNER_INTERVALS[0])
                    val adjustedDate = learnItem?.let { li ->
                        if (li.scheduleMode == "WEEKLY" && li.scheduleDaysOfWeek.isNotBlank())
                            nextAllowedDate(nextDate, li.scheduleDaysOfWeek) else nextDate
                    } ?: nextDate
                    val reviewTaskId = taskRepository.insertTask(TaskEntity(
                        title = updated.title.replace("📖", "🔄"),
                        date = adjustedDate,
                        type = "TASK",
                        status = "PENDING",
                        label = "Review",
                        labelColor = 0xFFFFB300,
                        linkedLearnSectionId = section.id,
                        priorityLevel = updated.priorityLevel
                    ))
                    learnRepository.updateSection(section.copy(
                        status = "IN_REVIEW",
                        reviewStage = 0,
                        lastReviewDate = completionDate,
                        nextReviewDate = adjustedDate,
                        reviewTaskId = reviewTaskId
                    ))
                    checkLearnItemCompletion(section.learnItemId)
                }
                "Review" -> {
                    _pendingReviewTask.value = updated
                    _pendingReviewSection.value = section
                    _pendingReviewLearnItem.value = learnItem
                }
            }
        }
    }

    private suspend fun checkLearnItemCompletion(itemId: Long) {
        val sections = learnRepository.getSectionsForItemSync(itemId)
        if (sections.all { it.status == "MASTERED" }) {
            learnRepository.getItemById(itemId)?.let {
                learnRepository.updateItem(it.copy(status = "COMPLETED"))
            }
        }
    }

    private fun addDays(dateStr: String, days: Int): String {
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        try {
            val cal = java.util.Calendar.getInstance()
            cal.time = fmt.parse(dateStr) ?: java.util.Calendar.getInstance().time
            cal.add(java.util.Calendar.DAY_OF_YEAR, days)
            return fmt.format(cal.time)
        } catch (_: Exception) {
            return dateStr
        }
    }

    private fun daysBetweenDates(from: String, to: String): Int {
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return try {
            val fromCal = java.util.Calendar.getInstance().apply { time = fmt.parse(from) ?: return 1 }
            val toCal = java.util.Calendar.getInstance().apply { time = fmt.parse(to) ?: return 1 }
            ((toCal.timeInMillis - fromCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
        } catch (_: Exception) { 1 }
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

    fun deleteDiaryEntryWithUndo(entry: DiaryEntryEntity) {
        viewModelScope.launch {
            try {
                diaryRepository.deleteEntryByDate(entry.date)
                val title = entry.title.ifBlank { entry.date }
                pushUndo(UndoSnapshot.DiarySnapshot(entry), title)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete diary entry with undo", e)
            }
        }
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

    fun deleteShopItemWithUndo(item: ShopItemEntity) {
        viewModelScope.launch {
            try {
                shopItemRepository.deleteItem(item)
                pushUndo(UndoSnapshot.ShopItemSnapshot(item), item.name)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete shop item with undo", e)
            }
        }
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

    fun deleteMottoWithUndo(motto: MottoEntity) {
        viewModelScope.launch {
            try {
                mottoRepository.deleteMotto(motto)
                if (_todayMotto.value?.id == motto.id) {
                    refreshTodayMotto()
                }
                val shortText = motto.text.take(40)
                pushUndo(UndoSnapshot.MottoSnapshot(motto), shortText)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete motto with undo", e)
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

    fun deleteDayReviewWithUndo(review: DayReviewEntity) {
        viewModelScope.launch {
            try {
                dayReviewRepository.deleteReviewByDate(review.date)
                pushUndo(UndoSnapshot.DayReviewSnapshot(review), review.date)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete day review with undo", e)
            }
        }
    }

    fun updateReviewReminderTime(time: String) {
        val normalized = time.split(":").let { parts ->
            val h = parts.getOrNull(0)?.toIntOrNull() ?: 21
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
            String.format(Locale.getDefault(), "%02d:%02d", h.coerceIn(0, 23), m.coerceIn(0, 59))
        }
        prefs.edit().putString("review_reminder_time", normalized).apply()
        _reviewReminderTime.value = normalized
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

    fun updateSleepReminderTime(time: String) {
        prefs.edit().putString("sleep_reminder_time", time).apply()
        _sleepReminderTime.value = time
        if (_sleepReminderEnabled.value) {
            cancelSleepReminderAlarm(context)
            scheduleSleepReminderAlarm(context)
        }
    }

    fun updateSleepReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sleep_reminder_enabled", enabled).apply()
        _sleepReminderEnabled.value = enabled
        if (enabled) {
            scheduleSleepReminderAlarm(context)
        } else {
            cancelSleepReminderAlarm(context)
        }
    }

    fun updateDiaryReminderTime(time: String) {
        prefs.edit().putString("diary_reminder_time", time).apply()
        _diaryReminderTime.value = time
        if (_diaryReminderEnabled.value) {
            cancelDiaryReminderAlarm(context)
            scheduleDiaryReminderAlarm(context)
        }
    }

    fun updateDiaryReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("diary_reminder_enabled", enabled).apply()
        _diaryReminderEnabled.value = enabled
        if (enabled) {
            scheduleDiaryReminderAlarm(context)
        } else {
            cancelDiaryReminderAlarm(context)
        }
    }

    fun updatePlannerReminderTime(time: String) {
        prefs.edit().putString("planner_reminder_time", time).apply()
        _plannerReminderTime.value = time
        if (_plannerReminderEnabled.value) {
            cancelPlannerReminderAlarm(context)
            schedulePlannerReminderAlarm(context)
        }
    }

    fun updatePlannerReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("planner_reminder_enabled", enabled).apply()
        _plannerReminderEnabled.value = enabled
        if (enabled) {
            schedulePlannerReminderAlarm(context)
        } else {
            cancelPlannerReminderAlarm(context)
        }
    }

    fun updateHabitsReminderTime(time: String) {
        prefs.edit().putString("habits_reminder_time", time).apply()
        _habitsReminderTime.value = time
        if (_habitsReminderEnabled.value) {
            cancelHabitsReminderAlarm(context)
            scheduleHabitsReminderAlarm(context)
        }
    }

    fun updateHabitsReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("habits_reminder_enabled", enabled).apply()
        _habitsReminderEnabled.value = enabled
        if (enabled) {
            scheduleHabitsReminderAlarm(context)
        } else {
            cancelHabitsReminderAlarm(context)
        }
    }

    fun updateTomorrowPlannerReminderTime(time: String) {
        prefs.edit().putString("tomorrow_planner_reminder_time", time).apply()
        _tomorrowPlannerReminderTime.value = time
        if (_tomorrowPlannerReminderEnabled.value) {
            cancelTomorrowPlannerReminderAlarm(context)
            scheduleTomorrowPlannerReminderAlarm(context)
        }
    }

    fun updateTomorrowPlannerReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("tomorrow_planner_reminder_enabled", enabled).apply()
        _tomorrowPlannerReminderEnabled.value = enabled
        if (enabled) {
            scheduleTomorrowPlannerReminderAlarm(context)
        } else {
            cancelTomorrowPlannerReminderAlarm(context)
        }
    }

    fun updateLearnReviewReminderTime(time: String) {
        val normalized = time.split(":").let { parts ->
            val h = parts.getOrNull(0)?.toIntOrNull() ?: 19
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
            String.format(Locale.getDefault(), "%02d:%02d", h.coerceIn(0, 23), m.coerceIn(0, 59))
        }
        prefs.edit().putString("learn_review_reminder_time", normalized).apply()
        _learnReviewReminderTime.value = normalized
        if (_learnReviewReminderEnabled.value) {
            cancelLearnReviewReminderAlarm(context)
            scheduleLearnReviewReminderAlarm(context)
        }
    }

    fun updateLearnReviewReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("learn_review_reminder_enabled", enabled).apply()
        _learnReviewReminderEnabled.value = enabled
        if (enabled) {
            scheduleLearnReviewReminderAlarm(context)
        } else {
            cancelLearnReviewReminderAlarm(context)
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

    private fun computeStartTimestamp(hour: Int?, minute: Int?): Long {
        return if (hour != null && minute != null) {
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, hour)
            cal.set(java.util.Calendar.MINUTE, minute)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            cal.timeInMillis
        } else {
            System.currentTimeMillis()
        }
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
                        for (subTodo in snap.subTodos) {
                            todoRepository.insertTodo(subTodo.copy(id = 0, parentTodoId = newTodoId))
                        }
                    }
                    is UndoSnapshot.IdeaSnapshot -> {
                        val newIdeaId = ideaRepository.insertIdea(snap.idea.copy(id = 0))
                        for (stage in snap.stages) {
                            ideaRepository.insertStage(stage.copy(id = 0, ideaId = newIdeaId))
                        }
                    }
                    is UndoSnapshot.IdeaToTaskSnapshot -> {
                        taskRepository.deleteTaskById(snap.parentTaskId)
                        for (subtaskId in snap.subtaskIds) {
                            taskRepository.deleteTaskById(subtaskId)
                        }
                        val newIdeaId = ideaRepository.insertIdea(snap.idea.copy(id = 0))
                        for (stage in snap.stages) {
                            ideaRepository.insertStage(stage.copy(id = 0, ideaId = newIdeaId))
                        }
                    }
                    is UndoSnapshot.HabitSnapshot -> {
                        val newHabitId = habitRepository.insertHabit(snap.habit.copy(id = 0))
                        for (log in snap.logs) {
                            habitRepository.insertLog(log.copy(id = 0, habitId = newHabitId))
                        }
                        if (snap.habit.reminderEnabled && !snap.habit.habitTime.isNullOrBlank()) {
                            com.example.core.manager.ReminderManager.scheduleHabitReminder(
                                context = context,
                                habit = snap.habit.copy(id = newHabitId),
                                vibrate = true,
                                sound = true
                            )
                        }
                    }
                    is UndoSnapshot.DiarySnapshot -> {
                        diaryRepository.insertEntry(snap.entry.copy(id = 0))
                    }
                    is UndoSnapshot.DayReviewSnapshot -> {
                        dayReviewRepository.insertReview(snap.review.copy(id = 0))
                        prefs.edit().remove("reviewed_today").apply()
                    }
                    is UndoSnapshot.TimerTemplateSnapshot -> {
                        timerRepository.insertTemplate(snap.template.copy(id = 0))
                    }
                    is UndoSnapshot.TimerSessionSnapshot -> {
                        timerRepository.insertSession(snap.session.copy(id = 0))
                    }
                    is UndoSnapshot.ShopItemSnapshot -> {
                        shopItemRepository.insertItem(snap.item.copy(id = 0))
                    }
                    is UndoSnapshot.MottoSnapshot -> {
                        mottoRepository.insertMotto(snap.motto.copy(id = 0))
                        refreshTodayMotto()
                    }
                    is UndoSnapshot.LearnItemSnapshot -> {
                        val newItemId = learnRepository.insertItem(snap.item.copy(id = 0))
                        val oldToNewSectionId = mutableMapOf<Long, Long>()
                        for (section in snap.sections) {
                            val newSectionId = learnRepository.insertSection(
                                section.copy(id = 0, learnItemId = newItemId, studyTaskId = null, reviewTaskId = null)
                            )
                            oldToNewSectionId[section.id] = newSectionId
                        }
                        val oldToNewStudyTaskId = mutableMapOf<Long, Long>()
                        for (oldTaskId in snap.studyTaskIds) {
                            val task = taskRepository.getTaskById(oldTaskId) ?: continue
                            val newLinkedSectionId = task.linkedLearnSectionId?.let { oldToNewSectionId[it] }
                            val newTaskId = taskRepository.insertTask(
                                task.copy(id = 0, linkedLearnSectionId = newLinkedSectionId)
                            )
                            oldToNewStudyTaskId[oldTaskId] = newTaskId
                        }
                        val oldToNewReviewTaskId = mutableMapOf<Long, Long>()
                        for (oldTaskId in snap.reviewTaskIds) {
                            val task = taskRepository.getTaskById(oldTaskId) ?: continue
                            val newLinkedSectionId = task.linkedLearnSectionId?.let { oldToNewSectionId[it] }
                            val newTaskId = taskRepository.insertTask(
                                task.copy(id = 0, linkedLearnSectionId = newLinkedSectionId)
                            )
                            oldToNewReviewTaskId[oldTaskId] = newTaskId
                        }
                        for (section in snap.sections) {
                            val newSectionId = oldToNewSectionId[section.id] ?: continue
                            val newStudyTaskId = section.studyTaskId?.let { oldToNewStudyTaskId[it] }
                            val newReviewTaskId = section.reviewTaskId?.let { oldToNewReviewTaskId[it] }
                            val existing = learnRepository.getSectionById(newSectionId) ?: continue
                            learnRepository.updateSection(existing.copy(
                                studyTaskId = newStudyTaskId,
                                reviewTaskId = newReviewTaskId
                            ))
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
    private val learnRepository: com.example.core.repository.LearnRepository,
    private val context: android.content.Context
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                taskRepository, timerRepository, habitRepository, sleepLogRepository,
                ideaRepository, todoRepository, diaryRepository,
                shopItemRepository, mottoRepository, dayReviewRepository,
                learnRepository, context
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
