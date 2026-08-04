import re

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'r') as f:
    content = f.read()

# I want to wrap the outer Row in a Column, or better, just change the Row to Column and put a Row inside.
# But it's easier to use python to replace precisely.

bullet_start = """    Row(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 1f else 0f)
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isDragging) MaterialTheme.colorScheme.surfaceVariant
                else if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else Color.Transparent
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .then(
                if (onReorder != null && !isCompleted) {
                    Modifier.pointerInput(task.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { _ -> isDragging = true },
                            onDragEnd = {
                                isDragging = false
                                val itemHeight = 60.dp.toPx()
                                val deltaIndex = (offsetY / itemHeight).roundToInt()
                                val isSubtask = offsetX > 50.dp.toPx()
                                offsetX = 0f
                                offsetY = 0f
                                if (deltaIndex != 0 || isSubtask) {
                                    onReorder(deltaIndex, isSubtask)
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                offsetX = 0f
                                offsetY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            }
                        )
                    }
                } else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {"""

new_bullet_start = """    Column(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 1f else 0f)
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isDragging) MaterialTheme.colorScheme.surfaceVariant
                else if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else Color.Transparent
            )
            .then(
                if (onReorder != null && !isCompleted) {
                    Modifier.pointerInput(task.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { _ -> isDragging = true },
                            onDragEnd = {
                                isDragging = false
                                val itemHeight = 60.dp.toPx()
                                val deltaIndex = (offsetY / itemHeight).roundToInt()
                                val isSubtask = offsetX > 50.dp.toPx()
                                offsetX = 0f
                                offsetY = 0f
                                if (deltaIndex != 0 || isSubtask) {
                                    onReorder(deltaIndex, isSubtask)
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                offsetX = 0f
                                offsetY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            }
                        )
                    }
                } else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {"""

content = content.replace(bullet_start, new_bullet_start)

# End of BulletTaskItem
bullet_end = """                DropdownMenuItem(
                    text = { Text("Delete Intention") },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    onClick = {
                        onDelete()
                        expandedMenu = false
                    }
                )
            }
        }
    }
}"""

new_bullet_end = """                DropdownMenuItem(
                    text = { Text("Delete Intention") },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    onClick = {
                        onDelete()
                        expandedMenu = false
                    }
                )
            }
        }
    }
    if (subtasks.isNotEmpty()) {
        val completedSubtasks = subtasks.count { it.status == "COMPLETED" }
        val progress = if (subtasks.isEmpty()) 0f else completedSubtasks.toFloat() / subtasks.size
        androidx.compose.material3.LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    }
}
}"""

content = content.replace(bullet_end, new_bullet_end)

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'w') as f:
    f.write(content)

