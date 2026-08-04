import re

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'r') as f:
    content = f.read()

bullet_task_item_sig = """fun BulletTaskItem(
    task: TaskEntity,
    subtasks: List<TaskEntity> = emptyList(),
    onCheckToggle: () -> Unit,
    onMigrate: (String) -> Unit,
    onDelete: () -> Unit,
    onStartPomodoro: () -> Unit,
    onTaskClick: () -> Unit,
    onEdit: () -> Unit = {},
    onSubtaskToggle: (TaskEntity) -> Unit = {},
    onMigrateSubtask: (TaskEntity, String) -> Unit = { _, _ -> },
    onMakeMainTask: (TaskEntity) -> Unit = {},
    onDeleteSubtask: (TaskEntity) -> Unit = {}"""

new_bullet_task_item_sig = """fun BulletTaskItem(
    task: TaskEntity,
    subtasks: List<TaskEntity> = emptyList(),
    onCheckToggle: () -> Unit,
    onMigrate: (String) -> Unit,
    onDelete: () -> Unit,
    onStartPomodoro: () -> Unit,
    onTaskClick: () -> Unit,
    onEdit: () -> Unit = {},
    onSubtaskToggle: (TaskEntity) -> Unit = {},
    onMigrateSubtask: (TaskEntity, String) -> Unit = { _, _ -> },
    onMakeMainTask: (TaskEntity) -> Unit = {},
    onDeleteSubtask: (TaskEntity) -> Unit = {},
    onReorder: ((Int, Boolean) -> Unit)? = null"""

content = content.replace(bullet_task_item_sig, new_bullet_task_item_sig)

bullet_task_impl = """    var expandedMenu by remember { mutableStateOf(false) }

    val isCompleted = task.status == "COMPLETED"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {"""

new_bullet_task_impl = """    var expandedMenu by remember { mutableStateOf(false) }
    val isCompleted = task.status == "COMPLETED"

    var offsetX by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var offsetY by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .androidx.compose.ui.zIndex(if (isDragging) 1f else 0f)
            .androidx.compose.foundation.layout.offset { androidx.compose.ui.unit.IntOffset(Math.round(offsetX), Math.round(offsetY)) }
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .then(
                if (onReorder != null && !isCompleted) {
                    Modifier.androidx.compose.ui.input.pointer.pointerInput(task.id) {
                        androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress(
                            onDragStart = { isDragging = true },
                            onDragEnd = {
                                isDragging = false
                                val itemHeight = 60.dp.toPx()
                                val deltaIndex = Math.round(offsetY / itemHeight)
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

content = content.replace(bullet_task_impl, new_bullet_task_impl)

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'w') as f:
    f.write(content)
