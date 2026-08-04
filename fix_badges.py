import re

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'r') as f:
    content = f.read()

old_code = """                if (task.label.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (task.labelColor != null) Color(task.labelColor).copy(alpha = 0.2f) else MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = task.label.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (task.labelColor != null) Color(task.labelColor) else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }
            if (task.type == "EVENT" && !task.eventTime.isNullOrBlank()) {"""

new_code = """                if (task.label.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (task.labelColor != null) Color(task.labelColor).copy(alpha = 0.2f) else MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = task.label.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (task.labelColor != null) Color(task.labelColor) else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }
            
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val priorityColor = when(task.priorityLevel) {
                    "High" -> Color(0xFFE53935)
                    "Medium" -> Color(0xFFFB8C00)
                    "Low" -> Color(0xFF43A047)
                    else -> MaterialTheme.colorScheme.secondary
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = priorityColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${task.priorityLevel.uppercase()} PRIORITY",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = priorityColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                
                if (subtasks.isNotEmpty()) {
                    val completed = subtasks.count { it.status == "COMPLETED" }
                    val total = subtasks.size
                    val isAllCompleted = completed == total
                    val subtaskColor = if (isAllCompleted) Color(0xFF43A047) else MaterialTheme.colorScheme.primary
                    
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = subtaskColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "$completed/$total SUBTASKS",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = subtaskColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (task.type == "EVENT" && !task.eventTime.isNullOrBlank()) {"""

content = content.replace(old_code, new_code)

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'w') as f:
    f.write(content)
