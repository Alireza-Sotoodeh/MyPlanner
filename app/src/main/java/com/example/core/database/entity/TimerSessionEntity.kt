package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "timer_sessions",
    foreignKeys = [
        ForeignKey(entity = TaskEntity::class, parentColumns = ["id"], childColumns = ["taskId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [
        Index("taskId")
    ]
)
data class TimerSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "POMODORO" or "CHRONOMETER"
    val taskId: Long? = null,
    val label: String = "",
    val durationSeconds: Int,
    val date: String, // "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    /** Soft reference to TimerTemplateEntity.name (not a foreign key) */
    val templateName: String? = null
)
