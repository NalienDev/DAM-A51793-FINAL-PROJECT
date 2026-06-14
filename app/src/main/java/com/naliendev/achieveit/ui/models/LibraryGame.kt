package com.naliendev.achieveit.ui.models

data class LibraryGame(
    val id: String,
    val title: String,
    val platform: Platform,
    val imageUrl: String,
    val progressFraction: Float,
    val earnedTrophies: Int,
    val totalTrophies: Int,
    val lastActivity: String,
    val isPsn: Boolean
)

enum class Platform(val displayName: String) {
    RETRO_ACHIEVEMENTS("RetroAchievements"),
    STEAM("Steam"),
    PLAYSTATION("PlayStation")
}
