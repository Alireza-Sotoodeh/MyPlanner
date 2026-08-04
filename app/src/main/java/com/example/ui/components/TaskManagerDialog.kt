package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import com.example.core.database.entity.TaskEntity

import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics

@Composable
fun HardwareAcceleratedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (visible) {
        androidx.compose.foundation.layout.Box(modifier = modifier.fillMaxWidth()) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskManagerDialog(
    viewModel: MainViewModel,
    initialDate: String,
    taskToEdit: TaskEntity? = null,
    initialSubtasks: List<TaskEntity> = emptyList(),
    onDismiss: () -> Unit
) {
    val customLabels by viewModel.customLabels.collectAsState()

    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var titleError by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var showDescription by remember { mutableStateOf(taskToEdit?.description?.isNotEmpty() == true) }
    var type by remember { mutableStateOf(taskToEdit?.type ?: "TASK") }
    var priorityLevel by remember { mutableStateOf(taskToEdit?.priorityLevel ?: "Medium") }
    
    // Label System
    var selectedLabel by remember { 
        mutableStateOf<Pair<String, Long>?>(
            taskToEdit?.let { t -> 
                if (t.label.isNotEmpty()) t.label to (t.labelColor ?: 0L) else null 
            }
        ) 
    }
    var showNewLabelDialog by remember { mutableStateOf(false) }
    var labelToEdit by remember { mutableStateOf<Pair<String, Long>?>(null) }
    var showEditLabelDialog by remember { mutableStateOf(false) }

    // Date
    var selectedDate by remember { mutableStateOf(taskToEdit?.date ?: initialDate) }
    var datePickerTarget by remember { mutableStateOf("NONE") } // NONE, START, END

    // Recurrence
    var recurrenceMode by remember { mutableStateOf(taskToEdit?.recurrenceMode ?: "NONE") }
    var recurrenceInterval by remember { mutableStateOf(taskToEdit?.recurrenceInterval ?: 1) }
    var recurrenceDaysOfWeek by remember { mutableStateOf(taskToEdit?.recurrenceDaysOfWeek ?: "") }
    var recurrenceEndDate by remember { mutableStateOf(taskToEdit?.recurrenceEndDate) }

    // Subtasks (Title to Importance)
    val subtasks = remember { mutableStateListOf<Pair<String, String>>().apply { addAll(initialSubtasks.map { it.title to it.subtaskImportance }) } }
    var showSubtasks by remember { mutableStateOf(subtasks.isNotEmpty()) }
    var newSubtask by remember { mutableStateOf("") }
    var newSubtaskImportance by remember { mutableStateOf("OPTIONAL") }
    var editingSubtaskIndex by remember { mutableStateOf<Int?>(null) }
    var editingSubtaskText by remember { mutableStateOf("") }
    var editingSubtaskImportance by remember { mutableStateOf("OPTIONAL") }
    
    // Event Time & Reminders
    var enableReminder by remember { mutableStateOf(taskToEdit?.reminderMinutesBefore != null || !taskToEdit?.eventTime.isNullOrBlank() || taskToEdit?.notifyNightBefore == true) }
    var eventTime by remember { mutableStateOf(taskToEdit?.eventTime ?: "") }
    var notifyNightBefore by remember { mutableStateOf(taskToEdit?.notifyNightBefore ?: false) }
    var reminderMinutesBefore by remember { mutableStateOf(taskToEdit?.reminderMinutesBefore?.toString() ?: "") }
    
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = eventTime.substringBefore(":").toIntOrNull() ?: 12,
        initialMinute = eventTime.substringAfter(":", "").toIntOrNull() ?: 0,
        is24Hour = true
    )

    val context = androidx.compose.ui.platform.LocalContext.current


    if (datePickerTarget != "NONE") {
        var isPersian by remember { mutableStateOf(false) }
        val targetDateStr = if (datePickerTarget == "START") selectedDate else (recurrenceEndDate ?: selectedDate)

        if (!isPersian) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(targetDateStr)?.time ?: System.currentTimeMillis()
            )
            DatePickerDialog(
                onDismissRequest = { datePickerTarget = "NONE" },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
                            if (datePickerTarget == "START") selectedDate = formatted else recurrenceEndDate = formatted
                        }
                        datePickerTarget = "NONE"
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { datePickerTarget = "NONE" }) {
                        Text("Cancel")
                    }
                }
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { isPersian = true }) { Text("Switch to Persian Calendar") }
                    }
                    DatePicker(state = datePickerState)
                }
            }
        } else {
            val initialParts = remember(targetDateStr) { com.example.core.utils.PersianCalendarHelper.getPersianDateParts(targetDateStr) }
            var pYear by remember { mutableStateOf(initialParts.first) }
            var pMonth by remember { mutableStateOf(initialParts.second) }
            var pDay by remember { mutableStateOf(initialParts.third) }
            
            AlertDialog(
                onDismissRequest = { datePickerTarget = "NONE" },
                title = { Text("Select Persian Date") },
                text = {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { isPersian = false }) { Text("Switch to Western Calendar") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = pYear.toString(),
                                onValueChange = { if (it.isEmpty()) pYear = 0 else it.toIntOrNull()?.let { y -> pYear = y } },
                                label = { Text("Year") },
                                modifier = Modifier.weight(1.2f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = pMonth.toString(),
                                onValueChange = { if (it.isEmpty()) pMonth = 0 else it.toIntOrNull()?.let { m -> if (m in 1..12) pMonth = m } },
                                label = { Text("Month") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = pDay.toString(),
                                onValueChange = { if (it.isEmpty()) pDay = 0 else it.toIntOrNull()?.let { d -> if (d in 1..31) pDay = d } },
                                label = { Text("Day") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (pYear > 0 && pMonth in 1..12 && pDay in 1..31) {
                            val greg = com.example.core.utils.PersianCalendarHelper.getGregorianDateString(pYear, pMonth, pDay)
                            if (greg.isNotBlank()) {
                                if (datePickerTarget == "START") selectedDate = greg else recurrenceEndDate = greg
                                datePickerTarget = "NONE"
                            }
                        }
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { datePickerTarget = "NONE" }) { Text("Cancel") }
                }
            )
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    val hour = timePickerState.hour
                    val min = timePickerState.minute
                    eventTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, min)
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showNewLabelDialog) {
        var newLabelName by remember { mutableStateOf("") }
        var selectedColor by remember { mutableStateOf(0xFF6750A4) }
        val colors = listOf(0xFF6750A4, 0xFF381E72, 0xFFEADDFF, 0xFF4F378B, 0xFFD0BCFF, 0xFFB3261E, 0xFFF9DEDC, 0xFF410E0B, 0xFF1D192B)
        
        AlertDialog(
            onDismissRequest = { showNewLabelDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("New Label") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newLabelName,
                        onValueChange = { newLabelName = it },
                        label = { Text("Label Name") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(colors) { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(color))
                                    .border(2.dp, if (selectedColor == color) MaterialTheme.colorScheme.onSurface else Color.Transparent, CircleShape)
                                    .clickable { selectedColor = color }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newLabelName.isNotBlank()) {
                        viewModel.updateCustomLabels(customLabels + (newLabelName.trim() to selectedColor))
                        selectedLabel = newLabelName.trim() to selectedColor
                    }
                    showNewLabelDialog = false
                }) { Text("ADD") }
            },
            dismissButton = {
                TextButton(onClick = { showNewLabelDialog = false }) { Text("CANCEL") }
            }
        )
    }

    if (showEditLabelDialog && labelToEdit != null) {
        var editLabelName by remember { mutableStateOf(labelToEdit!!.first) }
        var selectedColor by remember { mutableStateOf(labelToEdit!!.second) }
        val colors = listOf(0xFF6750A4, 0xFF381E72, 0xFFEADDFF, 0xFF4F378B, 0xFFD0BCFF, 0xFFB3261E, 0xFFF9DEDC, 0xFF410E0B, 0xFF1D192B)
        
        AlertDialog(
            onDismissRequest = { showEditLabelDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Edit Label") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editLabelName,
                        onValueChange = { editLabelName = it },
                        label = { Text("Label Name") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(colors) { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(color))
                                    .border(2.dp, if (selectedColor == color) MaterialTheme.colorScheme.onSurface else Color.Transparent, CircleShape)
                                    .clickable { selectedColor = color }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = {
                        val newLabels = customLabels.toMutableList()
                        newLabels.remove(labelToEdit)
                        viewModel.updateCustomLabels(newLabels)
                        if (selectedLabel == labelToEdit) {
                            selectedLabel = null
                        }
                        showEditLabelDialog = false
                    }) { Text("DELETE", color = MaterialTheme.colorScheme.error) }
                    
                    Row {
                        TextButton(onClick = { showEditLabelDialog = false }) { Text("CANCEL") }
                        TextButton(onClick = {
                            if (editLabelName.isNotBlank()) {
                                val newLabels = customLabels.toMutableList()
                                val index = newLabels.indexOf(labelToEdit)
                                if (index != -1) {
                                    val newLabel = editLabelName.trim() to selectedColor
                                    newLabels[index] = newLabel
                                    viewModel.updateCustomLabels(newLabels)
                                    if (selectedLabel == labelToEdit) {
                                        selectedLabel = newLabel
                                    }
                                }
                            }
                            showEditLabelDialog = false
                        }) { Text("SAVE") }
                    }
                }
            },
            dismissButton = {
                // Empty, handled in confirmButton Row
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(text = "Task Manager", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {


                // Type
                Text(text = "Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    listOf("TASK", "EVENT", "NOTE").forEach { item ->
                        val selected = type == item
                        val bgColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        val textColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .clickable { type = item }.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (item) {
                                        "TASK" -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .background(textColor, shape = androidx.compose.foundation.shape.CircleShape)
                                            )
                                        }
                                        "EVENT" -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .border(1.2.dp, textColor, shape = androidx.compose.foundation.shape.CircleShape)
                                            )
                                        }
                                        "NOTE" -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(width = 7.dp, height = 1.5.dp)
                                                    .background(textColor, shape = RoundedCornerShape(0.5.dp))
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = when (item) { "TASK" -> "Task"; "EVENT" -> "Event"; else -> "Note" },
                                    color = textColor,
                                    fontSize = 11.sp, fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Text("Date and Repetition:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    var recurrenceExpanded by remember { mutableStateOf(false) }
                    val isRepeating = recurrenceMode != "NONE"
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { recurrenceExpanded = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isRepeating) "Repeat every..." else "Do once (Specific date)")
                        }
                        DropdownMenu(
                            expanded = recurrenceExpanded,
                            onDismissRequest = { recurrenceExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Do once (Specific date)") },
                                onClick = { recurrenceMode = "NONE"; recurrenceExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Repeat every...") },
                                onClick = { recurrenceMode = "WEEKLY"; recurrenceExpanded = false }
                            )
                        }
                    }
                    
                    if (recurrenceMode == "NONE") {
                        val persianDateStr = remember(selectedDate) {
                            com.example.core.utils.PersianCalendarHelper.getPersianDateString(selectedDate)
                        }
                        val dayFormat = java.text.SimpleDateFormat("EEEE", Locale.getDefault())
                        val gregorianDay = remember(selectedDate) {
                            try {
                                val d = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
                                if (d != null) dayFormat.format(d) else ""
                            } catch (e: Exception) { "" }
                        }
                        val persianDay = remember(selectedDate) {
                            com.example.core.utils.PersianCalendarHelper.getPersianDayOfWeekName(selectedDate)
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = selectedDate, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text(text = persianDateStr, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "$gregorianDay / $persianDay", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { datePickerTarget = "START" }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Change Date")
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Text("Repeat every ", fontSize = 14.sp)
                            OutlinedTextField(
                                value = recurrenceInterval.toString(),
                                onValueChange = { recurrenceInterval = it.toIntOrNull() ?: 1 },
                                modifier = Modifier.width(70.dp).padding(horizontal = 8.dp),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            )
                            Text("week(s)", fontSize = 14.sp)
                        }
                        Text("On days:", fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            val daysList = listOf(
                                Triple(Calendar.SUNDAY, "S", "ی"),
                                Triple(Calendar.MONDAY, "M", "د"),
                                Triple(Calendar.TUESDAY, "T", "س"),
                                Triple(Calendar.WEDNESDAY, "W", "چ"),
                                Triple(Calendar.THURSDAY, "T", "پ"),
                                Triple(Calendar.FRIDAY, "F", "ج"),
                                Triple(Calendar.SATURDAY, "S", "ش")
                            )
                            val currentDays = recurrenceDaysOfWeek.split(",").filter { it.isNotBlank() }
                            daysList.forEach { (dayVal, enLabel, faLabel) ->
                                val isSelected = currentDays.contains(dayVal.toString())
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(36.dp).clickable {
                                        val mutableDays = currentDays.toMutableList()
                                        if (isSelected) mutableDays.remove(dayVal.toString()) else mutableDays.add(dayVal.toString())
                                        recurrenceDaysOfWeek = mutableDays.joinToString(",")
                                    }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = enLabel, fontSize = 12.sp, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Text("Start Date: $selectedDate", fontSize = 12.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = { datePickerTarget = "START" }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.DateRange, contentDescription = "Change Start Date", modifier = Modifier.size(20.dp))
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                            Text(if (recurrenceEndDate != null) "End Date: $recurrenceEndDate" else "End Date: Optional", fontSize = 12.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = { datePickerTarget = "END" }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.DateRange, contentDescription = "Change End Date", modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                HardwareAcceleratedVisibility(
                    visible = type == "EVENT"
                ) {
                    Column {
                        Text("Event Reminder:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { enableReminder = !enableReminder }) {
                            Checkbox(checked = enableReminder, onCheckedChange = { enableReminder = it })
                            Text("Enable Event Reminder", fontSize = 14.sp)
                        }
                        
                        if (enableReminder) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true }) {
                                OutlinedTextField(
                                    value = eventTime.ifBlank { "Select Time" },
                                    onValueChange = { },
                                    enabled = false,
                                    label = { Text("Time (HH:mm)") },
                                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = "Time") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { notifyNightBefore = !notifyNightBefore }) {
                                Checkbox(checked = notifyNightBefore, onCheckedChange = { notifyNightBefore = it })
                                Text("Remind me the night before", fontSize = 14.sp)
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = reminderMinutesBefore,
                                onValueChange = { reminderMinutesBefore = it.filter { char -> char.isDigit() } },
                                label = { Text("Minutes before event") },
                                placeholder = { Text("e.g. 45") },
                                leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = "Minutes Before") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { 
                                title = it
                                if (it.isNotBlank()) titleError = false
                            },
                            label = { Text("Title") },
                            isError = titleError,
                            supportingText = {
                                if (titleError) {
                                    Text("Title cannot be empty", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        HardwareAcceleratedVisibility(
                            visible = !showDescription || !showSubtasks
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AnimatedVisibility(
                                    visible = !showDescription,
                                    enter = fadeIn() + expandHorizontally(),
                                    exit = fadeOut() + shrinkHorizontally()
                                ) {
                                    TextButton(
                                        onClick = { showDescription = true },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add Description", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Description", fontSize = 12.sp)
                                    }
                                }
                                
                                AnimatedVisibility(
                                    visible = !showSubtasks,
                                    enter = fadeIn() + expandHorizontally(),
                                    exit = fadeOut() + shrinkHorizontally()
                                ) {
                                    TextButton(
                                        onClick = { showSubtasks = true },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add Subtasks", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Subtasks", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        HardwareAcceleratedVisibility(
                            visible = showDescription
                        ) {
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Description (Optional)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        HardwareAcceleratedVisibility(
                            visible = showSubtasks
                        ) {
                            Column {
                                Text("Subtasks:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    subtasks.forEachIndexed { index, sub ->
                                        if (editingSubtaskIndex == index) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                IconButton(
                                                    onClick = { editingSubtaskIndex = null },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Cancel Edit",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                
                                                OutlinedTextField(
                                                    value = editingSubtaskText,
                                                    onValueChange = { editingSubtaskText = it },
                                                    placeholder = { Text("Edit subtask...") },
                                                    modifier = Modifier.weight(1f),
                                                    singleLine = true,
                                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                                                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                                        onDone = {
                                                            if (editingSubtaskText.isNotBlank()) {
                                                                subtasks[index] = editingSubtaskText.trim() to editingSubtaskImportance
                                                                editingSubtaskIndex = null
                                                            }
                                                        }
                                                    )
                                                )
                                                
                                                var expandImportance by remember { mutableStateOf(false) }
                                                Box {
                                                    OutlinedButton(
                                                        onClick = { expandImportance = true },
                                                        modifier = Modifier.size(36.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                     ) {
                                                        Text(
                                                            when (editingSubtaskImportance) {
                                                                "IMPORTANT" -> "⭐"
                                                                "OPTIONAL" -> "☕"
                                                                else -> "—"
                                                            }, fontSize = 14.sp
                                                        )
                                                    }
                                                    DropdownMenu(expanded = expandImportance, onDismissRequest = { expandImportance = false }) {
                                                        DropdownMenuItem(text = { Text("☕ Optional") }, onClick = { editingSubtaskImportance = "OPTIONAL"; expandImportance = false })
                                                        DropdownMenuItem(text = { Text("⭐ Important") }, onClick = { editingSubtaskImportance = "IMPORTANT"; expandImportance = false })
                                                    }
                                                }
                                                
                                                IconButton(
                                                    onClick = {
                                                        if (editingSubtaskText.isNotBlank()) {
                                                            subtasks[index] = editingSubtaskText.trim() to editingSubtaskImportance
                                                            editingSubtaskIndex = null
                                                        }
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Save Edit",
                                                        tint = Color(0xFF4CAF50),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        } else {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                IconButton(
                                                    onClick = { subtasks.removeAt(index) }, 
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Text("-", color = Color(0xFF4CAF50), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                                
                                                IconButton(
                                                    onClick = {
                                                        editingSubtaskIndex = index
                                                        editingSubtaskText = sub.first
                                                        editingSubtaskImportance = sub.second
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Edit Subtask",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                
                                                Spacer(modifier = Modifier.width(8.dp))
                                                val importanceIcon = when (sub.second) {
                                                    "IMPORTANT" -> "⭐ "
                                                    "OPTIONAL" -> "☕ "
                                                    else -> ""
                                                }
                                                Text(
                                                    text = "$importanceIcon${sub.first}", 
                                                    fontSize = 14.sp, 
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable {
                                                            editingSubtaskIndex = index
                                                            editingSubtaskText = sub.first
                                                            editingSubtaskImportance = sub.second
                                                        }
                                                )
                                            }
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                if (newSubtask.isNotBlank()) {
                                                    subtasks.add(newSubtask.trim() to newSubtaskImportance)
                                                    newSubtask = ""
                                                    newSubtaskImportance = "OPTIONAL"
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Text("+", color = Color(0xFF4CAF50), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedTextField(
                                            value = newSubtask,
                                            onValueChange = { newSubtask = it },
                                            placeholder = { Text("Add subtask...") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                                onDone = {
                                                    if (newSubtask.isNotBlank()) {
                                                        subtasks.add(newSubtask.trim() to newSubtaskImportance)
                                                        newSubtask = ""
                                                        newSubtaskImportance = "OPTIONAL"
                                                    }
                                                }
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        
                                        var expandImportance by remember { mutableStateOf(false) }
                                        Box {
                                            OutlinedButton(
                                                onClick = { expandImportance = true },
                                                modifier = Modifier.size(36.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(
                                                    when (newSubtaskImportance) {
                                                        "IMPORTANT" -> "⭐"
                                                        "OPTIONAL" -> "☕"
                                                        else -> "—"
                                                    }, fontSize = 14.sp
                                                )
                                            }
                                            DropdownMenu(expanded = expandImportance, onDismissRequest = { expandImportance = false }) {
                                                DropdownMenuItem(text = { Text("☕ Optional") }, onClick = { newSubtaskImportance = "OPTIONAL"; expandImportance = false })
                                                DropdownMenuItem(text = { Text("⭐ Important") }, onClick = { newSubtaskImportance = "IMPORTANT"; expandImportance = false })
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                
                // Priority
                HardwareAcceleratedVisibility(
                    visible = type != "NOTE"
                ) {
                    Column {
                        Text("Priority:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val priorities = listOf("Low", "Medium", "High")
                        priorities.forEach { priority ->
                            val isSelected = priorityLevel == priority
                            val color = when (priority) {
                                "Low" -> Color(0xFF4CAF50)
                                "Medium" -> Color(0xFFFF9800)
                                "High" -> Color(0xFFF44336)
                                else -> MaterialTheme.colorScheme.primary
                            }
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, color) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { priorityLevel = priority }
                                    .padding(vertical = 4.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                    Text(
                                        text = priority,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                }
                
                // Labels
                Column {
                    Text("Labels:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp).height(32.dp)
                    ) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.clickable { showNewLabelDialog = true }.fillMaxHeight()
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                                    Text(text = "+ New", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        item {
                            val isSelected = selectedLabel == null
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.clickable { selectedLabel = null }.fillMaxHeight()
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                                    Text(
                                        text = "None",
                                        fontSize = 14.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        items(customLabels) { label ->
                            val isSelected = selectedLabel == label
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(label.second).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .combinedClickable(
                                        onClick = { selectedLabel = label },
                                        onLongClick = { 
                                            labelToEdit = label
                                            showEditLabelDialog = true
                                        }
                                    )
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                                    Text(
                                        text = label.first,
                                        fontSize = 14.sp,
                                        color = if (isSelected) Color(label.second) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isBlank()) {
                    titleError = true
                } else {
                    if (newSubtask.isNotBlank()) {
                        subtasks.add(newSubtask.trim() to newSubtaskImportance)
                        newSubtask = ""
                        newSubtaskImportance = "OPTIONAL"
                    }
                    if (taskToEdit != null) {
                        viewModel.updateTaskWithSubtasks(
                            task = taskToEdit,
                            title = title,
                            description = description,
                            date = selectedDate,
                            type = type,
                            label = selectedLabel?.first ?: "",
                            labelColor = selectedLabel?.second,
                            subtasks = subtasks.toList(),
                            recurrenceMode = recurrenceMode,
                            recurrenceInterval = recurrenceInterval,
                            recurrenceDaysOfWeek = recurrenceDaysOfWeek,
                            recurrenceEndDate = recurrenceEndDate,
                            eventTime = eventTime.takeIf { type == "EVENT" && enableReminder && it.isNotBlank() },
                            notifyNightBefore = if (type == "EVENT" && enableReminder) notifyNightBefore else false,
                            reminderMinutesBefore = if (type == "EVENT" && enableReminder) reminderMinutesBefore.toIntOrNull() else null,
                            priorityLevel = priorityLevel
                        )
                    } else {
                        viewModel.addTask(
                            title = title,
                            description = description,
                            date = selectedDate,
                            type = type,
                            duration = 25,
                            label = selectedLabel?.first ?: "",
                            labelColor = selectedLabel?.second,
                            subtasks = subtasks.toList(),
                            recurrenceMode = recurrenceMode,
                            recurrenceInterval = recurrenceInterval,
                            recurrenceDaysOfWeek = recurrenceDaysOfWeek,
                            recurrenceEndDate = recurrenceEndDate,
                            eventTime = eventTime.takeIf { type == "EVENT" && enableReminder && it.isNotBlank() },
                            notifyNightBefore = if (type == "EVENT" && enableReminder) notifyNightBefore else false,
                            reminderMinutesBefore = if (type == "EVENT" && enableReminder) reminderMinutesBefore.toIntOrNull() else null,
                            priorityLevel = priorityLevel
                        )
                    }
                    onDismiss()
                }
            }) { Text("SAVE") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        }
    )
}
