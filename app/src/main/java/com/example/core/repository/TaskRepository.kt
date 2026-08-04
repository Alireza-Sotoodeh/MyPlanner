package com.example.core.repository

import com.example.core.database.dao.TaskDao
import com.example.core.database.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao
) {
    fun getTasksForDate(date: String): Flow<List<TaskEntity>> = taskDao.getTasksForDate(date)

    suspend fun getTasksForDateSync(date: String): List<TaskEntity> = taskDao.getTasksForDateSync(date)

    fun getTasksForMonth(datePrefix: String): Flow<List<TaskEntity>> = taskDao.getTasksForMonth(datePrefix)

    fun getTasksForYear(yearPrefix: String): Flow<List<TaskEntity>> = taskDao.getTasksForYear(yearPrefix)

    fun getTasksForDateRange(startDate: String, endDate: String): Flow<List<TaskEntity>> = taskDao.getTasksForDateRange(startDate, endDate)

    suspend fun getTasksForDateRangeSync(startDate: String, endDate: String): List<TaskEntity> = taskDao.getTasksForDateRangeSync(startDate, endDate)

    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun deleteTaskAndSubtasks(task: TaskEntity) = taskDao.deleteTaskAndSubtasks(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun updateTaskPriorities(tasks: List<TaskEntity>) = taskDao.updateTaskPriorities(tasks)

    suspend fun getSubtasks(parentId: Long): List<TaskEntity> = taskDao.getSubtasks(parentId)
}
