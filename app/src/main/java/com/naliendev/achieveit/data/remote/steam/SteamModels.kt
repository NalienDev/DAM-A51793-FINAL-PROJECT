package com.naliendev.achieveit.data.remote.steam

import com.google.gson.annotations.SerializedName

data class SteamOwnedGamesResponse(
    @SerializedName("response") val response: SteamOwnedGamesResult
)

data class SteamOwnedGamesResult(
    @SerializedName("game_count") val gameCount: Int,
    @SerializedName("games") val games: List<SteamGame>? = emptyList()
)

data class SteamGame(
    @SerializedName("appid") val appId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("playtime_forever") val playtimeForever: Int,
    @SerializedName("img_icon_url") val imgIconUrl: String,
    @SerializedName("has_community_visible_stats") val hasCommunityVisibleStats: Boolean = false
)

data class SteamPlayerAchievementsResponse(
    @SerializedName("playerstats") val playerstats: SteamPlayerStats?
)

data class SteamPlayerStats(
    @SerializedName("steamID") val steamID: String?,
    @SerializedName("gameName") val gameName: String?,
    @SerializedName("achievements") val achievements: List<SteamAchievement>? = null,
    @SerializedName("success") val success: Boolean,
    @SerializedName("error") val error: String? = null
)

data class SteamAchievement(
    @SerializedName("apiname") val apiname: String,
    @SerializedName("achieved") val achieved: Int,
    @SerializedName("unlocktime") val unlocktime: Long
)

data class SteamSchemaResponse(
    @SerializedName("game") val game: SteamSchemaGame?
)

data class SteamSchemaGame(
    @SerializedName("gameName") val gameName: String?,
    @SerializedName("availableGameStats") val availableGameStats: SteamSchemaStats?
)

data class SteamSchemaStats(
    @SerializedName("achievements") val achievements: List<SteamSchemaAchievement>?
)

data class SteamSchemaAchievement(
    @SerializedName("name") val name: String,
    @SerializedName("defaultvalue") val defaultvalue: Int,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("hidden") val hidden: Int,
    @SerializedName("description") val description: String?,
    @SerializedName("icon") val icon: String,
    @SerializedName("icongray") val icongray: String
)
