package com.naliendev.achieveit.data.model

data class UserProfile(
    val id: String,
    val username: String,
    val avatarImage: String,
    val linkedAccounts: List<String>,
    val totalTrophies: Int,
    val totalGames: Int = 124,         // Mocks for Home Screen
    val achievements: Int = 1452,      // Mocks for Home Screen
    val completion: Float = 68f        // Mocks for Home Screen
)

data class Game(
    val id: String,
    val title: String,
    val platformSource: String,
    val coverImage: String,
    val completionPercentage: Float,
    val releaseYear: Int = 2024,
    val genre: String = "Action-Adventure",
    val description: String = "Embark on an odyssey through the neon-drenched corridors...",
    val totalTrophies: Int = 62,
    val earnedTrophies: Int = 42,
    val playTimeHours: Int = 0,
    val lastPlayed: String = "2h ago"
)

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val unlockDate: String?,
    val rarityPoints: Int,
    val rarityColor: String // "Gold", "Silver", "Bronze"
)
