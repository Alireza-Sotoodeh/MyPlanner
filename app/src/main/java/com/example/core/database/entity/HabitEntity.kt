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
    val recurrenceMode: String = "ALWAYS", // ALWAYS, WEEKLY
    val recurrenceInterval: Int = 1, // 1 for every week, 2 for every 2 weeks, etc.
    val recurrenceDaysOfWeek: String = "", // Comma-separated days 1-7 (1=Sun...7=Sat)
    val recurrenceEndDate: String? = null,
    val habitTime: String? = null, // notification time "HH:mm"
    val reminderEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
