package com.naliendev.achieveit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naliendev.achieveit.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(onGameClick: () -> Unit) {
    val tabs = listOf("All Games", "Steam", "PlayStation", "Retro", "Xbox")
    var selectedTabIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PurplePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Gamepad, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("AchieveIt", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Row {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = TextPrimary)
            }
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = BackgroundDark,
            contentColor = TextPrimary,
            edgePadding = 24.dp,
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = PurplePrimary
                    )
                }
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            title,
                            color = if (selectedTabIndex == index) PurplePrimary else TextSecondary,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Your games library will appear here",
                color = TextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        /* Placeholder UI Commented Out
        // Section Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Your Collection", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("124 Games", color = TextSecondary, fontSize = 14.sp)
            }
            Text("SORT BY RECENT \u25BC", color = PurpleLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        // Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(6) { index ->
                val mockData = getMockDataForIndex(index)
                LibraryGameCard(
                    title = mockData.title,
                    platform = mockData.platform,
                    progress = mockData.progress,
                    progressLabel = mockData.progressLabel,
                    gradientColors = mockData.gradientColors,
                    onClick = onGameClick
                )
            }
        }
        */
    }
}

data class LibraryMock(val title: String, val platform: String, val progress: Float, val progressLabel: String, val gradientColors: List<Color>)

fun getMockDataForIndex(index: Int): LibraryMock {
    return when(index) {
        0 -> LibraryMock("Elden Ring", "Steam", 0.85f, "85%", listOf(Color(0xFFE2C8A8), Color(0xFF3E3A35)))
        1 -> LibraryMock("God of War: Ragnarök", "PlayStation", 1.0f, "Plat", listOf(Color(0xFF8198AB), Color(0xFF16222A)))
        2 -> LibraryMock("Sonic Mania", "RetroAchievements", 0.42f, "42%", listOf(Color(0xFF5E9CFF), Color(0xFF0F2027)))
        3 -> LibraryMock("Hades", "Steam", 0.68f, "68%", listOf(Color(0xFF8E3B46), Color(0xFF1C1115)))
        4 -> LibraryMock("Spider-Man 2", "PlayStation", 0.92f, "92%", listOf(Color(0xFFEFE8CE), Color(0xFF4A4E53)))
        else -> LibraryMock("Celeste", "Steam", 0.15f, "15%", listOf(Color(0xFFC7B1A6), Color(0xFF382329)))
    }
}

@Composable
fun LibraryGameCard(title: String, platform: String, progress: Float, progressLabel: String, gradientColors: List<Color>, onClick: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(colors = gradientColors)),
            contentAlignment = Alignment.BottomStart
        ) {
            // Platform Icon Mock
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Gamepad, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }

            // Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = PurplePrimary,
                    trackColor = ProgressTrack
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(progressLabel, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(platform, color = TextSecondary, fontSize = 12.sp)
    }
}
