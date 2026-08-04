package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "timer_templates")
data class TimerTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val focusMinutes: Int,
    val shortBreakMinutes: Int? = null,
    val longBreakMinutes: Int? = null,
    val targetSessions: Int? = null, // null = continuous
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
