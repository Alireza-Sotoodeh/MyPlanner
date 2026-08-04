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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.core.repository.TodoRepository
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.PlannerScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.PomodoroScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MainViewModelFactory
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.filled.Timer

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize local offline-first Database and Repositories
        val database = AppDatabase.getDatabase(applicationContext)
        val taskRepository = TaskRepository(database.taskDao(), database.pomodoroSessionDao())
        val habitRepository = HabitRepository(database.habitDao())
        val sleepLogRepository = SleepLogRepository(database.sleepLogDao())
        val ideaRepository = IdeaRepository(database.ideaGroupDao(), database.ideaDao(), database.ideaStageDao())
        val todoRepository = TodoRepository(database.todoDao())
        val diaryRepository = DiaryRepository(database.diaryDao())
        val shopItemRepository = ShopItemRepository(database.shopItemDao())
        val mottoRepository = MottoRepository(database.mottoDao())
        val dayReviewRepository = DayReviewRepository(database.dayReviewDao())

        // 2. Initialize unified MainViewModel
        val viewModelFactory = MainViewModelFactory(
            taskRepository,
            habitRepository,
            sleepLogRepository,
            ideaRepository,
            todoRepository,
            diaryRepository,
            shopItemRepository,
            mottoRepository,
            dayReviewRepository,
            applicationContext
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val selectedTab by viewModel.currentTab.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
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
                            2 -> PomodoroScreen(viewModel = viewModel)
                            3 -> StatsScreen(viewModel = viewModel)
                            4 -> MoreScreen(
                                viewModel = viewModel,
                                onNavigateToPlanner = { viewModel.selectTab(0) }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.refreshSystemDate()
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
                NavigationItem("Pomodoro", Icons.Default.Timer, 2),
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
