package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "learn_sections",
    foreignKeys = [
        ForeignKey(
            entity = LearnItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["learnItemId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["studyTaskId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["reviewTaskId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["learnItemId"]),
        Index(value = ["status"]),
        Index(value = ["studyTaskId"]),
        Index(value = ["reviewTaskId"]),
        Index(value = ["nextReviewDate"]),
        Index(value = ["learnItemId", "status"])
    ]
)
data class LearnSectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val learnItemId: Long,
    val orderIndex: Int,
    val title: String,
    val amount: Int = 0,
    val status: String = "NOT_STARTED", // NOT_STARTED, STUDIED, IN_REVIEW, MASTERED
    val studyTaskId: Long? = null,
    val reviewTaskId: Long? = null,
    val reviewStage: Int = -1,
    val lastReviewDate: String? = null,
    val nextReviewDate: String? = null
)
