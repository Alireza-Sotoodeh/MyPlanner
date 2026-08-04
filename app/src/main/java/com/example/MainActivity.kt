package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.core.database.AppDatabase
import com.example.core.repository.DayReviewRepository
import com.example.core.repository.DiaryRepository
import com.example.core.repository.HabitRepository
import com.example.core.repository.IdeaRepository
import com.example.core.repository.MottoRepository
import com.example.core.repository.ShopItemRepository
import com.example.core.repository.SleepLogRepository
import com.example.core.repository.TaskRepository
import com.example.core.repository.LearnRepository
import com.example.core.repository.TimerRepository
import com.example.core.repository.TodoRepository
import com.example.core.service.TimerForegroundService
import com.example.ui.components.UndoBar
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.PlannerScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MainViewModelFactory
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableLongStateOf
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.core.manager.BackupWorker

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var dayReviewTriggeredReceiver: BroadcastReceiver

    private val driveSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.onDriveSignInResult(result.data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize local offline-first Database and Repositories
        val database = AppDatabase.getDatabase(applicationContext)
        val taskRepository = TaskRepository(database.taskDao())
        val timerRepository = TimerRepository(database.timerSessionDao(), database.timerTemplateDao())
        val habitRepository = HabitRepository(database.habitDao())
        val sleepLogRepository = SleepLogRepository(database.sleepLogDao())
        val ideaRepository = IdeaRepository(database.ideaGroupDao(), database.ideaDao(), database.ideaStageDao())
        val todoRepository = TodoRepository(database.todoDao())
        val diaryRepository = DiaryRepository(database.diaryDao())
        val shopItemRepository = ShopItemRepository(database.shopItemDao())
        val mottoRepository = MottoRepository(database.mottoDao())
        val dayReviewRepository = DayReviewRepository(database.dayReviewDao())
        val learnRepository = LearnRepository(database.learnDao(), database.learnGroupDao())

        // 2. Initialize unified MainViewModel
        val viewModelFactory = MainViewModelFactory(
            taskRepository,
            timerRepository,
            habitRepository,
            sleepLogRepository,
            ideaRepository,
            todoRepository,
            diaryRepository,
            shopItemRepository,
            mottoRepository,
            dayReviewRepository,
            learnRepository,
            applicationContext
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        scheduleDailyBackup()

        if (intent.getBooleanExtra("open_day_review", false)) {
            viewModel.checkAndTriggerDayReviewPrompt()
        }
        intent.getStringExtra("pomodoro_action")?.let { action ->
            viewModel.handlePomodoroAction(this@MainActivity, action)
        }
        intent.getIntExtra("navigate_to_tab", -1).let { tab ->
            if (tab >= 0) viewModel.selectTab(tab)
        }
        intent.getIntExtra("open_timer_subtab", -1).let { sub ->
            if (sub >= 0) viewModel.setPreferredTimerTab(sub)
        }
        intent.getStringExtra("open_date")?.let { date ->
            viewModel.selectDate(date)
        }
        intent.getStringExtra("open_more_screen")?.let { screen ->
            viewModel.setPendingMoreScreen(screen)
        }

        setContent {
            MyApplicationTheme {
                val selectedTab by viewModel.currentTab.collectAsState()
                val showPrompt by viewModel.showDayReviewPrompt.collectAsState()
                val todayDate by viewModel.todayDate.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }
                var showDayReviewOverlay by remember { mutableStateOf(false) }
                val undoStack by viewModel.undoStack.collectAsState()
                var tick by remember { mutableLongStateOf(System.currentTimeMillis()) }

                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current
                var showPermissions by remember { mutableStateOf(!viewModel.hasAllRequiredPermissions(context)) }

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            showPermissions = !viewModel.hasAllRequiredPermissions(context)
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                LaunchedEffect(undoStack) {
                    while (undoStack.isNotEmpty()) {
                        delay(1000)
                        tick = System.currentTimeMillis()
                    }
                }

                val currentUndoEntry = undoStack.lastOrNull()
                val remaining = currentUndoEntry?.let {
                    ((it.expiryTime - tick) / 1000).toInt().coerceAtLeast(0)
                } ?: 0

                Box(modifier = Modifier.fillMaxSize()) {
                    if (showPermissions) {
                        PermissionsScreen(
                            viewModel = viewModel,
                            onAllPermissionsGranted = { showPermissions = false }
                        )
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            bottomBar = {
                                AestheticNavigationBar(
                                    selectedTab = selectedTab,
                                    onTabSelected = { viewModel.selectTab(it) }
                                )
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (selectedTab) {
                                    0 -> PlannerScreen(viewModel = viewModel)
                                    1 -> HabitsScreen(viewModel = viewModel)
                                    2 -> TimerScreen(viewModel = viewModel)
                                    3 -> StatsScreen(viewModel = viewModel)
                                    4 -> MoreScreen(
                                        viewModel = viewModel,
                                        onNavigateToPlanner = { viewModel.selectTab(0) }
                                    )
                                }
                                currentUndoEntry?.let { entry ->
                                    UndoBar(
                                        message = entry.message,
                                        countdownSeconds = remaining,
                                        onRestore = { viewModel.restoreFromUndo(entry.id) },
                                        onDismiss = { viewModel.dismissUndo(entry.id) },
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                }
                            }
                        }

                        if (showDayReviewOverlay) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                com.example.ui.screens.DayReviewScreen(
                                    viewModel = viewModel,
                                    initialDate = todayDate,
                                    onBack = {
                                        showDayReviewOverlay = false
                                        viewModel.dismissDayReviewPrompt()
                                    }
                                )
                            }
                        }
                    }
                }

                LaunchedEffect(showPrompt) {
                    if (showPrompt) {
                        val result = snackbarHostState.showSnackbar(
                            message = "Time to review your day!",
                            actionLabel = "Review",
                            duration = SnackbarDuration.Long
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            showDayReviewOverlay = true
                        }
                    } else {
                        showDayReviewOverlay = false
                        snackbarHostState.currentSnackbarData?.dismiss()
                    }
                }

                val pendingSignIn by viewModel.pendingDriveSignInIntent.collectAsState()
                LaunchedEffect(pendingSignIn) {
                    val intent = pendingSignIn
                    if (intent != null) {
                        driveSignInLauncher.launch(intent)
                    }
                }
            }
        }
    }

    private fun scheduleDailyBackup() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val dailyBackup = PeriodicWorkRequestBuilder<BackupWorker>(24, java.util.concurrent.TimeUnit.HOURS)
            .setConstraints(constraints)
            .addTag("daily_drive_backup")
            .setInitialDelay(1, java.util.concurrent.TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_drive_backup",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyBackup
        )
    }

    override fun onResume() {
        super.onResume()
        TimerForegroundService.isAppInForeground = true
        if (::viewModel.isInitialized) {
            viewModel.refreshSystemDate()
            viewModel.checkAndTriggerDayReviewPrompt()
        }
        val filter = IntentFilter("com.example.action.DAY_REVIEW_TRIGGERED")
        dayReviewTriggeredReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                viewModel.checkAndTriggerDayReviewPrompt()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(dayReviewTriggeredReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(dayReviewTriggeredReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        if (::dayReviewTriggeredReceiver.isInitialized) {
            try {
                unregisterReceiver(dayReviewTriggeredReceiver)
            } catch (e: Exception) {
                Log.e("MainActivity", "Unregister receiver failed", e)
            }
        }
    }

    override fun onStop() {
        TimerForegroundService.isAppInForeground = false
        super.onStop()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra("open_day_review", false)) {
            viewModel.checkAndTriggerDayReviewPrompt()
        }
        intent.getStringExtra("pomodoro_action")?.let { action ->
            viewModel.handlePomodoroAction(this@MainActivity, action)
        }
        intent.getIntExtra("navigate_to_tab", -1).let { tab ->
            if (tab >= 0) viewModel.selectTab(tab)
        }
        intent.getIntExtra("open_timer_subtab", -1).let { sub ->
            if (sub >= 0) viewModel.setPreferredTimerTab(sub)
        }
        intent.getStringExtra("open_date")?.let { date ->
            viewModel.selectDate(date)
        }
        intent.getStringExtra("open_more_screen")?.let { screen ->
            viewModel.setPendingMoreScreen(screen)
        }
    }
}

@Composable
fun AestheticNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.navigationBarsPadding()) {
        // Simple single-pixel border at the top of bottom bar
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navItems = listOf(
                NavigationItem("Planner", Icons.Default.Task, 0),
                NavigationItem("Habits", Icons.Default.Favorite, 1),
                NavigationItem("Timer", Icons.Default.Timer, 2),
                NavigationItem("Stats", Icons.Default.Leaderboard, 3),
                NavigationItem("More", Icons.Default.MoreHoriz, 4)
            )

            navItems.forEach { item ->
                val isSelected = selectedTab == item.index

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable { onTabSelected(item.index) }
                        .padding(horizontal = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val index: Int
)
