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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel

@Composable
fun StatsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val totalScreenTimeMinutes by viewModel.totalScreenTimeMinutes.collectAsState()
    val appUsageItems by viewModel.appUsageItems.collectAsState()
    val screenTimeError by viewModel.screenTimeError.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val habitLogs by viewModel.habitLogs.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()

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
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
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
                        val loggedCount = habitLogs.count { it.value > 0f }
                        val totalHabits = habits.size
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

        // 4. Time Spent by Label (Pie Chart)
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

                    Spacer(modifier = Modifier.height(16.dp))

                    val completedTasks = allTasks.filter { it.status == "COMPLETED" }
                    val labelGroups = completedTasks.groupBy { if (it.label.isBlank()) "Unlabeled" else it.label.uppercase() }
                    
                    val labelStats = labelGroups.map { entry ->
                        val duration = entry.value.sumOf { it.durationMinutes }
                        val colorLong = entry.value.firstNotNullOfOrNull { it.labelColor }
                        val color = if (colorLong != null) Color(colorLong) else MaterialTheme.colorScheme.primary
                        Triple(entry.key, duration, color)
                    }.filter { it.second > 0 }.sortedByDescending { it.second }

                    if (labelStats.isEmpty()) {
                        Text(
                            text = "Complete tasks with durations to see label analytics.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val totalDuration = labelStats.sumOf { it.second }.toFloat()
                        val defaultColors = listOf(
                            Color(0xFF6750A4), Color(0xFFD0BCFF), Color(0xFF381E72),
                            Color(0xFFEADDFF), Color(0xFF4F378B), Color(0xFF21005D)
                        )
                        
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                    var startAngle = -90f
                                    labelStats.forEachIndexed { index, stat ->
                                        val sweepAngle = (stat.second / totalDuration) * 360f
                                        val chartColor = if (stat.first == "UNLABELED") defaultColors[index % defaultColors.size] else stat.third
                                        drawArc(
                                            color = chartColor,
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle,
                                            useCenter = true
                                        )
                                        startAngle += sweepAngle
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(24.dp))
                            
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                labelStats.forEachIndexed { index, stat ->
                                    val chartColor = if (stat.first == "UNLABELED") defaultColors[index % defaultColors.size] else stat.third
                                    val hours = stat.second / 60
                                    val mins = stat.second % 60
                                    val timeString = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(chartColor))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = stat.first,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = timeString,
                                                fontSize = 11.sp,
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


        // 5. Activity Time of Day (24h Timeline)
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

                    val todaysTasks = allTasks.filter { it.status == "COMPLETED" && it.createdAt >= todayStart }
                    
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
                    val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant

                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp) // Total height including text
                    ) {
                        val canvasWidth = size.width
                        val pieceWidth = canvasWidth / 24f
                        val spacing = 2.dp.toPx()
                        val squareWidth = pieceWidth - spacing
                        val squareHeight = 24.dp.toPx()
                        
                        for (i in 0 until 24) {
                            val xOffset = i * pieceWidth
                            
                            // Draw empty square
                            drawRect(
                                color = surfaceVariantColor,
                                topLeft = androidx.compose.ui.geometry.Offset(xOffset, 0f),
                                size = androidx.compose.ui.geometry.Size(squareWidth, squareHeight),
                            )
                            
                            // Find tasks that overlap with hour i
                            val hourStartMin = i * 60
                            val hourEndMin = (i + 1) * 60
                            
                            todaysTasks.forEach { task ->
                                val taskStartMin = ((task.createdAt - todayStart) / 60000L).toInt()
                                val taskEndMin = taskStartMin + task.durationMinutes
                                
                                val overlapStart = maxOf(hourStartMin, taskStartMin)
                                val overlapEnd = minOf(hourEndMin, taskEndMin)
                                
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
                    
                    // Hour labels
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
}
