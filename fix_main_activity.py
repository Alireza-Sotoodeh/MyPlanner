import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

old_content = """        setContent {
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
                            4 -> CoachScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }"""

new_content = """        setContent {
            MyApplicationTheme {
                var hasAllPermissions by androidx.compose.runtime.remember { 
                    androidx.compose.runtime.mutableStateOf(viewModel.hasAllRequiredPermissions(applicationContext)) 
                }
                
                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                        if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                            hasAllPermissions = viewModel.hasAllRequiredPermissions(applicationContext)
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                if (!hasAllPermissions) {
                    com.example.ui.screens.PermissionsScreen(
                        viewModel = viewModel,
                        onAllPermissionsGranted = { hasAllPermissions = true }
                    )
                } else {
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
                                4 -> CoachScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }"""

content = content.replace(old_content, new_content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
