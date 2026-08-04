package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY priority ASC, id ASC")
    suspend fun getTasksForDateSync(date: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY priority ASC, id ASC")
    fun getTasksForDate(date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date LIKE :datePrefix || '%' ORDER BY date ASC, priority ASC")
    fun getTasksForMonth(datePrefix: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE type = 'EVENT' AND eventTime IS NOT NULL AND eventTime != ''")
    suspend fun getUpcomingEventsSync(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE parentTaskId = :parentId")
    suspend fun getSubtasks(parentId: Long): List<TaskEntity>

    @Query("DELETE FROM tasks WHERE parentTaskId = :parentId")
    suspend fun deleteSubtasks(parentId: Long)

    @Transaction
    suspend fun deleteTaskAndSubtasks(task: TaskEntity) {
        deleteSubtasks(task.id)
        deleteTask(task)
    }

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Transaction
    suspend fun updateTaskPriorities(tasks: List<TaskEntity>) {
        tasks.forEach { updateTask(it) }
    }
}
