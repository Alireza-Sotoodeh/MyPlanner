import re

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'r') as f:
    content = f.read()

old_call = """                                    onMakeMainTask = { subtask -> viewModel.updateTask(subtask.copy(parentTaskId = null)) },
                                    onDeleteSubtask = { subtask -> viewModel.deleteTask(subtask) }
                                )"""

new_call = """                                    onMakeMainTask = { subtask -> viewModel.updateTask(subtask.copy(parentTaskId = null)) },
                                    onDeleteSubtask = { subtask -> viewModel.deleteTask(subtask) },
                                    onReorder = { deltaIndex, isSubtask ->
                                        viewModel.reorderTask(task, activeTasks, deltaIndex, isSubtask)
                                    }
                                )"""

# We only want to replace the first one (for activeTasks). The second one is for completedTasks.
content = content.replace(old_call, new_call, 1)

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'w') as f:
    f.write(content)
