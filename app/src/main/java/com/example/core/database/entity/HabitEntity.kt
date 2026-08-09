package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

/** Returns true if this habit is scheduled to occur on the given date (yyyy-MM-dd). */
fun HabitEntity.isActiveOn(date: String): Boolean {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val parsedDate = sdf.parse(date) ?: return true
    val cal = Calendar.getInstance().apply { time = parsedDate }
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

    val scheduled = when (recurrenceMode) {
        "ALWAYS" -> true
        "WEEKLY" -> {
            val days = recurrenceDaysOfWeek
                .split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toSet()
            dayOfWeek in days
        }
        else -> false
    }
    if (!scheduled) return false

    if (recurrenceEndDate != null) {
        try {
            val endDate = sdf.parse(recurrenceEndDate) ?: return true
            if (parsedDate.after(endDate)) return false
        } catch (_: Exception) { /* ignore, treat as no end date */ }
    }

    val createdCal = Calendar.getInstance().apply { timeInMillis = createdAt }
    val createdDate = Calendar.getInstance().apply {
        set(createdCal.get(Calendar.YEAR), createdCal.get(Calendar.MONTH), createdCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return !cal.before(createdDate)
}
