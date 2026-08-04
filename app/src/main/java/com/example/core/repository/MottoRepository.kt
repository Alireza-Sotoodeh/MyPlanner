package com.example.core.repository

import com.example.core.database.dao.MottoDao
import com.example.core.database.entity.MottoEntity
import kotlinx.coroutines.flow.Flow

class MottoRepository(private val mottoDao: MottoDao) {
    val allMottos: Flow<List<MottoEntity>> = mottoDao.getAllMottos()

    suspend fun getRandomMotto(): MottoEntity? = mottoDao.getRandomMotto()

    suspend fun insertMotto(motto: MottoEntity): Long = mottoDao.insertMotto(motto)

    suspend fun updateMotto(motto: MottoEntity) = mottoDao.updateMotto(motto)

    suspend fun deleteMotto(motto: MottoEntity) = mottoDao.deleteMotto(motto)
}
