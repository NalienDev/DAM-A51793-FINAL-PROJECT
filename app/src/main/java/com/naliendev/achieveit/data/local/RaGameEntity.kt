package com.naliendev.achieveit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached copy of a RetroAchievements game entry, keyed by (gameId, ownerUid).
 * ownerUid ties the cache to the logged-in Firebase user so multiple
 * Firebase accounts on the same device get separate caches.
 */
@Entity(tableName = "ra_games", primaryKeys = ["gameId", "ownerUid"])
data class RaGameEntity(
    val gameId: Int,
    val ownerUid: String,
    val title: String,
    val imageIcon: String,
    val consoleName: String,
    val numAwarded: Int,
    val numPossibleAchievements: Int,
    val pctWon: Double,
    val highestAwardKind: String?,
    val mostRecentAwardedDate: String?,
    val cachedAt: Long = System.currentTimeMillis()
)
