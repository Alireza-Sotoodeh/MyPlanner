with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)',
    'MaterialTheme.colorScheme.surfaceVariant'
)

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'w') as f:
    f.write(content)
