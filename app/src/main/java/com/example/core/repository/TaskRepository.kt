package com.example.core.repository

import com.example.core.database.dao.TaskDao
import com.example.core.database.dao.PomodoroSessionDao
import com.example.core.database.entity.TaskEntity
import com.example.core.database.entity.PomodoroSessionEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val pomodoroSessionDao: PomodoroSessionDao
) {
    fun getTasksForDate(date: String): Flow<List<TaskEntity>> = taskDao.getTasksForDate(date)

    fun getTasksForMonth(datePrefix: String): Flow<List<TaskEntity>> = taskDao.getTasksForMonth(datePrefix)

    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun deleteTaskAndSubtasks(task: TaskEntity) = taskDao.deleteTaskAndSubtasks(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun updateTaskPriorities(tasks: List<TaskEntity>) = taskDao.updateTaskPriorities(tasks)

    // Pomodoro session history
    fun getSessionsForTask(taskId: Long): Flow<List<PomodoroSessionEntity>> = 
        pomodoroSessionDao.getSessionsForTask(taskId)

    fun getAllSessions(): Flow<List<PomodoroSessionEntity>> = 
        pomodoroSessionDao.getAllSessions()

    suspend fun insertSession(session: PomodoroSessionEntity): Long = 
        pomodoroSessionDao.insertSession(session)

    suspend fun deleteSessionsForTask(taskId: Long) = 
        pomodoroSessionDao.deleteSessionsForTask(taskId)
}
