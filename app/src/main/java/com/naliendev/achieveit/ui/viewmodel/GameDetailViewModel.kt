package com.naliendev.achieveit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.naliendev.achieveit.data.local.AchieveItDatabase
import com.naliendev.achieveit.data.repository.PsnRepository
import com.naliendev.achieveit.data.repository.RaRepository
import com.naliendev.achieveit.ui.models.UnifiedGameDetail
import com.naliendev.achieveit.ui.models.UnifiedTrophy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

sealed class GameDetailUiState {
    object Loading : GameDetailUiState()
    data class Success(val gameDetail: UnifiedGameDetail) : GameDetailUiState()
    data class Error(val message: String) : GameDetailUiState()
}

class GameDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AchieveItDatabase.getInstance(application)
    private val raRepository = RaRepository(db.raGameDao())
    private val psnRepository = PsnRepository(db.psnGameDao())

    private val _uiState = MutableStateFlow<GameDetailUiState>(GameDetailUiState.Loading)
    val uiState: StateFlow<GameDetailUiState> = _uiState.asStateFlow()

    fun fetchGameDetails(gameId: String) {
        viewModelScope.launch {
            _uiState.value = GameDetailUiState.Loading
            try {
                val raId = gameId.toIntOrNull()
                if (raId != null) {
                    // It's a RetroAchievements game
                    val response = raRepository.getGameDetail(raId)
                    if (response != null) {
                        val unifiedDetail = UnifiedGameDetail(
                            title = response.title,
                            description = response.consoleName,
                            imageUrl = response.imageIconUrl,
                            earnedTrophies = response.numAwardedToUser,
                            totalTrophies = response.numAchievements,
                            trophies = response.achievements.values.map { ach ->
                                UnifiedTrophy(
                                    id = ach.id.toString(),
                                    title = ach.title,
                                    description = ach.description,
                                    imageUrl = if (ach.isEarned) ach.badgeUrl else ach.badgeLockedUrl,
                                    isEarned = ach.isEarned,
                                    earnedDate = ach.dateEarned,
                                    type = "Achievement"
                                )
                            }
                        )
                        _uiState.value = GameDetailUiState.Success(unifiedDetail)
                    } else {
                        _uiState.value = GameDetailUiState.Error("No credentials saved.")
                    }
                } else if (gameId.startsWith("steam_")) {
                    val steamRepository = com.naliendev.achieveit.data.repository.SteamRepository(db.steamGameDao())
                    val appId = gameId.removePrefix("steam_").toIntOrNull()
                    if (appId != null) {
                        val response = steamRepository.getGameSchemaAndStats(appId)
                        if (response != null) {
                            val (schema, stats) = response
                            val achievements = schema?.availableGameStats?.achievements ?: emptyList()
                            val playerStats = stats?.achievements ?: emptyList()
                            val achievedMap = playerStats.associateBy { it.apiname }
                            
                            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            val cachedGame = db.steamGameDao().getGame(uid, appId)
                            
                            val unifiedDetail = UnifiedGameDetail(
                                title = schema?.gameName ?: cachedGame?.name ?: "Steam Game",
                                description = "Steam",
                                imageUrl = cachedGame?.iconUrl ?: "",
                                earnedTrophies = playerStats.count { it.achieved == 1 },
                                totalTrophies = achievements.size,
                                trophies = achievements.map { ach ->
                                    val playerAch = achievedMap[ach.name]
                                    val isEarned = playerAch?.achieved == 1
                                    val earnedDateStr = if (isEarned && playerAch != null) {
                                        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                                            .format(java.util.Date(playerAch.unlocktime * 1000))
                                    } else ""
                                    
                                    UnifiedTrophy(
                                        id = ach.name,
                                        title = ach.displayName,
                                        description = ach.description ?: "",
                                        imageUrl = if (isEarned) ach.icon else ach.icongray,
                                        isEarned = isEarned,
                                        earnedDate = earnedDateStr,
                                        type = "Achievement"
                                    )
                                }
                            )
                            _uiState.value = GameDetailUiState.Success(unifiedDetail)
                        } else {
                            _uiState.value = GameDetailUiState.Error("Failed to fetch Steam stats.")
                        }
                    } else {
                        _uiState.value = GameDetailUiState.Error("Invalid Steam App ID.")
                    }
                } else {
                    val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val cachedGame = db.psnGameDao().getGame(uid, gameId)
                    val npServiceName = cachedGame?.npServiceName ?: "trophy"

                    val response = psnRepository.getGameTrophies(gameId, npServiceName)
                    if (response != null) {
                        val unifiedDetail = UnifiedGameDetail(
                            title = cachedGame?.title ?: "PlayStation Game",
                            description = cachedGame?.platform ?: "PlayStation",
                            imageUrl = cachedGame?.imageIcon ?: "",
                            earnedTrophies = cachedGame?.totalEarned ?: response.trophies.count { it.earned },
                            totalTrophies = cachedGame?.totalTrophies ?: response.trophies.size,
                            trophies = response.trophies.map { trophy ->
                                UnifiedTrophy(
                                    id = trophy.trophyId.toString(),
                                    title = trophy.trophyName ?: "Hidden Trophy",
                                    description = trophy.trophyDetail ?: "Details hidden.",
                                    imageUrl = trophy.trophyIconUrl ?: "",
                                    isEarned = trophy.earned,
                                    earnedDate = trophy.earnedDateTime,
                                    type = trophy.displayType
                                )
                            }
                        )
                        _uiState.value = GameDetailUiState.Success(unifiedDetail)
                    } else {
                        _uiState.value = GameDetailUiState.Error("Failed to fetch PSN trophies.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = GameDetailUiState.Error("Error: ${e.localizedMessage}")
            }
        }
    }
}
