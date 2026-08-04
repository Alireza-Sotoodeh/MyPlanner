with open('app/src/main/java/com/example/ui/components/TaskManagerDialog.kt', 'r') as f:
    content = f.read()

import re
content = re.sub(
    r"transitionSpec = \{ androidx.compose.animation.core.spring\(stiffness = 400f, dampingRatio = 0.8f\) \}",
    r"transitionSpec = { androidx.compose.animation.core.tween(durationMillis = 250, easing = androidx.compose.animation.core.FastOutSlowInEasing) }",
    content
)

with open('app/src/main/java/com/example/ui/components/TaskManagerDialog.kt', 'w') as f:
    f.write(content)
