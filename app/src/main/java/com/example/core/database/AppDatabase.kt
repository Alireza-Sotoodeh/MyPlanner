package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.core.database.dao.DayReviewDao
import com.example.core.database.dao.DiaryDao
import com.example.core.database.dao.HabitDao
import com.example.core.database.dao.IdeaDao
import com.example.core.database.dao.IdeaGroupDao
import com.example.core.database.dao.IdeaStageDao
import com.example.core.database.dao.MottoDao
import com.example.core.database.dao.TimerSessionDao
import com.example.core.database.dao.TimerTemplateDao
import com.example.core.database.dao.ShopItemDao
import com.example.core.database.dao.SleepLogDao
import com.example.core.database.dao.LearnDao
import com.example.core.database.dao.LearnGroupDao
import com.example.core.database.dao.TaskDao
import com.example.core.database.dao.TodoDao
import com.example.core.database.entity.DayReviewEntity
import com.example.core.database.entity.DiaryEntryEntity
import com.example.core.database.entity.HabitEntity
import com.example.core.database.entity.HabitLogEntity
import com.example.core.database.entity.IdeaEntity
import com.example.core.database.entity.IdeaGroupEntity
import com.example.core.database.entity.IdeaStageEntity
import com.example.core.database.entity.LearnGroupEntity
import com.example.core.database.entity.LearnItemEntity
import com.example.core.database.entity.LearnSectionEntity
import com.example.core.database.entity.MottoEntity
import com.example.core.database.entity.TimerSessionEntity
import com.example.core.database.entity.TimerTemplateEntity
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
        TimerSessionEntity::class,
        TimerTemplateEntity::class,
        IdeaGroupEntity::class,
        IdeaEntity::class,
        IdeaStageEntity::class,
        TodoEntity::class,
        DiaryEntryEntity::class,
        ShopItemEntity::class,
        MottoEntity::class,
        DayReviewEntity::class,
        LearnGroupEntity::class,
        LearnItemEntity::class,
        LearnSectionEntity::class
    ],
    version = 27,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun sleepLogDao(): SleepLogDao
    abstract fun timerSessionDao(): TimerSessionDao
    abstract fun timerTemplateDao(): TimerTemplateDao
    abstract fun ideaGroupDao(): IdeaGroupDao
    abstract fun ideaDao(): IdeaDao
    abstract fun ideaStageDao(): IdeaStageDao
    abstract fun todoDao(): TodoDao
    abstract fun diaryDao(): DiaryDao
    abstract fun shopItemDao(): ShopItemDao
    abstract fun mottoDao(): MottoDao
    abstract fun dayReviewDao(): DayReviewDao
    abstract fun learnDao(): LearnDao
    abstract fun learnGroupDao(): LearnGroupDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_16_17 = Migration(16, 17) { db ->
            db.execSQL("ALTER TABLE tasks ADD COLUMN postponed INTEGER NOT NULL DEFAULT 0")
        }

        private val MIGRATION_17_18 = Migration(17, 18) { db ->
            db.execSQL("CREATE TABLE IF NOT EXISTS `timer_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT NOT NULL, `taskId` INTEGER, `label` TEXT NOT NULL DEFAULT '', `durationSeconds` INTEGER NOT NULL, `date` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `note` TEXT NOT NULL DEFAULT '', `templateName` TEXT)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `timer_templates` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `focusMinutes` INTEGER NOT NULL, `shortBreakMinutes` INTEGER, `longBreakMinutes` INTEGER, `targetSessions` INTEGER)")
            db.execSQL("INSERT INTO timer_sessions (type, taskId, label, durationSeconds, date, timestamp, note, templateName) SELECT 'POMODORO', taskId, '', durationMinutes * 60, date, timestamp, '', NULL FROM pomodoro_sessions")
            db.execSQL("DROP TABLE IF EXISTS pomodoro_sessions")
        }

        private val MIGRATION_23_24 = Migration(23, 24) { db ->
            db.execSQL("ALTER TABLE learn_items ADD COLUMN priorityLevel TEXT NOT NULL DEFAULT 'Medium'")
        }

        private val MIGRATION_24_25 = Migration(24, 25) { db ->
            db.execSQL("CREATE TABLE IF NOT EXISTS `learn_groups` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `color` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL DEFAULT 0)")
            db.execSQL("ALTER TABLE learn_items ADD COLUMN groupId INTEGER REFERENCES learn_groups(id) ON DELETE CASCADE")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_learn_items_groupId ON learn_items(groupId)")
        }

        private val MIGRATION_25_26 = Migration(25, 26) { db ->
            db.execSQL("ALTER TABLE learn_items ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
        }

        private val MIGRATION_26_27 = Migration(26, 27) { db ->
            db.execSQL("ALTER TABLE learn_items ADD COLUMN scheduleMode TEXT NOT NULL DEFAULT 'CONTINUOUS'")
            db.execSQL("ALTER TABLE learn_items ADD COLUMN scheduleDaysOfWeek TEXT NOT NULL DEFAULT ''")
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bulletcoach_database"
                )
                    .addMigrations(MIGRATION_16_17, MIGRATION_17_18, MIGRATION_23_24, MIGRATION_24_25, MIGRATION_25_26, MIGRATION_26_27)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
