package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "mottos")
data class MottoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val author: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
