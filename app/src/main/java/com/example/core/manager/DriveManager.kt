package com.example.core.manager

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.Scope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object DriveManager {
    private const val TAG = "DriveManager"
    private const val APP_FOLDER_NAME = "bulletcoach_backups"
    const val MAX_BACKUPS = 3
    private const val DRIVE_API = "https://www.googleapis.com/drive/v3"
    private const val DRIVE_UPLOAD = "https://www.googleapis.com/upload/drive/v3"
    private const val SCOPE = "oauth2:https://www.googleapis.com/auth/drive.file"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private var cachedToken: String? = null
    private var tokenExpiry: Long = 0L

    fun getSignInIntent(context: Context): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope("https://www.googleapis.com/auth/drive.file"))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso).signInIntent
    }

    fun handleSignInResult(data: Intent?): GoogleSignInAccount? {
        return try {
            GoogleSignIn.getSignedInAccountFromIntent(data).result
        } catch (e: Exception) {
            Log.e(TAG, "Sign-in result failed", e)
            null
        }
    }

    fun isSignedIn(context: Context): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }

    fun signOut(context: Context) {
        cachedToken = null
        tokenExpiry = 0L
        GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
    }

    fun invalidateToken() {
        cachedToken = null
        tokenExpiry = 0L
    }

    private fun getToken(context: Context): String? {
        val now = System.currentTimeMillis()
        if (cachedToken != null && tokenExpiry > now + 60000) return cachedToken

        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        return try {
            val token = GoogleAuthUtil.getToken(context, account.account!!, SCOPE)
            cachedToken = token
            tokenExpiry = now + 55 * 60 * 1000L
            token
        } catch (e: UserRecoverableAuthException) {
            Log.w(TAG, "Recoverable auth error, need user interaction", e)
            cachedToken = null
            tokenExpiry = 0L
            null
        } catch (e: GoogleAuthException) {
            Log.e(TAG, "Auth failed (token may be revoked), clearing state", e)
            cachedToken = null
            tokenExpiry = 0L
            null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get token", e)
            if (cachedToken != null) {
                cachedToken = null
                tokenExpiry = 0L
                return try {
                    val token = GoogleAuthUtil.getToken(context, account.account!!, SCOPE)
                    cachedToken = token
                    tokenExpiry = now + 55 * 60 * 1000L
                    token
                } catch (_: Exception) { null }
            }
            null
        }
    }

    fun isPlayServicesAvailable(context: Context): Boolean {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == com.google.android.gms.common.ConnectionResult.SUCCESS
    }

    private fun getFolderId(context: Context, token: String): String? {
        return try {
            val listUrl = "$DRIVE_API/files?q=name='$APP_FOLDER_NAME' and mimeType='application/vnd.google-apps.folder' and trashed=false&pageSize=1"
            val listReq = Request.Builder().url(listUrl).header("Authorization", "Bearer $token").build()
            val listResp = client.newCall(listReq).execute()
            val listBody = listResp.body?.string() ?: return null
            val listJson = JSONObject(listBody)
            val files = listJson.optJSONArray("files")
            if (files != null && files.length() > 0) return files.getJSONObject(0).getString("id")

            val createJson = JSONObject().apply {
                put("name", APP_FOLDER_NAME)
                put("mimeType", "application/vnd.google-apps.folder")
            }
            val createReq = Request.Builder().url("$DRIVE_API/files")
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .post(createJson.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                .build()
            val createResp = client.newCall(createReq).execute()
            val createBody = createResp.body?.string() ?: return null
            JSONObject(createBody).optString("id", null)
        } catch (e: Exception) {
            Log.e(TAG, "Folder lookup/creation failed", e)
            null
        }
    }

    fun uploadBackup(context: Context, data: ByteArray, filename: String): String? {
        return try {
            val token = getToken(context) ?: return null
            val folderId = getFolderId(context, token) ?: return null

            val metadata = JSONObject().apply {
                put("name", filename)
                put("parents", JSONArray().put(folderId))
            }

            val boundary = "drive_boundary_${System.currentTimeMillis()}"
            val bodyBuilder = StringBuilder()
            bodyBuilder.append("--$boundary\r\n")
            bodyBuilder.append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
            bodyBuilder.append(metadata.toString())
            bodyBuilder.append("\r\n--$boundary\r\n")
            bodyBuilder.append("Content-Type: application/octet-stream\r\n\r\n")

            val prefixBytes = bodyBuilder.toString().toByteArray(Charsets.UTF_8)
            val suffixBytes = "\r\n--$boundary--\r\n".toByteArray(Charsets.UTF_8)
            val bodyBytes = prefixBytes + data + suffixBytes

            val request = Request.Builder()
                .url("$DRIVE_UPLOAD/files?uploadType=multipart")
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "multipart/related; boundary=$boundary")
                .post(bodyBytes.toRequestBody("multipart/related".toMediaTypeOrNull()))
                .build()

            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: return null
            val fileId = JSONObject(respBody).optString("id", null)
            if (fileId != null) {
                rotateBackups(context)
                rotateLocalBackups(context)
            }
            fileId
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed", e)
            null
        }
    }

    fun downloadLatest(context: Context): ByteArray? {
        return try {
            val token = getToken(context) ?: return null
            val listUrl = "$DRIVE_API/files?q=name contains 'bulletcoach_' and trashed=false&orderBy=createdTime desc&pageSize=1"
            val listReq = Request.Builder().url(listUrl).header("Authorization", "Bearer $token").build()
            val listResp = client.newCall(listReq).execute()
            val listBody = listResp.body?.string() ?: return null
            val listJson = JSONObject(listBody)
            val files = listJson.optJSONArray("files")
            if (files == null || files.length() == 0) return null

            val fileId = files.getJSONObject(0).getString("id")
            val downloadReq = Request.Builder()
                .url("$DRIVE_API/files/$fileId?alt=media")
                .header("Authorization", "Bearer $token")
                .build()
            val downloadResp = client.newCall(downloadReq).execute()
            downloadResp.body?.bytes()
        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            null
        }
    }

    data class DriveFileInfo(val id: String, val name: String, val createdTime: Long)

    fun listBackups(context: Context): List<DriveFileInfo> {
        return try {
            val token = getToken(context) ?: return emptyList()
            val listUrl = "$DRIVE_API/files?q=name contains 'bulletcoach_' and trashed=false&orderBy=createdTime desc&pageSize=10&fields=files(id,name,createdTime)"
            val listReq = Request.Builder().url(listUrl).header("Authorization", "Bearer $token").build()
            val listResp = client.newCall(listReq).execute()
            val listBody = listResp.body?.string() ?: return emptyList()
            val files = JSONObject(listBody).optJSONArray("files") ?: return emptyList()
            (0 until files.length()).map { i ->
                val f = files.getJSONObject(i)
                DriveFileInfo(
                    id = f.getString("id"),
                    name = f.optString("name", ""),
                    createdTime = try {
                        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                            .parse(f.optString("createdTime", ""))?.time ?: 0L
                    } catch (_: Exception) { 0L }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "List backups failed", e)
            emptyList()
        }
    }

    fun deleteBackup(context: Context, fileId: String): Boolean {
        val token = getToken(context) ?: return false
        val req = Request.Builder()
            .url("$DRIVE_API/files/$fileId")
            .header("Authorization", "Bearer $token")
            .delete()
            .build()
        return try {
            val resp = client.newCall(req).execute()
            resp.isSuccessful
        } catch (e: Exception) {
            Log.w(TAG, "Delete failed", e)
            false
        }
    }

    fun rotateBackups(context: Context) {
        try {
            val backups = listBackups(context)
            if (backups.size >= MAX_BACKUPS) {
                val toDelete = backups.sortedBy { it.createdTime }.take(backups.size - MAX_BACKUPS + 1)
                toDelete.forEach { deleteBackup(context, it.id) }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Rotation failed", e)
        }
    }

    fun rotateLocalBackups(context: Context) {
        try {
            val backupDir = context.filesDir
            val files = backupDir.listFiles { f -> f.name.startsWith("bulletcoach_backup_") && f.name.endsWith(".json.gz") }
                ?.sortedBy { it.lastModified() } ?: return
            if (files.size >= MAX_BACKUPS) {
                val toDelete = files.take(files.size - MAX_BACKUPS + 1)
                toDelete.forEach { if (it.delete()) Log.d(TAG, "Deleted old local backup: ${it.name}") }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Local rotation failed", e)
        }
    }
}
