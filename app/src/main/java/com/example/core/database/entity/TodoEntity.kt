package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "todos",
    foreignKeys = [
        ForeignKey(entity = TaskEntity::class, parentColumns = ["id"], childColumns = ["linkedTaskId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = TodoEntity::class, parentColumns = ["id"], childColumns = ["parentTodoId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [
        Index("linkedTaskId"),
        Index("parentTodoId")
    ]
)
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: String = "Medium",
    val linkedTaskId: Long? = null,
    val parentTodoId: Long? = null,
    val status: String = "PENDING",
    val subtaskImportance: String = "OPTIONAL",
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)
