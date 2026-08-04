package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.IdeaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IdeaDao {
    @Query("SELECT * FROM ideas WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun getIdeasForGroup(groupId: Long): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas WHERE groupId IS NULL ORDER BY createdAt DESC")
    fun getUngroupedIdeas(): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas ORDER BY createdAt DESC")
    fun getAllIdeas(): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas WHERE id = :id")
    suspend fun getIdeaById(id: Long): IdeaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdea(idea: IdeaEntity): Long

    @Update
    suspend fun updateIdea(idea: IdeaEntity)

    @Delete
    suspend fun deleteIdea(idea: IdeaEntity)

    @Query("UPDATE ideas SET groupId = :newGroupId WHERE id = :ideaId")
    suspend fun moveIdeaToGroup(ideaId: Long, newGroupId: Long?)
}
