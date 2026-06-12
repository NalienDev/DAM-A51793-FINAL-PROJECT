package com.naliendev.achieveit.data.local.psn

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "psn_games")
data class PsnGameEntity(
    @PrimaryKey
    val npCommunicationId: String,
    val ownerUid: String,
    val title: String,
    val imageIcon: String,
    val platform: String,
    val npServiceName: String,
    val bronze: Int,
    val silver: Int,
    val gold: Int,
    val platinum: Int,
    val earnedBronze: Int,
    val earnedSilver: Int,
    val earnedGold: Int,
    val earnedPlatinum: Int,
    val progress: Int,
    val lastUpdated: String
) {
    val totalTrophies: Int
        get() = bronze + silver + gold + platinum

    val totalEarned: Int
        get() = earnedBronze + earnedSilver + earnedGold + earnedPlatinum
}
