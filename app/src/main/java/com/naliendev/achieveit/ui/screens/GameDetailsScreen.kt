package com.naliendev.achieveit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naliendev.achieveit.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailsScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF4A686A), BackgroundDark)
                    )
                )
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Game Details", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Row {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                    }
                }
            }

            // Info overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PurplePrimary)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("TRENDING", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Achevelt", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Text("Action-Adventure • Released 2024", color = TextSecondary, fontSize = 14.sp)
            }
        }

        // Overview
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text("Overview", color = PurpleLight, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Embark on an odyssey through the neon-drenched corridors of Neo-Veridia. In Achevelt, every decision shapes your legacy. Master the art of tactical infiltration and unlock the secrets of a world where data is the only currency that matters.",
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Game details and achievements will appear here",
                color = TextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        /* Placeholder UI Commented Out
        // Total Progress
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, SurfaceVariantDark, RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .padding(20.dp)
        ) {
            Column {
                Text("TOTAL PROGRESS", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("68%", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Completed", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 6.dp))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("42 / 62", color = PurpleLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("TROPHIES WON", color = TextSecondary, fontSize = 10.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = 0.68f,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = PurplePrimary,
                    trackColor = ProgressTrack
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TrophyStat(Gold, "8 Gold")
                    TrophyStat(Silver, "14 Silver")
                    TrophyStat(Bronze, "20 Bronze")
                }
            }
        }

        // Pending Trophies Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pending Trophies", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("View All", color = PurpleLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pending Trophies List
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            TrophyListItem(Icons.Outlined.FlashOn, "Speed Runner", "Complete Level 12 in un...", Gold, "GOLD")
            TrophyListItem(Icons.Outlined.VisibilityOff, "Ghost Protocol", "Infiltrate the Central Hu...", Silver, "SILVER")
            TrophyListItem(Icons.Outlined.Shield, "Iron Wall", "Block 50 consecutive ...", Bronze, "BRONZE")
            TrophyListItem(Icons.Outlined.Psychology, "Mastermind", "Solve the final decrypti...", Silver, "SILVER")
        }

        Spacer(modifier = Modifier.height(32.dp))
        */
    }
}

@Composable
fun TrophyStat(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TrophyListItem(icon: ImageVector, title: String, desc: String, rarityColor: Color, rarityName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PurpleDark.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PurpleLight)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(desc, color = TextSecondary, fontSize = 14.sp)
            }
            Box(
                modifier = Modifier
                    .border(1.dp, rarityColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(rarityName, color = rarityColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
