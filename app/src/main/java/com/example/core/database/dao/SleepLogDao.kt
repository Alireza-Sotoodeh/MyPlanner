package com.example.core.database.dao

import androidx.room.*
import com.example.core.database.entity.SleepLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepLogDao {
    @Query("SELECT * FROM sleep_logs ORDER BY date DESC")
    fun getAllSleepLogs(): Flow<List<SleepLogEntity>>

    @Query("SELECT * FROM sleep_logs WHERE date = :date LIMIT 1")
    suspend fun getSleepLogForDate(date: String): SleepLogEntity?

    @Query("SELECT * FROM sleep_logs WHERE date = :date")
    fun getSleepLogForDateFlow(date: String): Flow<SleepLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepLog(sleepLog: SleepLogEntity): Long

    @Query("DELETE FROM sleep_logs WHERE date = :date")
    suspend fun deleteSleepLogByDate(date: String)
}
