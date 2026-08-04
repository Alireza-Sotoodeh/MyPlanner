package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private fun formatDisplayDate(dateStr: String): String {
    return try {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = fmt.parse(dateStr) ?: return dateStr
        val outFmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        outFmt.format(date)
    } catch (_: Exception) { dateStr }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val todayDate by viewModel.todayDate.collectAsState()

    var currentDate by remember { mutableStateOf(todayDate) }
    val entry by viewModel.diaryEntryForDate(currentDate).collectAsState(initial = null)

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(currentDate) {
        val savedEntry = viewModel.diaryEntryForDate(currentDate).first()
        title = savedEntry?.title ?: ""
        content = savedEntry?.content ?: ""
    }

    var autoSaveJob by remember { mutableStateOf<Job?>(null) }

    fun triggerAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = scope.launch {
            delay(300)
            viewModel.saveDiaryEntry(currentDate, title.trim(), content.trim())
        }
    }

    fun saveNow() {
        autoSaveJob?.cancel()
        viewModel.saveDiaryEntry(currentDate, title.trim(), content.trim())
    }

    fun deleteEntry() {
        autoSaveJob?.cancel()
        viewModel.deleteDiaryEntry(currentDate)
        title = ""
        content = ""
    }

    fun navigateDate(offset: Int) {
        saveNow()
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        try { cal.time = fmt.parse(currentDate) ?: Calendar.getInstance().time } catch (_: Exception) {}
        cal.add(Calendar.DAY_OF_YEAR, offset)
        currentDate = fmt.format(cal.time)
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Row 1: Label + actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { saveNow(); onBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "DIARY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.selectTab(0); viewModel.selectDate(viewModel.todayDate.value) }) {
                Icon(Icons.Default.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            }
            IconButton(
                onClick = { showDeleteConfirm = true },
                enabled = entry != null || content.isNotBlank() || title.isNotBlank()
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete entry")
            }
            IconButton(onClick = { showSettingsDialog = true }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            }
        }

        // Row 2: Centered date navigation
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = { navigateDate(-1) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous day")
            }
            IconButton(
                onClick = { saveNow(); currentDate = todayDate },
                enabled = currentDate != todayDate
            ) {
                Icon(Icons.Default.Home, contentDescription = "Go to today",
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            }
            Text(
                formatDisplayDate(currentDate),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { saveNow(); showDatePicker = true }
            )
            IconButton(onClick = { navigateDate(1) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next day")
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                triggerAutoSave()
            },
            placeholder = { Text("Title", fontSize = 16.sp) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent
            )
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

        Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp)) {
            BasicTextField(
                value = content,
                onValueChange = { newValue ->
                    content = newValue
                    triggerAutoSave()
                },
                modifier = Modifier.fillMaxSize(),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurface),
                decorationBox = { innerTextField ->
                    if (content.isEmpty()) {
                        Text(
                            "Write your thoughts...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                    innerTextField()
                }
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Delete Entry", fontWeight = FontWeight.Bold) },
            text = { Text("Delete diary entry for ${formatDisplayDate(currentDate)}?", fontSize = 14.sp) },
            confirmButton = {
                TextButton(onClick = { deleteEntry(); showDeleteConfirm = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(currentDate)?.time
                    ?: System.currentTimeMillis()
            } catch (_: Exception) { System.currentTimeMillis() }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance().apply { timeInMillis = millis }
                        saveNow()
                        currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
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
    if (showSettingsDialog) {
        SettingsDialog(viewModel = viewModel, onDismiss = { showSettingsDialog = false })
    }
}
