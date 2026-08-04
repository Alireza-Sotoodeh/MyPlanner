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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import kotlin.math.roundToInt
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Switch
import androidx.compose.material3.Slider
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.TaskEntity
import com.example.core.database.entity.TodoEntity
import com.example.core.database.entity.IdeaEntity
import com.example.core.database.entity.IdeaGroupEntity
import com.example.core.database.entity.IdeaStageEntity
import com.example.ui.components.ActivePomodoroWidget
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlannerScreen(viewModel: MainViewModel) {
    val todayDate by viewModel.todayDate.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val taskForPomodoroSetup by viewModel.taskForPomodoroSetup.collectAsState()
    val tabTitles = listOf("DAILY", "WEEKLY", "MONTHLY", "TO-DO", "IDEAS")

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

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 0.dp,
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
                1 -> WeeklyPlannerView(viewModel, null) { date ->
                    selectedTab = 0
                    viewModel.selectDate(date)
                }
                 2 -> {
                        var showYearOverview by remember { mutableStateOf(true) }
                        if (showYearOverview) {
                            YearOverviewView(
                                viewModel = viewModel,
                                onMonthSelected = { showYearOverview = false }
                            )
                        } else {
                            MonthlyPlannerView(
                                viewModel = viewModel,
                                onBackToYear = { showYearOverview = true }
                            )
                        }
                    }
                3 -> TodoTab(viewModel)
                4 -> IdeasTab(viewModel)
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
                                    onMoveToTodo = { viewModel.moveTaskToTodo(task, taskSubtasks) },
                                    onTurnIntoIdea = { viewModel.turnNoteIntoIdea(task, taskSubtasks) },
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
                                            },
                                            onMoveToTodo = { viewModel.moveTaskToTodo(task, taskSubtasks) },
                                            onTurnIntoIdea = { viewModel.turnNoteIntoIdea(task, taskSubtasks) }
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

}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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
    onMoveToTodo: () -> Unit = {},
    onTurnIntoIdea: () -> Unit = {},
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
    var showRescheduleDialog by remember { mutableStateOf(false) }
    var isReschedulePersian by remember { mutableStateOf(false) }
    var rescheduleDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var reschedulePYear by remember { mutableIntStateOf(0) }
    var reschedulePMonth by remember { mutableIntStateOf(0) }
    var reschedulePDay by remember { mutableIntStateOf(0) }

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
                    text = { Text("Reschedule...") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    onClick = {
                        isReschedulePersian = false
                        rescheduleDateMillis = System.currentTimeMillis()
                        reschedulePYear = 0; reschedulePMonth = 0; reschedulePDay = 0
                        showRescheduleDialog = true
                        expandedMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Move to To-Do") },
                    leadingIcon = { Icon(Icons.Default.Checklist, contentDescription = null) },
                    onClick = {
                        onMoveToTodo()
                        expandedMenu = false
                    }
                )
                if (task.type == "NOTE") {
                    DropdownMenuItem(
                        text = { Text("Turn into Idea") },
                        leadingIcon = { Icon(Icons.Default.Lightbulb, contentDescription = null) },
                        onClick = {
                            onTurnIntoIdea()
                            expandedMenu = false
                        }
                    )
                }
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
    if (showRescheduleDialog) {
        if (!isReschedulePersian) {
            val dpState = rememberDatePickerState(
                initialSelectedDateMillis = rescheduleDateMillis
            )
            DatePickerDialog(
                onDismissRequest = { showRescheduleDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        dpState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val todayNorm = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                            if (millis >= todayNorm) {
                                onMigrate(sdf.format(Date(millis)))
                                showRescheduleDialog = false
                            }
                        }
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showRescheduleDialog = false }) { Text("Cancel") }
                }
            ) {
                Column {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            val parts = com.example.core.utils.PersianCalendarHelper.getPersianDateParts(
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(rescheduleDateMillis))
                            )
                            reschedulePYear = parts.first; reschedulePMonth = parts.second; reschedulePDay = parts.third
                            isReschedulePersian = true
                        }) { Text("Switch to Persian Calendar") }
                    }
                    DatePicker(state = dpState)
                }
            }
        } else {
            AlertDialog(
                onDismissRequest = { showRescheduleDialog = false },
                title = { Text("Select Persian Date") },
                text = {
                    Column {
                        Row(
                            Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { isReschedulePersian = false }) {
                                Text("Switch to Western Calendar")
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = if (reschedulePYear > 0) reschedulePYear.toString() else "",
                                onValueChange = { reschedulePYear = it.toIntOrNull() ?: 0 },
                                label = { Text("Year") },
                                modifier = Modifier.weight(1.2f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = if (reschedulePMonth > 0) reschedulePMonth.toString() else "",
                                onValueChange = { reschedulePMonth = (it.toIntOrNull() ?: 0).coerceIn(0, 12) },
                                label = { Text("Month") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = if (reschedulePDay > 0) reschedulePDay.toString() else "",
                                onValueChange = { reschedulePDay = (it.toIntOrNull() ?: 0).coerceIn(0, 31) },
                                label = { Text("Day") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (reschedulePYear > 0 && reschedulePMonth in 1..12 && reschedulePDay in 1..31) {
                            val greg = com.example.core.utils.PersianCalendarHelper.getGregorianDateString(
                                reschedulePYear, reschedulePMonth, reschedulePDay
                            )
                            if (greg.isNotBlank()) {
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val selected = sdf.parse(greg)?.time ?: 0L
                                val todayNorm = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                                if (selected >= todayNorm) {
                                    onMigrate(greg)
                                    showRescheduleDialog = false
                                }
                            }
                        }
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showRescheduleDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
}

@Composable
fun WeeklyPlannerView(viewModel: MainViewModel, filterLabel: String? = null, onNavigateToDaily: (String) -> Unit = {}) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val todayDate by viewModel.todayDate.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()

    // Local week anchor — decouples week scrolling from selectedDate.
    // Initializes from selectedDate each time WeeklyPlannerView enters composition.
    var weekAnchorDate by remember { mutableStateOf(selectedDate) }

    val weekDays = remember(weekAnchorDate) {
        getDaysOfWeek(weekAnchorDate)
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
                weekAnchorDate = getOffsetDateString(weekAnchorDate, -7)
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Previous Week",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            TextButton(
                onClick = { weekAnchorDate = todayDate },
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
                weekAnchorDate = getOffsetDateString(weekAnchorDate, 7)
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
            val topLevelTasks = allTasks.filter { it.date == dayDate && it.parentTaskId == null && (filterLabel == null || it.label == filterLabel) }
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateObj = sdf.parse(dayDate)
            val gregorianDayLabel = SimpleDateFormat("EEEE", Locale.getDefault()).format(dateObj ?: Date())
            val persianDayLabel = com.example.core.utils.PersianCalendarHelper.getPersianDayOfWeekName(dayDate)
            val dayLabel = if (persianDayLabel.isNotEmpty()) "$gregorianDayLabel / $persianDayLabel" else gregorianDayLabel
            val dateLabelGreg = SimpleDateFormat("MMM d", Locale.getDefault()).format(dateObj ?: Date())
            val persianStr = com.example.core.utils.PersianCalendarHelper.getPersianDateString(dayDate)
            val dateLabel = "$dateLabelGreg ($persianStr)"

            val isSelectedDay = dayDate == selectedDate
            val isPast = try {
                val dayMillis = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dayDate)?.time ?: 0L
                dayMillis < SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(todayDate)?.time ?: 0L
            } catch (e: Exception) { false }
            val isToday = dayDate == todayDate

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isSelectedDay -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        isToday -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (isSelectedDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onNavigateToDaily(dayDate) }
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .alpha(if (isPast) 0.5f else 1f)
                ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dayLabel.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (isPast) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = dateLabel,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                    isPast -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                    else -> MaterialTheme.colorScheme.onBackground
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (topLevelTasks.isEmpty()) {
                            Text(
                                text = "No planned intentions.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        } else {
                            topLevelTasks.forEach { task ->
                                val subtaskCount = allTasks.count { it.parentTaskId == task.id }
                                val isCompletedTask = task.status == "COMPLETED"
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (task.type == "EVENT") {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .border(1.2.dp, MaterialTheme.colorScheme.primary, shape = CircleShape)
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
                                                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = task.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isCompletedTask) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textDecoration = if (isCompletedTask) TextDecoration.LineThrough else TextDecoration.None
                                        )
                                        if (subtaskCount > 0) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "+ $subtaskCount ${if (subtaskCount == 1) "subtask" else "subtasks"}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    if (isToday) {
                                        IconButton(
                                            onClick = { onNavigateToDaily(task.date) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowForwardIos,
                                                contentDescription = "View details",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
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
    }
}

@Composable
fun YearOverviewView(
    viewModel: MainViewModel,
    onMonthSelected: () -> Unit
) {
    val selectedYear by viewModel.selectedYear.collectAsState()
    val yearTasks by viewModel.yearTasks.collectAsState()

    val topLevelTasks = remember(yearTasks) {
        yearTasks.filter { it.parentTaskId == null }
    }

    val tasksByMonth = remember(topLevelTasks) {
        topLevelTasks.groupBy { task ->
            if (task.date.length >= 7) task.date.substring(0, 7) else task.date
        }.mapValues { it.value.size }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Year Header Navigation
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val cal = Calendar.getInstance()
                val sdf = SimpleDateFormat("yyyy", Locale.getDefault())
                val date = sdf.parse(selectedYear) ?: Date()
                cal.time = date
                cal.add(Calendar.YEAR, -1)
                viewModel.selectYear(sdf.format(cal.time))
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Previous Year",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = selectedYear,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            IconButton(onClick = {
                val cal = Calendar.getInstance()
                val sdf = SimpleDateFormat("yyyy", Locale.getDefault())
                val date = sdf.parse(selectedYear) ?: Date()
                cal.time = date
                cal.add(Calendar.YEAR, 1)
                viewModel.selectYear(sdf.format(cal.time))
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "Next Year",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Month grid
        if (topLevelTasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tasks for this year.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                val monthNames = listOf(
                    "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                    "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
                )

                items(12) { index ->
                    val monthStr = String.format("%s-%02d", selectedYear, index + 1)
                    val count = tasksByMonth[monthStr] ?: 0

                    Card(
                        onClick = {
                            viewModel.selectMonth(monthStr)
                            viewModel.selectYear(selectedYear)
                            onMonthSelected()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (count > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = monthNames[index],
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = count.toString(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (count > 0) MaterialTheme.colorScheme.onSurface
                                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
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
fun MonthlyPlannerView(viewModel: MainViewModel, filterLabel: String? = null, onBackToYear: () -> Unit = {}) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val rawMonthlyTasks by viewModel.monthlyTasks.collectAsState()
    val monthlyTasks = if (filterLabel != null) rawMonthlyTasks.filter { it.label == filterLabel } else rawMonthlyTasks

    val mainTasks = monthlyTasks.filter { it.parentTaskId == null }
    var showCompleted by remember { mutableStateOf(false) }

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
                letterSpacing = 1.sp,
                modifier = Modifier.clickable { onBackToYear() }
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

                if (mainTasks.isEmpty()) {
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
                    val activeTasks = mainTasks.filter { it.status != "COMPLETED" }
                    val completedTasks = mainTasks.filter { it.status == "COMPLETED" }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
                    ) {
                        items(activeTasks) { task ->
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
                                items(completedTasks) { task ->
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
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
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
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            Text(
                                                text = "COMPLETED",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                fontSize = 9.sp,
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

    val postNotificationsLauncher = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                android.widget.Toast.makeText(context, "Notifications disabled — enable in Settings", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    } else null

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
                            onCheckedChange = { enabled ->
                                enteredReviewEnabled = enabled
                                if (enabled) {
                                    postNotificationsLauncher?.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    if (android.os.Build.VERSION.SDK_INT in android.os.Build.VERSION_CODES.S until android.os.Build.VERSION_CODES.TIRAMISU) {
                                        val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
                                        if (!alarmManager.canScheduleExactAlarms()) {
                                            context.startActivity(android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                                        }
                                    }
                                }
                            }
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

private enum class TodoTabFilter { ALL, PENDING, DONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoTab(viewModel: MainViewModel) {
    val allTodos by viewModel.allTodos.collectAsState()

    var filter by remember { mutableStateOf(TodoTabFilter.ALL) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTodo by remember { mutableStateOf<TodoEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<TodoEntity?>(null) }
    var todoForLinking by remember { mutableStateOf<TodoEntity?>(null) }
    var showUnlinkConfirm by remember { mutableStateOf<TodoEntity?>(null) }

    val displayTodos = when (filter) {
        TodoTabFilter.ALL -> allTodos
        TodoTabFilter.PENDING -> allTodos.filter { it.status == "PENDING" }
        TodoTabFilter.DONE -> allTodos.filter { it.status == "DONE" }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TO-DO LIST",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp
                )
                Text(
                    "${displayTodos.size} items",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TodoTabFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { filter = f },
                        label = { Text(f.name, fontSize = 12.sp) }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (displayTodos.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Checklist,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No to-dos yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                            Text(
                                "Tap + to create your first to-do",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(displayTodos, key = { it.id }) { todo ->
                            TodoItem(
                                todo = todo,
                                viewModel = viewModel,
                                onEdit = { editingTodo = it },
                                onDelete = { showDeleteConfirm = it },
                                onLink = { todoForLinking = it },
                                onUnlink = { showUnlinkConfirm = it }
                            )
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background,
                    shape = CircleShape,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add To-Do")
                }
            }
        }
    }

    if (showAddDialog) {
        AddTodoDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, description, priority ->
                viewModel.addTodo(title, description, priority)
                showAddDialog = false
            }
        )
    }
    editingTodo?.let { todo ->
        EditTodoDialog(
            todo = todo,
            onDismiss = { editingTodo = null },
            onConfirm = { title, description, priority ->
                viewModel.updateTodo(todo.copy(title = title, description = description, priority = priority))
                editingTodo = null
            }
        )
    }
    showDeleteConfirm?.let { todo ->
        if (todo.linkedTaskId != null) {
            LinkedDeleteConfirmDialog(
                onDismiss = { showDeleteConfirm = null },
                onDeleteBoth = { viewModel.deleteTodo(todo); showDeleteConfirm = null },
                onKeepTodo = { viewModel.unlinkTodoFromTask(todo); viewModel.deleteTodo(todo); showDeleteConfirm = null }
            )
        } else {
            DeleteConfirmDialog(
                title = "Delete To-Do",
                message = "Delete \"${todo.title}\"?",
                onDismiss = { showDeleteConfirm = null },
                onConfirm = { viewModel.deleteTodo(todo); showDeleteConfirm = null }
            )
        }
    }
    todoForLinking?.let { todo ->
        LinkToPlannerDialog(
            todo = todo,
            viewModel = viewModel,
            onDismiss = { todoForLinking = null }
        )
    }
    showUnlinkConfirm?.let { todo ->
        DeleteConfirmDialog(
            title = "Unlink To-Do",
            message = "Remove link to planner task? The to-do will be kept.",
            onDismiss = { showUnlinkConfirm = null },
            onConfirm = { viewModel.unlinkTodoFromTask(todo); showUnlinkConfirm = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TodoItem(
    todo: TodoEntity,
    viewModel: MainViewModel,
    onEdit: (TodoEntity) -> Unit,
    onDelete: (TodoEntity) -> Unit,
    onLink: (TodoEntity) -> Unit,
    onUnlink: (TodoEntity) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isDone = todo.status == "DONE"

    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(
            onClick = { viewModel.toggleTodoCompletion(todo) },
            onLongClick = { showMenu = true }
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isDone,
                onCheckedChange = { viewModel.toggleTodoCompletion(todo) },
                modifier = Modifier.size(24.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            )
            Spacer(Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    todo.title,
                    fontSize = 14.sp,
                    fontWeight = if (isDone) FontWeight.Normal else FontWeight.Medium,
                    color = if (isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PriorityBadge(todo.priority)
                    if (todo.linkedTaskId != null) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Linked to planner",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            if (todo.linkedTaskId != null) {
                IconButton(onClick = { onUnlink(todo) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.LinkOff, contentDescription = "Unlink", modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            } else {
                IconButton(onClick = { onLink(todo) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Link to planner", modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit(todo) })
                    if (todo.linkedTaskId != null) {
                        DropdownMenuItem(text = { Text("Unlink") }, onClick = { showMenu = false; onUnlink(todo) })
                    } else {
                        DropdownMenuItem(text = { Text("Schedule") }, onClick = { showMenu = false; onLink(todo) })
                    }
                    DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDelete(todo) })
                }
            }
        }
    }
}

@Composable
private fun PriorityBadge(priority: String) {
    val color = when (priority) {
        "High" -> Color(0xFFB3261E)
        "Low" -> Color(0xFF00E676)
        else -> Color(0xFFFF7043)
    }
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            priority,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun AddTodoDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("New To-Do", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    maxLines = 3
                )
                Text("Priority", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Low", "Medium", "High").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p, fontSize = 12.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank()) onConfirm(title.trim(), description.trim(), priority) },
                enabled = title.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EditTodoDialog(
    todo: TodoEntity,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(todo.title) }
    var description by remember { mutableStateOf(todo.description) }
    var priority by remember { mutableStateOf(todo.priority) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Edit To-Do", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    maxLines = 3
                )
                Text("Priority", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Low", "Medium", "High").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p, fontSize = 12.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank()) onConfirm(title.trim(), description.trim(), priority) },
                enabled = title.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LinkToPlannerDialog(
    todo: TodoEntity,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val currentDate by viewModel.selectedDate.collectAsState()
    var date by remember { mutableStateOf(currentDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(date)?.time
                    ?: System.currentTimeMillis()
            } catch (_: Exception) { System.currentTimeMillis() }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
                        date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.time)
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

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Schedule as Task", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("\"${todo.title}\"", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Date:", fontSize = 14.sp, modifier = Modifier.width(60.dp))
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        modifier = Modifier.weight(1f).height(48.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Pick date", modifier = Modifier.size(18.dp))
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.linkTodoToTask(todo, date)
                onDismiss()
            }) { Text("Schedule") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun LinkedDeleteConfirmDialog(
    onDismiss: () -> Unit,
    onDeleteBoth: () -> Unit,
    onKeepTodo: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Linked Task", fontWeight = FontWeight.Bold) },
        text = { Text("This to-do is linked to a planner task.", fontSize = 14.sp) },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onKeepTodo) { Text("Keep (unlink)") }
                TextButton(onClick = onDeleteBoth) { Text("Delete Both", color = MaterialTheme.colorScheme.error) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun DeleteConfirmDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message, fontSize = 14.sp) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Delete", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun IdeasTab(viewModel: MainViewModel) {
    val groups by viewModel.ideaGroups.collectAsState()
    val ideas by viewModel.allIdeas.collectAsState()

    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showCreateGroupFromIdeaDialog by remember { mutableStateOf(false) }
    var showCreateIdeaDialog by remember { mutableStateOf(false) }
    var editingIdea by remember { mutableStateOf<IdeaEntity?>(null) }
    var showDeleteIdeaConfirm by remember { mutableStateOf<IdeaEntity?>(null) }
    var showDeleteGroupConfirm by remember { mutableStateOf<IdeaGroupEntity?>(null) }
    var editingGroup by remember { mutableStateOf<IdeaGroupEntity?>(null) }
    var ideaForPlanner by remember { mutableStateOf<IdeaEntity?>(null) }

    val filteredIdeas = if (selectedGroupId == null) ideas
    else ideas.filter { it.groupId == selectedGroupId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "IDEAS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "${filteredIdeas.size} ideas",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { showCreateGroupDialog = true }) {
                        Text("+ Group", fontSize = 11.sp)
                    }
                }
            }

            // Group filter chips
            GroupChipRow(
                groups = groups,
                selectedGroupId = selectedGroupId,
                onGroupSelected = { selectedGroupId = it },
                onEditGroup = { editingGroup = it },
                onDeleteGroup = { showDeleteGroupConfirm = it }
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (filteredIdeas.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No ideas yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                            Text(
                                "Tap + to create your first idea",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredIdeas, key = { it.id }) { idea ->
                            IdeaCard(
                                idea = idea,
                                viewModel = viewModel,
                                onEdit = { editingIdea = it },
                                onDelete = { showDeleteIdeaConfirm = it },
                                onAddToPlanner = { ideaForPlanner = it }
                            )
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { showCreateIdeaDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background,
                    shape = CircleShape,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Idea")
                }
            }
        }
    }

    if (showCreateGroupDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateGroupDialog = false },
            onConfirm = { name, color -> viewModel.addGroup(name, color); showCreateGroupDialog = false }
        )
    }
    if (showCreateGroupFromIdeaDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateGroupFromIdeaDialog = false },
            onConfirm = { name, color -> viewModel.addGroup(name, color); showCreateGroupFromIdeaDialog = false }
        )
    }
    editingGroup?.let { group ->
        CreateGroupDialog(
            initialName = group.name,
            initialColor = group.color,
            onDismiss = { editingGroup = null },
            onConfirm = { name, color -> viewModel.updateGroup(group.copy(name = name, color = color)); editingGroup = null }
        )
    }
    if (showCreateIdeaDialog) {
        CreateIdeaDialog(
            groups = groups,
            onDismiss = { showCreateIdeaDialog = false },
            onConfirm = { groupId, title, description -> viewModel.addIdea(groupId, title, description); showCreateIdeaDialog = false },
            onShowCreateGroup = { showCreateGroupFromIdeaDialog = true }
        )
    }
    editingIdea?.let { idea ->
        CreateIdeaDialog(
            groups = groups,
            initialTitle = idea.title,
            initialDescription = idea.description,
            initialGroupId = idea.groupId,
            onDismiss = { editingIdea = null },
            onConfirm = { groupId, title, description -> viewModel.updateIdea(idea.copy(groupId = groupId, title = title, description = description)); editingIdea = null },
            onShowCreateGroup = { showCreateGroupFromIdeaDialog = true }
        )
    }
    showDeleteIdeaConfirm?.let { idea ->
        DeleteConfirmDialog(
            title = "Delete Idea",
            message = "Delete \"${idea.title}\" and all its stages?",
            onDismiss = { showDeleteIdeaConfirm = null },
            onConfirm = { viewModel.deleteIdea(idea); showDeleteIdeaConfirm = null }
        )
    }
    showDeleteGroupConfirm?.let { group ->
        DeleteConfirmDialog(
            title = "Delete Group",
            message = "Delete \"${group.name}\" and all ideas inside it?",
            onDismiss = { showDeleteGroupConfirm = null },
            onConfirm = { viewModel.deleteGroup(group); showDeleteGroupConfirm = null }
        )
    }
    ideaForPlanner?.let { idea ->
        AddToPlannerDialog(
            idea = idea,
            viewModel = viewModel,
            onDismiss = { ideaForPlanner = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupChipRow(
    groups: List<IdeaGroupEntity>,
    selectedGroupId: Long?,
    onGroupSelected: (Long?) -> Unit,
    onEditGroup: (IdeaGroupEntity) -> Unit,
    onDeleteGroup: (IdeaGroupEntity) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedGroupId == null,
                onClick = { onGroupSelected(null) },
                label = { Text("All", fontSize = 12.sp) }
            )
        }
        items(groups, key = { it.id }) { group ->
            FilterChip(
                selected = selectedGroupId == group.id,
                onClick = { onGroupSelected(group.id) },
                label = { Text(group.name, fontSize = 12.sp) },
                trailingIcon = {
                    Box(
                        modifier = Modifier.size(8.dp).background(Color(group.color), CircleShape)
                    )
                },
                modifier = Modifier.combinedClickable(
                    onClick = { onGroupSelected(group.id) },
                    onLongClick = { onEditGroup(group) }
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IdeaCard(
    idea: IdeaEntity,
    viewModel: MainViewModel,
    onEdit: (IdeaEntity) -> Unit,
    onDelete: (IdeaEntity) -> Unit,
    onAddToPlanner: (IdeaEntity) -> Unit
) {
    val stages by viewModel.stagesForIdea(idea.id).collectAsState(initial = emptyList())
    var showAddStage by remember { mutableStateOf(false) }
    var newStageTitle by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showIdeaMenu by remember { mutableStateOf(false) }
    var addStageIdeaId by remember { mutableStateOf<Long?>(null) }

    val ideaGroup = remember(idea.groupId) {
        viewModel.ideaGroups.value.find { it.id == idea.groupId }
    }
    val groupColor = ideaGroup?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Lightbulb, contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = groupColor
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        idea.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (idea.description.isNotBlank()) {
                        Text(
                            idea.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Box {
                    IconButton(onClick = { showIdeaMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = showIdeaMenu, onDismissRequest = { showIdeaMenu = false }) {
                        DropdownMenuItem(text = { Text("Edit") }, onClick = { showIdeaMenu = false; onEdit(idea) })
                        DropdownMenuItem(text = { Text("Add to Planner") }, onClick = { showIdeaMenu = false; onAddToPlanner(idea) })
                        DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = { showIdeaMenu = false; onDelete(idea) })
                    }
                }
            }

            if (stages.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))
                stages.forEach { stage ->
                    StageRow(
                        stage = stage,
                        stages = stages,
                        viewModel = viewModel,
                        onDelete = { viewModel.deleteStage(it) }
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }

            if (showAddStage && addStageIdeaId == idea.id) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    OutlinedTextField(
                        value = newStageTitle,
                        onValueChange = { newStageTitle = it },
                        placeholder = { Text("Stage title", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                    IconButton(onClick = {
                        if (newStageTitle.isNotBlank()) {
                            viewModel.addStage(idea.id, newStageTitle.trim())
                            newStageTitle = ""
                            addStageIdeaId = null
                            showAddStage = false
                        }
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = {
                        newStageTitle = ""
                        addStageIdeaId = null
                        showAddStage = false
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(18.dp))
                    }
                }
            } else {
                TextButton(
                    onClick = { showAddStage = true; addStageIdeaId = idea.id },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("+ Add Stage", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun StageRow(
    stage: IdeaStageEntity,
    stages: List<IdeaStageEntity>,
    viewModel: MainViewModel,
    onDelete: (IdeaStageEntity) -> Unit
) {
    val index = stages.indexOf(stage)
    val previousCompleted = index == 0 || stages.take(index).all { it.isCompleted }
    val canToggle = previousCompleted

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = stage.isCompleted,
            onCheckedChange = { checked ->
                viewModel.updateStage(stage.copy(isCompleted = checked))
                if (!checked) {
                    stages.drop(index + 1).forEach {
                        viewModel.updateStage(it.copy(isCompleted = false))
                    }
                }
            },
            enabled = canToggle || stage.isCompleted,
            modifier = Modifier.size(20.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        )
        Spacer(Modifier.width(6.dp))
        Text(
            stage.title,
            fontSize = 13.sp,
            color = if (stage.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.onSurface,
            textDecoration = if (stage.isCompleted) TextDecoration.LineThrough else TextDecoration.None
        )
        Spacer(Modifier.weight(1f))
        if (canToggle) {
            IconButton(onClick = { onDelete(stage) }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Delete stage", modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
private fun CreateGroupDialog(
    initialName: String? = null,
    initialColor: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Long) -> Unit
) {
    var name by remember { mutableStateOf(initialName ?: "") }
    var selectedColor by remember { mutableStateOf(initialColor ?: 0xFF4CAF50) }

    val presetColors = listOf(0xFF4CAF50, 0xFF2196F3, 0xFFFF9800, 0xFFE91E63, 0xFF9C27B0, 0xFF00BCD4, 0xFFFF5722, 0xFF607D8B)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (initialName != null) "Edit Group" else "New Group", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Color", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presetColors.forEach { c ->
                        val isSelected = selectedColor == c
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .border(if (isSelected) 2.dp else 0.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    CircleShape)
                                .clickable { selectedColor = c }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), selectedColor) },
                enabled = name.isNotBlank()
            ) { Text(if (initialName != null) "Save" else "Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateIdeaDialog(
    groups: List<IdeaGroupEntity>,
    initialTitle: String = "",
    initialDescription: String = "",
    initialGroupId: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, String) -> Unit,
    onShowCreateGroup: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var selectedGroupId by remember { mutableStateOf(initialGroupId) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (initialTitle.isNotEmpty()) "Edit Idea" else "New Idea", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    maxLines = 3
                )
                Text("Group", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box {
                    Surface(
                        onClick = { expanded = true },
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                groups.find { it.id == selectedGroupId }?.name ?: "None",
                                fontSize = 13.sp,
                                color = if (selectedGroupId != null) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Create New Group...", color = MaterialTheme.colorScheme.primary) },
                            onClick = { expanded = false; onShowCreateGroup() }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank()) onConfirm(selectedGroupId ?: 0L, title.trim(), description.trim()) },
                enabled = title.isNotBlank()
            ) { Text(if (initialTitle.isNotEmpty()) "Save" else "Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToPlannerDialog(
    idea: IdeaEntity,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val currentDate by viewModel.selectedDate.collectAsState()
    val stages by viewModel.stagesForIdea(idea.id).collectAsState(initial = emptyList())
    var date by remember { mutableStateOf(currentDate) }
    var selectedType by remember { mutableStateOf("TASK") }
    var selectedMode by remember { mutableStateOf("entire") }
    var selectedStageId by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(date)?.time
                    ?: System.currentTimeMillis()
            } catch (_: Exception) { System.currentTimeMillis() }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
                        date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.time)
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

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Add to Planner", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("\"${idea.title}\"", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Date:", fontSize = 14.sp, modifier = Modifier.width(60.dp))
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        modifier = Modifier.weight(1f).height(48.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Pick date", modifier = Modifier.size(18.dp))
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                }
                Text("Type", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("TASK", "EVENT", "NOTE").forEach { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = { selectedType = t },
                            label = { Text(t, fontSize = 12.sp) }
                        )
                    }
                }
                Text("Stage", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedMode == "entire",
                        onClick = { selectedMode = "entire"; selectedStageId = null },
                        label = { Text("Entire Idea", fontSize = 12.sp) }
                    )
                    if (stages.isNotEmpty()) {
                        FilterChip(
                            selected = selectedMode == "single",
                            onClick = { selectedMode = "single" },
                            label = { Text("Single Stage", fontSize = 12.sp) }
                        )
                    }
                }
                if (selectedMode == "single" && stages.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        stages.filter { it.title.isNotBlank() }.forEach { stage ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedStageId == stage.id,
                                    onClick = { selectedStageId = stage.id }
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(stage.title, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (selectedMode == "single" && selectedStageId != null) {
                    val stage = stages.find { it.id == selectedStageId }
                    if (stage != null) viewModel.addStageToPlanner(stage, date, selectedType)
                } else {
                    viewModel.addIdeaToPlanner(idea, date, selectedType)
                }
                onDismiss()
            }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
