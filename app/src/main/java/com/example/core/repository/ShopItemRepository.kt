package com.example.core.repository

import com.example.core.database.dao.ShopItemDao
import com.example.core.database.entity.ShopItemEntity
import kotlinx.coroutines.flow.Flow

class ShopItemRepository(private val shopItemDao: ShopItemDao) {
    val allItems: Flow<List<ShopItemEntity>> = shopItemDao.getAllItems()

    val unpurchasedItems: Flow<List<ShopItemEntity>> = shopItemDao.getUnpurchasedItems()

    val purchasedItems: Flow<List<ShopItemEntity>> = shopItemDao.getPurchasedItems()

    suspend fun insertItem(item: ShopItemEntity): Long = shopItemDao.insertItem(item)

    suspend fun updateItem(item: ShopItemEntity) = shopItemDao.updateItem(item)

    suspend fun deleteItem(item: ShopItemEntity) = shopItemDao.deleteItem(item)
}
