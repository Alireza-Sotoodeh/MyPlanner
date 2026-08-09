package com.example.core.manager

import android.content.SharedPreferences
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SettingsBackup(
    val values: Map<String, String> = emptyMap()
)

object SettingsBackupHelper {

    private const val TYPE_BOOLEAN = "boolean"
    private const val TYPE_INT = "int"
    private const val TYPE_STRING = "string"

    val KEYS: Map<String, String> = linkedMapOf(
        "custom_labels" to TYPE_STRING,
        "custom_labels_todo" to TYPE_STRING,
        "auto_reschedule_unfinished" to TYPE_BOOLEAN,
        "auto_sort_enabled" to TYPE_BOOLEAN,
        "default_break_minutes" to TYPE_INT,
        "daily_expand_all_items" to TYPE_BOOLEAN,
        "daily_expand_all_subtasks" to TYPE_BOOLEAN,
        "todo_expand_all_descriptions" to TYPE_BOOLEAN,
        "ideas_expand_all_ideas" to TYPE_BOOLEAN,
        "learn_expand_all_items" to TYPE_BOOLEAN,
        "motto_enabled" to TYPE_BOOLEAN,
        "use_persian_calendar" to TYPE_BOOLEAN,
        "pomodoro_dnd_enabled" to TYPE_BOOLEAN,
        "pomodoro_ringtone_enabled" to TYPE_BOOLEAN,
        "pomodoro_ringtone_uri" to TYPE_STRING,
        "pomodoro_vibrate_enabled" to TYPE_BOOLEAN,
        "pomodoro_vibrate_pattern" to TYPE_STRING,
        "event_reminder_enabled" to TYPE_BOOLEAN,
        "event_reminder_sound" to TYPE_BOOLEAN,
        "event_reminder_vibrate" to TYPE_BOOLEAN,
        "diary_reminder_enabled" to TYPE_BOOLEAN,
        "diary_reminder_time" to TYPE_STRING,
        "habits_reminder_enabled" to TYPE_BOOLEAN,
        "habits_reminder_time" to TYPE_STRING,
        "learn_review_reminder_enabled" to TYPE_BOOLEAN,
        "learn_review_reminder_time" to TYPE_STRING,
        "planner_reminder_enabled" to TYPE_BOOLEAN,
        "planner_reminder_time" to TYPE_STRING,
        "review_reminder_enabled" to TYPE_BOOLEAN,
        "review_reminder_time" to TYPE_STRING,
        "sleep_reminder_enabled" to TYPE_BOOLEAN,
        "sleep_reminder_time" to TYPE_STRING,
        "tomorrow_planner_reminder_enabled" to TYPE_BOOLEAN,
        "tomorrow_planner_reminder_time" to TYPE_STRING,
        "backup_enabled" to TYPE_BOOLEAN,
        "backup_time" to TYPE_STRING,
        "backup_days_of_week" to TYPE_STRING,
        "backup_failure_notify" to TYPE_BOOLEAN,
        "backup_max_months" to TYPE_INT
    )

    fun capture(prefs: SharedPreferences): SettingsBackup {
        val values = KEYS.mapNotNull { (key, type) ->
            val value = when (type) {
                TYPE_BOOLEAN -> if (prefs.contains(key)) prefs.getBoolean(key, false).toString() else null
                TYPE_INT -> if (prefs.contains(key)) prefs.getInt(key, 0).toString() else null
                else -> prefs.getString(key, null)
            }
            value?.let { key to it }
        }.toMap()
        return SettingsBackup(values)
    }

    fun apply(prefs: SharedPreferences, backup: SettingsBackup) {
        val editor = prefs.edit()
        backup.values.forEach { (key, value) ->
            val type = KEYS[key] ?: return@forEach
            try {
                when (type) {
                    TYPE_BOOLEAN -> editor.putBoolean(key, value.toBooleanStrict())
                    TYPE_INT -> editor.putInt(key, value.toInt())
                    else -> editor.putString(key, value)
                }
            } catch (_: Exception) {
                // skip invalid values, keep existing/default
            }
        }
        editor.apply()
    }
}
