package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(entity = TaskEntity::class, parentColumns = ["id"], childColumns = ["parentTaskId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = TodoEntity::class, parentColumns = ["id"], childColumns = ["linkedTodoId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = IdeaEntity::class, parentColumns = ["id"], childColumns = ["linkedIdeaId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = LearnSectionEntity::class, parentColumns = ["id"], childColumns = ["linkedLearnSectionId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [
        Index("parentTaskId"),
        Index("linkedTodoId"),
        Index("linkedIdeaId"),
        Index("linkedLearnSectionId")
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    /** Date string: "yyyy-MM-dd" for daily, "yyyy-MM" for monthly, "yyyy-'W'ww" for weekly tasks */
    val date: String,
    val status: String = "PENDING", // PENDING, COMPLETED, MIGRATED, CANCELLED
    val type: String = "TASK", // TASK (•), EVENT (o), NOTE (-)
    val durationMinutes: Int = 0, // custom Pomodoro duration (0 = unset, use Pomodoro/Chronometer)
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
    val linkedLearnSectionId: Long? = null,
    val postponed: Boolean = false,
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val dateType: String
        get() = when {
            date.matches(Regex("^\\d{4}-\\d{2}$")) -> "monthly"
            date.matches(Regex("^\\d{4}-W\\d{2}$")) -> "weekly"
            else -> "daily"
        }
}
