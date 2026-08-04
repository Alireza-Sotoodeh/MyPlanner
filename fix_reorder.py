import re

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'r') as f:
    content = f.read()

# 1. Modify BulletTaskItem signature
old_sig = """    onDeleteSubtask: (TaskEntity) -> Unit = {},
    onReorder: ((Int, Boolean) -> Unit)? = null
) {"""

new_sig = """    onDeleteSubtask: (TaskEntity) -> Unit = {},
    onReorder: ((Float, Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {"""

content = content.replace(old_sig, new_sig)

# 2. Add modifier to Column in BulletTaskItem
old_col = """    Column(
        modifier = Modifier"""

new_col = """    Column(
        modifier = modifier.then(Modifier"""

content = content.replace(old_col, new_col)

# 3. Change onReorder invocation in BulletTaskItem
old_reorder_call = """                                val itemHeight = 60.dp.toPx()
                                val deltaIndex = (offsetY / itemHeight).roundToInt()
                                val isSubtask = offsetX > 50.dp.toPx()
                                offsetX = 0f
                                offsetY = 0f
                                if (deltaIndex != 0 || isSubtask) {
                                    onReorder(deltaIndex, isSubtask)
                                }"""

new_reorder_call = """                                val isSubtask = offsetX > 50.dp.toPx()
                                val finalOffsetY = offsetY
                                offsetX = 0f
                                offsetY = 0f
                                if (finalOffsetY != 0f || isSubtask) {
                                    onReorder(finalOffsetY, isSubtask)
                                }"""

content = content.replace(old_reorder_call, new_reorder_call)

# 4. Modify how it's called in Daily View
old_daily_items = """                        items(activeTasks, key = { it.id }) { task ->
                            var showInteractDialog by remember { mutableStateOf(false) }
                            val taskSubtasks = allTasks.filter { it.parentTaskId == task.id }"""

new_daily_items = """                        val itemHeights = remember { androidx.compose.runtime.mutableStateMapOf<Long, Int>() }
                        items(activeTasks, key = { it.id }) { task ->
                            var showInteractDialog by remember { mutableStateOf(false) }
                            val taskSubtasks = allTasks.filter { it.parentTaskId == task.id }"""

content = content.replace(old_daily_items, new_daily_items)

old_daily_call = """                            BulletTaskItem(
                                task = task,
                                subtasks = taskSubtasks,
                                onCheckToggle = { viewModel.toggleTaskStatus(task) },
                                onMigrate = { viewModel.updateTask(task.copy(date = it, status = "MIGRATED")) },
                                onDelete = { viewModel.deleteTask(task) },
                                onStartPomodoro = { showInteractDialog = true },
                                onTaskClick = { showInteractDialog = true },
                                onEdit = { onEditTask(task, taskSubtasks) },
                                onSubtaskToggle = { viewModel.toggleTaskStatus(it) },
                                onMigrateSubtask = { t, d -> viewModel.updateTask(t.copy(date = d, status = "MIGRATED")) },
                                onMakeMainTask = { viewModel.updateTask(it.copy(parentTaskId = null)) },
                                onDeleteSubtask = { viewModel.deleteTask(it) },
                                onReorder = { delta, isSubtask -> viewModel.reorderTask(task, activeTasks, delta, isSubtask) }
                            )"""

new_daily_call = """                            BulletTaskItem(
                                modifier = Modifier.androidx.compose.ui.layout.onGloballyPositioned { itemHeights[task.id] = it.size.height },
                                task = task,
                                subtasks = taskSubtasks,
                                onCheckToggle = { viewModel.toggleTaskStatus(task) },
                                onMigrate = { viewModel.updateTask(task.copy(date = it, status = "MIGRATED")) },
                                onDelete = { viewModel.deleteTask(task) },
                                onStartPomodoro = { showInteractDialog = true },
                                onTaskClick = { showInteractDialog = true },
                                onEdit = { onEditTask(task, taskSubtasks) },
                                onSubtaskToggle = { viewModel.toggleTaskStatus(it) },
                                onMigrateSubtask = { t, d -> viewModel.updateTask(t.copy(date = d, status = "MIGRATED")) },
                                onMakeMainTask = { viewModel.updateTask(it.copy(parentTaskId = null)) },
                                onDeleteSubtask = { viewModel.deleteTask(it) },
                                onReorder = { draggedOffsetY, isSubtask ->
                                    val draggedIndex = activeTasks.indexOf(task)
                                    if (draggedIndex == -1) return@BulletTaskItem
                                    var currentOffset = draggedOffsetY
                                    var calculatedDelta = 0
                                    
                                    if (currentOffset > 0) {
                                        for (i in draggedIndex + 1 until activeTasks.size) {
                                            val h = itemHeights[activeTasks[i].id] ?: 150
                                            if (currentOffset > h / 2f) {
                                                calculatedDelta++
                                                currentOffset -= h
                                            } else break
                                        }
                                    } else if (currentOffset < 0) {
                                        for (i in draggedIndex - 1 downTo 0) {
                                            val h = itemHeights[activeTasks[i].id] ?: 150
                                            if (-currentOffset > h / 2f) {
                                                calculatedDelta--
                                                currentOffset += h
                                            } else break
                                        }
                                    }
                                    if (calculatedDelta != 0 || isSubtask) {
                                        viewModel.reorderTask(task, activeTasks, calculatedDelta, isSubtask)
                                    }
                                }
                            )"""

content = content.replace(old_daily_call, new_daily_call)

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'w') as f:
    f.write(content)
