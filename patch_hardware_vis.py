with open('app/src/main/java/com/example/ui/components/TaskManagerDialog.kt', 'r') as f:
    content = f.read()

old_layout = "layout(placeable.width, (placeable.height * heightFactor).toInt()) {"
new_layout = "layout(placeable.width, maxOf(0, (placeable.height * heightFactor).toInt())) {"

content = content.replace(old_layout, new_layout)

with open('app/src/main/java/com/example/ui/components/TaskManagerDialog.kt', 'w') as f:
    f.write(content)
