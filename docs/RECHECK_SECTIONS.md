# App Section Recheck Checklist

Use this file to systematically recheck every section of the app. Each checkbox corresponds to a self-contained functional area. An LLM agent should process this file top-to-bottom, verifying correctness of each section and marking `[x]` when verified.

---

## 1. Entry Point & Navigation

### 1.1 MainActivity — Lifecycle & Setup
- [x] `MainActivity.kt:101-367` — Activity lifecycle, ViewModel init with 11 repos, drive sign-in launcher, intent routing (open_day_review, pomodoro_action, navigate_to_tab, open_timer_subtab, open_date, open_more_screen), scheduleDailyBackup, onResume/onPause/onStop/onNewIntent, DAY_REVIEW_TRIGGERED BroadcastReceiver, day review snackbar overlay

### 1.2 Bottom Navigation Bar
- [x] `MainActivity.kt:362-428` — `AestheticNavigationBar` composable: 5 tabs (Planner/Habits/Timer/Stats/More), icon+label, selected tint, 1dp top border, 72dp height
- [x] `MainActivity.kt:430-434` — `NavigationItem` data class

### 1.3 Permissions Gate
- [x] `PermissionsScreen.kt:37-218` — `PermissionsScreen`: 5 permission items (Notifications/SDK 33+, Exact Alarms/SDK 31+, Usage Access/Optional, DND, Full-Screen Alerts/SDK 34+), CONTINUE button when all granted, lifecycle recheck on resume
- [x] `PermissionsScreen.kt:220-282` — `PermissionItem` reusable composable: icon, title, description, granted state, GRANT/SETTINGS button, CheckCircle when done

---

## 2. Planner Tab (Tab 0) — PlannerScreen.kt (7958 lines)

### 2.1 Root Composables
- [x] `PlannerScreen.kt:205-408` — `PlannerScreen`: tab state, 6-tab ScrollableTabRow (DAILY/WEEKLY/MONTHLY/TO-DO/IDEAS/LEARN), ActiveTimerWidget, FAB for tabs 0/3/4, SettingsDialog/FastPomodoroSetupDialog/pending completion AlertDialog/TaskManagerDialog/ReviewRatingSheet overlays, label filter state passing

### 2.2 Header
- [x] `PlannerScreen.kt:410-487` — `HeaderSection`: formatted date (Gregorian + Persian), day-of-week display, HeaderActions (home + settings icons)

### 2.3 Daily Planner View
- [ ] `PlannerScreen.kt:468-1204` — `DailyPlannerView`: label FilterChip row, day navigator (prev/next), tasks LazyColumn with active+completed tasks, drag-to-reorder with `detectDragGesturesAfterLongPress`, CalendarDatePickerDialog, TaskInteractionDialog, Pending Details Popup (priority/subtask breakdown)

### 2.4 Task Item Card
- [ ] `PlannerScreen.kt:1206-2189` — `BulletTaskItem`: bullet type indicator (•/○/–/□/—), checkbox, title, priority/label/subtask badges, expanded description, subtask list with drag-to-reorder, pomodoro trigger button, 3-dot DropdownMenu (Edit/Migrate/Reschedule/Move to To-Do/Turn into Idea/Delete), subtask selector dialog, reschedule dialogs (Gregorian DatePickerDialog + Persian manual input)

### 2.5 Weekly Planner
- [ ] `PlannerScreen.kt:2191-2502` — `WeeklyPlannerView`: independent week anchor, week navigator, calendar toggle (EN/FA), LazyColumn of day cards, each lists top-level tasks with bullet indicators

### 2.6 Year Overview
- [ ] `PlannerScreen.kt:2504-2515` — `YearOverviewView`: dispatches to Gregorian/Persian based on `usePersianCalendar`
- [ ] `PlannerScreen.kt:2517-2719` — `GregorianYearOverviewView`: year navigation, 12-month grid with PEND/DONE counts, click selects month
- [ ] `PlannerScreen.kt:2721-2906` — `PersianYearOverviewView`: same for Persian calendar using `PersianCalendarHelper`

### 2.7 Monthly Planner
- [ ] `PlannerScreen.kt:2910` — `MonthFilter` enum (TOTAL/PENDING/DONE)
- [ ] `PlannerScreen.kt:2910-3204` — `MonthlyPlannerView`: month navigation, card with month intentions list, filterable by TOTAL/PENDING/DONE, each row shows title/target date/status badge

### 2.8 Task Interaction Dialog
- [ ] `PlannerScreen.kt:3206-3482` — `TaskInteractionDialog`: mark complete, log time (start/end time pickers + manual duration), start pomodoro timer, 2 inline AlertDialogs for time pickers

### 2.9 Utility Functions
- [ ] `PlannerScreen.kt:3485-3541` — `getRelativeDayString`, `getOffsetDateString`, `getOffsetMonthString`, `getDaysOfWeek`

### 2.10 Settings Dialog
- [ ] `PlannerScreen.kt:3543-4412` — `SettingsDialog` (ModalBottomSheet): Cloud & Sync (Google Drive backup/restore), Export for AI Analysis, Timer & Focus (DND, pomodoro sound/vibrate/ringtone), 7 Daily Reminders with time pickers (Review/Sleep/Diary/Planner/Habits/Tomorrow Planner/Learn Review), Event Notifications toggle, More Screen toggle, cancel-confirmation dialog, restore confirm dialog, 7 TimePicker AlertDialogs

### 2.11 Settings Helper Composables
- [ ] `PlannerScreen.kt:4414-4432` — `SettingsCard` reusable card wrapper
- [ ] `PlannerScreen.kt:4434-4489` — `ReminderItem`: icon, title, subtitle, switch toggle, animated time/test button
- [ ] `PlannerScreen.kt:4491-4497` — `ReminderDivider`

### 2.12 Todo Tab
- [ ] `PlannerScreen.kt:4499` — `TodoTabFilter` enum (ALL/PENDING/DONE/LINKED/UNLINKED)
- [ ] `PlannerScreen.kt:4502-4951` — `TodoTab`: filter chips, sortable/draggable LazyColumn, edit/delete/link/unlink/move-to-planner dialogs, pending sub-todo completion dialog, break down popup
- [ ] `PlannerScreen.kt:4953-5280` — `TodoItem`: checkbox/icon, title, priority badge, subtask progress, link/unlink icon, 3-dot DropdownMenu (Edit/Unlink or Schedule/Move to Planner/Delete), expandable description, subtask list
- [ ] `PlannerScreen.kt:5282-5301` — `PriorityBadge`: colored label (High=red, Low=green, Medium=orange)

### 2.13 Todo → Planner Dialogs
- [ ] `PlannerScreen.kt:5304-5378` — `LinkToPlannerDialog`: schedule todo as planner task on chosen date
- [ ] `PlannerScreen.kt:5380-5459` — `MoveToPlannerDialog`: move todo+sub-todos to planner as task
- [ ] `PlannerScreen.kt:5461-5483` — `LinkedDeleteConfirmDialog`: delete both or keep (unlink)
- [ ] `PlannerScreen.kt:5485-5505` — `DeleteConfirmDialog`: generic delete

### 2.14 Ideas Tab
- [ ] `PlannerScreen.kt:5508-5884` — `IdeasTab`: group filter chips (GroupChipRow), sortable/draggable LazyColumn, group CRUD, idea editing (via TaskManagerDialog), idea deletion, Add-to-Planner dialog, breakdown popup
- [ ] `PlannerScreen.kt:5886-5923` — `GroupChipRow`: horizontal LazyRow of group filter chips
- [ ] `PlannerScreen.kt:5925-5997` — `LearnGroupChipRow`: status filter + group chips
- [ ] `PlannerScreen.kt:5999-6052` — `CreateLearnGroupDialog`: name + color picker
- [ ] `PlannerScreen.kt:6054-6203` — `IdeaCard`: lightbulb icon, title, priority badge, "Add to Planner" button, 3-dot DropdownMenu (Edit/Add to Planner/Delete), expandable description, stages list
- [ ] `PlannerScreen.kt:6205-6301` — `StageRow`: sequential completion toggle (only if previous done), 2-item DropdownMenu (Mark Done/Undone, Delete)
- [ ] `PlannerScreen.kt:6303-6357` — `CreateGroupDialog`: name + color picker for idea groups
- [ ] `PlannerScreen.kt:6359-6481` — `AddToPlannerDialog`: add idea/stage as TASK/EVENT/NOTE

### 2.15 Learn Tab
- [ ] `PlannerScreen.kt:6483-6886` — `LearnTab`: status/group filter chips, sortable/draggable LazyColumn, FAB for create, learn item CRUD, start learning dialog, popup breakdown, delete group confirmation
- [ ] `PlannerScreen.kt:6888-7299` — `LearnItemCard`: book icon, type/group/priority badges, status, progress bar, section breakdown, today's pending tasks, action buttons (Start/Pause/Resume/Edit/Delete), long-press DropdownMenu
- [ ] `PlannerScreen.kt:7301-7587` — `LearnItemDialog`: create/edit (title, type BOOK/COURSE, sections count, priority, group), 3 inline dialogs (new group, edit group, delete group confirm)
- [ ] `PlannerScreen.kt:7589-7852` — `StartLearningDialog`: daily/weekly schedule, sections/day or deadline, start date, `daysBetweenDates`, `countAllowedDaysBetween`
- [ ] `PlannerScreen.kt:7854-7904` — `ReviewRatingSheet`: ModalBottomSheet with Easy/Medium/Hard buttons
- [ ] `PlannerScreen.kt:7906-7952` — `DatePickerField`: reusable field with DatePickerDialog
- [ ] `PlannerScreen.kt:7954-7958` — `LabelInfo` data class

---

## 3. Habits Tab (Tab 1)

- [ ] `HabitsScreen.kt:101-1691` — `HabitsScreen`: tab row (Habits/Sleep), date navigator, FAB, add/edit habit dialog (name, type, target, unit, reminder time), habit list with check-in calendar, sleep log section with add/edit dialog

---

## 4. Timer Tab (Tab 2)

- [ ] `TimerScreen.kt:47-1879` — `TimerScreen`: 3 sub-tabs (Pomodoro/Cronometer/History), task selector dropdown, template selector, focus/break time inputs, active timer display with controls, manage templates dialog, history date range filter, chronometer summary dialog, settings dialog, inline template CRUD AlertDialogs

---

## 5. Stats Tab (Tab 3)

- [ ] `StatsScreen.kt:81-2564` — `StatsScreen`: yearly/monthly toggle, line/bar charts (LineChartCanvas), task completion stats, habit check-in calendar, pomodoro sessions, sleep logs, daily breakdown, screen time (UsageStatsManager), mood/motto stats, app usage top N

---

## 6. More Tab (Tab 4) & Sub-Screens

### 6.1 More Screen Root
- [ ] `MoreScreen.kt:24-43` — `MoreSubScreen` sealed class (Diary/ShopList/Mottos/DayReview/None), `MoreTile` data class, `tiles` list (4 tiles)
- [ ] `MoreScreen.kt:46-214` — `MoreScreen`: motto card (if enabled), 3×2 LazyVerticalGrid, sub-screen routing, pendingMoreScreen deep-link handling
- [ ] `MoreScreen.kt:179-212` — `MoreTileItem`: single grid tile with icon + label

### 6.2 Diary
- [ ] `DiaryScreen.kt:37-233` — `DiaryScreen`: date navigator, title+content markdown editor, auto-save 300ms debounce, undo/redo via saveDiaryEntry, CalendarDatePickerDialog, delete with confirmation, formatDisplayDate helper

### 6.3 Shop List
- [ ] `ShopListScreen.kt:28-442` — `ShopListScreen`: 3 filter chips (ALL/TO_BUY/PURCHASED), add/edit dialog (name, quantity, price, note), purchased toggle with strikethrough, delete confirmation, SettingsDialog

### 6.4 Mottos
- [ ] `MottoManagementScreen.kt:25-294` — `MottoManagementScreen`: LazyColumn, add/edit dialog (text + author), delete confirmation, SettingsDialog, inline MottoListItem

### 6.5 Day Review
- [ ] `DayReviewScreen.kt:28-548` — `DayReviewScreen`: 4 text fields (good/bad/improve/gratitude), 5-star mood rating, 0-10 score slider, notes field, daily stats summary (completed/total tasks, habits checked, pomodoro count, hours slept, has diary), save with auto-calc stats

---

## 7. Alarm & Pomodoro Activities

- [ ] `AlarmActivity.kt:34-184` — `AlarmActivity`: ringtone (looping) + vibrator (waveform), "Understood" button, "Snooze 5 min" button, snooze scheduling via ReminderReceiver SNOOZE_ALARM action, lifecycle cleanup
- [ ] `PomodoroFinishActivity.kt:59-411` — `PomodoroFinishActivity`: ringtone + vibrator + audio focus, `PomodoroFinishContent` (phase icon, session info, task name, Continue/End buttons, animated entry), `PomodoroTestContent` (test mode with dismiss), 5-min safety auto-stop

---

## 8. Reusable Components

- [ ] `ActiveTimerWidget.kt:48-225` — `ActiveTimerWidget`: floating timer indicator with pause/resume/stop, time display, phase indicator
- [ ] `CalendarDatePicker.kt:51-607` — `CalendarDatePickerDialog`: Gregorian + Persian calendar, month/year navigation, day grid, today highlight, date range calculation helpers
- [ ] `DayReviewCard.kt:1-74` — `DayReviewCard`: compact mood + score display card
- [ ] `FastPomodoroSetupDialog.kt:1-174` — `FastPomodoroSetupDialog`: quick pomodoro start with task/focus/break settings
- [ ] `HeaderActions.kt:1-43` — `HeaderActions`: home icon + settings icon row
- [ ] `LineChart.kt:1-239` — `LineChartCanvas`, `LineChartLine`, `BarChart`, `LegendItem`: custom canvas chart components
- [ ] `MottoCard.kt:1-127` — `MottoCard`: animated quote card with fade transitions
- [ ] `TaskManagerDialog.kt:60-1391` — `TaskManagerDialog`: unified create/edit for TASK/EVENT/NOTE/TODO/IDEA — title, description, type selector, date, priority, label, recurrence (NONE/DAILY/WEEKLY/MONTHLY/YEARLY/WEEKDAY with 52-week expansion), subtask list, event time, notes, event reminders (night before, X min before), validation, preview
- [ ] `TimerSetupComponents.kt:36-284` — `TemplateSelector` (template cards with check), `TimeInput` (colon-separated mm:ss), `PresetChip`
- [ ] `UndoBar.kt:1-105` — `UndoBar`: animated snackbar with countdown timer, Restore + Dismiss buttons

---

## 9. ViewModel — MainViewModel.kt (5274 lines)

### 9.1 Data Classes & Types
- [ ] `MainViewModel.kt:85-191` — `AppUsageItem`, `UndoSnapshot` (10 subtypes: Task/Todo/Idea/IdeaToTask/Habit/Diary/DayReview/TimerTemplate/TimerSession/ShopItem/Motto/LearnItem), `UndoEntry`, `BulletCoachBackup` (17 entity lists), `PendingTaskCompletion`, `PendingSubTodoCompletion`, `PomodoroCompletionState`

### 9.2 Constructor & State
- [ ] `MainViewModel.kt:194-583` — Class declaration with 11 repo params + Context, moshi/moshiStrict/prefs fields, tab StateFlow, 13 setting StateFlows, companion (HEARTBEAT_PATTERN, LEITNER_INTERVALS), date/week helpers, pending review StateFlows, pomodoro/chrono StateFlows, timer StateFlows, settings update functions (loadCustomLabels, updateCustomLabels, updateDefaultBreakMinutes, updateGoogleDriveConnected, etc.), pending drive sign-in intent

### 9.3 Backup & Restore
- [ ] `MainViewModel.kt:586-661` — `backupDataToGoogleDrive()`: collect all entities from 11 repos → BulletCoachBackup → moshi JSON → save local gzip → upload to Drive via DriveManager, max 3 backups
- [ ] `MainViewModel.kt:663-812` — `restoreDataFromGoogleDrive()`: download from Drive (gzip handling) or local fallback → parse with moshiStrict → clear 14 DB tables in transaction → restore FK-safe order → nullify orphaned FK refs → reapply system settings → reschedule all event/habit alarms
- [ ] `MainViewModel.kt:814-1066` — `exportForLlm()`: collect all data → buildSummary + buildEntitiesJson + buildStructuredSettings → write to Downloads via MediaStore (API 33+) / ExternalStorage, computeLongestStreak, computeAvgSleep, computeAvgMood

### 9.4 Date Navigation
- [ ] `MainViewModel.kt:1068-1151` — selectedDate/Month/Year, Persian calendar, dateChangeReceiver (ACTION_DATE_CHANGED, ACTION_TIME_CHANGED, ACTION_TIMEZONE_CHANGED), refreshSystemDate() polling

### 9.5 Query StateFlows
- [ ] `MainViewModel.kt:1153-1211` — dailyTasks, monthlyTasks, yearTasks, persianMonthTasks, persianYearTasks, allTasks, habits, habitLogs, todayHabitLogs, allHabitLogs, allSleepLogs, sleepLog

### 9.6 Pomodoro & Chronometer State
- [ ] `MainViewModel.kt:1213-1362` — 11 pomodoro StateFlows (activeTask, secondsLeft, running, phase, currentSession, targetSessions, break/focus minutes, etc.), 6 chronometer StateFlows (elapsed, running, paused, selectedTaskId, timerServiceJob, processedCompletion), timer Templates/Sessions StateFlows, history date range + selected date + filtered sessions

### 9.7 Task & Sub-Todo Operations
- [ ] `MainViewModel.kt:1364-1512` — `confirmCompleteTask` (task+subtask completion, linked-todo sync, pomodoro logging), `cancelPendingTaskCompletion`, `confirmCompleteTodoWithSubtodos`, `cancelPendingSubTodoCompletion`, `addSubTodo`, `toggleSubTodoCompletion`, `deleteSubTodo`

### 9.8 Entity State & Reminder Times
- [ ] `MainViewModel.kt:1534-1697` — ideaGroups, allIdeas, pendingTodos, allTodos, diaryDates, shopItems, mottos, learnItems, learnGroups, 7 reminder time/enabled StateFlows, deep-link pending more screen, day review prompt state

### 9.9 Notification Channels & Alarm Scheduling
- [ ] `MainViewModel.kt:1699-1786` — DayReview notification channel + alarm schedule/cancel/send-immediate
- [ ] `MainViewModel.kt:1788-1836` — 6 more channel creators (Sleep/Diary/Planner/Habits/TomorrowPlanner/LearnReview)
- [ ] `MainViewModel.kt:1838-2118` — `scheduleReminderAlarm`/`cancelReminderAlarm` helpers, 7 pairs of schedule/cancel wrappers, 7 immediate notification senders (fetch data: today's tasks, missed habits, tomorrow's count)
- [ ] `MainViewModel.kt:2120-2199` — Day Review prompt (dismiss/check+trigger), `init` block (BroadcastReceiver registration, 15s polling, 3 mock habits + 3 mock tasks seeding if DB empty, motto init, timer service state collection)

### 9.10 Date Navigation Methods
- [ ] `MainViewModel.kt:2230-2272` — `selectDate`, `selectMonth`, `selectYear`, `toggleUsePersianCalendar`, `navigateMonth` (supports Persian), `selectPersianMonth`

### 9.11 Task CRUD
- [ ] `MainViewModel.kt:2274-2288` — `addTask`: creates with up to 52-week recurrence generation, subtasks, EVENT reminder scheduling
- [ ] `MainViewModel.kt:2390-2493` — `updateTaskWithSubtasks`: update fields, sync linked todo title, re-schedule/cancel EVENT alarms, sync subtasks + linked todo sub-todos
- [ ] `MainViewModel.kt:2495-2526` — `updateTask` (simple), `toggleTaskCompletion` (subtask confirmation, linked-todo sync, handleLearnTaskToggle)
- [ ] `MainViewModel.kt:2528-2560` — `deleteTask` (cancel EVENT reminders, nullify linkedTodoId, delete+subtasks), `deleteTaskWithUndo`
- [ ] `MainViewModel.kt:2562-2618` — `completeTaskWithManualDuration` (start/end time, pomodoro session, linked todo sync)
- [ ] `MainViewModel.kt:2620-2654` — `migrateTask` (move to new date, sync learn-linked sections)

### 9.12 Habit & Tracker CRUD
- [ ] `MainViewModel.kt:2656-2748` — `addHabit` (with reminder), `updateHabit`, `deleteHabit/deletedHabitWithUndo`, `logHabit` (insert/update/delete), `deleteHabitLog`
- [ ] `MainViewModel.kt:2750-2771` — `saveSleepLog/deleteSleepLog`

### 9.13 Pomodoro Management
- [ ] `MainViewModel.kt:2773-2830` — `startPomodoro`: update task, set StateFlows, DND enable, TimerForegroundService intent
- [ ] `MainViewModel.kt:2831-2855` — `pausePomodoro/resumePomodoro/stopPomodoroEarly/discardPomodoro`
- [ ] `MainViewModel.kt:2857-2879` — `resetPomodoroState/resetPomodoro` (restore DND), `adjustPomodoroPlusOne`
- [ ] `MainViewModel.kt:2881-3016` — `collectTimerServiceState` (map service state to StateFlows), `handlePhaseCompletion` (log session, auto-complete task, set PomodoroCompletionState)
- [ ] `MainViewModel.kt:3018-3100` — `setCompletionStateAndNotify`, `continueFromPomodoroCompletion` (start next phase), `endPomodoroChain` (restore DND), `handlePomodoroAction`, `testPomodoroAlarm`

### 9.14 Chronometer
- [ ] `MainViewModel.kt:3119-3201` — `startChronometer` (with DND), `pauseChronometer`, `stopChronometer`, `saveChronometerSession`, `discardChronometer`, `resetChronometer`, `adjustChronoMinusOne`

### 9.15 Timer Templates & Sessions
- [ ] `MainViewModel.kt:3203-3280` — Template CRUD (create/update/delete/deleteWithUndo), Session CRUD (update/delete/deleteWithUndo/addManualSession)
- [ ] `MainViewModel.kt:3282-3304` — `markTaskCompleteFromTimer`
- [ ] `MainViewModel.kt:3306-3371` — `firePomodoroCompletionNotification` (full-screen notification → PomodoroFinishActivity), `cancelPomodoroNotification`

### 9.16 Usage Stats & Permissions
- [ ] `MainViewModel.kt:3373-3446` — `updateAppUsage`: query UsageStatsManager for today, top 6 apps, total, cache labels in ConcurrentHashMap
- [ ] `MainViewModel.kt:3449-3552` — Permission check functions (notification/exact alarm/all required/usage stats/DND/full-screen intent), DND save/restore

### 9.17 Task Reorder
- [ ] `MainViewModel.kt:3554-3577` — `reorderTask`: drag-to-reorder with subtask promotion support

### 9.18 Idea CRUD
- [ ] `MainViewModel.kt:3579-3738` — `stagesForIdea`, Group CRUD (add/update/delete with ungroup), `addIdea` (with stages, sort order), `triggerReorderIdeasByPriority`, `reorderIdea`, `updateIdea` (sync stages), `deleteIdea/deletedIdeaWithUndo`, `moveIdeaToGroup`, Stage CRUD (add/update/delete)
- [ ] `MainViewModel.kt:3740-3801` — `addIdeaToPlanner` (create task tree, delete original, push IdeaToTaskSnapshot), `addStageToPlanner`

### 9.19 Todo CRUD
- [ ] `MainViewModel.kt:3803-4231` — `addTodo` (with sub-todos, sort order), `reorderTodo`, `triggerReorderTodosByPriority`, `updateTodo/updateTodoWithSubtodos` (sync linked task sub-todos), `deleteTodo/deletedTodoWithUndo/unlinkAndDeleteTodoWithUndo`, `toggleTodoCompletion` (sub-todo prompt, linked-task auto-complete), `linkTodoToTask/unlinkTodoFromTask`, `moveTaskToTodo/turnNoteIntoIdea/moveTodoToTask`

### 9.20 Learn CRUD
- [ ] `MainViewModel.kt:4233-4270` — `addLearnItem` (auto-generate sections)
- [ ] `MainViewModel.kt:4272-4316` — `updateLearnItem` (regenerate sections if none started)
- [ ] `MainViewModel.kt:4318-4352` — `deleteLearnItem/deletedLearnItemWithUndo`
- [ ] `MainViewModel.kt:4354-4441` — `applyLearningAlgorithm`: create study tasks with CONTINUOUS/WEEKLY scheduling, deadline calculation
- [ ] `MainViewModel.kt:4443-4617` — `pauseLearnItem` (delete pending tasks), `resumeLearnItem` (re-create study/review tasks with Leitner intervals)
- [ ] `MainViewModel.kt:4619-4773` — `completeReviewWithRating` (HARD/EASY/MEDIUM → Leitner stage advancement), `dismissReviewRating`, `handleLearnTaskToggle` (Study→Review on complete, undo), `checkLearnItemCompletion`, date helpers

### 9.21 Diary/Shop/Motto/Day Review CRUD
- [ ] `MainViewModel.kt:4775-4806` — Diary: `diaryAllDates`, `diaryEntryForDate`, `saveDiaryEntry`, `deleteDiaryEntry/deletedEntryWithUndo`
- [ ] `MainViewModel.kt:4808-4836` — Shop: `addShopItem`, `updateShopItem`, `deleteShopItem/deletedItemWithUndo`, `toggleShopItemPurchased`
- [ ] `MainViewModel.kt:4838-4869` — Motto: `addMotto`, `updateMotto`, `deleteMotto` (refresh todayMotto if deleted), `deleteMottoWithUndo`
- [ ] `MainViewModel.kt:4871-4900` — Day Review: `reviewForDate`, `saveDayReview` (4 fields + mood + score, marks reviewed_today), `deleteDayReview/deletedDayReviewWithUndo`

### 9.22 Reminder Setting Updates
- [ ] `MainViewModel.kt:4902-5043` — 7 reminders × (updateTime + updateEnabled): Review, Sleep, Diary, Planner, Habits, Tomorrow Planner, Learn Review (with HH:mm normalization)

### 9.23 Undo System
- [ ] `MainViewModel.kt:5045-5236` — Helper utilities, `dismissUndo`, `restoreFromUndo` (large when block covering 10 snapshots with ID remapping, FK re-link, alarm reschedule, learn ID maps), `pushUndo` (5s expiry, auto-removal)

### 9.24 Lifecycle & Factory
- [ ] `MainViewModel.kt:5238-5274` — `onCleared` (unregister dateChangeReceiver), `MainViewModelFactory` (11 repos + Context)

---

## 10. Database Layer

### 10.1 AppDatabase
- [ ] `AppDatabase.kt:42-240` — 17 entities listed, version 30, exportSchema=true, 9 migrations (16→17, 17→18, 23→24, 24→25, 25→26, 26→27, 27→28, 28→29, 29→30), singleton with `fallbackToDestructiveMigration()`

### 10.2 Entities (17)
- [ ] `entity/TaskEntity.kt` — task fields (title, description, date, status, type, duration, priority, label, recurrence, eventTime, linkedTodoId, linkedIdeaId, linkedLearnSectionId, parentTaskId, postponed, etc.)
- [ ] `entity/HabitEntity.kt`
- [ ] `entity/HabitLogEntity.kt`
- [ ] `entity/SleepLogEntity.kt`
- [ ] `entity/TimerSessionEntity.kt`
- [ ] `entity/TimerTemplateEntity.kt`
- [ ] `entity/IdeaGroupEntity.kt`
- [ ] `entity/IdeaEntity.kt`
- [ ] `entity/IdeaStageEntity.kt`
- [ ] `entity/TodoEntity.kt`
- [ ] `entity/DiaryEntryEntity.kt`
- [ ] `entity/ShopItemEntity.kt`
- [ ] `entity/MottoEntity.kt`
- [ ] `entity/DayReviewEntity.kt`
- [ ] `entity/LearnGroupEntity.kt`
- [ ] `entity/LearnItemEntity.kt`
- [ ] `entity/LearnSectionEntity.kt`
- [ ] `entity/LearnConstants.kt` / `entity/LearnStatus.kt`

### 10.3 DAOs (15)
- [ ] `dao/TaskDao.kt` — CRUD + getTasksForDate + getTasksForMonth + getSubtasks + getAllTasksSync + markUpdated
- [ ] `dao/HabitDao.kt` — CRUD + getAllSync + getByDate
- [ ] `dao/IdeaDao.kt` — CRUD + getIdeasByGroup + @Transaction deleteIdeaWithStages
- [ ] `dao/IdeaGroupDao.kt` — CRUD
- [ ] `dao/IdeaStageDao.kt` — CRUD + getStagesForIdea + @Transaction deleteStagesForIdea
- [ ] `dao/TodoDao.kt` — CRUD + getSubtodos + getAllSync
- [ ] `dao/DiaryDao.kt` — CRUD + getAllDates
- [ ] `dao/ShopItemDao.kt` — CRUD + getUnpurchased/Purchased + getAllSync
- [ ] `dao/MottoDao.kt` — CRUD + getAllSync
- [ ] `dao/DayReviewDao.kt` — CRUD + getForDate + getAllSync
- [ ] `dao/SleepLogDao.kt` — CRUD + getForDate + getAllSync
- [ ] `dao/TimerSessionDao.kt` — CRUD + getByDateRange + getAllSync
- [ ] `dao/TimerTemplateDao.kt` — CRUD + getAllSync
- [ ] `dao/LearnDao.kt` — CRUD for LearnItem/LearnSection + sectionsForLearnItem + getByStatus + getAllSync
- [ ] `dao/LearnGroupDao.kt` — CRUD + getAllSync

---

## 11. Repositories (11)

- [ ] `repository/TaskRepository.kt` — Flow wrappers around TaskDao
- [ ] `repository/HabitRepository.kt`
- [ ] `repository/IdeaRepository.kt` — combines 3 DAOs
- [ ] `repository/TodoRepository.kt`
- [ ] `repository/DiaryRepository.kt`
- [ ] `repository/ShopItemRepository.kt`
- [ ] `repository/MottoRepository.kt`
- [ ] `repository/DayReviewRepository.kt`
- [ ] `repository/SleepLogRepository.kt`
- [ ] `repository/TimerRepository.kt` — combines TimerSessionDao + TimerTemplateDao
- [ ] `repository/LearnRepository.kt` — combines LearnDao + LearnGroupDao

---

## 12. Core Managers

- [ ] `manager/DriveManager.kt:24-276` — OkHttp Drive API client: getSignInIntent, handleSignInResult, isSignedIn, ensureAppFolder (search/create), listBackupFiles, uploadBackup (gzip JSON), downloadBackup, pruneOldBackups (max 3), getAccessToken (cached with expiry)
- [ ] `manager/ReminderManager.kt:16-257` — `scheduleReminders` (night before + X min before for EVENT tasks), cancelReminder, schedule/cancel habit reminders, scheduleEventNotification (full-screen AlarmActivity), rescheduleAllAlarms (boot/time change), cancelAllAlarms
- [ ] `manager/BackupWorker.kt:1-103` — WorkManager PeriodicWorkRequest, calls backupDataToGoogleDrive(), handles success/failure, reschedules on failure
- [ ] `manager/SystemSettingsApplier.kt:1-22` — apply DND/sound settings after restore

---

## 13. Broadcast Receiver

- [ ] `receiver/ReminderReceiver.kt:28-633` — `ReminderConfig` helper, `onReceive`: BOOT_COMPLETED/TIME_CHANGED/TIMEZONE_CHANGED/LOCALE_CHANGED → rescheduleAllAlarms + reschedule all 7 reminders, 7 reminder action handlers → notifications with snooze/deep-link, SNOOZE_ALARM action → AlarmActivity, POMODORO_TEST action → PomodoroFinishActivity, notification channel creation for each type

---

## 14. Theme

- [ ] `ui/theme/Color.kt` — color definitions
- [ ] `ui/theme/Theme.kt` — MyApplicationTheme with light/dark
- [ ] `ui/theme/Type.kt` — typography definitions

---

## 15. Build Configuration

- [ ] `build.gradle.kts` (root) — plugins, secrets (removed?)
- [ ] `app/build.gradle.kts` — AGP, Compose, Room, OkHttp, Moshi, Google Sign-In, WorkManager, Robolectric dependencies, signing config
- [ ] `gradle/libs.versions.toml` — version catalog

---
