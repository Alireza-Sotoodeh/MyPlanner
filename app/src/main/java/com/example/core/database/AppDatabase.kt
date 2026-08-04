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
    version = 30,
    exportSchema = true
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

        private val MIGRATION_27_28 = Migration(27, 28) { db ->
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_learn_items_status ON learn_items(status)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_learn_items_schedule_mode ON learn_items(scheduleMode)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_learn_items_sort_order ON learn_items(sortOrder)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_learn_sections_status ON learn_sections(status)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_learn_sections_study_task ON learn_sections(studyTaskId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_learn_sections_review_task ON learn_sections(reviewTaskId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_learn_sections_next_review ON learn_sections(nextReviewDate)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_learn_sections_item_status ON learn_sections(learnItemId, status)")
        }

        private val MIGRATION_28_29 = Migration(28, 29) { db ->
            db.execSQL("PRAGMA foreign_keys=OFF")

            // 1. Tasks — add FKs: parentTaskId→tasks, linkedTodoId→todos, linkedIdeaId→ideas, linkedLearnSectionId→learn_sections
            db.execSQL("CREATE TABLE IF NOT EXISTS `tasks_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `date` TEXT NOT NULL, `status` TEXT NOT NULL, `type` TEXT NOT NULL, `durationMinutes` INTEGER NOT NULL, `pomodorosCompleted` INTEGER NOT NULL, `priority` INTEGER NOT NULL, `label` TEXT NOT NULL, `labelColor` INTEGER, `recurrenceMode` TEXT NOT NULL, `recurrenceInterval` INTEGER NOT NULL, `recurrenceDaysOfWeek` TEXT NOT NULL, `recurrenceEndDate` TEXT, `subtaskImportance` TEXT NOT NULL, `eventTime` TEXT, `notifyNightBefore` INTEGER NOT NULL, `reminderMinutesBefore` INTEGER, `notes` TEXT NOT NULL, `priorityLevel` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `parentTaskId` INTEGER, `targetSessions` INTEGER, `breakMinutes` INTEGER, `linkedTodoId` INTEGER, `linkedIdeaId` INTEGER, `linkedLearnSectionId` INTEGER, `postponed` INTEGER NOT NULL, FOREIGN KEY(`parentTaskId`) REFERENCES `tasks`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL, FOREIGN KEY(`linkedTodoId`) REFERENCES `todos`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL, FOREIGN KEY(`linkedIdeaId`) REFERENCES `ideas`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL, FOREIGN KEY(`linkedLearnSectionId`) REFERENCES `learn_sections`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL)")
            db.execSQL("INSERT INTO tasks_new SELECT * FROM tasks")
            db.execSQL("DROP TABLE tasks")
            db.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_parentTaskId ON tasks(parentTaskId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_linkedTodoId ON tasks(linkedTodoId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_linkedIdeaId ON tasks(linkedIdeaId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_linkedLearnSectionId ON tasks(linkedLearnSectionId)")

            // 2. Todos — add FKs: linkedTaskId→tasks, parentTodoId→todos
            db.execSQL("CREATE TABLE IF NOT EXISTS `todos_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `priority` TEXT NOT NULL, `linkedTaskId` INTEGER, `parentTodoId` INTEGER, `status` TEXT NOT NULL, `subtaskImportance` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL, FOREIGN KEY(`linkedTaskId`) REFERENCES `tasks`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL, FOREIGN KEY(`parentTodoId`) REFERENCES `todos`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL)")
            db.execSQL("INSERT INTO todos_new SELECT * FROM todos")
            db.execSQL("DROP TABLE todos")
            db.execSQL("ALTER TABLE todos_new RENAME TO todos")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_todos_linkedTaskId ON todos(linkedTaskId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_todos_parentTodoId ON todos(parentTodoId)")

            // 3. Ideas — add FK: linkedTaskId→tasks
            db.execSQL("CREATE TABLE IF NOT EXISTS `ideas_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `groupId` INTEGER, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL, `priority` TEXT NOT NULL, `linkedTaskId` INTEGER, FOREIGN KEY(`groupId`) REFERENCES `idea_groups`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`linkedTaskId`) REFERENCES `tasks`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL)")
            db.execSQL("INSERT INTO ideas_new SELECT * FROM ideas")
            db.execSQL("DROP TABLE ideas")
            db.execSQL("ALTER TABLE ideas_new RENAME TO ideas")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_ideas_groupId ON ideas(groupId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_ideas_linkedTaskId ON ideas(linkedTaskId)")

            // 4. Timer sessions — add FK: taskId→tasks
            db.execSQL("CREATE TABLE IF NOT EXISTS `timer_sessions_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT NOT NULL, `taskId` INTEGER, `label` TEXT NOT NULL, `durationSeconds` INTEGER NOT NULL, `date` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `note` TEXT NOT NULL, `templateName` TEXT, FOREIGN KEY(`taskId`) REFERENCES `tasks`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL)")
            db.execSQL("INSERT INTO timer_sessions_new SELECT * FROM timer_sessions")
            db.execSQL("DROP TABLE timer_sessions")
            db.execSQL("ALTER TABLE timer_sessions_new RENAME TO timer_sessions")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_timer_sessions_taskId ON timer_sessions(taskId)")

            // 5. Habit logs — add FK: habitId→habits, unique index on (habitId, date)
            db.execSQL("CREATE TABLE IF NOT EXISTS `habit_logs_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `habitId` INTEGER NOT NULL, `date` TEXT NOT NULL, `value` REAL NOT NULL, `notes` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
            db.execSQL("INSERT INTO habit_logs_new SELECT * FROM habit_logs")
            db.execSQL("DROP TABLE habit_logs")
            db.execSQL("ALTER TABLE habit_logs_new RENAME TO habit_logs")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_habit_logs_habitId_date ON habit_logs(habitId, date)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_habit_logs_habitId ON habit_logs(habitId)")

            // 6. Learn sections — add FKs: studyTaskId→tasks, reviewTaskId→tasks
            db.execSQL("CREATE TABLE IF NOT EXISTS `learn_sections_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `learnItemId` INTEGER NOT NULL, `orderIndex` INTEGER NOT NULL, `title` TEXT NOT NULL, `amount` INTEGER NOT NULL, `status` TEXT NOT NULL, `studyTaskId` INTEGER, `reviewTaskId` INTEGER, `reviewStage` INTEGER NOT NULL, `lastReviewDate` TEXT, `nextReviewDate` TEXT, FOREIGN KEY(`learnItemId`) REFERENCES `learn_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`studyTaskId`) REFERENCES `tasks`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL, FOREIGN KEY(`reviewTaskId`) REFERENCES `tasks`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL)")
            db.execSQL("INSERT INTO learn_sections_new SELECT * FROM learn_sections")
            db.execSQL("DROP TABLE learn_sections")
            db.execSQL("ALTER TABLE learn_sections_new RENAME TO learn_sections")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_learn_sections_learnItemId ON learn_sections(learnItemId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_learn_sections_status ON learn_sections(status)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_learn_sections_studyTaskId ON learn_sections(studyTaskId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_learn_sections_reviewTaskId ON learn_sections(reviewTaskId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_learn_sections_nextReviewDate ON learn_sections(nextReviewDate)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_learn_sections_learnItemId_status ON learn_sections(learnItemId, status)")

            // 7. Idea stages — add unique composite index (no FK change)
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_idea_stages_ideaId_orderIndex ON idea_stages(ideaId, orderIndex)")

            db.execSQL("PRAGMA foreign_keys=ON")
        }

        private val MIGRATION_29_30 = Migration(29, 30) { db ->
            db.execSQL("ALTER TABLE tasks ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE tasks ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE habits ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE habits ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE habit_logs ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE habit_logs ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE sleep_logs ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE sleep_logs ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE timer_sessions ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE timer_sessions ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE timer_templates ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE timer_templates ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE idea_groups ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE idea_groups ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE ideas ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE ideas ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE idea_stages ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE idea_stages ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE todos ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE todos ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE diary_entries ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE shop_items ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE shop_items ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE mottos ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE mottos ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE day_reviews ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE day_reviews ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE learn_groups ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE learn_groups ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE learn_items ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE learn_items ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE learn_sections ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE learn_sections ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bulletcoach_database"
                )
                    .addMigrations(MIGRATION_16_17, MIGRATION_17_18, MIGRATION_23_24, MIGRATION_24_25, MIGRATION_25_26, MIGRATION_26_27, MIGRATION_27_28, MIGRATION_28_29, MIGRATION_29_30)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
