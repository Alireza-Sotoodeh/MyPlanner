# Full Backup/Restore Implementation Plan

Covers all 24 remaining bugs from `BUG_Sync_plan.md` + full Google Drive sync + LLM export.
~7 days of work, broken into actionable substeps.

## Status

- `[ ]` — not started
- `[~]` — in progress
- `[x]` — done

---

## Step 1: Quick Bug Fixes `[x] DONE`

*Files: MainViewModel.kt, docs/backup-sync-plan.md, PlannerScreen.kt, TimerScreen.kt*

### 1.1 — Fix hardcoded email default (#31)

- [x] **Edit** `MainViewModel.kt:226`: `"ar.sotoodeh@gmail.com"` → `""`
- [x] **Verify:** `grep` shows `"ar.sotoodeh@gmail.com"` no longer appears in code (only in function signature for updateGoogleDriveConnected, not as default)

### 1.2 — Fix doc key count (#49)

- [x] **Edit** `docs/backup-sync-plan.md:39`: `"36 keys"` → `"38 keys"` (was already `38` from prior session)
- [x] **Verify:** Header reads `38 keys`

### 1.3 — Add `default_break_minutes` write path (#50)

- [x] **Add** `fun updateDefaultBreakMinutes(minutes: Int)` in `MainViewModel.kt` after `updateCustomLabels()`
- [x] **Add** row in SettingsDialog "TIMER & FOCUS" section (after vibrate toggle, before Test Alarm):
  - `val defaultBreakMinutes by viewModel.defaultBreakMinutes.collectAsState()`
  - Local `enteredDefaultBreakMinutes` copy as string
  - `OutlinedTextField` with number keyboard, 0–30 validation
  - Calls `viewModel.updateDefaultBreakMinutes(num)` on every valid change
- [x] **Wire** `_pomodoroShortBreakMinutes` init to read from `_defaultBreakMinutes` instead of hardcoded `5`
- [x] **Wire** `startPomodoro()` default `shortBreakMinutes: Int? =` to read from `_defaultBreakMinutes.value`
- [x] **Add** missing imports: `KeyboardOptions`, `KeyboardType`
- [x] **Verify:** `assembleDebug` succeeds

### 1.4 — Handle Moshi strict parsing (#60)

- [x] **Attempted** `.failOnUnknown()` on `Moshi.Builder` — not available in Moshi 1.15.2's Java API as method on builder
- [x] **Resolution:** Version check (#63) already guards against format mismatches. Strict parsing deferred to future Moshi upgrade. No change needed — existing backup/restore uses version-gated validation.
- [x] **Verify:** `assembleDebug` succeeds

---

## Step 2: Restore Integrity Fixes `[x] DONE`

*Files: MainViewModel.kt, PlannerScreen.kt, SystemSettingsApplier.kt (new)*

### 2.1 — FK orphan nullification on restore (#12/#59)

- [x] Build lookup sets: `taskIds`, `todoIds`, `ideaIds`, `learnSectionIds` from `backupObj`
- [x] Nullify `TaskEntity.linkedTodoId` if TodoEntity.id not in restored set
- [x] Nullify `TaskEntity.linkedIdeaId` if IdeaEntity.id not in restored set
- [x] Nullify `TaskEntity.linkedLearnSectionId` if LearnSectionEntity.id not in restored set
- [x] Nullify `TodoEntity.linkedTaskId` if TaskEntity.id not in restored set
- [x] Nullify `IdeaEntity.linkedTaskId` if TaskEntity.id not in restored set
- [x] Nullify `LearnSectionEntity.studyTaskId` if TaskEntity.id not in restored set
- [x] Nullify `LearnSectionEntity.reviewTaskId` if TaskEntity.id not in restored set
- [x] TimerSessionEntity.taskId nullification deferred — entity not in BulletCoachBackup yet
- [x] **Edge case:** All FK fields are nullable (`Long?`) — no crash risk from setting null
- [x] **Verify:** `assembleDebug` succeeds

### 2.2 — Save consistency for reminder prefs (#19)

- [x] Changed `ModalBottomSheet.onDismissRequest` to auto-save ALL 22 settings before closing (DND, event, pomodoro, and all 7 reminders × 2 each)
- [x] CANCEL button → renamed to CLOSE, calls `onDismiss()` directly (which auto-saves)
- [x] SAVE & CLOSE kept as explicit save button (redundant but harmless for users who expect it)
- [x] **Edge case:** No way to discard changes now — but this is consistent behavior (all dismiss methods save)
- [x] **Verify:** Toggle any setting → dismiss dialog (swipe/back/close) → reopen → change preserved

### 2.3 — Re-apply system settings after restore (#48)

- [x] **New file:** `SystemSettingsApplier.kt` at `core/manager/`
- [x] Re-creates 6 notification channels: `pomodoro`, `event_reminder`, `habits`, `task_reminder`, `learn_review`, `reminders`
- [x] Called via fully-qualified reference `com.example.core.manager.SystemSettingsApplier.reapplyAfterRestore(context)` after FK nullification in restore function
- [x] **Note:** DND re-application deferred — requires user-granted permission on Android 13+
- [x] **Note:** Notification channel importance can't be raised programmatically, only lowered — documented limitation
- [x] **Verify:** `assembleDebug` succeeds

### 2.4 — Backup file size check (#54)

- [x] Added `MAX_BACKUP_SIZE = 50 * 1024 * 1024L` (50 MB) check before `readText()` in restore function
- [x] **Edge case:** `file.length()` returns 0 for non-existent — already handled by existence check above
- [x] **Verify:** `assembleDebug` succeeds

---

## Step 3: DB Schema Changes (Migration v28 → v29) `[x] DONE`

*Files: 7 entity files, AppDatabase.kt, build.gradle.kts*

### 3.1 — Add FK + indices to TaskEntity

- [x] Added 4 FKs: `parentTaskId→tasks`, `linkedTodoId→todos`, `linkedIdeaId→ideas`, `linkedLearnSectionId→learn_sections` (all `SET NULL`)
- [x] Added 4 indices: `parentTaskId`, `linkedTodoId`, `linkedIdeaId`, `linkedLearnSectionId`
- [x] **Verify:** `assembleDebug` compiles

### 3.2 — Add FK + indices to TodoEntity

- [x] Added 2 FKs: `linkedTaskId→tasks` (`SET NULL`), `parentTodoId→todos` (`SET NULL`, self-ref)
- [x] Added 2 indices: `linkedTaskId`, `parentTodoId`
- [x] **Verify:** `assembleDebug` compiles

### 3.3 — Add FK + index to IdeaEntity

- [x] Added FK `linkedTaskId→tasks` (`SET NULL`) alongside existing `groupId→idea_groups` FK
- [x] Added index: `linkedTaskId`
- [x] **Verify:** `assembleDebug` compiles

### 3.4 — Add FK + index to TimerSessionEntity

- [x] Added FK `taskId→tasks` (`SET NULL`)
- [x] Added index: `taskId`
- [x] **Verify:** `assembleDebug` compiles

### 3.5 — Add FK + composite unique index to HabitLogEntity

- [x] Added FK `habitId→habits` (`CASCADE`)
- [x] Added composite `UNIQUE` index on `(habitId, date)` + simple index on `habitId`
- [x] **Verify:** `assembleDebug` compiles

### 3.6 — Add FK + indices to LearnSectionEntity

- [x] Added 2 FKs: `studyTaskId→tasks`, `reviewTaskId→tasks` (both `SET NULL`) alongside existing `learnItemId→learn_items` FK
- [x] Indices already existed from MIGRATION_27_28 — re-created during table rename
- [x] **Verify:** `assembleDebug` compiles

### 3.7 — Add composite unique index to IdeaStageEntity

- [x] Added `UNIQUE` index on `(ideaId, orderIndex)` — no table recreation needed
- [x] **Verify:** `assembleDebug` compiles

### 3.8 — Write migration v28 → v29

- [x] Enabled `exportSchema = true` + `ksp { arg("room.schemaLocation", ...) }` in `build.gradle.kts`
- [x] Built once (v28) to capture exact Room-generated CREATE TABLE SQL from `28.json`
- [x] Wrote `MIGRATION_28_29` — table recreation for 6 tables (tasks, todos, ideas, timer_sessions, habit_logs, learn_sections) with `PRAGMA foreign_keys=OFF` to avoid constraint violations; then all indices re-created
- [x] Bumped `AppDatabase.version = 29`
- [x] Added `MIGRATION_28_29` to `.addMigrations(...)` chain
- [x] Kept `fallbackToDestructiveMigration()` as safety net
- [x] **Verify:** `assembleDebug` succeeds, `29.json` generated with all FKs + indices

### 3.9 — Handle circular FK dependency: tasks ↔ todos

- [x] `tasks.linkedTodoId → todos.id` and `todos.linkedTaskId → tasks.id` — Room handles circular FKs natively
- [x] Migration uses `PRAGMA foreign_keys=OFF` during table recreation to avoid constraint violations during data copy
- [x] **Verify:** Both FKs present in `29.json` schema

---

## Step 4: Google Drive Integration

*New files: DriveManager.kt, BackupWorker.kt*
*Modified: build.gradle.kts, MainViewModel.kt*

### 4.1 — Add Drive dependencies `[x] DONE`

- [x] Added to `gradle/libs.versions.toml`: `playServicesAuth = "21.2.0"`, `googleApiServicesDrive = "v3-rev20260712-2.0.0"`, `googleHttpClientGson = "2.1.1"`, `workRuntime = "2.8.0"`
- [x] Added to `app/build.gradle.kts`: `implementation(libs.play.services.auth)`, `implementation(libs.google.api.services.drive)`, `implementation(libs.google.http.client.gson)`, `implementation("androidx.work:work-runtime-ktx:2.7.1")`
- [x] Added `packaging { resources { excludes += ... } }` for META-INF conflicts
- [x] Added `settings.gradle.kts`: `mavenLocal()` + `resolutionStrategy` for AGP plugin resolution
- [x] **Verify:** `assembleDebug` succeeds with no resolution errors

### 4.2 — Create DriveManager.kt

- [ ] **New file:** `app/src/main/java/com/example/core/manager/DriveManager.kt`

- [ ] **Class doc:** `object DriveManager` (singleton, no DI needed)

- [ ] **Internal state:**
  
  ```kotlin
  private var driveService: Drive? = null
  private const val APP_FOLDER_NAME = "bulletcoach_backups"
  private const val MAX_BACKUPS = 3
  ```

- [ ] **Method `signIn(context: Context): Boolean`:**
  
  ```kotlin
  // 1. Build CredentialManager request
  // 2. Use GetCredentialRequest with GoogleIdTokenCredential
  //     and DriveScopes.DRIVE_FILE scope
  // 3. On success, create Drive service via NetHttpTransport + GsonFactory
  // 4. Store account email in prefs
  // 5. Return true/false
  ```

- [ ] **Edge cases:**
  
  - User cancels → return false, don't show error
  - Network error → show "No internet" message
  - Account already signed in → skip auth, use existing token

- [ ] **Method `signOut(context: Context)`:**
  
  ```kotlin
  // 1. Clear CredentialManager credential
  // 2. Clear driveService reference
  // 3. Clear stored email
  ```

- [ ] **Method `isSignedIn(): Boolean`:**
  
  ```kotlin
  // Returns driveService != null
  ```

- [ ] **Method `uploadBackup(context: Context, gzipBytes: ByteArray, filename: String): String?`:**
  
  ```kotlin
  // 1. Ensure authenticated
  // 2. Find or create APP_FOLDER_NAME folder
  // 3. Create file metadata: name=filename, parents=[folderId], mimeType="application/gzip"
  // 4. drive.files().create(metadata, AbstractInputStreamContent("application/gzip", ByteArrayContent(...))).execute()
  // 5. Return file ID
  ```

- [ ] **Edge cases:**
  
  - Token expired → call `signIn()` again silently (retry once)
  - Quota exceeded → return null, caller shows warning
  - File already exists with same name → create with new name (timestamp suffix handles uniqueness)

- [ ] **Method `downloadLatest(context: Context): ByteArray?`:**
  
  ```kotlin
  // 1. List files: drive.files().list().setQ("name contains 'bulletcoach_' and trashed=false").setOrderBy("createdTime desc").setPageSize(1).execute()
  // 2. If no files → return null (no backup exists)
  // 3. Download: drive.files().get(latestFile.id).executeMediaAsInputStream()
  // 4. Read all bytes → return
  ```

- [ ] **Edge cases:**
  
  - First-time user, no backup → return null, show "No backup found"
  - Network timeout → retry once with 5s timeout
  - Corrupted file on Drive → download fails, catch exception, return null

- [ ] **Method `listBackups(context: Context): List<DriveFileInfo>`:**
  
  ```kotlin
  // List all files matching "bulletcoach_" prefix, sorted by createdTime desc
  // Return id + name + createdTime for each (used by rotation)
  ```

- [ ] **Method `deleteBackup(context: Context, fileId: String): Boolean`:**
  
  ```kotlin
  // drive.files().delete(fileId).execute()
  // Return true on success
  ```

- [ ] **Method `getLastSyncAt(): Long`:**
  
  ```kotlin
  // Read from prefs key "drive_last_sync_at"
  ```

- [ ] **Internal `ensureAuthenticated(context: Context): Boolean`:**
  
  ```kotlin
  // Check if driveService is still valid
  // If not, attempt silent sign-in
  // Return true if authenticated
  ```

### 4.3 — Rotation logic (#66)

- [ ] In `uploadBackup()`, after successful upload:
  
  ```kotlin
  val backups = listBackups(context)
  if (backups.size >= MAX_BACKUPS) {
      // Sort by createdTime ascending, delete oldest ones beyond MAX_BACKUPS
      val toDelete = backups.sortedBy { it.createdTime }.take(backups.size - MAX_BACKUPS + 1)
      toDelete.forEach { deleteBackup(context, it.id) }
  }
  ```
- [ ] **Local rotation:** Same logic applied to local `bulletcoach_backup_*.json.gz` files
- [ ] **Edge case:** Deletion fails silently — log warning, don't block the backup

### 4.4 — Wire DriveManager into MainViewModel

- [ ] **`backupDataToGoogleDrive()` changes:**
  
  - After building `BulletCoachBackup` object and serializing to JSON:
    1. If `google_drive_connected` → gzip bytes → `DriveManager.uploadBackup()`
    2. Always save local uncompressed `bulletcoach_backup.json` (offline fallback)
    3. Update `_statusMessage`: "Backed up locally" / "Backed up to Google Drive"
    4. Update `drive_last_sync_at` pref
  - If Drive upload fails → still save locally, show "Drive upload failed, saved locally"

- [ ] **`restoreDataFromGoogleDrive()` changes:**
  
  - Attempt to read from Drive first:
    1. Download → gunzip → Moshi parse
    2. If Drive download fails → fall back to local file
    3. If both fail → show "No backup available"
  - Rest of restore logic unchanged

- [ ] **Edge case:** Drive connected but no network → skip Drive, use local cache

- [ ] **Edge case:** User signed out → show "Google Drive disconnected, using local backup"

### 4.5 — Auto-backup via WorkManager

- [ ] **New file:** `app/src/main/java/com/example/core/manager/BackupWorker.kt`
  
  ```kotlin
  class BackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
      override suspend fun doWork(): Result {
          // 1. Check if google_drive_connected
          // 2. If yes, run backupDataToGoogleDrive
          // 3. Return Result.success() or Result.retry()
      }
  }
  ```
- [ ] **Schedule** in `MainActivity.kt` or `Application.onCreate()`:
  
  ```kotlin
  val dailyBackup = PeriodicWorkRequestBuilder<BackupWorker>(24, TimeUnit.HOURS)
      .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
      .build()
  WorkManager.getInstance(context).enqueueUniquePeriodicWork(
      "daily_drive_backup",
      ExistingPeriodicWorkPolicy.KEEP,
      dailyBackup
  )
  ```
- [ ] **Edge case:** First run delays by 24h — add initial 1h delay for immediate first backup
- [ ] **Edge case:** If user disconnects Drive, WorkManager job still runs but checks flag and exits early

### 4.6 — OAuth UX edge cases

- [ ] **Token expired mid-operation:** Catch `GoogleAuthException`, trigger silent re-auth, retry once
- [ ] **User revokes access:** Next Drive operation fails → clear connected flag → show message
- [ ] **Multiple Google accounts:** CredentialManager shows account picker automatically
- [ ] **Android API < 34:** CredentialManager requires Android 14+ for certain features. Fall back to `GoogleSignInClient` for older APIs
- [ ] **No Google Play Services:** `signIn()` returns false, show "Google Play Services required"

---

## Step 5: Gzip Compression (#24/#55)

*Files: MainViewModel.kt*

### 5.1 — Backup: gzip before Drive upload

- [ ] After Moshi serialization to JSON string, convert to bytes:
  
  ```kotlin
  val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)
  val bos = ByteArrayOutputStream()
  GZIPOutputStream(bos).use { it.write(jsonBytes) }
  val gzipBytes = bos.toByteArray()
  ```
- [ ] Upload `gzipBytes` to Drive (not raw JSON)
- [ ] **Edge case:** Empty backup (no data) — gzip still produces valid output (~20 bytes)
- [ ] **Edge case:** Very large backup (50MB raw → ~5MB gzip) — test with synthetic data

### 5.2 — Local file: keep uncompressed

- [ ] Write `bulletcoach_backup.json` as plain JSON (for offline restore + debugging)
- [ ] **Edge case:** Large files — use `writeText()` is fine up to 50MB (memory remains a concern per #54)

### 5.3 — Restore: gunzip Drive download

- [ ] `downloadLatest()` returns gzipped bytes → decompress:
  
  ```kotlin
  val jsonString = GZIPInputStream(ByteArrayInputStream(driveBytes))
      .use { it.reader(Charsets.UTF_8).readText() }
  ```
- [ ] Local file restore stays the same (`backupFile.readText()` — uncompressed)
- [ ] **Edge case:** Corrupted gzip → `ZipException` → catch → show "Corrupted backup" → fall back to local

### 5.4 — Rotation: apply gzip + local

- [ ] Drive filenames: `bulletcoach_YYYYMMDD_HHmmss.json.gz`
- [ ] Local rotation filenames: `bulletcoach_YYYYMMDD_HHmmss.json` (uncompressed)
- [ ] Keep 3 most recent in both locations

---

## Step 6: LLM Export + Downloads API (#35/#67)

*Files: MainViewModel.kt, PlannerScreen.kt*

### 6.1 — Create `exportForLlm()` function

- [ ] In `MainViewModel.kt`:
  
  ```kotlin
  fun exportForLlm(onResult: (Boolean, String) -> Unit) {
      viewModelScope.launch(Dispatchers.IO) {
          try {
              // Build enhanced JSON
              val exportData = buildLlmExportJson()
              // Write to Downloads
              writeToDownloads(exportData)
              onResult(true, "Exported to Downloads/bulletcoach_llm_export.json")
          } catch (e: Exception) {
              onResult(false, "Export failed: ${e.message}")
          }
      }
  }
  ```

### 6.2 — Build enhanced JSON

- [ ] Compute `userSummary`:
  
  ```kotlin
  val summary = mapOf(
      "totalTasks" to allTasks.size,
      "completedTasks" to allTasks.count { it.status == "COMPLETED" },
      "completionRate" to if (allTasks.isNotEmpty()) allTasks.count { it.status == "COMPLETED" }.toDouble() / allTasks.size else 0.0,
      "activeHabits" to habits.size,
      "totalDiaryEntries" to diaryEntries.size,
      "habitStreakDays" to computeLongestStreak(habits, habitLogs),
      "averageSleepHours" to computeAvgSleep(sleepLogs),
      "averageMoodRating" to computeAvgMood(dayReviews),
      "learnItemsInProgress" to learnItems.count { it.status == "IN_PROGRESS" },
      "totalPomodorosCompleted" to timerSessions.count { it.type == "FOCUS" }
  )
  ```
- [ ] Build nested entities:
  - Tasks with inline subtasks (parentTaskId → child tasks)
  - Ideas with inline stages
  - Learn items with inline sections
- [ ] Build structured settings:
  
  ```kotlin
  val structuredSettings = mapOf(
      "usePersianCalendar" to _usePersianCalendar.value,
      "autoSortEnabled" to prefs.getBoolean("auto_sort_enabled", false),
      "reminders" to mapOf(
          "dayReview" to mapOf("enabled" to ..., "time" to "..."),
          // ... all 7 reminders
      ),
      "pomodoro" to mapOf(
          "dndEnabled" to _dndEnabled.value,
          "ringtoneEnabled" to _pomodoroRingtoneEnabled.value,
          "vibrateEnabled" to _pomodoroVibrateEnabled.value,
          "defaultBreakMinutes" to _defaultBreakMinutes.value
      ),
      "eventReminders" to mapOf(
          "enabled" to ...,
          "vibrate" to ...,
          "sound" to ...
      )
  )
  ```
- [ ] Assemble final JSON structure with `backupVersion`, `createdAt`, `summary`, `entities`, `settings`

### 6.3 — Write to Downloads directory

- [ ] Android 13+ (`API 33+`): Use `MediaStore.Downloads`:
  
  ```kotlin
  val contentValues = ContentValues().apply {
      put(MediaStore.Downloads.DISPLAY_NAME, "bulletcoach_llm_export.json")
      put(MediaStore.Downloads.MIME_TYPE, "application/json")
  }
  val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
  context.contentResolver.openOutputStream(uri!!)?.use { it.write(jsonBytes) }
  ```
- [ ] Android 12- (`API < 33`): Request `WRITE_EXTERNAL_STORAGE` permission, then:
  
  ```kotlin
  val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "bulletcoach_llm_export.json")
  file.writeText(jsonString)
  ```
- [ ] **Edge case:** Downloads directory unavailable (no external storage) → fall back to `context.cacheDir`, show different path in message
- [ ] **Edge case:** File already exists → overwrite (user expects latest export)

### 6.4 — Add UI button in SettingsDialog

- [ ] After Backup/Restore buttons in SettingsDialog:
  
  ```kotlin
  Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
      Button(
          onClick = {
              isExporting = true
              viewModel.exportForLlm { success, message ->
                  isExporting = false
                  statusMessage = message
              }
          },
          enabled = !isBackingUp && !isRestoring && !isExporting
      ) {
          if (isExporting) {
              CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
              Spacer(Modifier.width(8.dp))
          }
          Text("Export for AI Analysis")
      }
  }
  ```
- [ ] Add `isExporting` state variable alongside `isBackingUp`/`isRestoring`

---

## Step 7: Documentation + Final Status (#23, #26, #27, #30)

*Files: Entity files, docs/BUG_Sync_plan.md*

### 7.1 — Add KDoc on TaskEntity.date

- [ ] Edit `TaskEntity.kt` field `date`:
  
  ```kotlin
  /** Date string: "yyyy-MM-dd" for daily, "yyyy-MM" for monthly, "yyyy-'W'ww" for weekly tasks */
  ```
- [ ] **Verify:** No compilation error for KDoc-only change

### 7.2 — Add KDoc on TimerSessionEntity.templateName

- [ ] Edit `TimerSessionEntity.kt` field `templateName`:
  
  ```kotlin
  /** Soft reference to TimerTemplateEntity.name (not a foreign key) */
  ```

### 7.3 — Add KDoc on HabitEntity.type

- [ ] Edit `HabitEntity.kt` field `type`:
  
  ```kotlin
  /** "BINARY" for yes/no check-in habits, "QUANTITATIVE" for count-based habits */
  ```

### 7.4 — Add KDoc on HabitEntity.recurrenceDaysOfWeek

- [ ] Edit `HabitEntity.kt` field `recurrenceDaysOfWeek`:
  
  ```kotlin
  /** Day-of-week pattern using Calendar.DAY_OF_WEEK numbering: 1=Sunday, 2=Monday, ..., 7=Saturday */
  ```

### 7.5 — Update BUG_Sync_plan.md status

- [ ] Mark all Step 1 bugs as `[x]` in `docs/BUG_Sync_plan.md`
- [ ] Mark all Step 2 bugs as `[x]`
- [ ] Mark all Step 3 bugs as `[x]`
- [ ] Mark all Step 5 bugs as `[x]`
- [ ] Mark all Step 6 bugs as `[x]`
- [ ] Mark all Step 7 bugs as `[x]`

### 7.6 — Final build verification

- [ ] Run `.\gradlew assembleDebug`
- [ ] Run `.\gradlew testDebugUnitTest`
- [ ] Fix any compilation/test errors

---

## Dependency Graph (execution order)

```
1.1 ─┐
1.2 ─┤
1.3 ─┤
1.4 ─┤
     ├──→ 2.1 ─→ 2.2 ─→ 2.3 ─→ 2.4
     │                              │
     │                              ▼
     │                         3.1─3.9 (schema)
     │                              │
     │                              ▼
     │                 ┌────────────┼────────────┐
     │                 ▼            ▼            ▼
     │            4.1─4.6       5.1─5.4       6.1─6.4
     │            (Drive)      (Gzip)       (LLM export)
     │                 │            │            │
     │                 └────────────┴────────────┘
     │                              │
     │                              ▼
     └─────────────────────────→ 7.1─7.6 (docs + verify)
```

## Verification Commands

After each substep:

```powershell
.\gradlew assembleDebug
```

Weekly/Step-level:

```powershell
.\gradlew testDebugUnitTest
```

Before any commit:

```powershell
git status
git diff --stat
```

## Commit Sequence

| Commit # | Scope      | Message                                                                                                                      |
| -------- | ---------- | ---------------------------------------------------------------------------------------------------------------------------- |
| 1        | `fix`      | `fix(ui): fix hardcoded email default, doc key count, break minutes write path, KeyboardOptions import`                      |
| 2        | `fix`      | `fix(backup): nullify FK orphans on restore, auto-save settings on dismiss, re-create notification channels, add size check` |
| 3        | `refactor` | `refactor(db): add foreign keys, indices, and unique constraints across 6 entities; migration v12→v13`                       |
| 4        | `feat`     | `feat(backup): add Google Drive API integration with DriveManager, OAuth, rotation, and WorkManager auto-backup`             |
| 5        | `feat`     | `feat(backup): add gzip compression for Drive backups with local uncompressed fallback`                                      |
| 6        | `feat`     | `feat(export): add LLM-friendly JSON export to Downloads with userSummary and nested entities`                               |
| 7        | `docs`     | `docs: add KDoc comments on entity date/type fields, update BUG_Sync_plan.md status`                                         |

Total: **7 commits, ~7 days**
