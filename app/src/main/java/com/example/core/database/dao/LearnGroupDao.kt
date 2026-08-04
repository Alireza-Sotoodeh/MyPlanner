package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.LearnGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearnGroupDao {
    @Query("SELECT * FROM learn_groups ORDER BY sortOrder ASC, id ASC")
    fun getAllGroups(): Flow<List<LearnGroupEntity>>

    @Query("SELECT * FROM learn_groups ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllGroupsSync(): List<LearnGroupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: LearnGroupEntity): Long

    @Update
    suspend fun updateGroup(group: LearnGroupEntity)

    @Delete
    suspend fun deleteGroup(group: LearnGroupEntity)
}
