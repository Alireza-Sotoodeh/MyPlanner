package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.icu.util.ULocale
import com.example.core.utils.PersianCalendarHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarDatePickerDialog(
    highlightedDates: Set<String> = emptySet(),
    initialSelectedDate: String? = null,
    initialUsePersian: Boolean = false,
    minDate: String? = null,
    maxDate: String? = null,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    var usePersian by remember { mutableStateOf(initialUsePersian) }

    val today = Calendar.getInstance()

    val initialData = remember(initialSelectedDate, initialUsePersian) {
        if (initialSelectedDate != null) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdf.parse(initialSelectedDate)
                if (date != null) {
                    if (initialUsePersian) {
                        val parts = PersianCalendarHelper.getPersianDateParts(initialSelectedDate)
                        Triple(parts.first, parts.second, parts.third)
                    } else {
                        val c = Calendar.getInstance().apply { time = date }
                        Triple(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
                    }
                } else null
            } catch (_: Exception) { null }
        } else null
    }

    var currentYear by remember {
        mutableIntStateOf(
            initialData?.first ?: if (initialUsePersian) PersianCalendarHelper.getCurrentPersianYear()
            else today.get(Calendar.YEAR)
        )
    }
    var currentMonth by remember {
        mutableIntStateOf(
            initialData?.second ?: if (initialUsePersian) PersianCalendarHelper.getCurrentPersianMonth()
            else today.get(Calendar.MONTH) + 1
        )
    }
    var selectedDay by remember { mutableStateOf(initialData?.third) }
    var editingYear by remember { mutableStateOf(false) }
    var yearInput by remember { mutableStateOf("") }
    var showMonthPicker by remember { mutableStateOf(false) }

    fun toggleCalendar() {
        usePersian = !usePersian
        if (usePersian) {
            currentYear = PersianCalendarHelper.getCurrentPersianYear()
            currentMonth = PersianCalendarHelper.getCurrentPersianMonth()
        } else {
            val cal = Calendar.getInstance()
            currentYear = cal.get(Calendar.YEAR)
            currentMonth = cal.get(Calendar.MONTH) + 1
        }
        selectedDay = null
        editingYear = false
    }

    fun prevMonth() {
        if (usePersian) {
            val (y, m) = PersianCalendarHelper.getOffsetPersianMonth(currentYear, currentMonth, -1)
            currentYear = y; currentMonth = m
        } else {
            if (currentMonth == 1) { currentYear--; currentMonth = 12 }
            else currentMonth--
        }
        selectedDay = null
    }

    fun nextMonth() {
        if (usePersian) {
            val (y, m) = PersianCalendarHelper.getOffsetPersianMonth(currentYear, currentMonth, 1)
            currentYear = y; currentMonth = m
        } else {
            if (currentMonth == 12) { currentYear++; currentMonth = 1 }
            else currentMonth++
        }
        selectedDay = null
    }

    fun goToToday() {
        if (usePersian) {
            currentYear = PersianCalendarHelper.getCurrentPersianYear()
            currentMonth = PersianCalendarHelper.getCurrentPersianMonth()
        } else {
            val cal = Calendar.getInstance()
            currentYear = cal.get(Calendar.YEAR)
            currentMonth = cal.get(Calendar.MONTH) + 1
        }
        selectedDay = null
        editingYear = false
    }

    fun computeGregorian(day: Int): String {
        return if (usePersian) {
            PersianCalendarHelper.getGregorianDateString(currentYear, currentMonth, day)
        } else {
            val c = Calendar.getInstance()
            c.set(currentYear, currentMonth - 1, 1)
            val maxDay = c.getActualMaximum(Calendar.DAY_OF_MONTH)
            val clampedDay = day.coerceAtMost(maxDay)
            c.set(Calendar.DAY_OF_MONTH, clampedDay)
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.time)
        }
    }

    val isOkEnabled = remember(selectedDay, currentYear, currentMonth, usePersian, minDate, maxDate) {
        if (selectedDay == null) false
        else {
            val g = computeGregorian(selectedDay!!)
            g.isNotEmpty() &&
                (minDate == null || g >= minDate) &&
                (maxDate == null || g <= maxDate)
        }
    }

    if (showMonthPicker) {
        MonthPickerDialog(
            currentMonth = currentMonth,
            usePersian = usePersian,
            onConfirm = { m ->
                currentMonth = m
                selectedDay = null
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false }
        )
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                selectedDay?.let { day ->
                    val gregorian = computeGregorian(day)
                    if (gregorian.isNotEmpty()) onDateSelected(gregorian)
                }
            }, enabled = isOkEnabled) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            IconButton(onClick = { prevMonth() }) {
                Icon(Icons.AutoMirrored.Default.KeyboardArrowLeft, contentDescription = "Previous month")
            }

            val monthLabel = if (usePersian) {
                PersianCalendarHelper.monthNames.getOrElse(currentMonth - 1) { "" }
            } else {
                val c = Calendar.getInstance()
                c.set(currentYear, currentMonth - 1, 1)
                SimpleDateFormat("MMMM", Locale.getDefault()).format(c.time)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(
                    text = monthLabel,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable { showMonthPicker = true }
                )
                if (editingYear) {
                    val focusRequester = remember { FocusRequester() }
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }
                    BasicTextField(
                        value = yearInput,
                        onValueChange = { yearInput = it.filter { c -> c.isDigit() }.take(5) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                yearInput.toIntOrNull()?.let { y ->
                                    if (y > 0) { currentYear = y; selectedDay = null }
                                }
                                editingYear = false
                            }
                        ),
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .widthIn(min = 80.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                } else {
                    Text(
                        text = currentYear.toString(),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            yearInput = currentYear.toString()
                            editingYear = true
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }

            IconButton(onClick = { nextMonth() }) {
                Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, contentDescription = "Next month")
            }

            IconButton(onClick = { goToToday() }) {
                Icon(Icons.Default.Home, contentDescription = "Go to today", modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(4.dp))
            TextButton(onClick = { toggleCalendar() }) {
                Text(
                    text = if (usePersian) "EN" else "FA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        val calendarKey = "${currentYear}-${currentMonth}"
        AnimatedContent(
            targetState = calendarKey,
            transitionSpec = {
                val oldParts = initialState.split("-")
                val newParts = targetState.split("-")
                val oldTotal = oldParts[0].toInt() * 12 + oldParts[1].toInt()
                val newTotal = newParts[0].toInt() * 12 + newParts[1].toInt()
                if (newTotal > oldTotal) {
                    (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                }
            }
        ) {
            CalendarGrid(
                year = currentYear,
                month = currentMonth,
                usePersian = usePersian,
                selectedDay = selectedDay,
                highlightedDates = highlightedDates,
                onDaySelected = { day -> selectedDay = day }
            )
        }
    }
    }
}

@Composable
fun MonthPickerDialog(
    currentMonth: Int,
    usePersian: Boolean,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMonth by remember { mutableStateOf(currentMonth) }

    val months = remember(usePersian) {
        if (usePersian) PersianCalendarHelper.monthNames.toList()
        else {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val fmt = SimpleDateFormat("MMMM", Locale.getDefault())
            (0..11).map {
                cal.set(Calendar.MONTH, it)
                fmt.format(cal.time)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (usePersian) "Select Persian Month" else "Select Month", fontWeight = FontWeight.Bold) },
        text = {
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(initialScale = 0.85f) + fadeIn()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = modifier
                ) {
                    for (row in 0..3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (col in 0..2) {
                                val monthIndex = row * 3 + col
                                val monthNumber = monthIndex + 1
                                val isSelected = monthNumber == selectedMonth
                                val isCurrentMonth = monthNumber == currentMonth

                                val bg = when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isCurrentMonth -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                                val textColor = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(bg)
                                        .clickable { selectedMonth = monthNumber }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = months[monthIndex],
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = textColor,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedMonth) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CalendarGrid(
    highlightedDates: Set<String> = emptySet(),
    year: Int,
    month: Int,
    usePersian: Boolean,
    selectedDay: Int?,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val daysInMonth: Int
    val firstDayOffset: Int

    if (usePersian) {
        val cal = android.icu.util.Calendar.getInstance(ULocale("fa_IR@calendar=persian"))
        cal.clear()
        cal.set(android.icu.util.Calendar.YEAR, year)
        cal.set(android.icu.util.Calendar.MONTH, month - 1)
        cal.set(android.icu.util.Calendar.DAY_OF_MONTH, 1)
        daysInMonth = cal.getActualMaximum(android.icu.util.Calendar.DAY_OF_MONTH)
        firstDayOffset = cal.get(android.icu.util.Calendar.DAY_OF_WEEK) % 7
    } else {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        firstDayOffset = cal.get(Calendar.DAY_OF_WEEK) - 1
    }

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val dayHeaders = if (usePersian) listOf("ج", "پ", "چ", "س", "د", "ی", "ش")
        else listOf("S", "M", "T", "W", "T", "F", "S")

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                dayHeaders.forEach { header ->
                    Text(
                        text = header,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        var day = 1
        val rowCount = (daysInMonth + firstDayOffset + 6) / 7

        for (row in 0 until rowCount) {
            Row(modifier = Modifier.fillMaxWidth()) {
                val cellDays = arrayOfNulls<Int>(7)
                if (usePersian) {
                    for (logicalCol in 0 until 7) {
                        val visualCol = 6 - logicalCol
                        cellDays[visualCol] = if (row == 0 && logicalCol < firstDayOffset) null
                        else if (day <= daysInMonth) { val d = day; day++; d }
                        else null
                    }
                } else {
                    for (col in 0 until 7) {
                        cellDays[col] = if (row == 0 && col < firstDayOffset) null
                        else if (day <= daysInMonth) { val d = day; day++; d }
                        else null
                    }
                }

                for (col in 0 until 7) {
                    val cellDay = cellDays[col]
                    val dateStr = if (cellDay != null) {
                        if (usePersian) PersianCalendarHelper.getGregorianDateString(year, month, cellDay)
                        else "%04d-%02d-%02d".format(year, month, cellDay)
                    } else null

                    val isToday = dateStr == todayStr
                    val isSelected = cellDay != null && cellDay == selectedDay
                    val hasHistory = dateStr in highlightedDates

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .then(
                                if (isToday && !isSelected) Modifier.background(primaryColor.copy(alpha = 0.15f))
                                else Modifier
                            )
                            .then(
                                if (isSelected) Modifier.background(primaryColor)
                                else Modifier
                            )
                            .then(
                                if (!isSelected && isToday) Modifier.border(
                                    1.5.dp, primaryColor, CircleShape
                                ) else Modifier
                            )
                            .clickable(enabled = cellDay != null) {
                                if (cellDay != null) onDaySelected(cellDay)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (cellDay != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (isSelected && isToday) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(onPrimaryColor.copy(alpha = 0.7f))
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                                Text(
                                    text = cellDay.toString(),
                                    fontSize = 14.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) onPrimaryColor else onSurfaceColor
                                )
                                if (hasHistory) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) onPrimaryColor else primaryColor
                                            )
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
