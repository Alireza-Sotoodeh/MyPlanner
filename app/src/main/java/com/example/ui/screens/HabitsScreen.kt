package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.HabitEntity
import com.example.core.database.entity.SleepLogEntity
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HabitsScreen(viewModel: MainViewModel) {
    val habits by viewModel.habits.collectAsState()
    val logs by viewModel.habitLogs.collectAsState()
    val sleepLog by viewModel.sleepLog.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var showCreateHabitDialog by remember { mutableStateOf(false) }
    var showLogSleepDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
    ) {
        // Tracker Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MINDFULNESS & TRACKERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Habit & Sleep Log",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    IconButton(onClick = { viewModel.selectTab(0); viewModel.selectDate(viewModel.todayDate.value) }) {
                        Icon(Icons.Default.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }

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
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "LOG SLEEP",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (sleepLog == null) {
                        Text(
                            text = "No sleep logged for today.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Tap to input sleep hours and quality rating.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    } else {
                        val currentSleep = sleepLog!!
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
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

                            Spacer(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outline))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(5) { index ->
                                        Icon(
                                            imageVector = if (index < currentSleep.sleepQuality) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD54F), // Elegant gold
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Quality rating",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (currentSleep.notes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Notes: ${currentSleep.notes}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 2. Habits Section header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DAILY HABIT INTENTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp
                )

                IconButton(
                    onClick = { showCreateHabitDialog = true },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Habit",
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
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
                    Text(
                        text = "No habits created yet. Tap + to add one!",
                        fontSize = 13.sp,
                        color = Color(0xFF79747E)
                    )
                }
            }
        } else {
            items(habits) { habit ->
                val currentLog = logs.find { it.habitId == habit.id }
                HabitRowItem(
                    habit = habit,
                    logValue = currentLog?.value ?: 0f,
                    onLog = { value -> viewModel.logHabit(habit.id, value) },
                    onDelete = { viewModel.deleteHabit(habit) }
                )
            }
        }

        // Empty bottom padding
        item {
            Spacer(modifier = Modifier.height(80.dp))
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

    if (showCreateHabitDialog) {
        CreateHabitDialog(
            onDismiss = { showCreateHabitDialog = false },
            onConfirm = { name, type, target, unit ->
                viewModel.addHabit(name, type, target, unit)
                showCreateHabitDialog = false
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
                .padding(16.dp),
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

            Spacer(modifier = Modifier.width(16.dp))

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
                // Quantitative +/- button stepper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { if (logValue > 0f) onLog(logValue - 1f) },
                        modifier = Modifier
                            .size(32.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.primary)
                    }

                    Text(
                        text = "${logValue.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = { onLog(logValue + 1f) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Habit", tint = Color(0xFF79747E))
            }
        }
    }
}

@Composable
fun LogSleepDialog(
    sleepLog: SleepLogEntity?,
    onDismiss: () -> Unit,
    onConfirm: (Float, Int, String, String, String) -> Unit
) {
    var hours by remember { mutableFloatStateOf(sleepLog?.hoursSlept ?: 8f) }
    var quality by remember { mutableIntStateOf(sleepLog?.sleepQuality ?: 3) }
    var bedTime by remember { mutableStateOf(sleepLog?.sleepTime ?: "23:00") }
    var wakeTime by remember { mutableStateOf(sleepLog?.wakeTime ?: "07:30") }
    var notes by remember { mutableStateOf(sleepLog?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Log Daily Sleep", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sleep hours slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Hours Slept", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = String.format("%.1f hrs", hours), fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF6750A4))
                    }
                    Slider(
                        value = hours,
                        onValueChange = { hours = it },
                        valueRange = 0f..16f,
                        steps = 31,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF6750A4),
                            activeTrackColor = Color(0xFF6750A4)
                        )
                    )
                }

                // Quality star rating
                Column {
                    Text(text = "Sleep Quality", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) { index ->
                            val starIndex = index + 1
                            IconButton(onClick = { quality = starIndex }) {
                                Icon(
                                    imageVector = if (starIndex <= quality) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = null,
                                    tint = Color(0xFFEADB3F),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = bedTime,
                        onValueChange = { bedTime = it },
                        label = { Text("Bed Time") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6750A4),
                            focusedLabelColor = Color(0xFF6750A4)
                        )
                    )

                    OutlinedTextField(
                        value = wakeTime,
                        onValueChange = { wakeTime = it },
                        label = { Text("Wake Time") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6750A4),
                            focusedLabelColor = Color(0xFF6750A4)
                        )
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Daily logs / dreams / notes") },
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
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun CreateHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Float, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("BINARY") } // BINARY, QUANTITATIVE
    var target by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("times") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Create Custom Habit", fontWeight = FontWeight.Bold) },
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
                            onValueChange = { target = it },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        val targetFloat = target.toFloatOrNull() ?: 1.0f
                        onConfirm(name, type, targetFloat, unit)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
            ) {
                Text("CREATE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color(0xFF6750A4))
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}
