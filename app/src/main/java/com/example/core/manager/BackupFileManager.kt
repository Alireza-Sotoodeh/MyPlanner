package com.example.core.manager

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.core.database.entity.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class BackupFileManager(private val context: Context) {

    companion object {
        private const val TAG = "BackupFileManager"
        private const val PREFS_NAME = "bulletcoach_prefs"
        private const val NOTIFICATION_CHANNEL_ID = "backup_failures"
        private val DATE_FULL_REGEX = Regex("^\\d{4}-\\d{2}-\\d{2}$")
        private val DATE_MONTH_REGEX = Regex("^\\d{4}-\\d{2}$")
        private val DATE_WEEK_REGEX = Regex("^\\d{4}-W\\d{2}$")
        val MONTH_DIR_REGEX = Regex("""^\d{4}-\d{2}$""")
    }

    private val contentResolver: ContentResolver = context.contentResolver
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var useDirectFileAccess = false

    fun setDirectFileAccess(enabled: Boolean) {
        useDirectFileAccess = enabled
    }

    fun isDirectFileAccess(): Boolean = useDirectFileAccess

    fun hasManageExternalStorage(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    private fun toDocumentUri(treeUri: Uri): Uri {
        return if (DocumentsContract.isTreeUri(treeUri)) {
            try {
                val docId = DocumentsContract.getTreeDocumentId(treeUri)
                DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
            } catch (_: Exception) { treeUri }
        } else {
            treeUri
        }
    }

    fun resolveBackupPath(uri: Uri): File? {
        return try {
            val docId = DocumentsContract.getTreeDocumentId(uri)
            val colonIndex = docId.indexOf(':')
            if (colonIndex < 0) return null
            val volume = docId.substring(0, colonIndex)
            val path = docId.substring(colonIndex + 1)
            val basePath = if (volume == "primary") {
                android.os.Environment.getExternalStorageDirectory().absolutePath
            } else {
                "/storage/$volume"
            }
            File(basePath, path)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve backup path from $uri", e)
            null
        }
    }

    private fun uriToFile(uri: Uri): File? {
        if (uri.scheme == "file") return File(uri.path ?: return null)
        return resolveBackupPath(uri)
    }

    private fun fileToUri(file: File): Uri = Uri.fromFile(file)

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val adapterCache = ConcurrentHashMap<Class<*>, JsonAdapter<*>>()

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> listAdapter(clazz: Class<T>): JsonAdapter<List<T>> {
        return adapterCache.getOrPut(clazz) {
            val type = Types.newParameterizedType(List::class.java, clazz)
            moshi.adapter<List<T>>(type).indent("  ")
        } as JsonAdapter<List<T>>
    }

    fun <T : Any> toJson(list: List<T>, clazz: Class<T>): String {
        return listAdapter(clazz).toJson(list)
    }

    fun isTaskInMonth(task: TaskEntity, month: String): Boolean {
        val d = task.date
        return if (d.matches(DATE_FULL_REGEX)) {
            d.startsWith(month)
        } else if (d.matches(DATE_MONTH_REGEX)) {
            d == month
        } else if (d.matches(DATE_WEEK_REGEX)) {
            try {
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, d.substring(0, 4).toInt())
                cal.set(Calendar.WEEK_OF_YEAR, d.substring(6).toInt())
                cal.firstDayOfWeek = Calendar.MONDAY
                cal.minimalDaysInFirstWeek = 4
                SimpleDateFormat("yyyy-MM", Locale.US).format(cal.time) == month
            } catch (_: Exception) { false }
        } else {
            false
        }
    }

    fun hasWritePermission(uri: Uri): Boolean {
        if (useDirectFileAccess) {
            return try {
                val dir = uriToFile(uri)
                dir != null && (dir.exists() || dir.mkdirs()) && dir.canWrite()
            } catch (e: Exception) {
                Log.w(TAG, "hasWritePermission(file) failed for $uri", e)
                false
            }
        }
        return try {
            cleanupTestFiles(uri)
            val docUri = toDocumentUri(uri)
            val testFile = DocumentsContract.createDocument(
                contentResolver, docUri, "application/json", "write_test_p_${System.currentTimeMillis()}"
            )
            if (testFile != null) {
                deleteDocument(testFile)
            }
            testFile != null
        } catch (e: Exception) {
            Log.w(TAG, "hasWritePermission failed for $uri", e)
            false
        }
    }

    private fun cleanupTestFiles(uri: Uri) {
        try {
            for (child in listChildren(uri)) {
                if (child.name.startsWith("write_test_p_")) {
                    deleteDocument(child.uri)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "cleanupTestFiles failed for $uri", e)
        }
    }

    fun writeEntityFile(parentUri: Uri, name: String, json: String) {
        if (useDirectFileAccess) {
            val parentDir = uriToFile(parentUri)
                ?: throw IOException("Failed to resolve path for $parentUri")
            parentDir.mkdirs()
            val tempFile = File(parentDir, "${name}.tmp.${System.currentTimeMillis()}")
            val finalFile = File(parentDir, name)
            try {
                tempFile.writeText(json, Charsets.UTF_8)
                if (finalFile.exists()) finalFile.delete()
                if (!tempFile.renameTo(finalFile)) {
                    throw IOException("Failed to rename temp to final for $name")
                }
            } catch (e: Exception) {
                if (tempFile.exists()) tempFile.delete()
                throw IOException("Failed to write entity file $name", e)
            }
            return
        }
        val tempName = "${name}.tmp.${System.currentTimeMillis()}"
        val docUri = toDocumentUri(parentUri)
        val tempUri = DocumentsContract.createDocument(
            contentResolver, docUri, "application/json", tempName
        ) ?: throw IOException("Failed to create temp document for $name")

        try {
            contentResolver.openOutputStream(tempUri)?.use {
                it.write(json.toByteArray(StandardCharsets.UTF_8))
            } ?: throw IOException("Failed to open output stream for temp $name")

            val existing = findChildUri(parentUri, name)
            if (existing != null) {
                deleteDocument(existing)
            }

            val finalUri = DocumentsContract.renameDocument(contentResolver, tempUri, name)
                ?: throw IOException("Failed to rename temp to final for $name")
        } catch (e: Exception) {
            try { deleteDocument(tempUri) } catch (_: Exception) {}
            throw IOException("Failed to write entity file $name", e)
        }
    }

    @Throws(IOException::class)
    fun readEntityFile(parentUri: Uri?, name: String, clazz: Class<*>): List<Any> {
        if (useDirectFileAccess && parentUri != null && (parentUri.scheme == "file" || DocumentsContract.isTreeUri(parentUri))) {
            val parentFile = uriToFile(parentUri)
            val file = parentFile?.let { File(it, name) }
            if (file == null || !file.exists()) {
                throw IOException("Parent directory or file not found: $name")
            }
            val json = file.readText(Charsets.UTF_8)
            val list = listAdapter(clazz as Class<Any>).fromJson(json)
                ?: throw IOException("Failed to parse JSON for $name: null result")
            if (list.isEmpty()) {
                Log.w(TAG, "Read empty list for $name - this may indicate data loss")
            }
            return list
        }
        val fileUri = parentUri?.let { findChildUri(it, name) }
            ?: throw IOException("Parent directory or file not found: $name")

        val json = contentResolver.openInputStream(fileUri)?.use {
            it.reader(StandardCharsets.UTF_8).readText()
        } ?: throw IOException("Failed to read file: $name")

        val list = listAdapter(clazz as Class<Any>).fromJson(json)
            ?: throw IOException("Failed to parse JSON for $name: null result")

        if (list.isEmpty()) {
            Log.w(TAG, "Read empty list for $name - this may indicate data loss")
        }
        return list
    }

    fun findChildUri(parentUri: Uri, name: String): Uri? {
        if (useDirectFileAccess && (parentUri.scheme == "file" || DocumentsContract.isTreeUri(parentUri))) {
            val parentFile = uriToFile(parentUri) ?: return null
            val child = File(parentFile, name)
            return if (child.exists()) fileToUri(child) else null
        }
        val parentId = DocumentsContract.getTreeDocumentId(parentUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(parentUri, parentId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME
        )
        return contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val docId = cursor.getString(0)
                val displayName = cursor.getString(1)
                if (displayName == name) {
                    return DocumentsContract.buildDocumentUriUsingTree(parentUri, docId)
                }
            }
            null
        }
    }

    data class ChildInfo(val uri: Uri, val name: String, val mimeType: String)

    fun listChildren(parentUri: Uri): List<ChildInfo> {
        if (useDirectFileAccess && (parentUri.scheme == "file" || DocumentsContract.isTreeUri(parentUri))) {
            val parentFile = uriToFile(parentUri) ?: return emptyList()
            return parentFile.listFiles()?.map { f ->
                ChildInfo(
                    uri = fileToUri(f),
                    name = f.name,
                    mimeType = if (f.isDirectory) DocumentsContract.Document.MIME_TYPE_DIR else ""
                )
            }?.toList() ?: emptyList()
        }
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

    fun deleteDocument(uri: Uri) {
        if (useDirectFileAccess && (uri.scheme == "file")) {
            val file = File(uri.path ?: return)
            if (file.exists()) file.delete()
            return
        }
        try {
            DocumentsContract.deleteDocument(contentResolver, uri)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete document: $uri", e)
        }
    }

    fun deleteRecursive(dirUri: Uri) {
        if (useDirectFileAccess && (dirUri.scheme == "file")) {
            val dir = File(dirUri.path ?: return)
            if (dir.isDirectory) {
                dir.listFiles()?.forEach { child ->
                    if (child.isDirectory) deleteRecursive(fileToUri(child))
                    else child.delete()
                }
            }
            dir.delete()
            return
        }
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
            .filter { it.mimeType == DocumentsContract.Document.MIME_TYPE_DIR && it.name.matches(MONTH_DIR_REGEX) }
            .sortedByDescending { it.name }
        if (monthDirs.size > maxMonths) {
            monthDirs.drop(maxMonths).forEach { deleteRecursive(it.uri) }
        }
    }

    fun getOrCreateDir(parentUri: Uri, name: String, mimeType: String = DocumentsContract.Document.MIME_TYPE_DIR): Uri? {
        if (useDirectFileAccess) {
            val parentFile = uriToFile(parentUri) ?: return null
            val dir = File(parentFile, name)
            if (dir.exists() || dir.mkdirs()) return fileToUri(dir)
            return null
        }
        var dir = findChildUri(parentUri, name)
        if (dir == null) {
            val docUri = toDocumentUri(parentUri)
            dir = DocumentsContract.createDocument(contentResolver, docUri, mimeType, name)
        }
        return dir
    }

    fun getBackupRootDir(): Uri? {
        val uriStr = prefs.getString("backup_location_uri", null) ?: return null
        return try {
            Uri.parse(uriStr)
        } catch (_: Exception) {
            Log.w(TAG, "Failed to parse backup location URI: $uriStr")
            null
        }
    }

    fun clearBackupLocation() {
        prefs.edit().remove("backup_location_uri").apply()
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun notifyUser(title: String, message: String) {
        if (!prefs.getBoolean("backup_failure_notify", true)) return
        if (!hasNotificationPermission()) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                NOTIFICATION_CHANNEL_ID, "Backup", android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send notification", e)
        }
    }

    fun getCurrentMonth(): String = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

    fun getBackupMaxMonths(): Int = prefs.getInt("backup_max_months", 5)

    fun setLastSyncTimestamp(timestamp: Long) {
        prefs.edit().putLong("drive_last_sync_at", timestamp).apply()
    }
}