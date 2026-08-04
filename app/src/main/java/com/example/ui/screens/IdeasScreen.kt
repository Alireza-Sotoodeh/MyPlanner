package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.IdeaEntity
import com.example.core.database.entity.IdeaGroupEntity
import com.example.core.database.entity.IdeaStageEntity
import com.example.ui.viewmodel.MainViewModel

private val presetColors = listOf(
    0xFF6750A4, 0xFFB3261E, 0xFF00E676, 0xFF2196F3,
    0xFFFF7043, 0xFFFFEB3B, 0xFFE91E63, 0xFF00BCD4
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun IdeasScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val groups by viewModel.ideaGroups.collectAsState()
    val ideas by viewModel.allIdeas.collectAsState()

    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showCreateIdeaDialog by remember { mutableStateOf(false) }
    var editingIdea by remember { mutableStateOf<IdeaEntity?>(null) }
    var showDeleteIdeaConfirm by remember { mutableStateOf<IdeaEntity?>(null) }
    var showDeleteGroupConfirm by remember { mutableStateOf<IdeaGroupEntity?>(null) }
    var editingGroup by remember { mutableStateOf<IdeaGroupEntity?>(null) }
    var ideaForPlanner by remember { mutableStateOf<IdeaEntity?>(null) }

    val filteredIdeas = if (selectedGroupId == null) ideas
    else ideas.filter { it.groupId == selectedGroupId }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "IDEAS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Ideas",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        GroupChipRow(
            groups = groups,
            selectedGroupId = selectedGroupId,
            onSelectGroup = { selectedGroupId = it },
            onEditGroup = { editingGroup = it },
            onDeleteGroup = { showDeleteGroupConfirm = it }
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (filteredIdeas.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No ideas yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Text(
                            "Tap + to create your first idea",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredIdeas, key = { it.id }) { idea ->
                        IdeaCard(
                            idea = idea,
                            groups = groups,
                            viewModel = viewModel,
                            onEdit = { editingIdea = it },
                            onDelete = { showDeleteIdeaConfirm = it },
                            onAddToPlanner = { ideaForPlanner = it }
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { showCreateIdeaDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.background,
                shape = CircleShape,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Idea")
            }
        }
    }

    if (showCreateGroupDialog) {
        CreateGroupDialog(
            initialName = null,
            initialColor = presetColors[0],
            onDismiss = { showCreateGroupDialog = false },
            onConfirm = { name, color -> viewModel.addGroup(name, color); showCreateGroupDialog = false }
        )
    }
    editingGroup?.let { group ->
        CreateGroupDialog(
            initialName = group.name,
            initialColor = group.color,
            onDismiss = { editingGroup = null },
            onConfirm = { name, color ->
                viewModel.updateGroup(group.copy(name = name, color = color))
                editingGroup = null
            }
        )
    }
    if (showCreateIdeaDialog || editingIdea != null) {
        val existing = editingIdea
        val existingStages by viewModel.stagesForIdea(existing?.id ?: -1L).collectAsState(initial = emptyList())
        CreateIdeaDialog(
            viewModel = viewModel,
            groups = groups,
            initialTitle = existing?.title ?: "",
            initialDescription = existing?.description ?: "",
            initialGroupId = existing?.groupId,
            initialStages = existingStages,
            onDismiss = { showCreateIdeaDialog = false; editingIdea = null },
            onConfirm = { groupId, title, description, stages ->
                if (existing != null) {
                    viewModel.updateIdea(existing.copy(groupId = groupId, title = title, description = description), stages)
                } else {
                    viewModel.addIdea(groupId, title, description, stages)
                }
                showCreateIdeaDialog = false; editingIdea = null
            }
        )
    }
    showDeleteIdeaConfirm?.let { idea ->
        DeleteConfirmDialog(
            title = "Delete Idea",
            message = "Delete \"${idea.title}\" and all its stages?",
            onDismiss = { showDeleteIdeaConfirm = null },
            onConfirm = { viewModel.deleteIdea(idea); showDeleteIdeaConfirm = null }
        )
    }
    showDeleteGroupConfirm?.let { group ->
        DeleteConfirmDialog(
            title = "Delete Group",
            message = "Delete \"${group.name}\" and all ideas inside it?",
            onDismiss = { showDeleteGroupConfirm = null },
            onConfirm = { viewModel.deleteGroup(group); showDeleteGroupConfirm = null }
        )
    }
    ideaForPlanner?.let { idea ->
        AddToPlannerDialog(
            idea = idea,
            viewModel = viewModel,
            onDismiss = { ideaForPlanner = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupChipRow(
    groups: List<IdeaGroupEntity>,
    selectedGroupId: Long?,
    onSelectGroup: (Long?) -> Unit,
    onEditGroup: (IdeaGroupEntity) -> Unit,
    onDeleteGroup: (IdeaGroupEntity) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedGroupId == null,
                onClick = { onSelectGroup(null) },
                label = { Text("All") }
            )
        }
        items(groups, key = { it.id }) { group ->
            FilterChip(
                selected = selectedGroupId == group.id,
                onClick = { onSelectGroup(group.id) },
                label = { Text(group.name) },
                trailingIcon = {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(group.color))
                    )
                },
                modifier = Modifier.combinedClickable(
                    onClick = { onSelectGroup(group.id) },
                    onLongClick = { onEditGroup(group) }
                )
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IdeaCard(
    idea: IdeaEntity,
    groups: List<IdeaGroupEntity>,
    viewModel: MainViewModel,
    onEdit: (IdeaEntity) -> Unit,
    onDelete: (IdeaEntity) -> Unit,
    onAddToPlanner: (IdeaEntity) -> Unit
) {
    val stages by viewModel.stagesForIdea(idea.id).collectAsState(initial = emptyList())
    var showAddStage by remember { mutableStateOf(false) }
    var newStageTitle by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(true) }
    var showIdeaMenu by remember { mutableStateOf(false) }
    var addStageIdeaId by remember { mutableStateOf<Long?>(null) }

    val groupColor = groups.find { it.id == idea.groupId }?.let { Color(it.color) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = groupColor ?: MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        idea.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (idea.description.isNotBlank()) {
                        Text(
                            idea.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                Box {
                    IconButton(onClick = { showIdeaMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(expanded = showIdeaMenu, onDismissRequest = { showIdeaMenu = false }) {
                        DropdownMenuItem(text = { Text("Edit") }, onClick = { showIdeaMenu = false; onEdit(idea) })
                        DropdownMenuItem(text = { Text("Add to Planner") }, onClick = { showIdeaMenu = false; onAddToPlanner(idea) })
                        DropdownMenuItem(text = { Text("Delete") }, onClick = { showIdeaMenu = false; onDelete(idea) })
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    if (idea.description.isBlank()) {
                        Spacer(Modifier.height(4.dp))
                    } else {
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(Modifier.height(8.dp))
                    }

                    stages.forEach { stage ->
                        StageRow(
                            stage = stage,
                            stages = stages,
                            viewModel = viewModel,
                            onDelete = { viewModel.deleteStage(it) }
                        )
                        Spacer(Modifier.height(4.dp))
                    }

                    if (showAddStage && addStageIdeaId == idea.id) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = newStageTitle,
                                onValueChange = { newStageTitle = it },
                                placeholder = { Text("Stage title...", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f).height(40.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )
                            )
                            Spacer(Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    if (newStageTitle.isNotBlank()) {
                                        viewModel.addStage(idea.id, newStageTitle.trim())
                                        newStageTitle = ""
                                        showAddStage = false
                                        addStageIdeaId = null
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Add", modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { showAddStage = false; newStageTitle = ""; addStageIdeaId = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(16.dp))
                            }
                        }
                    } else {
                        TextButton(
                            onClick = { showAddStage = true; addStageIdeaId = idea.id },
                            modifier = Modifier.height(24.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Add Stage",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(
                            onClick = { onAddToPlanner(idea) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Planner", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StageRow(
    stage: IdeaStageEntity,
    stages: List<IdeaStageEntity>,
    viewModel: MainViewModel,
    onDelete: (IdeaStageEntity) -> Unit
) {
    val index = stages.indexOf(stage)
    val previousCompleted = index == 0 || stages.take(index).all { it.isCompleted }
    val canToggle = previousCompleted

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(
            checked = stage.isCompleted,
            onCheckedChange = { checked ->
                if (canToggle) {
                    if (checked) {
                        viewModel.updateStage(stage.copy(isCompleted = true))
                    } else {
                        viewModel.updateStage(stage.copy(isCompleted = false))
                        stages.drop(index + 1).forEach {
                            viewModel.updateStage(it.copy(isCompleted = false))
                        }
                    }
                }
            },
            enabled = canToggle,
            modifier = Modifier.size(24.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        )
        Spacer(Modifier.width(6.dp))
        Text(
            stage.title,
            fontSize = 13.sp,
            color = if (stage.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.onSurface,
            textDecoration = if (stage.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f)
        )
        if (canToggle) {
            IconButton(onClick = { onDelete(stage) }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Delete stage", modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
private fun CreateGroupDialog(
    initialName: String?,
    initialColor: Long,
    onDismiss: () -> Unit,
    onConfirm: (String, Long) -> Unit
) {
    var name by remember { mutableStateOf(initialName ?: "") }
    var selectedColor by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (initialName != null) "Edit Group" else "New Group", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Color", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presetColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .border(
                                    if (selectedColor == color) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                    else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                    CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), selectedColor) },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CreateIdeaDialog(
    viewModel: MainViewModel,
    groups: List<IdeaGroupEntity>,
    initialTitle: String,
    initialDescription: String,
    initialGroupId: Long?,
    initialStages: List<IdeaStageEntity> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (Long?, String, String, List<IdeaStageEntity>) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var selectedGroupId by remember { mutableStateOf(initialGroupId) }
    var stages by remember { mutableStateOf(initialStages) }
    var newStageTitle by remember { mutableStateOf("") }
    var editingStageIndex by remember { mutableStateOf(-1) }
    var editingStageText by remember { mutableStateOf("") }
    var showNewGroupDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (initialTitle.isNotEmpty()) "Edit Idea" else "New Idea", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 4
                )

                Text("Group:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp).height(32.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.clickable { showNewGroupDialog = true }.fillMaxHeight()
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                                Text("+ New", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    item {
                        val isNone = selectedGroupId == null
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isNone) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isNone) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.clickable { selectedGroupId = null }.fillMaxHeight()
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                                Text(
                                    "None",
                                    fontSize = 14.sp,
                                    color = if (isNone) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    items(groups) { group ->
                        val isSelected = selectedGroupId == group.id
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(group.color).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxHeight()
                                .combinedClickable(
                                    onClick = { selectedGroupId = group.id },
                                    onLongClick = {
                                        viewModel.deleteGroup(group)
                                    }
                                )
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                                Text(
                                    group.name,
                                    fontSize = 14.sp,
                                    color = if (isSelected) Color(group.color) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Text("STAGES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)

                stages.forEachIndexed { index, stage ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (editingStageIndex == index) {
                            OutlinedTextField(
                                value = editingStageText,
                                onValueChange = { editingStageText = it },
                                singleLine = true,
                                modifier = Modifier.weight(1f).height(48.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                            )
                            IconButton(onClick = {
                                if (editingStageText.isNotBlank()) {
                                    stages = stages.toMutableList().also { it[index] = stage.copy(title = editingStageText.trim()) }
                                }
                                editingStageIndex = -1
                                editingStageText = ""
                            }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(16.dp))
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f).clickable {
                                    editingStageIndex = index
                                    editingStageText = stage.title
                                }
                            ) {
                                Text(
                                    stage.title,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = {
                                stages = stages.toMutableList().also { it.removeAt(index) }
                                if (editingStageIndex == index) { editingStageIndex = -1; editingStageText = "" }
                            }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newStageTitle,
                        onValueChange = { newStageTitle = it },
                        placeholder = { Text("Add stage", fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).height(48.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                    )
                    Spacer(Modifier.width(4.dp))
                    FilledTonalButton(
                        onClick = {
                            if (newStageTitle.isNotBlank()) {
                                stages = stages + IdeaStageEntity(ideaId = 0L, title = newStageTitle.trim())
                                newStageTitle = ""
                            }
                        },
                        modifier = Modifier.height(40.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank()) onConfirm(selectedGroupId, title.trim(), description.trim(), stages) },
                enabled = title.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showNewGroupDialog) {
        var newGroupName by remember { mutableStateOf("") }
        var selectedColor by remember { mutableStateOf(presetColors[0]) }
        AlertDialog(
            onDismissRequest = { showNewGroupDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("New Group") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        label = { Text("Group Name") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presetColors) { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(color))
                                    .border(
                                        2.dp,
                                        if (selectedColor == color) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { selectedColor = color }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newGroupName.isNotBlank()) {
                        viewModel.addGroup(newGroupName.trim(), selectedColor)
                        showNewGroupDialog = false
                    }
                }) { Text("ADD") }
            },
            dismissButton = {
                TextButton(onClick = { showNewGroupDialog = false }) { Text("CANCEL") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToPlannerDialog(
    idea: IdeaEntity,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val currentDate by viewModel.selectedDate.collectAsState()
    val stages by viewModel.stagesForIdea(idea.id).collectAsState(initial = emptyList())

    var date by remember { mutableStateOf(currentDate) }
    var selectedType by remember { mutableStateOf("TASK") }
    var selectedMode by remember { mutableStateOf("entire") }
    var selectedStageId by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStagePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(date)?.time
                    ?: System.currentTimeMillis()
            } catch (_: Exception) { System.currentTimeMillis() }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
                        date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.time)
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

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Add to Planner", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Date:", fontSize = 14.sp, modifier = Modifier.width(60.dp))
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        modifier = Modifier.weight(1f).height(48.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Pick date", modifier = Modifier.size(18.dp))
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )
                }

                Text("Type:", fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("TASK", "EVENT", "NOTE").forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type, fontSize = 12.sp) }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedMode == "entire",
                        onClick = { selectedMode = "entire" }
                    )
                    Text("Entire idea (with all stages)", fontSize = 13.sp, modifier = Modifier.clickable { selectedMode = "entire" })
                }
                val hasNamedStages = stages.any { it.title.isNotBlank() }
                if (hasNamedStages) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedMode == "single",
                            onClick = { selectedMode = "single" }
                        )
                        Text("Pick a stage", fontSize = 13.sp, modifier = Modifier.clickable { selectedMode = "single" })
                    }
                    if (selectedMode == "single") {
                        Spacer(Modifier.height(4.dp))
                        stages.filter { it.title.isNotBlank() }.forEach { stage ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable { selectedStageId = stage.id }
                            ) {
                                RadioButton(
                                    selected = selectedStageId == stage.id,
                                    onClick = { selectedStageId = stage.id }
                                )
                                Text(stage.title, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when (selectedMode) {
                    "entire" -> viewModel.addIdeaToPlanner(idea, date, selectedType)
                    "single" -> {
                        val stage = stages.find { it.id == selectedStageId }
                        if (stage != null) viewModel.addStageToPlanner(stage, date, selectedType)
                    }
                }
                onDismiss()
            }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun DeleteConfirmDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message, fontSize = 14.sp) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Delete", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
