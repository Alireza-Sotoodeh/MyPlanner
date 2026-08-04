package com.example.core.repository

import com.example.core.database.dao.LearnDao
import com.example.core.database.dao.LearnGroupDao
import com.example.core.database.entity.LearnGroupEntity
import com.example.core.database.entity.LearnItemEntity
import com.example.core.database.entity.LearnSectionEntity
import kotlinx.coroutines.flow.Flow

class LearnRepository(
    private val learnDao: LearnDao,
    private val learnGroupDao: LearnGroupDao
) {
    val allGroups: Flow<List<LearnGroupEntity>> = learnGroupDao.getAllGroups()

    suspend fun getAllGroupsSync(): List<LearnGroupEntity> = learnGroupDao.getAllGroupsSync()

    suspend fun insertGroup(group: LearnGroupEntity): Long = learnGroupDao.insertGroup(group)

    suspend fun updateGroup(group: LearnGroupEntity) = learnGroupDao.updateGroup(group)

    suspend fun deleteGroup(group: LearnGroupEntity) = learnGroupDao.deleteGroup(group)

    fun getAllItems(): Flow<List<LearnItemEntity>> = learnDao.getAllItems()

    suspend fun getAllItemsSync(): List<LearnItemEntity> = learnDao.getAllItemsSync()

    suspend fun getItemById(id: Long): LearnItemEntity? = learnDao.getItemById(id)

    suspend fun insertItem(item: LearnItemEntity): Long = learnDao.insertItem(item)

    suspend fun updateItem(item: LearnItemEntity) = learnDao.updateItem(item)

    suspend fun deleteItem(item: LearnItemEntity) = learnDao.deleteItem(item)

    suspend fun deleteItemById(id: Long) = learnDao.deleteItemById(id)

    suspend fun moveItemToGroup(itemId: Long, newGroupId: Long?) =
        learnDao.moveItemToGroup(itemId, newGroupId)

    fun getItemsForGroup(groupId: Long): Flow<List<LearnItemEntity>> =
        learnDao.getItemsForGroup(groupId)

    fun getUngroupedItems(): Flow<List<LearnItemEntity>> =
        learnDao.getUngroupedItems()

    fun getSectionsForItem(itemId: Long): Flow<List<LearnSectionEntity>> =
        learnDao.getSectionsForItem(itemId)

    suspend fun getSectionsForItemSync(itemId: Long): List<LearnSectionEntity> =
        learnDao.getSectionsForItemSync(itemId)

    suspend fun getSectionById(id: Long): LearnSectionEntity? = learnDao.getSectionById(id)

    suspend fun getSectionByStudyTaskId(taskId: Long): LearnSectionEntity? =
        learnDao.getSectionByStudyTaskId(taskId)

    suspend fun getSectionByReviewTaskId(taskId: Long): LearnSectionEntity? =
        learnDao.getSectionByReviewTaskId(taskId)

    suspend fun insertSection(section: LearnSectionEntity): Long = learnDao.insertSection(section)

    suspend fun updateSection(section: LearnSectionEntity) = learnDao.updateSection(section)

    suspend fun deleteSection(section: LearnSectionEntity) = learnDao.deleteSection(section)

    suspend fun ungroupItemsByGroupId(groupId: Long) = learnDao.ungroupItemsByGroupId(groupId)
}
