# Timer Screen Rewrite — Implementation Plan

## Goal
Replace the 792-line `PomodoroScreen.kt` with a new `TimerScreen.kt` containing 3 tabs:
**Pomodoro** | **Cronometer** | **History** — plus a unified active timer widget.

---

## Phase 1: Database (4 files)

### 1.1 TimerSessionEntity (NEW)
`app/src/main/java/com/example/core/database/entity/TimerSessionEntity.kt`
```
id: Long (auto-gen PK)
type: String ("POMODORO" | "CHRONOMETER")
taskId: Long? (nullable FK, no foreign key constraint)
label: String (denormalized for stats-by-label)
durationSeconds: Int
date: String ("yyyy-MM-dd")
timestamp: Long (epoch millis)
note: String
templateName: String? (name of template used)
```

### 1.2 TimerTemplateEntity (NEW)
`app/src/main/java/com/example/core/database/entity/TimerTemplateEntity.kt`
```
id: Long (auto-gen PK)
name: String
focusMinutes: Int
shortBreakMinutes: Int? (null = skip)
longBreakMinutes: Int? (null = skip)
targetSessions: Int? (null = continuous)
```

### 1.3 TimerSessionDao (NEW)
`app/src/main/java/com/example/core/database/dao/TimerSessionDao.kt`
- `insert(session)` → Long
- `update(id, durationSeconds, note, date)`
- `delete(id)`
- `getByDateRange(startDate, endDate)` → Flow<List<TimerSessionEntity>>
- `getAll()` → Flow<List<TimerSessionEntity>>

### 1.4 TimerTemplateDao (NEW)
`app/src/main/java/com/example/core/database/dao/TimerTemplateDao.kt`
- `insert(template)` → Long
- `update(template)`
- `delete(id)`
- `getAll()` → Flow<List<TimerTemplateEntity>>

### 1.5 AppDatabase update
- Register `TimerSessionEntity`, `TimerTemplateEntity`
- Register `TimerSessionDao`, `TimerTemplateDao`
- Bump version 17 → 18
- `Migration(17, 18)`: CREATE both tables; INSERT FROM pomodoro_sessions (type=POMODORO, durationSeconds = durationMinutes * 60, status dropped); DROP pomodoro_sessions

---

## Phase 2: Repository

### 2.1 TimerRepository (NEW)
`app/src/main/java/com/example/core/repository/TimerRepository.kt`
Wraps TimerSessionDao + TimerTemplateDao; delegates all calls.

---

## Phase 3: ViewModel (MainViewModel.kt)

### 3.1 Chronometer state (7 fields)
- `_chronoElapsed: MutableStateFlow<Long>` — elapsed seconds
- `_chronoRunning: MutableStateFlow<Boolean>`
- `_chronoPaused: MutableStateFlow<Boolean>`
- `_chronoJob: Job?`
- Coroutine: increment every 1s when running

### 3.2 Pomodoro changes
- `_pomodoroMarkCompleteOnFinish: MutableStateFlow<Boolean>` — default false
- `handlePhaseCompletion`: only mark task COMPLETE if toggle is on
- `_pomodoroLongBreakMinutes: MutableStateFlow<Int?>` — new
- `_pomodoroShortBreakMinutes` (renamed for clarity, separated from long)
- Target sessions default: 1

### 3.3 -1 min (shared for both modes)
- `adjustTimerMinusOne(mode)` — subtract 60; floor at 0; works running or paused

### 3.4 Template CRUD
- `createTemplate(name, focus, shortBreak, longBreak, targets)` → insert
- `updateTemplate(id, ...)` → update
- `deleteTemplate(id)` → delete
- `templates` → Flow from DB

### 3.5 Timer session history CRUD
- `timerSessionsForDateRange(start, end)` → Flow from DB
- `updateTimerSession(id, durationSeconds, note, date)`
- `deleteTimerSession(id)`
- `addManualTimerSession(type, taskId?, duration, date, note)`

### 3.6 Chronometer lifecycle
- `startChronometer(taskId?)` — set elapsed=0, start counting, DND if enabled
- `pauseChronometer()` — pause
- `resumeChronometer()` — resume
- `stopChronometer()` — stop, returns elapsed for summary dialog
- `saveChronometerSession(duration, taskId, label, note)` — save to DB
- `discardChronometer()` — reset, no save, restore DND

### 3.7 Discard for Pomodoro
- `discardPomodoro()` — stop timer, no session saved, restore DND

### 3.8 Pre-selected task flow (Planner → Timer)
- `_preSelectedTaskForTimer: MutableStateFlow<Long?>` — task ID
- `setPreSelectedTaskForTimer(taskId)` + switch to tab 2
- Cleared after Timer screen consumes it

### 3.9 Manual Complete from Timer
- `markTaskCompleteFromTimer(taskId)` — sets status=COMPLETED (no session recorded)

### 3.10 DND for both modes
- Settings label: "Enable DND during timer" (covers both)
- Same `originalDndState` + `_dndEnabled` toggle shared across modes
- Chronometer: enable DND on start, restore on stop/discard

---

## Phase 4: UI — TimerScreen.kt (~1000-1200 lines)

### 4.1 3-tab layout (Material3 TabRow)
```
[ Pomodoro ] [ Cronometer ] [ History ]
```

### 4.2 Pomodoro tab
- **Task/Note selector**: Dropdown of pending TASK + NOTE items. Shows title + label chip. `[✓ Mark Complete]` button next to selected name.
- **Auto-complete toggle**: "Mark complete when done" checkbox (off by default)
- **Template section**: Dropdown of saved templates + `[+ Manage]` → ManageTemplatesDialog
- **Custom time entry**: Focus slider (5-120), short break slider (0-30), long break slider (0-30), target sessions (+/- with ∞)
- **Timer display**: Monospace 72sp digital clock. Phase badge (FOCUS / SHORT BREAK / LONG BREAK). Session counter
- **Controls row**: `[🗑 Discard] [▶/⏸] [⏹ Stop] [-1m]`

### 4.3 Cronometer tab
- **Task/Note selector**: Same as Pomodoro tab (shared state). `[✓ Mark Complete]` button.
- **Timer display**: Monospace 72sp `HH:MM:SS`
- **Controls row**: `[🗑 Discard] [▶/⏸] [⏹ Stop] [-1m]`
- **Stop summary dialog**: Shows elapsed + task. Buttons: `[Edit] [Save] [Discard]`

### 4.4 History tab
- **Date range**: Preset chips [Today] [This Week] [This Month] [Custom]
- **[+ Add Manual Entry]** → dialog with: Type, Duration, Date, Task, Note
- **Session list**: Icon (🍅/⏱) + Duration + Task/note + Time + Note preview + `✏️` `🗑`
- **Footer**: "X sessions · Total: Yh Zm"

### 4.5 ManageTemplatesDialog
- List of saved templates with Edit/Delete
- Add new template form (name, focus, short break, long break, targets)
- Inline dialog within Pomodoro tab

---

## Phase 5: Stats Integration (StatsScreen.kt)

- Add "Timer Sessions by Label" section after existing completed-tasks chart
- Query TimerSessionEntity, group by label, sum durationSeconds → bar chart

---

## Phase 6: Integration Changes

### 6.1 MainActivity.kt
- Rename nav: `"Pomodoro"` → `"Timer"`
- Route tab 2: `TimerScreen`
- Auto-navigate to tab 2 when pre-selected task is set

### 6.2 ActiveTimerWidget.kt (renamed from ActivePomodoroWidget)
- Compact card in Timer screen header
- Shows: mode icon + elapsed/remaining + task name + Play/Pause + Stop
- Works for both pomodoro and chronometer

### 6.3 PlannerScreen.kt
- Play icon → `viewModel.setPreSelectedTaskForTimer(task.id)` + navigate to tab 2
- Remove old Pomodoro setup dialog from Planner

---

## Phase 7: Cleanup

- Delete: `PomodoroScreen.kt`, `PomodoroSessionEntity.kt`, `PomodoroSessionDao.kt`, `ActivePomodoroWidget.kt`
- Remove old pomodoro references in `TaskRepository` (pomodoroSessionDao)
- Remove unused ViewModel fields: `_pomodoroBreakMinutes` (replaced by short/long)

---

## Edge Cases

| Case | Handling |
|---|---|
| Switch tab while timer running | Timer keeps running in ViewModel |
| Switch away from Timer tab | Timer keeps running |
| -1 min at 0 | Floor at 0 |
| Task deleted but in history | Show "(deleted task)" |
| Break = 0/null | Skip that break type |
| Long break = 0/null | Skip long break cycle |
| Target sessions = null | Infinite mode |
| Old pomodoro data | Migrated to TimerSessionEntity |
| Chronometer > 24h | HH:MM:SS overflows naturally |
| Both modes at once | Not allowed — one active timer |
| Pre-selected task completed | Clears silently |
| Discard accidental | Show confirmation |
| Empty date range | "No sessions in this period" |
| Screen rotation | State in ViewModel, survives |
| Empty template name | Validate — reject save |
| Chronometer stop dialog | Edit/Save/Discard with confirmation on Discard |
