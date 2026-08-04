package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // BINARY, QUANTITATIVE
    val target: Float = 1.0f, // e.g. 8.0 for 8 glasses of water, 1.0 for binary habit
    val unit: String = "times", // e.g. "glasses", "hours", "completed"
    val createdAt: Long = System.currentTimeMillis()
)
