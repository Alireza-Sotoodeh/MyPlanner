package com.example.core.repository

import android.content.SharedPreferences
import com.example.core.database.AppDatabase
import com.example.core.database.entity.MottoEntity

object MottoPicker {
    private const val PREF_USED_IDS = "motto_reminder_used_ids"

    suspend fun pickNext(database: AppDatabase, prefs: SharedPreferences): MottoEntity? {
        val all = database.mottoDao().getAllMottosSync()
        if (all.isEmpty()) return null

        val usedIds = prefs.getString(PREF_USED_IDS, "")
            ?.split(",")
            ?.mapNotNull { it.trim().toLongOrNull() }
            ?.toSet() ?: emptySet()

        val freshPool = all.filter { it.id !in usedIds }
        val picked = if (freshPool.isNotEmpty()) freshPool.random() else all.random()

        val newUsed = if (freshPool.isEmpty()) setOf(picked.id) else usedIds + picked.id
        prefs.edit().putString(PREF_USED_IDS, newUsed.joinToString(",")).apply()
        return picked
    }
}
