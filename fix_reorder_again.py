import re

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'r') as f:
    content = f.read()

# Add modifier to first BulletTaskItem
content = content.replace("                                BulletTaskItem(\n                                    task = task,", 
                          "                                BulletTaskItem(\n                                    modifier = Modifier.androidx.compose.ui.layout.onGloballyPositioned { itemHeights[task.id] = it.size.height },\n                                    task = task,")

# Replace onReorder logic
old_reorder = """                                    onReorder = { deltaIndex, isSubtask ->
                                        viewModel.reorderTask(task, activeTasks, deltaIndex, isSubtask)
                                    }"""

new_reorder = """                                    onReorder = { draggedOffsetY, isSubtask ->
                                        val draggedIndex = activeTasks.indexOf(task)
                                        if (draggedIndex != -1) {
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
                                    }"""

content = content.replace(old_reorder, new_reorder, 1) # Only replace the first one (activeTasks)

old_reorder2 = """                                            onReorder = { deltaIndex, isSubtask ->
                                                viewModel.reorderTask(task, completedTasks, deltaIndex, isSubtask)
                                            }"""

new_reorder2 = """                                            onReorder = { _, _ -> }"""
content = content.replace(old_reorder2, new_reorder2)

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'w') as f:
    f.write(content)
