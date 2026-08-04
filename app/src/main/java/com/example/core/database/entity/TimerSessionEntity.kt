package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer_sessions")
data class TimerSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "POMODORO" or "CHRONOMETER"
    val taskId: Long? = null,
    val label: String = "",
    val durationSeconds: Int,
    val date: String, // "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val templateName: String? = null
)
