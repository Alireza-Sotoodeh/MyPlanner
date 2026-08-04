package com.example.core.repository

import com.example.core.database.dao.IdeaDao
import com.example.core.database.dao.IdeaGroupDao
import com.example.core.database.dao.IdeaStageDao
import com.example.core.database.entity.IdeaEntity
import com.example.core.database.entity.IdeaGroupEntity
import com.example.core.database.entity.IdeaStageEntity
import kotlinx.coroutines.flow.Flow

class IdeaRepository(
    private val ideaGroupDao: IdeaGroupDao,
    private val ideaDao: IdeaDao,
    private val ideaStageDao: IdeaStageDao
) {
    val allGroups: Flow<List<IdeaGroupEntity>> = ideaGroupDao.getAllGroups()

    suspend fun insertGroup(group: IdeaGroupEntity): Long = ideaGroupDao.insertGroup(group)

    suspend fun updateGroup(group: IdeaGroupEntity) = ideaGroupDao.updateGroup(group)

    suspend fun deleteGroup(group: IdeaGroupEntity) = ideaGroupDao.deleteGroup(group)

    fun getIdeasForGroup(groupId: Long): Flow<List<IdeaEntity>> = ideaDao.getIdeasForGroup(groupId)

    fun getUngroupedIdeas(): Flow<List<IdeaEntity>> = ideaDao.getUngroupedIdeas()

    fun getAllIdeas(): Flow<List<IdeaEntity>> = ideaDao.getAllIdeas()

    suspend fun getAllIdeasSync(): List<IdeaEntity> = ideaDao.getAllIdeasSync()

    suspend fun getIdeaById(id: Long): IdeaEntity? = ideaDao.getIdeaById(id)

    suspend fun insertIdea(idea: IdeaEntity): Long = ideaDao.insertIdea(idea)

    suspend fun updateIdea(idea: IdeaEntity) = ideaDao.updateIdea(idea)

    suspend fun deleteIdea(idea: IdeaEntity) = ideaDao.deleteIdea(idea)

    suspend fun moveIdeaToGroup(ideaId: Long, newGroupId: Long?) =
        ideaDao.moveIdeaToGroup(ideaId, newGroupId)

    suspend fun updateIdeaSortOrders(ideas: List<IdeaEntity>) = ideaDao.updateIdeaSortOrders(ideas)

    fun getStagesForIdea(ideaId: Long): Flow<List<IdeaStageEntity>> =
        ideaStageDao.getStagesForIdea(ideaId)

    suspend fun getStagesForIdeaSync(ideaId: Long): List<IdeaStageEntity> =
        ideaStageDao.getStagesForIdeaSync(ideaId)

    suspend fun insertStage(stage: IdeaStageEntity): Long = ideaStageDao.insertStage(stage)

    suspend fun updateStage(stage: IdeaStageEntity) = ideaStageDao.updateStage(stage)

    suspend fun deleteStage(stage: IdeaStageEntity) = ideaStageDao.deleteStage(stage)
}
