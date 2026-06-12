package com.naliendev.achieveit.ui.models

data class LibraryGame(
    val id: String, // gameId for RA, npCommunicationId for PSN
    val title: String,
    val platform: Platform, // e.g. "PS5", "PS4", "RetroAchievements", or Console Name
    val imageUrl: String,
    val progressFraction: Float,
    val earnedTrophies: Int,
    val totalTrophies: Int,
    val lastActivity: String, // for sorting
    val isPsn: Boolean
)

enum class Platform(val displayName: String) {
    RETRO_ACHIEVEMENTS("RetroAchievements"),
    PLAYSTATION("PlayStation")
}
