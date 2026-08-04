import re

with open('app/src/main/java/com/example/ui/components/TaskManagerDialog.kt', 'r') as f:
    content = f.read()

old_save = """        confirmButton = {
            Button(onClick = {
                if (title.isNotEmpty()) {
                    if (taskToEdit != null) {"""

new_save = """        confirmButton = {
            Button(onClick = {
                if (title.isNotEmpty()) {
                    if (newSubtask.isNotBlank()) {
                        subtasks.add(newSubtask.trim() to newSubtaskImportance)
                        newSubtask = ""
                        newSubtaskImportance = "NORMAL"
                    }
                    if (taskToEdit != null) {"""

content = content.replace(old_save, new_save)

with open('app/src/main/java/com/example/ui/components/TaskManagerDialog.kt', 'w') as f:
    f.write(content)
