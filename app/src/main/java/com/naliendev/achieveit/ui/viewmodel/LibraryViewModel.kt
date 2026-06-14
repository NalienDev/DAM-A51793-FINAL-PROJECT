package com.naliendev.achieveit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.naliendev.achieveit.data.local.AchieveItDatabase
import com.naliendev.achieveit.data.repository.PsnRepository
import com.naliendev.achieveit.data.repository.RaRepository
import com.naliendev.achieveit.data.repository.SteamRepository
import com.naliendev.achieveit.data.repository.UserPrefsRepository
import com.naliendev.achieveit.ui.models.LibraryGame
import com.naliendev.achieveit.ui.models.Platform
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope

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
    private val steamRepository = SteamRepository(db.steamGameDao())
    private val prefsRepo = UserPrefsRepository()

    // 0 = All, 1 = RetroAchievements, 2 = Steam, 3 = PlayStation
    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _allGames = MutableStateFlow<List<LibraryGame>>(emptyList())
    val allGames: StateFlow<List<LibraryGame>> = _allGames.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    private val _hasCredentials = MutableStateFlow<Boolean?>(null) // null = unknown

    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    init {
        // Combine all games + selected tab + search query → filtered uiState
        viewModelScope.launch {
            combine(_allGames, _selectedTabIndex, _searchQuery, _isOffline, _hasCredentials) { games, tabIndex, query, offline, hasCreds ->
                when {
                    hasCreds == false -> LibraryUiState.NoCredentials
                    hasCreds == null -> LibraryUiState.Loading
                    else -> {
                        val filtered = when (tabIndex) {
                            1 -> games.filter { it.platform == Platform.RETRO_ACHIEVEMENTS }
                            2 -> games.filter { it.platform == Platform.STEAM }
                            3 -> games.filter { it.platform == Platform.PLAYSTATION }
                            else -> games // 0 = All
                        }.filter { it.title.contains(query, ignoreCase = true) }
                        LibraryUiState.Success(games = filtered, isOffline = offline)
                    }
                }
            }.collect { _uiState.value = it }
        }

        // Load data from cache flows
        viewModelScope.launch {
            val combinedCreds = combine(
                prefsRepo.raCredentialsFlow(),
                prefsRepo.psnCredentialsFlow(),
                prefsRepo.steamCredentialsFlow()
            ) { ra, psn, steam -> Triple(ra, psn, steam) }

            combine(
                raRepository.cachedGamesFlow(),
                psnRepository.cachedGamesFlow(),
                steamRepository.cachedGamesFlow(),
                combinedCreds
            ) { raGames, psnGames, steamGames, creds ->
                val (raCreds, psnCreds, steamCreds) = creds
                val hasAny = raCreds != null || psnCreds != null || steamCreds != null
                _hasCredentials.value = hasAny

                if (hasAny) {
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

                    unifiedGames.addAll(steamGames.map { game ->
                        val progressFraction = if (game.totalAchievements > 0)
                            game.earnedAchievements.toFloat() / game.totalAchievements.toFloat()
                        else 0f
                        LibraryGame(
                            id = "steam_${game.appId}",
                            title = game.name,
                            platform = Platform.STEAM,
                            imageUrl = game.iconUrl,
                            progressFraction = progressFraction,
                            earnedTrophies = game.earnedAchievements,
                            totalTrophies = game.totalAchievements,
                            lastActivity = game.lastUpdated.toString(), // Store timestamp as string for sorting
                            isPsn = false
                        )
                    })

                    unifiedGames.sortByDescending { it.lastActivity }
                    _allGames.value = unifiedGames
                }
            }.collect()
        }

        refresh()
    }

    fun setSelectedTab(index: Int) {
        _selectedTabIndex.value = index
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                supervisorScope {
                    val raJob = async { raRepository.refreshFromNetwork() }
                    val psnJob = async { psnRepository.refreshFromNetwork() }
                    val steamJob = async { steamRepository.refreshFromNetwork() }
                    try {
                        raJob.await()
                        psnJob.await()
                        steamJob.await()
                        _isOffline.value = false
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _isOffline.value = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _isOffline.value = true
            }
        }
    }
}
