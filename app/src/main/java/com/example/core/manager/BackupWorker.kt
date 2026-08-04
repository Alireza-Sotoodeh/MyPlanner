package com.example.core.manager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.core.database.AppDatabase
import com.example.core.repository.DayReviewRepository
import com.example.core.repository.DiaryRepository
import com.example.core.repository.HabitRepository
import com.example.core.repository.IdeaRepository
import com.example.core.repository.LearnRepository
import com.example.core.repository.MottoRepository
import com.example.core.repository.ShopItemRepository
import com.example.core.repository.SleepLogRepository
import com.example.core.repository.TaskRepository
import com.example.core.repository.TodoRepository
import com.example.ui.viewmodel.BulletCoachBackup
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "BackupWorker"
    }

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("bulletcoach_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("google_drive_connected", false)) {
            Log.d(TAG, "Drive not connected, skipping auto-backup")
            return Result.success()
        }

        return try {
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

            val tasks = taskRepository.getAllTasks().first()
            val habits = habitRepository.allHabits.first()
            val habitLogs = habitRepository.getAllLogs().first()
            val sleepLogs = sleepLogRepository.allSleepLogs.first()
            val ideaGroups = ideaRepository.getAllGroupsSync()
            val ideas = ideaRepository.getAllIdeasSync()
            val todos = todoRepository.getAllTodosSync()
            val diaryEntries = diaryRepository.getAllEntries().first()
            val shopItems = shopItemRepository.allItems.first()
            val mottos = mottoRepository.allMottos.first()
            val dayReviews = dayReviewRepository.getAllReviews().first()
            val ideaStages = ideas.flatMap { ideaRepository.getStagesForIdeaSync(it.id) }
            val learnGroups = learnRepository.getAllGroupsSync()
            val learnItems = learnRepository.getAllItemsSync()
            val learnSections = learnItems.flatMap { learnRepository.getSectionsForItemSync(it.id) }

            val backupObj = BulletCoachBackup(
                tasks = tasks,
                habits = habits,
                habitLogs = habitLogs,
                sleepLogs = sleepLogs,
                ideaGroups = ideaGroups,
                ideas = ideas,
                ideaStages = ideaStages,
                todos = todos,
                diaryEntries = diaryEntries,
                shopItems = shopItems,
                mottos = mottos,
                dayReviews = dayReviews,
                learnGroups = learnGroups,
                learnItems = learnItems,
                learnSections = learnSections
            )

            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(BulletCoachBackup::class.java)
            val jsonString = adapter.toJson(backupObj)

            val backupFile = File(applicationContext.filesDir, "bulletcoach_backup.json")
            backupFile.writeText(jsonString)

            val data = jsonString.toByteArray(Charsets.UTF_8)
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val filename = "bulletcoach_${dateStr}.json"
            val fileId = DriveManager.uploadBackup(applicationContext, data, filename)

            if (fileId != null) {
                DriveManager.rotateBackups(applicationContext)
                prefs.edit().putLong("drive_last_sync_at", System.currentTimeMillis()).apply()
                Log.d(TAG, "Auto-backup successful")
            } else {
                Log.w(TAG, "Auto-backup: Drive upload failed, saved locally")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Auto-backup failed", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
