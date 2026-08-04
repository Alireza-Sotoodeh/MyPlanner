package com.example.core.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import java.io.IOException
import androidx.core.app.NotificationCompat
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
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "BackupWorker"
    }

    private val contentResolver: ContentResolver get() = applicationContext.contentResolver

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val prefs = applicationContext.getSharedPreferences("bulletcoach_prefs", Context.MODE_PRIVATE)
        val uriStr = prefs.getString("backup_location_uri", null)

        if (uriStr == null) {
            Log.w(TAG, "No backup location set, notifying user")
            notifyNoLocation()
            return@withContext Result.success()
        }

        val rootUri = try {
            Uri.parse(uriStr)
        } catch (_: Exception) { null }

        if (rootUri == null || !documentExists(rootUri)) {
            Log.w(TAG, "Backup location unavailable, notifying user")
            notifyNoLocation()
            prefs.edit().remove("backup_location_uri").apply()
            return@withContext Result.success()
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

            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val adapterCache = HashMap<Class<*>, JsonAdapter<*>>()
            @Suppress("UNCHECKED_CAST")
            fun <T : Any> listAdapter(clazz: Class<T>): JsonAdapter<List<T>> {
                return adapterCache.getOrPut(clazz) {
                    val type = Types.newParameterizedType(List::class.java, clazz)
                    moshi.adapter<List<T>>(type).indent("  ")
                } as JsonAdapter<List<T>>
            }

            fun writeEntityFile(parentUri: Uri, name: String, json: String) {
                val existing = findChildUri(parentUri, name)
                if (existing != null) deleteDocument(existing)
                val createdUri = DocumentsContract.createDocument(
                    contentResolver, parentUri, "application/json", name.removeSuffix(".json")
                ) ?: throw IOException("Failed to create document for $name")
                contentResolver.openOutputStream(createdUri)?.use {
                    it.write(json.toByteArray(Charsets.UTF_8))
                } ?: throw IOException("Failed to open stream for $name")
            }

            fun isTaskInMonth(task: TaskEntity, month: String): Boolean {
                val d = task.date
                return when {
                    d.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$")) -> d.startsWith(month)
                    d.matches(Regex("^\\d{4}-\\d{2}$")) -> d == month
                    d.matches(Regex("^\\d{4}-W\\d{2}$")) -> {
                        try {
                            val cal = Calendar.getInstance()
                            cal.set(Calendar.YEAR, d.substring(0, 4).toInt())
                            cal.set(Calendar.WEEK_OF_YEAR, d.substring(6).toInt())
                            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                            SimpleDateFormat("yyyy-MM", Locale.US).format(cal.time) == month
                        } catch (_: Exception) { false }
                    }
                    else -> false
                }
            }

            fun deleteRecursive(dirUri: Uri) {
                for (child in listChildren(dirUri)) {
                    if (child.mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                        deleteRecursive(child.uri)
                    } else {
                        deleteDocument(child.uri)
                    }
                }
                deleteDocument(dirUri)
            }

            fun rotateOldBackups(root: Uri, maxMonths: Int) {
                val monthDirs = listChildren(root)
                    .filter { it.mimeType == DocumentsContract.Document.MIME_TYPE_DIR && it.name.matches(Regex("""^\d{4}-\d{2}$""")) }
                    .sortedByDescending { it.name }
                if (monthDirs.size > maxMonths) {
                    monthDirs.drop(maxMonths).forEach { deleteRecursive(it.uri) }
                }
            }

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

            val currentMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

            // Filter date-based entities by current month
            val monthTasks = tasksList.filter { isTaskInMonth(it, currentMonth) }
            val monthHabitLogs = habitLogsList.filter { it.date.startsWith(currentMonth) }
            val monthSleepLogs = sleepLogsList.filter { it.date.startsWith(currentMonth) }
            val monthDiaryEntries = diaryEntriesList.filter { it.date.startsWith(currentMonth) }
            val monthDayReviews = dayReviewsList.filter { it.date.startsWith(currentMonth) }
            val monthTimerSessions = timerSessionsList.filter { it.date.startsWith(currentMonth) }

            // Get or create _permanent directory
            var permanentDir = findChildUri(rootUri, "_permanent")
            if (permanentDir == null) {
                permanentDir = DocumentsContract.createDocument(
                    contentResolver, rootUri, DocumentsContract.Document.MIME_TYPE_DIR, "_permanent"
                )
            }

            if (permanentDir != null) {
                writeEntityFile(permanentDir, "HabitEntity.json", listAdapter(HabitEntity::class.java).toJson(habitsList))
                writeEntityFile(permanentDir, "TodoEntity.json", listAdapter(TodoEntity::class.java).toJson(todosList))
                writeEntityFile(permanentDir, "MottoEntity.json", listAdapter(MottoEntity::class.java).toJson(mottosList))
                writeEntityFile(permanentDir, "ShopItemEntity.json", listAdapter(ShopItemEntity::class.java).toJson(shopItemsList))
                writeEntityFile(permanentDir, "IdeaGroupEntity.json", listAdapter(IdeaGroupEntity::class.java).toJson(ideaGroupsList))
                writeEntityFile(permanentDir, "IdeaEntity.json", listAdapter(IdeaEntity::class.java).toJson(ideasList))
                writeEntityFile(permanentDir, "IdeaStageEntity.json", listAdapter(IdeaStageEntity::class.java).toJson(ideaStagesList))
                writeEntityFile(permanentDir, "LearnGroupEntity.json", listAdapter(LearnGroupEntity::class.java).toJson(learnGroupsList))
                writeEntityFile(permanentDir, "LearnItemEntity.json", listAdapter(LearnItemEntity::class.java).toJson(learnItemsList))
                writeEntityFile(permanentDir, "LearnSectionEntity.json", listAdapter(LearnSectionEntity::class.java).toJson(learnSectionsList))
                writeEntityFile(permanentDir, "TimerTemplateEntity.json", listAdapter(TimerTemplateEntity::class.java).toJson(timerTemplatesList))
            }

            // Get or create month directory
            var monthDir = findChildUri(rootUri, currentMonth)
            if (monthDir == null) {
                monthDir = DocumentsContract.createDocument(
                    contentResolver, rootUri, DocumentsContract.Document.MIME_TYPE_DIR, currentMonth
                )
            }

            if (monthDir != null) {
                writeEntityFile(monthDir, "TaskEntity.json", listAdapter(TaskEntity::class.java).toJson(monthTasks))
                writeEntityFile(monthDir, "HabitLogEntity.json", listAdapter(HabitLogEntity::class.java).toJson(monthHabitLogs))
                writeEntityFile(monthDir, "SleepLogEntity.json", listAdapter(SleepLogEntity::class.java).toJson(monthSleepLogs))
                writeEntityFile(monthDir, "DiaryEntryEntity.json", listAdapter(DiaryEntryEntity::class.java).toJson(monthDiaryEntries))
                writeEntityFile(monthDir, "DayReviewEntity.json", listAdapter(DayReviewEntity::class.java).toJson(monthDayReviews))
                writeEntityFile(monthDir, "TimerSessionEntity.json", listAdapter(TimerSessionEntity::class.java).toJson(monthTimerSessions))
            }

            // Run rotation
            val maxMonths = prefs.getInt("backup_max_months", 5)
            rotateOldBackups(rootUri, maxMonths)

            prefs.edit().putLong("drive_last_sync_at", System.currentTimeMillis()).apply()
            Log.d(TAG, "Auto-backup successful")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Auto-backup failed", e)
            notifyFailure("Error: ${e.message ?: "Unknown error"}")
            Result.failure()
        }
    }

    private fun notifyNoLocation() {
        val prefs = applicationContext.getSharedPreferences("bulletcoach_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("backup_failure_notify", true)) return

        val channelId = "backup_failures"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Backup", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Auto-backup skipped")
            .setContentText("No backup location selected — open Settings to choose one")
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send backup notification", e)
        }
    }

    private fun notifyFailure(message: String) {
        val prefs = applicationContext.getSharedPreferences("bulletcoach_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("backup_failure_notify", true)) return

        val channelId = "backup_failures"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Backup", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Auto-backup failed")
            .setContentText(message)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send backup failure notification", e)
        }
    }

    // DocumentFile replacement helpers using DocumentsContract directly

    private fun documentExists(uri: Uri): Boolean {
        return try {
            val docId = DocumentsContract.getDocumentId(uri)
            val docUri = DocumentsContract.buildDocumentUriUsingTree(
                DocumentsContract.buildTreeDocumentUri(uri.authority ?: return false, docId),
                docId
            )
            val projection = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            contentResolver.query(docUri, projection, null, null, null)?.use { it.moveToFirst() } ?: false
        } catch (_: Exception) { false }
    }

    private fun findChildUri(parentUri: Uri, name: String): Uri? {
        val parentId = DocumentsContract.getTreeDocumentId(parentUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(parentUri, parentId)
        val projection = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME)
        contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val docId = cursor.getString(0)
                val displayName = cursor.getString(1)
                if (displayName == name) {
                    return DocumentsContract.buildDocumentUriUsingTree(parentUri, docId)
                }
            }
        }
        return null
    }

    private data class ChildInfo(val uri: Uri, val name: String, val mimeType: String)

    private fun listChildren(parentUri: Uri): List<ChildInfo> {
        val parentId = DocumentsContract.getTreeDocumentId(parentUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(parentUri, parentId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        )
        val result = mutableListOf<ChildInfo>()
        contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val docId = cursor.getString(0)
                val name = cursor.getString(1) ?: continue
                val mimeType = cursor.getString(2) ?: ""
                result.add(ChildInfo(DocumentsContract.buildDocumentUriUsingTree(parentUri, docId), name, mimeType))
            }
        }
        return result
    }

    private fun deleteDocument(uri: Uri) {
        try {
            DocumentsContract.deleteDocument(contentResolver, uri)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete document: $uri", e)
        }
    }
}
