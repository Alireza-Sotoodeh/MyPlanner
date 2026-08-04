package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.TimerTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerTemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: TimerTemplateEntity): Long

    @Query("UPDATE timer_templates SET name = :name, focusMinutes = :focusMinutes, shortBreakMinutes = :shortBreakMinutes, longBreakMinutes = :longBreakMinutes, targetSessions = :targetSessions WHERE id = :id")
    suspend fun update(id: Long, name: String, focusMinutes: Int, shortBreakMinutes: Int?, longBreakMinutes: Int?, targetSessions: Int?)

    @Query("DELETE FROM timer_templates WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM timer_templates ORDER BY name ASC")
    fun getAll(): Flow<List<TimerTemplateEntity>>

    @Query("DELETE FROM timer_templates")
    suspend fun deleteAll()
}
