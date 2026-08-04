package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.DayReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DayReviewDao {
    @Query("SELECT * FROM day_reviews WHERE date = :date LIMIT 1")
    fun getReviewForDate(date: String): Flow<DayReviewEntity?>

    @Query("SELECT * FROM day_reviews ORDER BY date DESC")
    fun getAllReviews(): Flow<List<DayReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: DayReviewEntity): Long

    @Query("DELETE FROM day_reviews WHERE date = :date")
    suspend fun deleteReviewByDate(date: String)

    @Query("DELETE FROM day_reviews")
    suspend fun deleteAll()
}
