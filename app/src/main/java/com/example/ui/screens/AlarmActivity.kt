package com.example.ui.screens

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import java.util.concurrent.TimeUnit

class AlarmActivity : ComponentActivity() {
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val title = intent.getStringExtra("title") ?: "Event Reminder"
        val message = intent.getStringExtra("message") ?: "Starting soon"
        val vibrateEnabled = intent.getBooleanExtra("vibrate", true)
        val soundEnabled = intent.getBooleanExtra("sound", true)

        if (soundEnabled) {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ringtone = RingtoneManager.getRingtone(this, alarmUri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone?.isLooping = true
            }
            ringtone?.play()
        }

        if (vibrateEnabled) {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            val pattern = longArrayOf(0, 1000, 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            text = message,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    stopAlarm()
                                    finish()
                                },
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text("Understood", fontSize = 18.sp, modifier = Modifier.padding(8.dp))
                            }
                            Button(
                                onClick = {
                                    snoozeAlarm()
                                },
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text("Snooze 5 min", fontSize = 18.sp, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                    
                    DisposableEffect(Unit) {
                        onDispose {
                            stopAlarm()
                        }
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }
    
    private fun stopAlarm() {
        ringtone?.stop()
        vibrator?.cancel()
    }

    private fun snoozeAlarm() {
        stopAlarm()
        
        val title = intent.getStringExtra("title") ?: "Event Reminder"
        val message = intent.getStringExtra("message") ?: "Starting soon"
        val vibrateEnabled = intent.getBooleanExtra("vibrate", true)
        val soundEnabled = intent.getBooleanExtra("sound", true)
        
        val snoozeMinutes = 5
        val triggerTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(snoozeMinutes.toLong())
        
        val snoozeIntent = Intent(this, com.example.core.receiver.ReminderReceiver::class.java).apply {
            action = "com.example.action.SNOOZE_ALARM"
            putExtra("title", title)
            putExtra("message", message)
            putExtra("vibrate", vibrateEnabled)
            putExtra("sound", soundEnabled)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            (System.currentTimeMillis() / 1000).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
        
        finish()
    }
}
