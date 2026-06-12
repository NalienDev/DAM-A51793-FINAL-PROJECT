package com.naliendev.achieveit.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.naliendev.achieveit.data.local.RaGameDao
import com.naliendev.achieveit.data.local.RaGameEntity
import com.naliendev.achieveit.data.model.RaGame
import com.naliendev.achieveit.data.model.RaGameDetailResponse
import com.naliendev.achieveit.data.remote.RetroAchievementsClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class RaRepository(
    private val dao: RaGameDao,
    private val prefsRepo: UserPrefsRepository = UserPrefsRepository()
) {

    private val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /** Observe cached games from Room (always available offline). */
    fun cachedGamesFlow(): Flow<List<RaGameEntity>> = dao.getAllForUser(uid)

    /**
     * Fetches fresh game data from the RA API and updates the Room cache.
     * Returns the credentials that were used, or null if no credentials saved.
     * Throws on network error.
     */
    suspend fun refreshFromNetwork(): RaCredentials? {
        val creds = prefsRepo.raCredentialsFlow().firstOrNull() ?: return null

        val response = RetroAchievementsClient.api.getUserCompletionProgress(
            callerUsername = creds.username,
            apiKey = creds.apiKey,
            targetUsername = creds.username,
            count = 500,
            offset = 0
        )

        val entities = response.results.map { it.toEntity(uid) }
        dao.upsertAll(entities)
        return creds
    }

    /** Clear the local cache (e.g. when user disconnects RA). */
    suspend fun clearCache() = dao.clearForUser(uid)

    /**
     * Fetches game info and the user's achievement progress for a specific game.
     * Returns null if no credentials are saved.
     * Throws on network error.
     */
    suspend fun getGameDetail(gameId: Int): RaGameDetailResponse? {
        val creds = prefsRepo.raCredentialsFlow().firstOrNull() ?: return null

        return RetroAchievementsClient.api.getGameInfoAndUserProgress(
            callerUsername = creds.username,
            apiKey = creds.apiKey,
            targetUsername = creds.username,
            gameId = gameId
        )
    }
}

// Extension: map network model → Room entity
fun RaGame.toEntity(ownerUid: String) = RaGameEntity(
    gameId = gameId,
    ownerUid = ownerUid,
    title = title,
    imageIcon = imageIcon,
    consoleName = consoleName,
    numAwarded = numAwarded,
    numPossibleAchievements = numPossibleAchievements,
    pctWon = pctWon,
    highestAwardKind = highestAwardKind,
    mostRecentAwardedDate = mostRecentAwardedDate
)

// Extension: map Room entity → progress fraction
val RaGameEntity.progressFraction: Float
    get() = if (numPossibleAchievements > 0)
        numAwarded.toFloat() / numPossibleAchievements.toFloat()
    else 0f

val RaGameEntity.imageIconUrl: String
    get() = "https://media.retroachievements.org$imageIcon"
