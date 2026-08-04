import re

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'r') as f:
    content = f.read()

# 1. Add `expandAllItems` state
old_vars = """    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var subtasksToEdit by remember { mutableStateOf<List<TaskEntity>>(emptyList()) }
    
    var showLabels by remember { mutableStateOf(true) }"""

new_vars = """    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var subtasksToEdit by remember { mutableStateOf<List<TaskEntity>>(emptyList()) }
    var expandAllItems by remember { mutableStateOf(true) }
    
    var showLabels by remember { mutableStateOf(true) }"""

content = content.replace(old_vars, new_vars)

# 2. Add button in Daily Intentions header
old_header = """                    val pendingCount = tasks.count { it.status != "COMPLETED" }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outline)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "$pendingCount PENDING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }"""

new_header = """                    val pendingCount = tasks.count { it.status != "COMPLETED" }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { expandAllItems = !expandAllItems },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (expandAllItems) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (expandAllItems) "Collapse All" else "Expand All",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.outline)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "$pendingCount PENDING",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }"""

content = content.replace(old_header, new_header)

# 3. Add `isExpanded = expandAllItems` to both `activeTasks` and `completedTasks` mapping
old_bullet_1 = """                                BulletTaskItem(
                                    modifier = Modifier.onGloballyPositioned { itemHeights[task.id] = it.size.height },
                                    task = task,
                                    subtasks = taskSubtasks,
                                    onCheckToggle = { viewModel.toggleTaskCompletion(task, taskSubtasks) },"""

new_bullet_1 = """                                BulletTaskItem(
                                    modifier = Modifier.onGloballyPositioned { itemHeights[task.id] = it.size.height },
                                    task = task,
                                    subtasks = taskSubtasks,
                                    isExpanded = expandAllItems,
                                    onCheckToggle = { viewModel.toggleTaskCompletion(task, taskSubtasks) },"""

content = content.replace(old_bullet_1, new_bullet_1)

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'w') as f:
    f.write(content)
