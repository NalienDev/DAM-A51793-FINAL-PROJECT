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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naliendev.achieveit.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onGameClick: () -> Unit, onLogoutClick: () -> Unit = {}) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val accountName = currentUser?.displayName?.takeIf { it.isNotBlank() }
        ?: currentUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
        ?: "Player"
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
                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = TextPrimary)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = TextPrimary,
                    modifier = Modifier.clickable {
                        auth.signOut()
                        onLogoutClick()
                    }
                )
            }
        }

        // Greeting
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text("Hello, $accountName", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text(
                "Ready to unlock your next achievement?",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Stats and Games will appear here",
                color = TextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        /* Placeholder UI Commented Out
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
                value = "124",
                trend = "+2"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = PurpleLight) },
                title = "ACHIEVEMENTS",
                value = "1,452",
                trend = "+45"
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
                    Text("+1.2%", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("COMPLETION", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("68%", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
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

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mock items
            items(2) { index ->
                GameCardMock(
                    title = if (index == 0) "Cyber Nexus 2077" else "Elder Realms",
                    subtitle = if (index == 0) "Played 2h ago" else "Played yesterday",
                    progress = if (index == 0) 0.85f else 0.42f,
                    onClick = onGameClick
                )
            }
        }

        // Recommended Next
        Text(
            "Recommended Next",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 24.dp, top = 32.dp, bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PurpleDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = PurpleLight)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Grand Explorer", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Visit every district in Neo-Tokyo", color = TextSecondary, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("50G", color = PurpleLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("95% Rare", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
        */
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, icon: @Composable () -> Unit, title: String, value: String, trend: String) {
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
                Text(trend, color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    progress = progress,
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
