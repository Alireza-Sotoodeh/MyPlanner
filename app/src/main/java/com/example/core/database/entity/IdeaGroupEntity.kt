package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "idea_groups")
data class IdeaGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Long,
    val sortOrder: Int = 0,
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
