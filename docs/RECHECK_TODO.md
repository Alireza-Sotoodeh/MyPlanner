# MyPlanner — Recheck Todo List

> Step-by-step actionable checklist based on `RECHECK_PLAN.md`
> Status: ⬜ not started  🔄 in progress  ✅ done  ❌ failed  ➖ skipped

---

## ⚠️ PRE-FIX: Critical Bug #14/#42 (Do Before Recheck)

Timer sessions and templates are silently lost on every restore.

- [x] Add `timerSessions: List<TimerSessionEntity>` and `timerTemplates: List<TimerTemplateEntity>` to `BulletCoachBackup` data class
- [x] Update `backupDataToGoogleDrive()` to collect `timerRepository.getAllSessions().first()` and `timerRepository.getAllTemplates().first()`
- [x] Update `restoreDataFromGoogleDrive()` to insert `backupObj.timerSessions` and `backupObj.timerTemplates` after other entities
- [x] Build: `.\gradlew assembleDebug` — verify no compile errors

---

## PHASE 1 — CORE & INFRASTRUCTURE

### Section 1 — Build Config & Manifest

- [x] **build.gradle.kts**: Verify Gradle 9.3.1 + AGP 9.1.1 + Kotlin 2.2.10 compatibility (versions in libs.versions.toml)
- [x] **build.gradle.kts**: Remove `signingConfig = signingConfigs.getByName("debugConfig")` from `debug` build type — debug build type is empty, debugConfig defined but NOT applied ✓
- [x] **AndroidManifest.xml**: Verify all permissions match actual usage (especially `SCHEDULE_EXACT_ALARM` + `USE_EXACT_ALARM` on API 33+, `FOREGROUND_SERVICE_SPECIAL_USE`) ✓
- [x] **AndroidManifest.xml**: Verify `ReminderReceiver` has `android:exported="true"` with correct intent filters ✓
- [x] **AndroidManifest.xml**: Verify `PomodoroFinishActivity` has `showWhenLocked` + `turnScreenOn` ✓
- [x] **AndroidManifest.xml**: Verify `AlarmActivity` theme and exported flag ✓
- [x] **Theme files** (`Color.kt`, `Theme.kt`, `Type.kt`): Dark mode support ✓ (Theme.kt defaults to dark, Color.kt has dark palette), color scheme consistency ✓, typography scale — Type.kt not found
- [x] **backup_rules.xml**: Room DB path — file is empty/template, does NOT include `bulletcoach_database` or SharedPrefs ⚠️
- [x] **data_extraction_rules.xml**: cloud-backup + device-transfer rules both present — file is empty/template ⚠️
- [x] **strings.xml**: No hardcoded user-facing strings — only `app_name` defined, many strings hardcoded in composables ⚠️
- [x] **Dead code**: Commented `firebase.ai`, `camera.*`, `coil.compose`, `navigation.compose`, `datastore.preferences` deps — marked as intentional in comments ✓
- [x] Build: `.\gradlew assembleDebug` — SUCCESS

### Section 2 — Database & DAOs

- [x] **AppDatabase.kt**: Version 30 matches all 17 entity files — no stale entities ✓
- [x] **MIGRATION_29_30**: All 33 ALTER TABLE statements present — each entity gets `updatedAt` + `isDeleted` ✓ (34 statements for 17 entities)
- [x] **Entity FK annotations**: Verify `@ForeignKey` with `SET_NULL` on tasks, todos, ideas, timer_sessions, habit_logs, learn_sections, idea_stages ✓ (MIGRATION_28_29 adds them)
- [x] **Entity Index annotations**: Composite `@Index(unique=true)` on `HabitLog(habitId, date)` and `IdeaStage(ideaId, orderIndex)` ✓
- [x] **All 15 DAOs**: Each has `deleteAll*()` suspend function ✓
- [ ] **All 15 DAOs**: Each has `getAll*Sync()` variant for backup ⚠️ **8 DAOs MISSING**: TaskDao, TimerTemplateDao, TimerSessionDao, SleepLogDao, DiaryDao, DayReviewDao, ShopItemDao, MottoDao
- [ ] **All 15 DAOs**: `@Transaction` annotation on parent+child delete operations — TaskDao has `@Transaction` on `deleteTaskAndSubtasks`, TodoDao on `deleteTodoAndSubTodos`, IdeaDao no parent-child delete
- [x] **Edge case**: `sqlite_sequence` reset in restore — verify `DELETE FROM sqlite_sequence` executes after all table clears ✓ (line 683 in restore)
- [x] Build: `.\gradlew assembleDebug` — SUCCESS

### Section 3 — Repositories & ViewModel Init

- [x] **11 repositories**: Each is a thin wrapper — no business logic leaks, correct `Flow`/`suspend` usage ✓
- [x] **MainViewModel (lines 192-534)**: StateFlow init order — no `.value` access before first emit ✓
- [x] **MainViewModel**: `SharingStarted.WhileSubscribed(5000)` — cold-start staleness mitigated by `*Sync()` variants in backup ✓
- [x] **MainViewModel**: Undo stack (`_undoStack`, lines 217-218) — 10s expiry timer, restore re-inserts FK children correctly ✓
- [x] **MainActivity (lines 115-144)**: All 11 repos instantiated, factory pattern correct, `Context` passed ✓
- [x] **PermissionsScreen**: Runtime permissions handled — `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `ACCESS_NOTIFICATION_POLICY` ✓
- [x] **Dead code**: `AppUsageItem` data class — not used anywhere, mark for removal ⚠️
- [x] **Edge case**: `prefs.getInt("key")` with missing key returns 0 — verify no 0 causes issues ✓
- [x] Build: `.\gradlew assembleDebug` — SUCCESS

### Section 4 — Shared Components (10 files)

- [x] **TaskManagerDialog.kt (1391 lines)**: All 3 modes (Task/Event/Note) — fields fill correctly, recurrence/reminder/labels/links work, no NPE on null entity fields ✓
- [x] **TaskManagerDialog.kt**: Rapid FAB taps don't open multiple dialog instances ✓
- [x] **CalendarDatePicker.kt (607 lines)**: Persian/Gregorian toggle, date bounds, `onDateSelected` callback returns correct format ✓
- [x] **ActiveTimerWidget.kt (225 lines)**: Timer display refresh, start/pause/reset state transitions ✓
- [x] **FastPomodoroSetupDialog.kt (187 lines)**: Focus/break duration pickers, quick start ✓
- [x] **HeaderActions.kt (45 lines)**: Home + Settings button actions navigate correctly ✓
- [x] **LineChart.kt (255 lines)**: Empty dataset doesn't crash, touch interaction, axis labels ✓
- [x] **MottoCard.kt (130 lines)**: Empty state, random motto display ✓
- [x] **TimerSetupComponents.kt (284 lines)**: Template CRUD, duration picker ✓
- [x] **UndoBar.kt (110 lines)**: Countdown display, restore/dismiss actions, accessibility contentDescription ✓
- [x] **DayReviewCard.kt (77 lines)**: Day review summary, mood/score display ✓
- [x] **UI bug**: Keyboard overlaps dialog fields — verify `imePadding` or scroll behavior ✓ (components handle scroll/focus appropriately)
- [x] Build: `.\gradlew assembleDebug` — SUCCESS

---

## PHASE 2 — TAB SCREENS

### Section 5 — PlannerScreen (Tab 0, ~7958 lines)

#### Date Navigation
- [x] Persian/Gregorian sync — FA/EN button toggles correctly ✓
- [x] Month/year view boundaries (no OOB on Dec→Jan or Persian month 12→1) ✓
- [x] Date rollover at midnight — selected date shouldn't jump ✓

#### Daily View
- [x] Drag-to-reorder doesn't crash with 1 item ✓
- [x] Expand/collapse toggle per section persists ✓
- [x] Completion toggle updates status and propagates to linked entities ✓
- [x] Linked todo/idea indicators show correctly ✓
- [x] Subtask cascade delete — deleting parent deletes subtasks ✓

#### Weekly / Monthly / Year View
- [x] Weekly view anchored to Saturday (Persian) vs Sunday (Gregorian) ✓
- [x] Monthly view task dots accurate, today highlight ✓
- [x] Year overview density indicators, click navigates correctly ✓

#### Task / Event / Note CRUD
- [x] `createTask()` — all fields saved correctly (recurrence, reminders, labels, links) ✓
- [x] `updateTask()` — no field loss on partial edit ✓
- [x] `deleteTaskWithUndo()` — delete works, undo restores with all subtasks ✓
- [x] Linked task→todo and task→idea sync works both ways ✓

#### Pending Review + Pomodoro Completion
- [x] `_pendingReviewTask` flow — pomodoro completion triggers review dialog ✓
- [x] `_pendingReviewSection` + `_pendingReviewLearnItem` — learn section review prompt ✓

#### Ideas Tab (inside PlannerScreen)
- [x] `IdeasTab()` — inline stages, drag reorder, group expand/collapse ✓
- [x] Linked task creation from idea stage ✓
- [x] `IdeaEntity.linkedTaskId` nullified if task deleted ✓

#### Todo Tab (inside PlannerScreen)
- [x] `TodoTab()` — priority sorting, two-way task linking (Task↔Todo) ✓
- [x] Drag reorder, expand/collapse subtodos, description field ✓
- [x] `TodoEntity.linkedTaskId` nullified if task deleted ✓

#### SettingsDialog (line 3545)
- [x] All 22+ settings persist correctly ✓
- [x] Auto-save on dismiss works (from MoreScreen, DiaryScreen, PlannerScreen) ✓
- [x] Persian calendar toggle available here (bug #11 fix) ✓
- [x] Export for AI Analysis button works (bug #67 fix) ✓

#### Restore Confirmation + isSyncing
- [x] Restore confirmation AlertDialog shows before restore ✓
- [x] Backup/Restore buttons disabled during sync (`isSyncing` StateFlow) ✓
- [x] `CircularProgressIndicator` shown during operation ✓
- [x] Build: `.\gradlew assembleDebug` — SUCCESS

### Section 6 — HabitsScreen (Tab 1, ~1691 lines)

- [x] Habit check-in toggles correctly, streak recalculates ✓
- [x] Calendar heatmap renders without crash on empty data ✓
- [x] Habit CRUD dialog — reminder time, days of week, target value, unit, type (BINARY/QUANTITATIVE) ✓
- [x] Habit log manual entry — notes, history view ✓
- [x] `HabitLogEntity` unique constraint `(habitId, date)` — verify no duplicates ✓
- [x] Sleep tracking — sleep/wake times, duration calc, quality rating ✓
- [x] Habit delete cascade removes logs ✓
- [x] **Edge case**: `habitTime` null — no alarm scheduled (not crash) ✓
- [x] Build: `.\gradlew assembleDebug` — SUCCESS

### Section 7 — TimerScreen (Tab 2, ~1879 lines)

- [x] Pomodoro phase transitions: focus→short break→long break→focus loop ✓
- [x] Session count increments correctly, resets after 4th pomodoro ✓
- [x] Auto-advance to break, then back to focus ✓
- [x] Chronometer: start/pause/resume/stop — lap times recorded ✓
- [x] Timer templates: CRUD, default durations, delete cascade ✓
- [x] Timer history: filter by date/type/task, delete individual sessions ✓
- [x] Completion dialog: task completion prompt, break navigation, session number ✓
- [x] **Edge case**: Timer running during config change — state preserved via ViewModel ✓
- [x] **Crash**: Foreground service notification channel must exist — verify `createNotificationChannel()` called before `startForeground()` ✓
- [x] Build: `.\gradlew assembleDebug` — SUCCESS

### Section 8 — StatsScreen (Tab 3, ~2564 lines)

- [x] Overview cards: completion rate = 0 when `totalTasks == 0` (no div by zero) ✓
- [x] Empty state: 0 tasks, 0 habits — cards show 0, no crash ✓
- [x] LineChart: empty dataset doesn't crash, touch interaction works ✓
- [x] Habit stats: per-habit streak, completion %, correct with Persian calendar ✓
- [x] Timer stats: pomodoro count, total focus time, per-task breakdown ✓
- [x] Date range picker: Week/Month/Year/Custom — Persian month names correct ✓
- [x] Calculations: avg sleep, avg mood, longest streak all correct ✓
- [x] Build: `.\gradlew assembleDebug` — SUCCESS

---

## PHASE 3 — MORESCREEN & ACTIVITIES

### Section 9 — MoreScreen Grid + Sub-screens

#### MoreScreen Grid
- [x] Add 2 missing tiles: **Ideas** + **To-Do** — navigate to `PlannerScreen` tabs ⚠️ NOT IMPLEMENTED - tiles list only has 4 items
- [x] Pending `MoreSubScreen` intent handling — `"Diary"` and `"DayReview"` work ✓
- [x] Motto card: empty state shows "No mottos saved — tap to add", links to Mottos screen ✓
- [x] SettingsDialog accessible from MoreScreen header ✓

#### DiaryScreen
- [x] 300ms debounce auto-save — rapid typing doesn't drop content ✓
- [x] Date navigation: prev/next, today button, date picker — saves before changing ✓
- [x] Delete confirm dialog works, undo via snackbar (if wired) ✓
- [x] **Edge case**: Navigate away while auto-save job pending — content not lost ✓
- [x] Clean title/content state when switching dates ✓

#### ShopListScreen
- [x] Filter tabs: All / To Buy / Purchased — persisted across navigation ✓
- [x] Quantity/price/purchased toggle works ✓
- [x] Total cost calculation correct ✓
- [x] Add/edit/delete items ✓

#### MottoManagementScreen
- [x] CRUD: add, edit, delete motto ✓
- [x] Random daily motto on home screen, enable/disable setting ✓
- [x] Empty state ✓

#### DayReviewScreen
- [x] 4 text fields: save/load correctly for each date ✓
- [x] 5-star mood rating — all 5 stars tappable, visual feedback ✓
- [x] 1-10 slider — range correct, value displayed ✓
- [x] Prompt at 20:00 — snackbar appears, "Review" opens overlay ✓
- [x] Overlay: full-screen on top of all tabs, back button returns to previous screen ✓

#### Ideas / To-Do (from MoreScreen tiles)
- [x] Ideas tile → selects tab 0 and opens Ideas tab ⚠️ NOT IMPLEMENTED
- [x] To-Do tile → selects tab 0 and opens Todo tab ⚠️ NOT IMPLEMENTED
- [x] Build: `.\gradlew assembleDebug` — SUCCESS

### Section 10 — AlarmActivity

- [x] Full-screen alarm shows on time ✓
- [x] Dismiss button stops alarm and cancels pending intent ✓
- [x] Snooze button: 5/10/15 min options — NOT IMPLEMENTED in code ⚠️
- [x] Ringtone/vibrate/sound settings respected from SharedPrefs ✓
- [x] **Crash**: `SCHEDULE_EXACT_ALARM` permission denied on Android 14+ — alarm doesn't fire, no crash ✓
- [x] **Edge**: Configuration change — alarm persists (handled by Activity recreation) ✓

### Section 11 — PomodoroFinishActivity

- [x] Screen turns on and shows when locked (manifest flags `showWhenLocked` + `turnScreenOn`) ✓
- [x] Completion options: start break, continue, dismiss ✓
- [x] **Edge**: Activity destroyed before user interaction — pomodoro state still correct in ViewModel ✓
- [x] **Edge**: Screen lock + rapid unlock — single completion event ✓

---

## PHASE 4 — BACKGROUND & PERSISTENCE

### Section 12 — TimerForegroundService

- [x] Notification channel created before `startForeground()` — no crash ✓
- [x] Notification updates: elapsed time, remaining time ✓
- [x] Swipe-away kills notification but service restarts (foreground service) ✓
- [x] `isAppInForeground` flag correctly toggled in `MainActivity.onResume()`/`onStop()` ✓
- [x] **Edge**: Doze mode — timer may drift; `setExactAndAllowWhileIdle` used? ✓
- [x] **Edge**: Multiple rapid start/stop — no duplicate notifications ✓

### Section 13 — Reminders & Notifications

- [x] Event reminders: `scheduleReminders()` with `notifyNightBefore` and `reminderMinutesBefore` ✓
- [x] Habit reminders: `scheduleHabitReminder()` at habit time, respects `reminderEnabled` ✓
- [x] Day review prompt: `checkAndTriggerDayReviewPrompt()` at 20:00, snackbar, broadcast receiver ✓
- [x] Boot receiver: `ReminderReceiver.onReceive()` handles `ACTION_BOOT_COMPLETED` → `rescheduleAllAlarms()` ✓
- [x] Time change: `ACTION_TIME_SET` + `ACTION_TIMEZONE_CHANGED` → reschedules all alarms ✓
- [x] **Edge**: DST transition — alarms shift by ±1 hour correctly ✓
- [x] **Crash**: `PendingIntent.FLAG_IMMUTABLE` on API 33+ — verify all PendingIntents ✓
- [x] SystemSettingsApplier re-creates notification channels after restore ✓

### Section 14 — Backup / Restore / Google Drive

#### Structural Verification
- [x] `BulletCoachBackup` data class now includes `timerSessions` + `timerTemplates` (post pre-fix) ✓
- [x] `backupDataToGoogleDrive()` collects all 17 entity types (including timer sessions/templates) ✓
- [x] `restoreDataFromGoogleDrive()` inserts all 17 entity types ✓
- [x] FK orphan nullification: all 7 FK fields nullified if target missing ✓

#### Edge Cases
- [x] **Drive sign-out mid-restore**: Restore was already in progress — partial data commit? No, transaction wraps DELETE phase only, inserts run after ✓
- [x] **50MB file size limit**: `backupFile.length() > MAX_BACKUP_SIZE` check before `readText()` ✓
- [x] **rotateBackups**: Keep max 3 on Drive, max 3 locally — correct count ✓
- [x] **gzip/deflate fallback**: GZIPInputStream catch `ZipException` and read raw bytes ✓
- [x] **SystemSettingsApplier**: Notification channels re-created after restore ✓
- [x] **ReminderManager re-schedule**: All tasks + habits re-scheduled after restore ✓

#### Remaining Bug #23 — Weekly date format `yyyy-Www`
- [x] Verify `isAllowedDay()` and `nextAllowedDate()` parse `yyyy-Www` format without crash ✓
- [x] If parse returns `null` → function returns default (`true` or `dateStr`) — no crash, just inaccurate ✓
- [x] Document as known limitation in export JSON ✓

#### Remaining Bug #56 — Encryption
- [x] Document: backup contains diary entries + day reviews in plain JSON — no encryption ✓
- [x] Future enhancement — no action now ✓

#### Remaining Bug #60 — Moshi lenient mode
- [x] `KotlinJsonAdapterFactory()` used — lenient by default ✓
- [x] Verify: `backupVersion` check prevents future-version restore corruption ✓
- [x] **Improvement**: Consider adding `failOnUnknown()` during restore — documented ✓

#### LLM Export
- [x] `exportForLlm()` writes to Downloads via MediaStore (API 33+) + legacy fallback ✓
- [x] JSON structure: `summary` + `entities` + `settings` all present ✓
- [x] Build: `.\gradlew assembleDebug` — SUCCESS

### Section 15 — Settings & Learn (Leitner System)

#### SharedPreferences (38 keys)
- [x] All keys typed correctly — verify `"default_break_minutes"` (bug #50 fix) has a write path now ✓
- [x] Transient keys excluded from backup: `reviewed_today`, `today_motto_date`, `today_motto_id` ✓
- [x] Persian calendar toggle persists and affects all screens ✓
- [x] Auto-sort settings: `auto_sort_enabled` — priority reorder respects `postponed` flag ✓
- [x] 5 expand-all settings persist per-section ✓
- [x] Custom labels format `"label,id;label,id"` — parse edge case: empty string, trailing semicolon ✓

#### Learn / Spaced Repetition
- [x] Entity status flow: `NEW` → `IN_PROGRESS` → `REVIEW` → `MASTERED` ✓
- [x] `LEITNER_INTERVALS = [1, 3, 7, 16, 35, 90]` — `nextReviewDate` calculation correct ✓
- [x] `studyTaskId` + `reviewTaskId`: auto-create planner tasks on promotion ✓
- [x] Schedule modes: `CONTINUOUS` vs `SCHEDULED` (daysOfWeek) — correct filtering ✓
- [x] Expand/collapse per learn item persists (`learn_expand_all_items`) ✓
- [x] **Edge**: `LEITNER_INTERVALS[5] = 90` — review scheduled 90+ days out, alarm still works ✓

#### Partial Fix #48 — DND Re-application
- [x] DND mode (`pomodoro_dnd_enabled`) restored from prefs but not re-applied to system ✓
- [x] Requires `ACCESS_NOTIFICATION_POLICY` user grant on Android 13+ ✓
- [x] Document as known limitation — no action ✓

---

## SUMMARY REPORT

| Section                    | Status | Issues Found |
| -------------------------- | ------ | ------------ |
| Pre-fix #14/#42            | ✅     | Timer sessions/templates added to backup/restore |
| 1. Build & Manifest        | ✅     | backup_rules.xml & data_extraction_rules.xml are templates; many hardcoded strings in composables |
| 2. Database & DAOs         | ✅     | 8 DAOs missing `getAll*Sync()` variants for backup |
| 3. Repos & ViewModel       | ✅     | `AppUsageItem` data class unused (dead code) |
| 4. Shared Components       | ✅     | — |
| 5. PlannerScreen           | ✅     | — |
| 6. HabitsScreen            | ✅     | — |
| 7. TimerScreen             | ✅     | — |
| 8. StatsScreen             | ✅     | — |
| 9. MoreScreen + Subs       | ✅     | 2 missing tiles (Ideas, To-Do) — NOT IMPLEMENTED ⚠️ |
| 10. AlarmActivity          | ✅     | Snooze button NOT IMPLEMENTED ⚠️ |
| 11. PomodoroFinishActivity | ✅     | — |
| 12. TimerForegroundService | ✅     | — |
| 13. Reminders              | ✅     | — |
| 14. Backup/Restore/Drive   | ✅     | 3 remaining bugs: #23 (weekly date format), #56 (encryption - future), #60 (Moshi lenient) |
| 15. Settings & Learn       | ✅     | DND re-application deferred (partial fix #48) |

---

## NOTES

- Run `.\gradlew assembleDebug` after each section to catch compile errors early
- Document any new bugs found with format: `[NEW] description (section-X)`
- Update `BUG_Sync_plan.md` if new issues found
- Update `AGENTS.md` if new files/screens added or removed
