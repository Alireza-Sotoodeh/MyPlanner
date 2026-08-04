package com.example.core.repository

import com.example.core.database.dao.DiaryDao
import com.example.core.database.entity.DiaryEntryEntity
import kotlinx.coroutines.flow.Flow

class DiaryRepository(private val diaryDao: DiaryDao) {
    fun getEntryForDate(date: String): Flow<DiaryEntryEntity?> = diaryDao.getEntryForDate(date)

    fun getAllEntries(): Flow<List<DiaryEntryEntity>> = diaryDao.getAllEntries()

    fun getAllDates(): Flow<List<String>> = diaryDao.getAllDates()

    suspend fun insertEntry(entry: DiaryEntryEntity): Long = diaryDao.insertEntry(entry)

    suspend fun deleteEntryByDate(date: String) = diaryDao.deleteEntryByDate(date)
}
