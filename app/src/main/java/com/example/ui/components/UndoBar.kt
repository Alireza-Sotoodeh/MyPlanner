package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil

@Composable
fun UndoBar(
    message: String,
    countdownSeconds: Int,
    totalSeconds: Int = 5,
    onRestore: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val startFraction = remember {
        (countdownSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    }
    val progress = remember { Animatable(startFraction) }
    val progressValue by progress.asState()

    LaunchedEffect(startFraction) {
        val remainingMs = (startFraction * totalSeconds * 1000).toInt().coerceAtLeast(0)
        if (remainingMs > 0) {
            progress.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = remainingMs,
                    easing = LinearEasing
                )
            )
        }
        onDismiss()
    }

    val displaySeconds = ceil((progressValue * totalSeconds).toDouble()).toInt().coerceIn(0, totalSeconds)

    Card(
        modifier = modifier
            .padding(start = 16.dp, bottom = 4.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 8.dp, end = 2.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progressValue },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "$displaySeconds",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(6.dp))
            TextButton(
                onClick = onRestore,
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    "Restore",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
