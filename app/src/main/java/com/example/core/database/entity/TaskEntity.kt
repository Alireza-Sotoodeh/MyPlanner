package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val date: String, // "yyyy-MM-dd" for daily planner, "yyyy-MM" for monthly, "yyyy-Www" for weekly
    val status: String = "PENDING", // PENDING, COMPLETED, MIGRATED, CANCELLED
    val type: String = "TASK", // TASK (•), EVENT (o), NOTE (-)
    val durationMinutes: Int = 25, // custom Pomodoro duration
    val pomodorosCompleted: Int = 0,
    val priority: Int = 0, // ordering within the day
    val label: String = "", // labeling system (e.g., Work, Personal, Health, Study, etc.)
    val labelColor: Long? = null,
    val recurrenceMode: String = "NONE", // NONE, WEEKLY
    val recurrenceInterval: Int = 1, // 1 for every week, 2 for every 2 weeks, etc.
    val recurrenceDaysOfWeek: String = "", // Comma-separated days 1-7 (1=Sun...7=Sat)
    val recurrenceEndDate: String? = null,
    val subtaskImportance: String = "OPTIONAL", // OPTIONAL, IMPORTANT
    val eventTime: String? = null, // e.g., "14:30"
    val notifyNightBefore: Boolean = false,
    val reminderMinutesBefore: Int? = null,
    val notes: String = "", // additional notes
    val priorityLevel: String = "Medium", // Low, Medium, High
    val createdAt: Long = System.currentTimeMillis(),
    val parentTaskId: Long? = null, // For subtasks
    val targetSessions: Int? = null,
    val breakMinutes: Int? = 5,
    val linkedTodoId: Long? = null,
    val linkedIdeaId: Long? = null,
    val postponed: Boolean = false
)
