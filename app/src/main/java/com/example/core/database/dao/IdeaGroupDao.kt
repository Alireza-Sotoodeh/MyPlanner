package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.IdeaGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IdeaGroupDao {
    @Query("SELECT * FROM idea_groups ORDER BY sortOrder ASC, id ASC")
    fun getAllGroups(): Flow<List<IdeaGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: IdeaGroupEntity): Long

    @Update
    suspend fun updateGroup(group: IdeaGroupEntity)

    @Delete
    suspend fun deleteGroup(group: IdeaGroupEntity)
}
