package com.example.core.repository

import com.example.core.database.dao.HabitDao
import com.example.core.database.entity.HabitEntity
import com.example.core.database.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {
    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()

    fun getAllLogs(): Flow<List<HabitLogEntity>> = habitDao.getAllLogs()

    suspend fun insertHabit(habit: HabitEntity): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: HabitEntity) = habitDao.updateHabit(habit)

    suspend fun getHabitById(id: Long): HabitEntity? = habitDao.getHabitById(id)

    suspend fun deleteHabit(habit: HabitEntity) = habitDao.deleteHabit(habit)

    suspend fun deleteHabitById(id: Long) = habitDao.deleteHabitById(id)

    fun getLogsForDate(date: String): Flow<List<HabitLogEntity>> = habitDao.getLogsForDate(date)

    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>> = habitDao.getLogsForHabit(habitId)

    suspend fun insertLog(log: HabitLogEntity): Long = habitDao.insertLog(log)

    suspend fun deleteLogForDate(habitId: Long, date: String) = habitDao.deleteLogForDate(habitId, date)
}
