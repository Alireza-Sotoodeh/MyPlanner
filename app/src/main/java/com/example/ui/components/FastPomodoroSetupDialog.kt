package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.TaskEntity
import com.example.ui.viewmodel.MainViewModel

@Composable
fun FastPomodoroSetupDialog(
    task: TaskEntity,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val templates by viewModel.timerTemplates.collectAsState()

    var selectedTemplateId by remember { mutableStateOf<Long?>(null) }
    var focusMinutes by remember(task) { mutableIntStateOf(task.durationMinutes.takeIf { it > 0 } ?: 25) }
    var shortBreakMinutes by remember(task) { mutableStateOf<Int?>(task.breakMinutes) }
    var longBreakMinutes by remember(task) { mutableStateOf<Int?>(null) }
    var targetSessions by remember(task) { mutableStateOf<Int?>(task.targetSessions) }
    var markCompleteOnFinish by remember { mutableStateOf(false) }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Fast Set Up",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Set up timer for:\n\"${task.title}\"",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                TemplateSelector(
                    templates = templates,
                    selectedTemplateId = selectedTemplateId,
                    onSelectedTemplateIdChange = { templateId ->
                        selectedTemplateId = templateId
                        templateId?.let { id ->
                            val template = templates.find { it.id == id }
                            if (template != null) {
                                focusMinutes = template.focusMinutes
                                shortBreakMinutes = template.shortBreakMinutes
                                longBreakMinutes = template.longBreakMinutes
                                targetSessions = template.targetSessions
                            }
                        }
                    },
                    onManageClick = {
                        viewModel.setPreSelectedTaskForTimer(task.id)
                        onDismiss()
                    },
                    focusMinutes = focusMinutes,
                    shortBreakMinutes = shortBreakMinutes,
                    longBreakMinutes = longBreakMinutes,
                    targetSessions = targetSessions
                )

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        onClick = { markCompleteOnFinish = !markCompleteOnFinish },
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

                TimeControlRow("Focus", focusMinutes, 5, 120) { focusMinutes = it }
                TimeControlRowNullable("Short Break", shortBreakMinutes, 0, 30, onValueChange = { shortBreakMinutes = it })
                TimeControlRowNullable("Long Break", longBreakMinutes, 0, 30, onValueChange = { longBreakMinutes = it })
                TimeControlRowNullable("Target Sessions", targetSessions, 0, 99, onValueChange = { targetSessions = it }, step = 1, valueSuffix = "session")

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
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
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Focus", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.startChronometer(task.id)
                        viewModel.setPreferredTimerTab(1)
                        viewModel.setPreSelectedTaskForTimer(task.id)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Chronometer", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
