package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.DiaryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries WHERE date = :date LIMIT 1")
    fun getEntryForDate(date: String): Flow<DiaryEntryEntity?>

    @Query("SELECT * FROM diary_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<DiaryEntryEntity>>

    @Query("SELECT date FROM diary_entries")
    fun getAllDates(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DiaryEntryEntity): Long

    @Query("DELETE FROM diary_entries WHERE date = :date")
    suspend fun deleteEntryByDate(date: String)
}
