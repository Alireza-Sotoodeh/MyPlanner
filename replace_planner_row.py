import re

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'r') as f:
    content = f.read()

# Make sure imports are there
imports = [
    "import androidx.compose.ui.zIndex",
    "import androidx.compose.foundation.layout.offset",
    "import androidx.compose.ui.unit.IntOffset",
    "import androidx.compose.ui.input.pointer.pointerInput",
    "import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress",
    "import kotlin.math.roundToInt"
]

for imp in imports:
    if imp not in content:
        content = content.replace("import androidx.compose.foundation.lazy.LazyColumn", f"import androidx.compose.foundation.lazy.LazyColumn\n{imp}")

pattern = r"Row\(\s*modifier = Modifier\s*\.fillMaxWidth\(\)\s*\.androidx\.compose\.ui\.zIndex\(.*?verticalAlignment = Alignment\.CenterVertically\s*\) \{"

new_row = """Row(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 1f else 0f)
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isDragging) androidx.compose.material3.surfaceColorAtElevation(MaterialTheme.colorScheme, 8.dp)
                else if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else Color.Transparent
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .then(
                if (onReorder != null && !isCompleted) {
                    Modifier.pointerInput(task.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { _ -> isDragging = true },
                            onDragEnd = {
                                isDragging = false
                                val itemHeight = 60.dp.toPx()
                                val deltaIndex = (offsetY / itemHeight).roundToInt()
                                val isSubtask = offsetX > 50.dp.toPx()
                                offsetX = 0f
                                offsetY = 0f
                                if (deltaIndex != 0 || isSubtask) {
                                    onReorder(deltaIndex, isSubtask)
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
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {"""

content = re.sub(pattern, new_row, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'w') as f:
    f.write(content)
