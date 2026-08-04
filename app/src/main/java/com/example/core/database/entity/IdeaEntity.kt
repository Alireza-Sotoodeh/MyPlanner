package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ideas",
    foreignKeys = [
        ForeignKey(
            entity = IdeaGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["linkedTaskId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["groupId"]),
        Index("linkedTaskId")
    ]
)
data class IdeaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long? = null,
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0,
    val priority: String = "Medium",
    val linkedTaskId: Long? = null
)
