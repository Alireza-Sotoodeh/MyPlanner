package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** "BINARY" for yes/no check-in habits, "QUANTITATIVE" for count-based habits */
    val type: String,
    val target: Float = 1.0f, // e.g. 8.0 for 8 glasses of water, 1.0 for binary habit
    val unit: String = "times", // e.g. "glasses", "hours", "completed"
    val recurrenceMode: String = "ALWAYS", // ALWAYS, WEEKLY
    val recurrenceInterval: Int = 1, // 1 for every week, 2 for every 2 weeks, etc.
    /** Day-of-week pattern using Calendar.DAY_OF_WEEK numbering: 1=Sunday, 2=Monday, ..., 7=Saturday */
    val recurrenceDaysOfWeek: String = "",
    val recurrenceEndDate: String? = null,
    val habitTime: String? = null, // notification time "HH:mm"
    val reminderEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
