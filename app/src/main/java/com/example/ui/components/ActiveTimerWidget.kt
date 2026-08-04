package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel
import java.util.Locale

@Composable
fun ActiveTimerWidget(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activePomodoroTask by viewModel.activePomodoroTask.collectAsState()
    val secondsLeft by viewModel.pomodoroSecondsLeft.collectAsState()
    val isPomodoroRunning by viewModel.pomodoroRunning.collectAsState()
    val pomodoroPhase by viewModel.pomodoroPhase.collectAsState()

    val chronoElapsed by viewModel.chronoElapsed.collectAsState()
    val isChronoRunning by viewModel.chronoRunning.collectAsState()
    val isChronoPaused by viewModel.chronoPaused.collectAsState()

    val isPomodoroActive = activePomodoroTask != null
    val isChronoActive = isChronoRunning || (!isChronoRunning && chronoElapsed > 0L)
    val isAnyActive = isPomodoroActive || isChronoActive

    var showStopConfirmation by remember { mutableStateOf(false) }
    var stopTarget by remember { mutableStateOf("") }

    AnimatedVisibility(
        visible = isAnyActive,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val timeStr = if (isPomodoroActive) {
            String.format(Locale.getDefault(), "%02d:%02d", secondsLeft / 60, secondsLeft % 60)
        } else {
            val h = chronoElapsed / 3600
            val m = (chronoElapsed % 3600) / 60
            val s = chronoElapsed % 60
            String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
        }

        val modeLabel = if (isPomodoroActive) "POMODORO" else "CHRONOMETER"
        val phaseLabel = if (isPomodoroActive) " · $pomodoroPhase" else ""
        val isTimerRunning = if (isPomodoroActive) isPomodoroRunning else (isChronoRunning && !isChronoPaused)

        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "$modeLabel$phaseLabel",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = timeStr,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
                            .clickable {
                                if (isPomodoroActive) {
                                    if (isPomodoroRunning) viewModel.pausePomodoro()
                                    else viewModel.resumePomodoro(context)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isTimerRunning) "Pause" else "Resume",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background)
                            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f), CircleShape)
                            .clickable {
                                stopTarget = if (isPomodoroActive) "pomodoro" else "chrono"
                                showStopConfirmation = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showStopConfirmation) {
        AlertDialog(
            onDismissRequest = { showStopConfirmation = false },
            title = {
                Text(
                    text = "End Session?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = if (stopTarget == "pomodoro") "Stop Pomodoro session early?" else "Stop chronometer and save elapsed time?",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (stopTarget == "pomodoro") {
                            viewModel.stopPomodoroEarly(context)
                        }
                        showStopConfirmation = false
                    }
                ) {
                    Text("Yes", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopConfirmation = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.primary)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
