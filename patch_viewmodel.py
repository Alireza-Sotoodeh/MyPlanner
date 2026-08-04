import re

with open('app/src/main/java/com/example/ui/viewmodel/MainViewModel.kt', 'r') as f:
    content = f.read()

reorder_func = """    fun reorderTask(task: TaskEntity, activeTasks: List<TaskEntity>, deltaIndex: Int, isSubtask: Boolean) {
        viewModelScope.launch {
            val currentIndex = activeTasks.indexOf(task)
            if (currentIndex == -1) return@launch

            val newIndex = (currentIndex + deltaIndex).coerceIn(0, activeTasks.size - 1)
            
            if (isSubtask && newIndex > 0) {
                // Make it a subtask of the item above its new position
                val targetParent = activeTasks[newIndex - 1]
                updateTask(task.copy(parentTaskId = targetParent.id))
            } else if (deltaIndex != 0) {
                // Reorder
                val mutableTasks = activeTasks.toMutableList()
                mutableTasks.removeAt(currentIndex)
                mutableTasks.add(newIndex, task)
                
                // Update priorities for all affected tasks to match their new index
                val updatedTasks = mutableTasks.mapIndexed { index, t ->
                    t.copy(priority = index)
                }
                database.taskDao().updateTaskPriorities(updatedTasks)
            }
        }
    }
"""

# Insert before the last closing brace
content = content.rstrip()
if content.endswith('}'):
    content = content[:-1] + "\n" + reorder_func + "}\n"

with open('app/src/main/java/com/example/ui/viewmodel/MainViewModel.kt', 'w') as f:
    f.write(content)
