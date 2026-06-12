package com.naliendev.achieveit.ui.models

data class UnifiedGameDetail(
    val title: String,
    val description: String,
    val imageUrl: String,
    val earnedTrophies: Int,
    val totalTrophies: Int,
    val trophies: List<UnifiedTrophy>
)

data class UnifiedTrophy(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val isEarned: Boolean,
    val earnedDate: String?,
    val type: String // "Achievement", "Bronze", "Silver", "Gold", "Platinum"
)
