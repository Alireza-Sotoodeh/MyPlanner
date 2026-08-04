# Backup & Sync Implementation Plan

## Why This Matters

The current backup system is a **skeleton/facade** — it serializes 14 of 17 Room entity types to a local JSON file via Moshi but has **no actual Google Drive integration** despite the UI suggesting otherwise. All operations are purely local and the "Google Drive Connected" toggle is cosmetic.

This plan covers:
1. **Cloud Backup/Restore** — Proper Google Drive API v3 integration
2. **LLM Readability** — Enhanced JSON format for local LLM consumption (analysis, habit recommendations, behavior insights)

---

## 1. Data Inventory

### 1.1 Room Database Entities (17 registered in `AppDatabase.kt`)

| # | Entity | Table | Fields | Has `createdAt`? | Has `updatedAt`? | Currently backed up? |
|---|--------|-------|--------|-----------------|-----------------|---------------------|
| 1 | `TaskEntity` | `tasks` | 33 | ✅ | ❌ | ✅ |
| 2 | `HabitEntity` | `habits` | 12 | ✅ | ❌ | ✅ |
| 3 | `HabitLogEntity` | `habit_logs` | 6 | ✅ (as `timestamp`) | ❌ | ✅ |
| 4 | `SleepLogEntity` | `sleep_logs` | 8 | ✅ (as `timestamp`) | ❌ | ✅ |
| 5 | **`TimerSessionEntity`** | `timer_sessions` | 8 | ✅ (as `timestamp`) | ❌ | ❌ **MISSING** |
| 6 | **`TimerTemplateEntity`** | `timer_templates` | 5 | ❌ | ❌ | ❌ **MISSING** |
| 7 | `IdeaGroupEntity` | `idea_groups` | 4 | ❌ | ❌ | ✅ |
| 8 | `IdeaEntity` | `ideas` | 7 | ✅ | ❌ | ✅ |
| 9 | `IdeaStageEntity` | `idea_stages` | 5 | ❌ | ❌ | ✅ |
| 10 | `TodoEntity` | `todos` | 9 | ✅ | ❌ | ✅ |
| 11 | `DiaryEntryEntity` | `diary_entries` | 5 | ✅ | ✅ | ✅ |
| 12 | `ShopItemEntity` | `shop_items` | 6 | ✅ | ❌ | ✅ |
| 13 | `MottoEntity` | `mottos` | 3 | ✅ | ❌ | ✅ |
| 14 | `DayReviewEntity` | `day_reviews` | 9 | ✅ | ❌ | ✅ |
| 15 | `LearnGroupEntity` | `learn_groups` | 4 | ❌ | ❌ | ✅ |
| 16 | `LearnItemEntity` | `learn_items` | 13 | ✅ | ❌ | ✅ |
| 17 | `LearnSectionEntity` | `learn_sections` | 11 | ❌ (has `lastReviewDate`/`nextReviewDate`) | ❌ | ✅ |

**Totals:** 17 entities, ~147 fields, 5 entities missing `createdAt`, 16 entities missing `updatedAt`

### 1.2 SharedPreferences (`bulletcoach_prefs` — 38 keys)

#### App Settings
| Key | Type | Default |
|-----|------|---------|
| `google_drive_connected` | Boolean | false |
| `google_drive_email` | String | `"ar.sotoodeh@gmail.com"` |
| `use_persian_calendar` | Boolean | false |
| `auto_sort_enabled` | Boolean | false |
| `motto_enabled` | Boolean | true |
| `custom_labels` | String (JSON) | `""` |

#### Pomodoro / Timer Settings
| Key | Type | Default |
|-----|------|---------|
| `pomodoro_dnd_enabled` | Boolean | false |
| `pomodoro_ringtone_uri` | String | `""` |
| `pomodoro_ringtone_enabled` | Boolean | true |
| `pomodoro_vibrate_enabled` | Boolean | true |
| `default_break_minutes` | Int | 5 |

#### Event Reminder Settings
| Key | Type | Default |
|-----|------|---------|
| `event_reminder_vibrate` | Boolean | true |
| `event_reminder_sound` | Boolean | true |
| `event_reminder_enabled` | Boolean | true |

#### UI Expand/Collapse State (local only — skip from backup)
| Key | Type | Default |
|-----|------|---------|
| `daily_expand_all_items` | Boolean | true |
| `daily_expand_all_subtasks` | Boolean | true |
| `todo_expand_all_descriptions` | Boolean | false |
| `ideas_expand_all_ideas` | Boolean | true |
| `learn_expand_all_items` | Boolean | true |

#### Daily Cache (transient — skip from backup)
| Key | Type | Default |
|-----|------|---------|
| `reviewed_today` | Boolean | false |
| `reviewed_today_date` | String | null |
| `today_motto_date` | String | `""` |
| `today_motto_id` | Long | -1L |

#### Reminder Schedules (7 pairs = 14 keys)
| Reminder | Enabled Key | Time Key | Default Time |
|----------|------------|----------|-------------|
| Day Review | `review_reminder_enabled` | `review_reminder_time` | `"21:00"` |
| Sleep Log | `sleep_reminder_enabled` | `sleep_reminder_time` | `"09:00"` |
| Diary | `diary_reminder_enabled` | `diary_reminder_time` | `"20:00"` |
| Morning Planner | `planner_reminder_enabled` | `planner_reminder_time` | `"07:00"` |
| Habits Check-in | `habits_reminder_enabled` | `habits_reminder_time` | `"21:00"` |
| Tomorrow Planner | `tomorrow_planner_reminder_enabled` | `tomorrow_planner_reminder_time` | `"20:00"` |
| Learn Review | `learn_review_reminder_enabled` | `learn_review_reminder_time` | `"19:00"` |

---

## 2. Entity Relationships (must preserve on restore)

```
TaskEntity
  ├── parentTaskId → TaskEntity.id (subtasks)
  ├── linkedTodoId → TodoEntity.id
  ├── linkedIdeaId → IdeaEntity.id
  └── linkedLearnSectionId → LearnSectionEntity.id

IdeaEntity
  └── groupId → IdeaGroupEntity.id

IdeaStageEntity
  └── ideaId → IdeaEntity.id (CASCADE delete)

TodoEntity
  ├── linkedTaskId → TaskEntity.id
  └── parentTodoId → TodoEntity.id (sub-todos)

TimerSessionEntity
  └── taskId → TaskEntity.id (nullable)

LearnItemEntity
  └── groupId → LearnGroupEntity.id (CASCADE delete)

LearnSectionEntity
  ├── learnItemId → LearnItemEntity.id (CASCADE delete)
  ├── studyTaskId → TaskEntity.id
  └── reviewTaskId → TaskEntity.id

HabitLogEntity
  └── habitId → HabitEntity.id
```

**Restore order constraint:** Groups before items, parents before children.

---

## 3. Pre-req Fixes (must fix before sync implementation)

### Priority 1 — Data Integrity Risks

| # | Issue | File | Lines | Fix |
|---|-------|------|-------|-----|
| 1 | **Restore doesn't clear DB** — appends instead of replacing, causes duplicates on every restore | `MainViewModel.kt` | 534-548 | Call repository `deleteAll` methods before restore |
| 2 | **Missing TimerSession + TimerTemplate entities** — silently dropped from backup | `BulletCoachBackup`, `MainViewModel.kt` | 135-151, 464-513 | Add to data class + collect in backup function |
| 3 | **Missing SharedPreferences backup** — settings lost on restore | `MainViewModel.kt` | 464-558 | Add settings map to backup data class |
| 4 | **`ideaStagesList` uses stale StateFlow** — `allIdeas.value` may be outdated | `MainViewModel.kt` | 479 | Use `ideaRepository.getAllIdeas().first()` |
| 5 | **No backup format version** — future schema changes silently break restore | `BulletCoachBackup` | 135 | Add `backupVersion: Int` field |
| 6 | **Restore order for linked entities** — FK crashes if order wrong | `MainViewModel.kt` | 534-548 | Verify FK order is correct for all 17 entities |
| 7 | **Android auto-backup rules empty** — Room DB excluded from system backup | `backup_rules.xml`, `data_extraction_rules.xml` | Entire files | Add `<include>` directives for Room DB + SharedPrefs |

### Priority 2 — Architecture Gaps

| # | Issue | Detail | Fix |
|---|-------|--------|-----|
| 8 | **No `updatedAt` on 16 entities** — can't detect what changed for incremental sync | Only `DiaryEntryEntity` has `updatedAt` | Add `updatedAt: Long = System.currentTimeMillis()` to all entities |
| 9 | **Auto-increment Long IDs** — ID collision on cross-device restore | All entities use `@PrimaryKey(autoGenerate = true)` | Use UUIDs or ID remapping on restore |
| 10 | **No `isDeleted` soft-delete** — deletions can't sync back | No entity has a deletion flag | Add `isDeleted: Boolean = false` field |
| 11 | **Moshi reflective adapter** — 0 compile-time safety, slower serialization | `KotlinJsonAdapterFactory()` on line 199, no `@JsonClass` annotations | Add `@JsonClass(generateAdapter = true)` to all entities + `BulletCoachBackup` |
| 12 | **No transaction wrapping** — partial backup/restore leaves inconsistent state | Multiple repository calls, no atomicity | Wrap restore in `RoomDatabase.runInTransaction()` |

### Priority 3 — UX Improvements

| # | Issue | Detail | Fix |
|---|-------|--------|-----|
| 13 | **No restore confirmation** — tap Restore immediately alters data | `PlannerScreen.kt` SettingsDialog | Add `AlertDialog` confirmation before restore |
| 14 | **No loading spinner** — backup/restore fire-and-forget | `MainViewModel.kt` callbacks | Add `isSyncing: StateFlow<Boolean>` + `CircularProgressIndicator` |
| 15 | **Hardcoded default email** `ar.sotoodeh@gmail.com` | `MainViewModel.kt` line 218 | Use empty string default |

---

## 4. Implementation Plan

### Phase 1: Pre-req Fixes

**Step 1.1 — Data model fixes**
- Add `updatedAt: Long = System.currentTimeMillis()` to all 17 entities
- Add `@JsonClass(generateAdapter = true)` annotation to all entities + `BulletCoachBackup`
- Update `BulletCoachBackup`:
  - Add `backupVersion: Int = 1`
  - Add `settings: Map<String, Any>` for SharedPreferences
  - Add `timerSessions: List<TimerSessionEntity>`
  - Add `timerTemplates: List<TimerTemplateEntity>`
- Add `isDeleted: Boolean = false` to all entities (for future incremental sync)

**Step 1.2 — Moshi codegen setup**
- Add `@JsonClass(generateAdapter = true)` to all entities
- Build → verify KSP generates adapters
- Switch from `KotlinJsonAdapterFactory()` to generated adapters

**Step 1.3 — Backup function fixes**
```kotlin
fun backupDataToGoogleDrive(onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
        try {
            val tasksList = taskRepository.getAllTasks().first()
            val habitsList = habitRepository.allHabits.first()
            val habitLogsList = habitRepository.getAllLogs().first()
            val sleepLogsList = sleepLogRepository.allSleepLogs.first()
            val ideaGroupsList = ideaRepository.allGroups.first()
            val ideasList = ideaRepository.getAllIdeas().first()              // ✅ fresh data
            val todosList = todoRepository.allTodos.first()
            val diaryEntriesList = diaryRepository.getAllEntries().first()
            val shopItemsList = shopItemRepository.allItems.first()
            val mottosList = mottoRepository.allMottos.first()
            val dayReviewsList = dayReviewRepository.getAllReviews().first()
            val ideaStagesList = ideaRepository.getAllIdeas().first().flatMap { ideaRepository.getStagesForIdeaSync(it.id) }  // ✅ FIXED
            val timerSessionsList = timerRepository.allSessions.first()       // ✅ NEW
            val timerTemplatesList = timerRepository.allTemplates.first()     // ✅ NEW
            
            val settings = getBackupSettings()                                // ✅ NEW
            
            val backupObj = BulletCoachBackup(
                backupVersion = 1,                                          // ✅ NEW
                tasks = tasksList, habits = habitsList, habitLogs = habitLogsList,
                sleepLogs = sleepLogsList, ideaGroups = ideaGroupsList, ideas = ideasList,
                ideaStages = ideaStagesList, todos = todosList,
                diaryEntries = diaryEntriesList, shopItems = shopItemsList,
                mottos = mottosList, dayReviews = dayReviewsList,
                timerSessions = timerSessionsList, timerTemplates = timerTemplatesList,
                learnGroups = learnRepository.getAllGroupsSync(),
                learnItems = learnItems.value,
                learnSections = learnItems.value.flatMap { learnRepository.getSectionsForItemSync(it.id) },
                settings = settings
            )
            // ... serialize and upload
```

**Step 1.4 — Restore function fixes**
```kotlin
fun restoreDataFromGoogleDrive(onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
        try {
            // 1️⃣ Parse backup
            val backupObj = parseBackupFile() ?: return@launch
            
            // 2️⃣ Check backup version compatibility
            if (backupObj.backupVersion > CURRENT_BACKUP_VERSION) {
                onResult(false, "Backup from a newer app version. Please update the app.")
                return@launch
            }
            
            // 3️⃣ Clear existing data (transactional)
            database.runInTransaction {
                deleteAllTables()
            }
            
            // 4️⃣ Restore in FK-safe order
            restoreGroups(backupObj)      // idea_groups, learn_groups
            restoreEntities(backupObj)    // entities without FKs
            restoreLinked(backupObj)      // entities with FKs
            
            // 5️⃣ Restore SharedPreferences
            restoreSettings(backupObj.settings)
            
            // 6️⃣ Refresh all StateFlows
            refreshAllState()
            
            onResult(true, "Restored successfully!")
```

**Step 1.5 — Auto-backup rules**
```xml
<!-- backup_rules.xml -->
<full-backup-content>
    <include domain="database" path="bulletcoach_database"/>
    <include domain="sharedpref" path="bulletcoach_prefs.xml"/>
    <exclude domain="sharedpref" path="device.xml"/>
</full-backup-content>
```

```xml
<!-- data_extraction_rules.xml -->
<data-extraction-rules>
    <cloud-backup>
        <include domain="database" path="bulletcoach_database"/>
        <include domain="sharedpref" path="bulletcoach_prefs.xml"/>
        <include domain="file" path="bulletcoach_backup.json"/>
    </cloud-backup>
    <device-transfer>
        <include domain="database" path="bulletcoach_database"/>
        <include domain="sharedpref" path="bulletcoach_prefs.xml"/>
    </device-transfer>
</data-extraction-rules>
```

### Phase 2: Google Drive Integration

**Dependencies** (`build.gradle.kts`):
```kotlin
implementation("com.google.android.gms:play-services-auth:21.3.0")
implementation("com.google.apis:google-api-services-drive:v3-rev20241208-2.0.0")
implementation("com.google.http-client:google-http-client-gson:1.46.3")
```

**Implementation steps:**
1. Add `GoogleSignIn` OAuth flow with `CredentialManager`
2. Request `DriveScopes.DRIVE_FILE` scope
3. Upload JSON to app folder on Drive (`application/vnd.google-apps.folder`)
4. Download/overwrite on restore
5. Add `lastSyncAt: Long` tracking
6. Add periodic auto-backup option (e.g., daily via WorkManager)

### Phase 3: LLM Readability Enhancements

Enhanced JSON structure:
```json
{
  "backupVersion": 1,
  "createdAt": "2026-07-19T15:30:00Z",
  "deviceName": "Pixel 7",
  "userSummary": {
    "totalTasks": 47,
    "completedTasks": 23,
    "completionRate": 0.49,
    "activeHabits": 5,
    "totalDiaryEntries": 89,
    "habitStreakDays": 12,
    "averageSleepHours": 7.3,
    "averageMoodRating": 3.8,
    "learnItemsInProgress": 3,
    "totalPomodorosCompleted": 142
  },
  "entities": {
    "tasks": [
      {
        "id": 1,
        "title": "Write project proposal",
        "description": "Draft the initial proposal for the client meeting",
        "date": "2026-07-19",
        "dateType": "daily",
        "status": "PENDING",
        "type": "TASK",
        "priorityLevel": "High",
        "label": "Work",
        "labelColor": "#FF5733",
        "eventTime": null,
        "durationMinutes": 45,
        "pomodorosCompleted": 0,
        "targetSessions": 2,
        "reminderMinutesBefore": 15,
        "subtasks": [
          {
            "id": 2,
            "title": "Research competitors",
            "status": "COMPLETED",
            "importance": "IMPORTANT"
          },
          {
            "id": 3,
            "title": "Write outline",
            "status": "PENDING",
            "importance": "OPTIONAL"
          }
        ],
        "linkedTodo": null,
        "linkedIdea": null,
        "linkedLearnSection": null,
        "createdAt": 1772800000000,
        "updatedAt": 1772900000000
      }
    ],
    "habits": [...],
    "habitLogs": [...],
    "sleepLogs": [...],
    "timerSessions": [...],
    "timerTemplates": [...],
    "ideasWithStages": [...],
    "todos": [...],
    "diaryEntries": [...],
    "shopItems": [...],
    "mottos": [...],
    "dayReviews": [...],
    "learnGroups": [...],
    "learnItemsWithSections": [...]
  },
  "settings": {
    "usePersianCalendar": false,
    "autoSortEnabled": true,
    "mottoEnabled": true,
    "customLabels": ["Work", "Personal", "Health", "Study"],
    "reminders": {
      "dayReview": { "enabled": true, "time": "21:00" },
      "morningPlanner": { "enabled": true, "time": "07:00" },
      "habitsCheckin": { "enabled": true, "time": "21:00" },
      "diary": { "enabled": false, "time": "20:00" },
      "sleepLog": { "enabled": true, "time": "09:00" },
      "tomorrowPlanner": { "enabled": true, "time": "20:00" },
      "learnReview": { "enabled": true, "time": "19:00" }
    },
    "pomodoro": {
      "dndEnabled": false,
      "ringtoneEnabled": true,
      "vibrateEnabled": true,
      "defaultBreakMinutes": 5
    },
    "eventReminders": {
      "enabled": true,
      "vibrate": true,
      "sound": true
    }
  }
}
```

**LLM-friendly enhancements:**
- `userSummary` — aggregated statistics for instant context
- `entities.tasks[].subtasks` — inline children instead of separate flat array
- `entities.ideasWithStages` — ideas pre-merged with their stages
- `entities.learnItemsWithSections` — learn items pre-merged with sections
- `settings.reminders` — structured by type instead of flat key-value pairs
- Human-readable status labels alongside internal values
- `dateType` field clarifies whether a date is daily (`yyyy-MM-dd`), monthly (`yyyy-MM`), or weekly (`yyyy-'W'ww`)

---

## 5. Edge Cases & Risks

| Risk | Severity | Mitigation |
|------|----------|------------|
| **Large datasets** — 1000+ tasks + logs could produce 5MB+ JSON | Medium | Add gzip compression, streaming serialization |
| **Interrupted backup/restore** — crash mid-write leaves DB inconsistent | High | Wrap restore in `RoomDatabase.runInTransaction()` |
| **FK constraint crash** — restore entities in wrong order | High | Enforce strict FK-safe ordering |
| **Unique index conflicts** — `diary_entries.date` and `day_reviews.date` have unique indexes | High | Check+skip or overwrite by date |
| **Persian vs Gregorian dates** — LLM needs to know which calendar | Low | Add `calendar: "gregorian"` or `"persian"` to each date field |
| **Backup from newer app version** — fields exist in backup but not in current schema | Medium | Check `backupVersion`, ignore unknown fields with `@Json(ignoreUnknown = true)` or lenient adapter |
| **Google Drive API quota** — free tier allows ~10M requests/day | Low | Add rate limiting, cache responses |
| **No network during restore** — Drive file unavailable | Medium | Cache last backup locally; offer offline restore from cache |
| **Multiple devices syncing** — same Google account, different phones | Medium | Add device UUID to backup filename, merge strategy needed |

---

## 6. File Manifest

| File | Purpose |
|------|---------|
| `app/src/main/java/com/example/ui/viewmodel/MainViewModel.kt` | `BulletCoachBackup` data class, backup/restore functions |
| `app/src/main/java/com/example/ui/screens/PlannerScreen.kt` | SettingsDialog UI (lines ~3540-3798) |
| `app/src/main/java/com/example/core/database/entity/*.kt` | 17 entity files — need `@JsonClass`, `updatedAt` |
| `app/src/main/res/xml/backup_rules.xml` | Android auto-backup rules |
| `app/src/main/res/xml/data_extraction_rules.xml` | Android data extraction rules |
| `app/build.gradle.kts` | Google Drive API + Moshi codegen dependencies |

---

## 7. Future Considerations

- **Bi-directional sync** — This plan covers backup/restore only. True 2-way sync with conflict resolution (last-writer-wins, or 3-way merge) is a separate project.
- **End-to-end encryption** — For sensitive diary/health data, encrypt the JSON before uploading to Drive.
- **Selective restore** — Allow restoring only specific entity types (e.g., restore habits without touching tasks).
- **Scheduled auto-backup** — WorkManager-based daily backup to Drive.
- **Export for LLM** — One-tap export button that saves the enhanced JSON to Downloads for immediate LLM consumption.
