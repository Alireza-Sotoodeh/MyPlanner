with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'r') as f:
    content = f.read()

import re

old_bg = """.clip(RoundedCornerShape(12.dp))
            .background(if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.Transparent)"""

new_bg = """.clip(RoundedCornerShape(12.dp))
            .background(
                if (isDragging) androidx.compose.material3.surfaceColorAtElevation(MaterialTheme.colorScheme.surface, 8.dp)
                else if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            )"""

content = content.replace(old_bg, new_bg)

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'w') as f:
    f.write(content)
