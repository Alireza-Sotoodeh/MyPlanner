package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "day_reviews",
    indices = [Index(value = ["date"], unique = true)]
)
data class DayReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val good: String = "",
    val bad: String = "",
    val improve: String = "",
    val gratitude: String = "",
    val moodRating: Int = 3,
    val score: Int = 5,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
