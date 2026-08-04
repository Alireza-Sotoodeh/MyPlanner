package com.example.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.core.database.AppDatabase
import com.example.core.database.entity.TimerSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TimerServiceState(
    val mode: TimerMode? = null,
    val secondsLeft: Int = 0,
    val elapsedSeconds: Long = 0L,
    val running: Boolean = false,
    val paused: Boolean = false,
    val phase: String = "FOCUS",
    val sessionNumber: Int = 1,
    val taskTitle: String = "",
    val taskId: Long = -1L,
    val focusMinutes: Int = 25,
    val completed: Boolean = false
)

enum class TimerMode { POMODORO, CHRONOMETER }

class TimerForegroundService : Service() {

    private val notificationManager: NotificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var tickJob: Job? = null
    private var lastNotifUpdate = 0L
    private val commandChannel = Channel<ServiceCommand>(Channel.UNLIMITED)
    private var commandJob: Job? = null

    private sealed class ServiceCommand {
        data class StartPomodoro(
            val taskTitle: String,
            val taskId: Long,
            val focusMinutes: Int,
            val sessionNumber: Int
        ) : ServiceCommand()

        data class StartChronometer(val taskId: Long) : ServiceCommand()
        data class AdjustPomodoro(val seconds: Int) : ServiceCommand()
        data object TogglePause : ServiceCommand()
        data object Stop : ServiceCommand()
        data object Discard : ServiceCommand()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        commandJob = serviceScope.launch { processCommands() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_POMODORO -> {
                val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: ""
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
                val focusMinutes = intent.getIntExtra(EXTRA_FOCUS_MINUTES, 25)
                val sessionNumber = intent.getIntExtra(EXTRA_SESSION_NUMBER, 1)
                commandChannel.trySend(ServiceCommand.StartPomodoro(taskTitle, taskId, focusMinutes, sessionNumber))
            }
            ACTION_START_CHRONOMETER -> {
                val taskId = intent.getLongExtra(EXTRA_CHRONO_TASK_ID, -1L)
                commandChannel.trySend(ServiceCommand.StartChronometer(taskId))
            }
            ACTION_TOGGLE_PAUSE -> commandChannel.trySend(ServiceCommand.TogglePause)
            ACTION_STOP -> commandChannel.trySend(ServiceCommand.Stop)
            ACTION_DISCARD -> commandChannel.trySend(ServiceCommand.Discard)
            ACTION_ADJUST_POMODORO -> {
                val seconds = intent.getIntExtra(EXTRA_ADJUST_SECONDS, 60)
                commandChannel.trySend(ServiceCommand.AdjustPomodoro(seconds))
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        tickJob?.cancel()
        commandJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    private suspend fun processCommands() {
        for (cmd in commandChannel) {
            when (cmd) {
                is ServiceCommand.StartPomodoro -> handleStartPomodoro(cmd)
                is ServiceCommand.StartChronometer -> handleStartChronometer(cmd)
                is ServiceCommand.TogglePause -> handleTogglePause()
                is ServiceCommand.Stop -> handleStop()
                is ServiceCommand.Discard -> handleDiscard()
                is ServiceCommand.AdjustPomodoro -> handleAdjustPomodoro(cmd)
            }
        }
    }

    private fun handleStartPomodoro(cmd: ServiceCommand.StartPomodoro) {
        if (_state.value.running) return

        _state.value = TimerServiceState(
            mode = TimerMode.POMODORO,
            secondsLeft = cmd.focusMinutes * 60,
            running = true,
            paused = false,
            phase = "FOCUS",
            sessionNumber = cmd.sessionNumber,
            taskTitle = cmd.taskTitle,
            taskId = cmd.taskId,
            focusMinutes = cmd.focusMinutes
        )

        startForeground(NOTIF_ID_POMODORO_LIVE, buildNotification())
        lastNotifUpdate = System.currentTimeMillis()
        startTicking()
    }

    private fun handleStartChronometer(cmd: ServiceCommand.StartChronometer) {
        if (_state.value.running) return

        _state.value = TimerServiceState(
            mode = TimerMode.CHRONOMETER,
            elapsedSeconds = 0L,
            running = true,
            paused = false,
            taskId = cmd.taskId
        )

        startForeground(NOTIF_ID_CHRONOMETER_LIVE, buildNotification())
        lastNotifUpdate = System.currentTimeMillis()
        startTicking()
    }

    private fun handleTogglePause() {
        val current = _state.value
        if (!current.running) return
        _state.value = current.copy(paused = !current.paused)
        updateNotification()
    }

    private fun handleAdjustPomodoro(cmd: ServiceCommand.AdjustPomodoro) {
        val current = _state.value
        if (current.mode != TimerMode.POMODORO || !current.running) return
        val newLeft = (current.secondsLeft + cmd.seconds).coerceAtMost(7200)
        _state.value = current.copy(secondsLeft = newLeft)
        updateNotification()
    }

    private fun handleStop() {
        val current = _state.value
        tickJob?.cancel()
        tickJob = null

        when (current.mode) {
            TimerMode.POMODORO -> {
                if (current.secondsLeft < current.focusMinutes * 60) {
                    val secondsElapsed = (current.focusMinutes * 60) - current.secondsLeft
                    val minutesElapsed = (secondsElapsed / 60).coerceAtLeast(1)
                    serviceScope.launch {
                        saveTimerSession(
                            type = "POMODORO",
                            taskId = current.taskId.takeIf { it > 0 },
                            label = current.taskTitle,
                            durationSeconds = minutesElapsed * 60
                        )
                    }
                }
            }
            TimerMode.CHRONOMETER -> {
                if (current.elapsedSeconds > 0) {
                    serviceScope.launch {
                        saveTimerSession(
                            type = "CHRONOMETER",
                            taskId = current.taskId.takeIf { it > 0 },
                            label = "",
                            durationSeconds = current.elapsedSeconds.toInt()
                        )
                    }
                }
            }
            null -> {}
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        _state.value = TimerServiceState()
        stopSelf()
    }

    private fun handleDiscard() {
        tickJob?.cancel()
        tickJob = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        _state.value = TimerServiceState()
        stopSelf()
    }

    private fun startTicking() {
        tickJob?.cancel()
        tickJob = serviceScope.launch {
            while (true) {
                delay(1000)
                val current = _state.value
                if (!current.running || current.mode == null) break
                if (current.paused) continue

                if (current.mode == TimerMode.POMODORO) {
                    val newLeft = current.secondsLeft - 1
                    _state.value = current.copy(secondsLeft = newLeft)
                    if (newLeft <= 0) {
                        handlePomodoroCompletion()
                        break
                    }
                } else {
                    _state.value = current.copy(elapsedSeconds = current.elapsedSeconds + 1)
                }

                val now = System.currentTimeMillis()
                if (now - lastNotifUpdate >= UPDATE_INTERVAL_MS) {
                    updateNotification()
                    lastNotifUpdate = now
                }
            }
        }
    }

    private fun handlePomodoroCompletion() {
        val current = _state.value

        stopForeground(STOP_FOREGROUND_REMOVE)

        fireCompletionNotification(current)

        _state.value = current.copy(running = false, completed = true)

        stopSelf()
    }

    private fun fireCompletionNotification(state: TimerServiceState) {
        try {
            val channelId = "pomodoro_session_channel_fs"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.deleteNotificationChannel(channelId)
                val channel = NotificationChannel(
                    channelId, "Pomodoro Alarms", NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Full-screen Pomodoro session completion alerts"
                    enableVibration(true)
                    setBypassDnd(true)
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            }

            val activityIntent = Intent(this, com.example.ui.screens.PomodoroFinishActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("phase", state.phase)
                putExtra("sessionNumber", state.sessionNumber)
                putExtra("taskTitle", state.taskTitle)
                putExtra("taskId", state.taskId)
                putExtra("durationSeconds", state.focusMinutes * 60)
                putExtra("nextActionLabel", "")
                putExtra("nextActionMinutes", 0)
                putExtra("canProceed", false)
                putExtra("isFinal", false)
                putExtra("breakDuration", -1)

                val prefs = getSharedPreferences("bulletcoach_prefs", Context.MODE_PRIVATE)
                putExtra("ringtoneUri", prefs.getString("pomodoro_ringtone_uri", "") ?: "")
                putExtra("ringtoneEnabled", prefs.getBoolean("pomodoro_ringtone_enabled", true))
                putExtra("vibrateEnabled", prefs.getBoolean("pomodoro_vibrate_enabled", true))
            }
            val pendingIntent = PendingIntent.getActivity(
                this, 4003, activityIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val title = if (state.phase == "FOCUS") "Focus Session Completed!" else "Break Over!"
            val message = "Session ${state.sessionNumber} for '${state.taskTitle}' is done."

            val notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_timer_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(pendingIntent, true)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(4003, notification)
        } catch (e: Exception) {
            Log.e(TAG, "fireCompletionNotification failed", e)
        }
    }

    private suspend fun saveTimerSession(type: String, taskId: Long?, label: String, durationSeconds: Int) {
        withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(this@TimerForegroundService)
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                db.timerSessionDao().insert(
                    TimerSessionEntity(
                        type = type,
                        taskId = taskId,
                        label = label,
                        durationSeconds = durationSeconds,
                        date = date
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "saveTimerSession failed", e)
            }
        }
    }

    private fun buildNotification(): Notification {
        val current = _state.value

        val channelId = when (current.mode) {
            TimerMode.POMODORO -> CHANNEL_POMODORO_LIVE
            TimerMode.CHRONOMETER -> CHANNEL_CHRONOMETER_LIVE
            null -> CHANNEL_POMODORO_LIVE
        }

        val title = when (current.mode) {
            TimerMode.POMODORO -> {
                "POMODORO · ${current.phase}"
            }
            TimerMode.CHRONOMETER -> "CHRONOMETER"
            null -> "Timer"
        }

        val body = when (current.mode) {
            TimerMode.POMODORO -> {
                val mins = current.secondsLeft / 60
                val secs = current.secondsLeft % 60
                String.format(Locale.getDefault(), "%02d:%02d remaining", mins, secs)
            }
            TimerMode.CHRONOMETER -> {
                val h = current.elapsedSeconds / 3600
                val m = (current.elapsedSeconds % 3600) / 60
                val s = current.elapsedSeconds % 60
                String.format(Locale.getDefault(), "%02d:%02d:%02d elapsed", h, m, s)
            }
            null -> ""
        }

        val bigText = buildString {
            when (current.mode) {
                TimerMode.POMODORO -> {
                    append("Session ${current.sessionNumber}")
                    if (current.taskTitle.isNotEmpty()) {
                        append("\nTask: ${current.taskTitle}")
                    }
                    append("\n$body")
                }
                TimerMode.CHRONOMETER -> {
                    append(body)
                    if (current.taskId > 0) {
                        append("\nTask linked")
                    }
                }
                null -> {}
            }
        }

        val pauseIcon = if (current.paused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause
        val pauseLabel = if (current.paused) "Resume" else "Pause"

        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_tab", 2)
            putExtra("open_timer_subtab", if (current.mode == TimerMode.POMODORO) 0 else 1)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, RC_CONTENT, contentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val togglePauseIntent = Intent(this, TimerForegroundService::class.java).apply {
            action = ACTION_TOGGLE_PAUSE
        }
        val togglePausePendingIntent = PendingIntent.getService(
            this, RC_TOGGLE_PAUSE, togglePauseIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, TimerForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, RC_STOP, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val discardIntent = Intent(this, TimerForegroundService::class.java).apply {
            action = ACTION_DISCARD
        }
        val discardPendingIntent = PendingIntent.getService(
            this, RC_DISCARD, discardIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_timer_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPendingIntent)
            .addAction(pauseIcon, pauseLabel, togglePausePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .addAction(android.R.drawable.ic_menu_delete, "Discard", discardPendingIntent)
            .build()
    }

    private fun updateNotification() {
        try {
            notificationManager.notify(currentNotificationId(), buildNotification())
        } catch (e: Exception) {
            Log.e(TAG, "updateNotification failed", e)
        }
    }

    private fun currentNotificationId(): Int {
        return when (_state.value.mode) {
            TimerMode.POMODORO -> NOTIF_ID_POMODORO_LIVE
            TimerMode.CHRONOMETER -> NOTIF_ID_CHRONOMETER_LIVE
            null -> NOTIF_ID_POMODORO_LIVE
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            listOf(
                NotificationChannel(CHANNEL_POMODORO_LIVE, "Pomodoro Timer", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Ongoing pomodoro timer notification"
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                    setShowBadge(false)
                },
                NotificationChannel(CHANNEL_CHRONOMETER_LIVE, "Chronometer", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Ongoing chronometer notification"
                    lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                    setShowBadge(false)
                }
            ).forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    companion object {
        private val _state = MutableStateFlow(TimerServiceState())
        val state: StateFlow<TimerServiceState> = _state.asStateFlow()

        const val ACTION_START_POMODORO = "com.example.action.START_POMODORO"
        const val ACTION_START_CHRONOMETER = "com.example.action.START_CHRONOMETER"
        const val ACTION_TOGGLE_PAUSE = "com.example.action.TOGGLE_PAUSE"
        const val ACTION_STOP = "com.example.action.STOP"
        const val ACTION_DISCARD = "com.example.action.DISCARD"
        const val ACTION_ADJUST_POMODORO = "com.example.action.ADJUST_POMODORO"

        const val EXTRA_ADJUST_SECONDS = "adjustSeconds"

        const val EXTRA_FOCUS_MINUTES = "focusMinutes"
        const val EXTRA_TASK_TITLE = "taskTitle"
        const val EXTRA_TASK_ID = "taskId"
        const val EXTRA_SESSION_NUMBER = "sessionNumber"
        const val EXTRA_CHRONO_TASK_ID = "chronoTaskId"

        const val NOTIF_ID_POMODORO_LIVE = 4006
        const val NOTIF_ID_CHRONOMETER_LIVE = 4007
        const val CHANNEL_POMODORO_LIVE = "timer_live_pomodoro"
        const val CHANNEL_CHRONOMETER_LIVE = "timer_live_chronometer"

        private const val RC_CONTENT = 1000
        private const val RC_TOGGLE_PAUSE = 1001
        private const val RC_STOP = 1002
        private const val RC_DISCARD = 1003
        private const val TAG = "TimerFgService"
        private const val UPDATE_INTERVAL_MS = 10_000L

        fun clearCompletedFlag() {
            _state.value = TimerServiceState()
        }

        fun clearStoppedFlag() {
            _state.value = TimerServiceState()
        }
    }
}
