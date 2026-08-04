with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'r') as f:
    content = f.read()

# Add imports if missing
imports = [
    "import androidx.compose.ui.zIndex",
    "import androidx.compose.foundation.layout.offset",
    "import androidx.compose.ui.unit.IntOffset",
    "import androidx.compose.ui.input.pointer.pointerInput",
    "import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress",
    "import androidx.compose.ui.input.pointer.positionChange"
]

for imp in imports:
    if imp not in content:
        content = content.replace("import androidx.compose.foundation.lazy.LazyColumn", f"import androidx.compose.foundation.lazy.LazyColumn\\n{imp}")

# Fix BulletTaskItem Modifier
bad_modifier = """    Row(
        modifier = Modifier
            .fillMaxWidth()
            .androidx.compose.ui.zIndex(if (isDragging) 1f else 0f)
            .androidx.compose.foundation.layout.offset { androidx.compose.ui.unit.IntOffset(Math.round(offsetX), Math.round(offsetY)) }
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .then(
                if (onReorder != null && !isCompleted) {
                    Modifier.androidx.compose.ui.input.pointer.pointerInput(task.id) {
                        androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress(
                            onDragStart = { isDragging = true },
                            onDragEnd = {
                                isDragging = false
                                val itemHeight = 60.dp.toPx()
                                val deltaIndex = Math.round(offsetY / itemHeight)
                                val isSubtask = offsetX > 50.dp.toPx()
                                offsetX = 0f
                                offsetY = 0f
                                if (deltaIndex != 0L || isSubtask) {
                                    onReorder(deltaIndex.toInt(), isSubtask)
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                offsetX = 0f
                                offsetY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            }
                        )
                    }
                } else Modifier
            ),"""

# But wait, my previous Python script had some Long/Int mixup and consume() issues.
# In Compose 1.7+, pointerInputChange.consume() is correct. If it fails, maybe it's `change.consume()` in 1.7+.
# Oh, it's `change.consume()` but `change` is `PointerInputChange`.
# Wait! Math.round(Float) returns Int. offsetY is Float. Math.round(Float) returns Int in Java, but in Kotlin kotlin.math.roundToInt() is better.
