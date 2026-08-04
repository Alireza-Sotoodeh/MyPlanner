package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.TimerSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: TimerSessionEntity): Long

    @Query("UPDATE timer_sessions SET durationSeconds = :durationSeconds, note = :note, date = :date WHERE id = :id")
    suspend fun update(id: Long, durationSeconds: Int, note: String, date: String)

    @Query("DELETE FROM timer_sessions WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM timer_sessions WHERE date >= :startDate AND date <= :endDate ORDER BY timestamp DESC")
    fun getByDateRange(startDate: String, endDate: String): Flow<List<TimerSessionEntity>>

    @Query("SELECT * FROM timer_sessions ORDER BY timestamp DESC")
    fun getAll(): Flow<List<TimerSessionEntity>>

    @Query("SELECT * FROM timer_sessions WHERE date = :date ORDER BY timestamp DESC")
    fun getByDate(date: String): Flow<List<TimerSessionEntity>>
}
