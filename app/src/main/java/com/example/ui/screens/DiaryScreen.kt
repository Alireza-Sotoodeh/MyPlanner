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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private enum class Field { TITLE, CONTENT }

private class HistoryStack(private val maxSize: Int = 50) {
    private val items = mutableListOf<String>()
    private var index = -1

    fun push(item: String) {
        if (index < items.size - 1) {
            items.subList(index + 1, items.size).clear()
        }
        items.add(item)
        if (items.size > maxSize) {
            items.removeAt(0)
        }
        index = items.size - 1
    }

    fun undo(): String? {
        if (index > 0) {
            index--
            return items[index]
        }
        return null
    }

    fun redo(): String? {
        if (index < items.size - 1) {
            index++
            return items[index]
        }
        return null
    }

    val canUndo: Boolean get() = index > 0
    val canRedo: Boolean get() = index < items.size - 1
}

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
    val diaryDates by viewModel.diaryAllDates.collectAsState()

    var currentDate by remember { mutableStateOf(todayDate) }
    val entry by viewModel.diaryEntryForDate(currentDate).collectAsState(initial = null)

    var title by remember { mutableStateOf(entry?.title ?: "") }
    var content by remember { mutableStateOf(entry?.content ?: "") }

    val dateSet = diaryDates.toSet()
    val scope = rememberCoroutineScope()

    val titleHistory = remember { HistoryStack() }
    val contentHistory = remember { HistoryStack() }
    var lastModifiedField by remember { mutableStateOf(Field.CONTENT) }

    LaunchedEffect(entry) {
        val savedTitle = entry?.title ?: ""
        val savedContent = entry?.content ?: ""
        title = savedTitle
        content = savedContent
        titleHistory.push(savedTitle)
        contentHistory.push(savedContent)
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
            val canUndo = when (lastModifiedField) {
                Field.TITLE -> titleHistory.canUndo
                Field.CONTENT -> contentHistory.canUndo
            }
            val canRedo = when (lastModifiedField) {
                Field.TITLE -> titleHistory.canRedo
                Field.CONTENT -> contentHistory.canRedo
            }
            IconButton(
                onClick = {
                    val result = when (lastModifiedField) {
                        Field.TITLE -> titleHistory.undo()
                        Field.CONTENT -> contentHistory.undo()
                    }
                    result?.let {
                        when (lastModifiedField) {
                            Field.TITLE -> title = it
                            Field.CONTENT -> content = it
                        }
                    }
                },
                enabled = canUndo
            ) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
            }
            IconButton(
                onClick = {
                    val result = when (lastModifiedField) {
                        Field.TITLE -> titleHistory.redo()
                        Field.CONTENT -> contentHistory.redo()
                    }
                    result?.let {
                        when (lastModifiedField) {
                            Field.TITLE -> title = it
                            Field.CONTENT -> content = it
                        }
                    }
                },
                enabled = canRedo
            ) {
                Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
            }
            IconButton(
                onClick = { showDeleteConfirm = true },
                enabled = entry != null || content.isNotBlank() || title.isNotBlank()
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(formatDisplayDate(currentDate), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                if (currentDate in dateSet) {
                    Spacer(Modifier.width(4.dp))
                    Text("\u25CF", fontSize = 8.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            IconButton(onClick = { navigateDate(1) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next day")
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                titleHistory.push(it)
                lastModifiedField = Field.TITLE
                triggerAutoSave()
            },
            placeholder = { Text("Title", fontSize = 16.sp) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
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
                onValueChange = {
                    content = it
                    contentHistory.push(it)
                    lastModifiedField = Field.CONTENT
                    triggerAutoSave()
                },
                modifier = Modifier.fillMaxSize(),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, lineHeight = 22.sp),
                visualTransformation = MarkdownVisualTransformation(),
                decorationBox = { innerTextField ->
                    if (content.isEmpty()) {
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
}
