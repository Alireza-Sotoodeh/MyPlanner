package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DayReviewScreen(
    viewModel: MainViewModel,
    initialDate: String? = null,
    onBack: () -> Unit
) {
    val todayDate by viewModel.todayDate.collectAsState()
    val currentDate = initialDate ?: todayDate
    val review by viewModel.reviewForDate(currentDate).collectAsState(initial = null)

    var showSettingsDialog by remember { mutableStateOf(false) }
    var good by remember { mutableStateOf(review?.good ?: "") }
    var bad by remember { mutableStateOf(review?.bad ?: "") }
    var improve by remember { mutableStateOf(review?.improve ?: "") }
    var gratitude by remember { mutableStateOf(review?.gratitude ?: "") }
    var moodRating by remember { mutableIntStateOf(review?.moodRating ?: 3) }
    var score by remember { mutableIntStateOf(review?.score ?: 5) }
    var notes by remember { mutableStateOf(review?.notes ?: "") }
    var savedMessage by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(review) {
        if (review != null) {
            good = review!!.good
            bad = review!!.bad
            improve = review!!.improve
            gratitude = review!!.gratitude
            moodRating = review!!.moodRating
            score = review!!.score
            notes = review!!.notes
        }
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    fun hasContent(): Boolean {
        return good.isNotBlank() || bad.isNotBlank() || improve.isNotBlank() ||
                gratitude.isNotBlank() || notes.isNotBlank() || moodRating != 3 || score != 5
    }

    fun save() {
        if (hasContent() && !isSaving) {
            isSaving = true
            viewModel.saveDayReview(currentDate, good.trim(), bad.trim(), improve.trim(), gratitude.trim(), moodRating, score, notes.trim())
            savedMessage = "Saved"
            isSaving = false
        }
    }

    fun isFutureDate(): Boolean {
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val reviewDate = fmt.parse(currentDate) ?: return false
            val today = fmt.parse(todayDate) ?: return false
            reviewDate.after(today)
        } catch (_: Exception) { false }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column {
                    Text(
                        text = "DAY REVIEW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = formatDisplayDate(currentDate),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                if (review != null && hasContent()) {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
                IconButton(onClick = { viewModel.selectTab(0); viewModel.selectDate(viewModel.todayDate.value) }) {
                    Icon(Icons.Default.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { showSettingsDialog = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                }
            }
        }

        if (isFutureDate()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Day review is not available for future dates.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
            return
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (savedMessage.isNotBlank()) {
                Text(savedMessage, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
            }

            SectionCard(
                title = "How was your day?",
                icon = Icons.Default.EditNote
            ) {
                ReviewField("What went well?", good, { good = it })
                Spacer(Modifier.height(8.dp))
                ReviewField("What was bad?", bad, { bad = it })
                Spacer(Modifier.height(8.dp))
                ReviewField("What could improve?", improve, { improve = it })
                Spacer(Modifier.height(8.dp))
                ReviewField("Gratitude", gratitude, { gratitude = it })
            }

            // Mood section — no card wrapper
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Mood",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val moodEmojis = listOf("😢", "😞", "😐", "🙂", "😁")
                    val moodLabels = listOf("Awful", "Bad", "Okay", "Good", "Great")
                    for (i in 0..4) {
                        val level = i + 1
                        val selected = moodRating == level
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .let { m ->
                                    if (selected) m.background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                        RoundedCornerShape(12.dp)
                                    ) else m
                                }
                                .clickable(enabled = !selected) { moodRating = level }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = moodEmojis[i],
                                fontSize = if (selected) 32.sp else 24.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = moodLabels[i],
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = if (selected) 0.8f else 0.45f
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Score section — no card wrapper
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Overall Score",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        when {
                            score <= 3 -> "Rough day"
                            score <= 6 -> "Okay day"
                            score >= 8 -> "Great day!"
                            else -> "Decent day"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = score.toFloat().coerceIn(1f, 10f),
                    onValueChange = { score = it.toInt().coerceIn(1, 10) },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("1", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    Text("5", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    Text("10", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            }

            SectionCard(
                title = "Notes",
                icon = Icons.Default.Notes
            ) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it.take(500) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 5,
                    placeholder = { Text("Write your thoughts...", fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
                Text(
                    "${notes.length}/500",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = if (notes.length > 450) 0.7f else 0.35f
                    ),
                    modifier = Modifier.fillMaxWidth().padding(end = 4.dp),
                    textAlign = TextAlign.End
                )
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = { save(); onBack() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = hasContent() && !isSaving
            ) {
                Text("SAVE & CLOSE", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Delete Review", fontWeight = FontWeight.Bold) },
            text = { Text("Delete day review for ${formatDisplayDate(currentDate)}?", fontSize = 14.sp) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteDayReview(currentDate); showDeleteConfirm = false; onBack() }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
    if (showSettingsDialog) {
        SettingsDialog(viewModel = viewModel, onDismiss = { showSettingsDialog = false })
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 14.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                content = content
            )
        }
    }
}

@Composable
private fun ReviewField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.take(200)) },
        modifier = Modifier.fillMaxWidth().height(80.dp),
        maxLines = 3,
        label = { Text(label, fontSize = 13.sp) },
        placeholder = { Text("Write...", fontSize = 13.sp) },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    )
}

private fun formatDisplayDate(dateStr: String): String {
    return try {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = fmt.parse(dateStr) ?: return dateStr
        val outFmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        outFmt.format(date)
    } catch (_: Exception) { dateStr }
}
