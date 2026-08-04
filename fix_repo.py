with open('app/src/main/java/com/example/core/repository/TaskRepository.kt', 'r') as f:
    content = f.read()

target = """    suspend fun insertTask(task: TaskEntity): Long {
        return taskDao.insertTask(task)
    }"""

new_target = """    suspend fun insertTask(task: TaskEntity): Long {
        return taskDao.insertTask(task)
    }

    suspend fun updateTaskPriorities(tasks: List<TaskEntity>) {
        taskDao.updateTaskPriorities(tasks)
    }"""

content = content.replace(target, new_target)

with open('app/src/main/java/com/example/core/repository/TaskRepository.kt', 'w') as f:
    f.write(content)
