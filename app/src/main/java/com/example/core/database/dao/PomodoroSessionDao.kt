package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entity.PomodoroSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroSessionDao {
    @Query("SELECT * FROM pomodoro_sessions WHERE taskId = :taskId ORDER BY timestamp DESC")
    fun getSessionsForTask(taskId: Long): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT * FROM pomodoro_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<PomodoroSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PomodoroSessionEntity): Long

    @Query("DELETE FROM pomodoro_sessions WHERE taskId = :taskId")
    suspend fun deleteSessionsForTask(taskId: Long)
}
