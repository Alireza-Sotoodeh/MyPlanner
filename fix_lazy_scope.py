import re

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'r') as f:
    content = f.read()

bad_heights = """                        val itemHeights = remember { androidx.compose.runtime.mutableStateMapOf<Long, Int>() }
                        items(activeTasks, key = { it.id }) { task ->"""

good_heights = """                        items(activeTasks, key = { it.id }) { task ->"""

content = content.replace(bad_heights, good_heights)

bad_lazy = """                    var showCompleted by remember { mutableStateOf(false) }
                    LazyColumn("""

good_lazy = """                    var showCompleted by remember { mutableStateOf(false) }
                    val itemHeights = remember { androidx.compose.runtime.mutableStateMapOf<Long, Int>() }
                    LazyColumn("""

content = content.replace(bad_lazy, good_lazy)

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'w') as f:
    f.write(content)
