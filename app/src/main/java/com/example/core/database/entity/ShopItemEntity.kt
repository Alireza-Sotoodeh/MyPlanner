package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "shop_items")
data class ShopItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: Int = 1,
    val price: Float? = null,
    val notes: String = "",
    val isPurchased: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
