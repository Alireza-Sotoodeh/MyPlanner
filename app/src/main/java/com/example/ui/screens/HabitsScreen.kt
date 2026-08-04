package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.HabitEntity
import com.example.core.database.entity.HabitLogEntity
import com.example.core.database.entity.SleepLogEntity
import com.example.ui.components.CalendarDatePickerDialog
import com.example.ui.components.HeaderActions
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HabitsScreen(viewModel: MainViewModel) {
    val habits by viewModel.habits.collectAsState()
    val logs by viewModel.habitLogs.collectAsState()
    val sleepLog by viewModel.sleepLog.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val allHabitLogs by viewModel.allHabitLogs.collectAsState()

    var tabIndex by remember { mutableIntStateOf(0) }
    var showCreateHabitDialog by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<HabitEntity?>(null) }
    var showLogSleepDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TRACKERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Habit & Sleep",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            HeaderActions(
                onHomeClick = { viewModel.selectTab(0); viewModel.selectDate(viewModel.todayDate.value) },
                onSettingsClick = { showSettingsDialog = true },
                onManageHabits = { tabIndex = 1 }
            )
        }

        // Tabs
        TabRow(
            selectedTabIndex = tabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            listOf("Today", "All Habits", "History").forEachIndexed { index, title ->
                Tab(
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (tabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        // Tab content
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            when (tabIndex) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        // 1. Sleep Tracker Summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .clickable { showLogSleepDialog = true }
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SLEEP TRACKER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.5.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "LOG SLEEP",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (sleepLog == null) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "😴",
                                    fontSize = 28.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No sleep logged for today.",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "Tap to log your sleep hours and quality.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        } else {
                            val currentSleep = sleepLog!!
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${currentSleep.sleepTime} → ${currentSleep.wakeTime}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${currentSleep.hoursSlept} hrs",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Light,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Duration",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(1.dp).height(50.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Quality",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        repeat(5) { index ->
                                            Icon(
                                                imageVector = if (index < currentSleep.sleepQuality) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                                contentDescription = null,
                                                tint = Color(0xFFFFD54F),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (currentSleep.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentSleep.notes,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)).padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Habits Section header (without add button — moved to FAB)
            item {
                Text(
                    text = "DAILY HABITS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp
                )
            }

            // Habits logs lists
            if (habits.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "📋",
                                fontSize = 28.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No habits yet",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF79747E)
                            )
                            Text(
                                text = "Tap + to create your first habit",
                                fontSize = 11.sp,
                                color = Color(0xFF79747E).copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                items(habits) { habit ->
                    val currentLog = logs.find { it.habitId == habit.id }
                    HabitRowItem(
                        habit = habit,
                        logValue = currentLog?.value ?: 0f,
                        onLog = { value -> viewModel.logHabit(habit.id, value) },
                        onEdit = { editingHabit = habit },
                        onDelete = { viewModel.deleteHabit(habit) }
                    )
                }
            }
        }

                // FAB for adding habits
                FloatingActionButton(
                    onClick = { showCreateHabitDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 24.dp, bottom = 16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Habit"
                    )
                }
            }
            1 -> {
                AllHabitsTab(
                    habits = habits,
                    onEdit = { editingHabit = it },
                    onDelete = { viewModel.deleteHabit(it) },
                    onAdd = { showCreateHabitDialog = true }
                )
            }
            2 -> {
                HabitsHistoryTab(
                    habits = habits,
                    allLogs = allHabitLogs,
                    viewModel = viewModel
                )
            }
        }
    }
    }

    if (showLogSleepDialog) {
        LogSleepDialog(
            sleepLog = sleepLog,
            onDismiss = { showLogSleepDialog = false },
            onConfirm = { hours, quality, bedTime, wakeTime, notes ->
                viewModel.saveSleepLog(hours, quality, bedTime, wakeTime, notes)
                showLogSleepDialog = false
            }
        )
    }

    if (showCreateHabitDialog || editingHabit != null) {
        HabitDialog(
            habitToEdit = editingHabit,
            onDismiss = { showCreateHabitDialog = false; editingHabit = null },
            onConfirm = { name, type, target, unit, recurrenceMode, recurrenceInterval, recurrenceDaysOfWeek, recurrenceEndDate, habitTime, reminderEnabled ->
                if (editingHabit != null) {
                    viewModel.updateHabit(
                        editingHabit!!.copy(
                            name = name, type = type, target = target, unit = unit,
                            recurrenceMode = recurrenceMode, recurrenceInterval = recurrenceInterval,
                            recurrenceDaysOfWeek = recurrenceDaysOfWeek, recurrenceEndDate = recurrenceEndDate,
                            habitTime = habitTime, reminderEnabled = reminderEnabled
                        )
                    )
                } else {
                    viewModel.addHabit(
                        name = name, type = type, target = target, unit = unit,
                        recurrenceMode = recurrenceMode, recurrenceInterval = recurrenceInterval,
                        recurrenceDaysOfWeek = recurrenceDaysOfWeek, recurrenceEndDate = recurrenceEndDate,
                        habitTime = habitTime, reminderEnabled = reminderEnabled
                    )
                }
                showCreateHabitDialog = false; editingHabit = null
            }
        )
    }
    if (showSettingsDialog) {
        SettingsDialog(viewModel = viewModel, onDismiss = { showSettingsDialog = false })
    }
}

@Composable
fun HabitRowItem(
    habit: HabitEntity,
    logValue: Float,
    onLog: (Float) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Goal: ${habit.target.toInt()} ${habit.unit}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Habit logger interactions based on type (BINARY or QUANTITATIVE)
            if (habit.type == "BINARY") {
                val isDone = logValue >= 1f
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .background(if (isDone) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onLog(if (isDone) 0f else 1f) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                val goalReached = logValue >= habit.target
                if (goalReached) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, shape = CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { onLog(0f) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = { if (logValue > 0f) onLog(logValue - 1f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "${logValue.toInt()}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "/ ${habit.target.toInt()}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        IconButton(
                            onClick = { onLog(logValue + 1f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF79747E), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF79747E), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSleepDialog(
    sleepLog: SleepLogEntity?,
    onDismiss: () -> Unit,
    onConfirm: (Float, Int, String, String, String) -> Unit
) {
    var quality by remember { mutableIntStateOf(sleepLog?.sleepQuality ?: 3) }
    var bedTime by remember { mutableStateOf(sleepLog?.sleepTime ?: "23:00") }
    var wakeTime by remember { mutableStateOf(sleepLog?.wakeTime ?: "07:30") }
    var notes by remember { mutableStateOf(sleepLog?.notes ?: "") }

    fun calcHours(bed: String, wake: String): Float {
        val bParts = bed.split(":"); val wParts = wake.split(":")
        if (bParts.size != 2 || wParts.size != 2) return 0f
        val bMin = bParts[0].toIntOrNull()?.let { it * 60 + (bParts[1].toIntOrNull() ?: 0) } ?: return 0f
        val wMin = wParts[0].toIntOrNull()?.let { it * 60 + (wParts[1].toIntOrNull() ?: 0) } ?: return 0f
        val diff = if (wMin >= bMin) wMin - bMin else (1440 - bMin) + wMin
        return (diff / 60f * 10).let { kotlin.math.round(it) / 10f }
    }

    val hours = calcHours(bedTime, wakeTime)

    var showTimePicker by remember { mutableStateOf(false) }
    var timePickerTarget by remember { mutableStateOf("BED") } // "BED" or "WAKE"
    val timePickerState = rememberTimePickerState(
        initialHour = 12,
        initialMinute = 0,
        is24Hour = true
    )

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp),
            title = { Text(if (timePickerTarget == "BED") "Select Bed Time" else "Select Wake Time", fontWeight = FontWeight.Bold) },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val t = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                    if (timePickerTarget == "BED") bedTime = t else wakeTime = t
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Log Daily Sleep", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Bed & Wake time with clock icons
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { timePickerTarget = "BED"; timePickerState.hour = bedTime.substringBefore(":").toIntOrNull() ?: 23; timePickerState.minute = bedTime.substringAfter(":").toIntOrNull() ?: 0; showTimePicker = true }
                                    .padding(12.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Icon(imageVector = Icons.Default.Schedule, contentDescription = "Bed Time", tint = Color(0xFF6750A4), modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Bed Time", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = bedTime, fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { timePickerTarget = "WAKE"; timePickerState.hour = wakeTime.substringBefore(":").toIntOrNull() ?: 7; timePickerState.minute = wakeTime.substringAfter(":").toIntOrNull() ?: 30; showTimePicker = true }
                                    .padding(12.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Icon(imageVector = Icons.Default.Home, contentDescription = "Wake Time", tint = Color(0xFFFF9800), modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Wake Time", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = wakeTime, fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF6750A4).copy(alpha = 0.1f))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "💤", fontSize = 20.sp)
                                Text(
                                    text = String.format("%.1f", hours),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF6750A4)
                                )
                                Text(text = "hrs", fontSize = 14.sp, color = Color(0xFF6750A4), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Quality star rating
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Sleep Quality", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            repeat(5) { index ->
                                val starIndex = index + 1
                                IconButton(onClick = { quality = starIndex }, modifier = Modifier.size(40.dp)) {
                                    Icon(
                                        imageVector = if (starIndex <= quality) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Dreams / notes") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6750A4),
                        focusedLabelColor = Color(0xFF6750A4)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(hours, quality, bedTime, wakeTime, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
            ) {
                Text("SAVE LOG")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color(0xFF6750A4))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDialog(
    habitToEdit: HabitEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Float, String, String, Int, String, String?, String?, Boolean) -> Unit
) {
    val isEditing = habitToEdit != null
    var name by remember { mutableStateOf(habitToEdit?.name ?: "") }
    var type by remember { mutableStateOf(habitToEdit?.type ?: "BINARY") }
    var target by remember { mutableStateOf(habitToEdit?.target?.toInt()?.toString() ?: "1") }
    var unit by remember { mutableStateOf(habitToEdit?.unit ?: "times") }
    var recurrenceMode by remember { mutableStateOf(habitToEdit?.recurrenceMode ?: "ALWAYS") }
    var recurrenceInterval by remember { mutableStateOf(habitToEdit?.recurrenceInterval?.toString() ?: "1") }
    var recurrenceDaysOfWeek by remember { mutableStateOf(habitToEdit?.recurrenceDaysOfWeek ?: "") }
    var recurrenceEndDate by remember { mutableStateOf(habitToEdit?.recurrenceEndDate) }
    var habitTime by remember { mutableStateOf(habitToEdit?.habitTime ?: "") }
    var reminderEnabled by remember { mutableStateOf(habitToEdit?.reminderEnabled ?: false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = habitTime.substringBefore(":").toIntOrNull() ?: 12,
        initialMinute = habitTime.substringAfter(":", "").toIntOrNull() ?: 0,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (isEditing) "Edit Habit" else "Create Custom Habit", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6750A4),
                        focusedLabelColor = Color(0xFF6750A4)
                    )
                )

                Column {
                    Text(text = "Habit Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("BINARY", "QUANTITATIVE").forEach { item ->
                            val selected = type == item
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) Color(0xFF6750A4) else Color(0xFFF3EDF7))
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) Color(0xFF6750A4) else Color(0xFFCAC4D0),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { type = item }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (item == "BINARY") "Binary (Yes/No)" else "Quantitative (+/-)",
                                    color = if (selected) Color.White else Color(0xFF1C1B1F),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (type == "QUANTITATIVE") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = target,
                            onValueChange = { target = it.filter { c -> c.isDigit() } },
                            label = { Text("Daily Goal") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6750A4),
                                focusedLabelColor = Color(0xFF6750A4)
                            )
                        )

                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit (e.g. glasses)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6750A4),
                                focusedLabelColor = Color(0xFF6750A4)
                            )
                        )
                    }
                }

                Text(text = "Repetition:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("ALWAYS", "WEEKLY").forEach { mode ->
                        val selected = recurrenceMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) Color(0xFF6750A4) else Color(0xFFF3EDF7))
                                .border(
                                    width = 1.dp,
                                    color = if (selected) Color(0xFF6750A4) else Color(0xFFCAC4D0),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { recurrenceMode = mode }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (mode == "ALWAYS") "Every day" else "Repeat every...",
                                color = if (selected) Color.White else Color(0xFF1C1B1F),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (recurrenceMode == "WEEKLY") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Every ", fontSize = 13.sp)
                        OutlinedTextField(
                            value = recurrenceInterval,
                            onValueChange = { recurrenceInterval = it.filter { c -> c.isDigit() } },
                            modifier = Modifier.width(60.dp),
                            singleLine = true
                        )
                        Text(" week(s)", fontSize = 13.sp)
                    }
                    Text("On days:", fontSize = 12.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        val daysList = listOf(
                            Pair(java.util.Calendar.SUNDAY, "S"),
                            Pair(java.util.Calendar.MONDAY, "M"),
                            Pair(java.util.Calendar.TUESDAY, "T"),
                            Pair(java.util.Calendar.WEDNESDAY, "W"),
                            Pair(java.util.Calendar.THURSDAY, "T"),
                            Pair(java.util.Calendar.FRIDAY, "F"),
                            Pair(java.util.Calendar.SATURDAY, "S")
                        )
                        val currentDays = recurrenceDaysOfWeek.split(",").filter { it.isNotBlank() }
                        daysList.forEach { (dayVal, enLabel) ->
                            val isSelected = currentDays.contains(dayVal.toString())
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(36.dp).clickable {
                                    val mutableDays = currentDays.toMutableList()
                                    if (isSelected) mutableDays.remove(dayVal.toString()) else mutableDays.add(dayVal.toString())
                                    recurrenceDaysOfWeek = mutableDays.joinToString(",")
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = enLabel, fontSize = 12.sp, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                Text(text = "Notification:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { reminderEnabled = !reminderEnabled }) {
                    Checkbox(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                    Text("Enable Reminder", fontSize = 13.sp)
                }

                if (reminderEnabled) {
                    Box(modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true }) {
                        OutlinedTextField(
                            value = habitTime.ifBlank { "Select Time" },
                            onValueChange = { },
                            enabled = false,
                            label = { Text("Time (HH:mm)") },
                            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = "Time") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        val targetFloat = target.toFloatOrNull() ?: 1.0f
                        onConfirm(name, type, targetFloat, unit, recurrenceMode, recurrenceInterval.toIntOrNull() ?: 1, recurrenceDaysOfWeek, recurrenceEndDate, habitTime.ifBlank { null }, reminderEnabled)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
            ) {
                Text(if (isEditing) "SAVE" else "CREATE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color(0xFF6750A4))
            }
        },
        shape = RoundedCornerShape(28.dp)
    )

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val hour = timePickerState.hour
                    val min = timePickerState.minute
                    habitTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, min)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AllHabitsTab(
    habits: List<HabitEntity>,
    onEdit: (HabitEntity) -> Unit,
    onDelete: (HabitEntity) -> Unit,
    onAdd: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp)
        ) {
            if (habits.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No habits defined yet.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap + to create one.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            } else {
                items(habits, key = { it.id }) { habit ->
                    ManageHabitCard(
                        habit = habit,
                        onEdit = { onEdit(habit) },
                        onDelete = { onDelete(habit) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.background,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Habit"
            )
        }
    }
}

@Composable
private fun ManageHabitCard(
    habit: HabitEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val createdDate = remember(habit.createdAt) { dateFormat.format(Date(habit.createdAt)) }
    val recurrenceText = remember(habit) {
        when (habit.recurrenceMode) {
            "WEEKLY" -> {
                val interval = habit.recurrenceInterval
                val base = if (interval > 1) "Every $interval weeks" else "Every week"
                if (habit.recurrenceDaysOfWeek.isNotBlank()) {
                    val dayLabels = habit.recurrenceDaysOfWeek.split(",").mapNotNull { d ->
                        when (d) {
                            "1" -> "Sun"; "2" -> "Mon"; "3" -> "Tue"; "4" -> "Wed"
                            "5" -> "Thu"; "6" -> "Fri"; "7" -> "Sat"; else -> null
                        }
                    }
                    "$base (${dayLabels.joinToString(", ")})"
                } else base
            }
            else -> "Every day"
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF79747E), modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF79747E), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (habit.type == "BINARY") MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = habit.type,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (habit.type == "BINARY") MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = "${habit.target.toInt()} ${habit.unit}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "Repeats: $recurrenceText",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = if (habit.reminderEnabled) Icons.Default.Notifications else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (habit.reminderEnabled) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (habit.reminderEnabled && !habit.habitTime.isNullOrBlank()) "Reminder: ${habit.habitTime}"
                                   else if (habit.reminderEnabled) "Reminder: (no time set)"
                                   else "Reminder off",
                            fontSize = 11.sp,
                            color = if (habit.reminderEnabled) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    Text(
                        text = "Created: $createdDate",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitsHistoryTab(
    habits: List<HabitEntity>,
    allLogs: List<HabitLogEntity>,
    viewModel: MainViewModel
) {
    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    val yesterdayStr = remember {
        val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.time)
    }

    var selectedDate by remember { mutableStateOf(todayStr) }
    var dateChip by remember { mutableStateOf("today") }
    var showDatePicker by remember { mutableStateOf(false) }
    var editingLog by remember { mutableStateOf<HabitLogEntity?>(null) }

    val dateLogs = remember(allLogs, selectedDate) {
        allLogs.filter { it.date == selectedDate }
    }
    val highlightedDates = remember(allLogs) {
        allLogs.map { it.date }.toSet()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Date chips
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("today" to "Today", "yesterday" to "Yesterday").forEach { (key, label) ->
                FilterChip(
                    selected = dateChip == key,
                    onClick = {
                        dateChip = key
                        selectedDate = if (key == "today") todayStr else yesterdayStr
                    },
                    label = { Text(label, fontSize = 12.sp) }
                )
            }

            if (dateChip == "custom") {
                FilterChip(
                    selected = true,
                    onClick = {
                        dateChip = "today"
                        selectedDate = todayStr
                    },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(formatLogDate(selectedDate), fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                        }
                    }
                )
            }

            IconButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = "Pick date", modifier = Modifier.size(20.dp))
            }
        }

        // Summary
        Text(
            text = "${dateLogs.size} logs · ${habits.size} habits",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Log list
        if (habits.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No habits defined.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Create habits in the Habits tab.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(habits, key = { it.id }) { habit ->
                    val log = dateLogs.find { it.habitId == habit.id }
                    HistoryHabitRowItem(
                        habit = habit,
                        logValue = log?.value ?: 0f,
                        onLog = { value -> viewModel.logHabit(habit.id, value, date = selectedDate) },
                        onEdit = { editingLog = log?.copy() },
                        onDelete = { log?.let { viewModel.deleteHabitLog(it.id) } }
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        CalendarDatePickerDialog(
            highlightedDates = highlightedDates,
            initialUsePersian = false,
            initialSelectedDate = if (dateChip == "custom") selectedDate else null,
            onDismiss = { showDatePicker = false },
            onDateSelected = { gregorianDate ->
                selectedDate = gregorianDate
                dateChip = "custom"
                showDatePicker = false
            }
        )
    }

    if (editingLog != null) {
        val log = editingLog!!
        val habit = habits.find { it.id == log.habitId }
        if (habit != null) {
            HabitLogEditDialog(
                habit = habit,
                currentValue = log.value,
                currentNotes = log.notes,
                onDismiss = { editingLog = null },
                onSave = { value, notes ->
                    viewModel.logHabit(habit.id, value, notes, selectedDate)
                    editingLog = null
                }
            )
        } else {
            editingLog = null
        }
    }
}

@Composable
private fun HistoryHabitRowItem(
    habit: HabitEntity,
    logValue: Float,
    onLog: (Float) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Goal: ${habit.target.toInt()} ${habit.unit}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (habit.type == "BINARY") {
                val isDone = logValue >= 1f
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(2.dp, if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape)
                        .background(if (isDone) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onLog(if (isDone) 0f else 1f) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Default.Check, contentDescription = "Completed", tint = MaterialTheme.colorScheme.background, modifier = Modifier.size(20.dp))
                    }
                }
            } else {
                val goalReached = logValue >= habit.target
                if (goalReached) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { onLog(0f) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Completed", tint = MaterialTheme.colorScheme.background, modifier = Modifier.size(20.dp))
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(onClick = { if (logValue > 0f) onLog(logValue - 1f) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        Text(text = "${logValue.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        Text(text = "/ ${habit.target.toInt()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        IconButton(onClick = { onLog(logValue + 1f) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(2.dp))

            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp), enabled = logValue > 0f) {
                Icon(Icons.Default.Edit, contentDescription = "Edit log", tint = if (logValue > 0f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp), enabled = logValue > 0f) {
                Icon(Icons.Default.Delete, contentDescription = "Delete log", tint = if (logValue > 0f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun HabitLogEditDialog(
    habit: HabitEntity,
    currentValue: Float,
    currentNotes: String,
    onDismiss: () -> Unit,
    onSave: (Float, String) -> Unit
) {
    var value by remember { mutableFloatStateOf(currentValue) }
    var notes by remember { mutableStateOf(currentNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = habit.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (habit.type == "BINARY") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Completed", fontSize = 14.sp)
                        Checkbox(
                            checked = value >= 1f,
                            onCheckedChange = { value = if (it) 1f else 0f }
                        )
                    }
                } else {
                    Column {
                        Text(
                            text = "Value (goal: ${habit.target.toInt()} ${habit.unit})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = { if (value > 0f) value -= 1f },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.primary)
                            }
                            Text(
                                text = "${value.toInt()}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(48.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            IconButton(
                                onClick = { value += 1f },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(value, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

private fun formatLogDate(dateStr: String): String {
    return try {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
        val cal = Calendar.getInstance()
        val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(cal.time)
        val dateYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(parsed!!)
        val fmt = if (dateYear == currentYear) "MMM d" else "MMM d, yyyy"
        SimpleDateFormat(fmt, Locale.getDefault()).format(parsed)
    } catch (_: Exception) {
        dateStr
    }
}
