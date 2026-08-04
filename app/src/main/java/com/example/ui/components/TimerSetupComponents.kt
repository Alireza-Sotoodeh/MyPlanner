package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.TimerTemplateEntity

@Composable
fun TemplateSelector(
    templates: List<TimerTemplateEntity>,
    selectedTemplateId: Long?,
    onSelectedTemplateIdChange: (Long?) -> Unit,
    onManageClick: () -> Unit,
    focusMinutes: Int,
    shortBreakMinutes: Int?,
    longBreakMinutes: Int?,
    targetSessions: Int?
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "TEMPLATE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = onManageClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Manage", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("Manage", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (templates.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No templates yet — tap Manage to create one",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                val customSubtitle = buildString {
                    append("${focusMinutes}m")
                    shortBreakMinutes?.let { append(" / ${it}m") } ?: append(" / —")
                    longBreakMinutes?.let { append(" / ${it}m") } ?: append(" / —")
                    append(" · ${targetSessions?.toString() ?: "∞"}")
                }
                TemplateChip(
                    label = "Custom",
                    subtitle = customSubtitle,
                    isSelected = selectedTemplateId == null,
                    onClick = { onSelectedTemplateIdChange(null) }
                )
                templates.forEach { template ->
                    TemplateChip(
                        label = template.name,
                        subtitle = "${template.focusMinutes}m focus",
                        isSelected = template.id == selectedTemplateId,
                        onClick = { onSelectedTemplateIdChange(template.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TemplateChip(
    label: String,
    subtitle: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Column {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TimeControlRow(label: String, value: Int, min: Int, max: Int, onValueChange: (Int) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    LaunchedEffect(isEditing) {
        if (isEditing) inputText = value.toString()
    }

    Row(
        modifier = Modifier.fillMaxWidth().height(32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).clickable(enabled = value > min) { onValueChange(value - 5) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(18.dp),
                    tint = if (value > min) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            }
            if (isEditing) {
                val focusRequester = remember { FocusRequester() }
                var hadFocus by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
                BasicTextField(
                    value = TextFieldValue(inputText, selection = TextRange(inputText.length)),
                    onValueChange = { tfv ->
                        val filtered = tfv.text.filter { c -> c.isDigit() }
                        inputText = filtered
                        filtered.toIntOrNull()?.let { parsed ->
                            val clamped = parsed.coerceIn(1, max)
                            if (clamped != value) onValueChange(clamped)
                        }
                    },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        val newValue = inputText.toIntOrNull()?.coerceIn(1, max) ?: value
                        onValueChange(newValue)
                        isEditing = false
                        focusManager.clearFocus()
                    }),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary
                    ),
                    visualTransformation = object : VisualTransformation {
                        override fun filter(text: AnnotatedString): TransformedText {
                            val transformed = AnnotatedString("${text.text} min")
                            val mapping = object : OffsetMapping {
                                override fun originalToTransformed(offset: Int) = offset
                                override fun transformedToOriginal(offset: Int) = offset.coerceAtMost(text.text.length)
                            }
                            return TransformedText(transformed, mapping)
                        }
                    },
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .focusRequester(focusRequester)
                        .onFocusChanged { state ->
                            if (state.isFocused || state.hasFocus) hadFocus = true
                            if (!state.isFocused && !state.hasFocus && hadFocus && isEditing) {
                                val newValue = inputText.toIntOrNull()?.coerceIn(1, max) ?: value
                                onValueChange(newValue)
                                isEditing = false
                            }
                        }
                )
            } else {
                Text(text = "$value min", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { isEditing = true })
            }
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).clickable(enabled = value < max) { onValueChange(value + 5) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(18.dp),
                    tint = if (value < max) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
fun TimeControlRowNullable(label: String, value: Int?, min: Int, max: Int, onValueChange: (Int?) -> Unit, step: Int = 5, valueSuffix: String = "min") {
    val currentValue = value ?: 0
    Row(
        modifier = Modifier.fillMaxWidth().height(32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (value != null && value > 0) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).clickable {
                        val newVal = if (currentValue - step <= 0) 0 else currentValue - step
                        onValueChange(if (newVal == 0) null else newVal)
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(18.dp))
                }
            }
            Text(
                text = if (value != null && value > 0) "$value $valueSuffix${if (valueSuffix.isNotEmpty() && value > 1) "s" else ""}" else "Off",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (value != null && value > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (value == null || value == 0) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).clickable { onValueChange(step) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Enable", modifier = Modifier.size(18.dp))
                }
            } else if (currentValue < max) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).clickable { onValueChange(currentValue + step) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
