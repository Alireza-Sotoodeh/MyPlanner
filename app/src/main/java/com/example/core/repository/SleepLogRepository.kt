package com.example.core.repository

import com.example.core.database.dao.SleepLogDao
import com.example.core.database.entity.SleepLogEntity
import kotlinx.coroutines.flow.Flow

class SleepLogRepository(private val sleepLogDao: SleepLogDao) {
    val allSleepLogs: Flow<List<SleepLogEntity>> = sleepLogDao.getAllSleepLogs()

    suspend fun getSleepLogForDate(date: String): SleepLogEntity? = sleepLogDao.getSleepLogForDate(date)

    fun getSleepLogForDateFlow(date: String): Flow<SleepLogEntity?> = sleepLogDao.getSleepLogForDateFlow(date)

    suspend fun insertSleepLog(sleepLog: SleepLogEntity): Long = sleepLogDao.insertSleepLog(sleepLog)

    suspend fun deleteSleepLogByDate(date: String) = sleepLogDao.deleteSleepLogByDate(date)
}
