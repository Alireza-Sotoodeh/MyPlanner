package com.example.core.manager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.core.database.AppDatabase
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
import com.example.core.database.entity.ShopItemEntity
import com.example.core.database.entity.SleepLogEntity
import com.example.core.database.entity.TaskEntity
import com.example.core.database.entity.TimerSessionEntity
import com.example.core.database.entity.TimerTemplateEntity
import com.example.core.database.entity.TodoEntity
import com.example.core.repository.DayReviewRepository
import com.example.core.repository.DiaryRepository
import com.example.core.repository.HabitRepository
import com.example.core.repository.IdeaRepository
import com.example.core.repository.LearnRepository
import com.example.core.repository.MottoRepository
import com.example.core.repository.ShopItemRepository
import com.example.core.repository.SleepLogRepository
import com.example.core.repository.TaskRepository
import com.example.core.repository.TimerRepository
import com.example.core.repository.TodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class BackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "BackupWorker"
    }

    private val backupFileManager = BackupFileManager(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val rootUri = backupFileManager.getBackupRootDir()
        if (rootUri == null) {
            Log.w(TAG, "No backup location set, notifying user")
            backupFileManager.notifyUser(
                "Auto-backup skipped",
                "No backup location selected — open Settings to choose one"
            )
            return@withContext Result.retry()
        }

if (!backupFileManager.hasWritePermission(rootUri)) {
            Log.w(TAG, "Backup location unavailable, notifying user")
            backupFileManager.notifyUser(
                "Auto-backup skipped",
                "No write permission for backup location — check Settings"
            )
            return@withContext Result.retry()
        }

        try {
            val database = AppDatabase.getDatabase(applicationContext)
            val taskRepository = TaskRepository(database.taskDao())
            val habitRepository = HabitRepository(database.habitDao())
            val sleepLogRepository = SleepLogRepository(database.sleepLogDao())
            val ideaRepository = IdeaRepository(database.ideaGroupDao(), database.ideaDao(), database.ideaStageDao())
            val todoRepository = TodoRepository(database.todoDao())
            val diaryRepository = DiaryRepository(database.diaryDao())
            val shopItemRepository = ShopItemRepository(database.shopItemDao())
            val mottoRepository = MottoRepository(database.mottoDao())
            val dayReviewRepository = DayReviewRepository(database.dayReviewDao())
            val learnRepository = LearnRepository(database.learnDao(), database.learnGroupDao())
            val timerRepository = TimerRepository(database.timerSessionDao(), database.timerTemplateDao())

            val currentMonth = backupFileManager.getCurrentMonth()

            val tasksList = taskRepository.getAllTasks().first()
            val habitsList = habitRepository.allHabits.first()
            val habitLogsList = habitRepository.getAllLogs().first()
            val sleepLogsList = sleepLogRepository.allSleepLogs.first()
            val ideaGroupsList = ideaRepository.getAllGroupsSync()
            val ideasList = ideaRepository.getAllIdeasSync()
            val todosList = todoRepository.getAllTodosSync()
            val diaryEntriesList = diaryRepository.getAllEntries().first()
            val shopItemsList = shopItemRepository.allItems.first()
            val mottosList = mottoRepository.allMottos.first()
            val dayReviewsList = dayReviewRepository.getAllReviews().first()
            val timerSessionsList = timerRepository.getAllSessions().first()
            val timerTemplatesList = timerRepository.getAllTemplates().first()
            val ideaStagesList = ideasList.flatMap { ideaRepository.getStagesForIdeaSync(it.id) }
            val learnGroupsList = learnRepository.getAllGroupsSync()
            val learnItemsList = learnRepository.getAllItemsSync()
            val learnSectionsList = learnItemsList.flatMap { learnRepository.getSectionsForItemSync(it.id) }

            // Filter date-based entities by current month
            val monthTasks = tasksList.filter { backupFileManager.isTaskInMonth(it, currentMonth) }
            val monthHabitLogs = habitLogsList.filter { it.date.startsWith(currentMonth) }
            val monthSleepLogs = sleepLogsList.filter { it.date.startsWith(currentMonth) }
            val monthDiaryEntries = diaryEntriesList.filter { it.date.startsWith(currentMonth) }
            val monthDayReviews = dayReviewsList.filter { it.date.startsWith(currentMonth) }
            val monthTimerSessions = timerSessionsList.filter { it.date.startsWith(currentMonth) }

            // Get or create _permanent directory
            val permanentDir = backupFileManager.getOrCreateDir(rootUri, "_permanent")
                ?: throw IllegalStateException("Failed to create _permanent directory")

            backupFileManager.writeEntityFile(permanentDir, "HabitEntity.json", backupFileManager.toJson(habitsList, HabitEntity::class.java))
            backupFileManager.writeEntityFile(permanentDir, "TodoEntity.json", backupFileManager.toJson(todosList, TodoEntity::class.java))
            backupFileManager.writeEntityFile(permanentDir, "MottoEntity.json", backupFileManager.toJson(mottosList, MottoEntity::class.java))
            backupFileManager.writeEntityFile(permanentDir, "ShopItemEntity.json", backupFileManager.toJson(shopItemsList, ShopItemEntity::class.java))
            backupFileManager.writeEntityFile(permanentDir, "IdeaGroupEntity.json", backupFileManager.toJson(ideaGroupsList, IdeaGroupEntity::class.java))
            backupFileManager.writeEntityFile(permanentDir, "IdeaEntity.json", backupFileManager.toJson(ideasList, IdeaEntity::class.java))
            backupFileManager.writeEntityFile(permanentDir, "IdeaStageEntity.json", backupFileManager.toJson(ideaStagesList, IdeaStageEntity::class.java))
            backupFileManager.writeEntityFile(permanentDir, "LearnGroupEntity.json", backupFileManager.toJson(learnGroupsList, LearnGroupEntity::class.java))
            backupFileManager.writeEntityFile(permanentDir, "LearnItemEntity.json", backupFileManager.toJson(learnItemsList, LearnItemEntity::class.java))
            backupFileManager.writeEntityFile(permanentDir, "LearnSectionEntity.json", backupFileManager.toJson(learnSectionsList, LearnSectionEntity::class.java))
            backupFileManager.writeEntityFile(permanentDir, "TimerTemplateEntity.json", backupFileManager.toJson(timerTemplatesList, TimerTemplateEntity::class.java))

            // Get or create month directory
            val monthDir = backupFileManager.getOrCreateDir(rootUri, currentMonth)
                ?: throw IllegalStateException("Failed to create month directory: $currentMonth")

            backupFileManager.writeEntityFile(monthDir, "TaskEntity.json", backupFileManager.toJson(monthTasks, TaskEntity::class.java))
            backupFileManager.writeEntityFile(monthDir, "HabitLogEntity.json", backupFileManager.toJson(monthHabitLogs, HabitLogEntity::class.java))
            backupFileManager.writeEntityFile(monthDir, "SleepLogEntity.json", backupFileManager.toJson(monthSleepLogs, SleepLogEntity::class.java))
            backupFileManager.writeEntityFile(monthDir, "DiaryEntryEntity.json", backupFileManager.toJson(monthDiaryEntries, DiaryEntryEntity::class.java))
            backupFileManager.writeEntityFile(monthDir, "DayReviewEntity.json", backupFileManager.toJson(monthDayReviews, DayReviewEntity::class.java))
            backupFileManager.writeEntityFile(monthDir, "TimerSessionEntity.json", backupFileManager.toJson(monthTimerSessions, TimerSessionEntity::class.java))

            // Run rotation
            val maxMonths = backupFileManager.getBackupMaxMonths()
            backupFileManager.rotateOldBackups(rootUri, maxMonths)

            backupFileManager.setLastSyncTimestamp(System.currentTimeMillis())
            Log.d(TAG, "Auto-backup successful")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Auto-backup failed", e)
            backupFileManager.notifyUser("Auto-backup failed", "Error: ${e.message ?: "Unknown error"}")
            Result.failure()
        }
    }
}