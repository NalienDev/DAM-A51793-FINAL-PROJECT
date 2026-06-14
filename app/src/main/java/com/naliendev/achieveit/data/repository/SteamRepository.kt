package com.naliendev.achieveit.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.naliendev.achieveit.data.local.steam.SteamGameDao
import com.naliendev.achieveit.data.local.steam.SteamGameEntity
import com.naliendev.achieveit.data.remote.steam.SteamClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class SteamRepository(
    private val dao: SteamGameDao,
    private val prefsRepo: UserPrefsRepository = UserPrefsRepository()
) {
    private val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun cachedGamesFlow(): Flow<List<SteamGameEntity>> = dao.getAllForUser(uid)

    suspend fun refreshFromNetwork(): SteamCredentials? = coroutineScope {
        val creds = prefsRepo.steamCredentialsFlow().firstOrNull() ?: return@coroutineScope null

        val response = SteamClient.api.getOwnedGames(
            apiKey = creds.apiKey,
            steamId = creds.steamId
        )

        val games = response.response.games ?: emptyList()

        // For each game that reports community visible stats, verify it actually has achievements via schema
        val entities = games.map { game ->
            async {
                if (game.hasCommunityVisibleStats) {
                    // Fetch schema to know total achievements
                    val totalAchievements = try {
                        SteamClient.api.getSchemaForGame(
                            apiKey = creds.apiKey,
                            appId = game.appId
                        ).game?.availableGameStats?.achievements?.size ?: 0
                    } catch (e: Exception) {
                        0
                    }
                    if (totalAchievements > 0) {
                        // Heuristic: filter out demos, betas, playtests, trials based on name
                        val lowerName = game.name.lowercase()
                        if (lowerName.contains("demo") || lowerName.contains("beta") || lowerName.contains("playtest") || lowerName.contains("trial")) {
                            null
                        } else {
                            // Fetch earned achievements count
                            val earnedAchievements = try {
                                SteamClient.api.getPlayerAchievements(
                                    apiKey = creds.apiKey,
                                    steamId = creds.steamId,
                                    appId = game.appId
                                ).playerstats?.achievements?.count { it.achieved == 1 } ?: 0
                            } catch (e: Exception) {
                                0
                            }
                            SteamGameEntity(
                                appId = game.appId,
                                ownerUid = uid,
                                name = game.name,
                                iconUrl = "https://steamcdn-a.akamaihd.net/steam/apps/${game.appId}/library_600x900.jpg",
                                playtimeMinutes = game.playtimeForever,
                                totalAchievements = totalAchievements,
                                earnedAchievements = earnedAchievements
                            )
                        }
                    } else null
                } else null
            }
        }.awaitAll().filterNotNull()

        dao.upsertAll(entities)
        creds
    }

    suspend fun getGameSchemaAndStats(appId: Int): Pair<com.naliendev.achieveit.data.remote.steam.SteamSchemaGame?, com.naliendev.achieveit.data.remote.steam.SteamPlayerStats?>? = coroutineScope {
        val creds = prefsRepo.steamCredentialsFlow().firstOrNull() ?: return@coroutineScope null
        try {
            val schemaJob = async {
                SteamClient.api.getSchemaForGame(
                    apiKey = creds.apiKey,
                    appId = appId
                ).game
            }
            val statsJob = async {
                SteamClient.api.getPlayerAchievements(
                    apiKey = creds.apiKey,
                    steamId = creds.steamId,
                    appId = appId
                ).playerstats
            }
            Pair(schemaJob.await(), statsJob.await())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun clearCache() = dao.clearForUser(uid)
}
