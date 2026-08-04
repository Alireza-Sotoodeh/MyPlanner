package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
    var isEditing by remember { mutableStateOf(true) }
    var saveState by remember { mutableStateOf("") }

    val dateSet = diaryDates.toSet()

    LaunchedEffect(entry) {
        if (entry != null) {
            title = entry!!.title
            content = entry!!.content
        } else {
            title = ""
            content = ""
        }
    }

    var saveJob by remember { mutableStateOf<Job?>(null) }

    fun triggerAutoSave() {
        saveJob?.cancel()
        saveJob = MainScope().launch {
            saveState = "Saving..."
            delay(2000)
            viewModel.saveDiaryEntry(currentDate, title.trim(), content.trim())
            saveState = "Saved"
        }
    }

    fun saveNow() {
        saveJob?.cancel()
        viewModel.saveDiaryEntry(currentDate, title.trim(), content.trim())
        saveState = "Saved"
    }

    fun deleteEntry() {
        saveJob?.cancel()
        viewModel.deleteDiaryEntry(currentDate)
        title = ""
        content = ""
        saveState = ""
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
        onDispose {
            saveNow()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 4.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { saveNow(); onBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DIARY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navigateDate(-1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous day")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(formatDisplayDate(currentDate), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        if (currentDate in dateSet) {
                            Text("●", fontSize = 8.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(onClick = { navigateDate(1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next day")
                    }
                }
            }
            if (entry != null || content.isNotBlank() || title.isNotBlank()) {
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete entry")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                when {
                    saveState == "Saving..." -> "Saving..."
                    saveState == "Saved" -> "Saved ✓"
                    else -> ""
                },
                fontSize = 11.sp,
                color = when (saveState) {
                    "Saving..." -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    "Saved" -> MaterialTheme.colorScheme.primary
                    else -> Color.Transparent
                }
            )
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilterChip(
                    selected = isEditing,
                    onClick = { if (!isEditing) { saveNow(); isEditing = true } },
                    label = { Text("Edit", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
                FilterChip(
                    selected = !isEditing,
                    onClick = { if (isEditing) { saveNow(); isEditing = false } },
                    label = { Text("Preview", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        if (isEditing) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; triggerAutoSave() },
                    placeholder = { Text("Title", fontSize = 16.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it; triggerAutoSave() },
                    placeholder = { Text("Write in markdown...", fontSize = 14.sp) },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, lineHeight = 22.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    )
                )
            }
        } else {
            MarkdownPreview(
                content = content,
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)
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
                TextButton(onClick = { deleteEntry(); showDeleteConfirm = false }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun MarkdownPreview(content: String, modifier: Modifier = Modifier) {
    if (content.isBlank()) {
        Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
            Text("Start writing...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                fontSize = 14.sp, modifier = Modifier.padding(top = 24.dp))
        }
        return
    }

    val lines = remember(content) { content.split("\n") }

    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        items(lines) { line ->
            when {
                line.startsWith("### ") -> Heading3Line(line.removePrefix("### "))
                line.startsWith("## ") -> Heading2Line(line.removePrefix("## "))
                line.startsWith("# ") -> Heading1Line(line.removePrefix("# "))
                line.startsWith("- ") || line.startsWith("• ") -> BulletLine(line.removePrefix("- ").removePrefix("• "))
                Regex("""^\d+\.\s""").containsMatchIn(line) -> {
                    val num = line.substringBefore(".").trim()
                    val text = line.substringAfter(".").trim()
                    NumberedLine(num, text)
                }
                line.trim() == "---" || line.trim() == "___" || line.trim() == "***" -> DividerLine()
                line.isBlank() -> Spacer(Modifier.height(8.dp))
                else -> ParagraphLine(line)
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun Heading1Line(text: String) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)) { append(text.trim()) }
        },
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun Heading2Line(text: String) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) { append(text.trim()) }
        },
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun Heading3Line(text: String) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp)) { append(text.trim()) }
        },
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun BulletLine(text: String) {
    val formatted = parseInlineMarkdown(text)
    Row(modifier = Modifier.padding(start = 12.dp, top = 2.dp, bottom = 2.dp)) {
        Text("•  ", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        Text(formatted, fontSize = 14.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun NumberedLine(number: String, text: String) {
    val formatted = parseInlineMarkdown(text)
    Row(modifier = Modifier.padding(start = 12.dp, top = 2.dp, bottom = 2.dp)) {
        Text("$number.  ", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        Text(formatted, fontSize = 14.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ParagraphLine(text: String) {
    val formatted = parseInlineMarkdown(text)
    if (text.isBlank()) {
        Spacer(Modifier.height(8.dp))
    } else {
        Text(
            formatted,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(vertical = 1.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DividerLine() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    )
}

@Composable
private fun parseInlineMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var remaining = text
        while (remaining.isNotEmpty()) {
            val boldStart = remaining.indexOf("**")
            val italicStart = remaining.indexOf("*")
            val effectiveItalic = if (italicStart != -1 && boldStart != -1) {
                if (italicStart < boldStart) italicStart else Int.MAX_VALUE
            } else if (italicStart != -1) italicStart else Int.MAX_VALUE
            val effectiveBold = boldStart

            if (effectiveBold != -1 && (effectiveBold < effectiveItalic || effectiveItalic == Int.MAX_VALUE)) {
                append(remaining.substring(0, effectiveBold))
                val rest = remaining.substring(effectiveBold + 2)
                val end = rest.indexOf("**")
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(rest.substring(0, end)) }
                    remaining = rest.substring(end + 2)
                } else {
                    append("**$rest")
                    remaining = ""
                }
            } else if (effectiveItalic != Int.MAX_VALUE) {
                append(remaining.substring(0, effectiveItalic))
                val rest = remaining.substring(effectiveItalic + 1)
                val end = rest.indexOf("*")
                if (end != -1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(rest.substring(0, end)) }
                    remaining = rest.substring(end + 1)
                } else {
                    append("*$rest")
                    remaining = ""
                }
            } else {
                append(remaining)
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
