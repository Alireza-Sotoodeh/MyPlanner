import re

with open('app/src/main/java/com/example/ui/viewmodel/MainViewModel.kt', 'r') as f:
    content = f.read()

pattern = r"    fun reorderTask\(.*?database\.taskDao\(\)\.updateTaskPriorities\(updatedTasks\)\n            \}\n        \}\n    \}"

content = re.sub(pattern, "", content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/viewmodel/MainViewModel.kt', 'w') as f:
    f.write(content)
