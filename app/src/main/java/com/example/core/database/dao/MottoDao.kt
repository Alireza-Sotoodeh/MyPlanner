package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.MottoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MottoDao {
    @Query("SELECT * FROM mottos ORDER BY createdAt DESC")
    fun getAllMottos(): Flow<List<MottoEntity>>

    @Query("SELECT * FROM mottos ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomMotto(): MottoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMotto(motto: MottoEntity): Long

    @Update
    suspend fun updateMotto(motto: MottoEntity)

    @Delete
    suspend fun deleteMotto(motto: MottoEntity)
}
