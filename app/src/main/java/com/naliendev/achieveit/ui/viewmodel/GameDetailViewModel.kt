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
                } else {
                    // It's a PSN game — look up npServiceName from the cached game first
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
