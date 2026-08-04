package com.example.core.repository

import com.example.core.database.dao.TimerSessionDao
import com.example.core.database.dao.TimerTemplateDao
import com.example.core.database.entity.TimerSessionEntity
import com.example.core.database.entity.TimerTemplateEntity
import kotlinx.coroutines.flow.Flow

class TimerRepository(
    private val timerSessionDao: TimerSessionDao,
    private val timerTemplateDao: TimerTemplateDao
) {
    // Timer sessions
    fun getSessionsForDateRange(startDate: String, endDate: String): Flow<List<TimerSessionEntity>> =
        timerSessionDao.getByDateRange(startDate, endDate)

    fun getAllSessions(): Flow<List<TimerSessionEntity>> =
        timerSessionDao.getAll()

    fun getSessionsForDate(date: String): Flow<List<TimerSessionEntity>> =
        timerSessionDao.getByDate(date)

    suspend fun insertSession(session: TimerSessionEntity): Long =
        timerSessionDao.insert(session)

    suspend fun updateSession(id: Long, durationSeconds: Int, note: String, date: String) =
        timerSessionDao.update(id, durationSeconds, note, date)

    suspend fun updateSessionTaskId(id: Long, taskId: Long?, label: String) =
        timerSessionDao.updateTaskId(id, taskId, label, System.currentTimeMillis())

    suspend fun deleteSession(id: Long) =
        timerSessionDao.delete(id)

    // Timer templates
    fun getAllTemplates(): Flow<List<TimerTemplateEntity>> =
        timerTemplateDao.getAll()

    suspend fun insertTemplate(template: TimerTemplateEntity): Long =
        timerTemplateDao.insert(template)

    suspend fun updateTemplate(
        id: Long, name: String, focusMinutes: Int,
        shortBreakMinutes: Int?, longBreakMinutes: Int?, targetSessions: Int?
    ) = timerTemplateDao.update(id, name, focusMinutes, shortBreakMinutes, longBreakMinutes, targetSessions)

    suspend fun deleteTemplate(id: Long) =
        timerTemplateDao.delete(id)
}
