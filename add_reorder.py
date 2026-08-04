with open('app/src/main/java/com/example/ui/viewmodel/MainViewModel.kt', 'r') as f:
    content = f.read()

insert_target = """    // Helper utilities for date"""
new_reorder_func = """    fun reorderTask(task: TaskEntity, activeTasks: List<TaskEntity>, deltaIndex: Int, isSubtask: Boolean) {
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
                taskRepository.updateTaskPriorities(updatedTasks)
            }
        }
    }

    // Helper utilities for date"""

if "fun reorderTask" not in content:
    content = content.replace(insert_target, new_reorder_func)

with open('app/src/main/java/com/example/ui/viewmodel/MainViewModel.kt', 'w') as f:
    f.write(content)
