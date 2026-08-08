package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import kotlin.math.ceil
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Switch
import androidx.compose.material3.Slider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.core.database.entity.TaskEntity
import com.example.core.database.entity.TodoEntity
import com.example.core.database.entity.IdeaEntity
import com.example.core.database.entity.IdeaGroupEntity
import com.example.core.database.entity.IdeaStageEntity
import com.example.core.database.entity.LearnGroupEntity
import com.example.core.database.entity.LearnItemEntity
import com.example.core.database.entity.LearnSectionEntity
import com.example.ui.components.ActiveTimerWidget
import com.example.ui.components.CalendarDatePickerDialog
import com.example.ui.components.HeaderActions
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.core.utils.PersianCalendarHelper
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.net.Uri
import android.media.RingtoneManager
import android.Manifest
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.launch

private sealed class TaskManagerType {
    data object Task : TaskManagerType()
    data object Todo : TaskManagerType()
    data object Idea : TaskManagerType()

    val value: String get() = when (this) {
        Task -> "TASK"
        Todo -> "TODO"
        Idea -> "IDEA"
    }
}

@Composable
fun PlannerScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val todayDate by viewModel.todayDate.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showYearOverview by rememberSaveable { mutableStateOf(true) }

    BackHandler(selectedTab != 0) {
        selectedTab = 0
    }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showTaskManager by remember { mutableStateOf(false) }
    var taskManagerInitialType by remember { mutableStateOf<TaskManagerType>(TaskManagerType.Task) }
    val pendingReviewTask by viewModel.pendingReviewTask.collectAsState()
    val pendingReviewSection by viewModel.pendingReviewSection.collectAsState()
    val pendingReviewLearnItem by viewModel.pendingReviewLearnItem.collectAsState()
    val taskForPomodoroSetup by viewModel.taskForPomodoroSetup.collectAsState()
    val ideaGroups by viewModel.ideaGroups.collectAsState()
    val tabTitles = listOf("DAILY", "WEEKLY", "MONTHLY", "TO-DO", "IDEAS", "LEARN")

    val autoRescheduleMessage by viewModel.autoRescheduleMessage.collectAsState()
    LaunchedEffect(autoRescheduleMessage) {
        autoRescheduleMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearAutoRescheduleMessage()
        }
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
                val safeIndex = selectedTab.coerceAtMost(tabPositions.lastIndex)
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[safeIndex]),
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
                        .clickable {
                            selectedTab = index
                            if (index == 0) viewModel.selectDate(todayDate)
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
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
        var showPostponedOnly by remember { mutableStateOf(false) }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> DailyPlannerView(
                    viewModel,
                    selectedFilterLabels,
                    onLabelsSelected = { selectedFilterLabels = it },
                    showPostponedOnly = showPostponedOnly,
                    onPostponedToggle = { showPostponedOnly = it }
                )
                1 -> WeeklyPlannerView(viewModel, null) { date ->
                    selectedTab = 0
                    viewModel.selectDate(date)
                }
                 2 -> {
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
                 5 -> LearnTab(viewModel)
            }

            if (selectedTab == 0 || selectedTab == 3 || selectedTab == 4) {
                FloatingActionButton(
                    onClick = {
                        taskManagerInitialType = when (selectedTab) {
                            3 -> TaskManagerType.Todo
                            4 -> TaskManagerType.Idea
                            else -> TaskManagerType.Task
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
                        contentDescription = "Add Task",
                        modifier = Modifier.size(24.dp)
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
            initialType = taskManagerInitialType.value,
            ideaGroups = ideaGroups,
            onDismiss = { showTaskManager = false }
        )
    }

    pendingReviewTask?.let { task ->
        pendingReviewSection?.let { section ->
            pendingReviewLearnItem?.let { item ->
                ReviewRatingSheet(
                    learnItem = item,
                    task = task,
                    section = section,
                    onRate = { rating ->
                        viewModel.completeReviewWithRating(task.id, section.id, rating)
                    },
                    onDismiss = { viewModel.dismissReviewRating() }
                )
            }
        }
    }

}

@Composable
fun HeaderSection(viewModel: MainViewModel, onSettingsClick: () -> Unit, onHomeClick: () -> Unit) {
    val selectedDate by viewModel.selectedDate.collectAsState()

    val parsedDate = remember(selectedDate) {
        try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedDate)
        } catch (e: Exception) {
            null
        }
    }

    val formattedGregorian = remember(selectedDate, parsedDate) {
        val date = parsedDate
        if (date != null) {
            try {
                SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(date)
            } catch (e: Exception) {
                selectedDate
            }
        } else {
            selectedDate
        }
    }

    val formattedPersian = remember(selectedDate, parsedDate) {
        val date = parsedDate
        if (date != null) {
            try {
                com.example.core.utils.PersianCalendarHelper.getPersianDateString(date)
            } catch (e: Exception) {
                ""
            }
        } else {
            ""
        }
    }

    val formattedDayOfWeek = remember(selectedDate, parsedDate) {
        val date = parsedDate
        if (date != null) {
            try {
                val gregorianDay = SimpleDateFormat("EEEE", Locale.US).format(date).uppercase()
                val persianDay = com.example.core.utils.PersianCalendarHelper.getPersianDayOfWeekName(selectedDate).uppercase()
                if (persianDay.isNotEmpty()) {
                    "$gregorianDay / $persianDay"
                } else {
                    gregorianDay
                }
            } catch (e: Exception) {
                "PLANNER"
            }
        } else {
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
                text = formattedGregorian,
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = formattedPersian,
                fontSize = 18.sp,
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
fun DailyPlannerView(viewModel: MainViewModel, filterLabels: Set<String> = emptySet(), onLabelsSelected: (Set<String>) -> Unit = {}, showPostponedOnly: Boolean = false, onPostponedToggle: (Boolean) -> Unit = {}) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedDate.collectAsState()
    val todayDate by viewModel.todayDate.collectAsState()
    val rawTasks by viewModel.dailyTasks.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val allTodos by viewModel.allTodos.collectAsState()
    val tasks = rawTasks.filter { task ->
        val matchesLabel = filterLabels.isEmpty() || task.label in filterLabels
        val matchesStatus = !showPostponedOnly || task.postponed
        matchesLabel && matchesStatus
    }

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
    val expandAllItems by viewModel.expandAllItems.collectAsState()
    val expandAllSubtasks by viewModel.expandAllSubtasks.collectAsState()
    var showPendingDetailsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(expandAllSubtasks) {
        expandedSubtasksMap.clear()
    }
    
    var showHeaderExtras by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                if (available.y < -30) {
                    showHeaderExtras = false
                } else if (available.y > 30) {
                    showHeaderExtras = true
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    var showCalendarDialog by remember { mutableStateOf(false) }
    val usePersianCalendar by viewModel.usePersianCalendar.collectAsState()

    if (showCalendarDialog) {
        CalendarDatePickerDialog(
            initialSelectedDate = selectedDate,
            initialUsePersian = usePersianCalendar,
            onDismiss = { showCalendarDialog = false },
            onDateSelected = { date ->
                viewModel.selectDate(date)
                showCalendarDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .nestedScroll(nestedScrollConnection)
    ) {
        val postponedCount = rawTasks.count {
            it.status != "COMPLETED" && (it.type == "TASK" || it.type == "EVENT" || it.type == "NOTE") && it.postponed
        }
        AnimatedVisibility(
            visible = showHeaderExtras && (labelInfos.isNotEmpty() || postponedCount > 0),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterChip(
                        selected = filterLabels.isEmpty() && !showPostponedOnly,
                        onClick = { onLabelsSelected(emptySet()); onPostponedToggle(false) },
                        label = { Text("All", fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        modifier = Modifier.height(26.dp)
                    )
                }
                if (postponedCount > 0) {
                    item {
                        FilterChip(
                            selected = showPostponedOnly,
                            onClick = { onPostponedToggle(!showPostponedOnly) },
                            label = { Text("Postponed ($postponedCount)", fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.height(26.dp)
                        )
                    }
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
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(info.color))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text("${info.name.uppercase()} (${info.count})", fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        },
                        modifier = Modifier.height(26.dp)
                    )
                }
            }
        }

        // Day Navigator Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
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
                        text = "DAILY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )

                    val pendingCount = tasks.count { it.parentTaskId == null && it.status != "COMPLETED" }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showCalendarDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Pick Date",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
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
                            onClick = { viewModel.toggleExpandAllItems() },
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
                            onClick = { viewModel.toggleExpandAllSubtasks() },
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
                                                            Text("High", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
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
                                                            Text("Medium", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFB8C00))
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
                                                            Text("Low", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF43A047))
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
                                                            Text("Important", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
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
                                                            Text("Optional", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

                AnimatedContent(
                    targetState = filterLabels to showPostponedOnly,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) }
                ) {
                if (tasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when {
                                    showPostponedOnly -> "No postponed tasks."
                                    filterLabels.isNotEmpty() -> "No tasks with this label."
                                    else -> "Your daily log is empty."
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when {
                                    showPostponedOnly -> "All tasks are in progress."
                                    filterLabels.isNotEmpty() -> "Try removing the filter."
                                    else -> "Tap + to add standard tasks, events, or notes."
                                },
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

                    // Cleanup itemHeights for deleted tasks
                    val allTaskIds = allTasks.map { it.id }.toSet()
                    androidx.compose.runtime.SideEffect {
                        val keysToRemove = itemHeights.keys.filter { it !in allTaskIds }
                        keysToRemove.forEach { itemHeights.remove(it) }
                    }

                    LaunchedEffect(filterLabels, showPostponedOnly) {
                        lazyListState.animateScrollToItem(0)
                    }

                    LazyColumn(state = lazyListState,
                        modifier = Modifier
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

                        items(displayedActiveTasks, key = { "active-${it.id}" }) { task ->
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
                                    onLogTime = { duration, sh, sm, eh, em ->
                                        viewModel.completeTaskWithManualDuration(task, duration, sh, sm, eh, em)
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
                                items(completedTasks, key = { "done-${it.id}" }) { task ->
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
                                            onLogTime = { duration, sh, sm, eh, em ->
                                                viewModel.completeTaskWithManualDuration(task, duration, sh, sm, eh, em)
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

    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnDragCancel by rememberUpdatedState(onDragCancel)
    val currentOnReorder by rememberUpdatedState(onReorder)

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
                            onDragStart = { _ -> currentOnDragStart?.invoke() },
                            onDragEnd = { currentOnDragEnd?.invoke() },
                            onDragCancel = { currentOnDragCancel?.invoke() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                currentOnDrag?.invoke(dragAmount)
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
                                    currentOnReorder?.invoke(finalOffsetY, isSubtask)
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
                                if (task.linkedLearnSectionId != null) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color(0xFFFFB300)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.CircleShape)
                                    )
                                }
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
                if (task.postponeCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFE53935).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "\u00D7${task.postponeCount}",
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
                    if (task.linkedLearnSectionId != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color(0xFFFFB300)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = task.label,
                                fontSize = 11.sp,
                                color = Color(0xFFFFB300),
                                fontWeight = FontWeight.Medium
                            )
                        }
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
                DropdownMenuItem(
                    text = { Text("Turn into Idea") },
                    leadingIcon = { Icon(Icons.Default.Lightbulb, contentDescription = null) },
                    onClick = {
                        onTurnIntoIdea()
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
                    modifier = Modifier.padding(start = 56.dp, end = 8.dp),
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
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onSubtaskToggle(subtask) }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically, 
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (subCompleted) Icons.Default.Check else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = if (subCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
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
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
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
        CalendarDatePickerDialog(
            minDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            onDismiss = { showRescheduleDialog = false },
            onDateSelected = { date ->
                onMigrate(date)
                showRescheduleDialog = false
            }
        )
    }
    }

}

@Composable
fun WeeklyPlannerView(viewModel: MainViewModel, filterLabel: String? = null, onNavigateToDaily: (String) -> Unit = {}) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val todayDate by viewModel.todayDate.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val usePersianCalendar by viewModel.usePersianCalendar.collectAsState()

    // Local week anchor — decouples week scrolling from selectedDate.
    // Initializes from selectedDate each time WeeklyPlannerView enters composition.
    var weekAnchorDate by remember { mutableStateOf(selectedDate) }

    val weekDays = remember(weekAnchorDate) {
        getDaysOfWeek(weekAnchorDate)
    }

    val displayDays = remember(weekAnchorDate, usePersianCalendar) {
        if (usePersianCalendar) {
            getDaysOfWeek(weekAnchorDate, Calendar.SATURDAY)
        } else {
            getDaysOfWeek(weekAnchorDate)
        }
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

                val pFirstParts = com.example.core.utils.PersianCalendarHelper.getPersianDateParts(weekDays.first())
                val pLastParts = com.example.core.utils.PersianCalendarHelper.getPersianDateParts(weekDays.last())
                val persianRange = if (pFirstParts.third != 0 && pLastParts.third != 0) {
                    val helper = com.example.core.utils.PersianCalendarHelper
                    val abbr = { m: Int -> helper.monthAbbreviations.getOrElse(m - 1) { "?" } }
                    val pFirstStr = "${pFirstParts.third} ${abbr(pFirstParts.second)}"
                    val pLastStr = "${pLastParts.third} ${abbr(pLastParts.second)}"
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

            Text(
                text = if (usePersianCalendar) "FA" else "EN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { viewModel.toggleUsePersianCalendar() }
                    .padding(horizontal = 4.dp)
            )

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
            items(displayDays) { dayDate ->
            val topLevelTasks = allTasks.filter { it.date == dayDate && it.parentTaskId == null && (filterLabel == null || it.label == filterLabel) }
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateObj = sdf.parse(dayDate)
            val gregorianDayLabel = SimpleDateFormat("EEEE", Locale.getDefault()).format(dateObj ?: Date())
            val persianDayLabel = com.example.core.utils.PersianCalendarHelper.getPersianDayOfWeekName(dayDate)
            val dayLabel = if (usePersianCalendar) persianDayLabel else gregorianDayLabel
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                                Spacer(modifier = Modifier.width(2.dp))
                                IconButton(
                                    onClick = { onNavigateToDaily(dayDate) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForwardIos,
                                        contentDescription = "View in daily",
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
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
                                        } else if (task.linkedLearnSectionId != null) {
                                            Icon(
                                                imageVector = Icons.Default.MenuBook,
                                                contentDescription = null,
                                                modifier = Modifier.size(10.dp),
                                                tint = Color(0xFFFFB300)
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

            val usePersianCalendar by viewModel.usePersianCalendar.collectAsState()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (usePersianCalendar) "FA" else "EN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { viewModel.toggleUsePersianCalendar() }
                        .padding(horizontal = 4.dp)
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

            val usePersianCalendar by viewModel.usePersianCalendar.collectAsState()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (usePersianCalendar) "FA" else "EN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { viewModel.toggleUsePersianCalendar() }
                        .padding(horizontal = 4.dp)
                )
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

private enum class MonthFilter { TOTAL, PENDING, DONE }

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
    var monthFilter by remember { mutableStateOf(MonthFilter.PENDING) }

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
                    val totalCount = mainTasks.size
                    val pendingCount = mainTasks.count { it.status != "COMPLETED" }
                    val completedCount = mainTasks.count { it.status == "COMPLETED" }
                    val completionProgress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

                    val filteredTasks = remember(monthFilter, mainTasks) {
                        when (monthFilter) {
                            MonthFilter.TOTAL -> mainTasks
                            MonthFilter.PENDING -> mainTasks.filter { it.status != "COMPLETED" }
                            MonthFilter.DONE -> mainTasks.filter { it.status == "COMPLETED" }
                        }
                    }

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
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { monthFilter = MonthFilter.TOTAL }
                                    .background(
                                        if (monthFilter == MonthFilter.TOTAL) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        else Color.Transparent
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "$totalCount",
                                    fontSize = 22.sp,
                                    fontWeight = if (monthFilter == MonthFilter.TOTAL) FontWeight.Bold else FontWeight.Light,
                                    color = if (monthFilter == MonthFilter.TOTAL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "TOTAL",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (monthFilter == MonthFilter.TOTAL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(32.dp)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { monthFilter = MonthFilter.PENDING }
                                    .background(
                                        if (monthFilter == MonthFilter.PENDING) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        else Color.Transparent
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "$pendingCount",
                                    fontSize = 22.sp,
                                    fontWeight = if (monthFilter == MonthFilter.PENDING) FontWeight.Bold else FontWeight.Light,
                                    color = if (monthFilter == MonthFilter.PENDING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "PENDING",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (monthFilter == MonthFilter.PENDING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(32.dp)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { monthFilter = MonthFilter.DONE }
                                    .background(
                                        if (monthFilter == MonthFilter.DONE) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        else Color.Transparent
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "$completedCount",
                                    fontSize = 22.sp,
                                    fontWeight = if (monthFilter == MonthFilter.DONE) FontWeight.Bold else FontWeight.Light,
                                    color = if (monthFilter == MonthFilter.DONE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "DONE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (monthFilter == MonthFilter.DONE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        if (totalCount > 0) {
                            LinearProgressIndicator(
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
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredTasks) { task ->
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
                                        color = if (task.status == "COMPLETED") MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
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
    onDismiss: () -> Unit,
    onMarkAsDone: () -> Unit,
    onStartPomodoro: () -> Unit,
    onLogTime: (durationMinutes: Int, startHour: Int?, startMinute: Int?, endHour: Int?, endMinute: Int?) -> Unit = { _, _, _, _, _ -> }
) {
    var showTimeLogInput by remember { mutableStateOf(false) }
    var enteredDuration by remember { mutableStateOf("0") }
    var startHour by remember { mutableStateOf<Int?>(null) }
    var startMinute by remember { mutableStateOf<Int?>(null) }
    var endHour by remember { mutableStateOf<Int?>(null) }
    var endMinute by remember { mutableStateOf<Int?>(null) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val startPickerState = rememberTimePickerState(
        initialHour = startHour ?: java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY),
        initialMinute = startMinute ?: java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE),
        is24Hour = true
    )
    val endPickerState = rememberTimePickerState(
        initialHour = endHour ?: (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) + 1).coerceAtMost(23),
        initialMinute = startMinute ?: java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE),
        is24Hour = true
    )

    val calculatedDuration = remember(startHour, startMinute, endHour, endMinute) {
        val sh = startHour; val sm = startMinute; val eh = endHour; val em = endMinute
        if (sh != null && sm != null && eh != null && em != null) {
            (eh * 60 + em - sh * 60 - sm).coerceAtLeast(0)
        } else enteredDuration.toIntOrNull() ?: 0
    }

    if (showStartPicker) {
        AlertDialog(
            onDismissRequest = { showStartPicker = false },
            title = { Text("Start Time", fontWeight = FontWeight.Bold) },
            text = { TimePicker(state = startPickerState) },
            confirmButton = {
                TextButton(onClick = {
                    startHour = startPickerState.hour
                    startMinute = startPickerState.minute
                    showStartPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text("Cancel") }
            }
        )
    }
    if (showEndPicker) {
        AlertDialog(
            onDismissRequest = { showEndPicker = false },
            title = { Text("End Time", fontWeight = FontWeight.Bold) },
            text = { TimePicker(state = endPickerState) },
            confirmButton = {
                TextButton(onClick = {
                    endHour = endPickerState.hour
                    endMinute = endPickerState.minute
                    showEndPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text("Cancel") }
            }
        )
    }

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

                if (!showTimeLogInput) {
                    Text(
                        text = "How would you like to proceed with this intention?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

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

                    OutlinedButton(
                        onClick = {
                            val now = java.util.Calendar.getInstance()
                            startHour = startHour ?: now.get(java.util.Calendar.HOUR_OF_DAY)
                            startMinute = startMinute ?: now.get(java.util.Calendar.MINUTE)
                            endHour = endHour ?: (now.get(java.util.Calendar.HOUR_OF_DAY) + 1).coerceAtMost(23)
                            endMinute = endMinute ?: now.get(java.util.Calendar.MINUTE)
                            showTimeLogInput = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Update, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Complete & Log Time")
                        }
                    }

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
                    Text(
                        text = "Log time spent on this task:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("From:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(
                            onClick = { showStartPicker = true },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (startHour != null) String.format("%02d:%02d", startHour, startMinute) else "--:--",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text("To:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(
                            onClick = { showEndPicker = true },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (endHour != null) String.format("%02d:%02d", endHour, endMinute) else "--:--",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (calculatedDuration > 0) {
                        Text(
                            text = "Duration: ${calculatedDuration}m",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedTextField(
                        value = enteredDuration,
                        onValueChange = { enteredDuration = it },
                        label = { Text("Manual minutes (0 = no log)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
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
                        TextButton(onClick = { showTimeLogInput = false }) {
                            Text("BACK", color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val sh = startHour; val sm = startMinute; val eh = endHour; val em = endMinute
                                val effectiveDuration = if (sh != null && sm != null && eh != null && em != null) {
                                    (eh * 60 + em - sh * 60 - sm).coerceAtLeast(0)
                                } else {
                                    enteredDuration.toIntOrNull() ?: 0
                                }
                                onLogTime(effectiveDuration, sh, sm, eh, em)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(if (calculatedDuration > 0) "DONE & LOG" else "MARK COMPLETE")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            if (!showTimeLogInput) {
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
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val selected = sdf.parse(selectedDate) ?: return "TODAY"
        val today = sdf.parse(todayDate) ?: return "TODAY"

        val diff = selected.time - today.time
        val days = Math.round(diff.toDouble() / (1000 * 60 * 60 * 24)).toInt()

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
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(dateStr) ?: return dateStr
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DAY_OF_MONTH, offsetDays)
        return sdf.format(cal.time)
    } catch (e: Exception) {
        return dateStr
    }
}

fun getOffsetMonthString(monthStr: String, offsetMonths: Int): String {
    val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val date = sdf.parse(monthStr) ?: Date()
    val cal = Calendar.getInstance()
    cal.time = date
    cal.add(Calendar.MONTH, offsetMonths)
    return sdf.format(cal.time)
}

fun getDaysOfWeek(dateStr: String, firstDayOfWeek: Int? = null): List<String> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = sdf.parse(dateStr) ?: Date()
    val cal = Calendar.getInstance()
    cal.time = date
    val target = firstDayOfWeek ?: cal.firstDayOfWeek
    var diff = cal.get(Calendar.DAY_OF_WEEK) - target
    if (diff < 0) diff += 7
    cal.add(Calendar.DAY_OF_YEAR, -diff)

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
    val backupLocationUri by viewModel.backupLocationUri.collectAsState()
    val backupMaxMonths by viewModel.backupMaxMonths.collectAsState()
    val lastBackupTimestamp by viewModel.lastBackupTimestamp.collectAsState()
    val backupEnabled by viewModel.backupEnabled.collectAsState()
    val backupTime by viewModel.backupTime.collectAsState()
    val backupFailureNotify by viewModel.backupFailureNotify.collectAsState()
    val dndEnabled by viewModel.dndEnabled.collectAsState()
    val eventReminderVibrate by viewModel.eventReminderVibrate.collectAsState()
    val eventReminderSound by viewModel.eventReminderSound.collectAsState()
    val eventReminderEnabled by viewModel.eventReminderEnabled.collectAsState()
    val usePersianCalendar by viewModel.usePersianCalendar.collectAsState()

    var enteredBackupEnabled by remember { mutableStateOf(backupEnabled) }
    var enteredBackupTime by remember { mutableStateOf(backupTime) }
    var enteredBackupFailureNotify by remember { mutableStateOf(backupFailureNotify) }
    var showBackupTimePicker by remember { mutableStateOf(false) }
    var enteredDndEnabled by remember { mutableStateOf(dndEnabled) }
    var enteredEventVibrate by remember { mutableStateOf(eventReminderVibrate) }
    var enteredEventSound by remember { mutableStateOf(eventReminderSound) }
    var enteredEventEnabled by remember { mutableStateOf(eventReminderEnabled) }

    var statusMessage by remember { mutableStateOf("") }
    var isSuccessStatus by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Permission states for the Permissions card
    var permHasNotification by remember { mutableStateOf(viewModel.hasNotificationPermission(context)) }
    var permHasExactAlarm by remember { mutableStateOf(viewModel.hasExactAlarmPermission(context)) }
    var permHasUsageStats by remember { mutableStateOf(viewModel.hasUsageStatsPermission(context)) }
    var permHasDndAccess by remember { mutableStateOf(viewModel.checkNotificationPolicyPermission(context)) }
    var permHasFullScreenIntent by remember { mutableStateOf(viewModel.hasFullScreenIntentPermission(context)) }
    var permHasManageStorage by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) android.os.Environment.isExternalStorageManager()
            else true
        )
    }
    var notificationPermanentlyDenied by remember { mutableStateOf(false) }
    var permHasBatteryOpt by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val pm = context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
                pm.isIgnoringBatteryOptimizations(context.packageName)
            } else true
        )
    }

    var showRestoreMonthPicker by remember { mutableStateOf(false) }
    var restoreMonths by remember { mutableStateOf<List<String>>(emptyList()) }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var pendingRestoreMonth by remember { mutableStateOf("") }
    val isSyncing by viewModel.isSyncing.collectAsState()
    val currentMonth = remember {
        java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US).format(java.util.Date())
    }

    // Launcher for backup folder picker
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                android.widget.Toast.makeText(
                    context,
                    "Could not persist folder access — please try again",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            viewModel.setBackupLocationUri(uri.toString())
        }
    }

    // Launcher for MANAGE_EXTERNAL_STORAGE settings
    val manageStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshBackupWritable()
    }

    // Launcher for battery optimization settings
    val batteryOptLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
            permHasBatteryOpt = pm.isIgnoringBatteryOptimizations(context.packageName)
        }
    }

    // Permission launchers for the Permissions card
    val permNotificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permHasNotification = granted
            if (!granted) {
                val activity = context as? android.app.Activity
                if (activity != null && !androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                        activity, Manifest.permission.POST_NOTIFICATIONS
                    )
                ) {
                    notificationPermanentlyDenied = true
                }
            }
        }
    }

    val permExactAlarmLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        permHasExactAlarm = viewModel.hasExactAlarmPermission(context)
    }

    val permUsageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        permHasUsageStats = viewModel.hasUsageStatsPermission(context)
    }

    val permDndLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        permHasDndAccess = viewModel.checkNotificationPolicyPermission(context)
    }

    val permFullScreenLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= 34) {
            permHasFullScreenIntent = viewModel.hasFullScreenIntentPermission(context)
        }
    }

    val permManageStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        permHasManageStorage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else true
    }

    // Refresh permission states when returning from settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permHasNotification = viewModel.hasNotificationPermission(context)
                permHasExactAlarm = viewModel.hasExactAlarmPermission(context)
                permHasUsageStats = viewModel.hasUsageStatsPermission(context)
                permHasDndAccess = viewModel.checkNotificationPolicyPermission(context)
                if (Build.VERSION.SDK_INT >= 34) {
                    permHasFullScreenIntent = viewModel.hasFullScreenIntentPermission(context)
                }
                permHasManageStorage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    android.os.Environment.isExternalStorageManager()
                } else true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Formatted last backup time
    val lastBackupTime = if (lastBackupTimestamp > 0) {
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(lastBackupTimestamp))
    } else null

    val reviewReminderTime by viewModel.reviewReminderTime.collectAsState()
    val reviewReminderEnabled by viewModel.reviewReminderEnabled.collectAsState()
    var enteredReviewTime by remember { mutableStateOf(reviewReminderTime) }
    var enteredReviewEnabled by remember { mutableStateOf(reviewReminderEnabled) }
    var showReviewTimePicker by remember { mutableStateOf(false) }

    val sleepReminderTime by viewModel.sleepReminderTime.collectAsState()
    val sleepReminderEnabled by viewModel.sleepReminderEnabled.collectAsState()
    var enteredSleepTime by remember { mutableStateOf(sleepReminderTime) }
    var enteredSleepEnabled by remember { mutableStateOf(sleepReminderEnabled) }
    var showSleepTimePicker by remember { mutableStateOf(false) }

    val diaryReminderTime by viewModel.diaryReminderTime.collectAsState()
    val diaryReminderEnabled by viewModel.diaryReminderEnabled.collectAsState()
    var enteredDiaryTime by remember { mutableStateOf(diaryReminderTime) }
    var enteredDiaryEnabled by remember { mutableStateOf(diaryReminderEnabled) }
    var showDiaryTimePicker by remember { mutableStateOf(false) }

    val plannerReminderTime by viewModel.plannerReminderTime.collectAsState()
    val plannerReminderEnabled by viewModel.plannerReminderEnabled.collectAsState()
    var enteredPlannerTime by remember { mutableStateOf(plannerReminderTime) }
    var enteredPlannerEnabled by remember { mutableStateOf(plannerReminderEnabled) }
    var showPlannerTimePicker by remember { mutableStateOf(false) }

    val habitsReminderTime by viewModel.habitsReminderTime.collectAsState()
    val habitsReminderEnabled by viewModel.habitsReminderEnabled.collectAsState()
    var enteredHabitsTime by remember { mutableStateOf(habitsReminderTime) }
    var enteredHabitsEnabled by remember { mutableStateOf(habitsReminderEnabled) }
    var showHabitsTimePicker by remember { mutableStateOf(false) }

    val tomorrowPlannerReminderTime by viewModel.tomorrowPlannerReminderTime.collectAsState()
    val tomorrowPlannerReminderEnabled by viewModel.tomorrowPlannerReminderEnabled.collectAsState()
    var enteredTomorrowPlannerTime by remember { mutableStateOf(tomorrowPlannerReminderTime) }
    var enteredTomorrowPlannerEnabled by remember { mutableStateOf(tomorrowPlannerReminderEnabled) }
    var showTomorrowPlannerTimePicker by remember { mutableStateOf(false) }

    val learnReviewReminderTime by viewModel.learnReviewReminderTime.collectAsState()
    val learnReviewReminderEnabled by viewModel.learnReviewReminderEnabled.collectAsState()
    var enteredLearnReviewReminderTime by remember { mutableStateOf(learnReviewReminderTime) }
    var enteredLearnReviewReminderEnabled by remember { mutableStateOf(learnReviewReminderEnabled) }
    var showLearnReviewReminderTimePicker by remember { mutableStateOf(false) }

    val pomodoroRingtoneUri by viewModel.pomodoroRingtoneUri.collectAsState()
    val pomodoroRingtoneEnabled by viewModel.pomodoroRingtoneEnabled.collectAsState()
    val pomodoroVibrateEnabled by viewModel.pomodoroVibrateEnabled.collectAsState()
    val pomodoroVibratePattern by viewModel.pomodoroVibratePattern.collectAsState()
    val defaultBreakMinutes by viewModel.defaultBreakMinutes.collectAsState()
    var enteredPomodoroRingtoneUri by remember { mutableStateOf(pomodoroRingtoneUri) }
    var enteredPomodoroRingtoneEnabled by remember { mutableStateOf(pomodoroRingtoneEnabled) }
    var enteredPomodoroVibrateEnabled by remember { mutableStateOf(pomodoroVibrateEnabled) }
    var enteredPomodoroVibratePattern by remember { mutableStateOf(pomodoroVibratePattern) }
    var enteredDefaultBreakMinutes by remember { mutableStateOf(defaultBreakMinutes.toString()) }
    val reviewTimePickerState = rememberTimePickerState(
        initialHour = enteredReviewTime.substringBefore(":").toIntOrNull() ?: 21,
        initialMinute = enteredReviewTime.substringAfter(":").toIntOrNull() ?: 0,
        is24Hour = true
    )
    val sleepTimePickerState = rememberTimePickerState(
        initialHour = enteredSleepTime.substringBefore(":").toIntOrNull() ?: 9,
        initialMinute = enteredSleepTime.substringAfter(":").toIntOrNull() ?: 0,
        is24Hour = true
    )
    val diaryTimePickerState = rememberTimePickerState(
        initialHour = enteredDiaryTime.substringBefore(":").toIntOrNull() ?: 20,
        initialMinute = enteredDiaryTime.substringAfter(":").toIntOrNull() ?: 0,
        is24Hour = true
    )
    val plannerTimePickerState = rememberTimePickerState(
        initialHour = enteredPlannerTime.substringBefore(":").toIntOrNull() ?: 7,
        initialMinute = enteredPlannerTime.substringAfter(":").toIntOrNull() ?: 0,
        is24Hour = true
    )
    val habitsTimePickerState = rememberTimePickerState(
        initialHour = enteredHabitsTime.substringBefore(":").toIntOrNull() ?: 21,
        initialMinute = enteredHabitsTime.substringAfter(":").toIntOrNull() ?: 0,
        is24Hour = true
    )
    val tomorrowPlannerTimePickerState = rememberTimePickerState(
        initialHour = enteredTomorrowPlannerTime.substringBefore(":").toIntOrNull() ?: 20,
        initialMinute = enteredTomorrowPlannerTime.substringAfter(":").toIntOrNull() ?: 0,
        is24Hour = true
    )
    val learnReviewReminderTimePickerState = rememberTimePickerState(
        initialHour = enteredLearnReviewReminderTime.substringBefore(":").toIntOrNull() ?: 19,
        initialMinute = enteredLearnReviewReminderTime.substringAfter(":").toIntOrNull() ?: 0,
        is24Hour = true
    )

    val backupTimePickerState = rememberTimePickerState(
        initialHour = enteredBackupTime.substringBefore(":").toIntOrNull() ?: 23,
        initialMinute = enteredBackupTime.substringAfter(":").toIntOrNull() ?: 0,
        is24Hour = true
    )

    val notificationManager = remember { context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager }

    val postNotificationsLauncher = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                android.widget.Toast.makeText(context, "Notifications disabled — enable in Settings", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    } else null

    var dirty by remember { mutableStateOf(false) }
    var showCancelConfirm by remember { mutableStateOf(false) }

    val onReminderToggle = { onEnable: (Boolean) -> Unit ->
        { enabled: Boolean ->
            onEnable(enabled)
            dirty = true
            if (enabled) {
                postNotificationsLauncher?.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                if (Build.VERSION.SDK_INT in Build.VERSION_CODES.S until Build.VERSION_CODES.TIRAMISU) {
                    val alarmMgr = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
                    if (!alarmMgr.canScheduleExactAlarms()) {
                        context.startActivity(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                    }
                }
            }
        }
    }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newValue ->
            if (newValue == SheetValue.Hidden && dirty) {
                showCancelConfirm = true
                false
            } else true
        }
    )

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.updateDndEnabled(enteredDndEnabled)
            viewModel.updateBackupEnabled(enteredBackupEnabled)
            viewModel.updateBackupTime(enteredBackupTime)
            viewModel.updateBackupFailureNotify(enteredBackupFailureNotify)
            viewModel.updateEventReminderVibrate(enteredEventVibrate)
            viewModel.updateEventReminderSound(enteredEventSound)
            viewModel.updateEventReminderEnabled(enteredEventEnabled)
            viewModel.updatePomodoroRingtoneUri(enteredPomodoroRingtoneUri)
            viewModel.updatePomodoroRingtoneEnabled(enteredPomodoroRingtoneEnabled)
            viewModel.updatePomodoroVibrateEnabled(enteredPomodoroVibrateEnabled)
            viewModel.updatePomodoroVibratePattern(enteredPomodoroVibratePattern)
            viewModel.updateReviewReminderTime(enteredReviewTime)
            viewModel.updateReviewReminderEnabled(enteredReviewEnabled)
            viewModel.updateSleepReminderTime(enteredSleepTime)
            viewModel.updateSleepReminderEnabled(enteredSleepEnabled)
            viewModel.updateDiaryReminderTime(enteredDiaryTime)
            viewModel.updateDiaryReminderEnabled(enteredDiaryEnabled)
            viewModel.updatePlannerReminderTime(enteredPlannerTime)
            viewModel.updatePlannerReminderEnabled(enteredPlannerEnabled)
            viewModel.updateHabitsReminderTime(enteredHabitsTime)
            viewModel.updateHabitsReminderEnabled(enteredHabitsEnabled)
            viewModel.updateTomorrowPlannerReminderTime(enteredTomorrowPlannerTime)
            viewModel.updateTomorrowPlannerReminderEnabled(enteredTomorrowPlannerEnabled)
            viewModel.updateLearnReviewReminderTime(enteredLearnReviewReminderTime)
            viewModel.updateLearnReviewReminderEnabled(enteredLearnReviewReminderEnabled)
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Settings", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            // 1. Backup & Restore
            SettingsCard(title = "BACKUP & RESTORE") {
                // Backup location
                Text(
                    text = if (backupLocationUri != null) "Backup location set" else "No backup location set",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (backupLocationUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (backupLocationUri != null) {
                    val displayPath = remember(backupLocationUri) {
                        try {
                            Uri.decode(backupLocationUri).substringAfter("tree/")
                        } catch (_: Exception) {
                            backupLocationUri ?: ""
                        }
                    }
                    Text(
                        text = displayPath,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { folderPickerLauncher.launch(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (backupLocationUri != null) "Change Folder" else "Choose Backup Folder")
                }

                // Permission indicator
                if (backupLocationUri != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val backupWritable by viewModel.backupLocationWritable.collectAsState()
                    LaunchedEffect(backupLocationUri) {
                        viewModel.refreshBackupWritable()
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (backupWritable) Color(0xFF4CAF50)
                                    else Color(0xFFE53935),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (backupWritable) "Location accessible"
                                   else "Location not accessible — tap Change Folder to reselect",
                            fontSize = 11.sp,
                            color = if (backupWritable) Color(0xFF4CAF50) else Color(0xFFE53935)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Text(
                    text = if (lastBackupTime != null) "Last backup: $lastBackupTime" else "Last backup: Never",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.backupDataToLocation { success, msg ->
                                isSuccessStatus = success
                                statusMessage = msg
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = backupLocationUri != null && !isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Backup Now", fontSize = 12.sp)
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                restoreMonths = viewModel.listBackupMonths()
                                showRestoreMonthPicker = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = backupLocationUri != null && !isSyncing,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restore", fontSize = 12.sp)
                            }
                        }
                    }
                }

                if (backupLocationUri == null) {
                    Text(
                        text = "Set a backup folder first",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                // Inline status message (visible right after backup action)
                if (statusMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
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

                // Direct file access toggle
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Use direct file access", fontSize = 13.sp)
                        Text(
                            text = "Fallback to java.io.File when SAF is unavailable (requires All files access)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val directAccess by viewModel.useDirectFileAccess.collectAsState()
                    Switch(
                        checked = directAccess,
                        onCheckedChange = { enabled ->
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                                !android.os.Environment.isExternalStorageManager()
                            ) {
                                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                try {
                                    manageStorageLauncher.launch(intent)
                                } catch (_: Exception) { }
                            } else {
                                viewModel.setUseDirectFileAccess(enabled)
                            }
                        }
                    )
                }

                // Keep last N months
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Keep last N months", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("$backupMaxMonths months", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Slider(
                        value = backupMaxMonths.toFloat(),
                        onValueChange = { viewModel.setBackupMaxMonths(it.toInt()) },
                        valueRange = 1f..24f,
                        steps = 22,
                        modifier = Modifier.width(140.dp)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Auto Backup (always visible)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto Backup", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Daily backup at scheduled time", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = enteredBackupEnabled,
                        onCheckedChange = { enteredBackupEnabled = it; dirty = true }
                    )
                }
                if (enteredBackupEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Backup time", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(enteredBackupTime, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(onClick = { showBackupTimePicker = true }) {
                            Text("Change", fontSize = 12.sp)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notify on failure", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Switch(
                            checked = enteredBackupFailureNotify,
                            onCheckedChange = { enteredBackupFailureNotify = it; dirty = true }
                        )
                    }
                }
            }

            if (showRestoreMonthPicker) {
                AlertDialog(
                    onDismissRequest = { showRestoreMonthPicker = false },
                    title = { Text("Restore from month") },
                    text = {
                        Column {
                            Text("Select a month to restore. _permanent data (habits, todos, mottos, etc.) will be merged from every backup.")
                            Spacer(modifier = Modifier.height(8.dp))
                            if (restoreMonths.isEmpty()) {
                                Text("No backup months found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                restoreMonths.forEach { month ->
                                    TextButton(
                                        onClick = {
                                            showRestoreMonthPicker = false
                                            pendingRestoreMonth = month
                                            showRestoreConfirm = true
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(month)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showRestoreMonthPicker = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (showRestoreConfirm) {
                val isOlderMonth = pendingRestoreMonth < currentMonth
                AlertDialog(
                    onDismissRequest = { showRestoreConfirm = false },
                    title = { Text("Confirm Restore") },
                    text = {
                        Column {
                            if (isOlderMonth) {
                                Text(
                                    text = "Warning: restoring from $pendingRestoreMonth will REPLACE all current data, including data from months after $pendingRestoreMonth.",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Text("A safety snapshot of current data will be saved to _pre_restore/ before restore.")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("This action cannot be undone beyond the safety snapshot.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showRestoreConfirm = false
                            viewModel.restoreFromMonth(pendingRestoreMonth) { success, msg ->
                                isSuccessStatus = success
                                statusMessage = msg
                            }
                        }) {
                            Text("Restore")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRestoreConfirm = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // 2. Permissions status
            val requiredGranted = (if (permHasNotification) 1 else 0) +
                (if (permHasExactAlarm) 1 else 0) +
                (if (permHasUsageStats) 1 else 0) +
                (if (permHasDndAccess) 1 else 0) +
                (if (Build.VERSION.SDK_INT >= 34 && permHasFullScreenIntent) 1 else 0)
            val totalRequired = 4 + if (Build.VERSION.SDK_INT >= 34) 1 else 0
            val allGranted = requiredGranted == totalRequired
            val statusColor = if (allGranted) Color(0xFF4CAF50) else Color(0xFFE53935)

            SettingsCard(title = "PERMISSIONS") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (allGranted) Icons.Default.CheckCircle else Icons.Default.Close,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$requiredGranted/$totalRequired required permissions granted",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    PermissionItem(
                        title = "Notifications",
                        description = "Task reminders and pomodoro alerts",
                        icon = Icons.Default.Notifications,
                        isGranted = permHasNotification,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (notificationPermanentlyDenied) {
                                    context.startActivity(
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = android.net.Uri.parse("package:${context.packageName}")
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                    )
                                } else {
                                    permNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        },
                        buttonText = if (notificationPermanentlyDenied) "SETTINGS" else "GRANT"
                    )

                    PermissionItem(
                        title = "Exact Alarms",
                        description = "Precise timer and event notifications",
                        icon = Icons.Default.Alarm,
                        isGranted = permHasExactAlarm,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                try {
                                    context.startActivity(
                                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                            data = android.net.Uri.parse("package:${context.packageName}")
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                    )
                                } catch (_: Exception) { }
                            }
                        }
                    )

                    PermissionItem(
                        title = "Usage Access",
                        description = "Screen time tracking in stats",
                        icon = Icons.Default.Analytics,
                        isGranted = permHasUsageStats,
                        onClick = {
                            try {
                                context.startActivity(
                                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                )
                            } catch (_: Exception) { }
                        }
                    )

                    PermissionItem(
                        title = "Do Not Disturb",
                        description = "Pomodoro DND management",
                        icon = Icons.Default.DoNotDisturb,
                        isGranted = permHasDndAccess,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                try {
                                    context.startActivity(
                                        Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                    )
                                } catch (_: Exception) { }
                            }
                        }
                    )

                    if (Build.VERSION.SDK_INT >= 34) {
                        PermissionItem(
                            title = "Full-Screen Alerts",
                            description = "Pomodoro completion screen automatically",
                            icon = Icons.Default.OpenInFull,
                            isGranted = permHasFullScreenIntent,
                            onClick = {
                                try {
                                    context.startActivity(
                                        Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                                            data = android.net.Uri.parse("package:${context.packageName}")
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                    )
                                } catch (_: Exception) { }
                            }
                        )
                    }

                    PermissionItem(
                        title = "Backup Storage (Optional)",
                        description = "Folder for automated backups",
                        icon = Icons.Default.Backup,
                        isGranted = backupLocationUri != null,
                        onClick = { folderPickerLauncher.launch(null) },
                        buttonText = "CHOOSE"
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        PermissionItem(
                            title = "All Files Access (Optional)",
                            description = "Fallback when SAF is unavailable",
                            icon = Icons.Default.Folder,
                            isGranted = permHasManageStorage,
                            onClick = {
                                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                    data = android.net.Uri.parse("package:${context.packageName}")
                                }
                                try {
                                    permManageStorageLauncher.launch(intent)
                                } catch (_: Exception) { }
                            },
                            buttonText = "GRANT"
                        )
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PermissionItem(
                            title = "Battery Optimization (Recommended)",
                            description = "Stops system from killing timer in background",
                            icon = Icons.Default.Settings,
                            isGranted = permHasBatteryOpt,
                            onClick = {
                                try {
                                    val intent = Intent(
                                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                    ).apply {
                                        data = android.net.Uri.parse("package:${context.packageName}")
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    batteryOptLauncher.launch(intent)
                                } catch (_: Exception) { }
                            },
                            buttonText = if (permHasBatteryOpt) "OK" else "WHITELIST"
                        )
                    }
                }

            // 3. Timer & Focus
            SettingsCard(title = "TIMER & FOCUS") {
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
                            text = "Turn on DND when timer starts",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = enteredDndEnabled,
                        onCheckedChange = { checked ->
                            if (checked && !notificationManager.isNotificationPolicyAccessGranted) {
                                val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                context.startActivity(intent)
                            } else {
                                enteredDndEnabled = checked; dirty = true
                            }
                        }
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                // Pomodoro sound toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Play sound", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Switch(
                        checked = enteredPomodoroRingtoneEnabled,
                        onCheckedChange = { enteredPomodoroRingtoneEnabled = it; dirty = true }
                    )
                }
                // Ringtone selector
                val ringtoneName = if (enteredPomodoroRingtoneUri.isBlank()) "Default ringtone"
                    else try {
                        val rt = RingtoneManager.getRingtone(context, android.net.Uri.parse(enteredPomodoroRingtoneUri))
                        rt?.getTitle(context) ?: "Custom ringtone"
                    } catch (e: Exception) { "Custom ringtone" }
                val ringtoneLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    result.data?.getParcelableExtra<android.net.Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)?.let { uri ->
                        enteredPomodoroRingtoneUri = uri.toString(); dirty = true
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ringtoneName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (enteredPomodoroRingtoneEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                    TextButton(
                        onClick = {
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Pomodoro Alarm")
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false)
                                if (enteredPomodoroRingtoneUri.isNotBlank()) {
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, android.net.Uri.parse(enteredPomodoroRingtoneUri))
                                }
                            }
                            ringtoneLauncher.launch(intent)
                        },
                        enabled = enteredPomodoroRingtoneEnabled
                    ) {
                        Text("Change", fontSize = 12.sp)
                    }
                }
                // Vibrate toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Vibrate", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Switch(
                        checked = enteredPomodoroVibrateEnabled,
                        onCheckedChange = { enteredPomodoroVibrateEnabled = it; dirty = true }
                    )
                }
                AnimatedVisibility(visible = enteredPomodoroVibrateEnabled) {
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        Text(
                            text = "Vibration Pattern",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            com.example.ui.viewmodel.MainViewModel.VIBRATION_PRESETS.forEach { preset ->
                                FilterChip(
                                    selected = enteredPomodoroVibratePattern == preset.name,
                                    onClick = { enteredPomodoroVibratePattern = preset.name; dirty = true },
                                    label = { Text(preset.displayName, fontSize = 10.sp) },
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Default Break (min)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedTextField(
                        value = enteredDefaultBreakMinutes,
                        onValueChange = { newVal ->
                            val filtered = newVal.filter { it.isDigit() }
                            if (filtered.isNotEmpty()) {
                                val num = filtered.toIntOrNull() ?: return@OutlinedTextField
                                if (num in 0..30) {
                                    enteredDefaultBreakMinutes = filtered
                                    viewModel.updateDefaultBreakMinutes(num)
                                }
                            } else {
                                enteredDefaultBreakMinutes = ""
                            }
                        },
                        modifier = Modifier.width(80.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                    )
                }
                TextButton(onClick = {
                    viewModel.updatePomodoroRingtoneUri(enteredPomodoroRingtoneUri)
                    viewModel.updatePomodoroRingtoneEnabled(enteredPomodoroRingtoneEnabled)
                    viewModel.updatePomodoroVibrateEnabled(enteredPomodoroVibrateEnabled)
                    viewModel.updatePomodoroVibratePattern(enteredPomodoroVibratePattern)
                    viewModel.testPomodoroAlarm(context)
                }) {
                    Text("Test Alarm", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            // 3. Daily Reminders
            SettingsCard(title = "DAILY REMINDERS") {
                ReminderItem(
                    icon = Icons.Default.RateReview,
                    title = "Day Review",
                    subtitle = "Remind to review your day",
                    enabled = enteredReviewEnabled,
                    onEnabledChange = onReminderToggle { enteredReviewEnabled = it },
                    time = enteredReviewTime,
                    showDetails = enteredReviewEnabled,
                    onTimeClick = { showReviewTimePicker = true },
                    onTestClick = {
                        val intent = Intent(context, com.example.core.receiver.ReminderReceiver::class.java).apply {
                            action = "com.example.action.DAY_REVIEW"
                            putExtra("title", "Day Review Reminder")
                            putExtra("message", "Time to review your day!")
                        }
                        context.sendBroadcast(intent)
                    }
                )
                ReminderDivider()
                ReminderItem(
                    icon = Icons.Default.CalendarMonth,
                    title = "Morning Planner",
                    subtitle = "Today's task summary",
                    enabled = enteredPlannerEnabled,
                    onEnabledChange = onReminderToggle { enteredPlannerEnabled = it },
                    time = enteredPlannerTime,
                    showDetails = enteredPlannerEnabled,
                    onTimeClick = { showPlannerTimePicker = true },
                    onTestClick = { viewModel.sendImmediatePlannerReminderNotification(context) }
                )
                ReminderDivider()
                ReminderItem(
                    icon = Icons.Default.CheckCircle,
                    title = "Habits Check-in",
                    subtitle = "Missed habits list",
                    enabled = enteredHabitsEnabled,
                    onEnabledChange = onReminderToggle { enteredHabitsEnabled = it },
                    time = enteredHabitsTime,
                    showDetails = enteredHabitsEnabled,
                    onTimeClick = { showHabitsTimePicker = true },
                    onTestClick = { viewModel.sendImmediateHabitsReminderNotification(context) }
                )
                ReminderDivider()
                ReminderItem(
                    icon = Icons.Default.MenuBook,
                    title = "Diary",
                    subtitle = "Remind to write in your diary",
                    enabled = enteredDiaryEnabled,
                    onEnabledChange = onReminderToggle { enteredDiaryEnabled = it },
                    time = enteredDiaryTime,
                    showDetails = enteredDiaryEnabled,
                    onTimeClick = { showDiaryTimePicker = true },
                    onTestClick = { viewModel.sendImmediateDiaryReminderNotification(context) }
                )
                ReminderDivider()
                ReminderItem(
                    icon = Icons.Default.Bedtime,
                    title = "Sleep Log",
                    subtitle = "Remind to log your sleep",
                    enabled = enteredSleepEnabled,
                    onEnabledChange = onReminderToggle { enteredSleepEnabled = it },
                    time = enteredSleepTime,
                    showDetails = enteredSleepEnabled,
                    onTimeClick = { showSleepTimePicker = true },
                    onTestClick = { viewModel.sendImmediateSleepReminderNotification(context) }
                )
                ReminderDivider()
                ReminderItem(
                    icon = Icons.Default.Schedule,
                    title = "Tomorrow Planner",
                    subtitle = "Preview of tomorrow's schedule",
                    enabled = enteredTomorrowPlannerEnabled,
                    onEnabledChange = onReminderToggle { enteredTomorrowPlannerEnabled = it },
                    time = enteredTomorrowPlannerTime,
                    showDetails = enteredTomorrowPlannerEnabled,
                    onTimeClick = { showTomorrowPlannerTimePicker = true },
                    onTestClick = { viewModel.sendImmediateTomorrowPlannerReminderNotification(context) }
                )
                ReminderDivider()
                ReminderItem(
                    icon = Icons.Default.MenuBook,
                    title = "Learn Review",
                    subtitle = "Remind about pending learn reviews",
                    enabled = enteredLearnReviewReminderEnabled,
                    onEnabledChange = onReminderToggle { enteredLearnReviewReminderEnabled = it },
                    time = enteredLearnReviewReminderTime,
                    showDetails = enteredLearnReviewReminderEnabled,
                    onTimeClick = { showLearnReviewReminderTimePicker = true },
                    onTestClick = { viewModel.sendImmediateLearnReviewReminderNotification(context) }
                )
            }

            // 4. Event Notifications
            SettingsCard(title = "EVENT NOTIFICATIONS") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Event Reminders", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Switch(
                        checked = enteredEventEnabled,
                        onCheckedChange = { enteredEventEnabled = it; dirty = true }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Vibrate on Reminder", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Switch(
                        checked = enteredEventVibrate,
                        onCheckedChange = { enteredEventVibrate = it; dirty = true }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Play Sound on Reminder", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Switch(
                        checked = enteredEventSound,
                        onCheckedChange = { enteredEventSound = it; dirty = true }
                    )
                }
            }

            // 5. Auto-Reschedule
            SettingsCard(title = "AUTO-RESCHEDULE") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Reschedule Unfinished", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Move unfinished tasks & notes to the next day", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    val autoRescheduleEnabled by viewModel.autoRescheduleUnfinished.collectAsState()
                    Switch(
                        checked = autoRescheduleEnabled,
                        onCheckedChange = { viewModel.updateAutoRescheduleUnfinished(it) }
                    )
                }
            }

            // 6. More Screen
            SettingsCard(title = "MORE SCREEN") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show Daily Motto", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    val mottoEnabled by viewModel.mottoEnabled.collectAsState()
                    Switch(
                        checked = mottoEnabled,
                        onCheckedChange = { viewModel.updateMottoEnabled(it) }
                    )
                }
            }

            // 7. Calendar
            SettingsCard(title = "CALENDAR") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Default Calendar Format",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Choose the default calendar for date pickers across all screens",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, topEnd = 0.dp, bottomEnd = 0.dp)
                        Button(
                            onClick = { if (!usePersianCalendar) viewModel.toggleUsePersianCalendar() },
                            shape = shape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (usePersianCalendar) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (usePersianCalendar) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.height(44.dp).width(88.dp),
                            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 8.dp)
                        ) {
                            Text("FA", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Button(
                            onClick = { if (usePersianCalendar) viewModel.toggleUsePersianCalendar() },
                            shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 10.dp, bottomEnd = 10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!usePersianCalendar) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (!usePersianCalendar) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.height(44.dp).width(88.dp),
                            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 8.dp)
                        ) {
                            Text("EN", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Currently: ${if (usePersianCalendar) "Persian (Shamsi)" else "Western (Gregorian)"}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            // Creator credit
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Created by",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "Alireza Sotoodeh",
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom bar
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { onDismiss() }) {
                    Text("CLOSE", color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        viewModel.updateBackupEnabled(enteredBackupEnabled)
                        viewModel.updateBackupTime(enteredBackupTime)
                        viewModel.updateBackupFailureNotify(enteredBackupFailureNotify)
                        viewModel.updateDndEnabled(enteredDndEnabled)
                        viewModel.updateEventReminderVibrate(enteredEventVibrate)
                        viewModel.updateEventReminderSound(enteredEventSound)
                        viewModel.updateEventReminderEnabled(enteredEventEnabled)
                        viewModel.updatePomodoroRingtoneUri(enteredPomodoroRingtoneUri)
                        viewModel.updatePomodoroRingtoneEnabled(enteredPomodoroRingtoneEnabled)
                        viewModel.updatePomodoroVibrateEnabled(enteredPomodoroVibrateEnabled)
                        viewModel.updatePomodoroVibratePattern(enteredPomodoroVibratePattern)
                        viewModel.updateReviewReminderTime(enteredReviewTime)
                        viewModel.updateReviewReminderEnabled(enteredReviewEnabled)
                        viewModel.updateSleepReminderTime(enteredSleepTime)
                        viewModel.updateSleepReminderEnabled(enteredSleepEnabled)
                        viewModel.updateDiaryReminderTime(enteredDiaryTime)
                        viewModel.updateDiaryReminderEnabled(enteredDiaryEnabled)
                        viewModel.updatePlannerReminderTime(enteredPlannerTime)
                        viewModel.updatePlannerReminderEnabled(enteredPlannerEnabled)
                        viewModel.updateHabitsReminderTime(enteredHabitsTime)
                        viewModel.updateHabitsReminderEnabled(enteredHabitsEnabled)
                        viewModel.updateTomorrowPlannerReminderTime(enteredTomorrowPlannerTime)
                        viewModel.updateTomorrowPlannerReminderEnabled(enteredTomorrowPlannerEnabled)
                        viewModel.updateLearnReviewReminderTime(enteredLearnReviewReminderTime)
                        viewModel.updateLearnReviewReminderEnabled(enteredLearnReviewReminderEnabled)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("SAVE & CLOSE")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        } // end scrollable Column
    }

    // Cancel confirmation
    if (showCancelConfirm) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false },
            title = { Text("Discard changes?", fontWeight = FontWeight.Bold) },
            text = { Text("You have unsaved changes. Are you sure you want to discard them?") },
            confirmButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirm = false }) { Text("Keep editing") }
            }
        )
    }

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
            dismissButton = { TextButton(onClick = { showReviewTimePicker = false }) { Text("Cancel") } }
        )
    }

    if (showSleepTimePicker) {
        AlertDialog(
            onDismissRequest = { showSleepTimePicker = false },
            title = { Text("Select Reminder Time", fontWeight = FontWeight.Bold) },
            text = { TimePicker(state = sleepTimePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val hour = sleepTimePickerState.hour; val min = sleepTimePickerState.minute
                    enteredSleepTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, min)
                    showSleepTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showSleepTimePicker = false }) { Text("Cancel") } }
        )
    }

    if (showDiaryTimePicker) {
        AlertDialog(
            onDismissRequest = { showDiaryTimePicker = false },
            title = { Text("Select Reminder Time", fontWeight = FontWeight.Bold) },
            text = { TimePicker(state = diaryTimePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val hour = diaryTimePickerState.hour; val min = diaryTimePickerState.minute
                    enteredDiaryTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, min)
                    showDiaryTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDiaryTimePicker = false }) { Text("Cancel") } }
        )
    }

    if (showPlannerTimePicker) {
        AlertDialog(
            onDismissRequest = { showPlannerTimePicker = false },
            title = { Text("Select Reminder Time", fontWeight = FontWeight.Bold) },
            text = { TimePicker(state = plannerTimePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val hour = plannerTimePickerState.hour; val min = plannerTimePickerState.minute
                    enteredPlannerTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, min)
                    showPlannerTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPlannerTimePicker = false }) { Text("Cancel") } }
        )
    }

    if (showHabitsTimePicker) {
        AlertDialog(
            onDismissRequest = { showHabitsTimePicker = false },
            title = { Text("Select Reminder Time", fontWeight = FontWeight.Bold) },
            text = { TimePicker(state = habitsTimePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val hour = habitsTimePickerState.hour; val min = habitsTimePickerState.minute
                    enteredHabitsTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, min)
                    showHabitsTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showHabitsTimePicker = false }) { Text("Cancel") } }
        )
    }

    if (showTomorrowPlannerTimePicker) {
        AlertDialog(
            onDismissRequest = { showTomorrowPlannerTimePicker = false },
            title = { Text("Select Reminder Time", fontWeight = FontWeight.Bold) },
            text = { TimePicker(state = tomorrowPlannerTimePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val hour = tomorrowPlannerTimePickerState.hour; val min = tomorrowPlannerTimePickerState.minute
                    enteredTomorrowPlannerTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, min)
                    showTomorrowPlannerTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTomorrowPlannerTimePicker = false }) { Text("Cancel") } }
        )
    }

    if (showLearnReviewReminderTimePicker) {
        AlertDialog(
            onDismissRequest = { showLearnReviewReminderTimePicker = false },
            title = { Text("Select Reminder Time", fontWeight = FontWeight.Bold) },
            text = { TimePicker(state = learnReviewReminderTimePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val hour = learnReviewReminderTimePickerState.hour; val min = learnReviewReminderTimePickerState.minute
                    enteredLearnReviewReminderTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, min)
                    showLearnReviewReminderTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showLearnReviewReminderTimePicker = false }) { Text("Cancel") } }
        )
    }

    if (showBackupTimePicker) {
        AlertDialog(
            onDismissRequest = { showBackupTimePicker = false },
            title = { Text("Select Backup Time", fontWeight = FontWeight.Bold) },
            text = { TimePicker(state = backupTimePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val hour = backupTimePickerState.hour; val min = backupTimePickerState.minute
                    enteredBackupTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, min)
                    showBackupTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showBackupTimePicker = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
private fun ReminderItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    time: String,
    showDetails: Boolean,
    onTimeClick: () -> Unit,
    onTestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange
            )
        }
        AnimatedVisibility(visible = enabled && showDetails) {
            Column(modifier = Modifier.padding(start = 30.dp, top = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Reminder Time:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    TextButton(onClick = onTimeClick) {
                        Text(time, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                TextButton(onClick = onTestClick) {
                    Text("Test Notification", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun ReminderDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    )
}

private enum class TodoTabFilter { ALL, PENDING, DONE, LINKED, UNLINKED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoTab(viewModel: MainViewModel) {
    val allTodos by viewModel.allTodos.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()

    var filter by remember { mutableStateOf(TodoTabFilter.UNLINKED) }
    var editingTodo by remember { mutableStateOf<TodoEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<TodoEntity?>(null) }
    var todoForLinking by remember { mutableStateOf<TodoEntity?>(null) }
    var todoForMovingToPlanner by remember { mutableStateOf<TodoEntity?>(null) }
    var showUnlinkConfirm by remember { mutableStateOf<TodoEntity?>(null) }
    var showPendingDetailsDialog by remember { mutableStateOf(false) }
    val expandAllDescriptions by viewModel.expandAllDescriptions.collectAsState()

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
        TodoTabFilter.LINKED -> allRootTodos.filter { it.linkedTaskId != null }
        TodoTabFilter.UNLINKED -> allRootTodos.filter { it.linkedTaskId == null }
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TodoTabFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { filter = f },
                        label = { Text(f.name, fontSize = 10.sp) },
                        modifier = Modifier.height(26.dp)
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
                        IconButton(
                            onClick = { viewModel.toggleExpandAllDescriptions() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (expandAllDescriptions) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (expandAllDescriptions) "Collapse All" else "Expand All",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
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
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
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
                                                TodoTabFilter.LINKED -> allRootTodos.filter { it.linkedTaskId != null }
                                                TodoTabFilter.UNLINKED -> allRootTodos.filter { it.linkedTaskId == null }
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
            initialSubtasks = allTodos.filter { it.parentTodoId == todo.id }
                .map { TaskEntity(title = it.title, date = "", subtaskImportance = it.subtaskImportance) },
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
                    val completedSubTodos = subTodos.count { it.status == "DONE" }
                    val totalSubTodos = subTodos.size
                    val subProgress = if (totalSubTodos > 0) completedSubTodos.toFloat() / totalSubTodos else 0f
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .then(
                                if (subTodos.isNotEmpty()) Modifier.clickable { onToggleSubTodosExpanded() }
                                else Modifier
                            )
                    ) {
                        PriorityBadge(todo.priority)
                        if (subTodos.isNotEmpty()) {
                                Spacer(Modifier.width(6.dp))
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
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "${(subProgress * 100).toInt()}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (expanded || expandedSubTodos) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle Subtasks",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                Spacer(modifier = Modifier.height(8.dp))
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
                                val importanceIcon = when (subTodo.subtaskImportance) {
                                    "IMPORTANT" -> "⭐ "
                                    "OPTIONAL" -> "☕ "
                                    else -> ""
                                }
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
                                            text = "$importanceIcon${subTodo.title}",
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
        CalendarDatePickerDialog(
            initialSelectedDate = date,
            onDismiss = { showDatePicker = false },
            onDateSelected = { d ->
                date = d
                showDatePicker = false
            }
        )
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
        CalendarDatePickerDialog(
            initialSelectedDate = date,
            onDismiss = { showDatePicker = false },
            onDateSelected = { d ->
                date = d
                showDatePicker = false
            }
        )
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
    val expandAllIdeas by viewModel.expandAllIdeas.collectAsState()
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
                            onClick = { viewModel.toggleExpandAllIdeas() },
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
        val ideaStages by viewModel.stagesForIdea(idea.id).collectAsState(initial = null)
        ideaStages?.let { loadedStages ->
            com.example.ui.components.TaskManagerDialog(
                viewModel = viewModel,
                initialDate = "",
                ideaToEdit = idea,
                initialIdeaStages = loadedStages,
                ideaGroups = groups,
                onDismiss = { editingIdea = null }
            )
        }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LearnGroupChipRow(
    groups: List<LearnGroupEntity>,
    selectedGroupId: Long?,
    onGroupSelected: (Long?) -> Unit,
    onEditGroup: (LearnGroupEntity) -> Unit,
    onDeleteGroup: (LearnGroupEntity) -> Unit,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    inProgressCount: Int,
    plannedCount: Int,
    completedCount: Int
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = statusFilter == "in_progress",
                    onClick = { onStatusFilterChange("in_progress") },
                    label = { Text("In Progress ($inProgressCount)", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = statusFilter == "planned",
                    onClick = { onStatusFilterChange("planned") },
                    label = { Text("Planned ($plannedCount)", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = statusFilter == "completed",
                    onClick = { onStatusFilterChange("completed") },
                    label = { Text("Completed ($completedCount)", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = statusFilter == "all",
                    onClick = { onStatusFilterChange("all") },
                    label = { Text("All", fontSize = 12.sp) }
                )
            }
        }
        if (groups.isNotEmpty()) {
            item {
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
            }
            item {
                FilterChip(
                    selected = selectedGroupId == null,
                    onClick = { onGroupSelected(null) },
                    label = { Text("All", fontSize = 12.sp) }
                )
            }
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

@Composable
private fun CreateLearnGroupDialog(
    initialName: String? = null,
    initialColor: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Long) -> Unit
) {
    var name by remember { mutableStateOf(initialName ?: "") }
    var selectedColor by remember { mutableStateOf(initialColor ?: 0xFF4CAF50) }
    val presetColors = listOf(
        0xFF4CAF50, 0xFF2196F3, 0xFFFF9800, 0xFFE91E63,
        0xFF9C27B0, 0xFF00BCD4, 0xFFFF5722, 0xFF607D8B
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialName != null) "Edit Group" else "New Group") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Color", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presetColors.forEach { c ->
                        val isSelected = selectedColor == c
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .border(if (isSelected) 2.dp else 0.dp, if (isSelected) MaterialTheme.colorScheme.onSurface else androidx.compose.ui.graphics.Color.Transparent, CircleShape)
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
                IconButton(onClick = { onAddToPlanner(idea) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Add to Planner", modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
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
            val stageImportanceIcon = when (stage.importance) {
                "IMPORTANT" -> "⭐"
                "OPTIONAL" -> "☕"
                else -> ""
            }
            Text(
                text = if (stageImportanceIcon.isNotEmpty()) "$stageImportanceIcon ${stage.title}" else stage.title,
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
        CalendarDatePickerDialog(
            initialSelectedDate = date,
            onDismiss = { showDatePicker = false },
            onDateSelected = { d ->
                date = d
                showDatePicker = false
            }
        )
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

@Composable
fun LearnTab(viewModel: MainViewModel) {
    val learnItems by viewModel.learnItems.collectAsState()
    val learnGroups by viewModel.learnGroups.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val todayDate by viewModel.todayDate.collectAsState()
    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<LearnItemEntity?>(null) }
    var itemToStart by remember { mutableStateOf<LearnItemEntity?>(null) }
    var editingGroup by remember { mutableStateOf<LearnGroupEntity?>(null) }
    var showDeleteGroupConfirm by remember { mutableStateOf<LearnGroupEntity?>(null) }
    var showGroupChips by remember { mutableStateOf(true) }
    var showLearnBreakdown by remember { mutableStateOf(false) }
    var statusFilter by remember { mutableStateOf("planned") }
    val expandAllLearnItems by viewModel.expandAllLearnItems.collectAsState()

    var draggingLearnItemId by remember { mutableStateOf<Long?>(null) }
    var dragOffsetX by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var dragOffsetY by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var draggedLearnItems by remember { mutableStateOf<List<LearnItemEntity>?>(null) }
    val learnItemHeights = remember { androidx.compose.runtime.mutableStateMapOf<Long, Int>() }
    val densityL = LocalDensity.current

    val groupChipScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                if (available.y < -15) showGroupChips = false
                else if (available.y > 15) showGroupChips = true
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    val inProgressCount = learnItems.count { it.status == "ACTIVE" || it.status == "PAUSED" }
    val plannedCount = learnItems.count { it.status == "NOT_STARTED" }
    val completedCount = learnItems.count { it.status == "COMPLETED" }
    val statusFiltered = when (statusFilter) {
        "in_progress" -> learnItems.filter { it.status == "ACTIVE" || it.status == "PAUSED" }
        "planned" -> learnItems.filter { it.status == "NOT_STARTED" }
        "completed" -> learnItems.filter { it.status == "COMPLETED" }
        else -> learnItems
    }
    val filteredItems = if (selectedGroupId == null) statusFiltered
    else statusFiltered.filter { it.groupId == selectedGroupId }
    val displayItems = draggedLearnItems ?: filteredItems

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).nestedScroll(groupChipScrollConnection)
        ) {
            AnimatedVisibility(
                visible = showGroupChips,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                LearnGroupChipRow(
                    groups = learnGroups,
                    selectedGroupId = selectedGroupId,
                    onGroupSelected = { selectedGroupId = it },
                    onEditGroup = { editingGroup = it },
                    onDeleteGroup = { showDeleteGroupConfirm = it },
                    statusFilter = statusFilter,
                    onStatusFilterChange = { statusFilter = it },
                    inProgressCount = inProgressCount,
                    plannedCount = plannedCount,
                    completedCount = completedCount
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
                            text = "LEARN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.5.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleExpandAllLearnItems() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (expandAllLearnItems) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (expandAllLearnItems) "Collapse All" else "Expand All",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Box {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { showLearnBreakdown = !showLearnBreakdown }
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${filteredItems.size} item${if (filteredItems.size != 1) "s" else ""}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (showLearnBreakdown) {
                                val localDensity = LocalDensity.current
                                val offsetY = with(localDensity) { 32.dp.roundToPx() }
                                Popup(
                                    alignment = Alignment.TopEnd,
                                    offset = IntOffset(x = 0, y = offsetY),
                                    onDismissRequest = { showLearnBreakdown = false },
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
                                                text = "Items by Group",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            if (learnItems.isEmpty()) {
                                                Text(
                                                    text = "No learn items yet",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            } else {
                                                val itemsByGroup = learnItems.groupBy { it.groupId }
                                                val groupsWithItems = learnGroups.filter { group ->
                                                    itemsByGroup[group.id]?.isNotEmpty() == true
                                                }
                                                val ungroupedCount = itemsByGroup[null]?.size ?: 0
                                                groupsWithItems.forEach { group ->
                                                    val count = itemsByGroup[group.id]?.size ?: 0
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
                                                            text = "${count} item${if (count != 1) "s" else ""}",
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                if (ungroupedCount > 0) {
                                                    if (groupsWithItems.isNotEmpty()) {
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
                                                            text = "${ungroupedCount} item${if (ungroupedCount != 1) "s" else ""}",
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
                        if (filteredItems.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No learn items yet.\nTap + to add a book or course.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 80.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(displayItems, key = { it.id }) { item ->
                                    val isDragging = draggingLearnItemId == item.id
                                    val isCompleted = item.status == "COMPLETED"
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .animateItem()
                                            .onGloballyPositioned { learnItemHeights[item.id] = it.size.height }
                                    ) {
                                        LearnItemCard(
                                            item = item,
                                            expanded = expandAllLearnItems,
                                            isDragging = isDragging,
                                            dragOffsetX = if (isDragging) dragOffsetX else 0f,
                                            dragOffsetY = if (isDragging) dragOffsetY else 0f,
                                            viewModel = viewModel,
                                            allTasks = allTasks,
                                            todayDate = todayDate,
                                            onEdit = { itemToEdit = item },
                                            onStart = { itemToStart = item },
                                            onDelete = { viewModel.deleteLearnItemWithUndo(item) },
                                            onDragStart = if (!isCompleted) {
                                                {
                                                    draggingLearnItemId = item.id
                                                    dragOffsetX = 0f
                                                    dragOffsetY = 0f
                                                    draggedLearnItems = filteredItems.toList()
                                                }
                                            } else null,
                                            onDrag = if (!isCompleted) {
                                                { amount ->
                                                    if (draggingLearnItemId == item.id) {
                                                        dragOffsetX += amount.x
                                                        dragOffsetY += amount.y
                                                        val currentList = draggedLearnItems
                                                        if (currentList != null) {
                                                            val draggedIndex = currentList.indexOfFirst { it.id == item.id }
                                                            if (draggedIndex != -1) {
                                                                val spacing = with(densityL) { 8.dp.toPx() }
                                                                if (dragOffsetY > 0 && draggedIndex < currentList.size - 1) {
                                                                    val nextItem = currentList[draggedIndex + 1]
                                                                    val nextHeight = learnItemHeights[nextItem.id] ?: 150
                                                                    val threshold = nextHeight / 2f + spacing
                                                                    if (dragOffsetY > threshold) {
                                                                        val mutable = currentList.toMutableList()
                                                                        mutable.removeAt(draggedIndex)
                                                                        mutable.add(draggedIndex + 1, item)
                                                                        draggedLearnItems = mutable
                                                                        dragOffsetY -= (nextHeight + spacing)
                                                                    }
                                                                } else if (dragOffsetY < 0 && draggedIndex > 0) {
                                                                    val prevItem = currentList[draggedIndex - 1]
                                                                    val prevHeight = learnItemHeights[prevItem.id] ?: 150
                                                                    val threshold = -(prevHeight / 2f) - spacing
                                                                    if (dragOffsetY < threshold) {
                                                                        val mutable = currentList.toMutableList()
                                                                        mutable.removeAt(draggedIndex)
                                                                        mutable.add(draggedIndex - 1, item)
                                                                        draggedLearnItems = mutable
                                                                        dragOffsetY += (prevHeight + spacing)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            } else null,
                                            onDragEnd = if (!isCompleted) {
                                                {
                                                    if (draggingLearnItemId == item.id) {
                                                        val originalIndex = filteredItems.indexOfFirst { it.id == item.id }
                                                        val currentList = draggedLearnItems
                                                        if (currentList != null && originalIndex != -1) {
                                                            val finalIndex = currentList.indexOfFirst { it.id == item.id }
                                                            val deltaIndex = finalIndex - originalIndex
                                                            if (deltaIndex != 0) {
                                                                viewModel.reorderLearnItem(item, filteredItems, deltaIndex)
                                                            }
                                                        }
                                                        draggingLearnItemId = null
                                                        draggedLearnItems = null
                                                        dragOffsetX = 0f
                                                        dragOffsetY = 0f
                                                    }
                                                }
                                            } else null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.background,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Create learn item")
        }
    }

    editingGroup?.let { group ->
        CreateLearnGroupDialog(
            initialName = group.name,
            initialColor = group.color,
            onDismiss = { editingGroup = null },
            onConfirm = { name, color ->
                viewModel.updateLearnGroup(group.copy(name = name, color = color))
                editingGroup = null
            }
        )
    }

    showDeleteGroupConfirm?.let { group ->
        AlertDialog(
            onDismissRequest = { showDeleteGroupConfirm = null },
            title = { Text("Delete group?") },
            text = { Text("All learn items in \"${group.name}\" will also be deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteLearnGroup(group)
                    if (selectedGroupId == group.id) selectedGroupId = null
                    showDeleteGroupConfirm = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteGroupConfirm = null }) { Text("Cancel") }
            }
        )
    }

    if (showCreateDialog) {
        LearnItemDialog(
            viewModel = viewModel,
            existingItem = null,
            onDismiss = { showCreateDialog = false }
        )
    }

    itemToEdit?.let { item ->
        LearnItemDialog(
            viewModel = viewModel,
            existingItem = item,
            onDismiss = { itemToEdit = null }
        )
    }

    itemToStart?.let { item ->
        StartLearningDialog(
            viewModel = viewModel,
            learnItem = item,
            onDismiss = { itemToStart = null }
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun LearnItemCard(
    item: LearnItemEntity,
    expanded: Boolean = true,
    isDragging: Boolean = false,
    dragOffsetX: Float = 0f,
    dragOffsetY: Float = 0f,
    viewModel: MainViewModel,
    allTasks: List<TaskEntity>,
    todayDate: String,
    onEdit: () -> Unit,
    onStart: () -> Unit,
    onDelete: () -> Unit,
    onDragStart: (() -> Unit)? = null,
    onDrag: ((androidx.compose.ui.geometry.Offset) -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null,
) {
    var showMenu by remember { mutableStateOf(false) }
    val sections by viewModel.sectionsForLearnItem(item.id).collectAsState(initial = emptyList())
    val learnGroups by viewModel.learnGroups.collectAsState()
    val groupColor = remember(item.groupId, learnGroups) {
        learnGroups.find { it.id == item.groupId }?.color?.let { Color(it) }
    }
    val masteredCount = sections.count { it.status == "MASTERED" }
    val inReviewCount = sections.count { it.status == "IN_REVIEW" }
    val studiedCount = sections.count { it.status == "STUDIED" }
    val notStartedCount = sections.count { it.status == "NOT_STARTED" }
    val pausedDaysAgo = remember(item.pausedAt) {
        if (item.pausedAt > 0) {
            ((System.currentTimeMillis() - item.pausedAt) / (1000 * 60 * 60 * 24)).toInt()
        } else 0
    }
    val isCompleted = item.status == "COMPLETED"

    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isDragging) 1.04f else 1.0f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessLow),
        label = "scale"
    )
    val elevation by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isDragging) 12.dp else 0.dp,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessLow),
        label = "elevation"
    )
    val cardContainerColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isDragging) MaterialTheme.colorScheme.surfaceVariant
        else if (item.status == "PAUSED") MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(200),
        label = "bgColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isDragging) Modifier.zIndex(10f) else Modifier)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    shadowElevation = elevation.toPx()
                }
                .offset { IntOffset(dragOffsetX.roundToInt(), dragOffsetY.roundToInt()) }
                .combinedClickable(
                    onClick = {},
                    onLongClick = { showMenu = true }
                )
                .then(
                    if (onDragStart != null && !isCompleted) {
                        Modifier.pointerInput(item.id) {
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
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = cardContainerColor),
            border = groupColor?.let { BorderStroke(2.dp, it) }
                ?: BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // === HEADER: icon + type badge + group badge + title + priority + status ===
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Color(0xFFFFB300)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val typeColor = if (item.type == "BOOK") Color(0xFFFFB300) else Color(0xFF2196F3)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = typeColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = item.type,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = typeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                if (item.groupId != null) {
                    val group = learnGroups.find { it.id == item.groupId }
                    if (group != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(group.color).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = group.name,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(group.color),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                val priorityColor = when (item.priorityLevel) {
                    "High" -> Color(0xFFE53935)
                    "Medium" -> Color(0xFFFFB300)
                    else -> Color(0xFF4CAF50)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.priorityLevel.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = priorityColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (item.status) {
                        "NOT_STARTED" -> "Not Started"
                        "ACTIVE" -> "Active"
                        "PAUSED" -> "Paused — ${pausedDaysAgo}d ago"
                        "COMPLETED" -> "Completed"
                        else -> item.status
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when (item.status) {
                        "NOT_STARTED" -> MaterialTheme.colorScheme.onSurfaceVariant
                        "ACTIVE" -> Color(0xFFFFB300)
                        "PAUSED" -> MaterialTheme.colorScheme.onSurfaceVariant
                        "COMPLETED" -> Color(0xFF4CAF50)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            // === PROGRESS: mastered count + progress bar ===
            if (sections.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                val totalSections = sections.size
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "$masteredCount/$totalSections mastered",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (masteredCount == totalSections) Color(0xFF4CAF50) else Color(0xFFFFB300)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { if (totalSections > 0) masteredCount.toFloat() / totalSections else 0f },
                        modifier = Modifier.weight(1f).height(6.dp),
                        color = Color(0xFF4CAF50),
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                }
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        // === SECTION BREAKDOWN (ACTIVE/PAUSED) ===
                        if (item.status == "ACTIVE" || item.status == "PAUSED") {
                            val muted = item.status == "PAUSED"
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (notStartedCount > 0) {
                                    Text(
                                        text = "$notStartedCount to study",
                                        fontSize = 10.sp,
                                        color = if (muted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                                if (studiedCount > 0) {
                                    Text(
                                        text = "$studiedCount reviewing",
                                        fontSize = 10.sp,
                                        color = if (muted) Color(0xFFFFB300).copy(alpha = 0.3f)
                                        else Color(0xFFFFB300).copy(alpha = 0.6f)
                                    )
                                }
                                if (inReviewCount > 0) {
                                    Text(
                                        text = "$inReviewCount to review",
                                        fontSize = 10.sp,
                                        color = if (muted) Color(0xFFFFB300).copy(alpha = 0.3f)
                                        else Color(0xFFFFB300)
                                    )
                                }
                                if (masteredCount > 0) {
                                    Text(
                                        text = "$masteredCount mastered",
                                        fontSize = 10.sp,
                                        color = if (muted) Color(0xFF4CAF50).copy(alpha = 0.5f)
                                        else Color(0xFF4CAF50)
                                    )
                                }
                            }
                            if (item.status == "ACTIVE") {
                                val remaining = totalSections - masteredCount
                                val daysEstimate = kotlin.math.ceil(remaining.toFloat() / item.sectionsPerDay.coerceAtLeast(1)).toInt()
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "~${daysEstimate}d remaining",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Paused — ${pausedDaysAgo}d ago",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                )
                            }
                        }
                        // === TODAY'S PENDING TASKS (ACTIVE only) ===
                        if (item.status == "ACTIVE") {
                            val sectionIds = sections.map { it.id }.toSet()
                            val pendingToday = allTasks.filter { task ->
                                task.linkedLearnSectionId in sectionIds &&
                                task.date == todayDate &&
                                task.status == "PENDING"
                            }
                            val studyTasksToday = pendingToday.count { it.label == "Study" }
                            val reviewTasksToday = pendingToday.count { it.label == "Review" }
                            if (studyTasksToday > 0 || reviewTasksToday > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (studyTasksToday > 0) {
                                        Icon(
                                            Icons.Default.MenuBook,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = Color(0xFFFFB300)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "$studyTasksToday study today",
                                            fontSize = 11.sp,
                                            color = Color(0xFFFFB300)
                                        )
                                    }
                                    if (studyTasksToday > 0 && reviewTasksToday > 0) {
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }
                                    if (reviewTasksToday > 0) {
                                        Icon(
                                            Icons.Default.Update,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = Color(0xFF2196F3)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "$reviewTasksToday review today",
                                            fontSize = 11.sp,
                                            color = Color(0xFF2196F3)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // === ACTION BUTTONS ===
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                when (item.status) {
                    "NOT_STARTED", "ARCHIVED" -> {
                        TextButton(onClick = onStart) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start", fontSize = 12.sp)
                        }
                    }
                    "ACTIVE" -> {
                        TextButton(onClick = { viewModel.pauseLearnItem(item) }) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pause", fontSize = 12.sp)
                        }
                    }
                    "PAUSED" -> {
                        TextButton(onClick = { viewModel.resumeLearnItem(item) }) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resume", fontSize = 12.sp)
                        }
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    }

    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text("Edit") },
            onClick = { showMenu = false; onEdit() },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
        DropdownMenuItem(
            text = { Text("Delete") },
            onClick = { showMenu = false; onDelete() },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
        when (item.status) {
            "ACTIVE" -> DropdownMenuItem(
                text = { Text("Pause") },
                onClick = {
                    showMenu = false
                    viewModel.pauseLearnItem(item)
                },
                leadingIcon = { Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            "PAUSED" -> DropdownMenuItem(
                text = { Text("Resume") },
                onClick = {
                    showMenu = false
                    viewModel.resumeLearnItem(item)
                },
                leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            "NOT_STARTED", "ARCHIVED" -> DropdownMenuItem(
                text = { Text("Archive") },
                onClick = {
                    showMenu = false
                    viewModel.archiveLearnItem(item)
                },
                leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun LearnItemDialog(
    viewModel: MainViewModel,
    existingItem: LearnItemEntity?,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(existingItem?.title ?: "") }
    var type by remember { mutableStateOf(existingItem?.type?.uppercase()?.let { if (it == "BOOK" || it == "COURSE") it else "BOOK" } ?: "BOOK") }
    var totalSections by remember { mutableStateOf(existingItem?.totalSections?.toString() ?: "1") }
    var priorityLevel by remember { mutableStateOf(existingItem?.priorityLevel ?: "Medium") }
    var selectedGroupId by remember { mutableStateOf(existingItem?.groupId) }
    var showNewGroupDialog by remember { mutableStateOf(false) }
    var groupMenuTarget by remember { mutableStateOf<LearnGroupEntity?>(null) }
    var editingGroup by remember { mutableStateOf<LearnGroupEntity?>(null) }
    var showDeleteGroupConfirm by remember { mutableStateOf<LearnGroupEntity?>(null) }
    val learnGroups by viewModel.learnGroups.collectAsState()
    val isEditing = existingItem != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Learn Item" else "New Learn Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Type", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "BOOK",
                        onClick = { type = "BOOK" },
                        label = { Text("Book") }
                    )
                    FilterChip(
                        selected = type == "COURSE",
                        onClick = { type = "COURSE" },
                        label = { Text("Course") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = totalSections,
                    onValueChange = { totalSections = it.filter { c -> c.isDigit() } },
                    label = { Text("Number of ${if (type == "BOOK") "chapters" else "lessons"}") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Priority", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Low", "Medium", "High").forEach { p ->
                        FilterChip(
                            selected = priorityLevel == p,
                            onClick = { priorityLevel = p },
                            label = { Text(p) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Group", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showNewGroupDialog = true }
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                Text("+ New", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }
                    item {
                        val isNone = selectedGroupId == null
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isNone) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedGroupId = null }
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                Text("None", fontSize = 11.sp)
                            }
                        }
                    }
                    items(learnGroups) { group ->
                        val isSelected = selectedGroupId == group.id
                        Box {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(group.color).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .combinedClickable(
                                        onClick = { selectedGroupId = group.id },
                                        onLongClick = { groupMenuTarget = group }
                                    )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Box(modifier = Modifier.size(8.dp).background(Color(group.color), CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(group.name, fontSize = 11.sp, color = if (isSelected) Color(group.color) else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                            DropdownMenu(
                                expanded = groupMenuTarget?.id == group.id,
                                onDismissRequest = { groupMenuTarget = null }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = { editingGroup = group; groupMenuTarget = null }
                                )
                                DropdownMenuItem(
                                    text = { Text("Remove", color = MaterialTheme.colorScheme.error) },
                                    onClick = { showDeleteGroupConfirm = group; groupMenuTarget = null }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val sections = totalSections.toIntOrNull() ?: return@TextButton
                    if (title.isBlank() || sections <= 0) return@TextButton
                    if (isEditing) {
                        existingItem?.let { item ->
                            viewModel.updateLearnItem(
                                item = item,
                                newTitle = title,
                                newType = type,
                                newTotalSections = sections,
                                newPriorityLevel = priorityLevel,
                                newGroupId = selectedGroupId
                            )
                        }
                    } else {
                        viewModel.addLearnItem(
                            title = title,
                            type = type,
                            totalSections = sections,
                            priorityLevel = priorityLevel,
                            groupId = selectedGroupId
                        )
                    }
                    onDismiss()
                },
                enabled = title.isNotBlank() && (totalSections.toIntOrNull() ?: 0) > 0
            ) { Text(if (isEditing) "Save" else "Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showNewGroupDialog) {
        var newGroupName by remember { mutableStateOf("") }
        var newGroupColor by remember { mutableStateOf(0xFF4CAF50) }
        val presetColors = listOf(
            0xFF4CAF50, 0xFF2196F3, 0xFFFF9800, 0xFFE91E63,
            0xFF9C27B0, 0xFF00BCD4, 0xFFFF5722, 0xFF607D8B
        )

        AlertDialog(
            onDismissRequest = { showNewGroupDialog = false },
            title = { Text("New Group") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        label = { Text("Group Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presetColors) { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(color))
                                    .border(2.dp, if (newGroupColor == color) MaterialTheme.colorScheme.onSurface else Color.Transparent, CircleShape)
                                    .clickable { newGroupColor = color }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newGroupName.isNotBlank()) {
                        viewModel.addLearnGroup(newGroupName.trim(), newGroupColor)
                        showNewGroupDialog = false
                    }
                }) { Text("ADD") }
            },
            dismissButton = {
                TextButton(onClick = { showNewGroupDialog = false }) { Text("Cancel") }
            }
        )
    }

    editingGroup?.let { group ->
        var editName by remember(group.id) { mutableStateOf(group.name) }
        var editColor by remember(group.id) { mutableStateOf(group.color) }
        val presetColors = listOf(
            0xFF4CAF50, 0xFF2196F3, 0xFFFF9800, 0xFFE91E63,
            0xFF9C27B0, 0xFF00BCD4, 0xFFFF5722, 0xFF607D8B
        )
        AlertDialog(
            onDismissRequest = { editingGroup = null },
            title = { Text("Edit Group") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Group Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presetColors) { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(color))
                                    .border(2.dp, if (editColor == color) MaterialTheme.colorScheme.onSurface else Color.Transparent, CircleShape)
                                    .clickable { editColor = color }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editName.isNotBlank()) {
                            viewModel.updateLearnGroup(group.copy(name = editName.trim(), color = editColor))
                            editingGroup = null
                        }
                    },
                    enabled = editName.isNotBlank()
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingGroup = null }) { Text("Cancel") }
            }
        )
    }

    showDeleteGroupConfirm?.let { group ->
        AlertDialog(
            onDismissRequest = { showDeleteGroupConfirm = null },
            title = { Text("Delete group?") },
            text = { Text("All learn items in \"${group.name}\" will also be deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    if (selectedGroupId == group.id) selectedGroupId = null
                    viewModel.deleteLearnGroup(group)
                    showDeleteGroupConfirm = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteGroupConfirm = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun StartLearningDialog(
    viewModel: MainViewModel,
    learnItem: LearnItemEntity,
    onDismiss: () -> Unit
) {
    var useDeadline by remember { mutableStateOf(false) }
    var sectionsPerDay by remember { mutableStateOf("1") }
    var deadlineDate by remember { mutableStateOf(viewModel.todayDate.value) }
    var startDate by remember { mutableStateOf(viewModel.todayDate.value) }
    var scheduleMode by remember { mutableStateOf("CONTINUOUS") }
    var scheduleDaysOfWeek by remember { mutableStateOf("") }
    val usePersianCalendar by viewModel.usePersianCalendar.collectAsState()

    fun daysBetweenDates(from: String, to: String): Int {
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return try {
            val fromCal = java.util.Calendar.getInstance().apply { time = fmt.parse(from) ?: return 0 }
            val toCal = java.util.Calendar.getInstance().apply { time = fmt.parse(to) ?: return 0 }
            ((toCal.timeInMillis - fromCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        } catch (_: Exception) { 0 }
    }

    val daysOfWeekList = listOf(
        Pair(java.util.Calendar.SUNDAY, "S"),
        Pair(java.util.Calendar.MONDAY, "M"),
        Pair(java.util.Calendar.TUESDAY, "T"),
        Pair(java.util.Calendar.WEDNESDAY, "W"),
        Pair(java.util.Calendar.THURSDAY, "T"),
        Pair(java.util.Calendar.FRIDAY, "F"),
        Pair(java.util.Calendar.SATURDAY, "S")
    )
    val currentDays = scheduleDaysOfWeek.split(",").filter { it.isNotBlank() }

    fun countAllowedDaysBetween(from: String, to: String, daysOfWeek: String): Int {
        if (daysOfWeek.isBlank()) {
            val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            return try {
                val fromCal = java.util.Calendar.getInstance().apply { time = fmt.parse(from) ?: return 1 }
                val toCal = java.util.Calendar.getInstance().apply { time = fmt.parse(to) ?: return 1 }
                ((toCal.timeInMillis - fromCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
            } catch (_: Exception) { 1 }
        }
        val allowed = daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        if (allowed.isEmpty()) {
            val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            return try {
                val fromCal = java.util.Calendar.getInstance().apply { time = fmt.parse(from) ?: return 1 }
                val toCal = java.util.Calendar.getInstance().apply { time = fmt.parse(to) ?: return 1 }
                ((toCal.timeInMillis - fromCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
            } catch (_: Exception) { 1 }
        }
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance().apply { time = fmt.parse(from) ?: return 1 }
        val endCal = java.util.Calendar.getInstance().apply { time = fmt.parse(to) ?: return 1 }
        var count = 0
        while (!cal.after(endCal)) {
            if (cal.get(java.util.Calendar.DAY_OF_WEEK) in allowed) count++
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        return count.coerceAtLeast(1)
    }

    val effectivePerDay = if (useDeadline) {
        val perDay = sectionsPerDay.toIntOrNull() ?: 1
        perDay
    } else {
        sectionsPerDay.toIntOrNull() ?: 0
    }
    val totalDays = remember(useDeadline, effectivePerDay, scheduleMode, scheduleDaysOfWeek, startDate, deadlineDate) {
        if (scheduleMode == "WEEKLY" && scheduleDaysOfWeek.isNotBlank()) {
            val allowed = scheduleDaysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
            if (useDeadline && effectivePerDay > 0) {
                countAllowedDaysBetween(startDate, deadlineDate, scheduleDaysOfWeek)
            } else if (effectivePerDay > 0) {
                ceil(learnItem.totalSections.toFloat() / effectivePerDay.toFloat()).toInt()
            } else 0
        } else if (useDeadline && effectivePerDay > 0) {
            try {
                val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val from = fmt.parse(startDate)
                val to = fmt.parse(deadlineDate)
                if (from != null && to != null) {
                    ((to.time - from.time) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
                } else 1
            } catch (_: Exception) { 1 }
        } else if (effectivePerDay > 0) {
            ceil(learnItem.totalSections.toFloat() / effectivePerDay.toFloat()).toInt()
        } else 0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start Learning: ${learnItem.title}") },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = scheduleMode == "CONTINUOUS",
                        onClick = { scheduleMode = "CONTINUOUS" },
                        label = { Text("Daily") }
                    )
                    FilterChip(
                        selected = scheduleMode == "WEEKLY",
                        onClick = { scheduleMode = "WEEKLY" },
                        label = { Text("Weekly") }
                    )
                }
                if (scheduleMode == "WEEKLY") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Repeat on:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysOfWeekList.forEach { (dayVal, enLabel) ->
                            val isSelected = currentDays.contains(dayVal.toString())
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(36.dp).clickable {
                                    val mutableDays = currentDays.toMutableList()
                                    if (isSelected) mutableDays.remove(dayVal.toString()) else mutableDays.add(dayVal.toString())
                                    scheduleDaysOfWeek = mutableDays.joinToString(",")
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = enLabel, fontSize = 12.sp, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !useDeadline,
                        onClick = { useDeadline = false },
                        label = { Text("Sections/day") }
                    )
                    FilterChip(
                        selected = useDeadline,
                        onClick = { useDeadline = true },
                        label = { Text("Finish by date") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (useDeadline) {
                    DatePickerField(
                        label = "Deadline",
                        date = deadlineDate,
                        usePersianCalendar = usePersianCalendar,
                        onDateSelected = { deadlineDate = it }
                    )
                } else {
                    var sectionsPerDayError by remember { mutableStateOf(false) }
                    val perDayValue = sectionsPerDay.toIntOrNull() ?: 0
                    LaunchedEffect(perDayValue) {
                        sectionsPerDayError = !useDeadline && perDayValue <= 0 && sectionsPerDay.isNotBlank()
                    }
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = sectionsPerDay,
                            onValueChange = { 
                                sectionsPerDay = it.filter { c -> c.isDigit() } 
                                sectionsPerDayError = false
                            },
                            label = { Text("Sections per day") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            isError = sectionsPerDayError
                        )
                        if (sectionsPerDayError) {
                            Text(
                                text = "Enter at least 1 section per day",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                DatePickerField(
                    label = "Start Date",
                    date = startDate,
                    usePersianCalendar = usePersianCalendar,
                    onDateSelected = { startDate = it }
                )
                if (scheduleMode == "WEEKLY" && scheduleDaysOfWeek.isNotBlank()) {
                    val dayNames = scheduleDaysOfWeek.split(",").map { dayNum ->
                        when (dayNum) {
                            "1" -> "Sun"
                            "2" -> "Mon"
                            "3" -> "Tue"
                            "4" -> "Wed"
                            "5" -> "Thu"
                            "6" -> "Fri"
                            "7" -> "Sat"
                            else -> ""
                        }
                    }.filter { it.isNotBlank() }
                    Text(
                        text = "Repeat on: ${dayNames.joinToString(", ")}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (totalDays > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${learnItem.totalSections} sections over $totalDays day${if (totalDays != 1) "s" else ""}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val perDay = sectionsPerDay.toIntOrNull() ?: 0
                    if (perDay <= 0 && !useDeadline) return@TextButton
                    if (scheduleMode == "WEEKLY" && scheduleDaysOfWeek.isBlank()) return@TextButton
                    
                    // E1: Validate start date >= today for WEEKLY mode
                    if (scheduleMode == "WEEKLY") {
                        val today = viewModel.todayDate.value
                        if (daysBetweenDates(today, startDate) < 0) {
                            // startDate is before today
                            return@TextButton
                        }
                    }
                    
                    // E2: Validate deadline >= start date
                    if (useDeadline && daysBetweenDates(startDate, deadlineDate) < 0) {
                        return@TextButton
                    }
                    
                    viewModel.applyLearningAlgorithm(
                        learnItem.id, startDate,
                        if (useDeadline) 1 else perDay,
                        deadline = if (useDeadline) deadlineDate else null,
                        scheduleMode = scheduleMode,
                        scheduleDaysOfWeek = scheduleDaysOfWeek
                    )
                    onDismiss()
                },
                enabled = when {
                    useDeadline && scheduleMode == "WEEKLY" -> scheduleDaysOfWeek.isNotBlank()
                    useDeadline -> true
                    scheduleMode == "WEEKLY" -> (sectionsPerDay.toIntOrNull() ?: 0) > 0 && scheduleDaysOfWeek.isNotBlank()
                    else -> (sectionsPerDay.toIntOrNull() ?: 0) > 0
                }
            ) { Text("Start") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewRatingSheet(
    learnItem: LearnItemEntity,
    task: TaskEntity,
    section: LearnSectionEntity,
    onRate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "How was your review?",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${learnItem.title} - ${section.title}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onRate("EASY") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("Easy") }
                Button(
                    onClick = { onRate("MEDIUM") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                ) { Text("Medium") }
                Button(
                    onClick = { onRate("HARD") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) { Text("Hard") }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DatePickerField(
    label: String,
    date: String,
    usePersianCalendar: Boolean = false,
    onDateSelected: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = date,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "Pick date")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )

    if (showDatePicker) {
        CalendarDatePickerDialog(
            initialSelectedDate = date,
            initialUsePersian = usePersianCalendar,
            onDismiss = { showDatePicker = false },
            onDateSelected = { d ->
                onDateSelected(d)
                showDatePicker = false
            }
        )
    }
}

private data class LabelInfo(
    val name: String,
    val color: Long?,
    val count: Int
)
