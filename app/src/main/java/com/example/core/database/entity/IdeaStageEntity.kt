package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "idea_stages",
    foreignKeys = [ForeignKey(
        entity = IdeaEntity::class,
        parentColumns = ["id"],
        childColumns = ["ideaId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["ideaId"]),
        Index(value = ["ideaId", "orderIndex"], unique = true)
    ]
)
data class IdeaStageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ideaId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0,
    val importance: String = "OPTIONAL"
)
