package com.naliendev.achieveit.data.model

import com.google.gson.annotations.SerializedName

/**
 * Top-level response from API_GetUserCompletionProgress.php
 */
data class RaCompletionProgressResponse(
    @SerializedName("Count") val count: Int = 0,
    @SerializedName("Total") val total: Int = 0,
    @SerializedName("Results") val results: List<RaGame> = emptyList()
)

/**
 * A single game entry from the user's completion progress list.
 */
data class RaGame(
    @SerializedName("GameID") val gameId: Int = 0,
    @SerializedName("Title") val title: String = "",
    @SerializedName("ImageIcon") val imageIcon: String = "",
    @SerializedName("ConsoleName") val consoleName: String = "",
    @SerializedName("NumAwarded") val numAwarded: Int = 0,
    @SerializedName("NumPossibleAchievements") val numPossibleAchievements: Int = 0,
    @SerializedName("PctWon") val pctWon: Double = 0.0,
    @SerializedName("ScoreAwarded") val scoreAwarded: Int = 0,
    @SerializedName("MaxPossible") val maxPossible: Int = 0,
    @SerializedName("HighestAwardKind") val highestAwardKind: String? = null,
    @SerializedName("MostRecentAwardedDate") val mostRecentAwardedDate: String? = null
) {
    /** URL to the game's box art / icon on RA CDN */
    val imageIconUrl: String
        get() = "https://media.retroachievements.org$imageIcon"

    /** Progress as a float 0..1 */
    val progressFraction: Float
        get() = if (numPossibleAchievements > 0)
            numAwarded.toFloat() / numPossibleAchievements.toFloat()
        else 0f

    /** Whether the user has mastered (100%) this game */
    val isMastered: Boolean
        get() = numPossibleAchievements > 0 && numAwarded >= numPossibleAchievements
}

/**
 * Response from API_GetGameInfoAndUserProgress.php
 * Contains game metadata + a map of achievements keyed by achievement ID.
 */
data class RaGameDetailResponse(
    @SerializedName("ID") val id: Int = 0,
    @SerializedName("Title") val title: String = "",
    @SerializedName("ConsoleID") val consoleId: Int = 0,
    @SerializedName("ConsoleName") val consoleName: String = "",
    @SerializedName("ImageIcon") val imageIcon: String = "",
    @SerializedName("ImageTitle") val imageTitle: String = "",
    @SerializedName("ImageIngame") val imageIngame: String = "",
    @SerializedName("ImageBoxArt") val imageBoxArt: String = "",
    @SerializedName("Publisher") val publisher: String = "",
    @SerializedName("Developer") val developer: String = "",
    @SerializedName("Genre") val genre: String = "",
    @SerializedName("Released") val released: String? = null,
    @SerializedName("NumDistinctPlayers") val numDistinctPlayers: Int = 0,
    @SerializedName("NumAchievements") val numAchievements: Int = 0,
    @SerializedName("Achievements") val achievements: Map<String, RaAchievement> = emptyMap(),
    @SerializedName("NumAwardedToUser") val numAwardedToUser: Int = 0,
    @SerializedName("NumAwardedToUserHardcore") val numAwardedToUserHardcore: Int = 0,
    @SerializedName("UserCompletion") val userCompletion: String? = null,
    @SerializedName("UserCompletionHardcore") val userCompletionHardcore: String? = null
) {
    val imageIconUrl: String
        get() = "https://media.retroachievements.org$imageIcon"

    val imageBoxArtUrl: String
        get() = "https://media.retroachievements.org$imageBoxArt"

    val imageTitleUrl: String
        get() = "https://media.retroachievements.org$imageTitle"

    val progressFraction: Float
        get() = if (numAchievements > 0) numAwardedToUser.toFloat() / numAchievements.toFloat() else 0f

    /** Sorted achievement list: earned first (by date), then locked */
    val sortedAchievements: List<RaAchievement>
        get() = achievements.values.sortedWith(
            compareByDescending<RaAchievement> { it.dateEarned != null }
                .thenByDescending { it.dateEarned }
                .thenBy { it.displayOrder }
        )
}

/**
 * A single achievement within a game.
 */
data class RaAchievement(
    @SerializedName("ID") val id: Int = 0,
    @SerializedName("NumAwarded") val numAwarded: Int = 0,
    @SerializedName("NumAwardedHardcore") val numAwardedHardcore: Int = 0,
    @SerializedName("Title") val title: String = "",
    @SerializedName("Description") val description: String = "",
    @SerializedName("Points") val points: Int = 0,
    @SerializedName("TrueRatio") val trueRatio: Int = 0,
    @SerializedName("Author") val author: String = "",
    @SerializedName("DateModified") val dateModified: String? = null,
    @SerializedName("DateCreated") val dateCreated: String? = null,
    @SerializedName("BadgeName") val badgeName: String = "",
    @SerializedName("DisplayOrder") val displayOrder: Int = 0,
    @SerializedName("type") val type: String? = null,
    @SerializedName("DateEarned") val dateEarned: String? = null,
    @SerializedName("DateEarnedHardcore") val dateEarnedHardcore: String? = null
) {
    /** URL to the achievement badge image */
    val badgeUrl: String
        get() = "https://media.retroachievements.org/Badge/$badgeName.png"

    /** URL to the locked version of the badge */
    val badgeLockedUrl: String
        get() = "https://media.retroachievements.org/Badge/${badgeName}_lock.png"

    /** Whether the user has earned this achievement */
    val isEarned: Boolean
        get() = dateEarned != null

    /** Rarity percentage: how many distinct players have earned it */
    val rarityLabel: String
        get() = if (numAwarded > 0) "${numAwarded} unlocks" else "No unlocks"
}
