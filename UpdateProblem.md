# Day Review Enhancement Plan (Update Problem)

## Overview

The current Day Review feature has several limitations:

1. **Always shown** in daily planner view regardless of user settings
2. **No notification system** to remind users to fill their day review at the time of day they prefer
3. **No persistent prompt** when app is opened after the review time has passed

This plan addresses these gaps by implementing a controlled, opt-in notification system that respects user preferences while enhancing usability.

## Goals

1. **Remove always-visible DayReviewCard** from DailyPlannerView — no longer shown by default
2. **Allow users to enable Day Review Reminder** in SettingsDialog with custom time
3. **Schedule daily alarm** at user-specified time — sends system notification AND shows in-app Snackbar
4. **Show in-app Snackbar prompt** every time the app is opened after the review time (until reviewed)
5. **When user fills the review** — both the system notification and Snackbar go away
6. **Skip entirely** if user already reviewed today before the alarm time
7. **Provide 10-second auto-dismiss** for the Snackbar on each occurrence

## Behavior Specification (Confirmed)

### Scenarios

| Scenario | System Notification | In-app Snackbar | Notes |
|----------|-------------------|-----------------|-------|
| Alarm fires, app in foreground | ✅ Sent to drawer | ✅ Shows immediately | Both happen simultaneously |
| Alarm fires, app in background | ✅ Sent to drawer | ✅ Shows when user taps notification → app opens | Notification tap opens app → Snackbar appears |
| User opens app hours after review time (not reviewed) | ❌ (already sent earlier) | ✅ Shows immediately | App checks: is it past review time? Any review saved? → Show Snackbar |
| User reviewed today BEFORE alarm time | ❌ Skipped | ❌ Skipped | Alarm fires → checks review existence → skips |
| User reviews today AFTER notification + Snackbar appeared | ✅ Cancel from drawer | ✅ Dismiss | When `saveDayReview` completes, cancel notification + clear prompt |
| Reminder disabled in settings | ❌ Never | ❌ Never | No alarm scheduled, no checks |
| User opens app repeatedly without reviewing | ❌ (already sent) | ✅ Reappears every time | Snackbar shows on each open until review is saved |

### Core Logic Flow

```
┌──────────────────────────────────────────────────────────┐
│ MainViewModel.checkDayReviewPrompt()                     │
│ Called on: app resume, app open, alarm trigger           │
├──────────────────────────────────────────────────────────┤
│ 1. Is reminder enabled?              → No  → STOP        │
│ 2. Does today's review exist?        → Yes → STOP        │
│ 3. Has review time passed today?     → No  → STOP        │
│ 4. → Set _showDayReviewPrompt = true                     │
└──────────────────────────────────────────────────────────┘

When _showDayReviewPrompt becomes true:
  • MainActivity Snackbar appears (if in foreground)
  • Alarm manager fires → ReminderReceiver → system notification

When saveDayReview completes:
  • _showDayReviewPrompt = false → Snackbar dismissed
  • notificationManager.cancel(5000) → system notification removed
  • prefs: reviewed_today = true (cache flag)

When date changes (midnight):
  • prefs: reviewed_today = false (reset for new day)
```

## Solution Overview

### File Changes Required

**1. `PlannerScreen.kt` – DailyPlannerView**
- Remove `import com.example.ui.components.DayReviewCard`
- Remove `DayReviewCard` composable + `reviewForDate()` flow collection from Column
- Remove `showDayReviewDialog` state + `DayReviewScreen` dialog (no longer used — overlay replaces it)

**2. `MainViewModel.kt` – Day Review Logic**

New functions:
- `scheduleDayReviewAlarm(context)` → sets repeating AlarmManager at user time
- `cancelDayReviewAlarm(context)` → cancels the alarm
- `checkAndTriggerDayReviewPrompt()` → evaluates conditions (enabled, time passed, not reviewed) → sets `_showDayReviewPrompt`
- `dismissDayReviewPrompt()` → clears prompt + cancels notification

New state:
- `_showDayReviewPrompt: MutableStateFlow<Boolean>` → exposed as `showDayReviewPrompt: StateFlow<Boolean>`
- `_reviewedTodayCache: Boolean` → SharedPrefs flag set on review save, cleared on date change

Modified functions:
- `updateReviewReminderEnabled(true)` → also calls `scheduleDayReviewAlarm()`
- `updateReviewReminderEnabled(false)` → also calls `cancelDayReviewAlarm()`
- `updateReviewReminderTime()` → also calls `cancelDayReviewAlarm()` + `scheduleDayReviewAlarm()`
- `saveDayReview()` → after saving, calls `dismissDayReviewPrompt()` and sets `reviewed_today = true` flag
- `refreshSystemDate()` → on date change, reset `reviewed_today = false`

Integration hook:
- `checkAndTriggerDayReviewPrompt()` is called in `onResume()` via MainActivity

**3. `ReminderReceiver.kt` – Day Review Actions**

In `onReceive()`, add BEFORE the existing event reminder logic:
```kotlin
if (action == "com.example.action.DAY_REVIEW") {
    handleDayReview(context)
    return
}
```

New function `handleDayReview(context)`:
- Uses `goAsync()` + `CoroutineScope(Dispatchers.IO).launch` (same pattern as lines 53-75 for night-before events)
- Registers `day_review_reminder` notification channel (IMPORTANCE_DEFAULT)
- Queries DB via `AppDatabase.getDatabase(context).dayReviewDao().getReviewForDateSync(todayStr)` to check if today's review exists
- If review exists → do nothing (skip notification + return)
- If review doesn't exist → build notification with PendingIntent → MainActivity + `open_day_review=true` → `notificationManager.notify(5000, notification)`
- **LocalBroadcast**: After sending notification, also broadcasts `com.example.action.DAY_REVIEW_TRIGGERED` via `LocalBroadcastManager` → picked up by `MainActivity.onResume()` → calls `checkAndTriggerDayReviewPrompt()` immediately
- Finally block: `pendingResult.finish()`
- Notification ID: 5000

Also extend boot/time-change/timezone-change block:
```kotlin
if (prefs.getBoolean("review_reminder_enabled", false)) {
    val time = prefs.getString("review_reminder_time", "21:00") ?: "21:00"
    // Re-schedule using same logic as MainViewModel.scheduleDayReviewAlarm()
}
```

**4. `MainActivity.kt` – Snackbar & Day Review Prompt**

Scaffold additions:
- Add `SnackbarHostState` + `SnackbarHost(snackbarHostState)` to the existing Scaffold

**Runtime permissions (inside SettingsDialog composable, triggered on enable toggle, NOT on app start):**
```kotlin
@Composable
fun SettingsDayReviewSection(viewModel: MainViewModel) {
    val context = LocalContext.current

    // Android 13+ (API 33): POST_NOTIFICATIONS — runtime permission
    val postNotificationsLauncher = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(context, "Notifications disabled — enable in Settings", Toast.LENGTH_LONG).show()
            }
        }
    } else null

    // Called when user taps the enable switch
    val onToggleReminderEnabled: (Boolean) -> Unit = { enabled ->
        viewModel.updateReviewReminderEnabled(enabled)
        if (enabled) {
            // Request POST_NOTIFICATIONS on API 33+
            postNotificationsLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)

            // Request SCHEDULE_EXACT_ALARM on API 31-32 (on API 33+ this is auto-granted)
            if (Build.VERSION.SDK_INT in Build.VERSION_CODES.S until Build.VERSION_CODES.TIRAMISU) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!alarmManager.canScheduleExactAlarms()) {
                    context.startActivity(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                }
            }
        }
    }

    // ... existing SettingsDialog UI with onToggleReminderEnabled ...
}
```

Intent handling:
```kotlin
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    if (intent.getBooleanExtra("open_day_review", false)) {
        viewModel.checkAndTriggerDayReviewPrompt()
    }
}
```

Also check in `onCreate()` after ViewModel init:
```kotlin
if (intent.getBooleanExtra("open_day_review", false)) {
    viewModel.checkAndTriggerDayReviewPrompt()
}
```

**LocalBroadcast receiver for in-foreground alarm trigger:**
```kotlin
private val dayReviewTriggeredReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (::viewModel.isInitialized) {
            viewModel.checkAndTriggerDayReviewPrompt()
        }
    }
}
```

Register in `onResume()` (add to existing), unregister in `onPause()`:
```kotlin
override fun onResume() {
    super.onResume()
    if (::viewModel.isInitialized) {
        viewModel.refreshSystemDate()
        viewModel.checkAndTriggerDayReviewPrompt() // NEW
    }
    LocalBroadcastManager.getInstance(this)
        .registerReceiver(dayReviewTriggeredReceiver, IntentFilter("com.example.action.DAY_REVIEW_TRIGGERED"))
}

override fun onPause() {
    super.onPause()
    LocalBroadcastManager.getInstance(this).unregisterReceiver(dayReviewTriggeredReceiver)
}
```

Composite Snackbar + overlay logic:
```kotlin
val showPrompt by viewModel.showDayReviewPrompt.collectAsState()
var showDayReviewOverlay by rememberSaveable { mutableStateOf(false) }

LaunchedEffect(showPrompt) {
    if (showPrompt) {
        val result = snackbarHostState.showSnackbar(
            message = "Time to review your day!",
            actionLabel = "Review",
            duration = SnackbarDuration.Long
        )
        if (result == SnackbarResult.ActionPerformed) {
            showDayReviewOverlay = true
        }
        // After Snackbar dismisses (action, timeout, or swipe), don't clear prompt yet
        // Prompt clears only when review is saved
    } else {
        showDayReviewOverlay = false
        snackbarHostState.currentSnackbarData?.dismiss()
    }
}

// Overlay DayReviewScreen on top of current content
if (showDayReviewOverlay) {
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    DayReviewScreen(
        viewModel = viewModel,
        initialDate = todayDate,
        onBack = { showDayReviewOverlay = false }
    )
}
```

**5. `MoreScreen.kt` – No Changes**
- The overlay approach avoids routing complexity

### Technical Details

#### Notification Channel
Uses shared helper `createDayReviewChannel(context)` (idempotent, safe to call from any context — receiver, alarm, or activity):
```kotlin
fun createDayReviewChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "day_review_reminder",
            "Day Review Reminder",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily reminder to review your day"
        }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }
}
```

#### Predicate Logic for `checkAndTriggerDayReviewPrompt`
```kotlin
fun checkAndTriggerDayReviewPrompt() {
    if (!_reviewReminderEnabled.value) return
    if (_showDayReviewPrompt.value) return // already showing, skip duplicate

    // Check prefs cache first (faster than DB query)
    val todayStr = getTodayDateString()
    val cachedToday = prefs.getString("reviewed_today_date", "")
    val reviewedTodayCache = prefs.getBoolean("reviewed_today", false)
    if (cachedToday == todayStr && reviewedTodayCache) {
        _showDayReviewPrompt.value = false
        return
    }

    // Check if review time has passed
    val now = Calendar.getInstance()
    val parts = _reviewReminderTime.value.split(":")
    val reviewHour = parts.getOrNull(0)?.toIntOrNull() ?: 21
    val reviewMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    val currentHour = now.get(Calendar.HOUR_OF_DAY)
    val currentMinute = now.get(Calendar.MINUTE)
    val isAfterReviewTime = currentHour > reviewHour || 
        (currentHour == reviewHour && currentMinute >= reviewMinute)

    if (!isAfterReviewTime) return

    // Check DB (async via viewModelScope)
    viewModelScope.launch {
        val review = dayReviewRepository.getReviewForDate(todayStr).first()
        if (review != null) {
            // Already reviewed — cache this fact
            prefs.edit()
                .putBoolean("reviewed_today", true)
                .putString("reviewed_today_date", todayStr)
                .apply()
            _showDayReviewPrompt.value = false
        } else {
            _showDayReviewPrompt.value = true
        }
    }
}
```

#### Snackbar Configuration
```kotlin
LaunchedEffect(showPrompt) {
    if (showPrompt) {
        val result = snackbarHostState.showSnackbar(
            message = "Time to review your day!",
            actionLabel = "Review",
            duration = SnackbarDuration.Long // ~10 seconds
        )
        if (result == SnackbarResult.ActionPerformed) {
            showDayReviewOverlay = true
        }
        // Don't clear prompt here — it clears on save or next day
    } else {
        showDayReviewOverlay = false
        snackbarHostState.currentSnackbarData?.dismiss()
    }
}
```

#### Overlay DayReviewScreen
- Rendered as a full-screen Box at the MainActivity level, on top of the tab content
- Uses `AnimatedVisibility` or simple `if` block
- Back button closes overlay (returns to underlying tab)
- When review is saved → clears `_showDayReviewPrompt` → `showDayReviewOverlay = false`

```kotlin
if (showDayReviewOverlay) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DayReviewScreen(
            viewModel = viewModel,
            initialDate = todayDate,
            onBack = {
                showDayReviewOverlay = false
                viewModel.dismissDayReviewPrompt()
            }
        )
    }
}
```

#### Shared Helper — Notification Channel & PendingIntent Flag
```kotlin
// Create day review reminder channel (safe to call multiple times — idempotent)
fun createDayReviewChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "day_review_reminder",
            "Day Review Reminder",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily reminder to review your day"
        }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }
}

// FLAG_IMMUTABLE: required on API 31+ (Android 12+); crashes on older if used
fun getImmutableFlag(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0
}
```

#### Scheduling Logic
```kotlin
fun scheduleDayReviewAlarm(context: Context) {
    if (!_reviewReminderEnabled.value) return

    val time = _reviewReminderTime.value
    val parts = time.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 21
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (before(Calendar.getInstance())) {
            add(Calendar.DAY_OF_YEAR, 1)
            // Time has passed today — send immediate one-shot notification
            sendImmediateDayReviewNotification(context)
        }
    }

    val intent = Intent(context, ReminderReceiver::class.java).apply {
        action = "com.example.action.DAY_REVIEW"
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context, 5000, intent,
        getImmutableFlag() or PendingIntent.FLAG_UPDATE_CURRENT
    )
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP,
        cal.timeInMillis,
        AlarmManager.INTERVAL_DAY,
        pendingIntent
    )
}

fun cancelDayReviewAlarm(context: Context) {
    val intent = Intent(context, ReminderReceiver::class.java).apply {
        action = "com.example.action.DAY_REVIEW"
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context, 5000, intent,
        getImmutableFlag() or PendingIntent.FLAG_UPDATE_CURRENT
    )
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
    pendingIntent.cancel()
}

fun sendImmediateDayReviewNotification(context: Context) {
    createDayReviewChannel(context)

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra("open_day_review", true)
    }
    val pendingIntent = PendingIntent.getActivity(context, 5001, intent, getImmutableFlag())

    val notification = NotificationCompat.Builder(context, "day_review_reminder")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Day Review Reminder")
        .setContentText("Time to review your day!")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(5000, notification)
}
```

#### Dismissal After Review Save
```kotlin
fun saveDayReview(date: String, good: String, bad: String, improve: String,
                  gratitude: String, moodRating: Int, score: Int, notes: String) {
    viewModelScope.launch {
        val existing = dayReviewRepository.getReviewForDate(date).first()
        if (existing != null) {
            dayReviewRepository.insertReview(existing.copy(...))
        } else {
            dayReviewRepository.insertReview(DayReviewEntity(date = date, ...))
        }

        // Save reviewed-today cache
        prefs.edit()
            .putBoolean("reviewed_today", true)
            .putString("reviewed_today_date", date)
            .apply()

        // Dismiss prompt
        _showDayReviewPrompt.value = false

        // Cancel system notification
        val notificationManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(5000)
    }
}
```

#### Date-Change Reset (in refreshSystemDate)
```kotlin
fun refreshSystemDate() {
    val todayStr = getTodayDateString()
    if (_lastDate != todayStr) {
        // ...existing date change logic...
        _lastDate = todayStr

        // Reset reviewed-today cache for new day
        prefs.edit()
            .putBoolean("reviewed_today", false)
            .putString("reviewed_today_date", "")
            .apply()
    }
}
```

## UX Flow (Detailed)

```
SETUP:
User enables "Day Review Reminder" in Settings
  → Sets time (e.g., 21:00)
  → Saves → alarm scheduled daily at 21:00

DAY WITHOUT REVIEW:
  Before 21:00: nothing happens
  21:00: alarm fires
    → ReminderReceiver checks: review exists today? No
    → Sends system notification (drawer) NOT shown
    → If app is foreground → Snackbar appears: "Time to review your day! [Review]"
    → If app is background → notification sits in drawer

  User opens app at 23:00 (after 21:00, no review):
    → onResume() → checkAndTriggerDayReviewPrompt()
    → Review time passed ✅, no review exists ✅
    → _showDayReviewPrompt = true
    → Snackbar appears: "Time to review your day! [Review]"
    → Auto-dismisses after 10s
    → User continues using app

  User opens app again at 23:30 (still no review):
    → Same check → Snackbar appears again
    → (Repeats on every open until reviewed)

  User taps [Review] on Snackbar:
    → DayReviewScreen overlay opens on current tab
    → User fills fields, taps SAVE & CLOSE
    → saveDayReview() completes:
      → reviewed_today = true (prefs cache)
      → _showDayReviewPrompt = false (Snackbar dismisses)
      → notificationManager.cancel(5000) (notification removed from drawer)
      → Overlay closes → back to previous tab

DAY WITH REVIEW BEFORE ALARM:
  User fills review manually at 15:00
  → 21:00: alarm fires
  → ReminderReceiver checks: review exists today? Yes
  → Skips notification, returns early
  → Nothing happens

DAY AFTER REVIEW:
  Midnight → refreshSystemDate() resets reviewed_today flag
  Next day's alarm at 21:00 → same flow
```

## Benefits

1. **Controlled**: Only shown when enabled by user
2. **Respectful**: Standard, non-intrusive notification
3. **Convenient**: Overlay keeps user in current context
4. **Time-aware**: Respects user-selected time slot
5. **Persistent**: Reappears on each app open until reviewed (not just once)
6. **Simple**: Minimal navigation complexity
7. **Progressive**: Automatically cancels notification + Snackbar after review is saved

## Edge Cases Handled

| Edge Case | Handling |
|-----------|----------|
| App killed → alarm fires | Notification still sent (AlarmManager independent of app process) |
| Boot complete | `ReminderReceiver` reschedules day review alarm |
| Time changed / timezone changed | `ACTION_TIME_CHANGED` + `ACTION_TIMEZONE_CHANGED` trigger reschedule |
| Time changed to before review time | No leftover state — check reruns on next app open |
| User opens app hours after review time | Snackbar shows (check: time passed + no review) |
| User reviewed before alarm time | Alarm fires → check → skip notification |
| User reviews after notification | Cancel notification from drawer + dismiss Snackbar |
| User reviews from Manual More tab | Same as above — `saveDayReview()` always dismisses prompt |
| App opened repeatedly without reviewing | Snackbar reappears every time |
| Snackbar showing + user on different tab | Works — rendered at Scaffold level (above all tabs) |
| Overlay shown + user rotates | State preserved via `rememberSaveable` |
| Overlay open + app resumes (background→foreground) | `checkAndTriggerDayReviewPrompt()` guards against duplicate with `if (_showDayReviewPrompt.value) return` |
| Reminder disabled mid-day | Cancel alarm immediately |
| Phone in DND mode | Notification respects DND (IMPORTANCE_DEFAULT, no full-screen) |
| Alarm fires while app in foreground | `ReminderReceiver` sends LocalBroadcast → `MainActivity.onResume()` receiver calls `checkAndTriggerDayReviewPrompt()` → Snackbar appears immediately alongside system notification |
| Enabling reminder after review time passed | Alarm scheduled for tomorrow + immediate one-shot notification sent now |
| DB query in BroadcastReceiver | Uses existing `goAsync()` + `CoroutineScope(Dispatchers.IO)` + `pendingResult.finish()` pattern (lines 53-75) |
| `PendingIntent.FLAG_IMMUTABLE` on API < 31 | Version-guarded: `getImmutableFlag()` returns 0 on API < 31, `FLAG_IMMUTABLE` on API 31+ |
| POST_NOTIFICATIONS denied (API 33+) | Toast shown explaining how to re-enable in Settings; no crash |
| SCHEDULE_EXACT_ALARM denied (API 31-32) | `setRepeating()` inexact anyway; alarm still scheduled, may be delayed |
| Channel creation duplicated | Factored out `createDayReviewChannel()` shared helper (idempotent, safe from any context) |
| Two separate `LaunchedEffect` blocks | Consolidated into single `LaunchedEffect` with `if/else` for show/dismiss |

## Files Modified

1. `app/src/main/java/com/example/ui/screens/PlannerScreen.kt`
2. `app/src/main/java/com/example/ui/viewmodel/MainViewModel.kt`
3. `app/src/main/java/com/example/core/receiver/ReminderReceiver.kt`
4. `app/src/main/java/com/example/MainActivity.kt`
5. `app/src/main/java/com/example/ui/screens/MoreScreen.kt` (none)
6. `app/src/main/AndroidManifest.xml` — add `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM` permissions

## Implementation Key Points

### `PlannerScreen.kt`
- Remove DayReviewCard import + composable + `reviewForDate()` collect
- Remove `showDayReviewDialog` + `DayReviewScreen` dialog (overlay replaces it)
- Add `POST_NOTIFICATIONS` permission launcher in SettingsDialog (API 33+, triggered on enable toggle)
- Add `SCHEDULE_EXACT_ALARM` settings redirect in SettingsDialog (API 31-32, triggered on enable toggle)

### `MainViewModel.kt`
- New: `scheduleDayReviewAlarm(context)`, `cancelDayReviewAlarm(context)`
- New: `sendImmediateDayReviewNotification(context)` — one-shot notif when enabling after the day's review time
- New: `createDayReviewChannel(context)` — shared idempotent channel creation helper
- New: `getImmutableFlag()` — version-safe `PendingIntent.FLAG_IMMUTABLE` (returns 0 on API < 31)
- New: `_showDayReviewPrompt: MutableStateFlow<Boolean>`
- New: `checkAndTriggerDayReviewPrompt()` — predicate logic with time + review existence + duplicate guard
- New: `dismissDayReviewPrompt()` — clears prompt + cancels notification
- Modify: `saveDayReview()` → also cache `reviewed_today` + dismiss prompt + cancel notification
- Modify: `refreshSystemDate()` → reset `reviewed_today` cache on date change
- Modify: `updateReviewReminderEnabled/time()` → reschedule alarm (and send immediate notif if time already passed)

### `ReminderReceiver.kt`
- New handler: `action == "com.example.action.DAY_REVIEW"` → `handleDayReview(context)`
- New function: `handleDayReview()` → `goAsync()` + coroutine + `AppDatabase.getDatabase(context).dayReviewDao().getReviewForDateSync()` → skip or send notification + `LocalBroadcastManager.sendBroadcast(DAY_REVIEW_TRIGGERED)`
- Extend boot/time-change/timezone-change: reschedule day review alarm if enabled

### `MainActivity.kt`
- Add `SnackbarHostState` + `SnackbarHost` to existing Scaffold
- Add `onNewIntent()` override for `open_day_review` extra
- Add `showDayReviewOverlay: rememberSaveable` state + `DayReviewScreen` overlay rendering
- Add `dayReviewTriggeredReceiver` BroadcastReceiver registered in `onResume()`, unregistered in `onPause()` → calls `checkAndTriggerDayReviewPrompt()`
- Add single consolidated `LaunchedEffect(showPrompt)` → Snackbar (if true) or dismiss (if false) + overlay management
- Modify `onResume()` → also call `viewModel.checkAndTriggerDayReviewPrompt()` + register LocalBroadcast receiver
- Modify `onPause()` → unregister LocalBroadcast receiver
- Modify `onCreate()` → check intent for `open_day_review`

### `AndroidManifest.xml`
- Add `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />` (API 33+)
- Add `<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />` (API 31+)

---

## Implementation Stages (Execution Order)

### Stage 1: `PlannerScreen.kt` — Cleanup + SettingsDialog Permissions
- [x] Remove `import com.example.ui.components.DayReviewCard`
- [x] Remove `DayReviewCard` composable + `reviewForDate()` flow collection from Column
- [x] Remove `showDayReviewDialog` state + `DayReviewScreen` dialog
- [x] Add `POST_NOTIFICATIONS` permission launcher inside SettingsDialog composable (`rememberLauncherForActivityResult`, triggered on enable toggle, not on composition)
- [x] Add `SCHEDULE_EXACT_ALARM` settings redirect inside SettingsDialog enable toggle (API 31-32)
- [x] **VERIFY**: `.\gradlew.bat assembleDebug` — builds successfully

### Stage 2: `MainViewModel.kt` — Shared Helpers + Alarm + Prompt State
- [x] Add `createDayReviewChannel(context)` — idempotent notification channel helper
- [x] Add `getImmutableFlag()` — version-safe `PendingIntent.FLAG_IMMUTABLE` (returns 0 on API < 31)
- [x] Add `scheduleDayReviewAlarm(context)` — `AlarmManager.setRepeating()` with `RTC_WAKEUP`, 24h interval, PendingIntent → `ReminderReceiver` (requestCode=5000)
- [x] Add `cancelDayReviewAlarm(context)` — cancel alarm + cancel PendingIntent
- [x] Add `sendImmediateDayReviewNotification(context)` — one-shot notification (ID=5000), uses `createDayReviewChannel()` + `getImmutableFlag()` + properly declares `notificationManager`
- [x] Add `_showDayReviewPrompt: MutableStateFlow<Boolean>` — exposed as `showDayReviewPrompt: StateFlow<Boolean>`
- [x] Add `checkAndTriggerDayReviewPrompt()` — enabled guard, prefs cache fast-path, time-past check, DB query for today's review
- [x] Add `dismissDayReviewPrompt()` — sets `_showDayReviewPrompt = false` + `notificationManager.cancel(5000)`
- [x] Modify `saveDayReview()` — after DB upsert, cache `reviewed_today=true` in prefs, call `dismissDayReviewPrompt()`
- [x] Modify `refreshSystemDate()` — on date change, reset `reviewed_today=false` in prefs
- [x] Modify `updateReviewReminderEnabled(true)` → calls `scheduleDayReviewAlarm()`
- [x] Modify `updateReviewReminderEnabled(false)` → calls `cancelDayReviewAlarm()`
- [x] Modify `updateReviewReminderTime()` → calls `cancelDayReviewAlarm()` + `scheduleDayReviewAlarm()`
- [x] **VERIFY**: `.\gradlew.bat assembleDebug` — builds successfully

### Stage 3: `ReminderReceiver.kt` — DAY_REVIEW Handler
- [x] In `onReceive()`, add `action == "com.example.action.DAY_REVIEW"` check BEFORE existing event reminder logic → `handleDayReview(context); return`
- [x] Add `handleDayReview(context)`:
  - `goAsync()` → `CoroutineScope(Dispatchers.IO).launch`
  - `createDayReviewChannel(context)`
  - Query DB: `AppDatabase.getDatabase(context).dayReviewDao().getReviewForDate(todayStr).first()`
  - If review exists → `return@launch`
  - Build notification with PendingIntent → `MainActivity` + `open_day_review=true`
  - `context.sendBroadcast(Intent("com.example.action.DAY_REVIEW_TRIGGERED"))`
  - `notificationManager.notify(5000, builder.build())`
  - `finally { pendingResult.finish() }`
- [x] Extend boot/time-change/timezone-change block: if `review_reminder_enabled` prefs → reschedule alarm
- [x] **VERIFY**: `.\gradlew.bat assembleDebug` — builds successfully

### Stage 4: `MainActivity.kt` — Snackbar + Overlay + Broadcast + Intent
- [x] Add `SnackbarHostState` + `SnackbarHost(snackbarHostState)` to existing Scaffold
- [x] Add `dayReviewTriggeredReceiver` — `BroadcastReceiver` that calls `viewModel.checkAndTriggerDayReviewPrompt()` when action is `DAY_REVIEW_TRIGGERED`
- [x] Modify `onResume()` — register `dayReviewTriggeredReceiver` via `registerReceiver(...)`, add `viewModel.refreshSystemDate()` + `viewModel.checkAndTriggerDayReviewPrompt()`
- [x] Add `override fun onPause()` — `unregisterReceiver(dayReviewTriggeredReceiver)`
- [x] Add `showDayReviewOverlay: Boolean` state (`remember { mutableStateOf(false) }`)
- [x] Add single consolidated `LaunchedEffect(showPrompt)`:
  - If `true`: show Snackbar ("Time to review your day!" / "Review" action) → on action → `showDayReviewOverlay = true`
  - If `false`: `showDayReviewOverlay = false` + dismiss Snackbar
- [x] Add overlay rendering below Scaffold: `if (showDayReviewOverlay) { Box + DayReviewScreen(...) }`
  - `DayReviewScreen(viewModel, todayDate, onBack = { showDayReviewOverlay = false; viewModel.dismissDayReviewPrompt() })`
- [x] Override `onNewIntent(intent)` — if `open_day_review` extra → call `viewModel.checkAndTriggerDayReviewPrompt()`
- [x] In `onCreate()` after ViewModel init — check `intent.getBooleanExtra("open_day_review", false)` → call `viewModel.checkAndTriggerDayReviewPrompt()`
- [x] **VERIFY**: `.\gradlew.bat assembleDebug` — builds successfully

### Stage 5: `AndroidManifest.xml` — Permissions
- [x] Add `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />` inside `<manifest>`
- [x] Add `<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />` inside `<manifest>`
- [x] **VERIFY**: `.\gradlew.bat assembleDebug` — builds successfully

### Stage 6: Final Build & Cross-Check
- [x] Full build: `.\gradlew.bat assembleDebug` — **BUILD SUCCESSFUL**
- [x] Verify DayReviewCard is gone from DailyPlannerView
- [x] Verify SettingsDialog has Day Review Reminder section with enable switch and time picker
- [x] Verify all edge cases from table are covered by the implementation
- [x] Confirm no dead code or orphaned imports

---

## MoreScreen Double TopAppBar Fix

### Issue
When navigating to any sub-screen in the More tab (To-Do, Ideas, Diary, etc.), the screen title appears **twice** because:
1. `MoreScreen.kt` wraps each sub-screen in a `Column` with its own `TopAppBar` showing `currentScreen.label` (lines 55-65)
2. Every sub-screen (`TodoScreen`, `IdeasScreen`, `DiaryScreen`, `ShopListScreen`, `MottoManagementScreen`, `DayReviewScreen`) **already has its own** `TopAppBar` with title + back button

**Before fix UI:**
```
┌─ TopAppBar (MoreScreen wrapper) ──┐
│  ←  To-Do                         │
├───────────────────────────────────┤
│  ┌─ TopAppBar (TodoScreen) ────┐  │
│  │  ←  To-Do List              │  │
│  └─────────────────────────────┘  │
│  [actual content...]              │
└───────────────────────────────────┘
```

### Fix
Remove the wrapper `TopAppBar` and its enclosing `Column` from `MoreScreen.kt`'s sub-screen branch. Render sub-screens directly — each already provides its own `TopAppBar` with back navigation.

**File:** `app/src/main/java/com/example/ui/screens/MoreScreen.kt`

**Change:**
```kotlin
// BEFORE (lines 53-93)
if (currentScreen !is MoreSubScreen.None) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(                            // ← REMOVE this entire TopAppBar
            title = { Text(currentScreen.label) },
            navigationIcon = {
                IconButton(onClick = { currentScreen = MoreSubScreen.None }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        when (currentScreen) { ... sub-screens ... }
    }
}

// AFTER
if (currentScreen !is MoreSubScreen.None) {
    when (currentScreen) {                    // ← Directly render sub-screens
        is MoreSubScreen.Ideas -> IdeasScreen(viewModel, onBack = { currentScreen = MoreSubScreen.None })
        is MoreSubScreen.Todo -> TodoScreen(viewModel, onBack = { currentScreen = MoreSubScreen.None })
        is MoreSubScreen.Diary -> DiaryScreen(viewModel, onBack = { currentScreen = MoreSubScreen.None })
        is MoreSubScreen.ShopList -> ShopListScreen(viewModel, onBack = { currentScreen = MoreSubScreen.None })
        is MoreSubScreen.Mottos -> MottoManagementScreen(viewModel, onBack = { currentScreen = MoreSubScreen.None })
        is MoreSubScreen.DayReview -> DayReviewScreen(viewModel, onBack = { currentScreen = MoreSubScreen.None })
        else -> {}
    }
}
```

**After fix UI:**
```
┌─ TopAppBar (TodoScreen) ──────────┐
│  ←  To-Do List                    │
├───────────────────────────────────┤
│  [actual content...]              │
└───────────────────────────────────┘
```

### Dev Notes
- `currentScreen` state reset is still handled by each sub-screen's `onBack` callback
- No sub-screen references are broken — they all accept `viewModel` and `onBack`
- Build verification: `.\gradlew.bat assembleDebug`
