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
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.style.TextAlign
import com.example.core.utils.PersianCalendarHelper
import com.example.core.database.entity.TimerSessionEntity
import com.example.core.database.entity.HabitLogEntity
import com.example.core.database.entity.HabitEntity
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
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.roundToInt

@Composable
fun StatsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val totalScreenTimeMinutes by viewModel.totalScreenTimeMinutes.collectAsState()
    val appUsageItems by viewModel.appUsageItems.collectAsState()
    val screenTimeError by viewModel.screenTimeError.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val todayHabitLogs by viewModel.todayHabitLogs.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val allTimerSessions by viewModel.allTimerSessions.collectAsState()
    val usePersianCalendar by viewModel.usePersianCalendar.collectAsState()
    val todayDateStr by viewModel.todayDate.collectAsState()
    val allHabitLogs by viewModel.allHabitLogs.collectAsState()

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

    // Trigger update on screen load
    LaunchedEffect(Unit) {
        viewModel.updateAppUsage(context)
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

                    if (screenTimeError != null) {
                        Text(
                            text = screenTimeError!!,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        val hours = totalScreenTimeMinutes / 60
                        val minutes = totalScreenTimeMinutes % 60
                        val formattedTime = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

                        Text(
                            text = formattedTime,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.primary
                        )

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
                                text = "No usage statistics available.",
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
                        val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant
                        val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
                        val maxY = remember(dailyData) { dailyData.maxOfOrNull { it.completed } ?: 1 }
                        val trailingAvg = remember(dailyData) {
                            if (dailyData.size >= 7) dailyData.takeLast(7).map { it.completed }.average().toFloat()
                            else null
                        }
                        val monthCompletedDays = remember(dailyData) { dailyData.count { it.completed > 0 } }

                        HabitLineGraphCanvas(
                            data = dailyData,
                            maxY = maxY,
                            trailingAvg = trailingAvg,
                            primaryColor = primaryColor,
                            surfaceVariantColor = surfaceVariantColor,
                            onSurfaceColor = onSurfaceColor
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

        // 3. Bullet Journal Accomplishment Ratio
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
                        text = "BULLET TASK ACCOMPLISHMENTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val tasks = allTasks.filter { it.date == todayDateStr }
                    if (tasks.isEmpty()) {
                        Text(
                            text = "Create and complete intentions to compile stats.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val completed = tasks.count { it.status == "COMPLETED" }
                        val total = tasks.size
                        val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${(progress * 100).toInt()}% productivity",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$completed of $total done",
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
                    }
                }
            }
        }

        // 4. Time Spent by Label (Donut Chart) — from actual timer sessions
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
                }
            }
        }

        // 5. Activity Heatmap
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

        // 6. Activity Time of Day (24h Timeline) — from timer sessions
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

@Composable
private fun HabitLineGraphCanvas(
    data: List<DailyCompletion>,
    maxY: Int,
    trailingAvg: Float?,
    primaryColor: Color,
    surfaceVariantColor: Color,
    onSurfaceColor: Color
) {
    var tooltipIndex by remember { mutableStateOf<Int?>(null) }
    val labelPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }

    if (data.isEmpty()) {
        Text(
            text = "No habit data for this period.",
            fontSize = 13.sp,
            color = onSurfaceColor
        )
        return
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        if (data.size < 2) return@detectTapGestures
                        val leftPad = 40.dp.toPx()
                        val drawW = size.width - leftPad - 16.dp.toPx()
                        val stepW = drawW / (data.size - 1)
                        val idx = ((offset.x - leftPad) / stepW).roundToInt()
                            .coerceIn(0, data.size - 1)
                        tooltipIndex = if (tooltipIndex == idx) null else idx
                    }
                }
        ) {
            val leftPad = 40.dp.toPx()
            val rightPad = 16.dp.toPx()
            val topPad = 8.dp.toPx()
            val bottomPad = 24.dp.toPx()
            val drawW = size.width - leftPad - rightPad
            val drawH = size.height - topPad - bottomPad
            val maxYf = maxY.toFloat().coerceAtLeast(1f)

            val yStep = when {
                maxYf <= 3f -> 1
                maxYf <= 8f -> 2
                maxYf <= 20f -> 5
                else -> (maxYf / 4f).roundToInt().coerceAtLeast(5)
            }

            // Y-axis grid lines and labels
            var yVal = 0
            while (yVal <= maxYf.roundToInt()) {
                val yPos = topPad + drawH - (yVal.toFloat() / maxYf * drawH)
                drawLine(
                    color = surfaceVariantColor,
                    start = Offset(leftPad, yPos),
                    end = Offset(size.width - rightPad, yPos),
                    strokeWidth = 1.dp.toPx()
                )
                labelPaint.color = onSurfaceColor.hashCode()
                labelPaint.textSize = 9.sp.toPx()
                drawContext.canvas.nativeCanvas.drawText(
                    yVal.toString(),
                    leftPad - 8.dp.toPx(),
                    yPos + 3.dp.toPx(),
                    labelPaint
                )
                yVal += yStep
            }

            // Data points
            val points = data.mapIndexed { idx, d ->
                val x = leftPad + (idx.toFloat() / (data.size - 1).coerceAtLeast(1)) * drawW
                val y = topPad + drawH - (d.completed.toFloat() / maxYf * drawH)
                Offset(x, y)
            }

            if (points.size >= 2) {
                // Gradient fill under line
                val fillPath = Path().apply {
                    moveTo(points.first().x, topPad + drawH)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, topPad + drawH)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent),
                        endY = topPad + drawH
                    )
                )

                // Straight line segments connecting data points
                val linePath = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }
                drawPath(
                    path = linePath,
                    color = primaryColor,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // 7-day trailing average dashed line
                trailingAvg?.let { avg ->
                    if (data.size >= 7) {
                        val avgStartIdx = data.size - 7
                        val avgXStart = leftPad + (avgStartIdx.toFloat() / (data.size - 1)) * drawW
                        val avgY = topPad + drawH - (avg.toFloat() / maxYf * drawH)
                        drawLine(
                            color = primaryColor.copy(alpha = 0.5f),
                            start = Offset(avgXStart, avgY),
                            end = Offset(leftPad + drawW, avgY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                        )
                    }
                }
            }

            // Data point dots
            points.forEachIndexed { idx, pt ->
                val isSel = tooltipIndex == idx
                val radius = if (isSel) 5.dp.toPx() else 3.dp.toPx()
                drawCircle(color = primaryColor, radius = radius, center = pt)
                if (isSel) {
                    drawCircle(color = Color.White, radius = radius - 1.5.dp.toPx(), center = pt)
                    drawCircle(color = primaryColor, radius = radius - 1.5.dp.toPx(), center = pt)
                }
            }

            // X-axis day labels
            val labelIndices = data.indices.filter { idx ->
                val day = data[idx].day
                idx == 0 || idx == data.size - 1 || day % 5 == 0 || day == 1
            }
            labelPaint.textSize = 9.sp.toPx()
            labelPaint.color = onSurfaceColor.hashCode()
            labelIndices.forEach { idx ->
                val x = leftPad + (idx.toFloat() / (data.size - 1).coerceAtLeast(1)) * drawW
                drawContext.canvas.nativeCanvas.drawText(
                    data[idx].day.toString(),
                    x,
                    size.height - 4.dp.toPx(),
                    labelPaint
                )
            }

            // Tooltip on selected point
            tooltipIndex?.let { idx ->
                if (idx in points.indices) {
                    val pt = points[idx]
                    val d = data[idx]
                    val tipText = "${d.dateStr}: ${d.completed}"
                    labelPaint.textSize = 10.sp.toPx()
                    labelPaint.color = android.graphics.Color.WHITE
                    val textW = labelPaint.measureText(tipText)
                    val tipW = textW + 12.dp.toPx()
                    val tipH = 22.dp.toPx()
                    val tipX = (pt.x - tipW / 2f)
                        .coerceIn(4.dp.toPx(), size.width - tipW - 4.dp.toPx())
                    val tipY = pt.y - 12.dp.toPx() - tipH

                    drawRoundRect(
                        color = Color(0xDD333333),
                        topLeft = Offset(tipX, tipY),
                        size = Size(tipW, tipH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        tipText,
                        tipX + tipW / 2f,
                        tipY + tipH - 5.dp.toPx(),
                        labelPaint
                    )
                }
            }
        }
    }
}
