package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "habit_logs",
    foreignKeys = [
        ForeignKey(entity = HabitEntity::class, parentColumns = ["id"], childColumns = ["habitId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index(value = ["habitId", "date"], unique = true),
        Index("habitId")
    ]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val date: String, // "yyyy-MM-dd"
    val value: Float, // 1.0f for binary success, or actual numeric value for quantitative trackers
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
