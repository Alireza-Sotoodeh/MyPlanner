package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.core.database.entity.IdeaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IdeaDao {
    @Query("SELECT * FROM ideas WHERE groupId = :groupId ORDER BY sortOrder ASC, id DESC")
    fun getIdeasForGroup(groupId: Long): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas WHERE groupId IS NULL ORDER BY sortOrder ASC, id DESC")
    fun getUngroupedIdeas(): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas ORDER BY sortOrder ASC, id DESC")
    fun getAllIdeas(): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas ORDER BY sortOrder ASC, id DESC")
    suspend fun getAllIdeasSync(): List<IdeaEntity>

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

    @Query("UPDATE ideas SET groupId = NULL WHERE groupId = :groupId")
    suspend fun ungroupIdeasByGroupId(groupId: Long)

    @Transaction
    suspend fun updateIdeaSortOrders(ideas: List<IdeaEntity>) {
        ideas.forEach { updateIdea(it) }
    }

    @Query("DELETE FROM ideas")
    suspend fun deleteAll()
}
