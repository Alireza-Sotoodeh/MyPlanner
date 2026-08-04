package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.AppDatabase
import com.example.core.database.entity.TimerSessionEntity
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PomodoroFinishActivity : ComponentActivity() {
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: Any? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val phase = intent.getStringExtra("phase") ?: "FOCUS"
        val sessionNumber = intent.getIntExtra("sessionNumber", 1)
        val totalSessions = if (intent.hasExtra("totalSessions")) intent.getIntExtra("totalSessions", -1).let { if (it < 0) null else it } else null
        val taskTitle = intent.getStringExtra("taskTitle") ?: ""
        val durationSeconds = intent.getIntExtra("durationSeconds", 0)
        val nextActionLabel = intent.getStringExtra("nextActionLabel") ?: ""
        val canProceed = intent.getBooleanExtra("canProceed", true)
        val isFinal = intent.getBooleanExtra("isFinal", false)
        val ringtoneUriStr = intent.getStringExtra("ringtoneUri") ?: ""
        val ringtoneEnabled = intent.getBooleanExtra("ringtoneEnabled", true)
        val vibrateEnabled = intent.getBooleanExtra("vibrateEnabled", true)
        val vibratePattern = intent.getStringExtra("vibratePattern") ?: "heartbeat"

        val durationMinutes = durationSeconds / 60
        val isTest = phase == "TEST"

        backupSaveSession(phase, sessionNumber, taskTitle, durationMinutes, intent.getLongExtra("taskId", -1L))

        // Audio focus (always acquire for vibration timing)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val afRequestBuilder = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            afRequestBuilder.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            afRequestBuilder.setOnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS -> stopRingtone()
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> stopRingtone()
                    AudioManager.AUDIOFOCUS_GAIN -> if (ringtone?.isPlaying == false) ringtone?.play()
                }
            }
            audioFocusRequest = afRequestBuilder.build()
            audioManager?.requestAudioFocus(audioFocusRequest as AudioFocusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(null, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN)
        }

        // Ringtone (only if enabled)
        if (ringtoneEnabled) {
            val ringtoneUri = if (ringtoneUriStr.isNotBlank()) android.net.Uri.parse(ringtoneUriStr)
                else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            try {
                ringtone = RingtoneManager.getRingtone(this, ringtoneUri)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ringtone?.isLooping = true
                }
                ringtone?.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Vibration (heartbeat pattern)
        if (vibrateEnabled) {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(com.example.ui.viewmodel.MainViewModel.getVibrationPattern(vibratePattern), 0),
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(com.example.ui.viewmodel.MainViewModel.getVibrationPattern(vibratePattern), 0)
            }
        }

        setContent {
            MyApplicationTheme {
                if (isTest) {
                    PomodoroTestContent(onDismiss = { stopAll(); finish() })
                } else {
                    PomodoroFinishContent(
                        phase = phase,
                        sessionNumber = sessionNumber,
                        totalSessions = totalSessions,
                        taskTitle = taskTitle,
                        durationMinutes = durationMinutes,
                        nextActionLabel = nextActionLabel,
                        canProceed = canProceed,
                        isFinal = isFinal,
                        onContinue = { sendAction("continue") },
                        onEnd = { sendAction("end") }
                    )
                }

                DisposableEffect(Unit) {
                    onDispose {
                        stopAll()
                    }
                }
            }
        }

        // Safety auto-stop after 5 minutes
        Thread {
            try {
                Thread.sleep(5 * 60 * 1000L)
                runOnUiThread { stopAll(); finish() }
            } catch (e: InterruptedException) { /* stopped by user */ }
        }.apply { isDaemon = true }.start()
    }

    private fun backupSaveSession(phase: String, sessionNumber: Int, taskTitle: String, minutesElapsed: Int, taskId: Long) {
        if (minutesElapsed <= 0) return
        if (phase != "FOCUS") return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(this@PomodoroFinishActivity)
                val allSessions = db.timerSessionDao().getAllSync()
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val alreadySaved = allSessions.any { s ->
                    s.type == "POMODORO" && s.date == today &&
                        s.durationSeconds == minutesElapsed * 60 &&
                        s.taskId == (if (taskId > 0) taskId else null) &&
                        s.label == taskTitle
                }
                if (!alreadySaved) {
                    db.timerSessionDao().insert(
                        TimerSessionEntity(
                            type = "POMODORO",
                            taskId = if (taskId > 0) taskId else null,
                            label = taskTitle,
                            durationSeconds = minutesElapsed * 60,
                            date = today
                        )
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("PomodoroFinishActivity", "backupSaveSession failed", e)
            }
        }
    }

    private fun sendAction(action: String) {
        stopAll()
        val intent = Intent(this, com.example.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("pomodoro_action", action)
        }
        startActivity(intent)
        finish()
    }

    private fun stopAll() {
        stopRingtone()
        vibrator?.cancel()
    }

    private fun stopRingtone() {
        ringtone?.stop()
        ringtone = null
    }

    override fun onDestroy() {
        stopAll()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
            audioManager?.abandonAudioFocusRequest(audioFocusRequest as AudioFocusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(null)
        }
        super.onDestroy()
    }
}

@Composable
private fun PomodoroFinishContent(
    phase: String,
    sessionNumber: Int,
    totalSessions: Int?,
    taskTitle: String,
    durationMinutes: Int,
    nextActionLabel: String,
    canProceed: Boolean,
    isFinal: Boolean,
    onContinue: () -> Unit,
    onEnd: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(400))

    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Phase icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        if (phase == "FOCUS") Color(0xFF4CAF50).copy(alpha = 0.15f)
                        else Color(0xFFFF9800).copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (phase == "FOCUS") "✓" else "☕",
                    fontSize = 48.sp,
                    color = if (phase == "FOCUS") Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = if (phase == "FOCUS") "Focus Complete!" else "Break Over!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Session info
            val sessionStr = totalSessions?.let { "Session $sessionNumber of $it" }
                ?: "Session $sessionNumber"
            Text(
                text = sessionStr,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Task name
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (taskTitle.isNotBlank()) "\"$taskTitle\"" else "Untitled Session",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            if (durationMinutes > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Completed ${durationMinutes} minutes",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (canProceed && nextActionLabel.isNotBlank()) {
                    Button(
                        onClick = onContinue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (phase == "FOCUS") Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = nextActionLabel,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onEnd,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (isFinal) "Finish" else "Done",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
    }
}

}

@Composable
private fun PomodoroTestContent(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🔔", fontSize = 48.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Pomodoro Alarm Test",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "This is how the completion screen looks.\nThe selected ringtone and vibration are playing.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Dismiss",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
