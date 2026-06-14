package com.naliendev.achieveit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.naliendev.achieveit.ui.models.LibraryGame
import com.naliendev.achieveit.ui.theme.*
import com.naliendev.achieveit.ui.viewmodel.LibraryUiState
import com.naliendev.achieveit.ui.viewmodel.LibraryViewModel
import com.naliendev.achieveit.ui.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGameClick: (String) -> Unit,
    onLogoutClick: () -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    
    // ViewModels
    val libraryViewModel: LibraryViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    val profileState by profileViewModel.uiState.collectAsState()
    val libraryState by libraryViewModel.uiState.collectAsState()

    // Determine stats — completion rate comes from ProfileViewModel to stay in sync
    val (totalGamesStr, totalAchievementsStr, continuePlayingGames) = when (val state = libraryState) {
        is LibraryUiState.Success -> {
            val games = state.games
            val totalGames = games.size
            val totalAchievements = games.sumOf { it.earnedTrophies }

            // In-progress games first (> 0% and < 100%), then fall back to all games by recency
            val inProgress = games.filter { it.progressFraction > 0f && it.progressFraction < 1.0f }
            val displayGames = if (inProgress.isNotEmpty()) inProgress else games

            Triple(totalGames.toString(), totalAchievements.toString(), displayGames)
        }
        else -> Triple("0", "0", emptyList())
    }

    // Completion rate always sourced from ProfileViewModel (identical formula to Profile screen)
    val completionRateStr = profileState.completionRate

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
    ) {
        // Top App Bar Area
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PurpleDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.EmojiEvents, contentDescription = "Logo", tint = PurplePrimary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("AchieveIt", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = TextPrimary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            auth.signOut()
                            onLogoutClick()
                        }
                        .padding(8.dp)
                )
            }
        }

        // Greeting
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text("Hello, ${profileState.displayName}", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text(
                "Ready to unlock your next achievement?",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )
        }

        // Stats Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Outlined.Gamepad, contentDescription = null, tint = PurplePrimary) },
                title = "TOTAL GAMES",
                value = totalGamesStr,
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = PurpleLight) },
                title = "ACHIEVEMENTS",
                value = totalAchievementsStr,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Outlined.Insights, contentDescription = null, tint = PurplePrimary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("COMPLETION", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(completionRateStr, color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Continue Playing
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Continue Playing", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("View All", color = PurplePrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        if (continuePlayingGames.isEmpty()) {
            // Mock items if no integration is connected yet
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(2) { index ->
                    val title = if (index == 0) "Loading..." else "Loading..."
                    val subtitle = if (index == 0) "..." else "..."
                    val progress = if (index == 0) 0.0f else 0.0f
                    GameCardMock(
                        title = title,
                        subtitle = subtitle,
                        progress = progress,
                        onClick = {}
                    )
                }
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(continuePlayingGames, key = { it.id }) { game ->
                    RealGameCardHome(game = game, onClick = { onGameClick(game.id) })
                }
            }
        }

    }
}

data class Quadruple<T1, T2, T3, T4>(val first: T1, val second: T2, val third: T3, val fourth: T4)

@Composable
fun RealGameCardHome(game: LibraryGame, onClick: () -> Unit) {
    val progress = game.progressFraction
    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark),
            contentAlignment = Alignment.BottomStart
        ) {
            AsyncImage(
                model = game.imageUrl,
                contentDescription = game.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dark gradient overlay at the bottom for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 150f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = PurplePrimary,
                    trackColor = ProgressTrack
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("${(progress * 100).toInt()}% Complete", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            game.title,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(game.platform.displayName, color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, icon: @Composable () -> Unit, title: String, value: String) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                icon()
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(value, color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GameCardMock(title: String, subtitle: String, progress: Float, onClick: () -> Unit) {
    Column(modifier = Modifier
        .width(160.dp)
        .clickable { onClick() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF2C3E50), Color(0xFF000000))
                    )
                ),
            contentAlignment = Alignment.BottomStart
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = PurplePrimary,
                    trackColor = ProgressTrack
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("${(progress * 100).toInt()}% Complete", color = TextSecondary, fontSize = 10.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = TextSecondary, fontSize = 12.sp)
    }
}

