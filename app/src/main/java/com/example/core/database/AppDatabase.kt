package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.core.database.dao.DayReviewDao
import com.example.core.database.dao.DiaryDao
import com.example.core.database.dao.HabitDao
import com.example.core.database.dao.IdeaDao
import com.example.core.database.dao.IdeaGroupDao
import com.example.core.database.dao.IdeaStageDao
import com.example.core.database.dao.MottoDao
import com.example.core.database.dao.PomodoroSessionDao
import com.example.core.database.dao.ShopItemDao
import com.example.core.database.dao.SleepLogDao
import com.example.core.database.dao.TaskDao
import com.example.core.database.dao.TodoDao
import com.example.core.database.entity.DayReviewEntity
import com.example.core.database.entity.DiaryEntryEntity
import com.example.core.database.entity.HabitEntity
import com.example.core.database.entity.HabitLogEntity
import com.example.core.database.entity.IdeaEntity
import com.example.core.database.entity.IdeaGroupEntity
import com.example.core.database.entity.IdeaStageEntity
import com.example.core.database.entity.MottoEntity
import com.example.core.database.entity.PomodoroSessionEntity
import com.example.core.database.entity.ShopItemEntity
import com.example.core.database.entity.SleepLogEntity
import com.example.core.database.entity.TaskEntity
import com.example.core.database.entity.TodoEntity

@Database(
    entities = [
        TaskEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        SleepLogEntity::class,
        PomodoroSessionEntity::class,
        IdeaGroupEntity::class,
        IdeaEntity::class,
        IdeaStageEntity::class,
        TodoEntity::class,
        DiaryEntryEntity::class,
        ShopItemEntity::class,
        MottoEntity::class,
        DayReviewEntity::class
    ],
    version = 13,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun sleepLogDao(): SleepLogDao
    abstract fun pomodoroSessionDao(): PomodoroSessionDao
    abstract fun ideaGroupDao(): IdeaGroupDao
    abstract fun ideaDao(): IdeaDao
    abstract fun ideaStageDao(): IdeaStageDao
    abstract fun todoDao(): TodoDao
    abstract fun diaryDao(): DiaryDao
    abstract fun shopItemDao(): ShopItemDao
    abstract fun mottoDao(): MottoDao
    abstract fun dayReviewDao(): DayReviewDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bulletcoach_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
