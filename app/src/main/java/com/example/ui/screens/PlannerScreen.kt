package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material.icons.filled.Task
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
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.platform.LocalDensity
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
import com.example.ui.components.ActiveTimerWidget
import com.example.ui.components.HeaderActions
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.core.utils.PersianCalendarHelper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlannerScreen(viewModel: MainViewModel) {
    val todayDate by viewModel.todayDate.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showTaskManager by remember { mutableStateOf(false) }
    var taskManagerInitialType by remember { mutableStateOf("TASK") }
    val taskForPomodoroSetup by viewModel.taskForPomodoroSetup.collectAsState()
    val tabTitles = listOf("DAILY", "WEEKLY", "MONTHLY", "TO-DO", "IDEAS")

    LaunchedEffect(selectedTab) {
        viewModel.selectDate(viewModel.todayDate.value)
    }

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
                Box(
                    modifier = Modifier
                        .clickable { selectedTab = index }
                        .padding(horizontal = 2.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        letterSpacing = 1.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Active widgets loaded inside planner screen for high utility context
        ActiveTimerWidget(viewModel)

        var selectedFilterLabels by remember { mutableStateOf<Set<String>>(emptySet()) }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> DailyPlannerView(viewModel, selectedFilterLabels) { selectedFilterLabels = it }
                1 -> WeeklyPlannerView(viewModel, null) { date ->
                    selectedTab = 0
                    viewModel.selectDate(date)
                }
                 2 -> {
                        var showYearOverview by remember { mutableStateOf(true) }
                        val usePersianCalendar by viewModel.usePersianCalendar.collectAsState()
                        if (showYearOverview) {
                            YearOverviewView(
                                viewModel = viewModel,
                                usePersianCalendar = usePersianCalendar,
                                onMonthSelected = { showYearOverview = false }
                            )
                        } else {
                            MonthlyPlannerView(
                                viewModel = viewModel,
                                usePersianCalendar = usePersianCalendar,
                                onBackToYear = { showYearOverview = true }
                            )
                        }
                    }
                 3 -> TodoTab(viewModel)
                 4 -> IdeasTab(viewModel)
            }

            if (selectedTab == 0 || selectedTab == 3 || selectedTab == 4) {
                FloatingActionButton(
                    onClick = {
                        taskManagerInitialType = when (selectedTab) {
                            3 -> "TODO"
                            4 -> "IDEA"
                            else -> "TASK"
                        }
                        showTaskManager = true
                    },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create"
                    )
                }
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
        com.example.ui.components.FastPomodoroSetupDialog(
            task = task,
            viewModel = viewModel,
            onDismiss = { viewModel.setTaskForPomodoroSetup(null) }
        )
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

    if (showTaskManager) {
        com.example.ui.components.TaskManagerDialog(
            viewModel = viewModel,
            initialDate = selectedDate,
            initialType = taskManagerInitialType,
            onDismiss = { showTaskManager = false }
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
            verticalAlignment = Alignment.Top
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
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            HeaderActions(
                onHomeClick = onHomeClick,
                onSettingsClick = onSettingsClick,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
}

@Composable
fun DailyPlannerView(viewModel: MainViewModel, filterLabels: Set<String> = emptySet(), onLabelsSelected: (Set<String>) -> Unit = {}) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedDate.collectAsState()
    val todayDate by viewModel.todayDate.collectAsState()
    val rawTasks by viewModel.dailyTasks.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val allTodos by viewModel.allTodos.collectAsState()
    val tasks = if (filterLabels.isNotEmpty()) rawTasks.filter { it.label in filterLabels } else rawTasks

    val labelInfos = rawTasks
        .filter { it.status != "COMPLETED" && (it.type == "TASK" || it.type == "EVENT" || it.type == "NOTE") }
        .groupBy { it.label }
        .filterKeys { it.isNotBlank() }
        .map { (label, items) ->
            LabelInfo(
                name = label,
                color = items.firstOrNull { it.labelColor != null }?.labelColor,
                count = items.size
            )
        }
        .sortedBy { it.name }

    LaunchedEffect(filterLabels, labelInfos) {
        val validNames = labelInfos.map { it.name }.toSet()
        if (filterLabels.isNotEmpty() && filterLabels.none { it in validNames }) {
            onLabelsSelected(emptySet())
        }
    }

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
        AnimatedVisibility(
            visible = showHeaderExtras && labelInfos.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterChip(
                        selected = filterLabels.isEmpty(),
                        onClick = { onLabelsSelected(emptySet()) },
                        label = { Text("All", fontSize = 12.sp) }
                    )
                }
                items(labelInfos) { info ->
                    FilterChip(
                        selected = info.name in filterLabels,
                        onClick = {
                            val newSet = if (info.name in filterLabels) {
                                filterLabels - info.name
                            } else {
                                filterLabels + info.name
                            }
                            onLabelsSelected(newSet)
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (info.color != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(info.color))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text("${info.name.uppercase()} (${info.count})", fontSize = 12.sp)
                            }
                        }
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
                    val lazyListState = rememberLazyListState()

                    LaunchedEffect(filterLabels) {
                        lazyListState.animateScrollToItem(0)
                    }

                    LazyColumn(state = lazyListState,
                        modifier = Modifier
                            .weight(1f)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
                    ) {
                        val mainTasks = tasks.filter { it.parentTaskId == null }
                        val activeTasks = mainTasks.filter { it.status != "COMPLETED" }
                        val completedTasks = mainTasks.filter { it.status == "COMPLETED" }
                        val sortedActiveTasks = activeTasks.sortedWith(
                            compareBy(
                                { if (it.postponed) 0 else 1 },
                                { it.priority },
                                { it.id }
                            )
                        )

                        val displayedActiveTasks = draggedTasks ?: sortedActiveTasks

                        items(displayedActiveTasks, key = { it.id }) { task ->
                            var showInteractDialog by remember { mutableStateOf(false) }
                            val taskSubtasks = allTasks.filter { it.parentTaskId == task.id }.sortedWith(compareBy({ it.priority }, { it.id }))

                            Box(modifier = Modifier.animateItem()) {
                                BulletTaskItem(
                                    modifier = Modifier.onGloballyPositioned { itemHeights[task.id] = it.size.height },
                                    task = task,
                                    subtasks = taskSubtasks,
                                    linkedTodoTitle = allTodos.find { it.id == task.linkedTodoId }?.title,
                                    isExpanded = expandAllItems,
                                    isSubtasksExpanded = expandedSubtasksMap[task.id] ?: expandAllSubtasks,
                                    onCheckToggle = { viewModel.toggleTaskCompletion(task, taskSubtasks) },
                                    onMigrate = { targetDate -> viewModel.migrateTask(task, targetDate) },
                                    onDelete = { viewModel.deleteTaskWithUndo(task) },
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
                                    onDeleteSubtask = { subtask -> viewModel.deleteTaskWithUndo(subtask) },
                                    onReorderSubtask = { subtask, subtaskList, delta -> viewModel.reorderTask(subtask, subtaskList, delta, false) },
                                    isDragging = (draggingTaskId == task.id),
                                    dragOffsetX = if (draggingTaskId == task.id) dragOffsetX else 0f,
                                    dragOffsetY = if (draggingTaskId == task.id) dragOffsetY else 0f,
                                    onDragStart = {
                                        draggingTaskId = task.id
                                        dragOffsetX = 0f
                                        dragOffsetY = 0f
                                        draggedTasks = sortedActiveTasks.toList()
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
                                            val originalIndex = sortedActiveTasks.indexOfFirst { it.id == task.id }
                                            if (currentList != null && originalIndex != -1) {
                                                val finalIndex = currentList.indexOfFirst { it.id == task.id }
                                                val isSubtask = dragOffsetX > with(density) { 50.dp.toPx() }
                                                val deltaIndex = finalIndex - originalIndex
                                                if (deltaIndex != 0 || isSubtask) {
                                                    viewModel.reorderTask(task, sortedActiveTasks, deltaIndex, isSubtask)
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
                                TaskInteractionDialog(
                                    task = task,
                                    onDismiss = { showInteractDialog = false },
                                    onMarkAsDone = { viewModel.toggleTaskCompletion(task) },
                                    onMarkAsDoneWithDuration = { duration ->
                                        viewModel.completeTaskWithManualDuration(task, duration)
                                    },
                                    onStartPomodoro = {
                                        viewModel.setPreSelectedTaskForTimer(task.id)
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
                                            linkedTodoTitle = allTodos.find { it.id == task.linkedTodoId }?.title,
                                            isExpanded = expandAllItems,
                                            isSubtasksExpanded = expandedSubtasksMap[task.id] ?: expandAllSubtasks,
                                            onCheckToggle = { viewModel.toggleTaskCompletion(task, taskSubtasks) },
                                            onMigrate = { targetDate -> viewModel.migrateTask(task, targetDate) },
                                            onDelete = { viewModel.deleteTaskWithUndo(task) },
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
                                            onDeleteSubtask = { subtask -> viewModel.deleteTaskWithUndo(subtask) },
                                            onReorderSubtask = { subtask, subtaskList, delta -> viewModel.reorderTask(subtask, subtaskList, delta, false) },
                                            onToggleSubtasksExpanded = { expanded ->
                                                expandedSubtasksMap[task.id] = expanded
                                            },
                                            onMoveToTodo = { viewModel.moveTaskToTodo(task, taskSubtasks) },
                                            onTurnIntoIdea = { viewModel.turnNoteIntoIdea(task, taskSubtasks) }
                                        )
                                    }
                                    
                                    if (showInteractDialog) {
                                        TaskInteractionDialog(
                                            task = task,
                                            onDismiss = { showInteractDialog = false },
                                            onMarkAsDone = { viewModel.toggleTaskCompletion(task) },
                                            onMarkAsDoneWithDuration = { duration ->
                                                viewModel.completeTaskWithManualDuration(task, duration)
                                            },
                                            onStartPomodoro = {
                                                viewModel.setPreSelectedTaskForTimer(task.id)
                                            }
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

    if (showAddTaskDialog && taskToEdit != null) {
        com.example.ui.components.TaskManagerDialog(
            viewModel = viewModel,
            initialDate = taskToEdit!!.date,
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
    linkedTodoTitle: String? = null,
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
        } else if (task.postponed) {
            Color(0xFFE53935).copy(alpha = 0.06f)
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
                            "TODO" -> {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .border(2.dp, MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(2.dp))
                                )
                            }
                            "IDEA" -> {
                                Box(
                                    modifier = Modifier
                                        .size(width = 10.dp, height = 3.dp)
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
                if (task.postponed) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFE53935).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "POSTPONED",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
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
                if (task.type == "TASK") {
                    DropdownMenuItem(
                        text = { Text("Move to To-Do") },
                        leadingIcon = { Icon(Icons.Default.Checklist, contentDescription = null) },
                        onClick = {
                            onMoveToTodo()
                            expandedMenu = false
                        }
                    )
                }
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
            val persianParts = persianStr.split(" ")
            val persianDateLabel = if (persianParts.size >= 3) "${persianParts[0]} ${persianParts[1]}" else persianStr
            val dateLabel = "$dateLabelGreg ($persianDateLabel)"

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
                                        } else if (task.type == "TODO") {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .border(1.5.dp, MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(1.dp))
                                            )
                                        } else if (task.type == "IDEA") {
                                            Box(
                                                modifier = Modifier
                                                    .size(width = 7.dp, height = 2.dp)
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
    usePersianCalendar: Boolean,
    onMonthSelected: () -> Unit
) {
    if (usePersianCalendar) {
        PersianYearOverviewView(viewModel, onMonthSelected)
    } else {
        GregorianYearOverviewView(viewModel, onMonthSelected)
    }
}

@Composable
private fun GregorianYearOverviewView(
    viewModel: MainViewModel,
    onMonthSelected: () -> Unit
) {
    val selectedYear by viewModel.selectedYear.collectAsState()
    val yearTasks by viewModel.yearTasks.collectAsState()

    val topLevelTasks = remember(yearTasks) {
        yearTasks.filter { it.parentTaskId == null }
    }

    val monthStats = remember(topLevelTasks) {
        topLevelTasks.groupBy { task ->
            if (task.date.length >= 7) task.date.substring(0, 7) else task.date
        }.mapValues { entries ->
            val tasks = entries.value
            val completed = tasks.count { it.status == "COMPLETED" }
            val pending = tasks.size - completed
            Triple(completed, pending, tasks.size)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.toggleUsePersianCalendar() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Switch to Persian Calendar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
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
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                    val (completedCount, pendingCount, totalCount) = monthStats[monthStr] ?: Triple(0, 0, 0)

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
                            if (totalCount > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
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
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = monthNames[index],
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                if (totalCount > 0) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$pendingCount",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "PEND",
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$completedCount",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "DONE",
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "0",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
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

@Composable
private fun PersianYearOverviewView(
    viewModel: MainViewModel,
    onMonthSelected: () -> Unit
) {
    val persianYear by viewModel.persianYear.collectAsState()
    val persianYearTasks by viewModel.persianYearTasks.collectAsState()

    val topLevelTasks = remember(persianYearTasks) {
        persianYearTasks.filter { it.parentTaskId == null }
    }

    val persianMonthStats = remember(topLevelTasks) {
        topLevelTasks.groupBy { task ->
            PersianCalendarHelper.getPersianDateParts(task.date).second
        }.mapValues { entries ->
            val tasks = entries.value
            val completed = tasks.count { it.status == "COMPLETED" }
            val pending = tasks.size - completed
            Triple(completed, pending, tasks.size)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.selectPersianMonth(persianYear - 1, 1)
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Previous Year",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = persianYear.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.toggleUsePersianCalendar() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Switch to Western Calendar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = {
                    viewModel.selectPersianMonth(persianYear + 1, 1)
                }) {
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = "Next Year",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                items(12) { index ->
                    val (completedCount, pendingCount, totalCount) = persianMonthStats[index + 1] ?: Triple(0, 0, 0)

                    Card(
                        onClick = {
                            viewModel.selectPersianMonth(persianYear, index + 1)
                            onMonthSelected()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (totalCount > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
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
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = PersianCalendarHelper.monthAbbreviations[index],
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                if (totalCount > 0) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$pendingCount",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "PEND",
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$completedCount",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "DONE",
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "0",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
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

@Composable
fun MonthlyPlannerView(
    viewModel: MainViewModel,
    usePersianCalendar: Boolean,
    filterLabel: String? = null,
    onBackToYear: () -> Unit = {}
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val persianYear by viewModel.persianYear.collectAsState()
    val persianMonth by viewModel.persianMonth.collectAsState()

    val rawMonthlyTasks by (
        if (usePersianCalendar) viewModel.persianMonthTasks else viewModel.monthlyTasks
    ).collectAsState()
    val monthlyTasks = if (filterLabel != null) rawMonthlyTasks.filter { it.label == filterLabel } else rawMonthlyTasks

    val mainTasks = monthlyTasks.filter { it.parentTaskId == null }
    var showCompleted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.navigateMonth(-1)
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            val monthLabel = remember(selectedMonth, persianYear, persianMonth, usePersianCalendar) {
                if (usePersianCalendar) {
                    val name = PersianCalendarHelper.monthNames.getOrNull(persianMonth - 1) ?: ""
                    "${name.uppercase()} $persianYear"
                } else {
                    try {
                        val date = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(selectedMonth)
                        if (date != null) {
                            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date).uppercase()
                        } else {
                            selectedMonth
                        }
                    } catch (e: Exception) {
                        selectedMonth
                    }
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
                viewModel.navigateMonth(1)
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
                    val totalCount = mainTasks.size
                    val activeCount = activeTasks.size
                    val completedCount = completedTasks.size
                    val completionProgress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$totalCount", fontSize = 22.sp, fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.onSurface)
                                Text("TOTAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(32.dp)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$activeCount", fontSize = 22.sp, fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.primary)
                                Text("PENDING", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(32.dp)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$completedCount", fontSize = 22.sp, fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.primary)
                                Text("DONE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp)
                            }
                        }
                        if (totalCount > 0) {
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { completionProgress },
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape)
                            )
                        }
                    }

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
    var showReviewTimePicker by remember { mutableStateOf(false) }
    val reviewTimePickerState = rememberTimePickerState(
        initialHour = enteredReviewTime.substringBefore(":").toIntOrNull() ?: 21,
        initialMinute = enteredReviewTime.substringAfter(":").toIntOrNull() ?: 0,
        is24Hour = true
    )

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

                // Section 4: Timer DND Integration
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "TIMER SETTINGS",
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
                                text = "Turn on DND when Pomodoro or Chronometer starts",
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

                // Section: Motto Display
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "MORE SCREEN",
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
                                text = "Show Daily Motto",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        val mottoEnabled by viewModel.mottoEnabled.collectAsState()
                        Switch(
                            checked = mottoEnabled,
                            onCheckedChange = { viewModel.updateMottoEnabled(it) }
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
                            TextButton(onClick = { showReviewTimePicker = true }) {
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

    if (showReviewTimePicker) {
        AlertDialog(
            onDismissRequest = { showReviewTimePicker = false },
            title = { Text("Select Reminder Time", fontWeight = FontWeight.Bold) },
            text = { TimePicker(state = reviewTimePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val hour = reviewTimePickerState.hour
                    val min = reviewTimePickerState.minute
                    enteredReviewTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, min)
                    showReviewTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showReviewTimePicker = false }) { Text("Cancel") }
            }
        )
    }
}

private enum class TodoTabFilter { ALL, PENDING, DONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoTab(viewModel: MainViewModel) {
    val allTodos by viewModel.allTodos.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()

    var filter by remember { mutableStateOf(TodoTabFilter.PENDING) }
    var editingTodo by remember { mutableStateOf<TodoEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<TodoEntity?>(null) }
    var todoForLinking by remember { mutableStateOf<TodoEntity?>(null) }
    var todoForMovingToPlanner by remember { mutableStateOf<TodoEntity?>(null) }
    var showUnlinkConfirm by remember { mutableStateOf<TodoEntity?>(null) }
    var showPendingDetailsDialog by remember { mutableStateOf(false) }
    var expandAllDescriptions by remember { mutableStateOf(false) }

    var draggingTodoId by remember { mutableStateOf<Long?>(null) }
    var dragOffsetX by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var dragOffsetY by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var draggedTodos by remember { mutableStateOf<List<TodoEntity>?>(null) }
    val todoItemHeights = remember { androidx.compose.runtime.mutableStateMapOf<Long, Int>() }
    val densityD = androidx.compose.ui.platform.LocalDensity.current

    val allRootTodos = allTodos.filter { it.parentTodoId == null }
    var expandedSubTodosMap by remember { mutableStateOf(mapOf<Long, Boolean>()) }

    var showFilterChips by remember { mutableStateOf(true) }
    val filterChipScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                if (available.y < -15) showFilterChips = false
                else if (available.y > 15) showFilterChips = true
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    val displayTodos = (when (filter) {
        TodoTabFilter.ALL -> allRootTodos
        TodoTabFilter.PENDING -> allRootTodos.filter { it.status == "PENDING" }
        TodoTabFilter.DONE -> allRootTodos.filter { it.status == "DONE" }
    }).let { list -> draggedTodos ?: list }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .nestedScroll(filterChipScrollConnection)
    ) {
        AnimatedVisibility(
            visible = showFilterChips && allTodos.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TodoTabFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { filter = f },
                        label = { Text(f.name, fontSize = 12.sp) }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
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
                    val pendingTodos = allRootTodos.filter { it.status == "PENDING" }
                    val pendingCount = pendingTodos.size
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.triggerReorderTodosByPriority() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort by Priority",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (allTodos.any { it.description.isNotBlank() }) {
                            IconButton(
                                onClick = { expandAllDescriptions = !expandAllDescriptions },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (expandAllDescriptions) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (expandAllDescriptions) "Collapse All Descriptions" else "Expand All Descriptions",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
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
                            val highCount = pendingTodos.count { it.priority.equals("High", ignoreCase = true) }
                            val mediumCount = pendingTodos.count { it.priority.equals("Medium", ignoreCase = true) }
                            val lowCount = pendingTodos.count { it.priority.equals("Low", ignoreCase = true) }
                            val linkedCountPopup = pendingTodos.count { it.linkedTaskId != null }

                            val density = LocalDensity.current
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
                                            text = "Pending To-Dos",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
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
                                                    Text("$highCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                                                }
                                            }
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
                                                    Text("$mediumCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFB8C00))
                                                }
                                            }
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
                                                    Text("$lowCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF43A047))
                                                }
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                                    .padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("🔗 Linked", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                    Text("$linkedCountPopup", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
                            val subTodos = allTodos.filter { it.parentTodoId == todo.id }
                            val expandedSubTodos = expandedSubTodosMap[todo.id] ?: false
                            Box(modifier = Modifier.animateItem()) {
                                TodoItem(
                                    modifier = Modifier.onGloballyPositioned {
                                        todoItemHeights[todo.id] = it.size.height
                                    },
                                    todo = todo,
                                    expanded = expandAllDescriptions,
                                    viewModel = viewModel,
                                    linkedItemTitle = todo.linkedTaskId?.let { id -> allTasks.find { it.id == id }?.title },
                                    subTodos = subTodos,
                                    expandedSubTodos = expandedSubTodos,
                                    onToggleSubTodosExpanded = {
                                        expandedSubTodosMap = expandedSubTodosMap + (todo.id to !expandedSubTodos)
                                    },
                                    isDragging = draggingTodoId == todo.id,
                                    dragOffsetX = if (draggingTodoId == todo.id) dragOffsetX else 0f,
                                    dragOffsetY = if (draggingTodoId == todo.id) dragOffsetY else 0f,
                                    onDragStart = {
                                        draggingTodoId = todo.id
                                        dragOffsetX = 0f
                                        dragOffsetY = 0f
                                        draggedTodos = displayTodos.toList()
                                    },
                                    onDrag = { amount ->
                                        if (draggingTodoId == todo.id) {
                                            dragOffsetX += amount.x
                                            dragOffsetY += amount.y
                                            val currentList = draggedTodos
                                            if (currentList != null) {
                                                val draggedIndex = currentList.indexOfFirst { it.id == todo.id }
                                                if (draggedIndex != -1) {
                                                    val spacing = with(densityD) { 6.dp.toPx() }
                                                    if (dragOffsetY > 0) {
                                                        if (draggedIndex < currentList.size - 1) {
                                                            val nextItem = currentList[draggedIndex + 1]
                                                            val nextHeight = todoItemHeights[nextItem.id] ?: 80
                                                            val threshold = nextHeight / 2f + spacing
                                                            if (dragOffsetY > threshold) {
                                                                val mutableList = currentList.toMutableList()
                                                                mutableList.removeAt(draggedIndex)
                                                                mutableList.add(draggedIndex + 1, todo)
                                                                draggedTodos = mutableList
                                                                dragOffsetY -= (nextHeight + spacing)
                                                            }
                                                        }
                                                    } else if (dragOffsetY < 0) {
                                                        if (draggedIndex > 0) {
                                                            val prevItem = currentList[draggedIndex - 1]
                                                            val prevHeight = todoItemHeights[prevItem.id] ?: 80
                                                            val threshold = -prevHeight / 2f - spacing
                                                            if (dragOffsetY < threshold) {
                                                                val mutableList = currentList.toMutableList()
                                                                mutableList.removeAt(draggedIndex)
                                                                mutableList.add(draggedIndex - 1, todo)
                                                                draggedTodos = mutableList
                                                                dragOffsetY += (prevHeight + spacing)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        if (draggingTodoId == todo.id) {
                                            val originalTodos = when (filter) {
                                                TodoTabFilter.ALL -> allRootTodos
                                                TodoTabFilter.PENDING -> allRootTodos.filter { it.status == "PENDING" }
                                                TodoTabFilter.DONE -> allRootTodos.filter { it.status == "DONE" }
                                            }
                                            val originalIndex = originalTodos.indexOfFirst { it.id == todo.id }
                                            val currentList = draggedTodos
                                            if (currentList != null && originalIndex != -1) {
                                                val finalIndex = currentList.indexOfFirst { it.id == todo.id }
                                                val deltaIndex = finalIndex - originalIndex
                                                if (deltaIndex != 0) {
                                                    viewModel.reorderTodo(todo, originalTodos, deltaIndex)
                                                }
                                            }
                                            draggingTodoId = null
                                            draggedTodos = null
                                            dragOffsetX = 0f
                                            dragOffsetY = 0f
                                        }
                                    },
                                    onEdit = { editingTodo = it },
                                    onDelete = { showDeleteConfirm = it },
                                    onLink = { todoForLinking = it },
                                    onUnlink = { showUnlinkConfirm = it },
                                    onMoveToPlanner = { todoForMovingToPlanner = it }
                                )
                            }
                        }
                    }
                }
                }
            }
        }
    }

    editingTodo?.let { todo ->
        com.example.ui.components.TaskManagerDialog(
            viewModel = viewModel,
            initialDate = "",
            todoToEdit = todo,
            onDismiss = { editingTodo = null }
        )
    }
    showDeleteConfirm?.let { todo ->
        if (todo.linkedTaskId != null) {
            LinkedDeleteConfirmDialog(
                onDismiss = { showDeleteConfirm = null },
                onDeleteBoth = { viewModel.deleteTodoWithUndo(todo); showDeleteConfirm = null },
                onKeepTodo = { viewModel.unlinkAndDeleteTodoWithUndo(todo); showDeleteConfirm = null }
            )
        } else {
            DeleteConfirmDialog(
                title = "Delete To-Do",
                message = "Delete \"${todo.title}\"?",
                onDismiss = { showDeleteConfirm = null },
                onConfirm = { viewModel.deleteTodoWithUndo(todo); showDeleteConfirm = null }
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
    todoForMovingToPlanner?.let { todo ->
        MoveToPlannerDialog(
            todo = todo,
            viewModel = viewModel,
            onDismiss = { todoForMovingToPlanner = null }
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

    val pendingSubTodoCompletion by viewModel.pendingSubTodoCompletion.collectAsState()
    pendingSubTodoCompletion?.let { pending ->
        val incompleteCount = pending.subTodos.count { it.status != "DONE" }
        AlertDialog(
            onDismissRequest = { viewModel.cancelPendingSubTodoCompletion() },
            title = { Text("Complete with Sub-To-Dos?", fontWeight = FontWeight.Bold) },
            text = {
                Text("'${pending.todo.title}' has $incompleteCount incomplete subtask(s). Complete all $incompleteCount subtask(s) as well?")
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmCompleteTodoWithSubtodos(true) }) {
                    Text("Complete Subtasks")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { viewModel.confirmCompleteTodoWithSubtodos(false) }) {
                        Text("Only This Task")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.cancelPendingSubTodoCompletion() }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TodoItem(
    modifier: Modifier = Modifier,
    todo: TodoEntity,
    expanded: Boolean = false,
    viewModel: MainViewModel,
    linkedItemTitle: String? = null,
    subTodos: List<TodoEntity> = emptyList(),
    expandedSubTodos: Boolean = false,
    onToggleSubTodosExpanded: () -> Unit = {},
    isDragging: Boolean = false,
    dragOffsetX: Float = 0f,
    dragOffsetY: Float = 0f,
    onDragStart: (() -> Unit)? = null,
    onDrag: ((androidx.compose.ui.geometry.Offset) -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null,
    onEdit: (TodoEntity) -> Unit,
    onDelete: (TodoEntity) -> Unit,
    onLink: (TodoEntity) -> Unit,
    onUnlink: (TodoEntity) -> Unit,
    onMoveToPlanner: (TodoEntity) -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    val isDone = todo.status == "DONE"

    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isDragging) 1.04f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "todoDragScale"
    )

    val shadowElevation by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isDragging) 12.dp else 0.dp,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "todoDragShadow"
    )

    val bgColor = if (isDragging) {
        MaterialTheme.colorScheme.surfaceVariant
    } else if (isDone) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val outlineAlpha = if (isDone) 0.08f else 0.2f

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = shadowElevation),
        modifier = modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 10f else 0f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .offset { IntOffset(dragOffsetX.roundToInt(), dragOffsetY.roundToInt()) }
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = outlineAlpha), RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { viewModel.toggleTodoCompletion(todo) },
                onLongClick = { showMenu = true }
            )
            .then(
                if (onDragStart != null && !isDone) {
                    Modifier.pointerInput(todo.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { _ -> onDragStart() },
                            onDragEnd = { onDragEnd?.invoke() },
                            onDragCancel = { onDragEnd?.invoke() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag?.invoke(dragAmount)
                            }
                        )
                    }
                } else Modifier
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isDone) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else Color.Transparent,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Checklist,
                        contentDescription = if (isDone) "Done" else "To-Do",
                        modifier = Modifier.padding(2.dp).size(18.dp),
                        tint = if (isDone) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        todo.title,
                        fontSize = 14.sp,
                        fontWeight = if (isDone) FontWeight.Normal else FontWeight.SemiBold,
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
                                linkedItemTitle ?: "Linked to planner",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                        if (todo.linkedTaskId == null) {
                            DropdownMenuItem(text = { Text("Move to Planner") }, onClick = { showMenu = false; onMoveToPlanner(todo) })
                        }
                        DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = { showMenu = false; onDelete(todo) })
                    }
                }
            }

            if (todo.description.isNotBlank()) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(
                        animationSpec = androidx.compose.animation.core.spring(
                            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy
                        )
                    ) + fadeIn(animationSpec = androidx.compose.animation.core.tween(250)),
                    exit = shrinkVertically(
                        animationSpec = androidx.compose.animation.core.spring(
                            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy
                        )
                    ) + fadeOut(animationSpec = androidx.compose.animation.core.tween(200))
                ) {
                    Text(
                        text = todo.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 28.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
                    )
                }
            }

            // Subtasks
            if (subTodos.isNotEmpty()) {
                val completedSubTodos = subTodos.count { it.status == "DONE" }
                val totalSubTodos = subTodos.size
                val subProgress = completedSubTodos.toFloat() / totalSubTodos

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { onToggleSubTodosExpanded() }
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (completedSubTodos == totalSubTodos) Color(0xFF43A047).copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "$completedSubTodos/$totalSubTodos SUBTASKS",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (completedSubTodos == totalSubTodos) Color(0xFF43A047)
                            else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${(subProgress * 100).toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (expanded || expandedSubTodos) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Subtasks",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(
                    visible = expanded || expandedSubTodos,
                    enter = expandVertically(
                        animationSpec = androidx.compose.animation.core.spring(
                            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy
                        )
                    ) + fadeIn(animationSpec = androidx.compose.animation.core.tween(250)),
                    exit = shrinkVertically(
                        animationSpec = androidx.compose.animation.core.spring(
                            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy
                        )
                    ) + fadeOut(animationSpec = androidx.compose.animation.core.tween(200))
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier.padding(start = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            subTodos.forEach { subTodo ->
                                val subDone = subTodo.status == "DONE"
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
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
                                            .clickable { viewModel.toggleSubTodoCompletion(subTodo) }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (subDone) Icons.Default.Check else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = if (subDone) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = subTodo.title,
                                            fontSize = 11.sp,
                                            color = if (subDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                            textDecoration = if (subDone) TextDecoration.LineThrough else null,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Box {
                                        var subMenuExpanded by remember { mutableStateOf(false) }
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
                                                    viewModel.toggleSubTodoCompletion(subTodo)
                                                    subMenuExpanded = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Remove", color = MaterialTheme.colorScheme.error) },
                                                onClick = {
                                                    viewModel.deleteSubTodo(subTodo)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoveToPlannerDialog(
    todo: TodoEntity,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val currentDate by viewModel.selectedDate.collectAsState()
    var date by remember { mutableStateOf(currentDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    val subTodos = viewModel.allTodos.collectAsState().value.filter { it.parentTodoId == todo.id }

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
        title = { Text("Move to Planner", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("\"${todo.title}\"", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (subTodos.isNotEmpty()) {
                    Text("${subTodos.size} sub-todo(s) will also be moved.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
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
                viewModel.moveTodoToTask(todo, date, subTodos)
                onDismiss()
            }) { Text("Move") }
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
    var editingIdea by remember { mutableStateOf<IdeaEntity?>(null) }
    var showDeleteIdeaConfirm by remember { mutableStateOf<IdeaEntity?>(null) }
    var showDeleteGroupConfirm by remember { mutableStateOf<IdeaGroupEntity?>(null) }
    var editingGroup by remember { mutableStateOf<IdeaGroupEntity?>(null) }
    var ideaForPlanner by remember { mutableStateOf<IdeaEntity?>(null) }
    var expandAllIdeas by remember { mutableStateOf(true) }
    var showIdeaBreakdown by remember { mutableStateOf(false) }

    var draggingIdeaId by remember { mutableStateOf<Long?>(null) }
    var dragOffsetX by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var dragOffsetY by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var draggedIdeas by remember { mutableStateOf<List<IdeaEntity>?>(null) }
    val ideaItemHeights = remember { androidx.compose.runtime.mutableStateMapOf<Long, Int>() }
    val densityD = androidx.compose.ui.platform.LocalDensity.current

    var showGroupChips by remember { mutableStateOf(true) }
    val groupChipScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                if (available.y < -15) showGroupChips = false
                else if (available.y > 15) showGroupChips = true
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    val baseFilteredIdeas = if (selectedGroupId == null) ideas
    else ideas.filter { it.groupId == selectedGroupId }
    val filteredIdeas = draggedIdeas ?: baseFilteredIdeas

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .nestedScroll(groupChipScrollConnection)
    ) {
        // Group filter chips - outside the panel box (like Daily's labels)
        AnimatedVisibility(
            visible = showGroupChips && groups.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            GroupChipRow(
                groups = groups,
                selectedGroupId = selectedGroupId,
                onGroupSelected = { selectedGroupId = it },
                onEditGroup = { editingGroup = it },
                onDeleteGroup = { showDeleteGroupConfirm = it }
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
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
                        IconButton(
                            onClick = { viewModel.triggerReorderIdeasByPriority() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort by Priority",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { expandAllIdeas = !expandAllIdeas },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (expandAllIdeas) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (expandAllIdeas) "Collapse All" else "Expand All",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Box {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { showIdeaBreakdown = !showIdeaBreakdown }
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${filteredIdeas.size} ideas",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (showIdeaBreakdown) {
                                val localDensity = LocalDensity.current
                                val offsetY = with(localDensity) { 32.dp.roundToPx() }
                                Popup(
                                    alignment = Alignment.TopEnd,
                                    offset = IntOffset(x = 0, y = offsetY),
                                    onDismissRequest = { showIdeaBreakdown = false },
                                    properties = PopupProperties(focusable = true)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 8.dp,
                                        shadowElevation = 8.dp,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                        modifier = Modifier.width(280.dp).padding(4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(
                                                text = "Ideas by Group",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            if (ideas.isEmpty()) {
                                                Text(
                                                    text = "No ideas yet",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            } else {
                                                val ideasByGroup = ideas.groupBy { it.groupId }
                                                val groupsWithIdeas = groups.filter { group ->
                                                    ideasByGroup[group.id]?.isNotEmpty() == true
                                                }
                                                val ungroupedCount = ideasByGroup[null]?.size ?: 0
                                                groupsWithIdeas.forEach { group ->
                                                    val count = ideasByGroup[group.id]?.size ?: 0
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(10.dp)
                                                                    .background(Color(group.color), CircleShape)
                                                            )
                                                            Spacer(Modifier.width(8.dp))
                                                            Text(
                                                                text = group.name,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }
                                                        Text(
                                                            text = "${count} idea${if (count != 1) "s" else ""}",
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                if (ungroupedCount > 0) {
                                                    if (groupsWithIdeas.isNotEmpty()) {
                                                        HorizontalDivider(
                                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                                        )
                                                    }
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = "No group",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = "${ungroupedCount} idea${if (ungroupedCount != 1) "s" else ""}",
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            Box(modifier = Modifier.animateItem().onGloballyPositioned { ideaItemHeights[idea.id] = it.size.height }) {
                                IdeaCard(
                                    idea = idea,
                                    expanded = expandAllIdeas,
                                    viewModel = viewModel,
                                    isDragging = draggingIdeaId == idea.id,
                                    dragOffsetX = if (draggingIdeaId == idea.id) dragOffsetX else 0f,
                                    dragOffsetY = if (draggingIdeaId == idea.id) dragOffsetY else 0f,
                                    onDragStart = {
                                        draggingIdeaId = idea.id
                                        dragOffsetX = 0f
                                        dragOffsetY = 0f
                                        draggedIdeas = baseFilteredIdeas
                                    },
                                    onDrag = { amount ->
                                        if (draggingIdeaId == idea.id) {
                                            dragOffsetX += amount.x
                                            dragOffsetY += amount.y
                                            val currentList = draggedIdeas
                                            if (currentList != null) {
                                                val draggedIndex = currentList.indexOfFirst { it.id == idea.id }
                                                if (draggedIndex != -1) {
                                                    val spacing = with(densityD) { 12.dp.toPx() }
                                                    if (dragOffsetY > 0) {
                                                        if (draggedIndex < currentList.size - 1) {
                                                            val nextItem = currentList[draggedIndex + 1]
                                                            val nextHeight = ideaItemHeights[nextItem.id] ?: 80
                                                            val threshold = nextHeight / 2f + spacing
                                                            if (dragOffsetY > threshold) {
                                                                val mutableList = currentList.toMutableList()
                                                                mutableList.removeAt(draggedIndex)
                                                                mutableList.add(draggedIndex + 1, idea)
                                                                draggedIdeas = mutableList
                                                                dragOffsetY -= (nextHeight + spacing)
                                                            }
                                                        }
                                                    } else if (dragOffsetY < 0) {
                                                        if (draggedIndex > 0) {
                                                            val prevItem = currentList[draggedIndex - 1]
                                                            val prevHeight = ideaItemHeights[prevItem.id] ?: 80
                                                            val threshold = -prevHeight / 2f - spacing
                                                            if (dragOffsetY < threshold) {
                                                                val mutableList = currentList.toMutableList()
                                                                mutableList.removeAt(draggedIndex)
                                                                mutableList.add(draggedIndex - 1, idea)
                                                                draggedIdeas = mutableList
                                                                dragOffsetY += (prevHeight + spacing)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        if (draggingIdeaId == idea.id) {
                                            val originalIndex = baseFilteredIdeas.indexOfFirst { it.id == idea.id }
                                            val currentList = draggedIdeas
                                            if (currentList != null && originalIndex != -1) {
                                                val finalIndex = currentList.indexOfFirst { it.id == idea.id }
                                                val deltaIndex = finalIndex - originalIndex
                                                if (deltaIndex != 0) {
                                                    viewModel.reorderIdea(idea, baseFilteredIdeas, deltaIndex)
                                                }
                                            }
                                            draggingIdeaId = null
                                            draggedIdeas = null
                                            dragOffsetX = 0f
                                            dragOffsetY = 0f
                                        }
                                    },
                                    onEdit = { editingIdea = it },
                                    onDelete = { showDeleteIdeaConfirm = it },
                                    onAddToPlanner = { ideaForPlanner = it }
                                )
                            }
                        }
                    }
                }
                }

            }
        }
    }

    editingGroup?.let { group ->
        CreateGroupDialog(
            initialName = group.name,
            initialColor = group.color,
            onDismiss = { editingGroup = null },
            onConfirm = { name, color -> viewModel.updateGroup(group.copy(name = name, color = color)); editingGroup = null }
        )
    }
    editingIdea?.let { idea ->
        val ideaStages by viewModel.stagesForIdea(idea.id).collectAsState(initial = emptyList())
        com.example.ui.components.TaskManagerDialog(
            viewModel = viewModel,
            initialDate = "",
            ideaToEdit = idea,
            initialIdeaStages = ideaStages,
            ideaGroups = groups,
            onDismiss = { editingIdea = null }
        )
    }
    showDeleteIdeaConfirm?.let { idea ->
        DeleteConfirmDialog(
            title = "Delete Idea",
            message = "Delete \"${idea.title}\" and all its stages?",
            onDismiss = { showDeleteIdeaConfirm = null },
            onConfirm = { viewModel.deleteIdeaWithUndo(idea); showDeleteIdeaConfirm = null }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun IdeaCard(
    idea: IdeaEntity,
    expanded: Boolean = true,
    viewModel: MainViewModel,
    isDragging: Boolean = false,
    dragOffsetX: Float = 0f,
    dragOffsetY: Float = 0f,
    onDragStart: (() -> Unit)? = null,
    onDrag: ((androidx.compose.ui.geometry.Offset) -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null,
    onEdit: (IdeaEntity) -> Unit,
    onDelete: (IdeaEntity) -> Unit,
    onAddToPlanner: (IdeaEntity) -> Unit
) {
    val stages by viewModel.stagesForIdea(idea.id).collectAsState(initial = emptyList())
    var showIdeaMenu by remember { mutableStateOf(false) }
    val ideaGroup = remember(idea.groupId) {
        viewModel.ideaGroups.value.find { it.id == idea.groupId }
    }
    val groupColor = ideaGroup?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary

    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isDragging) 1.04f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "ideaDragScale"
    )

    val shadowElevation by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isDragging) 12.dp else 0.dp,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "ideaDragShadow"
    )

    val bgColor = if (isDragging) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 10f else 0f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .offset { IntOffset(dragOffsetX.roundToInt(), dragOffsetY.roundToInt()) }
            .shadow(elevation = shadowElevation, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .then(
                if (onDragStart != null) {
                    Modifier.pointerInput(idea.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { _ -> onDragStart() },
                            onDragEnd = { onDragEnd?.invoke() },
                            onDragCancel = { onDragEnd?.invoke() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag?.invoke(dragAmount)
                            }
                        )
                    }
                } else Modifier
            )
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PriorityBadge(idea.priority)
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

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    if (idea.description.isNotBlank()) {
                        Text(
                            idea.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(start = 26.dp, end = 4.dp, top = 2.dp)
                        )
                    }

                    if (stages.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))
                        stages.forEachIndexed { index, stage ->
                            StageRow(
                                stage = stage,
                                stages = stages,
                                viewModel = viewModel,
                                onDelete = { viewModel.deleteStage(it) }
                            )
                            if (index < stages.lastIndex) {
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }


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
    var stageMenuExpanded by remember { mutableStateOf(false) }

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
                .clickable {
                    if (canToggle || stage.isCompleted) {
                        viewModel.updateStage(stage.copy(isCompleted = !stage.isCompleted))
                        if (stage.isCompleted) {
                            stages.drop(index + 1).forEach {
                                viewModel.updateStage(it.copy(isCompleted = false))
                            }
                        }
                    }
                }
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = if (stage.isCompleted) Icons.Default.Check else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = if (stage.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                stage.title,
                fontSize = 11.sp,
                color = if (stage.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = if (stage.isCompleted) TextDecoration.LineThrough else null,
                fontWeight = FontWeight.Medium
            )
        }

        Box {
            IconButton(onClick = { stageMenuExpanded = true }, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Stage Actions",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            DropdownMenu(
                expanded = stageMenuExpanded,
                onDismissRequest = { stageMenuExpanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                if (canToggle || stage.isCompleted) {
                    DropdownMenuItem(
                        text = { Text(if (stage.isCompleted) "Mark Undone" else "Mark Done") },
                        onClick = {
                            viewModel.updateStage(stage.copy(isCompleted = !stage.isCompleted))
                            if (stage.isCompleted) {
                                stages.drop(index + 1).forEach {
                                    viewModel.updateStage(it.copy(isCompleted = false))
                                }
                            }
                            stageMenuExpanded = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        onDelete(stage)
                        stageMenuExpanded = false
                    }
                )
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

private data class LabelInfo(
    val name: String,
    val color: Long?,
    val count: Int
)
