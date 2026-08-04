# Day Review Enhancement Plan (Update Problem)

## Overview

The current Day Review feature has several limitations:

1. **Always shown** in daily planner view regardless of user settings
2. **No notification system** to remind users to fill their day review at the time of day they prefer
3. **No persistent prompt** when app is opened after the review time has passed

This plan addresses these gaps by implementing a controlled, opt-in notification system that respects user preferences while enhancing usability.

## Goals

1. **Not show DayReviewCard** in DailyPlannerView by default
2. **Allow users to enable Day Review Reminder** in SettingsDialog with custom time
3. **Schedule daily alarm** at user-specified time to trigger a Snackbar notification
4. **Show Snackbar notification** when app is opened or unlocked after the review time
5. **Allow users to tap Review** to open DayReviewScreen as a full-screen overlay on current tab
6. **Provide 10-second auto-dismiss** for the notification if not interacted with

## Solution Overview

### File Changes Required

**1. `PlannerScreen.kt` - DailyPlannerView**
- Remove DayReviewCard from DailyPlannerView
- Keep showDayReviewDialog state + DayReviewScreen dialog (used by prompt trigger)

**2. `MainViewModel.kt` - Day Review Logic**
- Add `scheduleDayReviewAlarm(context)` / `cancelDayReviewAlarm(context)` (AlarmManager repeating daily)
- Add `_showDayReviewPrompt: MutableStateFlow<Boolean>` + `triggerDayReviewPrompt()` / `dismissDayReviewPrompt()`
- Integrate scheduling into `updateReviewReminderEnabled()` and `updateReviewReminderTime()`

**3. `ReminderReceiver.kt` - Day Review Actions**
- Add handler for `com.example.action.DAY_REVIEW` action
- Build standard notification (non-full-screen) → PendingIntent → MainActivity with `open_day_review=true`
- Extend boot/time-change reschedule to cover day review alarm

**4. `MainActivity.kt` - Snackbar & Day Review Prompt**
- Add `SnackbarHostState` + `SnackbarHost` to Scaffold
- Add `onNewIntent()` override + check for `open_day_review` extra
- Add `LaunchedEffect` to show Snackbar when `showDayReviewPrompt` is true
- Snackbar: "Time to review your day! [Review]" with `SnackbarDuration.Long`
- When user taps "Review", render DayReviewScreen as full-screen overlay (direct on current tab)
- Auto-dismiss after 10 seconds or when user swipes

**5. `MoreScreen.kt` - No Changes**
- The overlay approach avoids routing complexity (overlay DayReviewScreen over current tab)

### Technical Details

#### Notification Channel
- Channel ID: "day_review_reminder"
- Importance: NotificationManager.IMPORTANCE_DEFAULT
- Description: "Daily prompts to review your day"

#### Snackbar Configuration
```kotlin
// Show when review is due and user hasn't completed it
SnackbarHostState {
    LaunchedEffect(showDayReviewPrompt) {
        if (showDayReviewPrompt) {
            host.showSnackbar(
                message = "Time to review your day!",
                actionLabel = "Review",
                duration = SnackbarDuration.Long,
                onActionClicked = { /* Open DayReviewScreen overlay */ }
            )
        }
    }
}
```

#### Overlay DayReviewScreen
- Render using `Dialog` (AlertDialog with DayReviewScreen content)
- Full-screen or bottom-sheet based on screen size
- Allows user to stay on current tab while filling review
- Back arrow closes overlay and returns to previous tab

#### Scheduling Logic
```kotlin
// In MainViewModel.scheduleDayReviewAlarm()
fun scheduleDayReviewAlarm(context: Context) {
    val time = _reviewReminderTime.value
    val (hour, minute) = parseTimeString(time) // "HH:mm" -> hour, minute
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1) // Tomorrow if passed today
    }
    
    val intent = Intent(context, ReminderReceiver::class.java).apply {
        action = "com.example.action.DAY_REVIEW"
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context, 
        5000, // Unique request code
        intent, 
        PendingIntent.FLAG_IMMUTABLE
    )
    
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setRepeating(
        AlarmManager.RTC_WAKEUP, 
        cal.timeInMillis, 
        AlarmManager.INTERVAL_DAY,
        pendingIntent
    )
}
```

#### Unread Detection
- Check if review exists for current date (`reviewForDate(date) != null`)
- If review exists but user hasn't marked as completed (based on a completion flag)
- Or if no review exists at all
- Either condition should trigger the prompt

## UX Flow

```
1. User enables "Day Review Reminder" in Settings
2. Sets time (e.g., 21:00) and saves
3. System schedules repeating alarm at 21:00 daily

Day 1:
- 21:00 → DAY_REVIEW alarm triggers
- App opens (or notification appears)
- Snackbar: "Time to review your day! [Review]" (auto-dismiss after 10s)
- User taps "Review" → DayReviewScreen overlay opens
- User fills review and saves
- Prompt state cleared → No more Snackbars

Day 2:
- Same flow if user hasn't filled review
```

## Benefits

1. **Controlled**: Only shown when enabled by user
2. **Respectful**: Standard, non-intrusive notification
3. **Convenient**: Overlay keeps user in current context
4. **Time-aware**: Respects user-selected time slot
5. **Persistent**: Remains across app close/open
6. **Simple**: Minimal navigation complexity

## Edge Cases Handled

1. **App killed/killed during alarm** → Reschedules on boot
2. **Time changed** → Reschedules to new time
3. **User has review but opened after time** → Snackbar shows
4. **User completed review** → Prompt not shown again
5. **Manual save from other screens** → Check completion status

## Files Modified

1. `app/src/main/java/com/example/ui/screens/PlannerScreen.kt`
2. `app/src/main/java/com/example/ui/viewmodel/MainViewModel.kt`
3. `app/src/main/java/com/example/core/receiver/ReminderReceiver.kt`
4. `app/src/main/java/com/example/MainActivity.kt`
5. `app/src/main/java/com/example/ui/screens/MoreScreen.kt` (none)

## Files Created/Modified for Implementation

1. New notification channel and receiver logic
2. Snackbar integration in MainActivity
3. AlarmManager scheduling logic
4. Overlay DayReviewScreen component
5. State management for read/unread detection