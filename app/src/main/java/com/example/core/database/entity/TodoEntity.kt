package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: String = "Medium",
    val linkedTaskId: Long? = null,
    val status: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)
