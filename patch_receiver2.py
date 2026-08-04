with open('app/src/main/java/com/example/core/receiver/ReminderReceiver.kt', 'r') as f:
    content = f.read()

import re

new_onReceive = """    override fun onReceive(context: Context, intent: Intent) {
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

    private fun showNotification(context: Context, title: String, message: String, vibrate: Boolean, sound: Boolean, taskId: Long, isAlarm: Boolean) {"""

content = re.sub(
    r"    override fun onReceive.*?private fun showNotification\(context: Context, title: String, message: String, vibrate: Boolean, sound: Boolean, taskId: Long\) \{",
    new_onReceive,
    content,
    flags=re.DOTALL
)

old_builder = """        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)"""

new_builder = """        val builder = NotificationCompat.Builder(context, channelId)
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
        }"""

content = content.replace(old_builder, new_builder)

with open('app/src/main/java/com/example/core/receiver/ReminderReceiver.kt', 'w') as f:
    f.write(content)
