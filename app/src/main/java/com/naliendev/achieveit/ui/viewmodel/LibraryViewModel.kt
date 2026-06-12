package com.naliendev.achieveit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.naliendev.achieveit.data.local.AchieveItDatabase
import com.naliendev.achieveit.data.repository.PsnRepository
import com.naliendev.achieveit.data.repository.RaRepository
import com.naliendev.achieveit.data.repository.UserPrefsRepository
import com.naliendev.achieveit.ui.models.LibraryGame
import com.naliendev.achieveit.ui.models.Platform
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class LibraryUiState {
    object NoCredentials : LibraryUiState()
    object Loading : LibraryUiState()
    data class Success(
        val games: List<LibraryGame>,
        val isOffline: Boolean = false
    ) : LibraryUiState()
    data class Error(
        val message: String,
        val cachedGames: List<LibraryGame> = emptyList()
    ) : LibraryUiState()
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AchieveItDatabase.getInstance(application)
    private val raRepository = RaRepository(db.raGameDao())
    private val psnRepository = PsnRepository(db.psnGameDao())
    private val prefsRepo = UserPrefsRepository()

    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                raRepository.cachedGamesFlow(),
                psnRepository.cachedGamesFlow(),
                prefsRepo.raCredentialsFlow(),
                prefsRepo.psnCredentialsFlow()
            ) { raGames, psnGames, raCreds, psnCreds ->
                if (raCreds == null && psnCreds == null) {
                    _uiState.value = LibraryUiState.NoCredentials
                } else {
                    val unifiedGames = mutableListOf<LibraryGame>()
                    
                    unifiedGames.addAll(raGames.map { game ->
                        val progressFraction = if (game.numPossibleAchievements > 0)
                            game.numAwarded.toFloat() / game.numPossibleAchievements.toFloat()
                        else 0f
                        LibraryGame(
                            id = game.gameId.toString(),
                            title = game.title,
                            platform = Platform.RETRO_ACHIEVEMENTS,
                            imageUrl = "https://media.retroachievements.org${game.imageIcon}",
                            progressFraction = progressFraction,
                            earnedTrophies = game.numAwarded,
                            totalTrophies = game.numPossibleAchievements,
                            lastActivity = game.mostRecentAwardedDate ?: "",
                            isPsn = false
                        )
                    })

                    unifiedGames.addAll(psnGames.map { game ->
                        val progressFraction = game.progress.toFloat() / 100f
                        LibraryGame(
                            id = game.npCommunicationId,
                            title = game.title,
                            platform = Platform.PLAYSTATION,
                            imageUrl = game.imageIcon,
                            progressFraction = progressFraction,
                            earnedTrophies = game.totalEarned,
                            totalTrophies = game.totalTrophies,
                            lastActivity = game.lastUpdated,
                            isPsn = true
                        )
                    })

                    // Sort by last activity descending
                    unifiedGames.sortByDescending { it.lastActivity }

                    if (unifiedGames.isNotEmpty() || (raCreds != null || psnCreds != null)) {
                        _uiState.value = LibraryUiState.Success(unifiedGames)
                    }
                }
            }.collect()
        }

        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                // Fetch in parallel
                val raJob = launch { raRepository.refreshFromNetwork() }
                val psnJob = launch { psnRepository.refreshFromNetwork() }
                
                raJob.join()
                psnJob.join()
                
            } catch (e: Exception) {
                e.printStackTrace()
                // Show cached via Flow already, can flag as offline if needed
                val current = _uiState.value
                if (current is LibraryUiState.Success) {
                    _uiState.value = current.copy(isOffline = true)
                }
            }
        }
    }
}
