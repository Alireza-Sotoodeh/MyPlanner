package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import kotlin.math.roundToInt
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.zIndex
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material3.Switch
import androidx.compose.material3.Slider
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.TaskEntity
import com.example.ui.components.ActivePomodoroWidget
import com.example.ui.components.DayReviewCard
import com.example.ui.components.MottoCard
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlannerScreen(viewModel: MainViewModel) {
    val todayDate by viewModel.todayDate.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val taskForPomodoroSetup by viewModel.taskForPomodoroSetup.collectAsState()
    val tabTitles = listOf("DAILY", "WEEKLY", "MONTHLY")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Headers and Planner Switcher
        HeaderSection(
            viewModel = viewModel, 
            onSettingsClick = { showSettingsDialog = true },
            onHomeClick = {
                selectedTab = 0
                viewModel.selectDate(todayDate)
            }
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 2.dp
                )
            },
            divider = {}
        ) {
            tabTitles.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            letterSpacing = 1.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Active widgets loaded inside planner screen for high utility context
        ActivePomodoroWidget(viewModel)

        val allTasks by viewModel.allTasks.collectAsState(initial = emptyList())
        val uniqueLabels = allTasks.mapNotNull { it.label.takeIf { label -> label.isNotBlank() } }.distinct().sorted()
        var selectedFilterLabel by remember { mutableStateOf<String?>(null) }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> DailyPlannerView(viewModel, selectedFilterLabel, uniqueLabels) { selectedFilterLabel = it }
                1 -> WeeklyPlannerView(viewModel, null)
                2 -> MonthlyPlannerView(viewModel, null)
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }

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

        val context = LocalContext.current

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
                        viewModel.selectTab(2) // Navigate to Pomodoro Screen (Tab index 2)
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
}

@Composable
fun HeaderSection(viewModel: MainViewModel, onSettingsClick: () -> Unit, onHomeClick: () -> Unit) {
    val selectedDate by viewModel.selectedDate.collectAsState()

    // Formatting date beautifully like the design theme: Thursday, October 24
    val formattedDate = remember(selectedDate) {
        try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
            if (date != null) {
                val gregorian = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(date)
                val persian = com.example.core.utils.PersianCalendarHelper.getPersianDateString(date)
                "$gregorian ($persian)"
            } else {
                selectedDate
            }
        } catch (e: Exception) {
            selectedDate
        }
    }

    val formattedDayOfWeek = remember(selectedDate) {
        try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
            if (date != null) {
                val gregorianDay = SimpleDateFormat("EEEE", Locale.getDefault()).format(date).uppercase()
                val persianDay = com.example.core.utils.PersianCalendarHelper.getPersianDayOfWeekName(selectedDate).uppercase()
                if (persianDay.isNotEmpty()) {
                    "$gregorianDay / $persianDay"
                } else {
                    gregorianDay
                }
            } else {
                "PLANNER"
            }
        } catch (e: Exception) {
            "PLANNER"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = formattedDayOfWeek,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp
            )
            Text(
                text = formattedDate,
                fontSize = 24.sp,
                fontWeight = FontWeight.Light, // Clean elegant thin typography
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onHomeClick) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            // Settings Icon Button
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun DailyPlannerView(viewModel: MainViewModel, filterLabel: String? = null, uniqueLabels: List<String> = emptyList(), onLabelSelected: (String?) -> Unit = {}) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedDate.collectAsState()
    val todayDate by viewModel.todayDate.collectAsState()
    val rawTasks by viewModel.dailyTasks.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val tasks = if (filterLabel != null) rawTasks.filter { it.label == filterLabel } else rawTasks

    val expandedSubtasksMap = remember { androidx.compose.runtime.mutableStateMapOf<Long, Boolean>() }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var subtasksToEdit by remember { mutableStateOf<List<TaskEntity>>(emptyList()) }
    var expandAllItems by remember { mutableStateOf(true) }
    var expandAllSubtasks by remember { mutableStateOf(true) }
    var showPendingDetailsDialog by remember { mutableStateOf(false) }
    var showDayReviewDialog by remember { mutableStateOf(false) }

    LaunchedEffect(expandAllSubtasks) {
        expandedSubtasksMap.clear()
    }
    
    var showHeaderExtras by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                if (available.y < -15) {
                    showHeaderExtras = false
                } else if (available.y > 15) {
                    showHeaderExtras = true
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .nestedScroll(nestedScrollConnection)
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = showHeaderExtras && uniqueLabels.isNotEmpty(),
            enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    androidx.compose.material3.FilterChip(
                        selected = filterLabel == null,
                        onClick = { onLabelSelected(null) },
                        label = { Text("All", fontSize = 12.sp) }
                    )
                }
                items(uniqueLabels) { label ->
                    androidx.compose.material3.FilterChip(
                        selected = filterLabel == label,
                        onClick = { onLabelSelected(if (filterLabel == label) null else label) },
                        label = { Text(label.uppercase(), fontSize = 12.sp) }
                    )
                }
            }
        }

        val todayMotto by viewModel.todayMotto.collectAsState()
        MottoCard(
            motto = todayMotto,
            visible = showHeaderExtras
        )

        // Day Navigator Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.selectDate(getOffsetDateString(selectedDate, -1))
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Previous Day",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            TextButton(onClick = { viewModel.selectDate(todayDate) }) {
                val relativeText = getRelativeDayString(selectedDate, todayDate)
                Text(
                    text = relativeText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }

            IconButton(onClick = {
                viewModel.selectDate(getOffsetDateString(selectedDate, 1))
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "Next Day",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Tasks Card Panel (Goodtime flat container)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Panel Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAILY INTENTIONS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )

                    val pendingCount = tasks.count { it.status != "COMPLETED" }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.triggerReorderByPriority() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Reorder Tasks by Priority",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { expandAllItems = !expandAllItems },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (expandAllItems) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (expandAllItems) "Collapse All" else "Expand All",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { expandAllSubtasks = !expandAllSubtasks },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (expandAllSubtasks) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                                contentDescription = if (expandAllSubtasks) "Collapse All Subtasks" else "Expand All Subtasks",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { showPendingDetailsDialog = !showPendingDetailsDialog }
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "$pendingCount PENDING",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (showPendingDetailsDialog) {
                                val pendingMainTasks = tasks.filter { it.parentTaskId == null && it.status != "COMPLETED" }
                                val mainHighCount = pendingMainTasks.count { it.priorityLevel.equals("High", ignoreCase = true) }
                                val mainMediumCount = pendingMainTasks.count { it.priorityLevel.equals("Medium", ignoreCase = true) }
                                val mainLowCount = pendingMainTasks.count { it.priorityLevel.equals("Low", ignoreCase = true) }

                                val dailyMainTaskIds = tasks.filter { it.parentTaskId == null }.map { it.id }.toSet()
                                val dailySubtasks = allTasks.filter { it.parentTaskId in dailyMainTaskIds }
                                val pendingSubtasks = dailySubtasks.filter { it.status != "COMPLETED" }
                                val importantSubtaskCount = pendingSubtasks.count { it.subtaskImportance == "IMPORTANT" }
                                val optionalSubtaskCount = pendingSubtasks.count { it.subtaskImportance == "OPTIONAL" }

                                val density = androidx.compose.ui.platform.LocalDensity.current
                                val offsetY = with(density) { 32.dp.roundToPx() }

                                Popup(
                                    alignment = Alignment.TopEnd,
                                    offset = IntOffset(x = 0, y = offsetY),
                                    onDismissRequest = { showPendingDetailsDialog = false },
                                    properties = PopupProperties(focusable = true)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 8.dp,
                                        shadowElevation = 8.dp,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                        modifier = Modifier
                                            .width(280.dp)
                                            .padding(4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(
                                                text = "Pending Tasks",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            // Main Tasks Summary
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = "Main Tasks: ${pendingMainTasks.size}",
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                                
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    // High
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Color(0xFFE53935).copy(alpha = 0.1f))
                                                            .padding(vertical = 6.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Text("🔴 High", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                                                            Text("$mainHighCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                                                        }
                                                    }
                                                    // Medium
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Color(0xFFFB8C00).copy(alpha = 0.1f))
                                                            .padding(vertical = 6.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Text("🟡 Med", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFB8C00))
                                                            Text("$mainMediumCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFB8C00))
                                                        }
                                                    }
                                                    // Low
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Color(0xFF43A047).copy(alpha = 0.1f))
                                                            .padding(vertical = 6.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Text("🟢 Low", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF43A047))
                                                            Text("$mainLowCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF43A047))
                                                        }
                                                    }
                                                }
                                            }

                                            // Subtasks Summary
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = "Subtasks: ${pendingSubtasks.size}",
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                                
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    // Important
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                                            .padding(vertical = 6.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Text("⭐ Important", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                                            Text("$importantSubtaskCount", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                                        }
                                                    }
                                                    // Optional
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                            .padding(vertical = 6.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Text("☕ Optional", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                            Text("$optionalSubtaskCount", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (tasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Your daily log is empty.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap + to add standard tasks, events, or notes.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    var showCompleted by remember { mutableStateOf(false) }
                    val itemHeights = remember { androidx.compose.runtime.mutableStateMapOf<Long, Int>() }
                    var draggingTaskId by remember { mutableStateOf<Long?>(null) }
                    var dragOffsetX by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
                    var dragOffsetY by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
                    var draggedTasks by remember { mutableStateOf<List<TaskEntity>?>(null) }
                    val density = androidx.compose.ui.platform.LocalDensity.current

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
                    ) {
                        val mainTasks = tasks.filter { it.parentTaskId == null }
                        val activeTasks = mainTasks.filter { it.status != "COMPLETED" }
                        val completedTasks = mainTasks.filter { it.status == "COMPLETED" }

                        val displayedActiveTasks = draggedTasks ?: activeTasks

                        items(displayedActiveTasks, key = { it.id }) { task ->
                            var showInteractDialog by remember { mutableStateOf(false) }
                            val taskSubtasks = allTasks.filter { it.parentTaskId == task.id }.sortedWith(compareBy({ it.priority }, { it.id }))

                            Box(modifier = Modifier.animateItem()) {
                                BulletTaskItem(
                                    modifier = Modifier.onGloballyPositioned { itemHeights[task.id] = it.size.height },
                                    task = task,
                                    subtasks = taskSubtasks,
                                    isExpanded = expandAllItems,
                                    isSubtasksExpanded = expandedSubtasksMap[task.id] ?: expandAllSubtasks,
                                    onCheckToggle = { viewModel.toggleTaskCompletion(task, taskSubtasks) },
                                    onMigrate = { targetDate -> viewModel.migrateTask(task, targetDate) },
                                    onDelete = { viewModel.deleteTask(task) },
                                    onStartPomodoro = { 
                                        viewModel.setTaskForPomodoroSetup(task)
                                    },
                                    onStartSubtaskPomodoro = { subtask ->
                                        viewModel.setTaskForPomodoroSetup(subtask)
                                    },
                                    onTaskClick = { showInteractDialog = true },
                                    onEdit = {
                                        taskToEdit = task
                                        subtasksToEdit = taskSubtasks
                                        showAddTaskDialog = true
                                    },
                                    onSubtaskToggle = { subtask -> viewModel.toggleTaskCompletion(subtask, emptyList()) },
                                    onMigrateSubtask = { subtask, date -> viewModel.migrateTask(subtask, date) },
                                    onMakeMainTask = { subtask -> viewModel.updateTask(subtask.copy(parentTaskId = null)) },
                                    onDeleteSubtask = { subtask -> viewModel.deleteTask(subtask) },
                                    onReorderSubtask = { subtask, subtaskList, delta -> viewModel.reorderTask(subtask, subtaskList, delta, false) },
                                    isDragging = (draggingTaskId == task.id),
                                    dragOffsetX = if (draggingTaskId == task.id) dragOffsetX else 0f,
                                    dragOffsetY = if (draggingTaskId == task.id) dragOffsetY else 0f,
                                    onDragStart = {
                                        draggingTaskId = task.id
                                        dragOffsetX = 0f
                                        dragOffsetY = 0f
                                        draggedTasks = activeTasks.toList()
                                    },
                                    onDrag = { dragAmount ->
                                        if (draggingTaskId == task.id) {
                                            dragOffsetX += dragAmount.x
                                            dragOffsetY += dragAmount.y
                                            
                                            val currentList = draggedTasks
                                            if (currentList != null) {
                                                val draggedIndex = currentList.indexOfFirst { it.id == task.id }
                                                if (draggedIndex != -1) {
                                                    val spacing = with(density) { 8.dp.toPx() }
                                                    if (dragOffsetY > 0) {
                                                        if (draggedIndex < currentList.size - 1) {
                                                            val nextItem = currentList[draggedIndex + 1]
                                                            val nextHeight = itemHeights[nextItem.id] ?: 150
                                                            val threshold = nextHeight / 2f + spacing
                                                            if (dragOffsetY > threshold) {
                                                                val mutableList = currentList.toMutableList()
                                                                mutableList.removeAt(draggedIndex)
                                                                mutableList.add(draggedIndex + 1, task)
                                                                draggedTasks = mutableList
                                                                dragOffsetY -= (nextHeight + spacing)
                                                            }
                                                        }
                                                    } else if (dragOffsetY < 0) {
                                                        if (draggedIndex > 0) {
                                                            val prevItem = currentList[draggedIndex - 1]
                                                            val prevHeight = itemHeights[prevItem.id] ?: 150
                                                            val threshold = -prevHeight / 2f - spacing
                                                            if (dragOffsetY < threshold) {
                                                                val mutableList = currentList.toMutableList()
                                                                mutableList.removeAt(draggedIndex)
                                                                mutableList.add(draggedIndex - 1, task)
                                                                draggedTasks = mutableList
                                                                dragOffsetY += (prevHeight + spacing)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        if (draggingTaskId == task.id) {
                                            val currentList = draggedTasks
                                            val originalIndex = activeTasks.indexOfFirst { it.id == task.id }
                                            if (currentList != null && originalIndex != -1) {
                                                val finalIndex = currentList.indexOfFirst { it.id == task.id }
                                                val isSubtask = dragOffsetX > with(density) { 50.dp.toPx() }
                                                val deltaIndex = finalIndex - originalIndex
                                                if (deltaIndex != 0 || isSubtask) {
                                                    viewModel.reorderTask(task, activeTasks, deltaIndex, isSubtask)
                                                }
                                            }
                                            draggingTaskId = null
                                            draggedTasks = null
                                            dragOffsetX = 0f
                                            dragOffsetY = 0f
                                        }
                                    },
                                    onDragCancel = {
                                        if (draggingTaskId == task.id) {
                                            draggingTaskId = null
                                            draggedTasks = null
                                            dragOffsetX = 0f
                                            dragOffsetY = 0f
                                        }
                                    },
                                    onToggleSubtasksExpanded = { expanded ->
                                        expandedSubtasksMap[task.id] = expanded
                                    },
                                    onReorder = null
                                )
                            }

                            if (showInteractDialog) {
                                val sessions by viewModel.getSessionsForTask(task.id).collectAsState(initial = emptyList())
                                TaskInteractionDialog(
                                    task = task,
                                    sessions = sessions,
                                    onDismiss = { showInteractDialog = false },
                                    onMarkAsDone = { viewModel.toggleTaskCompletion(task) },
                                    onMarkAsDoneWithDuration = { duration ->
                                        viewModel.completeTaskWithManualDuration(task, duration)
                                    },
                                    onStartPomodoro = {
                                        viewModel.setTaskForPomodoroSetup(task)
                                    }
                                )
                            }
                        }

                        if (completedTasks.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable { showCompleted = !showCompleted }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Completed (${completedTasks.size})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Icon(
                                        imageVector = if (showCompleted) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Toggle Completed",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            if (showCompleted) {
                                items(completedTasks, key = { it.id }) { task ->
                                    var showInteractDialog by remember { mutableStateOf(false) }
                                    val taskSubtasks = allTasks.filter { it.parentTaskId == task.id }.sortedWith(compareBy({ it.priority }, { it.id }))

                                    Box(modifier = Modifier.animateItem()) {
                                        BulletTaskItem(
                                            task = task,
                                            subtasks = taskSubtasks,
                                            isExpanded = expandAllItems,
                                            isSubtasksExpanded = expandedSubtasksMap[task.id] ?: expandAllSubtasks,
                                            onCheckToggle = { viewModel.toggleTaskCompletion(task, taskSubtasks) },
                                            onMigrate = { targetDate -> viewModel.migrateTask(task, targetDate) },
                                            onDelete = { viewModel.deleteTask(task) },
                                            onStartPomodoro = { 
                                                viewModel.setTaskForPomodoroSetup(task)
                                            },
                                            onStartSubtaskPomodoro = { subtask ->
                                                viewModel.setTaskForPomodoroSetup(subtask)
                                            },
                                            onTaskClick = { showInteractDialog = true },
                                            onEdit = {
                                                taskToEdit = task
                                                subtasksToEdit = taskSubtasks
                                                showAddTaskDialog = true
                                            },
                                            onSubtaskToggle = { subtask -> viewModel.toggleTaskCompletion(subtask, emptyList()) },
                                            onMigrateSubtask = { subtask, date -> viewModel.migrateTask(subtask, date) },
                                            onMakeMainTask = { subtask -> viewModel.updateTask(subtask.copy(parentTaskId = null)) },
                                            onDeleteSubtask = { subtask -> viewModel.deleteTask(subtask) },
                                            onReorderSubtask = { subtask, subtaskList, delta -> viewModel.reorderTask(subtask, subtaskList, delta, false) },
                                            onToggleSubtasksExpanded = { expanded ->
                                                expandedSubtasksMap[task.id] = expanded
                                            }
                                        )
                                    }
                                    
                                    if (showInteractDialog) {
                                        val sessions by viewModel.getSessionsForTask(task.id).collectAsState(initial = emptyList())
                                        TaskInteractionDialog(
                                            task = task,
                                            sessions = sessions,
                                            onDismiss = { showInteractDialog = false },
                                            onMarkAsDone = { viewModel.toggleTaskCompletion(task) },
                                            onMarkAsDoneWithDuration = { duration ->
                                                viewModel.completeTaskWithManualDuration(task, duration)
                                            },
                                            onStartPomodoro = {
                                                viewModel.setTaskForPomodoroSetup(task)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                val reviewForDate by viewModel.reviewForDate(selectedDate).collectAsState(initial = null)
                DayReviewCard(
                    review = reviewForDate,
                    date = selectedDate,
                    onClick = { showDayReviewDialog = true }
                )
            }

            // FLOATING ACTION BUTTON inside the container or at bottom right
            FloatingActionButton(
                onClick = { 
                    taskToEdit = null
                    subtasksToEdit = emptyList()
                    showAddTaskDialog = true 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.background,
                shape = CircleShape, // Pure round for clean icon vibe
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Intention")
            }
        }
    }

    if (showAddTaskDialog) {
        com.example.ui.components.TaskManagerDialog(
            viewModel = viewModel,
            initialDate = selectedDate,
            taskToEdit = taskToEdit,
            initialSubtasks = subtasksToEdit,
            onDismiss = { 
                showAddTaskDialog = false
                taskToEdit = null
                subtasksToEdit = emptyList()
            }
        )
    }

    if (showDayReviewDialog) {
        DayReviewScreen(
            viewModel = viewModel,
            initialDate = selectedDate,
            onBack = { showDayReviewDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BulletTaskItem(
    task: TaskEntity,
    subtasks: List<TaskEntity> = emptyList(),
    isExpanded: Boolean = true,
    isSubtasksExpanded: Boolean = true,
    onCheckToggle: () -> Unit,
    onMigrate: (String) -> Unit,
    onDelete: () -> Unit,
    onStartPomodoro: () -> Unit,
    onTaskClick: () -> Unit,
    onEdit: () -> Unit = {},
    onSubtaskToggle: (TaskEntity) -> Unit = {},
    onMigrateSubtask: (TaskEntity, String) -> Unit = { _, _ -> },
    onMakeMainTask: (TaskEntity) -> Unit = {},
    onDeleteSubtask: (TaskEntity) -> Unit = {},
    onReorder: ((Float, Boolean) -> Unit)? = null,
    onReorderSubtask: ((TaskEntity, List<TaskEntity>, Int) -> Unit)? = null,
    onStartSubtaskPomodoro: (TaskEntity) -> Unit = {},
    isDragging: Boolean = false,
    dragOffsetX: Float = 0f,
    dragOffsetY: Float = 0f,
    onDragStart: (() -> Unit)? = null,
    onDrag: ((androidx.compose.ui.geometry.Offset) -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null,
    onDragCancel: (() -> Unit)? = null,
    onToggleSubtasksExpanded: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expandedMenu by remember { mutableStateOf(false) }
    val isCompleted = task.status == "COMPLETED"

    var offsetXInternal by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var offsetYInternal by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var isDraggingInternal by remember { mutableStateOf(false) }

    var draggingSubtaskId by remember { mutableStateOf<Long?>(null) }
    var subtaskDragOffsetY by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var draggedSubtasks by remember(subtasks) { mutableStateOf<List<TaskEntity>?>(null) }
    val subtaskItemHeights = remember { androidx.compose.runtime.mutableStateMapOf<Long, Int>() }
    var showSubtaskSelectorDialog by remember { mutableStateOf(false) }

    val actualIsDragging = if (onDragStart != null) isDragging else isDraggingInternal
    val actualOffsetX = if (onDragStart != null) dragOffsetX else offsetXInternal
    val actualOffsetY = if (onDragStart != null) dragOffsetY else offsetYInternal

    val density = androidx.compose.ui.platform.LocalDensity.current
    val isSubtaskPreview = actualIsDragging && actualOffsetX > with(density) { 50.dp.toPx() }

    // Lift-up and scale-up spring animations when dragging
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (actualIsDragging) 1.04f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "dragScale"
    )

    val shadowElevation by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (actualIsDragging) 12.dp else 0.dp,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "dragShadow"
    )

    val backgroundColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (actualIsDragging) {
            MaterialTheme.colorScheme.surfaceVariant
        } else if (isCompleted) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        } else {
            Color.Transparent
        },
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
        label = "cardBgColor"
    )

    // Smooth horizontal slide for subtask indentation preview
    val animatedStartPadding by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isSubtaskPreview) 24.dp else 0.dp,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "subtaskPadding"
    )

    Column(
        modifier = modifier.then(Modifier
            .fillMaxWidth()
            .zIndex(if (actualIsDragging) 10f else 0f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .offset { IntOffset(actualOffsetX.roundToInt(), actualOffsetY.roundToInt()) }
            .shadow(elevation = shadowElevation, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .then(
                if (onDragStart != null && !isCompleted) {
                    Modifier.pointerInput(task.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { _ -> onDragStart() },
                            onDragEnd = { onDragEnd?.invoke() },
                            onDragCancel = { onDragCancel?.invoke() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag?.invoke(dragAmount)
                            }
                        )
                    }
                } else if (onReorder != null && !isCompleted) {
                    Modifier.pointerInput(task.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { _ -> isDraggingInternal = true },
                            onDragEnd = {
                                isDraggingInternal = false
                                val isSubtask = offsetXInternal > 50.dp.toPx()
                                val finalOffsetY = offsetYInternal
                                offsetXInternal = 0f
                                offsetYInternal = 0f
                                if (finalOffsetY != 0f || isSubtask) {
                                    onReorder(finalOffsetY, isSubtask)
                                }
                            },
                            onDragCancel = {
                                isDraggingInternal = false
                                offsetXInternal = 0f
                                offsetYInternal = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetXInternal += dragAmount.x
                                offsetYInternal += dragAmount.y
                            }
                        )
                    }
                } else Modifier
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp + animatedStartPadding, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.Top
        ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = isSubtaskPreview,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandHorizontally(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkHorizontally()
        ) {
            Icon(
                imageVector = Icons.Default.SubdirectoryArrowRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp).padding(end = 4.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        if (task.labelColor != null) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(task.labelColor))
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Bullet Selector/Checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                .background(if (isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { onCheckToggle() },
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                // Visual indicators of Bullet Journal Types: Task (•), Event (o), Note (-)
                androidx.compose.animation.AnimatedContent(
                    targetState = task.type,
                    transitionSpec = {
                        (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(220)) +
                                androidx.compose.animation.scaleIn(initialScale = 0.8f, animationSpec = androidx.compose.animation.core.tween(220)))
                            .togetherWith(
                                androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(220)) +
                                        androidx.compose.animation.scaleOut(targetScale = 0.8f, animationSpec = androidx.compose.animation.core.tween(220))
                            )
                    },
                    label = "bulletTypeTransition",
                    modifier = Modifier.align(Alignment.Center)
                ) { targetType ->
                    Box(
                        modifier = Modifier.size(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (targetType) {
                            "TASK" -> {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.CircleShape)
                                )
                            }
                            "EVENT" -> {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .border(2.dp, MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.CircleShape)
                                )
                            }
                            "NOTE" -> {
                                Box(
                                    modifier = Modifier
                                        .size(width = 8.dp, height = 2.dp)
                                        .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(1.dp))
                                )
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Task Content Details
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onTaskClick() }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = task.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompleted) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onBackground,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
            
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val priorityColor = when(task.priorityLevel) {
                    "High" -> Color(0xFFE53935)
                    "Medium" -> Color(0xFFFB8C00)
                    "Low" -> Color(0xFF43A047)
                    else -> MaterialTheme.colorScheme.secondary
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = priorityColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${task.priorityLevel.uppercase()} PRIORITY",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = priorityColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (task.label.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (task.labelColor != null) Color(task.labelColor).copy(alpha = 0.2f) else MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = task.label.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (task.labelColor != null) Color(task.labelColor) else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                if (subtasks.isNotEmpty()) {
                    val completed = subtasks.count { it.status == "COMPLETED" }
                    val total = subtasks.size
                    val isAllCompleted = completed == total
                    val subtaskColor = if (isAllCompleted) Color(0xFF43A047) else MaterialTheme.colorScheme.primary
                    
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = subtaskColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "$completed/$total SUBTASKS",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = subtaskColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = isExpanded,
                enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
            ) {
                Column {
                    if (task.type == "EVENT" && !task.eventTime.isNullOrBlank()) {
                Text(
                    text = "🕐 ${task.eventTime}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            if (task.description.isNotEmpty()) {
                Text(
                    text = task.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${task.durationMinutes * task.pomodorosCompleted} min and ${task.pomodorosCompleted} session(s) done",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
                }
            }

            // Subtasks
            if (subtasks.isNotEmpty()) {
                val completedSubtasks = subtasks.count { it.status == "COMPLETED" }
                val progress = if (subtasks.isEmpty()) 0f else completedSubtasks.toFloat() / subtasks.size
                var subtasksExpanded by remember(isSubtasksExpanded) { mutableStateOf(isSubtasksExpanded) }
                
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    modifier = Modifier.fillMaxWidth().clickable { 
                        val nextExpanded = !subtasksExpanded
                        subtasksExpanded = nextExpanded
                        onToggleSubtasksExpanded(nextExpanded)
                    }
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (subtasksExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Subtasks",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                androidx.compose.animation.AnimatedVisibility(
                    visible = subtasksExpanded,
                    enter = androidx.compose.animation.expandVertically(
                        animationSpec = androidx.compose.animation.core.spring(
                            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy
                        )
                    ) + androidx.compose.animation.fadeIn(
                        animationSpec = androidx.compose.animation.core.tween(250)
                    ),
                    exit = androidx.compose.animation.shrinkVertically(
                        animationSpec = androidx.compose.animation.core.spring(
                            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy
                        )
                    ) + androidx.compose.animation.fadeOut(
                        animationSpec = androidx.compose.animation.core.tween(200)
                    )
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier.padding(start = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                        val displayedSubtasks = draggedSubtasks ?: subtasks
                        displayedSubtasks.forEach { subtask ->
                            val subCompleted = subtask.status == "COMPLETED"
                            var subMenuExpanded by remember { mutableStateOf(false) }
                            val isCurrentDragging = draggingSubtaskId == subtask.id
                            val subtaskOffsetY = if (isCurrentDragging) subtaskDragOffsetY else 0f

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onGloballyPositioned { subtaskItemHeights[subtask.id] = it.size.height }
                                    .zIndex(if (isCurrentDragging) 10f else 0f)
                                    .graphicsLayer {
                                        if (isCurrentDragging) {
                                            scaleX = 1.04f
                                            scaleY = 1.04f
                                        }
                                    }
                                    .offset { IntOffset(0, subtaskOffsetY.roundToInt()) }
                                    .shadow(elevation = if (isCurrentDragging) 8.dp else 0.dp, shape = RoundedCornerShape(8.dp))
                                    .background(if (isCurrentDragging) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f) else Color.Transparent)
                                    .then(
                                        if (onReorderSubtask != null && !subCompleted) {
                                            Modifier.pointerInput(subtask.id) {
                                                detectDragGesturesAfterLongPress(
                                                    onDragStart = {
                                                        draggingSubtaskId = subtask.id
                                                        subtaskDragOffsetY = 0f
                                                        draggedSubtasks = subtasks.toList()
                                                    },
                                                    onDragEnd = {
                                                        if (draggingSubtaskId == subtask.id) {
                                                            val currentList = draggedSubtasks
                                                            val originalIndex = subtasks.indexOfFirst { it.id == subtask.id }
                                                            if (currentList != null && originalIndex != -1) {
                                                                val finalIndex = currentList.indexOfFirst { it.id == subtask.id }
                                                                val deltaIndex = finalIndex - originalIndex
                                                                if (deltaIndex != 0) {
                                                                    onReorderSubtask(subtask, subtasks, deltaIndex)
                                                                }
                                                            }
                                                            draggingSubtaskId = null
                                                            draggedSubtasks = null
                                                            subtaskDragOffsetY = 0f
                                                        }
                                                    },
                                                    onDragCancel = {
                                                        if (draggingSubtaskId == subtask.id) {
                                                            draggingSubtaskId = null
                                                            draggedSubtasks = null
                                                            subtaskDragOffsetY = 0f
                                                        }
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        subtaskDragOffsetY += dragAmount.y
                                                        
                                                        val currentList = draggedSubtasks
                                                        if (currentList != null) {
                                                            val draggedIndex = currentList.indexOfFirst { it.id == subtask.id }
                                                            if (draggedIndex != -1) {
                                                                val spacing = with(density) { 4.dp.toPx() }
                                                                if (subtaskDragOffsetY > 0) {
                                                                    if (draggedIndex < currentList.size - 1) {
                                                                        val nextItem = currentList[draggedIndex + 1]
                                                                        val nextHeight = subtaskItemHeights[nextItem.id] ?: 100
                                                                        val threshold = nextHeight / 2f + spacing
                                                                        if (subtaskDragOffsetY > threshold) {
                                                                            val mutableList = currentList.toMutableList()
                                                                            mutableList.removeAt(draggedIndex)
                                                                            mutableList.add(draggedIndex + 1, subtask)
                                                                            draggedSubtasks = mutableList
                                                                            subtaskDragOffsetY -= (nextHeight + spacing)
                                                                        }
                                                                    }
                                                                } else if (subtaskDragOffsetY < 0) {
                                                                    if (draggedIndex > 0) {
                                                                        val prevItem = currentList[draggedIndex - 1]
                                                                        val prevHeight = subtaskItemHeights[prevItem.id] ?: 100
                                                                        val threshold = -prevHeight / 2f - spacing
                                                                        if (subtaskDragOffsetY < threshold) {
                                                                            val mutableList = currentList.toMutableList()
                                                                            mutableList.removeAt(draggedIndex)
                                                                            mutableList.add(draggedIndex - 1, subtask)
                                                                            draggedSubtasks = mutableList
                                                                            subtaskDragOffsetY += (prevHeight + spacing)
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                        } else Modifier
                                    )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Icon(
                                        imageVector = Icons.Default.SubdirectoryArrowRight,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp).padding(end = 4.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically, 
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                            .clickable { onSubtaskToggle(subtask) }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (subCompleted) Icons.Default.Check else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = if (subCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        val importanceStr = when (subtask.subtaskImportance) {
                                            "IMPORTANT" -> "⭐ "
                                            "OPTIONAL" -> "☕ "
                                            else -> ""
                                        }
                                        Text(
                                            text = "$importanceStr${subtask.title}",
                                            fontSize = 11.sp,
                                            color = if (subCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            textDecoration = if (subCompleted) TextDecoration.LineThrough else null,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    
                                    Box {
                                        IconButton(onClick = { subMenuExpanded = true }, modifier = Modifier.size(24.dp)) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Subtask Actions",
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = subMenuExpanded,
                                            onDismissRequest = { subMenuExpanded = false },
                                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Done/Undone") },
                                                onClick = {
                                                    onSubtaskToggle(subtask)
                                                    subMenuExpanded = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Postpone to Tomorrow") },
                                                onClick = {
                                                    onMigrateSubtask(subtask, getOffsetDateString(subtask.date, 1))
                                                    subMenuExpanded = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Make Main Task") },
                                                onClick = {
                                                    onMakeMainTask(subtask)
                                                    subMenuExpanded = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Remove") },
                                                onClick = {
                                                    onDeleteSubtask(subtask)
                                                    subMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } // Close AnimatedVisibility
        } // Close if (subtasks.isNotEmpty())
    } // Close Column

    // Quick Pomodoro Trigger
        if (!isCompleted && task.type == "TASK") {
            IconButton(
                onClick = {
                    if (subtasks.isEmpty()) {
                        onStartPomodoro()
                    } else {
                        showSubtaskSelectorDialog = true
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Pomodoro",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (showSubtaskSelectorDialog) {
            val uncompletedSubtasks = subtasks.filter { it.status != "COMPLETED" }
            AlertDialog(
                onDismissRequest = { showSubtaskSelectorDialog = false },
                title = {
                    Text(
                        text = "Select Subtask for Pomodoro",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Choose an uncompleted subtask to focus on. The timer will start immediately.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (uncompletedSubtasks.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "All subtasks are already completed!",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uncompletedSubtasks.forEach { subtask ->
                                    val subImportanceStr = when (subtask.subtaskImportance) {
                                        "IMPORTANT" -> "⭐ "
                                        "OPTIONAL" -> "☕ "
                                        else -> ""
                                    }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onStartSubtaskPomodoro(subtask)
                                                showSubtaskSelectorDialog = false
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Start",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "$subImportanceStr${subtask.title}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSubtaskSelectorDialog = false }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            )
        }

        // Dropdown Menu Action triggers
        Box {
            IconButton(onClick = { expandedMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Task Actions",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            DropdownMenu(
                expanded = expandedMenu,
                onDismissRequest = { expandedMenu = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                DropdownMenuItem(
                    text = { Text("Edit Intention") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    onClick = {
                        onEdit()
                        expandedMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Migrate to Tomorrow") },
                    leadingIcon = { Icon(Icons.Default.Redo, contentDescription = null) },
                    onClick = {
                        val tomorrow = getOffsetDateString(task.date, 1)
                        onMigrate(tomorrow)
                        expandedMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Reschedule to Next Week") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    onClick = {
                        val nextWeek = getOffsetDateString(task.date, 7)
                        onMigrate(nextWeek)
                        expandedMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete Intention") },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    onClick = {
                        onDelete()
                        expandedMenu = false
                    }
                )
            }
        }
    }
    if (subtasks.isNotEmpty()) {
        val completedSubtasks = subtasks.count { it.status == "COMPLETED" }
        val progress = if (subtasks.isEmpty()) 0f else completedSubtasks.toFloat() / subtasks.size
        val animatedProgress by androidx.compose.animation.core.animateFloatAsState(targetValue = progress)
        androidx.compose.material3.LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    }
}
}

@Composable
fun WeeklyPlannerView(viewModel: MainViewModel, filterLabel: String? = null) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val todayDate by viewModel.todayDate.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()

    // Get days of selected week
    val weekDays = remember(selectedDate) {
        getDaysOfWeek(selectedDate)
    }

    val isCurrentWeek = remember(weekDays, todayDate) {
        weekDays.contains(todayDate)
    }

    val weekRangeLabel = remember(weekDays) {
        try {
            val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val firstDate = sdfInput.parse(weekDays.first())
            val lastDate = sdfInput.parse(weekDays.last())
            if (firstDate != null && lastDate != null) {
                val sdfOutput = SimpleDateFormat("MMM d", Locale.getDefault())
                val firstStr = sdfOutput.format(firstDate)
                val lastStr = sdfOutput.format(lastDate)
                val gregRange = "$firstStr - $lastStr"

                val pFirst = com.example.core.utils.PersianCalendarHelper.getPersianDateString(weekDays.first())
                val pLast = com.example.core.utils.PersianCalendarHelper.getPersianDateString(weekDays.last())
                val partsFirst = pFirst.split(" ")
                val partsLast = pLast.split(" ")
                val persianRange = if (partsFirst.size >= 3 && partsLast.size >= 3) {
                    val pFirstStr = "${partsFirst[0]} ${partsFirst[1]}"
                    val pLastStr = "${partsLast[0]} ${partsLast[1]}"
                    " / $pFirstStr - $pLastStr"
                } else {
                    ""
                }
                "$gregRange$persianRange".uppercase()
            } else {
                "WEEK VIEW"
            }
        } catch (e: Exception) {
            "WEEK VIEW"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Week Navigator Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.selectDate(getOffsetDateString(selectedDate, -7))
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Previous Week",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            TextButton(
                onClick = { viewModel.selectDate(todayDate) },
                enabled = !isCurrentWeek
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = weekRangeLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrentWeek) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    if (!isCurrentWeek) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Reset to current week",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            IconButton(onClick = {
                viewModel.selectDate(getOffsetDateString(selectedDate, 7))
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "Next Week",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
        ) {
            items(weekDays) { dayDate ->
            val tasksForDay = allTasks.filter { it.date == dayDate && (filterLabel == null || it.label == filterLabel) }
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateObj = sdf.parse(dayDate)
            val gregorianDayLabel = SimpleDateFormat("EEEE", Locale.getDefault()).format(dateObj ?: Date())
            val persianDayLabel = com.example.core.utils.PersianCalendarHelper.getPersianDayOfWeekName(dayDate)
            val dayLabel = if (persianDayLabel.isNotEmpty()) "$gregorianDayLabel / $persianDayLabel" else gregorianDayLabel
            val dateLabelGreg = SimpleDateFormat("MMM d", Locale.getDefault()).format(dateObj ?: Date())
            val persianStr = com.example.core.utils.PersianCalendarHelper.getPersianDateString(dayDate)
            val dateLabel = "$dateLabelGreg ($persianStr)"

            val isSelectedDay = dayDate == selectedDate

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelectedDay) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (isSelectedDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { viewModel.selectDate(dayDate) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dayLabel.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = dateLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (tasksForDay.isEmpty()) {
                        Text(
                            text = "No planned intentions.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    } else {
                        tasksForDay.take(3).forEach { task ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (task.type == "EVENT") {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .border(1.2.dp, MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.CircleShape)
                                        )
                                    } else if (task.type == "NOTE") {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 6.dp, height = 1.5.dp)
                                                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(0.5.dp))
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(4.5.dp)
                                                .background(MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.CircleShape)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = task.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                        if (tasksForDay.size > 3) {
                            Text(
                                text = "+ ${tasksForDay.size - 3} more intentions",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun MonthlyPlannerView(viewModel: MainViewModel, filterLabel: String? = null) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val rawMonthlyTasks by viewModel.monthlyTasks.collectAsState()
    val monthlyTasks = if (filterLabel != null) rawMonthlyTasks.filter { it.label == filterLabel } else rawMonthlyTasks

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Month Header Navigation
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.selectMonth(getOffsetMonthString(selectedMonth, -1))
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            val monthLabel = remember(selectedMonth) {
                try {
                    val date = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(selectedMonth)
                    if (date != null) {
                        val greg = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date).uppercase()
                        val persianDateStr = com.example.core.utils.PersianCalendarHelper.getPersianDateString("$selectedMonth-01")
                        val persianParts = persianDateStr.split(" ")
                        val persianStr = if (persianParts.size >= 3) {
                            "${persianParts[1]} ${persianParts.last()}".uppercase()
                        } else {
                            ""
                        }
                        if (persianStr.isNotEmpty()) {
                            "$greg / $persianStr"
                        } else {
                            greg
                        }
                    } else {
                        selectedMonth
                    }
                } catch (e: Exception) {
                    selectedMonth
                }
            }

            Text(
                text = monthLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            IconButton(onClick = {
                viewModel.selectMonth(getOffsetMonthString(selectedMonth, 1))
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "Next Month",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Month intentions list
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "MONTH INTENTIONS & GOALS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )
                }

                if (monthlyTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No planned items for this month.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
                    ) {
                        items(monthlyTasks) { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.selectDate(task.date) }
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Target Date: ${task.date}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = if (task.status == "COMPLETED") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = task.status,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (task.status == "COMPLETED") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskInteractionDialog(
    task: TaskEntity,
    sessions: List<com.example.core.database.entity.PomodoroSessionEntity>,
    onDismiss: () -> Unit,
    onMarkAsDone: () -> Unit,
    onMarkAsDoneWithDuration: (Int) -> Unit,
    onStartPomodoro: () -> Unit
) {
    var showCustomDurationInput by remember { mutableStateOf(false) }
    var enteredDuration by remember { mutableStateOf("25") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = task.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (task.label.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "#${task.label.uppercase()}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                if (!showCustomDurationInput) {
                    Text(
                        text = "How would you like to proceed with this intention?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Option 1: Mark as Done
                    Button(
                        onClick = {
                            onMarkAsDone()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mark as Completed")
                        }
                    }

                    // Option 2: Complete & Log Duration
                    OutlinedButton(
                        onClick = {
                            showCustomDurationInput = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Update, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Complete & Log Duration")
                        }
                    }

                    // Option 3: Start Pomodoro Focus
                    if (task.type == "TASK") {
                        FilledTonalButton(
                            onClick = {
                                onStartPomodoro()
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Pomodoro Timer")
                            }
                        }
                    }

                    if (sessions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Session Logs (${sessions.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 160.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                sessions.forEach { session ->
                                    val formattedTime = remember(session.timestamp) {
                                        java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(session.timestamp))
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val (iconColor, label) = when (session.status) {
                                            "COMPLETED" -> Pair(MaterialTheme.colorScheme.primary, "Completed")
                                            else -> Pair(MaterialTheme.colorScheme.error, "Interrupted")
                                        }
                                        Icon(
                                            imageVector = if (session.status == "COMPLETED") Icons.Default.Check else Icons.Default.Update,
                                            contentDescription = label,
                                            tint = iconColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${session.durationMinutes} min",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            text = formattedTime,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Logging custom duration view
                    Text(
                        text = "Approximate time spent completing this task (minutes):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = enteredDuration,
                        onValueChange = { enteredDuration = it },
                        label = { Text("Duration in minutes") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showCustomDurationInput = false }) {
                            Text("BACK", color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val mins = enteredDuration.toIntOrNull() ?: 25
                                onMarkAsDoneWithDuration(mins)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("DONE & LOG")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            if (!showCustomDurationInput) {
                TextButton(onClick = onDismiss) {
                    Text("CLOSE", color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

// Utility functions for Date calculations
fun getRelativeDayString(selectedDate: String, todayDate: String): String {
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selected = sdf.parse(selectedDate) ?: return "TODAY"
        val today = sdf.parse(todayDate) ?: return "TODAY"
        
        val diff = selected.time - today.time
        val days = Math.round(diff.toFloat() / (1000 * 60 * 60 * 24)).toInt()
        
        return when {
            days == 0 -> "TODAY"
            days == 1 -> "TOMORROW"
            days == -1 -> "YESTERDAY"
            days > 1 -> "IN $days DAYS"
            days < -1 -> "${-days} DAYS AGO"
            else -> "TODAY"
        }
    } catch (e: Exception) {
        return "TODAY"
    }
}

fun getOffsetDateString(dateStr: String, offsetDays: Int): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = sdf.parse(dateStr) ?: Date()
    val cal = Calendar.getInstance()
    cal.time = date
    cal.add(Calendar.DAY_OF_YEAR, offsetDays)
    return sdf.format(cal.time)
}

fun getOffsetMonthString(monthStr: String, offsetMonths: Int): String {
    val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val date = sdf.parse(monthStr) ?: Date()
    val cal = Calendar.getInstance()
    cal.time = date
    cal.add(Calendar.MONTH, offsetMonths)
    return sdf.format(cal.time)
}

fun getDaysOfWeek(dateStr: String): List<String> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = sdf.parse(dateStr) ?: Date()
    val cal = Calendar.getInstance()
    cal.time = date
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)

    val list = mutableListOf<String>()
    for (i in 0..6) {
        list.add(sdf.format(cal.time))
        cal.add(Calendar.DAY_OF_YEAR, 1)
    }
    return list
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val googleDriveConnected by viewModel.googleDriveConnected.collectAsState()
    val googleDriveEmail by viewModel.googleDriveEmail.collectAsState()
    val dndEnabled by viewModel.dndEnabled.collectAsState()
    val eventReminderVibrate by viewModel.eventReminderVibrate.collectAsState()
    val eventReminderSound by viewModel.eventReminderSound.collectAsState()

    var enteredEmail by remember { mutableStateOf(googleDriveEmail) }
    var enteredDndEnabled by remember { mutableStateOf(dndEnabled) }
    var enteredEventVibrate by remember { mutableStateOf(eventReminderVibrate) }
    var enteredEventSound by remember { mutableStateOf(eventReminderSound) }

    var statusMessage by remember { mutableStateOf("") }
    var isSuccessStatus by remember { mutableStateOf(true) }

    val reviewReminderTime by viewModel.reviewReminderTime.collectAsState()
    val reviewReminderEnabled by viewModel.reviewReminderEnabled.collectAsState()
    var enteredReviewTime by remember { mutableStateOf(reviewReminderTime) }
    var enteredReviewEnabled by remember { mutableStateOf(reviewReminderEnabled) }

    val context = LocalContext.current
    val notificationManager = remember { context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Settings & Cloud Sync", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 2: Google Drive Connection
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "GOOGLE DRIVE BACKUP & SYNC",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (googleDriveConnected) "Connected" else "Disconnected",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (googleDriveConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (googleDriveConnected) {
                                Text(
                                    text = googleDriveEmail,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = googleDriveConnected,
                            onCheckedChange = { isConnected ->
                                if (isConnected) {
                                    viewModel.updateGoogleDriveConnected(true, enteredEmail)
                                } else {
                                    viewModel.updateGoogleDriveConnected(false)
                                }
                            }
                        )
                    }

                    if (googleDriveConnected) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.backupDataToGoogleDrive { success, msg ->
                                        isSuccessStatus = success
                                        statusMessage = msg
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Backup", fontSize = 12.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.restoreDataFromGoogleDrive { success, msg ->
                                        isSuccessStatus = success
                                        statusMessage = msg
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Restore", fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = enteredEmail,
                            onValueChange = { enteredEmail = it },
                            label = { Text("Google Account Email") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Section 4: Pomodoro DND Integration
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "POMODORO SETTINGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Do Not Disturb Mode",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Turn on DND when Pomodoro starts",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = enteredDndEnabled,
                            onCheckedChange = { checked ->
                                if (checked && !notificationManager.isNotificationPolicyAccessGranted) {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                    context.startActivity(intent)
                                } else {
                                    enteredDndEnabled = checked
                                }
                            }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Section 5: Event Reminders
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "EVENT REMINDERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Vibrate on Reminder",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = enteredEventVibrate,
                            onCheckedChange = { enteredEventVibrate = it }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Play Sound on Reminder",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = enteredEventSound,
                            onCheckedChange = { enteredEventSound = it }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Section 6: Day Review Reminder
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "DAY REVIEW REMINDER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Daily Reminder",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = enteredReviewEnabled,
                            onCheckedChange = { enteredReviewEnabled = it }
                        )
                    }
                    if (enteredReviewEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Reminder Time:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            TextButton(onClick = {
                                // Open time picker
                            }) {
                                Text(enteredReviewTime, fontWeight = FontWeight.Bold)
                            }
                        }
                        TextButton(onClick = {
                            val intent = android.content.Intent(context, com.example.core.receiver.ReminderReceiver::class.java).apply {
                                action = "com.example.action.DAY_REVIEW"
                                putExtra("title", "Day Review Reminder")
                                putExtra("message", "Time to review your day!")
                            }
                            context.sendBroadcast(intent)
                        }) {
                            Text("Test Notification", fontSize = 12.sp)
                        }
                    }
                }

                // Status Message display
                if (statusMessage.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSuccessStatus) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = statusMessage,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSuccessStatus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.updateDndEnabled(enteredDndEnabled)
                    viewModel.updateEventReminderVibrate(enteredEventVibrate)
                    viewModel.updateEventReminderSound(enteredEventSound)
                    viewModel.updateReviewReminderTime(enteredReviewTime)
                    viewModel.updateReviewReminderEnabled(enteredReviewEnabled)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("SAVE & CLOSE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = MaterialTheme.colorScheme.primary)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
