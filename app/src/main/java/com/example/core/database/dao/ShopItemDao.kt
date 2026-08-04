package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.ShopItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopItemDao {
    @Query("SELECT * FROM shop_items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<ShopItemEntity>>

    @Query("SELECT * FROM shop_items ORDER BY createdAt DESC")
    suspend fun getAllItemsSync(): List<ShopItemEntity>

    @Query("SELECT * FROM shop_items WHERE isPurchased = 0 ORDER BY createdAt DESC")
    fun getUnpurchasedItems(): Flow<List<ShopItemEntity>>

    @Query("SELECT * FROM shop_items WHERE isPurchased = 1 ORDER BY createdAt DESC")
    fun getPurchasedItems(): Flow<List<ShopItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShopItemEntity): Long

    @Update
    suspend fun updateItem(item: ShopItemEntity)

    @Delete
    suspend fun deleteItem(item: ShopItemEntity)

    @Query("DELETE FROM shop_items")
    suspend fun deleteAll()
}
