package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.viewmodel.MainViewModel

@Composable
fun PermissionsScreen(viewModel: MainViewModel, onAllPermissionsGranted: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var hasNotification by remember { mutableStateOf(viewModel.hasNotificationPermission(context)) }
    var hasExactAlarm by remember { mutableStateOf(viewModel.hasExactAlarmPermission(context)) }
    var hasUsageStats by remember { mutableStateOf(viewModel.hasUsageStatsPermission(context)) }
    var hasDndAccess by remember { mutableStateOf(viewModel.checkNotificationPolicyPermission(context)) }
    var hasFullScreenIntent by remember { mutableStateOf(viewModel.hasFullScreenIntentPermission(context)) }

    val allGranted = hasNotification && hasExactAlarm && hasUsageStats && hasDndAccess && hasFullScreenIntent
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotification = viewModel.hasNotificationPermission(context)
                hasExactAlarm = viewModel.hasExactAlarmPermission(context)
                hasUsageStats = viewModel.hasUsageStatsPermission(context)
                hasDndAccess = viewModel.checkNotificationPolicyPermission(context)
                hasFullScreenIntent = viewModel.hasFullScreenIntentPermission(context)

                if (hasNotification && hasExactAlarm && hasUsageStats && hasDndAccess && hasFullScreenIntent) {
                    onAllPermissionsGranted()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotification = isGranted
        }
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "App Setup",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please grant the following permissions to ensure all features work correctly.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            PermissionItem(
                title = "Notifications",
                description = "Required for task reminders and pomodoro alerts.",
                icon = Icons.Default.Notifications,
                isGranted = hasNotification,
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )

            PermissionItem(
                title = "Exact Alarms",
                description = "Needed for precise timer and event notifications.",
                icon = Icons.Default.Alarm,
                isGranted = hasExactAlarm,
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                }
            )

            PermissionItem(
                title = "Usage Access",
                description = "Used to track actual device screen time in stats.",
                icon = Icons.Default.Analytics,
                isGranted = hasUsageStats,
                onClick = {
                    viewModel.requestUsagePermission(context)
                }
            )

            PermissionItem(
                title = "Do Not Disturb",
                description = "Allows Pomodoro timer to manage DND mode.",
                icon = Icons.Default.DoNotDisturb,
                isGranted = hasDndAccess,
                onClick = {
                    viewModel.requestNotificationPolicyPermission(context)
                }
            )

            if (Build.VERSION.SDK_INT >= 34) {
                PermissionItem(
                    title = "Full-Screen Alerts",
                    description = "Shows the pomodoro completion screen automatically.",
                    icon = Icons.Default.Alarm,
                    isGranted = hasFullScreenIntent,
                    onClick = {
                        viewModel.requestFullScreenIntentSettings(context)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (allGranted) {
                Button(
                    onClick = onAllPermissionsGranted,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("CONTINUE", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = "Some permissions are still missing. Grant them above.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isGranted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else icon,
                    contentDescription = null,
                    tint = if (isGranted) Color.White else MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (!isGranted) {
                Button(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("GRANT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
