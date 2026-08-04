# MyPlanner (BulletCoach) — Comprehensive Recheck Plan

> Generated: 2026-07-19 (updated)
> Status: Based on 64 fixed + 1 partial + 3 remaining from `BUG_Sync_plan.md`

---

## APP ARCHITECTURE OVERVIEW

| Layer          | Details                                                                                             |
| -------------- | --------------------------------------------------------------------------------------------------- |
| **Module**     | Single `:app` module (Gradle 9.3.1, AGP 9.1.1, Kotlin 2.2.10)                                       |
| **DB**         | Room v30, 13 entities, `fallbackToDestructiveMigration()`                                           |
| **State**      | Single `MainViewModel` (5218 lines), 11 repositories, `StateFlow` → `collectAsState()`              |
| **Navigation** | Manual: 5 bottom tabs + `MoreScreen` with `sealed class MoreSubScreen` (only 4 tiles shown in code) |
| **AI**         | Firebase AI (Gemini) — key from `.env` via Secrets Gradle Plugin (deps commented out in build file) |
| **Backup**     | `BulletCoachBackup` data class → Google Drive (gzip JSON) + local fallback                          |

---

## EXECUTION APPROACH — 4 Phases, 15 Sections

| Phase                           | Sections | Focus                                                                |
| ------------------------------- | -------- | -------------------------------------------------------------------- |
| **1. Core & Infrastructure**    | 1-4      | Build config, manifest, DB, repos, ViewModel init, shared components |
| **2. Tab Screens**              | 5-8      | Planner, Habits, Timer, Stats — all CRUD + edge cases                |
| **3. MoreScreen & Activities**  | 9-11     | Grid, sub-screens, AlarmActivity, PomodoroFinishActivity             |
| **4. Background & Persistence** | 12-15    | Foreground service, reminders, backup/Drive/DND, settings, learn     |

**Deliverable**: Markdown report with ✅ PASS / ⚠️ WARNING / ❌ FAIL per check, plus any new issues found.

---

## PER-SECTION CHECKLIST TEMPLATE (applied to every section)

| Category         | What to Look For                                                                          |
| ---------------- | ----------------------------------------------------------------------------------------- |
| **Dead code**    | Unused imports, commented-out blocks, inaccessible composables/functions                  |
| **Crash bugs**   | NPE on nullable fields, index OOB in lists, permission denied (Android 14+), div by zero  |
| **Logical**      | Date boundary off-by-one, sort order inversion, StateFlow staleness, FK orphan after CRUD |
| **UI bugs**      | Recomposition flicker (`key = { it.id }`), dialog dismiss gap, keyboard overlap, clipping |
| **Edge cases**   | Empty data, null inputs, rapid clicks, date rollover, config change, Persian/Gregorian    |
| **Improvements** | Loading states, empty-state messages, accessibility labels, Material3 theming consistency |

---

## PHASE 1 — CORE & INFRASTRUCTURE

### Section 1 — Build Config & Manifest

| File                                            | What to Verify                                                                                                                                                                        |
| ----------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `app/build.gradle.kts`                          | Gradle 9.3.1 + AGP 9.1.1 + Kotlin 2.2.10 compatibility; minSdk 24 / targetSdk 36; remove `debugConfig` signing for debug build (per AGENTS.md)                                        |
| `settings.gradle.kts`                           | Repository urls correct, plugin resolution strategy                                                                                                                                   |
| `AndroidManifest.xml`                           | All permissions declared match actual usage; receiver `android:exported="true"` ok; `foregroundServiceType="specialUse"`; `showWhenLocked` + `turnScreenOn` on PomodoroFinishActivity |
| `res/values/strings.xml`                        | App name, no hardcoded user-facing strings without resource reference                                                                                                                 |
| `res/xml/backup_rules.xml`                      | Room DB path `bulletcoach_database` (no `.db`, no subdirectory) and SharedPrefs includes correct                                                                                      |
| `res/xml/data_extraction_rules.xml`             | Cloud-backup + device-transfer rules present                                                                                                                                          |
| Theme files (`Color.kt`, `Theme.kt`, `Type.kt`) | Dark mode support, color consistency, typography scale                                                                                                                                |
| `.env.example`                                  | Template exists with `GEMINI_API_KEY` placeholder                                                                                                                                     |

**Dead code**: commented Firebase AI, Camera, Coil, Navigation Compose, DataStore deps.

---

### Section 2 — Database & DAOs

| Area           | Files                                                    | Critical Checks                                                                                                                                                                                                                                                        |
| -------------- | -------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Schema**     | `AppDatabase.kt`, 19 entities                            | Version 30 matches all entities; no entity has stale fields not in DB; FK + Index annotations                                                                                                                                                                          |
| **Migrations** | `MIGRATION_28_29`, `MIGRATION_29_30` in `AppDatabase.kt` | MIGRATION_29_30 has 33 ALTER TABLE statements — all 17 entities get `updatedAt` + `isDeleted`; `fallbackToDestructiveMigration()` means no crash on mismatch but data loss                                                                                             |
| **DAOs**       | 15 DAO files                                             | All 15 have `deleteAll*()` suspend fun; all have `getAll*Sync()` variants for backup; correct `@Transaction` usage for parent+child deletes                                                                                                                            |
| **Indices**    | Entity annotations                                       | Composite UNIQUE on `HabitLogEntity(habitId, date)` and `IdeaStageEntity(ideaId, orderIndex)`; FK indices on `Task.parentTaskId`, `Task.linkedTodoId`, `Task.linkedIdeaId`, `Task.linkedLearnSectionId`, `Todo.linkedTaskId`, `Todo.parentTodoId`, `Idea.linkedTaskId` |

**Crash**: migration order wrong → `IllegalStateException`; **Edge**: `sqlite_sequence` reset after `DELETE FROM`; **Logical**: FK `SET_NULL` cascade works on delete.

---

### Section 3 — Repositories & ViewModel Init

| Area             | Lines in `MainViewModel.kt`                                       | Critical Checks                                                                  |
| ---------------- | ----------------------------------------------------------------- | -------------------------------------------------------------------------------- |
| **Repositories** | 11 files in `core/repository/`                                    | Thin wrappers over DAOs; correct `Flow`/`suspend` usage; no business logic leaks |
| **ViewModel**    | `MainViewModel.kt:192-534`                                        | StateFlow init order; `SharingStarted.WhileSubscribed(5000)` — cold start risk   |
| **Undo Stack**   | `_undoStack`, `UndoSnapshot` sealed class (lines 91-142, 217-218) | 10s expiry timer works; restore re-inserts FK children correctly                 |
| **DI**           | `MainActivity.kt:115-144`                                         | All 11 repos instantiated; factory pattern; Context passed correctly             |
| **Permissions**  | `PermissionsScreen.kt`, `MainActivity.kt:179`                     | Runtime permissions handled (POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM, etc.)     |

**Crash**: `.value` on cold StateFlow → empty default; NPE on null `context`; **Dead code**: `AppUsageItem` data class unused?; **Edge**: `prefs.getInt` with missing keys returns 0.

---

### Section 4 — Shared Components (10 files)

| File                         | Lines      | Used By                    | Critical Checks                                                                                             |
| ---------------------------- | ---------- | -------------------------- | ----------------------------------------------------------------------------------------------------------- |
| `TaskManagerDialog.kt`       | 1391       | PlannerScreen (FAB)        | NPE on null entity fields; subtask/event/note creation all 3 modes; recurrence/reminder/labels/links fields |
| `ActiveTimerWidget.kt`       |            | TimerScreen                | Timer display refresh; start/pause/reset state transitions                                                  |
| `CalendarDatePicker.kt`      |            | DiaryScreen, PlannerScreen | Persian/Gregorian toggle; date bounds                                                                       |
| `CalendarDatePickerDialog`   | (in above) |                            | Dialog lifecycle; onDateSelected callback with correct format                                               |
| `DayReviewCard.kt`           |            | MoreScreen                 | Star rating; slider; 4 text fields                                                                          |
| `FastPomodoroSetupDialog.kt` |            | TimerScreen                | Quick setup; focus/break duration pickers                                                                   |
| `HeaderActions.kt`           |            | Multiple screens           | Home + Settings button actions; navigation correctness                                                      |
| `LineChart.kt`               |            | StatsScreen                | Data binding; empty dataset; touch interaction; axis labels                                                 |
| `MottoCard.kt`               |            | MoreScreen                 | Random motto display; empty state                                                                           |
| `TimerSetupComponents.kt`    |            | TimerScreen                | Template CRUD; duration picker                                                                              |
| `UndoBar.kt`                 |            | MainActivity (all screens) | Countdown display; restore/dismiss actions; accessibility                                                   |

**Crash**: `TaskManagerDialog` index OOB on list iteration; **UI**: keyboard overlaps dialog fields; **Edge**: rapid FAB taps open multiple instances.

---

## PHASE 2 — TAB SCREENS

### Section 5 — PlannerScreen (Tab 0, ~7958 lines)

| Sub-Area             | Key Functions/Composables                                                 | Critical Checks                                                                                                   |
| -------------------- | ------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------- |
| **Date Navigation**  | `_selectedDate`, `_selectedMonth`, `_selectedYear`, FA/EN toggle          | Persian/Gregorian sync; month/year view boundaries; date rollover at midnight                                     |
| **Daily View**       | `DailyView()`, `TaskItem()`, `SubtaskItem()`                              | Drag-to-reorder (priority), expand/collapse, completion toggle, linked todo/idea indicators, subtask cascade      |
| **Weekly View**      | `WeeklyView()`, `WeekDayColumn()`                                         | Week boundaries anchored to Saturday (Persian); task distribution across days                                     |
| **Monthly View**     | `MonthView()`, `MonthDayCell()`                                           | Date grid accuracy; task dots; today highlight; Persian month names                                               |
| **Year Overview**    | `YearOverviewView()`                                                      | 12-month grid; density indicators; click navigates to month                                                       |
| **FAB Dialog**       | `TaskManagerDialog()`                                                     | Task/Event/Note creation; recurrence (daily/weekly/monthly); reminders (minutes before, night before)             |
| **Task CRUD**        | `createTask()`, `updateTask()`, `deleteTaskWithUndo()`                    | Subtask cascade delete; `linkedTodoId`/`linkedIdeaId` sync; undo stack push                                       |
| **Pending Review**   | `_pendingReviewTask`, `_pendingReviewSection`, `_pendingReviewLearnItem`  | Pomodoro completion → review flow; pomodoro → learn section review prompt                                         |
| **Ideas Tab**        | `IdeasTab()`, `IdeaGroupItem()`, `IdeaStageItem()`                        | Inline stages; drag reorder; linked task creation; group expand/collapse                                          |
| **Todo Tab**         | `TodoTab()`, `TodoItem()`, `SubTodoItem()`                                | Priority; two-way task linking (Task↔Todo); drag reorder; expand/collapse subtodos; description field             |
| **SettingsDialog**   | `PlannerScreen.kt:3545`                                                   | Persists 22+ settings (line 3545 defined, used from MoreScreen, DiaryScreen, PlannerScreen); auto-save on dismiss |
| **Restore Confirm**  | `AlertDialog` in PlannerScreen                                            | Shows before restore; prevents accidental data loss                                                               |
| **isSyncing Wiring** | Backup/Restore buttons disabled during sync + `CircularProgressIndicator` | Buttons disabled; spinner shown; dynamic success message                                                          |

**Crash**: drag reorder index OOB when items == 1; **Edge**: daily→weekly→monthly→yearly drill-down state preservation; **UI**: expand/collapse toggle persists across navigation (5 `_expandAll*` flags).

---

### Section 6 — HabitsScreen (Tab 1, ~1691 lines)

| Sub-Area           | Key Functions/Composables                         | Critical Checks                                                               |
| ------------------ | ------------------------------------------------- | ----------------------------------------------------------------------------- |
| **Habit List**     | `HabitItem()`, `HabitLogCalendar()`               | Check-in toggle; streak calculation; calendar heatmap; BINARY vs QUANTITATIVE |
| **Habit CRUD**     | `HabitDialog()`, `createHabit()`, `updateHabit()` | Reminder time; days of week; target value; unit; type selection               |
| **Habit Logs**     | `getLogsForHabit()`, `HabitLogDialog()`           | Manual log entry; notes; history view; date-bound uniqueness                  |
| **Sleep Tracking** | `SleepLogDialog()`, `sleepLogs` Flow              | Sleep/wake times; duration calc; quality rating                               |

**Crash**: streak calc on empty habit log list; **Edge**: `habitTime` null → no reminder scheduled; **UI**: calendar heatmap color intensity.

---

### Section 7 — TimerScreen (Tab 2, ~1879 lines)

| Sub-Area              | Key Functions/Composables                                      | Critical Checks                                                                  |
| --------------------- | -------------------------------------------------------------- | -------------------------------------------------------------------------------- |
| **Pomodoro**          | `pomodoroRunning`, `pomodoroSecondsLeft`, `activePomodoroTask` | Phase transitions (focus/ short break / long break); session count; auto-advance |
| **Chronometer**       | `chronoRunning`, `chronoElapsed`, `chronoPaused`               | Start/pause/resume/stop; lap times; save to history                              |
| **Templates**         | `TimerTemplateDialog()`, `timerTemplates` Flow                 | CRUD; default focus/break/target sessions; template deletion cascade             |
| **History**           | `TimerHistoryTab()`, `allSessions` Flow                        | Filter by date/type/task; stats aggregation; delete individual sessions          |
| **Completion Dialog** | `PomodoroCompletionState` → dialog                             | Task completion prompt; break navigation; session number display                 |

**Crash**: foreground service notification channel does not exist; **Edge**: timer running during config change (rotation); **UI**: seconds display accuracy.

---

### Section 8 — StatsScreen (Tab 3, ~2564 lines)

| Sub-Area           | Key Functions/Composables                            | Critical Checks                                                |
| ------------------ | ---------------------------------------------------- | -------------------------------------------------------------- |
| **Overview Cards** | Task completion rate, habit streaks, avg sleep, mood | Calculations correct; empty state (0 tasks/0 habits) handled   |
| **Charts**         | `LineChart.kt` composable                            | Data binding (empty list); touch interaction; date axis labels |
| **Habit Stats**    | `HabitStatsSection`                                  | Per-habit streak; completion %; calendar view                  |
| **Timer Stats**    | `TimerStatsSection`                                  | Pomodoro count; total focus time; per-task breakdown           |
| **Date Range**     | `DateRangePicker`                                    | Week/Month/Year/Custom; Persian calendar month support         |

**Crash**: division by zero when `totalTasks == 0` in completion rate; **Edge**: Persian calendar month names in chart labels.

---

## PHASE 3 — MORESCREEN & ACTIVITIES

### Section 9 — MoreScreen Grid + Sub-screens

| Screen              | File                            | Key Checks                                                                                                                                   |
| ------------------- | ------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| **MoreScreen Grid** | `MoreScreen.kt` (214 lines)     | **MISSING 2 tiles** — only 4 shown (Diary, Shop, Mottos, DayReview). Add Ideas and To-Do per AGENTS.md spec                                  |
| **Diary**           | `DiaryScreen.kt` (233 lines)    | Markdown preview? (code shows plain text); 300ms debounce auto-save; undo/redo not visible in code — verify; delete confirm; date navigation |
| **Shop List**       | `ShopListScreen.kt` (442 lines) | Quantity/price/purchased; filter tabs (All/To Buy/Purchased); total cost calculation                                                         |
| **Mottos**          | `MottoManagementScreen.kt`      | CRUD; random daily motto; enable/disable setting; empty state                                                                                |
| **Day Review**      | `DayReviewScreen.kt`            | 4 text fields; 5-star mood; 1-10 slider; save/load; prompt at 20:00                                                                          |
| **Ideas**           | *Inside PlannerScreen*          | Accessible from MoreScreen per AGENTS.md — verify navigation                                                                                 |
| **To-Do**           | *Inside PlannerScreen*          | Accessible from MoreScreen per AGENTS.md — verify navigation                                                                                 |

**Crash**: `null` motto when `mottoEnabled=true` but no mottos; **Edge**: DiaryScreen date navigation triggers auto-save then discards current edits.

---

### Section 10 — AlarmActivity

| File               | Critical Checks                                                                                                                                                               |
| ------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `AlarmActivity.kt` | Full-screen alarm: `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` on Android 14+; dismiss/snooze buttons work; ringtone/vibrate settings respected; configuration change survival |

---

### Section 11 — PomodoroFinishActivity

| File                        | Critical Checks                                                                                                              |
| --------------------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| `PomodoroFinishActivity.kt` | `showWhenLocked` + `turnScreenOn` flags (manifest); activity destroyed before user taps; completion options respect settings |

---

## PHASE 4 — BACKGROUND & PERSISTENCE

### Section 12 — TimerForegroundService

| File                        | Critical Checks                                                                                                                                                                                               |
| --------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `TimerForegroundService.kt` | Notification channel must exist before `startForeground()`; survives swipe-away (service restart); timer accuracy under doze mode; `isAppInForeground` flag correctly set in `MainActivity.onResume`/`onStop` |

---

### Section 13 — Reminders & Notifications

| Area                  | Files                                            | Critical Checks                                                                                 |
| --------------------- | ------------------------------------------------ | ----------------------------------------------------------------------------------------------- |
| **Event Reminders**   | `ReminderManager.kt`, `ReminderReceiver.kt`      | `scheduleReminders()`; `notifyNightBefore`; `reminderMinutesBefore`; exact alarms               |
| **Habit Reminders**   | `ReminderManager.scheduleHabitReminder()`        | Daily at habit time; respects `reminderEnabled` flag                                            |
| **Day Review Prompt** | `MainViewModel.checkAndTriggerDayReviewPrompt()` | 20:00 daily check; snackbar → DayReviewScreen overlay; broadcast receiver                       |
| **Boot/Time Change**  | `ReminderReceiver.onReceive()`                   | `ACTION_BOOT_COMPLETED`, `ACTION_TIME_SET`, `ACTION_TIMEZONE_CHANGED` → `rescheduleAllAlarms()` |
| **Pomodoro Alarms**   | `TimerForegroundService`, `AlarmActivity.kt`     | Full-screen intent; ringtone/vibrate settings; dismiss/snooze                                   |

**Edge**: DST transition — alarms shift correctly; **Crash**: `PendingIntent.FLAG_IMMUTABLE` on API 33+.

---

### Section 14 — Backup / Restore / Google Drive

| Area                | Files / Lines                                                | Critical Checks                                                                                                                                                                            |
| ------------------- | ------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **Backup**          | `MainViewModel.backupDataToGoogleDrive()` (lines 536-606)    | **⚠️ FIX NEEDED**: `BulletCoachBackup` (lines 144-160) MISSING `timerSessions` and `timerTemplates`. Backup function does not read them. **Bug #14/#42 marked fixed but NOT implemented.** |
| **Restore**         | `MainViewModel.restoreDataFromGoogleDrive()` (lines 608-756) | Restore clears `timer_sessions` + `timer_templates` (lines 664-665) but never fills them back. Data permanently lost on every restore.                                                     |
| **Drive Auth**      | `DriveManager.kt` (276 lines)                                | Silent sign-in; token refresh with retry; sign-out clears prefs; `PlayServices` check                                                                                                      |
| **BackupWorker**    | `BackupWorker.kt`                                            | 24h periodic; network constraint; unique work policy `KEEP`                                                                                                                                |
| **System Settings** | `SystemSettingsApplier.kt`                                   | Notification channel re-creation after restore; DND re-application deferred (needs permission)                                                                                             |
| **LLM Export**      | `MainViewModel.exportForLlm()` (lines 758-804)               | Downloads API (API 33+) + legacy fallback; structured JSON with summary + entities + settings                                                                                              |

**Remaining bugs to verify**:

| Bug                                   | Status                     | Impact                                                                                                                          |
| ------------------------------------- | -------------------------- | ------------------------------------------------------------------------------------------------------------------------------- |
| **#23** weekly date format `yyyy-Www` | Unresolved — document only | `isAllowedDay`/`nextAllowedDate` date parsing; verify no crash, just inaccurate                                                 |
| **#56** encryption                    | Future feature             | No sensitive data in backup — document as known limitation                                                                      |
| **#60** Moshi lenient mode            | Unresolved                 | `KotlinJsonAdapterFactory()` is lenient by default. Corrupted backup can partially parse. Add `failOnUnknown()` during restore. |

---

### Section 15 — Settings & Learn (Leitner System)

| Area                   | Location / Lines                                            | Critical Checks                                                                                                                                                             |
| ---------------------- | ----------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **SharedPreferences**  | `MainViewModel.kt:228-340` (38 keys)                        | All keys typed; defaults sensible; migration on restore; transient keys excluded (`reviewed_today`, `today_motto_date`, `today_motto_id`)                                   |
| **Calendar**           | `_usePersianCalendar`, `_persianYear`, `_persianMonth`      | Toggle persists from any screen (FA/EN buttons, SettingsDialog); `PersianCalendarHelper.kt` edge cases                                                                      |
| **Auto-sort**          | `_autoSortEnabled`                                          | Priority reorder respects postponed; `priorityLevel` mapping (High→1, Medium→2, Low→3, else→4)                                                                              |
| **Expand/Collapse**    | 5 `_expandAll*` settings                                    | Persist per-section; restore on navigation                                                                                                                                  |
| **Ringtone/Vibration** | `pomodoro_ringtone_uri`, `event_reminder_*`                 | URI resolution; fallback to default `RingtoneManager`                                                                                                                       |
| **Custom Labels**      | `_customLabels`, `loadCustomLabels()`                       | Serialization format `"label,id;label,id"`; parse edge cases                                                                                                                |
| **Learn / SR**         | `LearnItemEntity`, `LearnSectionEntity`, `LearnGroupEntity` | Status flow (NEW→IN_PROGRESS→REVIEW→MASTERED); `LEITNER_INTERVALS = [1,3,7,16,35,90]`; CONTINUOUS vs SCHEDULED mode; `studyTaskId`/`reviewTaskId` auto-create planner tasks |

**Edge**: `LEITNER_INTERVALS[5] = 90` — a review at day 90+ into the future; **Partial fix #48**: DND re-application deferred (needs user-granted `ACCESS_NOTIFICATION_POLICY`).

---

## CLARIFYING QUESTIONS (resolved by user)

| #   | Question                                                            | Answer                                        |
| --- | ------------------------------------------------------------------- | --------------------------------------------- |
| 1   | **MoreScreen tiles** — Code shows 4, AGENTS.md says 6. Add missing? | **Yes — add Ideas + To-Do tiles**             |
| 2   | **AI Coach** — Firebase AI deps commented out. Re-enable?           | **Removed intentionally** — no further action |
| 3   | **Test failure** — Robolectric SDK 36 fail. Investigate?            | **Accept** — pre-existing, not blocking       |
| 4   | **Signing config** — Remove `debugConfig` for debug builds?         | **Yes — remove from debug build type**        |
| 5   | **Persian calendar** — Known conversion bugs?                       | **Verify during recheck**                     |
| 6   | **Learn/Spaced Repetition** — Complete or experimental?             | **Verify during recheck**                     |

---

## PRE-FIX REQUIRED BEFORE RECHECK EXECUTION

**Bug #14/#42**: `BulletCoachBackup` data class must be updated to include `timerSessions` and `timerTemplates`. The backup function must collect them and the restore function must insert them. Without this fix, every restore destroys all timer history.

----

## WHAT YOU DON'T NEED TO CHECK ADDITIONALLY

Per your request, here's what's already covered by the above sections — no separate "dead code / crash / UI bug / edge case / improvement" pass is needed:

| Category                 | Covered In Section(s)                                                                                                                                         |
| ------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Dead code**            | All sections — every composable/function reachable from navigation entry points                                                                               |
| **Crash-causing bugs**   | DB migrations, FK nullification in restore, NPE guards in ViewModel (`.first()`, `?.let`), alarm exact permission checks                                      |
| **Logical issues**       | StateFlow initialization order, date boundary logic (Persian/Gregorian), recurrence calculations, Leitner intervals                                           |
| **UI bugs**              | Recomposition keys (`key = { it.id }`), lazy list item animations, dialog dismissal, keyboard handling                                                        |
| **Edge cases**           | Empty states (no tasks/habits), date rollover at midnight, backup file too large (>50MB), Drive signed-out mid-restore, alarm permission denied (Android 14+) |
| **UI improvements**      | Material3 theming consistency, accessibility (contentDescription), loading/skeleton states                                                                    |
| **Feature improvements** | Already tracked in `BUG_Sync_plan.md` (64 fixed, 1 partial, 3 remaining)                                                                                      |

---
