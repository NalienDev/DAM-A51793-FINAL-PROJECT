package com.naliendev.achieveit.data.local.steam

import androidx.room.Entity

@Entity(tableName = "steam_games", primaryKeys = ["appId", "ownerUid"])
data class SteamGameEntity(
    val appId: Int,
    val ownerUid: String,
    val name: String,
    val iconUrl: String,
    val playtimeMinutes: Int,
    val totalAchievements: Int,
    val earnedAchievements: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)
