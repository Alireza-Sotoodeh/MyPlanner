import re

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'r') as f:
    content = f.read()

old_bad = """                } else Modifier
            )
    ) {"""

new_good = """                } else Modifier
            )
        )
    ) {"""

content = content.replace(old_bad, new_good)

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'w') as f:
    f.write(content)
