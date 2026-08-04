with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'androidx.compose.material3.surfaceColorAtElevation(MaterialTheme.colorScheme, 8.dp)',
    'MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)'
)

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'w') as f:
    f.write(content)
