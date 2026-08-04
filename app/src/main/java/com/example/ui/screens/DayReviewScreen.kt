package com.example.ui.screens

import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
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
        if (hasContent()) {
            viewModel.saveDayReview(currentDate, good.trim(), bad.trim(), improve.trim(), gratitude.trim(), moodRating, score, notes.trim())
            savedMessage = "Saved ✓"
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

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            IconButton(onClick = { save(); onBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
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

        if (isFutureDate()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Day review is not available for future dates.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
            return
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (savedMessage.isNotBlank()) {
                Text(savedMessage, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
            }

            ReviewField("What went well?", good, { good = it })
            ReviewField("What was bad?", bad, { bad = it })
            ReviewField("What could improve?", improve, { improve = it })
            ReviewField("Gratitude", gratitude, { gratitude = it })

            Text("Mood", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (i in 1..5) {
                    IconButton(onClick = { moodRating = i }, modifier = Modifier.size(40.dp)) {
                        Icon(
                            if (i <= moodRating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Star $i",
                            tint = if (i <= moodRating) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Text("Score: $score/10", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Slider(
                value = score.toFloat(),
                onValueChange = { score = it.toInt() },
                valueRange = 1f..10f,
                steps = 8,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Notes", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5,
                placeholder = { Text("Additional notes...", fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { save(); onBack() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = hasContent()
            ) {
                Text("SAVE & CLOSE", fontWeight = FontWeight.SemiBold)
            }
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
private fun ReviewField(label: String, value: String, onValueChange: (String) -> Unit) {
    Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().height(80.dp),
        maxLines = 3,
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
