package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
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

@Composable
fun StatsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val totalScreenTimeMinutes by viewModel.totalScreenTimeMinutes.collectAsState()
    val appUsageItems by viewModel.appUsageItems.collectAsState()
    val screenTimeError by viewModel.screenTimeError.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val habitLogs by viewModel.habitLogs.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val allTimerSessions by viewModel.allTimerSessions.collectAsState()
    val usePersianCalendar by viewModel.usePersianCalendar.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var heatmapYear by remember(usePersianCalendar) { mutableIntStateOf(if (usePersianCalendar) PersianCalendarHelper.getCurrentPersianYear() else Calendar.getInstance().get(Calendar.YEAR)) }
    var heatmapMonth by remember(usePersianCalendar) { mutableIntStateOf(if (usePersianCalendar) PersianCalendarHelper.getCurrentPersianMonth() else Calendar.getInstance().get(Calendar.MONTH) + 1) }

    // Trigger update on screen load
    LaunchedEffect(Unit) {
        viewModel.updateAppUsage(context)
    }



    LazyColumn(
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

        // 2. Habit Completion Progress
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
                        val activeHabits = remember(habits, selectedDate) {
                            if (selectedDate.isBlank()) return@remember habits
                            try {
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val parsedDate = sdf.parse(selectedDate) ?: return@remember habits
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
                        val loggedCount = habitLogs.count { log ->
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

                    val tasks = allTasks
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

                    val labelGroups = allTimerSessions.groupBy {
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
                year = heatmapYear,
                month = heatmapMonth,
                isPersian = usePersianCalendar,
                sessions = allTimerSessions,
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
    val isCurrentMonth: Boolean
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
    year: Int,
    month: Int,
    isPersian: Boolean,
    sessions: List<TimerSessionEntity>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit,
    onToggleCalendar: () -> Unit
) {
    val gridData = remember(year, month, isPersian, sessions) {
        computeGridData(year, month, isPersian, sessions)
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

            Row(modifier = Modifier.fillMaxWidth()) {
                (1..7).forEach { n ->
                    Text(
                        "$n",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.height(4.dp))

            for (rowIdx in 0..6) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (colIdx in 0 until gridData.numWeeks) {
                        val cell = gridData.cells[rowIdx][colIdx]
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
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            }
                        } else Color.Transparent
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(1.5.dp)
                                .background(color = color, shape = RoundedCornerShape(3.dp))
                        )
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
                isCurrentMonth = isCurrent
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
