import re

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'r') as f:
    content = f.read()

old_decl = """@Composable
fun BulletTaskItem("""

new_decl = """@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BulletTaskItem("""

content = content.replace(old_decl, new_decl)

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'w') as f:
    f.write(content)
