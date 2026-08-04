with open('app/src/main/java/com/example/core/manager/ReminderManager.kt', 'r') as f:
    content = f.read()

old_night_before = """                    scheduleAlarm(
                        context = context,
                        alarmManager = alarmManager,
                        timeInMillis = cal.timeInMillis,
                        taskId = task.id * 10, // unique id for night before
                        title = "Tomorrow: ${task.title}",
                        message = "Your event is tomorrow at ${task.eventTime}",
                        vibrate = vibrate,
                        sound = sound
                    )"""

new_night_before = """                    scheduleAlarm(
                        context = context,
                        alarmManager = alarmManager,
                        timeInMillis = cal.timeInMillis,
                        taskId = task.id * 10, // unique id for night before
                        isNightBefore = true,
                        title = "Tomorrow: ${task.title}",
                        message = "Your event is tomorrow at ${task.eventTime}",
                        vibrate = vibrate,
                        sound = sound
                    )"""

old_mins_before = """                        scheduleAlarm(
                            context = context,
                            alarmManager = alarmManager,
                            timeInMillis = cal.timeInMillis,
                            taskId = task.id * 10 + 1, // unique id for X mins before
                            title = "Upcoming Event: ${task.title}",
                            message = "Starting in $minutes minutes",
                            vibrate = vibrate,
                            sound = sound
                        )"""

new_mins_before = """                        scheduleAlarm(
                            context = context,
                            alarmManager = alarmManager,
                            timeInMillis = cal.timeInMillis,
                            taskId = task.id * 10 + 1, // unique id for X mins before
                            isNightBefore = false,
                            title = "Upcoming Event: ${task.title}",
                            message = "Starting in $minutes minutes",
                            vibrate = vibrate,
                            sound = sound
                        )"""

old_intent = """        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("vibrate", vibrate)
            putExtra("sound", sound)
            putExtra("taskId", taskId)
        }"""

new_intent = """        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("vibrate", vibrate)
            putExtra("sound", sound)
            putExtra("taskId", taskId)
            putExtra("isNightBefore", isNightBefore)
        }"""

content = content.replace(old_night_before, new_night_before)
content = content.replace(old_mins_before, new_mins_before)
content = content.replace(old_intent, new_intent)

with open('app/src/main/java/com/example/core/manager/ReminderManager.kt', 'w') as f:
    f.write(content)
