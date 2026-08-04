package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.TaskEntity
import com.example.ui.viewmodel.MainViewModel
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlin.math.roundToInt

@Composable
fun PomodoroScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val activeTask by viewModel.activePomodoroTask.collectAsState()
    val secondsLeft by viewModel.pomodoroSecondsLeft.collectAsState()
    val isRunning by viewModel.pomodoroRunning.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()

    // Pomodoro Phase/Goodtime Session States
    var currentPhase by remember { mutableStateOf("FOCUS") } // FOCUS, SHORT_BREAK, LONG_BREAK
    var completedSessionsCount by remember { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val totalSessionsTarget = 4

    // Local custom timer for break session when no active task task is set or during break
    var localTimerSecondsLeft by remember { mutableIntStateOf(5 * 60) }
    var localTimerRunning by remember { mutableStateOf(false) }
    var localTimerPhase by remember { mutableStateOf("SHORT_BREAK") } // SHORT_BREAK, LONG_BREAK

    // Coroutine effect for running local break timer
    LaunchedEffect(localTimerRunning, localTimerSecondsLeft) {
        if (localTimerRunning && localTimerSecondsLeft > 0) {
            kotlinx.coroutines.delay(1000)
            localTimerSecondsLeft -= 1
            if (localTimerSecondsLeft == 0) {
                // Break ended! Transition back to focus
                localTimerRunning = false
                currentPhase = "FOCUS"
                // Alert user
            }
        }
    }

    // Determine what timer is active
    val isTimerActive = activeTask != null
    val displaySeconds = if (isTimerActive) secondsLeft else localTimerSecondsLeft
    val displayRunning = if (isTimerActive) isRunning else localTimerRunning
    val displayPhase = if (isTimerActive) "FOCUS" else localTimerPhase

    val minutes = displaySeconds / 60
    val seconds = displaySeconds % 60
    val timeStr = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "GOODTIME SESSION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Focus Timer",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = "Session ${completedSessionsCount % totalSessionsTarget + 1}/$totalSessionsTarget",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
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

        Spacer(modifier = Modifier.weight(0.1f))

        // Huge Countdown Timer (Goodtime minimalist aesthetics)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Phase Badge
            val badgeColor = when (displayPhase) {
                "FOCUS" -> MaterialTheme.colorScheme.primary
                "SHORT_BREAK" -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.tertiary
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = badgeColor.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, badgeColor)
            ) {
                Text(
                    text = displayPhase.replace("_", " "),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = timeStr,
                fontSize = 88.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-2).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (activeTask != null) {
                val currentTask = activeTask!!
                val parentTask = currentTask.parentTaskId?.let { pid -> tasks.find { it.id == pid } }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    if (parentTask != null) {
                        val themeColor = if (parentTask.labelColor != null) Color(parentTask.labelColor) else MaterialTheme.colorScheme.primary

                        Text(
                            text = currentTask.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(themeColor.copy(alpha = 0.1f))
                                .border(1.dp, themeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SubdirectoryArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = themeColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SUBTASK OF: ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColor,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = parentTask.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        if (parentTask.label.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = themeColor.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, themeColor.copy(alpha = 0.4f)),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = parentTask.label.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    } else {
                        val themeColor = if (currentTask.labelColor != null) Color(currentTask.labelColor) else MaterialTheme.colorScheme.primary
                        Text(
                            text = currentTask.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (currentTask.label.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = themeColor.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, themeColor.copy(alpha = 0.4f)),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = currentTask.label.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No Task Selected (General Timer)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.15f))

        // Timer Control Panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset / Stop button
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable {
                        if (isTimerActive) {
                            viewModel.stopPomodoroEarly(context)
                        } else {
                            localTimerRunning = false
                            localTimerSecondsLeft = 5 * 60
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Timer",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Main Play/Pause Button
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        if (isTimerActive) {
                            if (isRunning) {
                                viewModel.pausePomodoro()
                            } else {
                                viewModel.resumePomodoro(context)
                            }
                        } else {
                            localTimerRunning = !localTimerRunning
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (displayRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (displayRunning) "Pause/Play" else "Start",
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Skip / Complete session button
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable {
                        if (isTimerActive) {
                            // Completing Pomodoro early
                            completedSessionsCount++
                            // Toggle Phase to break
                            if (completedSessionsCount % totalSessionsTarget == 0) {
                                localTimerPhase = "LONG_BREAK"
                                localTimerSecondsLeft = 15 * 60
                            } else {
                                localTimerPhase = "SHORT_BREAK"
                                localTimerSecondsLeft = 5 * 60
                            }
                            viewModel.stopPomodoroEarly(context)
                            currentPhase = "BREAK"
                        } else {
                            // If in break, skip back to focus
                            localTimerRunning = false
                            localTimerSecondsLeft = 25 * 60
                            currentPhase = "FOCUS"
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Skip Phase",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.15f))

        // Task Picker Section (Goodtime layout)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header of panel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT TASK TO FOCUS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Tasks",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                val activeTasks = tasks.filter { task ->
                    task.status != "COMPLETED" && task.type == "TASK" && (
                        task.parentTaskId != null || tasks.none { it.parentTaskId == task.id }
                    )
                }

                if (activeTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No pending tasks. Create a task in the Planner tab to focus!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(activeTasks, key = { it.id }) { task ->
                            val isCurrentFocus = activeTask?.id == task.id
                            val subtaskParent = if (task.parentTaskId != null) {
                                tasks.find { it.id == task.parentTaskId }
                            } else null

                            val themeColor = if (subtaskParent != null) {
                                if (subtaskParent.labelColor != null) Color(subtaskParent.labelColor) else MaterialTheme.colorScheme.primary
                            } else {
                                if (task.labelColor != null) Color(task.labelColor) else MaterialTheme.colorScheme.primary
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isCurrentFocus) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        else Color.Transparent
                                    )
                                    .border(
                                        1.dp,
                                        if (isCurrentFocus) themeColor
                                        else themeColor.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        viewModel.setTaskForPomodoroSetup(task)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (subtaskParent != null) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(24.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(themeColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                Icon(
                                    imageVector = if (isCurrentFocus) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Select focus",
                                    tint = themeColor,
                                    modifier = Modifier.size(18.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {

                                    if (subtaskParent != null) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Part of: ${subtaskParent.title}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = themeColor
                                            )
                                            if (subtaskParent.label.isNotEmpty()) {
                                                Surface(
                                                    shape = RoundedCornerShape(3.dp),
                                                    color = themeColor.copy(alpha = 0.15f),
                                                    border = BorderStroke(0.5.dp, themeColor.copy(alpha = 0.3f)),
                                                    modifier = Modifier.padding(vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = subtaskParent.label.uppercase(),
                                                        fontSize = 7.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = themeColor,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.5.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(1.dp))
                                    }

                                    Text(
                                        text = task.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val finalLabel = if (subtaskParent != null) subtaskParent.label else task.label
                                    if (finalLabel.isNotEmpty() && subtaskParent == null) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "#${finalLabel.uppercase()}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColor
                                        )
                                    }
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = "${task.durationMinutes}m",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val taskForPomodoroSetup by viewModel.taskForPomodoroSetup.collectAsState()

    taskForPomodoroSetup?.let { task ->
        val defaultFocus by viewModel.defaultFocusMinutes.collectAsState()
        val defaultBreak by viewModel.defaultBreakMinutes.collectAsState()

        var focusMinutes by remember(task, defaultFocus) { 
            mutableIntStateOf(if (task.durationMinutes == 25 && defaultFocus != 25) defaultFocus else task.durationMinutes) 
        }
        var targetSessions by remember(task) { mutableStateOf<Int?>(task.targetSessions) }
        var breakMinutes by remember(task, defaultBreak) { 
            mutableStateOf<Int?>(if ((task.breakMinutes == 5 || task.breakMinutes == null) && defaultBreak != 5) defaultBreak else (task.breakMinutes ?: 5)) 
        }
        var saveAsDefault by remember(task) { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { viewModel.setTaskForPomodoroSetup(null) },
            title = {
                Text(
                    text = "Fast Set Up Pomodoro",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Customize Pomodoro sessions for:\n\"${task.title}\"",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider()

                    // Focus Time Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Focus Duration",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$focusMinutes min",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = focusMinutes.toFloat(),
                            onValueChange = { focusMinutes = it.roundToInt() },
                            valueRange = 5f..120f,
                            steps = 22 // increments of 5 min: 5, 10, 15... 120
                        )
                    }

                    // Target Sessions Choice
                    Column {
                        Text(
                            text = "Target Sessions",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Button for Continuous
                            val isContinuous = targetSessions == null
                            FilledTonalButton(
                                onClick = { targetSessions = null },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isContinuous) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isContinuous) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1.3f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Continuous", fontSize = 11.sp)
                            }

                            // Plus Minus Controls
                            Row(
                                modifier = Modifier
                                    .weight(1.7f)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = {
                                        val current = targetSessions ?: 2
                                        if (current > 1) {
                                            targetSessions = current - 1
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }

                                Text(
                                    text = targetSessions?.let { "$it Session(s)" } ?: "No Limit",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                IconButton(
                                    onClick = {
                                        val current = targetSessions ?: 0
                                        targetSessions = current + 1
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    // Break Duration Choices
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Break Duration",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = breakMinutes?.let { "$it min" } ?: "No Breaks",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (breakMinutes != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = (breakMinutes ?: 0).toFloat(),
                            onValueChange = { 
                                val rounded = it.roundToInt()
                                breakMinutes = if (rounded == 0) null else rounded
                            },
                            valueRange = 0f..30f,
                            steps = 5 // increments of 5 min: 0, 5, 10, 15, 20, 25, 30
                        )
                    }

                    HorizontalDivider()

                    // Save as Default
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { saveAsDefault = !saveAsDefault },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = saveAsDefault,
                            onCheckedChange = { saveAsDefault = it }
                        )
                        Text(
                            text = "Save as default for next time fast set up",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.startPomodoro(
                            context = context,
                            task = task,
                            focusMinutes = focusMinutes,
                            targetSessions = targetSessions,
                            breakMinutes = breakMinutes,
                            saveAsDefault = saveAsDefault
                        )
                        viewModel.setTaskForPomodoroSetup(null)
                    }
                ) {
                    Text("Start Focus")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setTaskForPomodoroSetup(null) }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
    if (showSettingsDialog) {
        SettingsDialog(viewModel = viewModel, onDismiss = { showSettingsDialog = false })
    }
}
