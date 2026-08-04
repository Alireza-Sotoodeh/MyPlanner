package com.example.core.receiver

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
import kotlinx.coroutines.launch
import com.example.core.database.AppDatabase
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.MainActivity
import com.example.ui.screens.AlarmActivity
import com.example.R

class ReminderReceiver : BroadcastReceiver() {
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
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
            return
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
                    cal.add(Calendar.DAY_OF_YEAR, 1) // Tomorrow
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
            // Alarm clock style using Full Screen Intent
            showNotification(context, title, message, vibrate, sound, taskId, true)
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
