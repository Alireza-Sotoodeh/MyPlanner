package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.TaskEntity
import com.example.core.database.entity.TimerSessionEntity
import com.example.core.database.entity.TimerTemplateEntity
import com.example.core.utils.PersianCalendarHelper
import com.example.ui.components.*
import android.icu.util.ULocale
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val activeTask by viewModel.activePomodoroTask.collectAsState()
    val secondsLeft by viewModel.pomodoroSecondsLeft.collectAsState()
    val isRunning by viewModel.pomodoroRunning.collectAsState()
    val pomodoroPhase by viewModel.pomodoroPhase.collectAsState()
    val pomodoroCurrentSession by viewModel.pomodoroCurrentSession.collectAsState()
    val pomodoroTargetSessions by viewModel.pomodoroTargetSessions.collectAsState()

    val chronoElapsed by viewModel.chronoElapsed.collectAsState()
    val chronoRunning by viewModel.chronoRunning.collectAsState()
    val chronoPaused by viewModel.chronoPaused.collectAsState()

    val tasks by viewModel.allTasks.collectAsState()
    val templates by viewModel.timerTemplates.collectAsState()
    val allSessions by viewModel.allTimerSessions.collectAsState()

    var tabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Pomodoro", "Cronometer", "History")

    var selectedTaskId by remember { mutableStateOf<Long?>(null) }
    var markCompleteOnFinish by remember { mutableStateOf(false) }
    var focusMinutes by remember { mutableIntStateOf(25) }
    var shortBreakMinutes by remember { mutableStateOf<Int?>(null) }
    var longBreakMinutes by remember { mutableStateOf<Int?>(null) }
    var targetSessions by remember { mutableStateOf<Int?>(null) }
    var selectedTemplateId by remember { mutableStateOf<Long?>(null) }

    var showManageTemplates by remember { mutableStateOf(false) }
    var showChronoSummary by remember { mutableStateOf(false) }
    var chronoSummaryDuration by remember { mutableIntStateOf(0) }
    var showStopConfirm by remember { mutableStateOf(false) }

    var historyDateRange by remember { mutableStateOf("today") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var hasAutoSwitched by remember { mutableStateOf(false) }

    // Consume pre-selected task and preferred tab from Planner
    LaunchedEffect(Unit) {
        val preSelected = viewModel.consumePreSelectedTask()
        if (preSelected != null) {
            selectedTaskId = preSelected
        }
        val preferredTab = viewModel.consumePreferredTimerTab()
        if (preferredTab != null) {
            tabIndex = preferredTab
        }
    }

    // Auto-switch category when pre-selected task loads
    LaunchedEffect(selectedTaskId, tasks) {
        if (!hasAutoSwitched && selectedTaskId != null && tasks.isNotEmpty()) {
            val task = tasks.find { it.id == selectedTaskId }
            if (task != null) {
                when {
                    task.parentTaskId != null && task.type == "NOTE" -> selectedCategory = "SUB_NOTES"
                    task.parentTaskId != null -> selectedCategory = "SUBTASKS"
                    task.type == "NOTE" -> selectedCategory = "NOTES"
                }
                hasAutoSwitched = true
            }
        }
    }

    val sessions by viewModel.timerHistorySessions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header (matching other screens: label + title)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TIMER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Track the time",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            HeaderActions(
                onSettingsClick = { },
                onHomeClick = { }
            )
        }

        // Tabs
        TabRow(
            selectedTabIndex = tabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabTitles.forEachIndexed { index, title ->
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

        when (tabIndex) {
            0 -> PomodoroTab(
                viewModel = viewModel,
                activeTask = activeTask,
                secondsLeft = secondsLeft,
                isRunning = isRunning,
                pomodoroPhase = pomodoroPhase,
                pomodoroCurrentSession = pomodoroCurrentSession,
                pomodoroTargetSessions = pomodoroTargetSessions,
                tasks = tasks,
                templates = templates,
                context = context,
                selectedTaskId = selectedTaskId,
                onSelectedTaskIdChange = { selectedTaskId = it },
                markCompleteOnFinish = markCompleteOnFinish,
                onMarkCompleteOnFinishChange = { markCompleteOnFinish = it },
                focusMinutes = focusMinutes,
                onFocusMinutesChange = { focusMinutes = it },
                shortBreakMinutes = shortBreakMinutes,
                onShortBreakMinutesChange = { shortBreakMinutes = it },
                longBreakMinutes = longBreakMinutes,
                onLongBreakMinutesChange = { longBreakMinutes = it },
                targetSessions = targetSessions,
                onTargetSessionsChange = { targetSessions = it },
                selectedTemplateId = selectedTemplateId,
                onSelectedTemplateIdChange = { selectedTemplateId = it },
                showManageTemplates = showManageTemplates,
                onShowManageTemplatesChange = { showManageTemplates = it },
                showStopConfirm = showStopConfirm,
                onShowStopConfirmChange = { showStopConfirm = it },
                allTasks = tasks,
                selectedCategory = selectedCategory,
                onCategoryChange = { selectedCategory = it }
            )
            1 -> CronometerTab(
                viewModel = viewModel,
                chronoElapsed = chronoElapsed,
                chronoRunning = chronoRunning,
                chronoPaused = chronoPaused,
                tasks = tasks,
                context = context,
                selectedTaskId = selectedTaskId,
                onSelectedTaskIdChange = { selectedTaskId = it },
                showChronoSummary = showChronoSummary,
                onShowChronoSummaryChange = { showChronoSummary = it },
                chronoSummaryDuration = chronoSummaryDuration,
                onChronoSummaryDurationChange = { chronoSummaryDuration = it },
                allTasks = tasks,
                selectedCategory = selectedCategory,
                onCategoryChange = { selectedCategory = it }
            )
            2 -> HistoryTab(
                sessions = sessions,
                allSessions = allSessions,
                viewModel = viewModel,
                dateRange = historyDateRange,
                onDateRangeChange = {
                    historyDateRange = it
                    viewModel.setHistoryDateRange(it)
                },
                tasks = tasks
            )
        }
    }

    val pendingCompletion by viewModel.pendingTaskCompletion.collectAsState()
    pendingCompletion?.let { pending ->
        val incompleteCount = pending.subtasks.count { it.status != "COMPLETED" }
        AlertDialog(
            onDismissRequest = { viewModel.cancelPendingTaskCompletion() },
            title = { Text("Complete with Subtasks?", fontWeight = FontWeight.Bold) },
            text = {
                Text("'${pending.task.title}' has $incompleteCount incomplete subtask(s). Would you like to complete all $incompleteCount subtask(s) as well?")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmCompleteTask(true) }) {
                    Text("Complete Subtasks")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { viewModel.confirmCompleteTask(false) }) {
                        Text("Only This Task")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.cancelPendingTaskCompletion() }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
private fun PomodoroTab(
    viewModel: MainViewModel,
    activeTask: TaskEntity?,
    secondsLeft: Int,
    isRunning: Boolean,
    pomodoroPhase: String,
    pomodoroCurrentSession: Int,
    pomodoroTargetSessions: Int?,
    tasks: List<TaskEntity>,
    templates: List<TimerTemplateEntity>,
    context: android.content.Context,
    selectedTaskId: Long?,
    onSelectedTaskIdChange: (Long?) -> Unit,
    markCompleteOnFinish: Boolean,
    onMarkCompleteOnFinishChange: (Boolean) -> Unit,
    focusMinutes: Int,
    onFocusMinutesChange: (Int) -> Unit,
    shortBreakMinutes: Int?,
    onShortBreakMinutesChange: (Int?) -> Unit,
    longBreakMinutes: Int?,
    onLongBreakMinutesChange: (Int?) -> Unit,
    targetSessions: Int?,
    onTargetSessionsChange: (Int?) -> Unit,
    selectedTemplateId: Long?,
    onSelectedTemplateIdChange: (Long?) -> Unit,
    showManageTemplates: Boolean,
    onShowManageTemplatesChange: (Boolean) -> Unit,
    showStopConfirm: Boolean,
    onShowStopConfirmChange: (Boolean) -> Unit,
    allTasks: List<TaskEntity>,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit
) {
    val isTimerActive = activeTask != null
    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val timeStr = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    val availableTasks = remember(tasks, selectedCategory) {
        val parentIds = tasks.filter { t -> tasks.any { it.parentTaskId == t.id } }.map { it.id }.toSet()
        tasks.filter { t ->
            t.status != "COMPLETED" && when (selectedCategory) {
                "TASKS" -> t.parentTaskId == null && t.type == "TASK" && t.id !in parentIds
                "SUBTASKS" -> t.parentTaskId != null && t.type == "TASK"
                "NOTES" -> t.parentTaskId == null && t.type == "NOTE" && t.id !in parentIds
                "SUB_NOTES" -> t.parentTaskId != null && t.type == "NOTE"
                "ALL" -> (t.type == "TASK" || t.type == "NOTE") && (t.parentTaskId != null || t.id !in parentIds)
                else -> false
            }
        }
    }
    val selectedTask = remember(selectedTaskId, tasks) {
        tasks.find { it.id == selectedTaskId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!isTimerActive) {
            // Timer Setup Card (matches StatsScreen card pattern)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Template selector
                    TemplateSelector(
                        templates = templates,
                        selectedTemplateId = selectedTemplateId,
                        onSelectedTemplateIdChange = { templateId ->
                            onSelectedTemplateIdChange(templateId)
                            templateId?.let { id ->
                                val template = templates.find { it.id == id }
                                if (template != null) {
                                    onFocusMinutesChange(template.focusMinutes)
                                    onShortBreakMinutesChange(template.shortBreakMinutes)
                                    onLongBreakMinutesChange(template.longBreakMinutes)
                                    onTargetSessionsChange(template.targetSessions)
                                }
                            }
                        },
                        onManageClick = { onShowManageTemplatesChange(true) },
                        focusMinutes = focusMinutes,
                        shortBreakMinutes = shortBreakMinutes,
                        longBreakMinutes = longBreakMinutes,
                        targetSessions = targetSessions
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mark complete on finish toggle
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            onClick = { onMarkCompleteOnFinishChange(!markCompleteOnFinish) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (markCompleteOnFinish)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                if (markCompleteOnFinish) {
                                    Icon(
                                        Icons.Default.CheckCircleOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = "Auto-complete",
                                    fontSize = 10.sp,
                                    color = if (markCompleteOnFinish)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Custom time controls
                    TimeControlRow("Focus", focusMinutes, 5, 120, onFocusMinutesChange)
                    TimeControlRowNullable("Short Break", shortBreakMinutes, 0, 30, onShortBreakMinutesChange)
                    TimeControlRowNullable("Long Break", longBreakMinutes, 0, 30, onLongBreakMinutesChange)
                    TimeControlRowNullable("Target Sessions", targetSessions, 0, 99, onTargetSessionsChange, step = 1, valueSuffix = "session")

                    Spacer(modifier = Modifier.height(8.dp))

                    // Start button
                    Button(
                        onClick = {
                            val task = selectedTask
                            if (task != null) {
                                viewModel.startPomodoro(
                                    context = context,
                                    task = task,
                                    focusMinutes = focusMinutes,
                                    targetSessions = targetSessions,
                                    shortBreakMinutes = shortBreakMinutes,
                                    longBreakMinutes = longBreakMinutes,
                                    markCompleteOnFinish = markCompleteOnFinish,
                                    templateName = templates.find { it.id == selectedTemplateId }?.name
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        enabled = selectedTask != null || selectedTaskId != null
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Focus", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            TaskSelectorSection(
                availableTasks = availableTasks,
                selectedTaskId = selectedTaskId,
                onSelectedTaskIdChange = onSelectedTaskIdChange,
                viewModel = viewModel,
                allTasks = allTasks,
                selectedCategory = selectedCategory,
                onCategoryChange = onCategoryChange
            )
        }

        // Timer Display & Controls (active state)
        if (isTimerActive) {
            // Timer Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Session info
                    val sessionLabel = if (pomodoroPhase == "FOCUS") "FOCUS" else "BREAK"
                    val targetLabel = pomodoroTargetSessions?.let { "/$it" } ?: "/∞"
                    Text(
                        text = "$sessionLabel · Session $pomodoroCurrentSession$targetLabel",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Timer
                    Text(
                        text = timeStr,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Controls
                    TimerControls(
                        isRunning = isRunning,
                        isPaused = false,
                        onDiscard = { onShowStopConfirmChange(true) },
                        onStartPause = {
                            if (isRunning) viewModel.pausePomodoro()
                            else viewModel.resumePomodoro(context)
                        },
                        onStop = { viewModel.stopPomodoroEarly(context) },
                        onMinusOne = { viewModel.adjustPomodoroPlusOne() },
                        minusOneLabel = "+1m",
                        onReset = { viewModel.resetPomodoro() },
                        isDiscardConfirm = showStopConfirm,
                        onDiscardConfirmDismiss = { onShowStopConfirmChange(false) },
                        onDiscardConfirmed = { viewModel.discardPomodoro(context); onShowStopConfirmChange(false) }
                    )
                }
            }

            // Task selector (locked while timer runs)
            TaskSelectorSection(
                availableTasks = availableTasks,
                selectedTaskId = selectedTaskId,
                onSelectedTaskIdChange = onSelectedTaskIdChange,
                viewModel = viewModel,
                isLocked = true,
                allTasks = allTasks,
                selectedCategory = selectedCategory,
                onCategoryChange = onCategoryChange
            )
        }
    }

    if (showManageTemplates) {
        ManageTemplatesDialog(
            templates = templates,
            viewModel = viewModel,
            onDismiss = { onShowManageTemplatesChange(false) }
        )
    }
}

@Composable
private fun CronometerTab(
    viewModel: MainViewModel,
    chronoElapsed: Long,
    chronoRunning: Boolean,
    chronoPaused: Boolean,
    tasks: List<TaskEntity>,
    context: android.content.Context,
    selectedTaskId: Long?,
    onSelectedTaskIdChange: (Long?) -> Unit,
    showChronoSummary: Boolean,
    onShowChronoSummaryChange: (Boolean) -> Unit,
    chronoSummaryDuration: Int,
    onChronoSummaryDurationChange: (Int) -> Unit,
    allTasks: List<TaskEntity>,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit
) {
    val hours = chronoElapsed / 3600
    val minutes = (chronoElapsed % 3600) / 60
    val seconds = (chronoElapsed % 60)
    val timeStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

    val availableTasks = remember(tasks, selectedCategory) {
        val parentIds = tasks.filter { t -> tasks.any { it.parentTaskId == t.id } }.map { it.id }.toSet()
        tasks.filter { t ->
            t.status != "COMPLETED" && when (selectedCategory) {
                "TASKS" -> t.parentTaskId == null && t.type == "TASK" && t.id !in parentIds
                "SUBTASKS" -> t.parentTaskId != null && t.type == "TASK"
                "NOTES" -> t.parentTaskId == null && t.type == "NOTE" && t.id !in parentIds
                "SUB_NOTES" -> t.parentTaskId != null && t.type == "NOTE"
                "ALL" -> (t.type == "TASK" || t.type == "NOTE") && (t.parentTaskId != null || t.id !in parentIds)
                else -> false
            }
        }
    }

    var chronoNote by remember { mutableStateOf("") }

    val chronoIsLocked = chronoRunning || chronoPaused

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Timer Card (matching PomodoroTab active-state pattern)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = timeStr,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onBackground,
                    softWrap = false,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(16.dp))

                TimerControls(
                    isRunning = chronoRunning,
                    isPaused = chronoPaused,
                    onDiscard = { viewModel.discardChronometer() },
                    onStartPause = {
                        if (!chronoRunning) {
                            viewModel.startChronometer(selectedTaskId)
                        } else {
                            viewModel.pauseChronometer()
                        }
                    },
                    onStop = {
                        onChronoSummaryDurationChange(chronoElapsed.toInt())
                        viewModel.stopChronometer()
                        onShowChronoSummaryChange(true)
                    },
                    onMinusOne = { viewModel.adjustChronoMinusOne() },
                    onReset = { viewModel.resetChronometer() },
                    isDiscardConfirm = false,
                    onDiscardConfirmDismiss = {},
                    onDiscardConfirmed = {}
                )
            }
        }

        // Task selector
        TaskSelectorSection(
            availableTasks = availableTasks,
            selectedTaskId = selectedTaskId,
            onSelectedTaskIdChange = onSelectedTaskIdChange,
            viewModel = viewModel,
            isLocked = chronoIsLocked,
            allTasks = allTasks,
            selectedCategory = selectedCategory,
            onCategoryChange = onCategoryChange
        )
    }

    if (showChronoSummary) {
        AlertDialog(
            onDismissRequest = { onShowChronoSummaryChange(false) },
            title = { Text("Session Summary", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Duration: ${chronoSummaryDuration / 60}m ${chronoSummaryDuration % 60}s",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    val selTask = tasks.find { it.id == selectedTaskId }
                    if (selTask != null) {
                        Text("Task: ${selTask.title}", fontSize = 14.sp)
                    }
                    OutlinedTextField(
                        value = chronoNote,
                        onValueChange = { chronoNote = it },
                        label = { Text("Note (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveChronometerSession(chronoSummaryDuration, selectedTaskId, chronoNote)
                    onShowChronoSummaryChange(false)
                    chronoNote = ""
                }) { Text("Save") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        viewModel.discardChronometer()
                        onShowChronoSummaryChange(false)
                        chronoNote = ""
                    }) { Text("Discard", color = MaterialTheme.colorScheme.error) }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { onShowChronoSummaryChange(false) }) { Text("Cancel") }
                }
            }
        )
    }
}

@Composable
private fun HistoryTab(
    sessions: List<TimerSessionEntity>,
    allSessions: List<TimerSessionEntity>,
    viewModel: MainViewModel,
    dateRange: String,
    onDateRangeChange: (String) -> Unit,
    tasks: List<TaskEntity>
) {
    var showAddManual by remember { mutableStateOf(false) }
    var editingSession by remember { mutableStateOf<TimerSessionEntity?>(null) }
    var showHistoryDatePicker by remember { mutableStateOf(false) }

    val historySelectedDate by viewModel.historySelectedDate.collectAsState()
    val highlightedDates = remember(allSessions) {
        allSessions.map { it.date }.toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Date range chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("today" to "Today", "week" to "Week", "month" to "Month").forEach { (key, label) ->
                FilterChip(
                    selected = dateRange == key,
                    onClick = { onDateRangeChange(key) },
                    label = { Text(label, fontSize = 12.sp) }
                )
            }

            if (historySelectedDate != null) {
                FilterChip(
                    selected = true,
                    onClick = { viewModel.clearHistoryDateSelection() },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(formatHistoryChipDate(historySelectedDate!!), fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                        }
                    }
                )
            }

            IconButton(
                onClick = { showHistoryDatePicker = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = "Pick date", modifier = Modifier.size(20.dp))
            }
        }

        // Add manual entry button
        OutlinedButton(
            onClick = { showAddManual = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Manual Entry")
        }

        // Total summary
        val totalSeconds = sessions.sumOf { it.durationSeconds }
        val totalHours = totalSeconds / 3600
        val totalMinutes = (totalSeconds % 3600) / 60
        Text(
            text = "${sessions.size} sessions · Total: ${totalHours}h ${totalMinutes}m",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Sessions list
        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No sessions in this period",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val groupedSessions = remember(sessions) {
                sessions.groupBy { it.date }.toSortedMap(compareByDescending { it })
            }
            val dateLabels = remember {
                val todayCal = Calendar.getInstance()
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = sdf.format(todayCal.time)
                todayCal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterday = sdf.format(todayCal.time)
                val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
                Triple(today, yesterday, currentYear)
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                groupedSessions.forEach { (dateStr, dateSessions) ->
                    item {
                        val headerText = when (dateStr) {
                            dateLabels.first -> "Today"
                            dateLabels.second -> "Yesterday"
                            else -> {
                                val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                                val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(parsed!!)
                                val fmt = if (year == dateLabels.third) "EEE, MMM d" else "EEE, MMM d, yyyy"
                                SimpleDateFormat(fmt, Locale.getDefault()).format(parsed)
                            }
                        }
                        Text(
                            text = headerText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                        )
                    }

                    items(dateSessions, key = { it.id }) { session ->
                        val relatedTask = tasks.find { it.id == session.taskId }
                        val timeOfDay = remember(session.timestamp) {
                            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(session.timestamp))
                        }
                        val sessionMinutes = session.durationSeconds / 60
                        val sessionSeconds = session.durationSeconds % 60
                        val durationLabel = if (sessionMinutes >= 60) {
                            "${sessionMinutes / 60}h ${sessionMinutes % 60}m"
                        } else {
                            "${sessionMinutes}m ${sessionSeconds}s"
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (session.type == "POMODORO") "\uD83C\uDF45" else "\u23F1",
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = durationLabel,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = relatedTask?.title ?: "(no task)",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (session.note.isNotEmpty()) {
                                        Text(
                                            text = session.note,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Text(
                                    text = timeOfDay,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                IconButton(onClick = { editingSession = session }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = {
                                    viewModel.deleteTimerSession(session.id)
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showHistoryDatePicker) {
        HistoryDatePickerDialog(
            highlightedDates = highlightedDates,
            onDismiss = { showHistoryDatePicker = false },
            onDateSelected = { gregorianDate ->
                viewModel.selectHistoryDate(gregorianDate)
                showHistoryDatePicker = false
            }
        )
    }

    if (showAddManual) {
        AddManualSessionDialog(
            tasks = tasks,
            viewModel = viewModel,
            onDismiss = { showAddManual = false }
        )
    }

    editingSession?.let { session ->
        EditSessionDialog(
            session = session,
            tasks = tasks,
            viewModel = viewModel,
            onDismiss = { editingSession = null }
        )
    }
}

private fun formatHistoryChipDate(dateStr: String): String {
    return try {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
        val cal = Calendar.getInstance()
        val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(cal.time)
        val dateYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(parsed!!)
        val fmt = if (dateYear == currentYear) "MMM d" else "MMM d, yyyy"
        SimpleDateFormat(fmt, Locale.getDefault()).format(parsed)
    } catch (e: Exception) {
        dateStr
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryDatePickerDialog(
    highlightedDates: Set<String>,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    var usePersian by remember { mutableStateOf(false) }

    val today = Calendar.getInstance()
    var currentYear by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH) + 1) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    fun toggleCalendar() {
        if (usePersian) {
            val gregorian = PersianCalendarHelper.getGregorianDateString(currentYear, currentMonth, 1)
            if (gregorian.isNotEmpty()) {
                val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(gregorian)!!
                val c = Calendar.getInstance()
                c.time = parsed
                currentYear = c.get(Calendar.YEAR)
                currentMonth = c.get(Calendar.MONTH) + 1
            }
        } else {
            val dateStr = "%04d-%02d-01".format(currentYear, currentMonth)
            val parts = PersianCalendarHelper.getPersianDateParts(dateStr)
            currentYear = parts.first
            currentMonth = parts.second
        }
        usePersian = !usePersian
        selectedDay = null
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                selectedDay?.let { day ->
                    val gregorian = if (usePersian) {
                        PersianCalendarHelper.getGregorianDateString(currentYear, currentMonth, day)
                    } else {
                        val c = Calendar.getInstance()
                        c.set(currentYear, currentMonth - 1, day)
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.time)
                    }
                    if (gregorian.isNotEmpty()) onDateSelected(gregorian)
                }
            }, enabled = selectedDay != null) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text(
                text = if (usePersian) "Select Persian Date" else "Select Date",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { toggleCalendar() }) {
                Text(
                    text = if (usePersian) "EN" else "FA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        HistoryCalendarGrid(
            year = currentYear,
            month = currentMonth,
            usePersian = usePersian,
            selectedDay = selectedDay,
            highlightedDates = highlightedDates,
            onDaySelected = { day -> selectedDay = day },
            onMonthChange = { y, m ->
                currentYear = y
                currentMonth = m
                selectedDay = null
            }
        )
    }
}

@Composable
private fun HistoryCalendarGrid(
    year: Int,
    month: Int,
    usePersian: Boolean,
    selectedDay: Int?,
    highlightedDates: Set<String>,
    onDaySelected: (Int) -> Unit,
    onMonthChange: (Int, Int) -> Unit
) {
    val daysInMonth: Int
    val firstDayOffset: Int

    if (usePersian) {
        val cal = android.icu.util.Calendar.getInstance(ULocale("fa_IR@calendar=persian"))
        cal.clear()
        cal.set(android.icu.util.Calendar.YEAR, year)
        cal.set(android.icu.util.Calendar.MONTH, month - 1)
        cal.set(android.icu.util.Calendar.DAY_OF_MONTH, 1)
        daysInMonth = cal.getActualMaximum(android.icu.util.Calendar.DAY_OF_MONTH)
        firstDayOffset = cal.get(android.icu.util.Calendar.DAY_OF_WEEK) % 7
    } else {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        firstDayOffset = cal.get(Calendar.DAY_OF_WEEK) - 1
    }

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = {
                if (usePersian) {
                    val (y, m) = PersianCalendarHelper.getOffsetPersianMonth(year, month, -1)
                    onMonthChange(y, m)
                } else {
                    if (month == 1) onMonthChange(year - 1, 12) else onMonthChange(year, month - 1)
                }
            }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous month")
            }

            Text(
                text = if (usePersian) {
                    val name = PersianCalendarHelper.monthNames.getOrElse(month - 1) { "" }
                    "$name $year"
                } else {
                    val c = Calendar.getInstance()
                    c.set(year, month - 1, 1)
                    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(c.time)
                },
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            IconButton(onClick = {
                if (usePersian) {
                    val (y, m) = PersianCalendarHelper.getOffsetPersianMonth(year, month, 1)
                    onMonthChange(y, m)
                } else {
                    if (month == 12) onMonthChange(year + 1, 1) else onMonthChange(year, month + 1)
                }
            }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next month")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val dayHeaders = if (usePersian) listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
        else listOf("S", "M", "T", "W", "T", "F", "S")

        Row(modifier = Modifier.fillMaxWidth()) {
            dayHeaders.forEach { header ->
                Text(
                    text = header,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = onSurfaceVariantColor,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        var day = 1
        val rowCount = (daysInMonth + firstDayOffset + 6) / 7

        for (row in 0 until rowCount) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellDay = if (row == 0 && col < firstDayOffset) null
                    else if (day <= daysInMonth) { val d = day; day++; d }
                    else null

                    val dateStr = if (cellDay != null) {
                        if (usePersian) PersianCalendarHelper.getGregorianDateString(year, month, cellDay)
                        else "%04d-%02d-%02d".format(year, month, cellDay)
                    } else null

                    val isToday = dateStr == todayStr
                    val isSelected = cellDay != null && cellDay == selectedDay
                    val hasHistory = dateStr in highlightedDates

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .then(
                                if (isSelected) Modifier.background(primaryColor)
                                else Modifier
                            )
                            .then(
                                if (!isSelected && isToday) Modifier.border(
                                    1.5.dp, primaryColor, androidx.compose.foundation.shape.CircleShape
                                ) else Modifier
                            )
                            .clickable(enabled = cellDay != null) {
                                if (cellDay != null) onDaySelected(cellDay)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (cellDay != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = cellDay.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) onPrimaryColor else onSurfaceColor
                                )
                                if (hasHistory) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(
                                                if (isSelected) onPrimaryColor else primaryColor
                                            )
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskSelectorSection(
    availableTasks: List<TaskEntity>,
    selectedTaskId: Long?,
    onSelectedTaskIdChange: (Long?) -> Unit,
    viewModel: MainViewModel,
    isLocked: Boolean = false,
    allTasks: List<TaskEntity>,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit
) {
    val parentTitleMap = remember(allTasks) {
        allTasks.associate { it.id to it.title }
    }

    val palette = remember {
        listOf(
            0xFFFF6B6B, 0xFF4ECDC4, 0xFF45B7D1, 0xFF96CEB4,
            0xFFFFEAA7, 0xFFDDA0DD, 0xFF98D8C8, 0xFFF7DC6F,
            0xFFBB8FCE, 0xFF85C1E9, 0xFFF0B27A, 0xFF82E0AA
        )
    }

    val parentColorMap = remember(allTasks) {
        allTasks.mapNotNull { it.parentTaskId }.distinct()
            .mapIndexed { i, id -> id to palette[i % palette.size] }
            .toMap()
    }

    // Wrapped in a unified Card matching the TIMER SETUP card style
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Section header inside the card
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "TASKS / SUBTASKS / NOTES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.weight(1f)
                )
                if (isLocked) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Locked",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "ALL" to "All",
                    "TASKS" to "Tasks",
                    "SUBTASKS" to "Subtasks",
                    "NOTES" to "Notes",
                    "SUB_NOTES" to "SubNotes"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = selectedCategory == key,
                        onClick = { onCategoryChange(key) },
                        label = { Text(label, fontSize = 10.sp) },
                        modifier = Modifier.height(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (availableTasks.isEmpty()) {
                // Beautiful empty state matching app pattern
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val emptyMsg = when (selectedCategory) {
                            "SUBTASKS" -> "No subtasks available"
                            "NOTES" -> "No notes available"
                            "SUB_NOTES" -> "No sub notes available"
                            "ALL" -> "No items available"
                            else -> "No tasks available"
                        }
                        val emptyHint = when (selectedCategory) {
                            "SUBTASKS" -> "Create a task with subtasks in the planner first"
                            "NOTES" -> "Create a note in the planner first"
                            "SUB_NOTES" -> "Create a note with subtasks in the planner first"
                            "ALL" -> "Create tasks or notes in the planner first"
                            else -> "Create a task in the planner first"
                        }
                        Text(
                            text = emptyMsg,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = emptyHint,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                availableTasks.forEachIndexed { index, task ->
                    val isSelected = task.id == selectedTaskId
                    TaskSectionItem(
                        task = task,
                        isSelected = isSelected,
                        isLocked = isLocked,
                        onSelect = {
                            if (!isLocked) {
                                onSelectedTaskIdChange(task.id)
                            }
                        },
                        onMarkComplete = { viewModel.markTaskCompleteFromTimer(task.id) },
                        parentTitleMap = parentTitleMap,
                        parentColorMap = parentColorMap
                    )
                    if (index < availableTasks.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 2.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskSectionItem(
    task: TaskEntity,
    isSelected: Boolean,
    isLocked: Boolean,
    onSelect: () -> Unit,
    onMarkComplete: () -> Unit,
    parentTitleMap: Map<Long, String> = emptyMap(),
    parentColorMap: Map<Long, Long> = emptyMap()
) {
    val alpha = if (isLocked && !isSelected) 0.5f else 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLocked) { onSelect() }
            .then(
                if (isSelected) Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                else Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection checkbox
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(
                    width = if (isSelected) 0.dp else 1.5.dp,
                    color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(6.dp)
                )
                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Task info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Type badge — 4-way: TASK / SUBTASK · Parent / NOTE / SUB NOTE · Parent
                when {
                    task.parentTaskId != null -> {
                        val parentTitle = parentTitleMap[task.parentTaskId] ?: "Unknown"
                        val groupColor = Color(parentColorMap[task.parentTaskId] ?: 0xFF4ECDC4)
                        val badgeText = if (task.type == "NOTE") "SUB NOTE · $parentTitle" else "SUBTASK · $parentTitle"
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = groupColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold,
                                color = groupColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    task.type == "NOTE" -> {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "NOTE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    else -> {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "TASK",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                if (task.label.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = task.label.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Mark complete button
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
                .clickable { onMarkComplete() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircleOutline,
                contentDescription = "Mark complete",
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}



@Composable
private fun TimerControls(
    isRunning: Boolean,
    isPaused: Boolean,
    onDiscard: () -> Unit,
    onStartPause: () -> Unit,
    onStop: () -> Unit,
    onMinusOne: () -> Unit,
    minusOneLabel: String = "-1m",
    onReset: () -> Unit,
    isDiscardConfirm: Boolean,
    onDiscardConfirmDismiss: () -> Unit,
    onDiscardConfirmed: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Discard
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(3.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer)
                .clickable { onDiscard() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Discard", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Start/Pause
        Box(
            modifier = Modifier
                .size(68.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable { onStartPause() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isRunning && !isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isRunning && !isPaused) "Pause" else "Start",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Stop
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(3.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
                .clickable { onStop() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Stop,
                contentDescription = "Stop",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Reset
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(3.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
                .clickable { onReset() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Reset",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // -1 min
        Box(
            modifier = Modifier
                .size(52.dp)
                .shadow(3.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
                .clickable { onMinusOne() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = minusOneLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (isDiscardConfirm) {
        AlertDialog(
            onDismissRequest = onDiscardConfirmDismiss,
            title = { Text("Discard Session?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure? Current progress won't be saved.") },
            confirmButton = {
                TextButton(onClick = onDiscardConfirmed) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDiscardConfirmDismiss) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ManageTemplatesDialog(
    templates: List<TimerTemplateEntity>,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<TimerTemplateEntity?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Timer Templates", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!showAddForm && editingTemplate == null) {
                    Button(
                        onClick = { showAddForm = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("New Template")
                    }
                    templates.forEach { template ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(template.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    val parts = mutableListOf("${template.focusMinutes}m focus")
                                    template.shortBreakMinutes?.let { if (it > 0) parts.add("${it}m break") }
                                    template.longBreakMinutes?.let { if (it > 0) parts.add("${it}m long break") }
                                    template.targetSessions?.let { parts.add("${it} sessions") }
                                    Text(parts.joinToString(" · "), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { editingTemplate = template }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { viewModel.deleteTemplate(template.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                } else {
                    TemplateForm(
                        initial = editingTemplate,
                        viewModel = viewModel,
                        onSaved = {
                            showAddForm = false
                            editingTemplate = null
                        },
                        onCancel = {
                            showAddForm = false
                            editingTemplate = null
                        }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun TemplateForm(
    initial: TimerTemplateEntity?,
    viewModel: MainViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var focus by remember { mutableIntStateOf(initial?.focusMinutes ?: 25) }
    var shortBreak by remember { mutableStateOf(initial?.shortBreakMinutes) }
    var longBreak by remember { mutableStateOf(initial?.longBreakMinutes) }
    var targets by remember { mutableStateOf(initial?.targetSessions) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Template Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TimeControlRow("Focus", focus, 5, 120) { focus = it }
        TimeControlRowNullable("Short Break", shortBreak, 0, 30, onValueChange = { shortBreak = it })
        TimeControlRowNullable("Long Break", longBreak, 0, 30, onValueChange = { longBreak = it })
        TimeControlRowNullable("Target Sessions", targets, 0, 99, onValueChange = { targets = it }, step = 1, valueSuffix = "session")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        if (initial != null) {
                            viewModel.updateTemplate(initial.id, name, focus, shortBreak, longBreak, targets)
                        } else {
                            viewModel.createTemplate(name, focus, shortBreak, longBreak, targets)
                        }
                        onSaved()
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
            OutlinedButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddManualSessionDialog(
    tasks: List<TaskEntity>,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var type by remember { mutableStateOf("POMODORO") }
    var durationMinutes by remember { mutableIntStateOf(25) }
    var durationSeconds by remember { mutableIntStateOf(0) }
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var note by remember { mutableStateOf("") }
    var selectedTaskId by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val availableTasks = remember(tasks) {
        tasks.filter { it.status != "COMPLETED" && (it.type == "TASK" || it.type == "NOTE") }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Manual Entry", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Type toggle
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "POMODORO",
                        onClick = { type = "POMODORO" },
                        label = { Text("Pomodoro") }
                    )
                    FilterChip(
                        selected = type == "CHRONOMETER",
                        onClick = { type = "CHRONOMETER" },
                        label = { Text("Chronometer") }
                    )
                }

                // Duration
                OutlinedTextField(
                    value = durationMinutes.toString(),
                    onValueChange = { durationMinutes = it.toIntOrNull() ?: 0 },
                    label = { Text("Duration (minutes)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Date
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                // Task selector
                var taskExpanded by remember { mutableStateOf(false) }
                val selTask = availableTasks.find { it.id == selectedTaskId }
                Box {
                    OutlinedTextField(
                        value = selTask?.title ?: "No task",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Task/Note") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { taskExpanded = true },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    DropdownMenu(
                        expanded = taskExpanded,
                        onDismissRequest = { taskExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = { selectedTaskId = null; taskExpanded = false }
                        )
                        availableTasks.forEach { task ->
                            DropdownMenuItem(
                                text = { Text(task.title) },
                                onClick = { selectedTaskId = task.id; taskExpanded = false }
                            )
                        }
                    }
                }

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.addManualTimerSession(
                    type = type,
                    taskId = selectedTaskId,
                    durationSeconds = durationMinutes * 60 + durationSeconds,
                    date = date,
                    note = note
                )
                onDismiss()
            }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSessionDialog(
    session: TimerSessionEntity,
    tasks: List<TaskEntity>,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var durationMinutes by remember { mutableIntStateOf(session.durationSeconds / 60) }
    var durationSeconds by remember { mutableIntStateOf(session.durationSeconds % 60) }
    var note by remember { mutableStateOf(session.note) }
    var date by remember { mutableStateOf(session.date) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Session", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = durationMinutes.toString(),
                    onValueChange = { durationMinutes = it.toIntOrNull() ?: 0 },
                    label = { Text("Duration (minutes)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.updateTimerSession(
                    session.id,
                    durationMinutes * 60 + durationSeconds,
                    note,
                    date
                )
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = run {
                try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)?.time
                } catch (_: Exception) { System.currentTimeMillis() }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
