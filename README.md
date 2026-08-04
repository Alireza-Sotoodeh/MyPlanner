# MyPlanner — BulletCoach

A minimal, private, offline-first planner and habit tracker for Android.

## Features

- **Daily/Weekly/Monthly Planner** — Task management with labels, subtasks, drag-to-reorder, and date navigation
- **Habit Tracker** — Recurring habits with check-in calendar and streak tracking
- **Pomodoro Timer** — Focus sessions with linked tasks and chain completion
- **Todo List** — Priority-based todos with two-way task linking
- **Ideas** — Grouped ideas with inline stage management
- **Diary** — Markdown journal with auto-save and undo/redo
- **Shopping List** — Quantity, price, and purchase tracking
- **Day Review** — End-of-day reflection with mood and productivity ratings
- **Mottos** — Inspirational quotes collection
- **Statistics** — Screen time, habit completion, and time-spent-by-label charts
- **Reminders** — Alarm-based notifications with boot persistence
- **Backup/Restore** — JSON export/import to Google Drive

## Tech Stack

- **UI:** Jetpack Compose with Material 3
- **Database:** Room (13 entities, version 12)
- **Architecture:** Single-module, single ViewModel
- **Build:** Gradle 9.3.1, AGP 9.1.1, Kotlin 2.2.10

## Prerequisites

- Android Studio (latest stable)
- Android SDK 36

## Build & Run

1. Clone this repository
2. Open in Android Studio
3. Remove `signingConfig = signingConfigs.getByName("debugConfig")` from `app/build.gradle.kts` for local builds
4. Run on an emulator or physical device

## Commands

```powershell
.\gradlew assembleDebug      # Build debug APK
.\gradlew testDebugUnitTest  # Run unit tests
```

## Project Structure

```
app/src/main/java/com/example/
├── MainActivity.kt                    # Entry point with scaffold and bottom nav
├── core/
│   ├── database/                      # Room DB, entities, DAOs
│   ├── manager/ReminderManager.kt     # Alarm scheduling
│   ├── receiver/ReminderReceiver.kt   # Boot/time change handling
│   └── repository/                    # Repository wrappers
├── ui/
│   ├── components/                    # Reusable composables
│   ├── screens/                       # 13 screen composables
│   └── viewmodel/MainViewModel.kt     # All state and CRUD operations
```

## License

Private — All rights reserved.
