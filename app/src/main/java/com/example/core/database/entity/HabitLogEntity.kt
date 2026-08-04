package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_logs")
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: String, // "yyyy-MM-dd"
    val value: Float, // 1.0f for binary success, or actual numeric value for quantitative trackers
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
