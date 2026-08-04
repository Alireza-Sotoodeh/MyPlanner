package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "learn_items",
    foreignKeys = [ForeignKey(
        entity = LearnGroupEntity::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["groupId"])]
)
data class LearnItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String, // BOOK or COURSE
    val totalSections: Int,
    val unit: String = "PAGES", // PAGES or MINUTES
    val totalAmount: Int = 0,
    val sectionsPerDay: Int = 1,
    val groupId: Long? = null,
    val status: String = "NOT_STARTED", // NOT_STARTED, ACTIVE, COMPLETED, ARCHIVED
    val priorityLevel: String = "Medium", // Low, Medium, High
    val createdAt: Long = System.currentTimeMillis()
)
