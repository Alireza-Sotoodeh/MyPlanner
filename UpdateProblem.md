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

---

## TopAppBar → Custom Row Header Replacement (UI Consistency Fix)

### Issue
After the Day Review notification enhancement, the More tab and all its sub-screens use Material3 `TopAppBar`, while PlannerScreen and PomodoroScreen use custom two-line `Row` headers. This creates two problems:

1. **Visual inconsistency** — other tabs use "small uppercase label + large light title" pattern, More uses a plain bold title
2. **Double status bar padding** — `MainActivity` calls `enableEdgeToEdge()` (`:88`) and wraps all content in `Box(Modifier.fillMaxSize().padding(innerPadding))` (`:140-144`). The Scaffold's `innerPadding` already provides status bar top padding. `TopAppBar` internally adds its own `statusBarsPadding()` → **double padding** creates a visible `space → header` gap.

### Fix
Replace all `TopAppBar` composables with custom `Row` headers following the same two-line pattern used in PlannerScreen's `HeaderSection` (`:427-486`) and PomodoroScreen's header (`:83-104`).

### Pattern Code

**For sub-screens with back button + actions:**
```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(end = 16.dp, top = 12.dp, bottom = 12.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = "LABEL",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Title",
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
    Spacer(Modifier.weight(1f))
    // action buttons...
}
```

**For MoreScreen main grid (no back button):**
```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Column {
        Text(
            text = "MORE FEATURES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "More",
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
```

### Visual Before → After

**More tab (main grid):**
```
[BEFORE]                              [AFTER]
┌─────────────────────┐              ┌─────────────────────┐
│  [status bar]       │              │  [status bar]       │
│                     │◄─ double pad │  MORE FEATURES      │
│  More           (1) │              │  More               │
│  ┌──┬──┬──┐        │              │  ┌──┬──┬──┐         │
│  │Id│To│Di│        │              │  │Id│To│Di│         │
│  └──┴──┴──┘        │              │  └──┴──┴──┘         │
└─────────────────────┘              └─────────────────────┘
```

**Sub-screen (e.g., Ideas):**
```
[BEFORE]                              [AFTER]
┌─────────────────────┐              ┌─────────────────────┐
│  [status bar]       │              │  [status bar]       │
│                     │◄─ double pad │  ← IDEAS           │
│  ← Ideas        +   │              │    Ideas       +   │
│  GroupChipRow       │              │  GroupChipRow       │
│  idea cards ...     │              │  idea cards ...     │
└─────────────────────┘              └─────────────────────┘
```

### Files Modified (7 files)

| File | TopAppBar Lines | New Header |
|------|----------------|------------|
| `MoreScreen.kt` | 83–88 | No back. Label: "MORE FEATURES", Title: "More" |
| `IdeasScreen.kt` | 55–70 | Back + "IDEAS / Ideas" + Group btn |
| `TodoScreen.kt` | 48–56 | Back + "TO-DO / To-Do List" |
| `DiaryScreen.kt` | 107–137 | Back + "DIARY" + date navigator + Delete btn |
| `ShopListScreen.kt` | 49–64 | Back + "SHOP LIST / Shop List" + Add btn |
| `MottoManagementScreen.kt` | 36–51 | Back + "MOTTOS / Mottos" + Add btn |
| `DayReviewScreen.kt` | 80–100 | Back + "DAY REVIEW" + date + Delete btn |

### Label / Title Mapping

| Screen | Label | Title |
|--------|-------|-------|
| More (grid) | `MORE FEATURES` | `More` |
| Ideas | `IDEAS` | `Ideas` |
| To-Do | `TO-DO` | `To-Do List` |
| Diary | `DIARY` | Date navigator (chevrons + date + ●) |
| Shop List | `SHOP LIST` | `Shop List` |
| Mottos | `MOTTOS` | `Mottos` |
| Day Review | `DAY REVIEW` | `formatDisplayDate(currentDate)` |

### DiaryScreen Special Case
DiaryScreen's title area contains a date navigator (ChevronLeft + date text + ● indicator + ChevronRight). The two-line pattern wraps this:
- Line 1: small "DIARY" label
- Line 2: date navigator row (preserves existing chevron + date + ● functionality)

### DayReviewScreen Special Case
The existing TopAppBar title is already two-line ("Day Review" + date), styled differently. Converted to:
- Line 1: small "DAY REVIEW" label (11sp Bold primary)
- Line 2: `formatDisplayDate(currentDate)` (24sp Light onBackground)

### Affected Imports
- `TopAppBar` and `TopAppBarDefaults` imports may become unused in modified files and should be removed where no longer needed
- `Icons.AutoMirrored.Filled.ArrowBack` may already be imported but should be verified
- No new imports needed beyond what's already present (all files already import `Row`, `Column`, `Text`, `Spacer`, etc.)

### Build Verification
`.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL

---

## Diary Screen Enhancement: Live Markdown, Auto-Save, Undo/Redo

### Changes
- Replace Edit/Preview toggle with live in-place markdown transformation via `BasicTextField` + `VisualTransformation`
- Replace 2-second debounce auto-save with 300ms debounce — no "Saved" status text
- Add separate undo/redo stacks for title and content (50 steps each) with icon buttons in header
- Remove `MarkdownPreview` composable and all its sub-composables (replaced by inline `VisualTransformation`)

### Implementation

**File:** `app/src/main/java/com/example/ui/screens/DiaryScreen.kt`

### New Classes/Functions

#### `MarkdownVisualTransformation` (replaces `MarkdownPreview` + `parseInlineMarkdown`)
```kotlin
class MarkdownVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // Per-line processing:
        // - "# " lines → 24.sp Bold
        // - "## " lines → 20.sp Bold
        // - "### " lines → 18.sp SemiBold
        // - "- "/"• "/"1. " lines → primary color prefix
        // - "---" lines → dimmed color
        // - All lines → parse inline tokens: **bold**, *italic*, `code`, ~~strike~~
        return TransformedText(styled, OffsetMapping.Identity)
    }
}
```

#### `HistoryStack`
```kotlin
class HistoryStack(private val maxSize: Int = 50) {
    private val items = mutableListOf<String>()
    private var index = -1
    fun push(item: String) { /* trim redo, add, enforce max */ }
    fun undo(): String? { /* decrement index, return previous */ }
    fun redo(): String? { /* increment index, return next */ }
    val canUndo: Boolean get() = index > 0
    val canRedo: Boolean get() = index < items.size - 1
}
```

### Modified Composables

| Before | After |
|--------|-------|
| `isEditing` state toggling Edit/Preview | Removed — single unified editing view |
| `saveState` + "Saving..."/"Saved ✓" display | Removed — no save status indicator |
| `saveJob` with 2000ms delay | `autoSaveJob` with 300ms delay via `rememberCoroutineScope()` |
| `MainScope().launch` for saves | `scope.launch` (lifecycle-safe) |
| Edit/Preview `FilterChip` row | Removed |
| `OutlinedTextField(content)` with plain text | `BasicTextField(content)` with `MarkdownVisualTransformation` |
| Header with back + date nav + delete | Header with back + date nav + **undo/redo icons** + delete |

### New Imports
- `androidx.compose.ui.text.input.VisualTransformation`
- `androidx.compose.ui.text.input.TransformedText`
- `androidx.compose.ui.text.input.OffsetMapping`
- `androidx.compose.ui.text.font.FontFamily`
- `androidx.compose.ui.text.style.TextDecoration`

### Removed Imports
- `androidx.compose.foundation.lazy.LazyColumn`
- `androidx.compose.foundation.lazy.items`
- `kotlinx.coroutines.MainScope`

### Edge Cases

| Edge Case | Handling |
|-----------|----------|
| Entry loaded from DB | Initial snapshot pushed to both history stacks → can undo back to saved state |
| User types after undoing | Redo branch trimmed, new state pushed |
| History exceeds 50 steps | Oldest entries dropped from front |
| Date navigation | `saveNow()` fires before loading new date; history stacks scoped to `remember` are naturally discarded |
| Back button | `saveNow()` called for final save |
| Empty content / new entry | Empty string pushed as initial history state |
| App backgrounded | `DisposableEffect` triggers save on dispose (same as before) |
| Rapid typing | 300ms debounce prevents redundant DB writes |
| `*` vs `**` at same position | `**` (bold) takes priority over `*` (italic) at same index |
| Unmatched `**` or `*` | Left as plain text — same as current `parseInlineMarkdown` behavior |

---

## IdeasScreen Layout Fix: Add Idea Button Not Visible

### Issue
The "Add Idea" button (full-width `Button` pinned at the bottom of the `Column`) was **not visible** on the emulator because:

- Empty-state `Box(modifier = Modifier.fillMaxSize())` consumed **all** remaining height in the parent `Column`
- The button at line 136 was pushed below the visible viewport (off-screen / behind nav bar)

### Root Cause
```kotlin
// BEFORE (lines 95–134)
if (filteredIdeas.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize(), ...) {  // ← steals ALL space
        // empty state content
    }
} else {
    LazyColumn(modifier = Modifier.weight(1f), ...) { ... }
}
// Box with Add Idea button → pushed off-screen when empty
```

The `fillMaxSize()` on the empty-state Box doesn't respect siblings in the `Column` — it fills the entire parent, pushing the button below the visible bottom edge.

### Fix
Wrap both branches in a `Box(Modifier.weight(1f).fillMaxWidth())` so the content area flexes dynamically, and the button always stays pinned at the bottom:

```kotlin
// AFTER (lines 95–136)
Box(modifier = Modifier.weight(1f).fillMaxWidth()) {  // ← flex container
    if (filteredIdeas.isEmpty()) {
        Box(Modifier.fillMaxSize(), ...) { ... }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), ...) { ... }
    }
}
// Box with Add Idea button → always visible at bottom
```

**File:** `app/src/main/java/com/example/ui/screens/IdeasScreen.kt`

---

## Daily View: "Move to To-Do" and "Turn into Idea" (3-Dot Menu)

### Feature 1: Move to To-Do
**Available for:** All types (TASK, EVENT, NOTE)

**Behavior (MOVE):**
1. Creates a `TodoEntity` with `title = task.title`, `description = task.description + merged subtask titles`, `priority = task.priorityLevel`
2. Deletes the original task + subtasks via `taskRepository.deleteTaskAndSubtasks(task)`

**Edge case — subtasks:** If the task has subtasks, their titles are appended as a numbered list in the todo's description field:
```
Original description
\n\nSubtasks:\n1. Sub1\n2. Sub2
```

### Feature 2: Turn into Idea
**Available for:** NOTE type only

**Behavior (MOVE):**
1. Creates an `IdeaEntity` with `title = task.title`, `description = task.description`
2. Each subtask → `IdeaStageEntity` (sequential `orderIndex`, `isCompleted = false`)
3. Deletes the original note + subtasks via `taskRepository.deleteTaskAndSubtasks(task)`

### Implementation

**File:** `app/src/main/java/com/example/ui/viewmodel/MainViewModel.kt`

```kotlin
fun moveTaskToTodo(task: TaskEntity, subtasks: List<TaskEntity>) {
    viewModelScope.launch {
        val mergedDescription = buildString {
            append(task.description)
            if (subtasks.isNotEmpty()) {
                append("\n\nSubtasks:\n")
                subtasks.forEachIndexed { i, s -> append("${i + 1}. ${s.title}\n") }
            }
        }
        todoRepository.insertTodo(TodoEntity(
            title = task.title,
            description = mergedDescription.trim(),
            priority = task.priorityLevel,
            status = "PENDING"
        ))
        taskRepository.deleteTaskAndSubtasks(task)
    }
}

fun turnNoteIntoIdea(task: TaskEntity, subtasks: List<TaskEntity>) {
    viewModelScope.launch {
        val ideaId = ideaRepository.insertIdea(IdeaEntity(title = task.title, description = task.description))
        subtasks.forEachIndexed { index, subtask ->
            ideaRepository.insertStage(IdeaStageEntity(ideaId = ideaId, title = subtask.title, isCompleted = false, orderIndex = index))
        }
        taskRepository.deleteTaskAndSubtasks(task)
    }
}
```

**File:** `app/src/main/java/com/example/ui/screens/PlannerScreen.kt`

**BulletTaskItem changes:**
- Added `onMoveToTodo: () -> Unit = {}` and `onTurnIntoIdea: () -> Unit = {}` callback params
- Added "Move to To-Do" `DropdownMenuItem` (icon: `Checklist`) for all types
- Added "Turn into Idea" `DropdownMenuItem` (icon: `Lightbulb`) only when `task.type == "NOTE"`

**Call sites wired (2 locations):**
| Call Site | Lines | Context |
|-----------|-------|---------|
| Active tasks | ~1023 | `viewModel.moveTaskToTodo(task, taskSubtasks)` / `viewModel.turnNoteIntoIdea(task, taskSubtasks)` |
| Completed tasks | ~1105 | Same wiring (allows converting completed items too) |

### Build Verification
`.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL

---

## TodoScreen: Fix PriorityBadge Centering + Replace Quick-Add with FAB + Dialog

### Issue 1: PriorityBadge text not centered
The `Surface` has a fixed `Modifier.height(18.dp)` but the `Text` inside (9sp + 4dp padding ≈ 13dp) sits at the top of the 18dp box, creating uneven vertical spacing.

**Fix:** Remove `Modifier.height(18.dp)` from `Surface` so it wraps the content naturally — text is vertically centered by default.

```kotlin
// BEFORE
Surface(
    shape = RoundedCornerShape(4.dp),
    color = color.copy(alpha = 0.15f),
    modifier = Modifier.height(18.dp)  // ← fixed height forces text off-center
) { Text(...) }

// AFTER
Surface(
    shape = RoundedCornerShape(4.dp),
    color = color.copy(alpha = 0.15f)
    // no fixed height — wraps content, text centers naturally
) { Text(...) }
```

### Issue 2: Quick-add is too basic (title only, no description/priority)
The existing `OutlinedTextField` + "Add" button captures only a title. Users cannot set description or priority during creation — they must edit afterward.

**Fix:** Replace the quick-add row with a `FloatingActionButton` (matching PlannerScreen's pattern) that opens an `AddTodoDialog` with three fields:
- **Title** (required)
- **Description** (optional, multi-line)
- **Priority** — three `FilterChip`s: Low, Medium, High (default: Medium)

**Files modified:**
- `app/src/main/java/com/example/ui/screens/TodoScreen.kt`

### Layout Before/After

```
[BEFORE]                              [AFTER]
┌──────────────────────────┐          ┌──────────────────────────┐
│ TO-DO                    │          │ TO-DO                    │
│ To-Do List               │          │ To-Do List               │
│                          │          │                          │
│ ┌─────────────────┐ [Add]│          │ [ALL] [PENDING] [DONE]   │
│ │ What needs...    │      │          │                          │
│ └─────────────────┘      │          │  ┌──────────────────┐    │
│ [ALL] [PENDING] [DONE]   │          │  │ ☐ Buy groceries  │    │
│                          │          │  │    Medium        │    │
│  ┌──────────────────┐    │          │  └──────────────────┘    │
│  │ ☐ Buy groceries  │    │          │                          │
│  │    Medium        │    │          │                    [ + ] │
│  └──────────────────┘    │          └──────────────────────────┘
│                    [ + ] │
└──────────────────────────┘
```

### AddTodoDialog Contents
```
┌──────────────────────────────────┐
│  New To-Do                       │
│  ┌──────────────────────────┐    │
│  │ Title              [____] │    │
│  └──────────────────────────┘    │
│  ┌──────────────────────────┐    │
│  │ Description (optional)   │    │
│  │ [______________________] │    │
│  └──────────────────────────┘    │
│  Priority                        │
│  [Low] [Medium] [High]           │
│                                  │
│         [Cancel]    [Add]        │
└──────────────────────────────────┘
```

### Empty State Text Updated
"Add one above" → "Tap + to create your first to-do" (consistent with IdeasScreen's hint text)

### Build Verification
`.\gradlew.bat assembleDebug` — expects BUILD SUCCESSFUL

---

## IdeasScreen: Replace Add Button with FAB + Inline Group Creation

### Motivation
Align IdeasScreen's add-idea flow with the TodoScreen pattern (FAB + dialog) and allow creating new groups inline
without closing the idea dialog.

### Changes

**File:** `app/src/main/java/com/example/ui/screens/IdeasScreen.kt`

#### 1. Remove full-width "Add Idea" Button, add FAB

**Before:**
```kotlin
Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
    if (filteredIdeas.isEmpty()) { ... }
    else { LazyColumn(...) { ... } }
}
Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
    Button(onClick = { showCreateIdeaDialog = true }, ...) {
        Icon(Icons.Default.Add, ...)
        Text("Add Idea")
    }
}
```

**After:**
```kotlin
Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
    if (filteredIdeas.isEmpty()) { ... }
    else { LazyColumn(...) { ... } }
    FloatingActionButton(
        onClick = { showCreateIdeaDialog = true },
        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add Idea")
    }
}
```

- FAB placed inside the content `Box(weight(1f).fillMaxWidth())`, aligned `BottomEnd`
- LazyColumn gets extra bottom padding: `PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 80.dp)`
  to prevent last card from being hidden behind the FAB

#### 2. Inline "Create New Group" inside CreateIdeaDialog

**Current flow:** Close idea dialog → tap "+ Group" in header → create group → reopen idea dialog → select group.

**New flow:** Inside CreateIdeaDialog, tap "Create New Group..." as the first dropdown item:
1. Dropdown closes (`expanded = false`), idea dialog stays open
2. `onShowCreateGroup()` callback fires → parent sets `showCreateGroupFromIdeaDialog = true`
3. `CreateGroupDialog` renders as overlay on top of `CreateIdeaDialog` (AlertDialogs stack naturally)
4. User fills name, picks color, taps Save → `viewModel.addGroup()` runs
5. Group dialog dismisses, idea dialog still open with refreshed groups list
6. User opens dropdown again → selects the newly created group

**`CreateIdeaDialog` signature change:**
```kotlin
@Composable
private fun CreateIdeaDialog(
    groups: List<IdeaGroupEntity>,
    initialTitle: String,
    initialDescription: String,
    initialGroupId: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long?, String, String) -> Unit,
    onShowCreateGroup: () -> Unit = {}  // NEW
)
```

**Dropdown addition:**
```kotlin
DropdownMenu(...) {
    DropdownMenuItem(   // ← NEW: appears first
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, ..., modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Create New Group...")
            }
        },
        onClick = { expanded = false; onShowCreateGroup() }
    )
    DropdownMenuItem(
        text = { Text("None") },
        onClick = { selectedGroupId = null; expanded = false }
    )
    groups.forEach { group ->
        DropdownMenuItem(
            text = { Text(group.name) },
            onClick = { selectedGroupId = group.id; expanded = false }
        )
    }
}
```

**Parent state + dialog rendering:**
```kotlin
var showCreateGroupFromIdeaDialog by remember { mutableStateOf(false) }

// In CreateIdeaDialog call:
CreateIdeaDialog(
    ...
    onShowCreateGroup = { showCreateGroupFromIdeaDialog = true }
)

// New overlay dialog:
if (showCreateGroupFromIdeaDialog) {
    CreateGroupDialog(
        initialName = null,
        initialColor = presetColors[0],
        onDismiss = { showCreateGroupFromIdeaDialog = false },
        onConfirm = { name, color -> viewModel.addGroup(name, color); showCreateGroupFromIdeaDialog = false }
    )
}
```

### Visual Flow

```
┌──────────────────────────┐
│ IDEAS                    │
│ Ideas               [+Group]│
│ [All] [Work] [Personal]  │
│                          │
│  ┌────────────────────┐  │
│  │ 💡 Idea card 1     │  │
│  │    + Add Stage     │  │
│  └────────────────────┘  │
│                    [ + ] │  ← FAB
└──────────────────────────┘

Tapping FAB → New Idea dialog:
┌──────────────────────────┐
│ ╔══════════════════════╗ │
│ ║    New Idea          ║ │
│ ║  ┌──────────────┐   ║ │
│ ║  │ Title        │   ║ │
│ ║  └──────────────┘   ║ │
│ ║  ┌──────────────┐   ║ │
│ ║  │ Description  │   ║ │
│ ║  └──────────────┘   ║ │
│ ║  ┌──────────────┐   ║ │
│ ║  │ Group ▼      │   ║ │
│ ║  │ ├ Create New… │   ║ │  ← NEW item
│ ║  │ ├ None       │   ║ │
│ ║  │ ├ Work       │   ║ │
│ ║  │ └ Personal   │   ║ │
│ ║  └──────────────┘   ║ │
│ ║  [Cancel] [Save]    ║ │
│ ╚══════════════════════╝ │
└──────────────────────────┘

Tapping "Create New Group..." → overlay:
┌──────────────────────────┐
│ ╔══════════════════════╗ │
│ ║  ┌─────────────┐    ║ │
│ ║  │   New Group      ║ │
│ ║  │  ┌───────────┐  ║ │
│ ║  │  │ Group name│  ║ │
│ ║  │  └───────────┘  ║ │
│ ║  │  ● ● ● ● ●     ║ │
│ ║  │  [Cancel] [Save] ║ │
│ ║  └─────────────┘    ║ │ ← New Group dialog on top
│ ╚══════════════════════╝ │
│  (underneath: New Idea    │
│   dialog still open)      │
└──────────────────────────┘
```

### Edge Cases

| Edge Case | Handling |
|-----------|----------|
| Both dialogs open at once | Compose stacks AlertDialogs; group dialog on top, idea dialog underneath — works naturally |
| Group created but not auto-selected | User re-opens dropdown and selects new group; groups list auto-refreshes via `viewModel.ideaGroups` Flow |
| "Create New Group" during edit | Works identically — dialog says "Edit Idea", group creation flow is same |
| Empty groups list | Dropdown shows "Create New Group..." + "None" |
| FAB in empty state | Empty state hint already says "Tap + to create your first idea" — matches FAB action |
| LazyColumn FAB overlap | bottom = 80.dp padding prevents last card from being obscured |
| Cancel group creation | Group dialog dismisses — idea dialog remains open |
| `readOnly` TextField click not firing | `readOnly = true` OutlinedTextField still consumes pointer events internally, so `.clickable { expanded = true }` on it never fires. **Fix:** replaced OutlinedTextField with custom `Surface(onClick = ...)` trigger that visually matches OutlinedTextField (border, label, value text, dropdown arrow) but correctly opens the `DropdownMenu` on tap |
| Data sync between "+ Group" header button and dropdown "Create New Group" | Both call `viewModel.addGroup()` → `ideaGroups` StateFlow emits updated list → dropdown and header button reflect the same data immediately |

### Build Verification
`.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL
