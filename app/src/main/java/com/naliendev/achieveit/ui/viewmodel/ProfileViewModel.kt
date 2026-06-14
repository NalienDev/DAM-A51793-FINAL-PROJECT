package com.naliendev.achieveit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.naliendev.achieveit.data.local.AchieveItDatabase
import com.naliendev.achieveit.data.repository.PsnRepository
import com.naliendev.achieveit.data.repository.RaRepository
import com.naliendev.achieveit.data.repository.UserPrefsRepository
import com.naliendev.achieveit.data.repository.UserProfile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val displayName: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val memberSince: String = "2024",
    val totalPlaytime: String = "0h",
    val completionRate: String = "0%",
    val perfectGames: Int = 0,
    val platinumTrophies: Int = 0
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AchieveItDatabase.getInstance(application)
    private val raRepository = RaRepository(db.raGameDao())
    private val psnRepository = PsnRepository(db.psnGameDao())
    private val prefsRepo = UserPrefsRepository()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                prefsRepo.userProfileFlow(),
                raRepository.cachedGamesFlow(),
                psnRepository.cachedGamesFlow(),
                prefsRepo.raCredentialsFlow(),
                prefsRepo.psnCredentialsFlow()
            ) { profile, raGames, psnGames, raCreds, psnCreds ->
                val hasAnyCreds = raCreds != null || psnCreds != null
                
                val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                val finalDisplayName = profile.displayName.ifBlank {
                    authUser?.displayName?.takeIf { it.isNotBlank() }
                        ?: authUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                        ?: "Player"
                }

                val finalBio = profile.bio.ifBlank {
                    if (hasAnyCreds) "Achievement hunter."
                    else "Connect your accounts in settings to start tracking achievements!"
                }

                val creationTimestamp = authUser?.metadata?.creationTimestamp ?: System.currentTimeMillis()
                val sdf = java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault())
                val creationYear = sdf.format(java.util.Date(creationTimestamp))

                if (!hasAnyCreds) {
                    ProfileUiState(
                        displayName = finalDisplayName,
                        bio = finalBio,
                        avatarUrl = profile.avatarUrl,
                        memberSince = creationYear,
                        totalPlaytime = "0h",
                        completionRate = "0%",
                        perfectGames = 0,
                        platinumTrophies = 0
                    )
                } else {
                    val actualPlaytimeHours = 0
                    val playtimeStr = "${actualPlaytimeHours}h"

                    val perfectRa = raGames.count { it.numPossibleAchievements > 0 && it.numAwarded == it.numPossibleAchievements }
                    val perfectPsn = psnGames.count { it.progress >= 100 }
                    val perfectCount = perfectRa + perfectPsn

                    val platinums = psnGames.sumOf { it.earnedPlatinum }

                    var totalProgress = 0f
                    var gameCountWithProgress = 0
                    raGames.forEach { game ->
                        if (game.numPossibleAchievements > 0) {
                            totalProgress += (game.numAwarded.toFloat() / game.numPossibleAchievements.toFloat()) * 100f
                            gameCountWithProgress++
                        }
                    }
                    psnGames.forEach { game ->
                        totalProgress += game.progress.toFloat()
                        gameCountWithProgress++
                    }
                    val avgCompletion = if (gameCountWithProgress > 0) {
                        (totalProgress / gameCountWithProgress).toInt()
                    } else 0

                    ProfileUiState(
                        displayName = finalDisplayName,
                        bio = finalBio,
                        avatarUrl = profile.avatarUrl,
                        memberSince = creationYear,
                        totalPlaytime = playtimeStr,
                        completionRate = "$avgCompletion%",
                        perfectGames = perfectCount,
                        platinumTrophies = platinums
                    )
                }
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun saveProfile(displayName: String, bio: String, avatarUrl: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            prefsRepo.saveUserProfile(displayName, bio, avatarUrl)
            onComplete()
        }
    }
}
