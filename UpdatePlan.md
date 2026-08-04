# BulletCoach — Big Update Plan

## Overview

Add 6 major features to the existing BulletCoach Android app (Kotlin/Jetpack Compose):

1. **Idea List** — Grouped ideas with stage tracking, addable to planner as tasks
2. **To-Do List** — Separate todo list, linkable to planner tasks with two-way sync
3. **Diary** — Markdown journal with date-based entries and live preview
4. **Shop List** — Shopping/wishlist with purchased tracking
5. **Mottos** — Random daily quotes shown in the daily planner view
6. **Day Review** — End-of-day reflection form with notification reminder

---

## ✅ What Has Been Done (Stages 1–10)

### Stage 1 — Database Foundation ✅
- 8 new entities: `IdeaGroupEntity`, `IdeaEntity`, `IdeaStageEntity`, `TodoEntity`, `DiaryEntryEntity`, `ShopItemEntity`, `MottoEntity`, `DayReviewEntity`
- `TaskEntity.linkedTodoId` field added for two-way sync
- 8 DAOs with `Flow`-based reads and suspend mutations
- `AppDatabase` v11→12 with 13 entities, 12 DAOs, `fallbackToDestructiveMigration()`

### Stage 2 — Business Logic ✅
- 6 repositories: `IdeaRepository`, `TodoRepository`, `DiaryRepository`, `ShopItemRepository`, `MottoRepository`, `DayReviewRepository`
- `MainViewModel` updated with all StateFlows, CRUD, sync logic (`toggleTodoCompletion`, `linkTodoToTask`, `addIdeaToPlanner`, today motto caching, review alarm prefs)
- `MainViewModelFactory` updated with 9 repos; `MainActivity` initializes repos
- `BulletCoachBackup` extended with all 8 new entity lists

### Stage 3 — Navigation Shell ✅
- 5th bottom nav tab "More" (`Icons.Default.MoreHoriz`, tab index 4)
- `MoreScreen.kt` — 3-column grid tile menu, `MoreSubScreen` sealed class, back arrow routing
- All 6 sub-screens wired in routing (no placeholders)

### Stage 4 — IdeasScreen ✅
- Group chips with color indicator, long-press edit/delete, 8-color picker
- Idea cards with 3-dot menu, inline sequential stages (checkbox cascades), inline "Add Stage"
- Add-to-Planner dialog (Material3 `DatePicker`, type selector, mode: entire idea / single stage)
- Delete confirmations for ideas and groups

### Stage 5 — TodoScreen ✅
- Quick-add text field (keyboard done), filter chips (All/Pending/Done)
- Todo items: checkbox, priority badge (green/orange/red), linked indicator, schedule/link/unlink buttons, 3-dot menu
- Edit dialog, Link-to-Planner dialog (date picker), linked-delete dialog (delete both / keep + unlink)
- `unlinkTodoFromTask()` added to `MainViewModel`

### Stage 6 — DiaryScreen ✅
- Date nav arrows with dot indicator for existing entries
- Edit/Preview toggle via `FilterChip`, custom regex-based markdown renderer (`#`/`##`/`###` headings, `**bold**`, `*italic*`, `-` bullets, `1.` numbered, `---` dividers)
- Auto-save with 2 s debounce (`Job` cancel/re-launch), "Saving…"/"Saved ✓" indicator
- Save on back + `DisposableEffect`, delete confirmation
- `diaryAllDates` StateFlow added to ViewModel

### Stage 7 — ShopListScreen ✅
- Filter chips (All/To Buy/Purchased with counts), quantity stepper (+/-), optional price, notes
- Add/edit dialog, 3-dot menu with delete, checkbox toggle
- Empty state placeholder

### Stage 8 — Mottos ✅
- `MottoCard.kt` (ui/components/) — animated card for daily view, italic quote + author
- `MottoManagementScreen.kt` — lazy list with edit/delete buttons, add/edit dialog (quote + author)

### Stage 9 — Day Review ✅
- `DayReviewScreen.kt` — 4 text fields (good/bad/improve/gratitude), 5-star mood clicker, 1–10 slider, notes, SAVE & CLOSE, future-date gate
- `DayReviewCard.kt` (ui/components/) — review-status card for daily planner (mood stars, score, or "Tap to review")
- `reviewForDate()` flow exposed on ViewModel

### Stage 10 — PlannerScreen Integration ✅
- `PlannerScreen.kt` imports: `MottoCard`, `DayReviewCard`
- `showLabels` renamed to `showHeaderExtras` (controls both label chips + motto visibility via `NestedScrollConnection`)
- `MottoCard` inserted after label chips, driven by `todayMotto` StateFlow + `showHeaderExtras`
- `DayReviewCard` with `reviewForDate()` flow collection inserted in tasks Column, tapping sets `showDayReviewDialog = true`
- `var showDayReviewDialog` state + `DayReviewScreen` dialog at bottom of `DailyPlannerView`
- `SettingsDialog`: Day Review Reminder section with enable switch, time display button, test notification button, save via `viewModel.updateReviewReminderTime()` / `updateReviewReminderEnabled()`
- All state variables: `reviewReminderTime`, `reviewReminderEnabled`, `enteredReviewTime`, `enteredReviewEnabled`
- **Build verified — `BUILD SUCCESSFUL`** with only pre-existing warnings

### Not Yet Started (Pending)
- Material3 `TimePicker` dialog for the review reminder time button
- `ReminderReceiver` DAY_REVIEW action handler (alarm scheduling, boot/time-change reschedule)
- `MainActivity.onNewIntent` handling for `open_day_review` extra from notification

---

## 1. Navigation Structure

### Bottom Navigation Bar

Add 5th tab **"More"** to the existing 4-tab bottom bar:

```
Planner | Habits | Pomodoro | Stats | More
```

In `MainActivity.kt`, the `when(selectedTab)` block gets a new branch:
```kotlin
4 -> MoreScreen(viewModel = viewModel)
```

### MoreScreen Internal Navigation

The MoreScreen shows a **3-column grid of 6 icon tiles**. Each tile navigates to its sub-screen. Internal navigation is state-driven (a `currentSubScreen` StateFlow in the ViewModel, or local state in MoreScreen composable). Each sub-screen has a **back arrow** in the header returning to the More grid. The bottom nav stays on "More" while inside any sub-screen.

| Tile | Icon | Sub-screen |
|------|------|------------|
| Ideas | `Icons.Default.Lightbulb` | IdeasScreen |
| To-Do | `Icons.Default.Checklist` | TodoScreen |
| Diary | `Icons.Default.MenuBook` | DiaryScreen |
| Shop List | `Icons.Default.ShoppingCart` | ShopListScreen |
| Mottos | `Icons.Default.FormatQuote` | MottoManagementScreen |
| Day Review | `Icons.Default.RateReview` | DayReviewScreen |

### Sub-screen Routing

Each sub-screen gets a `onBack: () -> Unit` callback. MoreScreen manages a `sealed class MoreSubScreen` or simple `Int` state to switch between the grid and sub-screens.

---

## 2. Data Layer

### 2.1 New Room Entities

8 new entities added to `AppDatabase`, version bumped from **11 → 12** (uses `fallbackToDestructiveMigration()`).

#### IdeaGroupEntity
```kotlin
@Entity(tableName = "idea_groups")
data class IdeaGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Long,        // ARGB color int
    val sortOrder: Int = 0
)
```

#### IdeaEntity
```kotlin
@Entity(
    tableName = "ideas",
    foreignKeys = [ForeignKey(
        entity = IdeaGroupEntity::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class IdeaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long? = null,   // null = ungrouped
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
```

#### IdeaStageEntity
```kotlin
@Entity(
    tableName = "idea_stages",
    foreignKeys = [ForeignKey(
        entity = IdeaEntity::class,
        parentColumns = ["id"],
        childColumns = ["ideaId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class IdeaStageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ideaId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0
)
```

#### TodoEntity
```kotlin
@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: String = "Medium",     // Low, Medium, High
    val linkedTaskId: Long? = null,      // FK to tasks.id (no hard FK constraint)
    val status: String = "PENDING",      // PENDING, DONE
    val createdAt: Long = System.currentTimeMillis()
)
```

#### DiaryEntryEntity
```kotlin
@Entity(
    tableName = "diary_entries",
    indices = [Index(value = ["date"], unique = true)]
)
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,                    // "yyyy-MM-dd" — UNIQUE
    val title: String = "",
    val content: String = "",            // Raw markdown
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

#### ShopItemEntity
```kotlin
@Entity(tableName = "shop_items")
data class ShopItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: Int = 1,
    val price: Float? = null,
    val notes: String = "",
    val isPurchased: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
```

#### MottoEntity
```kotlin
@Entity(tableName = "mottos")
data class MottoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val author: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
```

#### DayReviewEntity
```kotlin
@Entity(
    tableName = "day_reviews",
    indices = [Index(value = ["date"], unique = true)]
)
data class DayReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,                    // "yyyy-MM-dd" — UNIQUE
    val good: String = "",               // What went well
    val bad: String = "",                // What was bad
    val improve: String = "",            // What could improve
    val gratitude: String = "",
    val moodRating: Int = 3,             // 1–5
    val score: Int = 5,                  // 1–10
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
```

#### TaskEntity Extension

Add one field to existing `TaskEntity`:
```kotlin
val linkedTodoId: Long? = null   // reverse-link from task back to todo
```

### 2.2 New DAOs

Each follows the existing `@Dao` pattern with Flow-based reads and suspend mutations.

#### IdeaGroupDao
```kotlin
@Dao
interface IdeaGroupDao {
    @Query("SELECT * FROM idea_groups ORDER BY sortOrder ASC, id ASC")
    fun getAllGroups(): Flow<List<IdeaGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: IdeaGroupEntity): Long

    @Update
    suspend fun updateGroup(group: IdeaGroupEntity)

    @Delete
    suspend fun deleteGroup(group: IdeaGroupEntity)   // CASCADE deletes ideas + stages
}
```

#### IdeaDao
```kotlin
@Dao
interface IdeaDao {
    @Query("SELECT * FROM ideas WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun getIdeasForGroup(groupId: Long): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas WHERE groupId IS NULL ORDER BY createdAt DESC")
    fun getUngroupedIdeas(): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas ORDER BY createdAt DESC")
    fun getAllIdeas(): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas WHERE id = :id")
    suspend fun getIdeaById(id: Long): IdeaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdea(idea: IdeaEntity): Long

    @Update
    suspend fun updateIdea(idea: IdeaEntity)

    @Delete
    suspend fun deleteIdea(idea: IdeaEntity)   // CASCADE deletes stages

    @Query("UPDATE ideas SET groupId = :newGroupId WHERE id = :ideaId")
    suspend fun moveIdeaToGroup(ideaId: Long, newGroupId: Long?)
}
```

#### IdeaStageDao
```kotlin
@Dao
interface IdeaStageDao {
    @Query("SELECT * FROM idea_stages WHERE ideaId = :ideaId ORDER BY orderIndex ASC, id ASC")
    fun getStagesForIdea(ideaId: Long): Flow<List<IdeaStageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStage(stage: IdeaStageEntity): Long

    @Update
    suspend fun updateStage(stage: IdeaStageEntity)

    @Delete
    suspend fun deleteStage(stage: IdeaStageEntity)

    @Transaction
    suspend fun reorderStages(stages: List<IdeaStageEntity>) {
        stages.forEach { updateStage(it) }
    }
}
```

#### TodoDao
```kotlin
@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Long): TodoEntity?

    @Query("SELECT * FROM todos WHERE linkedTaskId = :taskId LIMIT 1")
    suspend fun getTodoByLinkedTaskId(taskId: Long): TodoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity): Long

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Delete
    suspend fun deleteTodo(todo: TodoEntity)
}
```

#### DiaryDao
```kotlin
@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries WHERE date = :date LIMIT 1")
    fun getEntryForDate(date: String): Flow<DiaryEntryEntity?>

    @Query("SELECT * FROM diary_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<DiaryEntryEntity>>

    @Query("SELECT date FROM diary_entries")
    fun getAllDates(): Flow<List<String>>   // For dot indicators on date picker

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DiaryEntryEntity): Long

    @Query("DELETE FROM diary_entries WHERE date = :date")
    suspend fun deleteEntryByDate(date: String)
}
```

#### ShopItemDao
```kotlin
@Dao
interface ShopItemDao {
    @Query("SELECT * FROM shop_items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<ShopItemEntity>>

    @Query("SELECT * FROM shop_items WHERE isPurchased = 0 ORDER BY createdAt DESC")
    fun getUnpurchasedItems(): Flow<List<ShopItemEntity>>

    @Query("SELECT * FROM shop_items WHERE isPurchased = 1 ORDER BY createdAt DESC")
    fun getPurchasedItems(): Flow<List<ShopItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShopItemEntity): Long

    @Update
    suspend fun updateItem(item: ShopItemEntity)

    @Delete
    suspend fun deleteItem(item: ShopItemEntity)
}
```

#### MottoDao
```kotlin
@Dao
interface MottoDao {
    @Query("SELECT * FROM mottos ORDER BY createdAt DESC")
    fun getAllMottos(): Flow<List<MottoEntity>>

    @Query("SELECT * FROM mottos ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomMotto(): MottoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMotto(motto: MottoEntity): Long

    @Update
    suspend fun updateMotto(motto: MottoEntity)

    @Delete
    suspend fun deleteMotto(motto: MottoEntity)
}
```

#### DayReviewDao
```kotlin
@Dao
interface DayReviewDao {
    @Query("SELECT * FROM day_reviews WHERE date = :date LIMIT 1")
    fun getReviewForDate(date: String): Flow<DayReviewEntity?>

    @Query("SELECT * FROM day_reviews ORDER BY date DESC")
    fun getAllReviews(): Flow<List<DayReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: DayReviewEntity): Long

    @Query("DELETE FROM day_reviews WHERE date = :date")
    suspend fun deleteReviewByDate(date: String)
}
```

### 2.3 New Repositories

Each wraps a single DAO (same pattern as `HabitRepository`), except `IdeaRepository` wraps 3 DAOs.

#### IdeaRepository
```kotlin
class IdeaRepository(
    private val ideaGroupDao: IdeaGroupDao,
    private val ideaDao: IdeaDao,
    private val ideaStageDao: IdeaStageDao
) {
    // Groups
    val allGroups: Flow<List<IdeaGroupEntity>> = ideaGroupDao.getAllGroups()
    suspend fun insertGroup(group: IdeaGroupEntity) = ideaGroupDao.insertGroup(group)
    suspend fun updateGroup(group: IdeaGroupEntity) = ideaGroupDao.updateGroup(group)
    suspend fun deleteGroup(group: IdeaGroupEntity) = ideaGroupDao.deleteGroup(group)

    // Ideas
    fun getIdeasForGroup(groupId: Long) = ideaDao.getIdeasForGroup(groupId)
    fun getUngroupedIdeas() = ideaDao.getUngroupedIdeas()
    fun getAllIdeas() = ideaDao.getAllIdeas()
    suspend fun getIdeaById(id: Long) = ideaDao.getIdeaById(id)
    suspend fun insertIdea(idea: IdeaEntity) = ideaDao.insertIdea(idea)
    suspend fun updateIdea(idea: IdeaEntity) = ideaDao.updateIdea(idea)
    suspend fun deleteIdea(idea: IdeaEntity) = ideaDao.deleteIdea(idea)
    suspend fun moveIdeaToGroup(ideaId: Long, newGroupId: Long?) =
        ideaDao.moveIdeaToGroup(ideaId, newGroupId)

    // Stages
    fun getStagesForIdea(ideaId: Long) = ideaStageDao.getStagesForIdea(ideaId)
    suspend fun insertStage(stage: IdeaStageEntity) = ideaStageDao.insertStage(stage)
    suspend fun updateStage(stage: IdeaStageEntity) = ideaStageDao.updateStage(stage)
    suspend fun deleteStage(stage: IdeaStageEntity) = ideaStageDao.deleteStage(stage)
    suspend fun reorderStages(stages: List<IdeaStageEntity>) =
        ideaStageDao.reorderStages(stages)
}
```

#### TodoRepository
```kotlin
class TodoRepository(private val todoDao: TodoDao) {
    val allTodos: Flow<List<TodoEntity>> = todoDao.getAllTodos()
    val pendingTodos: Flow<List<TodoEntity>> = todoDao.getPendingTodos()
    suspend fun getTodoById(id: Long) = todoDao.getTodoById(id)
    suspend fun getTodoByLinkedTaskId(taskId: Long) = todoDao.getTodoByLinkedTaskId(taskId)
    suspend fun insertTodo(todo: TodoEntity) = todoDao.insertTodo(todo)
    suspend fun updateTodo(todo: TodoEntity) = todoDao.updateTodo(todo)
    suspend fun deleteTodo(todo: TodoEntity) = todoDao.deleteTodo(todo)
}
```

#### DiaryRepository
```kotlin
class DiaryRepository(private val diaryDao: DiaryDao) {
    fun getEntryForDate(date: String) = diaryDao.getEntryForDate(date)
    fun getAllEntries() = diaryDao.getAllEntries()
    fun getAllDates() = diaryDao.getAllDates()
    suspend fun insertEntry(entry: DiaryEntryEntity) = diaryDao.insertEntry(entry)
    suspend fun deleteEntryByDate(date: String) = diaryDao.deleteEntryByDate(date)
}
```

#### ShopItemRepository
```kotlin
class ShopItemRepository(private val shopItemDao: ShopItemDao) {
    val allItems: Flow<List<ShopItemEntity>> = shopItemDao.getAllItems()
    val unpurchasedItems: Flow<List<ShopItemEntity>> = shopItemDao.getUnpurchasedItems()
    val purchasedItems: Flow<List<ShopItemEntity>> = shopItemDao.getPurchasedItems()
    suspend fun insertItem(item: ShopItemEntity) = shopItemDao.insertItem(item)
    suspend fun updateItem(item: ShopItemEntity) = shopItemDao.updateItem(item)
    suspend fun deleteItem(item: ShopItemEntity) = shopItemDao.deleteItem(item)
}
```

#### MottoRepository
```kotlin
class MottoRepository(private val mottoDao: MottoDao) {
    val allMottos: Flow<List<MottoEntity>> = mottoDao.getAllMottos()
    suspend fun getRandomMotto() = mottoDao.getRandomMotto()
    suspend fun insertMotto(motto: MottoEntity) = mottoDao.insertMotto(motto)
    suspend fun updateMotto(motto: MottoEntity) = mottoDao.updateMotto(motto)
    suspend fun deleteMotto(motto: MottoEntity) = mottoDao.deleteMotto(motto)
}
```

#### DayReviewRepository
```kotlin
class DayReviewRepository(private val dayReviewDao: DayReviewDao) {
    fun getReviewForDate(date: String) = dayReviewDao.getReviewForDate(date)
    fun getAllReviews() = dayReviewDao.getAllReviews()
    suspend fun insertReview(review: DayReviewEntity) = dayReviewDao.insertReview(review)
    suspend fun deleteReviewByDate(date: String) = dayReviewDao.deleteReviewByDate(date)
}
```

### 2.4 Database Migration

In `AppDatabase.kt`:
- Add all 8 entity classes to the `@Database(entities = [...])` array
- Bump `version = 11` to `version = 12`
- `fallbackToDestructiveMigration()` stays (existing behavior, destroys data on schema change)

### 2.5 MainViewModel Additions

All addition go in existing `MainViewModel.kt`. Constructor gets 6 new repos. The `MainViewModelFactory` is updated accordingly.

#### StateFlows

```kotlin
// Idea
val ideaGroups: StateFlow<List<IdeaGroupEntity>>
val ideas: StateFlow<List<IdeaEntity>>             // combined all ideas

// Todo
val pendingTodos: StateFlow<List<TodoEntity>>

// Diary
fun diaryEntryForDate(date: String): Flow<DiaryEntryEntity?>

// Shop
val unpurchasedItems: StateFlow<List<ShopItemEntity>>
val shopFilter: MutableStateFlow<ShopFilter>        // ALL, TO_BUY, PURCHASED

// Motto
val todayMotto: StateFlow<MottoEntity?>

// Day Review
fun reviewForDate(date: String): Flow<DayReviewEntity?>
```

#### CRUD Functions

Each feature gets add/update/delete functions matching the repository methods. Key business logic:

**Todo ↔ Task Sync** (`toggleTodoCompletion`):
```
When todo completed:
  if linkedTaskId != null:
    val linkedTask = taskRepository.getTaskById(todo.linkedTaskId)
    if linkedTask != null && linkedTask.status != "COMPLETED":
      taskRepository.updateTask(linkedTask.copy(status = "COMPLETED"))
  update todo status = "DONE"

Reverse: when task completed in planner, find via linkedTodoId, update todo.
```

**Link Todo to Task** (`linkTodoToTask`):
```
1. Create TaskEntity(title=todo.title, description=todo.description, date=targetDate, label="TODO", linkedTodoId=todo.id)
2. val taskId = taskRepository.insertTask(task)
3. todoRepository.updateTodo(todo.copy(linkedTaskId = taskId))
```

**Idea → Planner** (`addIdeaToPlanner`):
```
// Entire idea
1. val parentTaskId = taskRepository.insertTask(TaskEntity(title=idea.title, date, type))
2. val stages = ideaStageDao.getStagesForIdeaSync(idea.id)   // suspend version
3. stages.filter { it.title.isNotBlank() }.forEach { stage ->
     taskRepository.insertTask(TaskEntity(title=stage.title, parentTaskId=parentTaskId, subtaskImportance="OPTIONAL", ...))
   }

// Single stage
1. taskRepository.insertTask(TaskEntity(title=stage.title, date, type))
```

**Today Motto Caching**:
```
val todayMottoId = prefs.getLong("today_motto_id", -1L)
val todayDate = prefs.getString("today_motto_date", "")

if (todayDate != getTodayDateString()) {
    // New day — pick random
    val random = mottoRepository.getRandomMotto()
    if (random != null) {
        prefs.edit()
            .putLong("today_motto_id", random.id)
            .putString("today_motto_date", getTodayDateString())
            .apply()
        _todayMotto.value = random
    } else {
        _todayMotto.value = null
    }
} else {
    // Same day — load cached
    _todayMotto.value = mottoRepository.getMottoById(todayMottoId) ?: null
}
```

**Day Review Alarm**:
```kotlin
fun scheduleDayReviewAlarm(context: Context) {
    val time = _reviewReminderTime.value  // "HH:mm"
    val parts = time.split(":")
    val hour = parts[0].toInt()
    val minute = parts[1].toInt()

    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
    }

    val intent = Intent(context, ReminderReceiver::class.java).apply {
        action = "com.example.action.DAY_REVIEW"
        putExtra("title", "Day Review Reminder")
        putExtra("message", "Time to review your day!")
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context, 5000, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis, 86400000L, pendingIntent)
}
```

#### Backup Integration

Extend the existing `BulletCoachBackup` data class:

```kotlin
data class BulletCoachBackup(
    val tasks: List<TaskEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val habitLogs: List<HabitLogEntity> = emptyList(),
    val sleepLogs: List<SleepLogEntity> = emptyList(),
    // New:
    val ideaGroups: List<IdeaGroupEntity> = emptyList(),
    val ideas: List<IdeaEntity> = emptyList(),
    val ideaStages: List<IdeaStageEntity> = emptyList(),
    val todos: List<TodoEntity> = emptyList(),
    val diaryEntries: List<DiaryEntryEntity> = emptyList(),
    val shopItems: List<ShopItemEntity> = emptyList(),
    val mottos: List<MottoEntity> = emptyList(),
    val dayReviews: List<DayReviewEntity> = emptyList()
)
```

Update `backupDataToGoogleDrive()` and `restoreDataFromGoogleDrive()` to include all new lists.

---

## 3. Feature Details

### 3.1 IdeasScreen

**Purpose**: Create and manage ideas organized into groups. Each idea has named stages (steps) that can be checked off. Ideas or individual stages can be sent to the Planner as tasks.

**UI Layout**:
```
┌──────────────────────────────────┐
│ ← Ideas               [+ Group] │  Header with back arrow
├──────────────────────────────────┤
│ [All] [Work] [Personal] [+]     │  Group chips (scrollable LazyRow)
├──────────────────────────────────┤
│ ┌─ Idea Card ────────────────┐  │
│ │ 💡 Idea Title        [📅]  │  │  📅 = Add to Planner
│ │ Description text           │  │
│ │ ────────────────────────  │  │
│ │ ☐ Stage 1 — First step    │  │  Checkbox + title, sequential
│ │ ☑ Stage 2 — Second step   │  │  Completed checked off
│ │ ☐ Stage 3 — Third step    │  │
│ │ [+ Add Stage]             │  │  Inline add
│ │ [✏️] [🗑️] [↕]            │  │  Edit, Delete, Reorder
│ └────────────────────────────┘  │
│ ... more cards ...              │
├──────────────────────────────────┤
│                    [+ Add Idea] │  FAB
└──────────────────────────────────┘
```

**Interactions**:
- **Group chip tap**: Filter ideas by group. "All" shows everything. "+" opens create group dialog.
- **Idea card tap**: Opens edit dialog (title + description).
- **Stage checkbox**: Toggle completion immediately.
- **"Add to Planner" button**: Opens bottom sheet with:
  - Date picker (existing pattern from TaskManagerDialog)
  - Type picker: Task / Event / Note
  - Mode: "Entire Idea (with all stages)" or "Pick a stage"
  - If "Entire Idea": creates parent task + subtask for each stage
  - If "Pick a stage": pick one stage → creates single task
- **FAB (+ Add Idea)**: Dialog with title, description, group selector dropdown.
- **Long-press on idea**: Context menu: Edit, Delete, Move to Group, Add to Planner.

**Dialogs**:
- Create/Edit Group: name + color picker (8 preset colors as circles)
- Create/Edit Idea: title + description
- Add Stage: title only (inline in the card)
- Delete confirmation: "Delete this idea and all its stages?" / "Delete group and all ideas inside?"

**Edge Cases**:
- **Empty group**: Allowed — shows as chip with count 0.
- **Idea with 0 stages**: Add to Planner creates a single task with no subtasks.
- **All stages completed**: Still addable to Planner (user might want to revisit).
- **Stage title blank**: Ignored when adding to planner; prevented in UI.
- **Group deleted**: CASCADE deletes all ideas + stages (Room FK handles it — no orphan records).
- **Last group deleted**: No groups shown, "All" chip is the only option.

### 3.2 TodoScreen

**Purpose**: Separate todo list (inbox). Todos can be linked to planner tasks via drag-to-weekly-view or date picker. Completion syncs both ways.

**UI Layout**:
```
┌──────────────────────────────────┐
│ ← To-Do List                    │  Header
├──────────────────────────────────┤
│ [✏️ What needs to be done?]     │  Quick-add text field at top
├──────────────────────────────────┤
│ Filter: [All] [Pending] [Done]  │  Filter chips
├──────────────────────────────────┤
│ ┌─ Todo Item ────────────────┐  │
│ │ ☐ Finish report        📅  │  │  📅 = Schedule to date (unlinked)
│ │   🔴 High priority         │  │  Priority indicator
│ │                             │  │
│ └────────────────────────────┘  │
│ ┌─ Todo Item (linked) ──────┐  │
│ │ ☑ Buy groceries       ✅  │  │  ✅ = Linked to Dec 15 (completed task)
│ │   🟢 Linked to Dec 15     │  │  Shows linked date + completed status
│ │                             │  │
│ └────────────────────────────┘  │
│ ... more items ...              │
└──────────────────────────────────┘
```

**Interactions**:
- **Quick-add field**: Type and press enter → creates PENDING todo.
- **Checkbox**: Toggle completion. If linked task exists, also toggle linked task.
- **📅 button**: Opens date picker → calls `linkTodoToTask()`. If already linked, shows "Re-schedule" option (unlink old, link new).
- **Long-press**: Context menu: Edit, Delete, Schedule, Unlink (only if linked).
- **Filter chips**: All / Pending / Done.

**Weekly View Integration** (modify `WeeklyPlannerView` in `PlannerScreen.kt`):
- Add a **persistent bottom sheet** handle at the bottom of the weekly view.
- When expanded: shows pending todos in a compact list.
- Each day column in the weekly view gets a small **"+"** icon in its header.
- Tap "+" → shows a popup list of pending todos → pick one → links to that day.
- Linked tasks appear in the weekly view with a `label="TODO"` tag and "TODO" badge.

**Dialogs**:
- Edit Todo: title + description + priority picker
- Delete confirmation: "Delete this todo?" If linked: "Delete task only (keeps linked task)" / "Delete both"
- Unlink confirmation: "Remove link to planner task?"

**Edge Cases**:
- **Linked task deleted from planner** → dialog: "Delete task only (keeps todo)" / "Delete both task and todo". Determined by the **existing** TaskEntity deletion path — when `deleteTask()` detects `linkedTodoId != null`, show confirmation dialog before proceeding. If user picks "keep todo", call `viewModel.unlinkTodoFromTask(todo)` first.
- **Linked todo deleted** → same dialog mirrored.
- **Todo completed → linked task also completes**. Implemented in `toggleTodoCompletion()`:
  ```
  If new status == "DONE" && linkedTaskId != null:
    val linkedTask = taskRepo.getTaskById(linkedTaskId)
    if (linkedTask != null && linkedTask.status != "COMPLETED"):
      taskRepo.updateTask(linkedTask.copy(status = "COMPLETED"))
  ```
- **Task completed → linked todo also done**. Implemented in `toggleTaskCompletion()`:
  ```
  If new status == "COMPLETED" && linkedTodoId != null:
    val linkedTodo = todoRepo.getTodoById(linkedTodoId)
    if (linkedTodo != null && linkedTodo.status != "DONE"):
      todoRepo.updateTodo(linkedTodo.copy(status = "DONE"))
  ```
- **Empty state**: "No to-dos yet. Add one above."
- **Past date linking**: Allowed (user might backfill tasks).

### 3.3 DiaryScreen

**Purpose**: Daily journal with markdown support. One entry per date.

**UI Layout**:
```
┌──────────────────────────────────┐
│ ← Diary    < Dec 14 >     🗑️   │  Header: back, date nav, delete
│              Saved ✓            │  Auto-save indicator
├──────────────────────────────────┤
│ [──────────────────────────────] │  Title field
├──────────────────────────────────┤
│ [📝 Edit]  [👁️ Preview]        │  Toggle tabs
├──────────────────────────────────┤
│ ┌─ Editor / Preview ──────────┐ │
│ │                              │ │
│ │  (Edit mode: raw markdown)   │ │
│ │  or                          │ │
│ │  (Preview mode: rendered)    │ │
│ │                              │ │
│ └──────────────────────────────┘ │
└──────────────────────────────────┘
```

**Markdown Renderer** (custom, regex-based):
- `# Heading 1` → Bold 20sp, top padding
- `## Heading 2` → Bold 18sp, top padding
- `**bold text**` → FontWeight.Bold span in AnnotatedString
- `*italic text*` → FontStyle.Italic span in AnnotatedString
- `- list item` → bullet point (•) with 12dp left indent
- `1. numbered item` → numbered with 12dp left indent
- `---` → HorizontalDivider
- `[text](url)` → clickable text (parsed but not rendered as clickable in initial version)

Parser processes line by line, builds a list of `AnnotatedString` segments with appropriate `SpanStyle` and `ParagraphStyle`.

**Interactions**:
- **Date arrows**: Navigate day by day. Dates with entries show a **dot indicator** below the date in the header.
- **Edit/Preview toggle**: Edit shows raw markdown in `OutlinedTextField`. Preview renders via the custom markdown parser. State is preserved between toggles.
- **Auto-save**: Triggers 2 seconds after last keystroke (debounced via `Job`). Also saves on back navigation (via `DisposableEffect`). Visual indicator: "Saving..." → "Saved ✓".
- **Delete**: Confirmation dialog → clears entry for this date.

**Edge Cases**:
- **Date already has entry**: Load existing content on navigate.
- **Empty title + content**: Still saveable (date is the anchor).
- **Auto-save race**: `diarySaveJob?.cancel()` before launching new save job.
- **Very long content**: `OutlinedTextField` with `singleLine = false` inside `verticalScroll`.
- **Markdown rendering failure**: Fall back to plain text display.
- **Day change while editing**: No conflict — user is editing a specific date.

### 3.4 ShopListScreen

**Purpose**: Simple shopping list / wishlist with purchased tracking. Items visible in monthly context.

**UI Layout**:
```
┌──────────────────────────────────┐
│ ← Shop List           [+ Add]   │  Header
├──────────────────────────────────┤
│ [All] [To Buy] [Purchased]      │  Filter chips
├──────────────────────────────────┤
│ ┌─ Item ──────────────────────┐ │
│ │ 🛒 3× Apples           💰5.99│ │  Quantity × Name, Price
│ │   📝 Notes: Fuji preferred   │ │  Optional notes
│ │   [✏️] [🗑️] [☐]             │ │  Edit, Delete, Toggle purchased
│ └──────────────────────────────┘ │
│ ... more items ...               │
├──────────────────────────────────┤
│ (only showing To Buy items)      │
└──────────────────────────────────┘
```

**Interactions**:
- **FAB (+ Add)**: Dialog with name, quantity stepper (+/-), price (optional), notes.
- **Checkbox ☐**: Toggle purchased. When purchased → item hides (default view is "To Buy").
- **Filter chips**: All (shows purchased with strikethrough) / To Buy / Purchased.
- **Long-press**: Edit/Delete popup.
- **Tap item (To Buy view)**: Quick toggle purchased.

**Dialogs**:
- Add/Edit Item: name, quantity (number picker or +/- buttons), price field, notes field.
- Delete confirmation.

**Edge Cases**:
- **Quantity < 1**: Reset to 1 on save.
- **Price negative**: Stored as null.
- **Empty name**: Prevented (OK button disabled).
- **Empty list**: "Your shopping list is empty. Add items you plan to buy."

### 3.5 MottoManagementScreen + MottoCard

**Purpose**: Manage inspirational quotes. One random motto shown per day in the daily planner.

**MottoManagementScreen UI**:
```
┌──────────────────────────────────┐
│ ← Mottos             [+ Add]    │
├──────────────────────────────────┤
│ ┌─ Motto Card ────────────────┐ │
│ │ "The only way... is to love │ │
│ │  what you do."              │ │
│ │  — Steve Jobs               │ │
│ │  [✏️] [🗑️]                  │ │
│ └──────────────────────────────┘ │
│ ... more cards ...               │
└──────────────────────────────────┘
```

**MottoCard UI** (in DailyPlannerView):
```
┌──────────────────────────────────┐
│ 💬 "The only way to do great   │
│     work is to love what you   │
│     do." — Steve Jobs          │
└──────────────────────────────────┘
```
- Small card, subtle background
- Italic quote text, smaller author text
- AnimatedVisibility controlled by same scroll signal as labels

**Integration into DailyPlannerView**:
- Located **between the label filter chips** and the **task list**
- Uses the same `NestedScrollConnection` that hides the labels on scroll
- The signal variable is renamed from `showLabels` to `showHeaderExtras`
- `AnimatedVisibility` wraps both labels and motto together

**Interactions**:
- **FAB (+ Add)**: Dialog with text field + optional author field.
- **Long-press**: Edit/Delete popup.
- **Random selection**: On day change (detected in `refreshSystemDate()`), pick a random motto from all available. Cache today's motto ID in SharedPrefs so it stays stable for the day.
- **MottoManagementScreen accessible**: From the More grid tile "Mottos".

**Edge Cases**:
- **0 mottos**: `todayMotto` is null → MottoCard is invisible. No crash.
- **1 motto**: Always shown every day (random selection returns the only one).
- **Last motto deleted while it's today's cached motto**: Immediately recompute `todayMotto`:
  - Try to pick a new random one (returns null if empty)
  - If null, set `todayMotto = null`
  - Clear cached ID from SharedPrefs
- **Very long quote**: Max 3 lines with `maxLines = 3` and `ellipsis = TextEllipsis.End` in the card; full text visible in management screen.
- **Motto card scroll behavior**: When user scrolls down past ~15px, `showHeaderExtras` → false → motto + labels both animate out. On scroll up → both animate in.

### 3.6 DayReviewScreen + DayReviewCard

**Purpose**: End-of-day reflection. User fills fields about their day. Daily notification reminder at custom time.

**DayReviewScreen UI**:
```
┌──────────────────────────────────┐
│ ← Day Review        [Dec 14]    │  Header with date display
├──────────────────────────────────┤
│ What went well?                  │
│ ┌────────────────────────────┐  │
│ │ I finished the project     │  │  Multi-line
│ └────────────────────────────┘  │
│                                  │
│ What was bad?                    │
│ ┌────────────────────────────┐  │
│ │ Woke up late               │  │
│ └────────────────────────────┘  │
│                                  │
│ What could improve?              │
│ ┌────────────────────────────┐  │
│ │ Sleep earlier              │  │
│ └────────────────────────────┘  │
│                                  │
│ Gratitude                        │
│ ┌────────────────────────────┐  │
│ │ Thankful for my team       │  │
│ └────────────────────────────┘  │
│                                  │
│ Mood:  ★ ★ ★ ☆ ☆                │  5 stars (clickable)
│ Score:  ═══●═════════  7/10     │  Slider 1–10
│                                  │
│ Notes                            │
│ ┌────────────────────────────┐  │
│ │                            │  │
│ └────────────────────────────┘  │
├──────────────────────────────────┤
│        [SAVE & CLOSE]            │
└──────────────────────────────────┘
```

**DayReviewCard UI** (in DailyPlannerView, bottom of list):
```
┌──────────────────────────────────┐
│ 📋 Day Review — Dec 14          │
│ Status: [Pending] / [Completed ✓]│
│ Mood: ★★★☆☆  Score: 7/10      │
│ (if completed, show summary)     │
└──────────────────────────────────┘
```
- Only shown for the **currently selected date** (not just today)
- Tapping opens the DayReviewDialog for that date

**DayReviewDialog** (full-screen or bottom sheet, triggered by notification):
Same content as DayReviewScreen but as a dialog. Used when the notification fires and the user taps it.

**Notification Flow**:
1. **User sets time**: In SettingsDialog, add a "Day Review Reminder" section with a Material3 `TimePicker` and an enable/disable switch.
2. **Alarm scheduling**: In `MainViewModel.scheduleDayReviewAlarm()`:
   ```
   val intent = Intent(context, ReminderReceiver::class.java)
   intent.action = "com.example.action.DAY_REVIEW"
   intent.putExtra("title", "Day Review Reminder")
   intent.putExtra("message", "Time to review your day!")
   val pendingIntent = PendingIntent.getBroadcast(context, 5000, intent, ...)
   alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, firstTimeInMillis, 86400000L, pendingIntent)
   ```
3. **ReminderReceiver handling**: In `onReceive()`, check for `action == "com.example.action.DAY_REVIEW"`. Instead of showing a notification with the default flow, show a notification whose `contentIntent` opens `MainActivity` with an extra:
   ```kotlin
   intent.putExtra("open_day_review", true)
   ```
4. **MainActivity handling**: On `onCreate()` / `onNewIntent()`, check for `"open_day_review"` extra. If true, set selected tab to "More" and auto-navigate to DayReviewScreen for today's date.
5. **Boot complete**: The existing `ReminderReceiver` already handles `ACTION_BOOT_COMPLETED` and calls `rescheduleAllAlarms()`. Extend that to also reschedule the day review alarm.

**DayReviewScreen notes**:
- Save button validates at least one field is filled (good, bad, improve, gratitude, notes, mood, or score — at least one non-empty/non-default).
- After save, show a confirmation snackbar/toast.
- Delete button (when editing existing review): confirmation → delete → back to previous screen.
- Future dates: review NOT allowed. Date picker restricted to today and past.

**Edge Cases**:
- **Review for future date**: Not allowed (gated at UI level — date navigation arrows disabled for future).
- **Review already exists**: Load existing data into fields for editing.
- **All fields empty + default mood (3) + default score (5)**: "Fill at least one field" validation prevents save.
- **Notification while app is open**: Don't show the notification; directly trigger the dialog. Detect by checking if `MainActivity` is in foreground (use `Lifecycle.Event.ON_RESUME` / `ON_PAUSE` state).
- **Phone off at review time**: Alarm missed. When user opens the app later, the DayReviewCard shows "Pending" for today. Tapping it opens the review normally.
- **Time data changes**: `ACTION_TIME_CHANGED` already triggers `rescheduleAllAlarms()` — extend to reschedule day review alarm too.
- **Midnight crossing**: `refreshSystemDate()` runs every 15s and catches day change. The DayReviewCard updates to the new day automatically.

---

## 4. PlannerScreen Modifications

### 4.1 DailyPlannerView Changes

**MottoCard**:
- Inserted between the `AnimatedVisibility` label chips section and the task list.
- Uses the same `showLabels` signal (renamed to `showHeaderExtras`).
- The existing `nestedScrollConnection` that sets `showLabels` based on scroll direction now controls both labels and motto.

**DayReviewCard**:
- Appended as the **last item** in the daily task list (`LazyColumn`).
- Shows the review status for the currently selected date.
- Tapping opens DayReviewScreen for that date.

**NestedScrollConnection** (existing, line 534 of PlannerScreen):
- Currently: `if (available.y < -15) showLabels = false else if (available.y > 15) showLabels = true`
- Rename variable to `showHeaderExtras` to control both labels + motto.

### 4.2 WeeklyPlannerView Changes

Add a **bottom sheet** or **expandable section** at the bottom:
- Shows pending todos (from `viewModel.pendingTodos`)
- Each day column header gets a small "+" icon button
- Tap "+" → dropdown popup with pending todo list → select → links to that day

### 4.3 SettingsDialog Changes

Add a new section: **"DAY REVIEW REMINDER"** after the existing "EVENT REMINDERS" section:

```
──────────────────────────────────
DAY REVIEW REMINDER
[Time Picker: 21:00]             // Material3 TimePicker
[Enable Daily Reminder] ☐        // Switch
[Test Notification]    [Button]  // Sends test notification
```

- Time picker uses Material3 `TimePicker` in a dialog or inline.
- Enable/disable switch controls whether the alarm is scheduled.
- "Test Notification" sends an immediate test notification to verify.
- Time saved to SharedPrefs (`review_reminder_time`, `review_reminder_enabled`).

### 4.4 MainActivity Changes

```kotlin
// Add to bottom nav items
val navItems = listOf(
    NavigationItem("Planner", Icons.Default.Task, 0),
    NavigationItem("Habits", Icons.Default.Favorite, 1),
    NavigationItem("Pomodoro", Icons.Default.Timer, 2),
    NavigationItem("Stats", Icons.Default.Leaderboard, 3),
    NavigationItem("More", Icons.Default.MoreHoriz, 4)   // NEW
)

// Add to when block
when (selectedTab) {
    0 -> PlannerScreen(viewModel = viewModel)
    1 -> HabitsScreen(viewModel = viewModel)
    2 -> PomodoroScreen(viewModel = viewModel)
    3 -> StatsScreen(viewModel = viewModel)
    4 -> MoreScreen(
        viewModel = viewModel,
        onNavigateToPlanner = { selectedTab = 0 }   // For Idea→Planner navigation callback
    )
}

// Handle day review notification intent
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    if (intent.getBooleanExtra("open_day_review", false)) {
        // Switch to More tab and open Day Review
    }
}
```

---

## 5. ReminderReceiver Changes

Extend `ReminderReceiver.kt`:

```kotlin
override fun onReceive(context: Context, intent: Intent) {
    val action = intent.action

    // Existing boot/time change handling
    if (action == Intent.ACTION_BOOT_COMPLETED || ...) {
        // reschedule events (existing) AND reschedule day review alarm
        com.example.core.manager.ReminderManager.rescheduleAllAlarms(context)
        // Add: reschedule day review
        val prefs = context.getSharedPreferences("bulletcoach_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("review_reminder_enabled", false)) {
            val time = prefs.getString("review_reminder_time", "21:00") ?: "21:00"
            // Re-schedule using MainViewModel's logic (or extract to a helper)
        }
        return
    }

    // New: Day review action
    if (action == "com.example.action.DAY_REVIEW") {
        // Show notification that opens MainActivity with open_day_review=true
        showDayReviewNotification(context)
        return
    }

    // ... existing event reminder handling ...
}
```

---

## 6. Edge Case Catalog (Full Reference)

### Idea List
| Edge Case | Handling |
|-----------|----------|
| Group deleted | CASCADE deletes all ideas + stages inside (Room FK) |
| Idea deleted | CASCADE deletes all stages |
| Idea with 0 stages | Added to planner as single task, no subtasks |
| All stages completed | Still addable to planner (user may want to revisit) |
| Stage title blank | Ignored on add-to-planner; prevented at UI |
| Group with 0 ideas | Allowed — shows as chip with count 0 |
| Empty state | "No ideas yet. Tap + to create your first idea." |
| Long titles | Wraps in card; no truncation |

### To-Do List
| Edge Case | Handling |
|-----------|----------|
| Linked task deleted | Dialog: "Keep todo (unlink)" / "Delete both" |
| Linked todo deleted | Same dialog mirrored |
| Todo completed | Linked task also completed (reverse sync) |
| Task completed (with linkedTodoId) | Linked todo also marked DONE |
| Linking to past date | Allowed |
| Linking already-linked todo | Relink: unlink old, link new |
| Empty state | "No to-dos yet. Add one above." |
| Multiple todos → same task | Prevented (1:1 link via linkedTaskId) |

### Diary
| Edge Case | Handling |
|-----------|----------|
| Date already has entry | Loaded for editing on navigate |
| Auto-save race | Job cancel before new launch |
| Empty title + content | Still saveable |
| Markdown parse failure | Fall back to plain text |
| Very long content | Scrollable text field |
| Day change while editing | No conflict — editing specific date |

### Shop List
| Edge Case | Handling |
|-----------|----------|
| Purchased item | Hidden by default (To Buy view); visible via filter |
| Quantity < 1 | Reset to 1 on save |
| Price negative | Stored as null |
| Empty name | Prevented (OK disabled) |
| Empty list | "Your shopping list is empty." placeholder |
| Duplicate names | Allowed |

### Mottos
| Edge Case | Handling |
|-----------|----------|
| 0 mottos | Card invisible, no crash |
| 1 motto | Always shown (only random candidate) |
| Last motto deleted while cached | Immediately pick new random or set null |
| Very long quote | Max 3 lines with ellipsis in daily card |
| Day change | New random picked in refreshSystemDate() |

### Day Review
| Edge Case | Handling |
|-----------|----------|
| Future date | Not allowed (date gated) |
| Already reviewed | Edit mode with pre-filled fields |
| All fields empty + defaults | "Fill at least one field" validation |
| Notification while app open | Suppress notification, show dialog directly |
| Phone off at alarm time | Catch-up via DayReviewCard in daily view |
| Time zone change | Reschedule alarm via ACTION_TIMEZONE_CHANGED |
| Boot complete | Reschedule alarm in ReminderReceiver |

### Cross-Cutting
| Edge Case | Handling |
|-----------|----------|
| Database migration | fallbackToDestructiveMigration (existing) |
| Screen rotation | All state in ViewModel |
| App killed | Room data persists; ViewModel reinitializes |
| Small screen (320dp) | Bottom nav still fits (5 compact tabs) |
| Permissions | All needed permissions already requested |
| JSON backup size | ~200KB for typical usage — fine |

---

## 7. File Change Summary

### New Files (32)

**Entities** (8):
```
app/src/main/java/com/example/core/database/entity/IdeaGroupEntity.kt
app/src/main/java/com/example/core/database/entity/IdeaEntity.kt
app/src/main/java/com/example/core/database/entity/IdeaStageEntity.kt
app/src/main/java/com/example/core/database/entity/TodoEntity.kt
app/src/main/java/com/example/core/database/entity/DiaryEntryEntity.kt
app/src/main/java/com/example/core/database/entity/ShopItemEntity.kt
app/src/main/java/com/example/core/database/entity/MottoEntity.kt
app/src/main/java/com/example/core/database/entity/DayReviewEntity.kt
```

**DAOs** (8):
```
app/src/main/java/com/example/core/database/dao/IdeaGroupDao.kt
app/src/main/java/com/example/core/database/dao/IdeaDao.kt
app/src/main/java/com/example/core/database/dao/IdeaStageDao.kt
app/src/main/java/com/example/core/database/dao/TodoDao.kt
app/src/main/java/com/example/core/database/dao/DiaryDao.kt
app/src/main/java/com/example/core/database/dao/ShopItemDao.kt
app/src/main/java/com/example/core/database/dao/MottoDao.kt
app/src/main/java/com/example/core/database/dao/DayReviewDao.kt
```

**Repositories** (6):
```
app/src/main/java/com/example/core/repository/IdeaRepository.kt
app/src/main/java/com/example/core/repository/TodoRepository.kt
app/src/main/java/com/example/core/repository/DiaryRepository.kt
app/src/main/java/com/example/core/repository/ShopItemRepository.kt
app/src/main/java/com/example/core/repository/MottoRepository.kt
app/src/main/java/com/example/core/repository/DayReviewRepository.kt
```

**Screens** (7):
```
app/src/main/java/com/example/ui/screens/MoreScreen.kt
app/src/main/java/com/example/ui/screens/IdeasScreen.kt
app/src/main/java/com/example/ui/screens/TodoScreen.kt
app/src/main/java/com/example/ui/screens/DiaryScreen.kt
app/src/main/java/com/example/ui/screens/ShopListScreen.kt
app/src/main/java/com/example/ui/screens/MottoManagementScreen.kt
app/src/main/java/com/example/ui/screens/DayReviewScreen.kt
```

**Components** (3):
```
app/src/main/java/com/example/ui/components/MottoCard.kt
app/src/main/java/com/example/ui/components/DayReviewCard.kt
app/src/main/java/com/example/ui/components/DayReviewDialog.kt
```

### Modified Files (7)

```
app/src/main/java/com/example/core/database/AppDatabase.kt           — add entities, version 11→12
app/src/main/java/com/example/core/database/entity/TaskEntity.kt     — add linkedTodoId field
app/src/main/java/com/example/ui/viewmodel/MainViewModel.kt          — add all 6 feature repos + logic
app/src/main/java/com/example/MainActivity.kt                        — add 5th nav tab + MoreScreen + notification handling
app/src/main/java/com/example/ui/screens/PlannerScreen.kt            — MottoCard, DayReviewCard, todo weekly integration, settings
app/src/main/java/com/example/core/receiver/ReminderReceiver.kt      — day review alarm handling + boot reschedule
app/src/main/java/com/example/core/manager/ReminderManager.kt        — add rescheduleDayReviewAlarms() (optional)
```

---

## 8. Implementation Stages

### Stage 1 — Database Foundation
*Prerequisite: none*

**Files**: 8 entities + 8 DAOs + TaskEntity.linkedTodoId + AppDatabase bump.

**Validation**: `./gradlew assembleDebug` compiles. DB creates all new tables.

### Stage 2 — Business Logic Layer
*Prerequisite: Stage 1*

**Files**: 6 repositories + MainViewModel additions (StateFlows, CRUD, Idea→Planner, Todo↔Task sync, Motto caching, Day Review alarm logic, Backup extension).

**Validation**: Compiles. All ViewModel functions callable.

### Stage 3 — Navigation Shell
*Prerequisite: Stage 2*

**Files**: MoreScreen.kt + MainActivity.kt modifications.

**Validation**: Run app → see "More" tab → see 6 tiles → each shows "Coming Soon" → back works.

### Stage 4 — Idea List
*Prerequisite: Stage 3*

**Files**: IdeasScreen.kt

**Validation**: Create group → add ideas → add stages → toggle stages → add to planner → see in daily view.

### Stage 5 — To-Do List
*Prerequisite: Stage 3*

**Files**: TodoScreen.kt + PlannerScreen modifications (weekly view).

**Validation**: Add todo → link to date → see task in planner → complete task → todo also done.

### Stage 6 — Diary
*Prerequisite: Stage 3*

**Files**: DiaryScreen.kt

**Validation**: Write markdown → preview → navigate away → content persists.

### Stage 7 — Shop List
*Prerequisite: Stage 3*

**Files**: ShopListScreen.kt

**Validation**: Add items → mark purchased → hidden → filter shows them.

### Stage 8 — Mottos
*Prerequisite: Stage 3 + Stage 2*

**Files**: MottoManagementScreen.kt + MottoCard.kt + PlannerScreen modifications.

**Validation**: Add mottos → see random one in daily view → scroll → hides → scroll back → visible.

### Stage 9 — Day Review
*Prerequisite: Stage 3 + Stage 2*

**Files**: DayReviewScreen.kt + DayReviewCard.kt + DayReviewDialog.kt + SettingsDialog changes + ReminderReceiver changes.

**Validation**: Set reminder time → fill review → see card → close app → notification fires → tap → opens review.

### Stage 10 — Polish & Backup
*Prerequisite: Stages 4–9*

**Files**: MainViewModel backup methods + final edge case testing.

**Validation**: Full backup/restore includes all new data. All edge cases tested.

---

## 9. Dependency Graph

```
Stage 1 (DB Foundation)
    ↓
Stage 2 (Repos + ViewModel)
    ↓
Stage 3 (Nav Shell)
    ↓
 ┌──┬──┬──┬──┬──┬──┐
 4  5  6  7  8  9    ← can be in ANY order
 └──┴──┴──┴──┴──┴──┘
    ↓
Stage 10 (Polish & Backup)
```

Stages 4–9 are **independent** — can be done in parallel with multiple agents.
