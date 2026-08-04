import re

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'r') as f:
    content = f.read()

# Let's inspect BulletTaskItem
