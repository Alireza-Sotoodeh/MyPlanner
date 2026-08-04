import re

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'r') as f:
    content = f.read()

pattern = re.compile(r'            if \(task\.type == "EVENT" && !task\.eventTime\.isNullOrBlank\(\)\) \{\n                Text\(\n                    text = "🕐 \$\{task\.eventTime\}",\n                    fontSize = 11\.sp,\n                    color = MaterialTheme\.colorScheme\.primary,\n                    fontWeight = FontWeight\.Medium\n                \)\n            \}\n            if \(task\.description\.isNotEmpty\(\)\) \{\n                Text\(\n                    text = task\.description,\n                    fontSize = 11\.sp,\n                    color = MaterialTheme\.colorScheme\.onSurfaceVariant\n                \)\n            \}\n            Text\(\n                text = "\$\{task\.durationMinutes\}m Pomodoro • \$\{task\.pomodorosCompleted\} Completed",\n                fontSize = 10\.sp,\n                fontWeight = FontWeight\.Bold,\n                color = MaterialTheme\.colorScheme\.primary\.copy\(alpha = 0\.8f\)\n            \)\n            // Subtasks\n            if \(subtasks\.isNotEmpty\(\)\) \{\n                val completedSubtasks = subtasks\.count \{ it\.status == "COMPLETED" \}\n                val progress = if \(subtasks\.isEmpty\(\)\) 0f else completedSubtasks\.toFloat\(\) / subtasks\.size\n                var subtasksExpanded by remember \{ mutableStateOf\(false\) \}')

replacement = """            androidx.compose.animation.AnimatedVisibility(
                visible = isExpanded,
                enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
            ) {
                Column {
                    if (task.type == "EVENT" && !task.eventTime.isNullOrBlank()) {
                        Text(
                            text = "🕐 ${task.eventTime}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (task.description.isNotEmpty()) {
                        Text(
                            text = task.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${task.durationMinutes}m Pomodoro • ${task.pomodorosCompleted} Completed",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                    // Subtasks
                    if (subtasks.isNotEmpty()) {
                        val completedSubtasks = subtasks.count { it.status == "COMPLETED" }
                        val progress = if (subtasks.isEmpty()) 0f else completedSubtasks.toFloat() / subtasks.size
                        var subtasksExpanded by remember(isExpanded) { mutableStateOf(isExpanded) }"""

content = re.sub(pattern, replacement, content)

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'w') as f:
    f.write(content)
