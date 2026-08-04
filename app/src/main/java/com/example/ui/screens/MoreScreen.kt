package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MottoCard
import com.example.ui.viewmodel.MainViewModel

sealed class MoreSubScreen(val label: String) {
    data object Ideas : MoreSubScreen("Ideas")
    data object Todo : MoreSubScreen("To-Do")
    data object Diary : MoreSubScreen("Diary")
    data object ShopList : MoreSubScreen("Shop List")
    data object Mottos : MoreSubScreen("Mottos")
    data object DayReview : MoreSubScreen("Day Review")
    data object None : MoreSubScreen("")
}

data class MoreTile(
    val label: String,
    val icon: ImageVector,
    val screen: MoreSubScreen
)

private val tiles = listOf(
    MoreTile("Ideas", Icons.Default.Lightbulb, MoreSubScreen.Ideas),
    MoreTile("To-Do", Icons.Default.Checklist, MoreSubScreen.Todo),
    MoreTile("Diary", Icons.Default.MenuBook, MoreSubScreen.Diary),
    MoreTile("Shop List", Icons.Default.ShoppingCart, MoreSubScreen.ShopList),
    MoreTile("Mottos", Icons.Default.FormatQuote, MoreSubScreen.Mottos),
    MoreTile("Day Review", Icons.Default.RateReview, MoreSubScreen.DayReview)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    viewModel: MainViewModel,
    onNavigateToPlanner: () -> Unit = {}
) {
    var currentScreen by remember { mutableStateOf<MoreSubScreen>(MoreSubScreen.None) }

    if (currentScreen !is MoreSubScreen.None) {
        when (currentScreen) {
                is MoreSubScreen.Ideas -> IdeasScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = MoreSubScreen.None }
                )
                is MoreSubScreen.Todo -> TodoScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = MoreSubScreen.None }
                )
                is MoreSubScreen.Diary -> DiaryScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = MoreSubScreen.None }
                )
                is MoreSubScreen.ShopList -> ShopListScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = MoreSubScreen.None }
                )
                is MoreSubScreen.Mottos -> MottoManagementScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = MoreSubScreen.None }
                )
                is MoreSubScreen.DayReview -> DayReviewScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = MoreSubScreen.None }
                )
                else -> {}
            }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MORE FEATURES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "More",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            val allMottos by viewModel.allMottos.collectAsState()
            val todayMotto by viewModel.todayMotto.collectAsState()

            if (allMottos.isNotEmpty()) {
                MottoCard(motto = todayMotto, visible = true)
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { currentScreen = MoreSubScreen.Mottos },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "Add your favorite quotes in Mottos \u2192",
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tiles) { tile ->
                    MoreTileItem(
                        tile = tile,
                        onClick = { currentScreen = tile.screen }
                    )
                }
            }
        }
    }
}

@Composable
private fun MoreTileItem(tile: MoreTile, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = tile.icon,
                contentDescription = tile.label,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tile.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


