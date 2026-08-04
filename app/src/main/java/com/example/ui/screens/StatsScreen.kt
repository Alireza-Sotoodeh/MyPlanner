package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import android.os.Build
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HeaderActions
import com.example.ui.components.LineChartCanvas
import com.example.ui.components.LineChartLine
import com.example.ui.viewmodel.MainViewModel
import com.example.core.database.entity.SleepLogEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Date
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.style.TextAlign
import com.example.core.utils.PersianCalendarHelper
import com.example.core.database.entity.TimerSessionEntity
import com.example.core.database.entity.HabitLogEntity
import com.example.core.database.entity.HabitEntity
import com.example.core.database.entity.TaskEntity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput

import androidx.compose.foundation.Canvas
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.roundToInt
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import com.example.ui.components.CalendarDatePickerDialog
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun StatsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val totalScreenTimeMinutes by viewModel.totalScreenTimeMinutes.collectAsState()
    val appUsageItems by viewModel.appUsageItems.collectAsState()
    val screenTimeError by viewModel.screenTimeError.collectAsState()
    val screenTimeLoading by viewModel.screenTimeLoading.collectAsState()
    val screenTimeLastUpdated by viewModel.screenTimeLastUpdated.collectAsState()
    val hasUsageStats = remember { viewModel.hasUsageStatsPermission(context) }
    val habits by viewModel.habits.collectAsState()
    val todayHabitLogs by viewModel.todayHabitLogs.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val allTimerSessions by viewModel.allTimerSessions.collectAsState()
    val usePersianCalendar by viewModel.usePersianCalendar.collectAsState()
    val todayDateStr by viewModel.todayDate.collectAsState()
    val allHabitLogs by viewModel.allHabitLogs.collectAsState()
    val allSleepLogs by viewModel.allSleepLogs.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }

    // Line graph state
    var lineGraphYear by remember(usePersianCalendar) { mutableIntStateOf(
        if (usePersianCalendar) PersianCalendarHelper.getCurrentPersianYear()
        else Calendar.getInstance().get(Calendar.YEAR)
    ) }
    var lineGraphMonth by remember(usePersianCalendar) { mutableIntStateOf(
        if (usePersianCalendar) PersianCalendarHelper.getCurrentPersianMonth()
        else Calendar.getInstance().get(Calendar.MONTH) + 1
    ) }
    var selectedRangeMode by remember { mutableStateOf("MONTH") }

    val lineGraphRange = remember(lineGraphYear, lineGraphMonth, usePersianCalendar, selectedRangeMode) {
        computeLineGraphRange(lineGraphYear, lineGraphMonth, usePersianCalendar, selectedRangeMode)
    }
    val dailyData = remember(lineGraphRange, habits, allHabitLogs) {
        computeDailyCompletions(lineGraphRange.first, lineGraphRange.second, habits, allHabitLogs)
    }
    var heatmapYear by remember(usePersianCalendar) { mutableIntStateOf(if (usePersianCalendar) PersianCalendarHelper.getCurrentPersianYear() else Calendar.getInstance().get(Calendar.YEAR)) }
    var heatmapMonth by remember(usePersianCalendar) { mutableIntStateOf(if (usePersianCalendar) PersianCalendarHelper.getCurrentPersianMonth() else Calendar.getInstance().get(Calendar.MONTH) + 1) }

    // Sleep graph state
    var sleepGraphYear by remember(usePersianCalendar) { mutableIntStateOf(
        if (usePersianCalendar) PersianCalendarHelper.getCurrentPersianYear()
        else Calendar.getInstance().get(Calendar.YEAR)
    ) }
    var sleepGraphMonth by remember(usePersianCalendar) { mutableIntStateOf(
        if (usePersianCalendar) PersianCalendarHelper.getCurrentPersianMonth()
        else Calendar.getInstance().get(Calendar.MONTH) + 1
    ) }
    var sleepRangeMode by remember { mutableStateOf("MONTH") }

    val sleepGraphRange = remember(sleepGraphYear, sleepGraphMonth, usePersianCalendar, sleepRangeMode) {
        computeLineGraphRange(sleepGraphYear, sleepGraphMonth, usePersianCalendar, sleepRangeMode)
    }
    val sleepDailyData = remember(sleepGraphRange, allSleepLogs) {
        computeSleepDailyData(sleepGraphRange.first, sleepGraphRange.second, allSleepLogs)
    }

    // Dreams & Notes state
    var dreamsFilterMode by remember { mutableStateOf("ALL") }
    var dreamsYear by remember(usePersianCalendar) { mutableIntStateOf(
        if (usePersianCalendar) PersianCalendarHelper.getCurrentPersianYear()
        else Calendar.getInstance().get(Calendar.YEAR)
    ) }
    var dreamsMonth by remember(usePersianCalendar) { mutableIntStateOf(
        if (usePersianCalendar) PersianCalendarHelper.getCurrentPersianMonth()
        else Calendar.getInstance().get(Calendar.MONTH) + 1
    ) }
    var dreamsSelectedDate by remember { mutableStateOf<String?>(null) }
    var showDreamsDatePicker by remember { mutableStateOf(false) }

    val dreamsMonthLabel = remember(dreamsYear, dreamsMonth, usePersianCalendar) {
        if (usePersianCalendar) {
            "${PersianCalendarHelper.monthNames.getOrElse(dreamsMonth - 1) { "" }} $dreamsYear"
        } else {
            val names = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
            "${names.getOrElse(dreamsMonth - 1) { "" }} $dreamsYear"
        }
    }

    val dreamDateLabels = remember {
        val todayCal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(todayCal.time)
        todayCal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = sdf.format(todayCal.time)
        val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        Triple(today, yesterday, currentYear)
    }

    val dreamHighlightedDates = remember(allSleepLogs) {
        allSleepLogs.filter { it.notes.isNotBlank() }.map { it.date }.toSet()
    }

    val dreamsDisplayData = remember(allSleepLogs, dreamsFilterMode, dreamsYear, dreamsMonth, usePersianCalendar, dreamsSelectedDate) {
        val dreamsLogs = allSleepLogs.filter { it.notes.isNotBlank() }
        val filtered = when (dreamsFilterMode) {
            "ALL" -> dreamsLogs
            "7D" -> {
                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val start = sdf.format(cal.time)
                dreamsLogs.filter { it.date >= start }
            }
            "30D" -> {
                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -29) }
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val start = sdf.format(cal.time)
                dreamsLogs.filter { it.date >= start }
            }
            "MONTH" -> {
                val (start, end, _) = computeLineGraphRange(dreamsYear, dreamsMonth, usePersianCalendar, "MONTH")
                dreamsLogs.filter { it.date in start..end }
            }
            else -> dreamsLogs
        }
        val finalLogs = if (dreamsSelectedDate != null) {
            filtered.filter { it.date == dreamsSelectedDate }
        } else filtered
        finalLogs.groupBy { it.date }.toSortedMap(compareByDescending { it })
    }

    // Trigger update on screen load and refresh every 30s
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updateAppUsage(context)
            delay(30_000)
        }
    }

    val lazyListState = rememberLazyListState()

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "INSIGHTS & SCREEN TIME",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Aesthetic Analytics",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                HeaderActions(
                    onHomeClick = { viewModel.selectTab(0); viewModel.selectDate(viewModel.todayDate.value) },
                    onSettingsClick = { showSettingsDialog = true }
                )
            }
        }

        // 1. Native UsageStatsManager Screen Time
        item {
            if (hasUsageStats) {
                val hours = totalScreenTimeMinutes / 60
                val minutes = totalScreenTimeMinutes % 60
                val formattedTime = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

                val lastUpdatedText = screenTimeLastUpdated?.let { ts ->
                    val elapsed = (System.currentTimeMillis() - ts) / 1000
                    when {
                        elapsed < 5 -> "Updated just now"
                        elapsed < 60 -> "Updated ${elapsed}s ago"
                        elapsed < 120 -> "Updated 1m ago"
                        else -> "Updated ${elapsed / 60}m ago"
                    }
                } ?: if (screenTimeLoading) "Updating..." else null

                val showApi34Hint = !screenTimeLoading
                        && appUsageItems.isEmpty()
                        && screenTimeError == null
                        && totalScreenTimeMinutes == 0L
                        && Build.VERSION.SDK_INT >= 34

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TODAY'S SCREEN TIME",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.5.sp
                            )
                            Icon(
                                imageVector = Icons.Default.Leaderboard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (screenTimeLoading && appUsageItems.isEmpty() && screenTimeError == null) {
                            Text(
                                text = formattedTime,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Loading...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        } else if (screenTimeError != null) {
                            Text(
                                text = screenTimeError!!,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = formattedTime,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (lastUpdatedText != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = lastUpdatedText,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "TOP APPS LOGGED:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (appUsageItems.isEmpty()) {
                                Text(
                                    text = "No usage data yet today.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    appUsageItems.forEach { item ->
                                        val maxDuration = appUsageItems.maxOfOrNull { it.durationMinutes } ?: 1L
                                        val progress = item.durationMinutes.toFloat() / maxDuration.toFloat()

                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = item.appName,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                                Text(
                                                    text = "${item.durationMinutes}m",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                                    .clip(CircleShape)
                                            )
                                        }
                                    }
                                }
                            }

                            if (showApi34Hint) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Screen time may be limited on this device. " +
                                                "Try Settings → Apps → MyPlanner → Usage Access → toggle off and on.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Screen Time",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Enable Usage Access in Settings to see today's screen time.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { viewModel.requestUsagePermission(context) }) {
                            Text("ENABLE", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 2. Habit Completion Progress + Line Graph
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "HABIT INTENTIONS REACHED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (habits.isEmpty()) {
                        Text(
                            text = "Create habits to view accomplishments progress.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val activeHabits = remember(habits, todayDateStr) {
                            if (todayDateStr.isBlank()) return@remember habits
                            try {
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val parsedDate = sdf.parse(todayDateStr) ?: return@remember habits
                                val cal = Calendar.getInstance().apply { time = parsedDate }
                                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                                habits.filter { habit ->
                                    val beforeEnd = if (habit.recurrenceEndDate != null) {
                                        try {
                                            val endDate = sdf.parse(habit.recurrenceEndDate)
                                            !parsedDate.after(endDate)
                                        } catch (_: Exception) { true }
                                    } else true
                                    beforeEnd && when (habit.recurrenceMode) {
                                        "ALWAYS" -> true
                                        "WEEKLY" -> {
                                            val days = habit.recurrenceDaysOfWeek
                                                .split(",")
                                                .mapNotNull { it.trim().toIntOrNull() }
                                                .toSet()
                                            dayOfWeek in days
                                        }
                                        else -> false
                                    }
                                }
                            } catch (_: Exception) { habits }
                        }
                        val habitTargetMap = activeHabits.associate { it.id to it.target }
                        val loggedCount = todayHabitLogs.count { log ->
                            val target = habitTargetMap[log.habitId] ?: 1f
                            log.value >= target
                        }
                        val totalHabits = activeHabits.size
                        val progress = if (totalHabits > 0) loggedCount.toFloat() / totalHabits.toFloat() else 0f

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${(progress * 100).toInt()}% completed",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$loggedCount of $totalHabits",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(CircleShape)
                        )

                        // --- Divider ---
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // --- Line Graph ---
                        // Nav + range toggle row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedRangeMode == "MONTH") {
                                IconButton(
                                    onClick = {
                                        if (usePersianCalendar) {
                                            val (y, m) = PersianCalendarHelper.getOffsetPersianMonth(lineGraphYear, lineGraphMonth, -1)
                                            lineGraphYear = y; lineGraphMonth = m
                                        } else {
                                            if (lineGraphMonth == 1) { lineGraphYear--; lineGraphMonth = 12 }
                                            else lineGraphMonth--
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Default.KeyboardArrowLeft, "Previous", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                                Text(
                                    text = lineGraphRange.third,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                                    IconButton(
                                        onClick = {
                                            if (usePersianCalendar) {
                                                val (y, m) = PersianCalendarHelper.getOffsetPersianMonth(lineGraphYear, lineGraphMonth, 1)
                                                lineGraphYear = y; lineGraphMonth = m
                                            } else {
                                                if (lineGraphMonth == 12) { lineGraphYear++; lineGraphMonth = 1 }
                                                else lineGraphMonth++
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, "Next", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = { viewModel.toggleUsePersianCalendar() }, modifier = Modifier.size(28.dp)) {
                                        Text(
                                            if (usePersianCalendar) "EN" else "FA",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else {
                                Text(
                                    text = if (selectedRangeMode == "7D") "Last 7 Days" else "Last 30 Days",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(Modifier.width(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("7D", "30D", "MONTH").forEach { mode ->
                                    val isSelected = selectedRangeMode == mode
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable {
                                            selectedRangeMode = mode
                                            if (mode == "MONTH") {
                                                if (usePersianCalendar) {
                                                    lineGraphYear = PersianCalendarHelper.getCurrentPersianYear()
                                                    lineGraphMonth = PersianCalendarHelper.getCurrentPersianMonth()
                                                } else {
                                                    val now = Calendar.getInstance()
                                                    lineGraphYear = now.get(Calendar.YEAR)
                                                    lineGraphMonth = now.get(Calendar.MONTH) + 1
                                                }
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = mode,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val primaryColor = MaterialTheme.colorScheme.primary
                        val maxYf = remember(dailyData) { dailyData.maxOfOrNull { it.completed }?.toFloat()?.coerceAtLeast(1f) ?: 1f }
                        val trailingAvg = remember(dailyData) {
                            if (dailyData.size >= 7) dailyData.takeLast(7).map { it.completed }.average().toFloat()
                            else null
                        }
                        val monthCompletedDays = remember(dailyData) { dailyData.count { it.completed > 0 } }
                        val yStep = when {
                            maxYf <= 3f -> 1f
                            maxYf <= 8f -> 2f
                            maxYf <= 20f -> 5f
                            else -> (maxYf / 4f).roundToInt().coerceAtLeast(5).toFloat()
                        }

                        LineChartCanvas(
                            lines = listOf(LineChartLine(
                                values = dailyData.map { it.completed.toFloat() },
                                color = primaryColor,
                                label = "Completed"
                            )),
                            maxY = maxYf,
                            yStep = yStep,
                            yLabelFormatter = { it.toInt().toString() },
                            xLabels = dailyData.map { it.day.toString() },
                            dateStrs = dailyData.map { it.dateStr },
                            gradientFill = true,
                            trailingAvg = trailingAvg,
                            height = 200.dp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = trailingAvg?.let { "Last 7d avg: ${"%.1f".format(it)}/day" } ?: "",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$monthCompletedDays of ${dailyData.size} days with habits",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 3. Habit Consistency Score
        item {
            var consistencyRangeMode by remember { mutableStateOf("30D") }
            var consistencySortBy by remember { mutableStateOf("CONSISTENCY") }

            val habitConsistencyData = remember(habits, allHabitLogs, consistencyRangeMode) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val cal = Calendar.getInstance()
                val now = sdf.format(cal.time)
                val rangeStart = when (consistencyRangeMode) {
                    "7D" -> { cal.add(Calendar.DAY_OF_MONTH, -6); sdf.format(cal.time) }
                    "30D" -> { cal.add(Calendar.DAY_OF_MONTH, -29); sdf.format(cal.time) }
                    else -> { cal.add(Calendar.DAY_OF_MONTH, -29); sdf.format(cal.time) }
                }
                computeHabitConsistency(rangeStart, now, habits, allHabitLogs)
            }

            val sortedConsistency = remember(habitConsistencyData, consistencySortBy) {
                when (consistencySortBy) {
                    "STREAK" -> habitConsistencyData.sortedByDescending { it.currentStreak }
                    "NAME" -> habitConsistencyData.sortedBy { it.habitName.lowercase() }
                    else -> habitConsistencyData.sortedByDescending { it.consistency }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HABIT CONSISTENCY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.5.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("7D", "30D").forEach { mode ->
                                val isSel = consistencyRangeMode == mode
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSel) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { consistencyRangeMode = mode }
                                ) {
                                    Text(
                                        text = mode,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (habitConsistencyData.isEmpty()) {
                        Text(
                            text = "No habit data in this period.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sort:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(4.dp))
                            listOf("CONSISTENCY" to "Consistency", "STREAK" to "Streak", "NAME" to "Name").forEach { (key, label) ->
                                val isSel = consistencySortBy == key
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSel) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .clickable { consistencySortBy = key }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        sortedConsistency.forEach { data ->
                            val consistencyColor = when {
                                data.consistency >= 0.75f -> Color(0xFF4CAF50)
                                data.consistency >= 0.4f -> Color(0xFFFFC107)
                                else -> Color(0xFFF44336)
                            }
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(consistencyColor)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = data.habitName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Text(
                                        text = "${(data.consistency * 100).toInt()}%",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = consistencyColor
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                LinearProgressIndicator(
                                    progress = { data.consistency },
                                    color = consistencyColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                            val streakLabel = when {
                                data.currentStreak > 0 -> "${data.currentStreak}-day streak"
                                else -> "No current streak"
                            }
                                    Text(
                                        text = streakLabel,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${data.doneDays}/${data.eligibleDays} days",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (data != sortedConsistency.last()) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Sleep Logs Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "SLEEP LOGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (allSleepLogs.isEmpty()) {
                        Text(
                            text = "Log your sleep in the Habits tab to see trends.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // ── Filter controls (shared by all 3 graphs) ──
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (sleepRangeMode == "MONTH") {
                                IconButton(
                                    onClick = {
                                        if (usePersianCalendar) {
                                            val (y, m) = PersianCalendarHelper.getOffsetPersianMonth(sleepGraphYear, sleepGraphMonth, -1)
                                            sleepGraphYear = y; sleepGraphMonth = m
                                        } else {
                                            if (sleepGraphMonth == 1) { sleepGraphYear--; sleepGraphMonth = 12 }
                                            else sleepGraphMonth--
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Default.KeyboardArrowLeft, "Previous", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                                Text(
                                    text = sleepGraphRange.third,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                                IconButton(
                                    onClick = {
                                        if (usePersianCalendar) {
                                            val (y, m) = PersianCalendarHelper.getOffsetPersianMonth(sleepGraphYear, sleepGraphMonth, 1)
                                            sleepGraphYear = y; sleepGraphMonth = m
                                        } else {
                                            if (sleepGraphMonth == 12) { sleepGraphYear++; sleepGraphMonth = 1 }
                                            else sleepGraphMonth++
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, "Next", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { viewModel.toggleUsePersianCalendar() }, modifier = Modifier.size(28.dp)) {
                                    Text(
                                        if (usePersianCalendar) "EN" else "FA",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                Text(
                                    text = if (sleepRangeMode == "7D") "Last 7 Days" else "Last 30 Days",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(Modifier.width(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("7D", "30D", "MONTH").forEach { mode ->
                                    val isSelected = sleepRangeMode == mode
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable {
                                            sleepRangeMode = mode
                                            if (mode == "MONTH") {
                                                if (usePersianCalendar) {
                                                    sleepGraphYear = PersianCalendarHelper.getCurrentPersianYear()
                                                    sleepGraphMonth = PersianCalendarHelper.getCurrentPersianMonth()
                                                } else {
                                                    val now = Calendar.getInstance()
                                                    sleepGraphYear = now.get(Calendar.YEAR)
                                                    sleepGraphMonth = now.get(Calendar.MONTH) + 1
                                                }
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = mode,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val sleepXLabels = sleepDailyData.map { it.day.toString() }
                        val sleepDateStrs = sleepDailyData.map { it.date }

                        // ── Duration Graph ──
                        Text(
                            text = "Sleep Duration",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        val durationMaxY = remember(sleepDailyData) { sleepDailyData.mapNotNull { it.duration }.maxOrNull()?.coerceAtLeast(1f) ?: 1f }
                        val durationStep = remember(durationMaxY) {
                            when {
                                durationMaxY <= 3f -> 1f
                                durationMaxY <= 8f -> 2f
                                else -> (durationMaxY / 4f).roundToInt().coerceAtLeast(2).toFloat()
                            }
                        }
                        LineChartCanvas(
                            lines = listOf(LineChartLine(
                                values = sleepDailyData.map { it.duration },
                                color = MaterialTheme.colorScheme.primary,
                                label = "Sleep"
                            )),
                            maxY = durationMaxY,
                            yStep = durationStep,
                            yLabelFormatter = { "${it.toInt()}h" },
                            xLabels = sleepXLabels,
                            dateStrs = sleepDateStrs,
                            gradientFill = true,
                            height = 160.dp
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // ── Mood/Quality Graph ──
                        Text(
                            text = "Sleep Quality",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        LineChartCanvas(
                            lines = listOf(LineChartLine(
                                values = sleepDailyData.map { it.quality },
                                color = MaterialTheme.colorScheme.tertiary,
                                label = "Quality"
                            )),
                            maxY = 5f,
                            minY = 1f,
                            yStep = 1f,
                            yLabelFormatter = { it.toInt().toString() },
                            xLabels = sleepXLabels,
                            dateStrs = sleepDateStrs,
                            gradientFill = false,
                            height = 140.dp
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // ── Bedtime/Wake Time Graph ──
                        Text(
                            text = "Sleep Schedule",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        LineChartCanvas(
                            lines = listOf(
                                LineChartLine(
                                    values = sleepDailyData.map { it.bedMinutes },
                                    color = Color(0xFF5C6BC0),
                                    label = "Bed"
                                ),
                                LineChartLine(
                                    values = sleepDailyData.map { it.wakeMinutes },
                                    color = Color(0xFFFF7043),
                                    label = "Wake"
                                )
                            ),
                            maxY = 1440f,
                            yStep = 120f,
                            yLabelFormatter = { formatMinutesToTime(it) },
                            xLabels = sleepXLabels,
                            dateStrs = sleepDateStrs,
                            gradientFill = false,
                            height = 180.dp
                        )
                    }
                }
            }
        }

        // ── Dream/Notes Card ──
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "DREAMS & NOTES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Filter bar ──
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("ALL" to "All", "7D" to "7d", "30D" to "30d", "MONTH" to "Month").forEach { (key, label) ->
                            val isSelected = dreamsFilterMode == key
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    dreamsFilterMode = key
                                    dreamsSelectedDate = null
                                    if (key == "MONTH") {
                                        if (usePersianCalendar) {
                                            dreamsYear = PersianCalendarHelper.getCurrentPersianYear()
                                            dreamsMonth = PersianCalendarHelper.getCurrentPersianMonth()
                                        } else {
                                            val now = Calendar.getInstance()
                                            dreamsYear = now.get(Calendar.YEAR)
                                            dreamsMonth = now.get(Calendar.MONTH) + 1
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        if (dreamsFilterMode == "MONTH") {
                            IconButton(
                                onClick = {
                                    if (usePersianCalendar) {
                                        val (y, m) = PersianCalendarHelper.getOffsetPersianMonth(dreamsYear, dreamsMonth, -1)
                                        dreamsYear = y; dreamsMonth = m
                                    } else {
                                        if (dreamsMonth == 1) { dreamsYear--; dreamsMonth = 12 }
                                        else dreamsMonth--
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Default.KeyboardArrowLeft, "Previous", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                            Text(
                                text = dreamsMonthLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = {
                                    if (usePersianCalendar) {
                                        val (y, m) = PersianCalendarHelper.getOffsetPersianMonth(dreamsYear, dreamsMonth, 1)
                                        dreamsYear = y; dreamsMonth = m
                                    } else {
                                        if (dreamsMonth == 12) { dreamsYear++; dreamsMonth = 1 }
                                        else dreamsMonth++
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, "Next", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(onClick = { showDreamsDatePicker = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date", modifier = Modifier.size(18.dp))
                        }
                    }

                    // ── Selected date chip ──
                    if (dreamsSelectedDate != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.clickable { dreamsSelectedDate = null }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = dreamsSelectedDate!!,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // ── Summary ──
                    Spacer(modifier = Modifier.height(10.dp))
                    val totalDreams = dreamsDisplayData.values.flatten().size
                    Text(
                        text = "${totalDreams} dream${if (totalDreams != 1) "s" else ""} recorded",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── Content ──
                    if (dreamsDisplayData.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (allSleepLogs.none { it.notes.isNotBlank() })
                                    "No dreams recorded yet. Add notes to your sleep log."
                                else
                                    "No dreams in this period.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            dreamsDisplayData.forEach { (dateStr, logs) ->
                                val headerText = when (dateStr) {
                                    dreamDateLabels.first -> "Today"
                                    dreamDateLabels.second -> "Yesterday"
                                    else -> {
                                        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                                        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(parsed!!)
                                        val fmt = if (year == dreamDateLabels.third) "EEE, MMM d" else "EEE, MMM d, yyyy"
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

                                logs.forEach { log ->
                                    var expanded by remember { mutableStateOf(false) }
                                    val needsExpand = log.notes.length > 100
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${log.hoursSlept} hrs",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${"★".repeat(log.sleepQuality)}${"☆".repeat(5 - log.sleepQuality)}",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = log.notes,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = if (expanded || !needsExpand) Int.MAX_VALUE else 4,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = if (needsExpand) Modifier.clickable { expanded = !expanded } else Modifier
                                            )
                                            if (needsExpand) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = if (expanded) "Show less" else "Show more",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.clickable { expanded = !expanded }
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

            if (showDreamsDatePicker) {
                CalendarDatePickerDialog(
                    highlightedDates = dreamHighlightedDates,
                    initialUsePersian = usePersianCalendar,
                    onDismiss = { showDreamsDatePicker = false },
                    onDateSelected = { date ->
                        dreamsSelectedDate = date
                        dreamsFilterMode = "ALL"
                        showDreamsDatePicker = false
                    }
                )
            }
        }

        // 5. Task Completion Trend + Time by Label + Time of Day
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // ── Task Completion Trend ──
                    var taskRangeMode by remember { mutableStateOf("7D") }
                    var taskGraphYear by remember(usePersianCalendar) { mutableIntStateOf(
                        if (usePersianCalendar) PersianCalendarHelper.getCurrentPersianYear()
                        else Calendar.getInstance().get(Calendar.YEAR)
                    ) }
                    var taskGraphMonth by remember(usePersianCalendar) { mutableIntStateOf(
                        if (usePersianCalendar) PersianCalendarHelper.getCurrentPersianMonth()
                        else Calendar.getInstance().get(Calendar.MONTH) + 1
                    ) }

                    val taskGraphRange = remember(taskGraphYear, taskGraphMonth, usePersianCalendar, taskRangeMode) {
                        computeLineGraphRange(taskGraphYear, taskGraphMonth, usePersianCalendar, taskRangeMode)
                    }

                    val taskCompletionData = remember(taskGraphRange, allTasks) {
                        computeTaskCompletionRate(taskGraphRange.first, taskGraphRange.second, allTasks)
                    }

                    Text(
                        text = "TASK COMPLETION TREND",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Nav + range toggle row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (taskRangeMode == "MONTH") {
                            IconButton(
                                onClick = {
                                    if (usePersianCalendar) {
                                        val (y, m) = PersianCalendarHelper.getOffsetPersianMonth(taskGraphYear, taskGraphMonth, -1)
                                        taskGraphYear = y; taskGraphMonth = m
                                    } else {
                                        if (taskGraphMonth == 1) { taskGraphYear--; taskGraphMonth = 12 }
                                        else taskGraphMonth--
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Default.KeyboardArrowLeft, "Previous", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            Text(
                                text = taskGraphRange.third,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            IconButton(
                                onClick = {
                                    if (usePersianCalendar) {
                                        val (y, m) = PersianCalendarHelper.getOffsetPersianMonth(taskGraphYear, taskGraphMonth, 1)
                                        taskGraphYear = y; taskGraphMonth = m
                                    } else {
                                        if (taskGraphMonth == 12) { taskGraphYear++; taskGraphMonth = 1 }
                                        else taskGraphMonth++
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, "Next", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { viewModel.toggleUsePersianCalendar() }, modifier = Modifier.size(28.dp)) {
                                Text(
                                    if (usePersianCalendar) "EN" else "FA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Text(
                                text = if (taskRangeMode == "7D") "Last 7 Days" else "Last 30 Days",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(Modifier.width(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("7D", "30D", "MONTH").forEach { mode ->
                                val isSelected = taskRangeMode == mode
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable {
                                        taskRangeMode = mode
                                        if (mode == "MONTH") {
                                            if (usePersianCalendar) {
                                                taskGraphYear = PersianCalendarHelper.getCurrentPersianYear()
                                                taskGraphMonth = PersianCalendarHelper.getCurrentPersianMonth()
                                            } else {
                                                val now = Calendar.getInstance()
                                                taskGraphYear = now.get(Calendar.YEAR)
                                                taskGraphMonth = now.get(Calendar.MONTH) + 1
                                            }
                                        }
                                    }
                                ) {
                                    Text(
                                        text = mode,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Today summary row
                    val sdfToday = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
                    val tomorrowStr = remember {
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        sdfToday.format(cal.time)
                    }
                    val doneToday = allTasks.count { it.date == todayDateStr && it.status == "COMPLETED" && !it.postponed && it.type != "NOTE" }
                    val postponedToTomorrow = allTasks.count { it.date == tomorrowStr && it.postponed && it.type != "NOTE" }
                    val remainingToday = allTasks.count { it.date == todayDateStr && it.status == "PENDING" && !it.postponed && it.type != "NOTE" }

                    val summaryPrimary = MaterialTheme.colorScheme.primary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "DONE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(text = "$doneToday", fontSize = 20.sp, fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "POSTPONED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "$postponedToTomorrow", fontSize = 20.sp, fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "REMAINING", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "$remainingToday", fontSize = 20.sp, fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Line chart: completion rate over days
                    if (taskCompletionData.all { it.total == 0 }) {
                        Text(
                            text = "No task data in this period.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val maxRate = 1f
                        val taskPrimary = MaterialTheme.colorScheme.primary
                        val rateValues = taskCompletionData.map { it.completionRate }
                        val avgRate = remember(rateValues) {
                            val nonZero = rateValues.filter { it > 0f }
                            if (nonZero.isNotEmpty()) nonZero.average().toFloat() else 0f
                        }

                        LineChartCanvas(
                            lines = listOf(LineChartLine(
                                values = rateValues,
                                color = taskPrimary,
                                label = "Completion"
                            )),
                            maxY = maxRate,
                            minY = 0f,
                            yStep = 0.25f,
                            yLabelFormatter = { "${(it * 100).toInt()}%" },
                            xLabels = taskCompletionData.map { it.day.toString() },
                            dateStrs = taskCompletionData.map { it.dateStr },
                            gradientFill = true,
                            height = 160.dp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Avg: ${"%.0f".format(avgRate * 100)}% completion",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${taskCompletionData.count { it.done > 0 }} of ${taskCompletionData.size} days with completions",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Time Spent by Label ──
                    Text(
                        text = "TIME SPENT BY LABEL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    val todaySessions = allTimerSessions.filter { it.date == todayDateStr }
                    val labelGroups = todaySessions.groupBy {
                        if (it.label.isBlank()) "Unlabeled" else it.label
                    }
                    val labelStats = labelGroups.map { (label, sessions) ->
                        label to sessions.sumOf { it.durationSeconds }
                    }.filter { it.second > 0 }.sortedByDescending { it.second }

                    if (labelStats.isEmpty()) {
                        Text(
                            text = "Use the Timer to log Pomodoro and Chronometer sessions.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val totalSeconds = labelStats.sumOf { it.second }
                        val totalDuration = totalSeconds.toFloat()
                        val totalHoursVal = totalSeconds / 3600
                        val totalMinsVal = (totalSeconds % 3600) / 60
                        val totalTimeString = when {
                            totalHoursVal > 0 -> "${totalHoursVal}h ${totalMinsVal}m"
                            totalMinsVal > 0 -> "${totalMinsVal}m"
                            else -> "${totalSeconds}s"
                        }

                        val pieColors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.error,
                            Color(0xFF9C27B0),
                            Color(0xFFFF9800),
                            Color(0xFF4CAF50),
                            Color(0xFFE91E63),
                            Color(0xFF00BCD4),
                            Color(0xFF795548),
                            Color(0xFF607D8B),
                            Color(0xFFCDDC39),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f),
                            Color(0xFF9C27B0).copy(alpha = 0.6f),
                            Color(0xFFFF9800).copy(alpha = 0.6f),
                            Color(0xFF4CAF50).copy(alpha = 0.6f)
                        )

                        // Donut chart + Legend row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Donut with total in center
                            Box(
                                modifier = Modifier.size(148.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeWidth = 36.dp.toPx()
                                    val gapDeg = 2.5f
                                    val radius = (size.width - strokeWidth) / 2f
                                    val arcTopLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

                                    var startAngle = -90f
                                    labelStats.forEachIndexed { index, stat ->
                                        val sweep = (stat.second / totalDuration) * 360f
                                        val arcSweep = if (sweep > gapDeg * 2) (sweep - gapDeg) else sweep.coerceAtLeast(0.5f)
                                        drawArc(
                                            color = pieColors[index % pieColors.size],
                                            startAngle = startAngle,
                                            sweepAngle = arcSweep,
                                            useCenter = false,
                                            topLeft = arcTopLeft,
                                            size = arcSize,
                                            style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Butt)
                                        )
                                        startAngle += sweep
                                    }
                                }
                                // Center total
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = totalTimeString,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "total",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            // Legend
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                labelStats.take(6).forEachIndexed { index, stat ->
                                    val pct = (stat.second / totalDuration * 100).toInt()
                                    val hours = stat.second / 3600
                                    val mins = (stat.second % 3600) / 60
                                    val secs = stat.second % 60
                                    val timeStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m ${secs}s"

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(pieColors[index % pieColors.size])
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stat.first,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = timeStr,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "${pct}%",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                                if (labelStats.size > 6) {
                                    Text(
                                        text = "+${labelStats.size - 6} more",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total tracked time",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = totalTimeString,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Time of Day Activity ──
                    Text(
                        text = "TIME OF DAY ACTIVITY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Today's productivity timeline across 24 hours.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    val todayStart = remember {
                        java.util.Calendar.getInstance().apply {
                            set(java.util.Calendar.HOUR_OF_DAY, 0)
                            set(java.util.Calendar.MINUTE, 0)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }.timeInMillis
                    }

                    val todaysSessions = allTimerSessions.filter { it.timestamp >= todayStart }

                    val primaryColor = MaterialTheme.colorScheme.primary
                    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
                    val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant

                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        val canvasWidth = size.width
                        val pieceWidth = canvasWidth / 24f
                        val spacing = 2.dp.toPx()
                        val squareWidth = pieceWidth - spacing
                        val squareHeight = 24.dp.toPx()

                        for (i in 0 until 24) {
                            val xOffset = i * pieceWidth

                            drawRect(
                                color = surfaceVariantColor,
                                topLeft = androidx.compose.ui.geometry.Offset(xOffset, 0f),
                                size = androidx.compose.ui.geometry.Size(squareWidth, squareHeight),
                            )

                            val hourStartMin = i * 60
                            val hourEndMin = (i + 1) * 60

                            todaysSessions.forEach { session ->
                                val sessionStartMin = ((session.timestamp - todayStart) / 60000L).toInt()
                                val sessionEndMin = sessionStartMin + (session.durationSeconds / 60)

                                val overlapStart = maxOf(hourStartMin, sessionStartMin)
                                val overlapEnd = minOf(hourEndMin, sessionEndMin)

                                if (overlapStart < overlapEnd) {
                                    val startFraction = (overlapStart - hourStartMin) / 60f
                                    val endFraction = (overlapEnd - hourStartMin) / 60f

                                    val startX = xOffset + startFraction * squareWidth
                                    val fillWidth = (endFraction - startFraction) * squareWidth

                                    drawRect(
                                        color = primaryColor,
                                        topLeft = androidx.compose.ui.geometry.Offset(startX, 0f),
                                        size = androidx.compose.ui.geometry.Size(fillWidth, squareHeight)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(0, 6, 12, 18, 24).forEach { hour ->
                            Text(
                                text = if (hour == 24) "24h" else "${hour}h",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 6. Activity Heatmap
        item {
            ActivityHeatmapSection(
                lazyListState = lazyListState,
                year = heatmapYear,
                month = heatmapMonth,
                isPersian = usePersianCalendar,
                sessions = allTimerSessions,
                todayDateStr = todayDateStr,
                onPrevMonth = {
                    if (usePersianCalendar) {
                        val (y, m) = PersianCalendarHelper.getOffsetPersianMonth(heatmapYear, heatmapMonth, -1)
                        heatmapYear = y; heatmapMonth = m
                    } else {
                        if (heatmapMonth == 1) { heatmapYear--; heatmapMonth = 12 }
                        else heatmapMonth--
                    }
                },
                onNextMonth = {
                    if (usePersianCalendar) {
                        val (y, m) = PersianCalendarHelper.getOffsetPersianMonth(heatmapYear, heatmapMonth, 1)
                        heatmapYear = y; heatmapMonth = m
                    } else {
                        if (heatmapMonth == 12) { heatmapYear++; heatmapMonth = 1 }
                        else heatmapMonth++
                    }
                },
                onToday = {
                    if (usePersianCalendar) {
                        heatmapYear = PersianCalendarHelper.getCurrentPersianYear()
                        heatmapMonth = PersianCalendarHelper.getCurrentPersianMonth()
                    } else {
                        val now = Calendar.getInstance()
                        heatmapYear = now.get(Calendar.YEAR)
                        heatmapMonth = now.get(Calendar.MONTH) + 1
                    }
                },
                onToggleCalendar = { viewModel.toggleUsePersianCalendar() }
            )
        }


        // Bottom space
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
    if (showSettingsDialog) {
        SettingsDialog(viewModel = viewModel, onDismiss = { showSettingsDialog = false })
    }
}

private data class DayCell(
    val seconds: Int,
    val isCurrentMonth: Boolean,
    val dateStr: String
)

private data class GridData(
    val cells: List<List<DayCell?>>,
    val maxSeconds: Int,
    val numWeeks: Int,
    val monthLabel: String,
    val quintiles: List<Int> = emptyList()
)

@Composable
private fun ActivityHeatmapSection(
    lazyListState: LazyListState,
    year: Int,
    month: Int,
    isPersian: Boolean,
    sessions: List<TimerSessionEntity>,
    todayDateStr: String,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit,
    onToggleCalendar: () -> Unit
) {
    val gridData = remember(year, month, isPersian, sessions) {
        computeGridData(year, month, isPersian, sessions)
    }
    val hasActivity = remember(gridData) {
        gridData.cells.flatten().any { it != null && it.seconds > 0 && it.isCurrentMonth }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "ACTIVITY HEATMAP",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onPrevMonth, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Default.KeyboardArrowLeft, contentDescription = "Previous month", tint = MaterialTheme.colorScheme.primary)
                }
                Text(
                    text = gridData.monthLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = onNextMonth, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, contentDescription = "Next month", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onToday, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Today", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onToggleCalendar, modifier = Modifier.size(28.dp)) {
                    Text(
                        if (isPersian) "EN" else "FA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            var tappedCell by remember { mutableStateOf<DayCell?>(null) }
            val isScrolling by remember { derivedStateOf { lazyListState.isScrollInProgress } }
            LaunchedEffect(isScrolling) { if (isScrolling) tappedCell = null }

            if (!hasActivity) {
                Text(
                    text = "No focus sessions this month.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                for (rowIdx in 0..6) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${rowIdx + 1}",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(14.dp),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.width(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            for (colIdx in 0 until gridData.numWeeks) {
                                val cell = gridData.cells[rowIdx][colIdx]
                                val isToday = cell != null && cell.dateStr == todayDateStr
                                val color = if (cell != null && cell.isCurrentMonth) {
                                    if (cell.seconds > 0) {
                                        val alpha = if (gridData.quintiles.isNotEmpty()) {
                                            val bucket = gridData.quintiles.indexOfFirst { cell.seconds <= it }
                                            when {
                                                bucket < 0 -> 1.0f
                                                bucket == 0 -> 0.15f
                                                bucket == 1 -> 0.35f
                                                bucket == 2 -> 0.55f
                                                bucket == 3 -> 0.75f
                                                else -> 1.0f
                                            }
                                        } else {
                                            0.55f
                                        }
                                        MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                } else Color.Transparent
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(color = color, shape = RoundedCornerShape(2.dp))
                                        .then(
                                            if (isToday) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                                            else Modifier
                                        )
                                        .then(
                                            if (cell != null && cell.isCurrentMonth) {
                                                Modifier.clickable { tappedCell = cell }
                                            } else Modifier
                                        )
                                )
                            }
                        }
                        Spacer(Modifier.weight(1f))
                    }
                    if (rowIdx < 6) Spacer(Modifier.height(2.dp))
                }
            }

            var showBanner by remember { mutableStateOf(false) }
            var bannerCell by remember { mutableStateOf<DayCell?>(null) }
            LaunchedEffect(tappedCell) {
                if (tappedCell != null) {
                    bannerCell = tappedCell
                    showBanner = true
                } else {
                    showBanner = false
                }
            }
            AnimatedVisibility(
                visible = showBanner,
                enter = slideInVertically(animationSpec = tween(250)) { -it / 4 } + fadeIn(animationSpec = tween(250)),
                exit = slideOutVertically(animationSpec = tween(250)) { -it / 4 } + fadeOut(animationSpec = tween(250))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bannerCell!!.dateStr,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = formatDuration(bannerCell!!.seconds),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Less", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    listOf(0.15f, 0.35f, 0.55f, 0.75f, 1.0f).forEach { alpha ->
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha), RoundedCornerShape(2.dp))
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Text("More", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(6.dp))
            Text(
                "1: Sat  2: Sun  3: Mon  4: Tue  5: Wed  6: Thu  7: Fri",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun computeGridData(
    year: Int,
    month: Int,
    isPersian: Boolean,
    sessions: List<TimerSessionEntity>
): GridData {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val cal = Calendar.getInstance()

    val (startDateStr, endDateStr) = if (isPersian) {
        PersianCalendarHelper.getGregorianDateRange(year, month)
    } else {
        cal.set(year, month - 1, 1)
        val start = sdf.format(cal.time)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val end = sdf.format(cal.time)
        start to end
    }

    val monthLabel = if (isPersian) {
        "${PersianCalendarHelper.monthNames[month - 1]} $year"
    } else {
        val names = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        "${names[month - 1]} $year"
    }

    val parsedStart = sdf.parse(startDateStr) ?: return GridData(emptyList(), 0, 0, monthLabel)
    val parsedEnd = sdf.parse(endDateStr) ?: return GridData(emptyList(), 0, 0, monthLabel)

    val perDaySeconds = mutableMapOf<String, Int>()
    sessions.forEach { session ->
        if (session.date >= startDateStr && session.date < endDateStr) {
            perDaySeconds[session.date] = (perDaySeconds[session.date] ?: 0) + session.durationSeconds
        }
    }
    val maxSeconds = perDaySeconds.values.maxOrNull() ?: 0

    cal.time = parsedStart
    val firstDow = cal.get(Calendar.DAY_OF_WEEK) % 7
    cal.add(Calendar.DAY_OF_MONTH, -firstDow)
    val firstTime = cal.time.time

    cal.time = parsedEnd
    val lastDow = cal.get(Calendar.DAY_OF_WEEK) % 7
    cal.add(Calendar.DAY_OF_MONTH, (6 - lastDow) % 7)
    val diffDays = ((cal.time.time - firstTime) / 86400000L).toInt()
    val numWeeks = diffDays / 7 + 1

    cal.timeInMillis = firstTime
    val monthPrefix = startDateStr.substring(0, 7)
    val cells = MutableList(7) { MutableList<DayCell?>(numWeeks) { null } }

    for (w in 0 until numWeeks) {
        for (d in 0..6) {
            val dateStr = sdf.format(cal.time)
            val isCurrent = if (isPersian) {
                val (y, m, _) = PersianCalendarHelper.getPersianDateParts(dateStr)
                y == year && m == month
            } else {
                dateStr.startsWith(monthPrefix)
            }
            cells[d][w] = DayCell(
                seconds = perDaySeconds[dateStr] ?: 0,
                isCurrentMonth = isCurrent,
                dateStr = dateStr
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val extracted = cells.flatten().filterNotNull()
        .filter { it.isCurrentMonth && it.seconds > 0 }
        .map { it.seconds }
        .sorted()
    val quintiles = if (extracted.size >= 5) {
        (1..4).map { extracted[(extracted.size * it / 5).coerceAtMost(extracted.size - 1)] }
    } else {
        emptyList()
    }

    return GridData(cells, maxSeconds, numWeeks, monthLabel, quintiles)
}

// ── Line Graph Helpers ──

private data class DailyCompletion(
    val dateStr: String,
    val day: Int,
    val completed: Int
)

private fun computeDailyCompletions(
    startDate: String,
    endDate: String,
    habits: List<HabitEntity>,
    allLogs: List<HabitLogEntity>
): List<DailyCompletion> {
    if (startDate.isBlank() || endDate.isBlank() || habits.isEmpty()) return emptyList()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val cal = Calendar.getInstance()
    val start = sdf.parse(startDate) ?: return emptyList()
    val end = sdf.parse(endDate) ?: return emptyList()

    val logMap = allLogs.filter { it.date in startDate..endDate }.groupBy { it.date }
    val results = mutableListOf<DailyCompletion>()
    cal.time = start
    while (!cal.time.after(end)) {
        val dateStr = sdf.format(cal.time)
        val dayLogs = logMap[dateStr] ?: emptyList()
        val completed = habits.count { habit ->
            dayLogs.any { it.habitId == habit.id && it.value >= habit.target }
        }
        results.add(DailyCompletion(dateStr, cal.get(Calendar.DAY_OF_MONTH), completed))
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    return results
}

private fun formatDuration(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return buildString {
        if (h > 0) append("${h}h ")
        if (m > 0 || h > 0) append("${m}m ")
        append("${s}s")
    }
}

// ── Task Completion Rate Helpers ──

private data class TaskCompletionPoint(
    val dateStr: String,
    val day: Int,
    val total: Int,
    val done: Int,
    val completionRate: Float
)

private fun computeTaskCompletionRate(
    startDate: String,
    endDate: String,
    allTasks: List<TaskEntity>
): List<TaskCompletionPoint> {
    if (startDate.isBlank() || endDate.isBlank()) return emptyList()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val cal = Calendar.getInstance()
    val start = sdf.parse(startDate) ?: return emptyList()
    val end = sdf.parse(endDate) ?: return emptyList()

    val tasksByDate = allTasks.groupBy { it.date }
    val results = mutableListOf<TaskCompletionPoint>()
    cal.time = start
    while (!cal.time.after(end)) {
        val dateStr = sdf.format(cal.time)
        val dayTasks = tasksByDate[dateStr] ?: emptyList()
        val total = dayTasks.count { !it.postponed && it.type != "NOTE" }
        val done = dayTasks.count { !it.postponed && it.status == "COMPLETED" && it.type != "NOTE" }
        results.add(
            TaskCompletionPoint(
                dateStr = dateStr,
                day = cal.get(Calendar.DAY_OF_MONTH),
                total = total,
                done = done,
                completionRate = if (total > 0) done.toFloat() / total.toFloat() else 0f
            )
        )
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    return results
}

// ── Habit Consistency Helpers ──

private data class HabitConsistencyData(
    val habitName: String,
    val consistency: Float,
    val currentStreak: Int,
    val bestStreak: Int,
    val doneDays: Int,
    val eligibleDays: Int
)

private fun computeHabitConsistency(
    startDate: String,
    endDate: String,
    habits: List<HabitEntity>,
    allLogs: List<HabitLogEntity>
): List<HabitConsistencyData> {
    if (startDate.isBlank() || endDate.isBlank() || habits.isEmpty()) return emptyList()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val cal = Calendar.getInstance()
    val start = sdf.parse(startDate) ?: return emptyList()
    val end = sdf.parse(endDate) ?: return emptyList()

    val logsByHabit = allLogs.filter { it.date in startDate..endDate }.groupBy { it.habitId }

    return habits.mapNotNull { habit ->
        val habitLogs = logsByHabit[habit.id] ?: emptyList()
        val logMap = habitLogs.associateBy { it.date }

        val eligibleDaysOfWeek = if (habit.recurrenceMode == "WEEKLY") {
            habit.recurrenceDaysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        } else null

        val allDates = mutableListOf<String>()
        cal.time = start
        while (!cal.time.after(end)) {
            val dateStr = sdf.format(cal.time)
            val isEligible = when (habit.recurrenceMode) {
                "ALWAYS" -> true
                "WEEKLY" -> {
                    val dow = when (cal.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.SUNDAY -> 1
                        Calendar.MONDAY -> 2
                        Calendar.TUESDAY -> 3
                        Calendar.WEDNESDAY -> 4
                        Calendar.THURSDAY -> 5
                        Calendar.FRIDAY -> 6
                        Calendar.SATURDAY -> 7
                        else -> -1
                    }
                    dow in eligibleDaysOfWeek!!
                }
                else -> false
            }
            if (isEligible) allDates.add(dateStr)
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        val eligibleDays = allDates.size
        if (eligibleDays == 0) return@mapNotNull null

        val doneDays = allDates.count { dateStr ->
            val log = logMap[dateStr]
            log != null && log.value >= habit.target
        }

        val todayStr = sdf.format(Calendar.getInstance().time)
        var currentStreak = 0
        val streakCal = Calendar.getInstance()
        val todayParsed = sdf.parse(todayStr) ?: return@mapNotNull null
        streakCal.time = todayParsed
        while (true) {
            val dateStr = sdf.format(streakCal.time)
            val log = logMap[dateStr]
            if (log != null && log.value >= habit.target) {
                currentStreak++
                streakCal.add(Calendar.DAY_OF_MONTH, -1)
            } else break
        }

        val doneLogs = habitLogs.filter { it.value >= habit.target }
            .sortedByDescending { it.date }
        var bestStreak = 0
        var run = 0
        var prevDate: String? = null
        val dayMs = 86400000L
        for (log in doneLogs) {
            if (prevDate == null) {
                run = 1
            } else {
                try {
                    val cur = sdf.parse(log.date)
                    val prev = sdf.parse(prevDate)
                    val diff = (prev.time - cur.time) / dayMs
                    if (diff == 1L) run++ else run = 1
                } catch (_: Exception) { run = 1 }
            }
            bestStreak = maxOf(bestStreak, run)
            prevDate = log.date
        }

        HabitConsistencyData(
            habitName = habit.name,
            consistency = if (eligibleDays > 0) doneDays.toFloat() / eligibleDays.toFloat() else 0f,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            doneDays = doneDays,
            eligibleDays = eligibleDays
        )
    }
}

private fun computeLineGraphRange(
    year: Int,
    month: Int,
    isPersian: Boolean,
    rangeMode: String
): Triple<String, String, String> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val cal = Calendar.getInstance()
    val now = sdf.format(cal.time)

    return when (rangeMode) {
        "7D" -> {
            cal.add(Calendar.DAY_OF_MONTH, -6)
            val start = sdf.format(cal.time)
            Triple(start, now, "Last 7 Days")
        }
        "30D" -> {
            cal.add(Calendar.DAY_OF_MONTH, -29)
            val start = sdf.format(cal.time)
            Triple(start, now, "Last 30 Days")
        }
        else -> {
            val (start, end) = if (isPersian) {
                PersianCalendarHelper.getGregorianDateRange(year, month)
            } else {
                cal.set(year, month - 1, 1)
                val s = sdf.format(cal.time)
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                s to sdf.format(cal.time)
            }
            val label = if (isPersian) {
                "${PersianCalendarHelper.monthNames[month - 1]} $year"
            } else {
                val names = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
                "${names[month - 1]} $year"
            }
            Triple(start, end, label)
        }
    }
}

// ── Sleep Data Helpers ──

private data class SleepDailyPoint(
    val date: String,
    val day: Int,
    val duration: Float?,
    val quality: Float?,
    val bedMinutes: Float?,
    val wakeMinutes: Float?
)

private fun parseTimeToMinutes(time: String): Float? {
    if (time.isBlank()) return null
    val parts = time.split(":")
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    return (h * 60 + m).toFloat()
}

private fun formatMinutesToTime(minutes: Float): String {
    val totalMin = minutes.toInt().coerceIn(0, 24 * 60 - 1)
    val h = totalMin / 60
    val m = totalMin % 60
    return "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"
}

private fun computeSleepDailyData(
    startDate: String,
    endDate: String,
    sleepLogs: List<SleepLogEntity>
): List<SleepDailyPoint> {
    if (startDate.isBlank() || endDate.isBlank()) return emptyList()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val cal = Calendar.getInstance()
    val start = sdf.parse(startDate) ?: return emptyList()
    val end = sdf.parse(endDate) ?: return emptyList()

    val logMap = sleepLogs.filter { it.date in startDate..endDate }.associateBy { it.date }
    val results = mutableListOf<SleepDailyPoint>()
    cal.time = start
    while (!cal.time.after(end)) {
        val dateStr = sdf.format(cal.time)
        val log = logMap[dateStr]
        results.add(
            SleepDailyPoint(
                date = dateStr,
                day = cal.get(Calendar.DAY_OF_MONTH),
                duration = log?.hoursSlept,
                quality = log?.sleepQuality?.toFloat(),
                bedMinutes = log?.sleepTime?.let { parseTimeToMinutes(it) },
                wakeMinutes = log?.wakeTime?.let { parseTimeToMinutes(it) }
            )
        )
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    return results
}
