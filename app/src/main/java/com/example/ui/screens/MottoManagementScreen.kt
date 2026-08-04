package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.MottoEntity
import com.example.ui.components.MottoListItem
import com.example.ui.viewmodel.MainViewModel

private const val MAX_QUOTE_LENGTH = 300
private const val MAX_AUTHOR_LENGTH = 80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MottoManagementScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val mottos by viewModel.allMottos.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingMotto by remember { mutableStateOf<MottoEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<MottoEntity?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Motto")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Icon(
                            Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "INSPIRATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Mottos",
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
                    IconButton(onClick = { viewModel.selectTab(0); viewModel.selectDate(viewModel.todayDate.value) }) {
                        Icon(Icons.Default.Home, contentDescription = "Home", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    }
                }
            }

            if (mottos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FormatQuote,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No mottos yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Add inspirational quotes to brighten your day",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            modifier = Modifier.padding(horizontal = 48.dp),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        OutlinedButton(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Add your first motto")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mottos, key = { it.id }) { motto ->
                        MottoListItem(
                            motto = motto,
                            onEdit = { editingMotto = it },
                            onDelete = { showDeleteConfirm = it }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog || editingMotto != null) {
        val existing = editingMotto
        AddEditMottoDialog(
            initialText = existing?.text ?: "",
            initialAuthor = existing?.author ?: "",
            onDismiss = { showAddDialog = false; editingMotto = null },
            onConfirm = { text, author ->
                if (existing != null) {
                    viewModel.updateMotto(existing.copy(text = text.trim(), author = author.trim()))
                } else {
                    viewModel.addMotto(text.trim(), author.trim())
                }
                showAddDialog = false; editingMotto = null
            }
        )
    }

    showDeleteConfirm?.let { motto ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Motto", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("\"${motto.text.take(100)}${if (motto.text.length > 100) "..." else ""}\"", fontSize = 14.sp, fontStyle = FontStyle.Italic)
                    Spacer(Modifier.height(8.dp))
                    Text("Remove this motto permanently?", fontSize = 14.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteMotto(motto); showDeleteConfirm = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancel") }
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(viewModel = viewModel, onDismiss = { showSettingsDialog = false })
    }
}

@Composable
private fun AddEditMottoDialog(
    initialText: String,
    initialAuthor: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    var author by remember { mutableStateOf(initialAuthor) }
    var isSaving by remember { mutableStateOf(false) }

    val isEdit = initialText.isNotEmpty()
    val textTrimmed = text.trim()
    val isValid = textTrimmed.isNotEmpty() && textTrimmed.length <= MAX_QUOTE_LENGTH && !isSaving

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp)
                ) {
                    Icon(
                        Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    if (isEdit) "Edit Motto" else "New Motto",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.take(MAX_QUOTE_LENGTH) },
                    label = { Text("Quote") },
                    placeholder = { Text("Type your favorite quote...", fontStyle = FontStyle.Italic) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    isError = textTrimmed.length > MAX_QUOTE_LENGTH,
                    supportingText = {
                        Text(
                            "${text.length}/$MAX_QUOTE_LENGTH",
                            fontSize = 10.sp,
                            color = if (text.length > MAX_QUOTE_LENGTH - 20)
                                MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    }
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it.take(MAX_AUTHOR_LENGTH) },
                    label = { Text("Author") },
                    placeholder = { Text("Optional") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                )

                if (textTrimmed.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Preview",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.FormatQuote,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            textTrimmed,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                        if (author.trim().isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "\u2014 ${author.trim()}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSaving = true
                    onConfirm(textTrimmed, author.trim())
                },
                enabled = isValid,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(if (isEdit) "Save" else "Add", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancel") }
        }
    )
}
