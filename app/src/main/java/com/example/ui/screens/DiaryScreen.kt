package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private class MarkdownVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val styled = buildAnnotatedString {
            val lines = text.text.split("\n")
            for ((i, line) in lines.withIndex()) {
                if (i > 0) append("\n")
                when {
                    line.startsWith("### ") -> {
                        withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)) {
                            append(line)
                        }
                    }
                    line.startsWith("## ") -> {
                        withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                            append(line)
                        }
                    }
                    line.startsWith("# ") -> {
                        withStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)) {
                            append(line)
                        }
                    }
                    line.startsWith("- ") || line.startsWith("\u2022 ") -> {
                        append("\u2022 ")
                        append(styleInline(line.removePrefix("- ").removePrefix("\u2022 ")))
                    }
                    Regex("""^\d+\.\s""").containsMatchIn(line) -> {
                        val num = line.substringBefore(".").trim()
                        val rest = line.substringAfter(".").trim()
                        withStyle(SpanStyle(color = Color(0xFF6750A4))) { append("$num. ") }
                        append(styleInline(rest))
                    }
                    line.trim() == "---" || line.trim() == "___" || line.trim() == "***" -> {
                        withStyle(SpanStyle(color = Color.Gray.copy(alpha = 0.4f))) { append(line) }
                    }
                    else -> append(styleInline(line))
                }
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }
}

private fun styleInline(text: String): AnnotatedString {
    return buildAnnotatedString {
        var remaining = text
        while (remaining.isNotEmpty()) {
            val boldIdx = remaining.indexOf("**")
            val italicIdx = remaining.indexOf("*")
            val codeIdx = remaining.indexOf("`")
            val strikeIdx = remaining.indexOf("~~")

            val candidates = mutableListOf<Pair<Int, String>>()
            if (boldIdx != -1) candidates.add(boldIdx to "bold")
            if (italicIdx != -1 && italicIdx != boldIdx) candidates.add(italicIdx to "italic")
            if (codeIdx != -1) candidates.add(codeIdx to "code")
            if (strikeIdx != -1) candidates.add(strikeIdx to "strike")

            if (candidates.isEmpty()) {
                append(remaining)
                break
            }

            val (start, type) = candidates.minBy { it.first }
            append(remaining.substring(0, start))

            val tokenLen = if (type == "bold" || type == "strike") 2 else 1
            val search = remaining.substring(start + tokenLen)
            val endIdx = when (type) {
                "bold" -> search.indexOf("**")
                "italic" -> search.indexOf("*")
                "code" -> search.indexOf("`")
                else -> search.indexOf("~~")
            }

            if (endIdx != -1) {
                val inner = search.substring(0, endIdx)
                when (type) {
                    "bold" -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(inner) }
                    "italic" -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(inner) }
                    "code" -> {
                        val fullMatch = remaining.substring(start, start + tokenLen + endIdx + 1)
                        withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0xFFE8E8E8))) {
                            append(fullMatch)
                        }
                    }
                    "strike" -> withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) { append(inner) }
                }
                remaining = search.substring(endIdx + tokenLen)
            } else {
                append(remaining.substring(start))
                remaining = ""
            }
        }
    }
}

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
    var contentValue by remember { mutableStateOf(TextFieldValue("")) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(currentDate) {
        val savedEntry = viewModel.diaryEntryForDate(currentDate).first()
        title = savedEntry?.title ?: ""
        contentValue = TextFieldValue(savedEntry?.content ?: "")
    }

    var autoSaveJob by remember { mutableStateOf<Job?>(null) }

    fun triggerAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = scope.launch {
            delay(300)
            viewModel.saveDiaryEntry(currentDate, title.trim(), contentValue.text.trim())
        }
    }

    fun saveNow() {
        autoSaveJob?.cancel()
        viewModel.saveDiaryEntry(currentDate, title.trim(), contentValue.text.trim())
    }

    fun deleteEntry() {
        autoSaveJob?.cancel()
        viewModel.deleteDiaryEntry(currentDate)
        title = ""
        contentValue = TextFieldValue("")
    }

    fun navigateDate(offset: Int) {
        saveNow()
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        try { cal.time = fmt.parse(currentDate) ?: Calendar.getInstance().time } catch (_: Exception) {}
        cal.add(Calendar.DAY_OF_YEAR, offset)
        currentDate = fmt.format(cal.time)
    }

    fun wrapText(prefix: String, suffix: String) {
        val t = contentValue.text
        val sel = contentValue.selection
        val s = sel.start; val e = sel.end
        val sb = StringBuilder(t).apply { insert(e, suffix); insert(s, prefix) }
        val cursor = if (s == e) s + prefix.length else e + prefix.length + suffix.length
        contentValue = TextFieldValue(text = sb.toString(), selection = TextRange(cursor))
        triggerAutoSave()
    }

    fun formatBold() = wrapText("**", "**")
    fun formatItalic() = wrapText("*", "*")
    fun formatStrikethrough() = wrapText("~~", "~~")

    fun insertBullet() {
        val t = contentValue.text
        val c = contentValue.selection.start
        val ls = t.lastIndexOf('\n', c - 1) + 1
        val lEnd = t.indexOf('\n', ls).let { if (it == -1) t.length else it }
        if (t.substring(ls, lEnd).startsWith("- ")) return
        contentValue = TextFieldValue(
            text = t.substring(0, ls) + "- " + t.substring(ls),
            selection = TextRange(c + 2)
        )
        triggerAutoSave()
    }

    fun insertHeading() {
        val t = contentValue.text
        val c = contentValue.selection.start
        val ls = t.lastIndexOf('\n', c - 1) + 1
        val lEnd = t.indexOf('\n', ls).let { if (it == -1) t.length else it }
        val line = t.substring(ls, lEnd)
        if (line.startsWith("### ")) {
            contentValue = TextFieldValue(
                text = t.substring(0, ls) + line.substring(4) + t.substring(lEnd),
                selection = TextRange((c - 4).coerceAtLeast(ls))
            )
        } else {
            contentValue = TextFieldValue(
                text = t.substring(0, ls) + "### " + t.substring(ls),
                selection = TextRange(c + 4)
            )
        }
        triggerAutoSave()
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    DisposableEffect(currentDate) {
        onDispose { saveNow() }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Row 1: Label + actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 4.dp, top = 12.dp),
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
            IconButton(
                onClick = { showDeleteConfirm = true },
                enabled = entry != null || contentValue.text.isNotBlank() || title.isNotBlank()
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete entry")
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
                    tint = MaterialTheme.colorScheme.primary)
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

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            val buttons = listOf(
                Triple("B", "Bold") { formatBold() },
                Triple("I", "Italic") { formatItalic() },
                Triple("S", "Strikethrough") { formatStrikethrough() },
                Triple("\u2022", "Bullet") { insertBullet() },
                Triple("H", "Heading") { insertHeading() },
            )
            buttons.forEachIndexed { i, (label, desc, action) ->
                if (i > 0) Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .clickable(onClick = action),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        fontSize = 14.sp,
                        fontWeight = when (label) {
                            "B" -> FontWeight.ExtraBold
                            "H" -> FontWeight.Bold
                            else -> FontWeight.Normal
                        },
                        fontStyle = if (label == "I") FontStyle.Italic else FontStyle.Normal,
                        textDecoration = if (label == "S") TextDecoration.LineThrough else TextDecoration.None
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp)) {
            BasicTextField(
                value = contentValue,
                onValueChange = {
                    contentValue = it
                    triggerAutoSave()
                },
                modifier = Modifier.fillMaxSize(),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurface),
                visualTransformation = MarkdownVisualTransformation(),
                decorationBox = { innerTextField ->
                    if (contentValue.text.isEmpty()) {
                        Text(
                            "Write in markdown...",
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
}
