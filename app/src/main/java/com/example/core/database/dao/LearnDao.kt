package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.LearnItemEntity
import com.example.core.database.entity.LearnSectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearnDao {
    @Query("SELECT * FROM learn_items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<LearnItemEntity>>

    @Query("SELECT * FROM learn_items ORDER BY createdAt DESC")
    suspend fun getAllItemsSync(): List<LearnItemEntity>

    @Query("SELECT * FROM learn_items WHERE id = :id")
    suspend fun getItemById(id: Long): LearnItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LearnItemEntity): Long

    @Update
    suspend fun updateItem(item: LearnItemEntity)

    @Delete
    suspend fun deleteItem(item: LearnItemEntity)

    @Query("DELETE FROM learn_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("SELECT * FROM learn_items WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun getItemsForGroup(groupId: Long): Flow<List<LearnItemEntity>>

    @Query("SELECT * FROM learn_items WHERE groupId IS NULL ORDER BY createdAt DESC")
    fun getUngroupedItems(): Flow<List<LearnItemEntity>>

    @Query("UPDATE learn_items SET groupId = :newGroupId WHERE id = :itemId")
    suspend fun moveItemToGroup(itemId: Long, newGroupId: Long?)

    @Query("SELECT * FROM learn_sections WHERE learnItemId = :itemId ORDER BY orderIndex ASC, id ASC")
    fun getSectionsForItem(itemId: Long): Flow<List<LearnSectionEntity>>

    @Query("SELECT * FROM learn_sections WHERE learnItemId = :itemId ORDER BY orderIndex ASC, id ASC")
    suspend fun getSectionsForItemSync(itemId: Long): List<LearnSectionEntity>

    @Query("SELECT * FROM learn_sections WHERE id = :id")
    suspend fun getSectionById(id: Long): LearnSectionEntity?

    @Query("SELECT * FROM learn_sections WHERE studyTaskId = :taskId")
    suspend fun getSectionByStudyTaskId(taskId: Long): LearnSectionEntity?

    @Query("SELECT * FROM learn_sections WHERE reviewTaskId = :taskId")
    suspend fun getSectionByReviewTaskId(taskId: Long): LearnSectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: LearnSectionEntity): Long

    @Update
    suspend fun updateSection(section: LearnSectionEntity)

    @Delete
    suspend fun deleteSection(section: LearnSectionEntity)
}
