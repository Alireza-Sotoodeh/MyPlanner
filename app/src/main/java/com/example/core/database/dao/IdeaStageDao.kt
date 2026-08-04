package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.IdeaStageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IdeaStageDao {
    @Query("SELECT * FROM idea_stages WHERE ideaId = :ideaId ORDER BY orderIndex ASC, id ASC")
    fun getStagesForIdea(ideaId: Long): Flow<List<IdeaStageEntity>>

    @Query("SELECT * FROM idea_stages WHERE ideaId = :ideaId ORDER BY orderIndex ASC, id ASC")
    suspend fun getStagesForIdeaSync(ideaId: Long): List<IdeaStageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStage(stage: IdeaStageEntity): Long

    @Update
    suspend fun updateStage(stage: IdeaStageEntity)

    @Delete
    suspend fun deleteStage(stage: IdeaStageEntity)
}
