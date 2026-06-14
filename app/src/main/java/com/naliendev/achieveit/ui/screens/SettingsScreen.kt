package com.naliendev.achieveit.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naliendev.achieveit.data.repository.RaCredentials
import com.naliendev.achieveit.data.repository.UserPrefsRepository
import com.naliendev.achieveit.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsRepo = remember { UserPrefsRepository() }

    // Observe current RA credentials from Firebase
    val credentials by prefsRepo.raCredentialsFlow().collectAsState(initial = null)
    
    // Observe current PSN credentials from Firebase
    val psnCredentials by prefsRepo.psnCredentialsFlow().collectAsState(initial = null)

    // Local form state - RA
    var raUsername by remember { mutableStateOf("") }
    var raApiKey by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isDisconnecting by remember { mutableStateOf(false) }
    var showForm by remember { mutableStateOf(false) }

    // Local form state - PSN
    var psnNpsso by remember { mutableStateOf("") }
    var showPsnNpsso by remember { mutableStateOf(false) }
    var isSavingPsn by remember { mutableStateOf(false) }
    var isDisconnectingPsn by remember { mutableStateOf(false) }
    var showPsnForm by remember { mutableStateOf(false) }

    // Observe current Steam credentials from Firebase
    val steamCredentials by prefsRepo.steamCredentialsFlow().collectAsState(initial = null)

    // Local form state - Steam
    var steamId by remember { mutableStateOf("") }
    var steamApiKey by remember { mutableStateOf("") }
    var showSteamApiKey by remember { mutableStateOf(false) }
    var isSavingSteam by remember { mutableStateOf(false) }
    var isDisconnectingSteam by remember { mutableStateOf(false) }
    var showSteamForm by remember { mutableStateOf(false) }

    // Sync form fields when credentials load
    LaunchedEffect(credentials) {
        if (credentials != null && raUsername.isBlank()) {
            raUsername = credentials!!.username
        }
    }

    LaunchedEffect(steamCredentials) {
        if (steamCredentials != null && steamId.isBlank()) {
            steamId = steamCredentials!!.steamId
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                "Settings",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Integrations section ────────────────────────────────────────────
        Text(
            "INTEGRATIONS",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        // RetroAchievements card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "RetroAchievements",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (credentials != null) "Connected as @${credentials!!.username}"
                            else "Not connected",
                            color = if (credentials != null) Color(0xFF4CAF50) else TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                    if (credentials != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF4CAF50).copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Connected",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (credentials != null && !showForm) {
                    // Connected state — show disconnect + edit buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { showForm = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpleLight),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Edit credentials")
                        }
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isDisconnecting = true
                                    prefsRepo.clearRaCredentials()
                                    raUsername = ""
                                    raApiKey = ""
                                    isDisconnecting = false
                                    Toast.makeText(context, "RetroAchievements disconnected", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f),
                            enabled = !isDisconnecting
                        ) {
                            if (isDisconnecting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFFEF5350), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Disconnect")
                            }
                        }
                    }
                } else {
                    // Not connected or editing — show form
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Username field
                        OutlinedTextField(
                            value = raUsername,
                            onValueChange = { raUsername = it },
                            label = { Text("RA Username") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PurplePrimary,
                                unfocusedBorderColor = SurfaceVariantDark,
                                focusedLabelColor = PurplePrimary,
                                unfocusedLabelColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = PurplePrimary
                            )
                        )

                        // API Key field
                        OutlinedTextField(
                            value = raApiKey,
                            onValueChange = { raApiKey = it },
                            label = { Text("Web API Key") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                TextButton(onClick = { showApiKey = !showApiKey }) {
                                    Text(if (showApiKey) "Hide" else "Show", color = PurpleLight, fontSize = 12.sp)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PurplePrimary,
                                unfocusedBorderColor = SurfaceVariantDark,
                                focusedLabelColor = PurplePrimary,
                                unfocusedLabelColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = PurplePrimary
                            )
                        )

                        // Helper text
                        Text(
                            "Get your API key at retroachievements.org/controlpanel.php → Keys section",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        // Action buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (showForm && credentials != null) {
                                OutlinedButton(
                                    onClick = { showForm = false; raApiKey = "" },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariantDark)
                                ) { Text("Cancel") }
                            }

                            Button(
                                onClick = {
                                    if (raUsername.isBlank() || raApiKey.isBlank()) {
                                        Toast.makeText(context, "Please fill in both fields", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    scope.launch {
                                        isSaving = true
                                        try {
                                            prefsRepo.saveRaCredentials(raUsername.trim(), raApiKey.trim())
                                            showForm = false
                                            raApiKey = ""
                                            Toast.makeText(context, "RetroAchievements connected!", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            isSaving = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isSaving,
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("Connect", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // PlayStation Network card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "PlayStation Network",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (psnCredentials != null) "Connected" else "Not connected",
                            color = if (psnCredentials != null) Color(0xFF4CAF50) else TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                    if (psnCredentials != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF4CAF50).copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Connected",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (psnCredentials != null && !showPsnForm) {
                    // Connected state
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { showPsnForm = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpleLight),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Update NPSSO")
                        }
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isDisconnectingPsn = true
                                    prefsRepo.clearPsnCredentials()
                                    psnNpsso = ""
                                    isDisconnectingPsn = false
                                    Toast.makeText(context, "PlayStation Network disconnected", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f),
                            enabled = !isDisconnectingPsn
                        ) {
                            if (isDisconnectingPsn) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFFEF5350), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Disconnect")
                            }
                        }
                    }
                } else {
                    // Form
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = psnNpsso,
                            onValueChange = { psnNpsso = it },
                            label = { Text("NPSSO Token") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (showPsnNpsso) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                TextButton(onClick = { showPsnNpsso = !showPsnNpsso }) {
                                    Text(if (showPsnNpsso) "Hide" else "Show", color = PurpleLight, fontSize = 12.sp)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PurplePrimary,
                                unfocusedBorderColor = SurfaceVariantDark,
                                focusedLabelColor = PurplePrimary,
                                unfocusedLabelColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = PurplePrimary
                            )
                        )

                        Text(
                            "Get your 64-character NPSSO token by logging into ca.account.sony.com in a browser.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (showPsnForm && psnCredentials != null) {
                                OutlinedButton(
                                    onClick = { showPsnForm = false; psnNpsso = "" },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariantDark)
                                ) { Text("Cancel") }
                            }

                            Button(
                                onClick = {
                                    if (psnNpsso.isBlank() || psnNpsso.trim().length != 64) {
                                        Toast.makeText(context, "Invalid NPSSO length (must be 64 characters)", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    scope.launch {
                                        isSavingPsn = true
                                        try {
                                            prefsRepo.savePsnNpsso(psnNpsso.trim())
                                            showPsnForm = false
                                            psnNpsso = ""
                                            Toast.makeText(context, "PlayStation Network connected!", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            isSavingPsn = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isSavingPsn,
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                            ) {
                                if (isSavingPsn) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("Connect", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Steam card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Steam",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (steamCredentials != null) "Connected" else "Not connected",
                            color = if (steamCredentials != null) Color(0xFF4CAF50) else TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                    if (steamCredentials != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF4CAF50).copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Connected",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (steamCredentials != null && !showSteamForm) {
                    // Connected state
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { showSteamForm = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpleLight),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Edit credentials")
                        }
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isDisconnectingSteam = true
                                    prefsRepo.clearSteamCredentials()
                                    steamId = ""
                                    steamApiKey = ""
                                    isDisconnectingSteam = false
                                    Toast.makeText(context, "Steam disconnected", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f),
                            enabled = !isDisconnectingSteam
                        ) {
                            if (isDisconnectingSteam) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFFEF5350), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Disconnect")
                            }
                        }
                    }
                } else {
                    // Form
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = steamId,
                            onValueChange = { steamId = it },
                            label = { Text("Steam ID (64-bit)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PurplePrimary,
                                unfocusedBorderColor = SurfaceVariantDark,
                                focusedLabelColor = PurplePrimary,
                                unfocusedLabelColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = PurplePrimary
                            )
                        )

                        OutlinedTextField(
                            value = steamApiKey,
                            onValueChange = { steamApiKey = it },
                            label = { Text("Web API Key") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (showSteamApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                TextButton(onClick = { showSteamApiKey = !showSteamApiKey }) {
                                    Text(if (showSteamApiKey) "Hide" else "Show", color = PurpleLight, fontSize = 12.sp)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PurplePrimary,
                                unfocusedBorderColor = SurfaceVariantDark,
                                focusedLabelColor = PurplePrimary,
                                unfocusedLabelColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = PurplePrimary
                            )
                        )

                        Text(
                            "Find your Steam ID at steamid.io and your API key at steamcommunity.com/dev/apikey",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (showSteamForm && steamCredentials != null) {
                                OutlinedButton(
                                    onClick = { showSteamForm = false; steamApiKey = "" },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariantDark)
                                ) { Text("Cancel") }
                            }

                            Button(
                                onClick = {
                                    if (steamId.isBlank() || steamApiKey.isBlank()) {
                                        Toast.makeText(context, "Please fill in both fields", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    scope.launch {
                                        isSavingSteam = true
                                        try {
                                            prefsRepo.saveSteamCredentials(steamId.trim(), steamApiKey.trim())
                                            showSteamForm = false
                                            steamApiKey = ""
                                            Toast.makeText(context, "Steam connected!", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            isSavingSteam = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isSavingSteam,
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                            ) {
                                if (isSavingSteam) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("Connect", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun IntegrationComingSoonCard(platform: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(platform, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Coming soon", color = TextSecondary, fontSize = 13.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariantDark)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Soon", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
