package com.naliendev.achieveit.data.remote.steam

import retrofit2.http.GET
import retrofit2.http.Query

interface SteamApi {

    @GET("IPlayerService/GetOwnedGames/v0001/")
    suspend fun getOwnedGames(
        @Query("key") apiKey: String,
        @Query("steamid") steamId: String,
        @Query("include_appinfo") includeAppInfo: Int = 1,
        @Query("include_played_free_games") includePlayedFreeGames: Int = 1,
        @Query("format") format: String = "json"
    ): SteamOwnedGamesResponse

    @GET("ISteamUserStats/GetPlayerAchievements/v0001/")
    suspend fun getPlayerAchievements(
        @Query("key") apiKey: String,
        @Query("steamid") steamId: String,
        @Query("appid") appId: Int,
        @Query("l") language: String = "english"
    ): SteamPlayerAchievementsResponse

    @GET("ISteamUserStats/GetSchemaForGame/v2/")
    suspend fun getSchemaForGame(
        @Query("key") apiKey: String,
        @Query("appid") appId: Int,
        @Query("l") language: String = "english"
    ): SteamSchemaResponse
}
