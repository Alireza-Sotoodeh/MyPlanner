with open('app/src/main/java/com/example/ui/components/TaskManagerDialog.kt', 'r') as f:
    content = f.read()

old_layout = """        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, maxOf(0, (placeable.height * heightFactor).toInt())) {
                placeable.place(0, 0)
            }
        }"""

new_layout = """        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            val h = (placeable.height * heightFactor).toInt().coerceIn(constraints.minHeight, constraints.maxHeight)
            val w = placeable.width.coerceIn(constraints.minWidth, constraints.maxWidth)
            layout(w, h) {
                if (alpha > 0f) {
                    placeable.place(0, 0)
                }
            }
        }"""

content = content.replace(old_layout, new_layout)

with open('app/src/main/java/com/example/ui/components/TaskManagerDialog.kt', 'w') as f:
    f.write(content)
