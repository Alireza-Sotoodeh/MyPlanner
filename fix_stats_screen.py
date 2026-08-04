import re

with open('app/src/main/java/com/example/ui/screens/StatsScreen.kt', 'r') as f:
    content = f.read()

old_code = """    val hasPermission = viewModel.hasUsageStatsPermission(context)"""

content = content.replace(old_code, "")

old_code2_pattern = re.compile(r'                    if \(!hasPermission\) \{.*?                    \}\n                \}\n            \}\n        \}\n\n        // 2\. Habit Completion Progress', re.DOTALL)

new_code2 = """                }
            }
        }

        // 2. Habit Completion Progress"""

content = re.sub(old_code2_pattern, new_code2, content)

with open('app/src/main/java/com/example/ui/screens/StatsScreen.kt', 'w') as f:
    f.write(content)
