package com.example.core.repository

import com.example.core.database.dao.DayReviewDao
import com.example.core.database.entity.DayReviewEntity
import kotlinx.coroutines.flow.Flow

class DayReviewRepository(private val dayReviewDao: DayReviewDao) {
    fun getReviewForDate(date: String): Flow<DayReviewEntity?> = dayReviewDao.getReviewForDate(date)

    fun getAllReviews(): Flow<List<DayReviewEntity>> = dayReviewDao.getAllReviews()

    suspend fun insertReview(review: DayReviewEntity): Long = dayReviewDao.insertReview(review)

    suspend fun deleteReviewByDate(date: String) = dayReviewDao.deleteReviewByDate(date)
}
