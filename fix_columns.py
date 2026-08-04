import re

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'r') as f:
    lines = f.readlines()

for i in [114, 301, 1110]:  # 0-indexed is 114, 301, 1110
    if "modifier = modifier.then(Modifier" in lines[i]:
        lines[i] = lines[i].replace("modifier = modifier.then(Modifier", "modifier = Modifier")

with open('app/src/main/java/com/example/ui/screens/PlannerScreen.kt', 'w') as f:
    f.writelines(lines)
