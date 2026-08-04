import re

with open('app/src/main/java/com/example/ui/components/TaskManagerDialog.kt', 'r') as f:
    content = f.read()

old_code_1 = """    var hasNotificationPermission by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        )
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasNotificationPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                } else {
                    androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }"""

content = content.replace(old_code_1, "")

old_code_2_pattern = re.compile(r'                if \(!hasNotificationPermission\) \{.*?                    \}\n                \}', re.DOTALL)

content = re.sub(old_code_2_pattern, "", content)

with open('app/src/main/java/com/example/ui/components/TaskManagerDialog.kt', 'w') as f:
    f.write(content)
