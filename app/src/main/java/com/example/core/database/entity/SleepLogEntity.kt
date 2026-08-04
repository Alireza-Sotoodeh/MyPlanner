package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_logs")
data class SleepLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // "yyyy-MM-dd"
    val hoursSlept: Float,
    val sleepQuality: Int, // 1 to 5 rating
    val sleepTime: String = "", // e.g. "23:00"
    val wakeTime: String = "", // e.g. "07:30"
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
