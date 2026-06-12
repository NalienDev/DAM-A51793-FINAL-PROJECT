package com.naliendev.achieveit.data.model

import com.google.gson.annotations.SerializedName

// Auth
data class PsnAuthTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Int
)

// Titles (Games)
data class PsnTitleListResponse(
    @SerializedName("trophyTitles") val trophyTitles: List<PsnTitle> = emptyList()
)

data class PsnTitle(
    @SerializedName("npServiceName") val npServiceName: String = "",
    @SerializedName("npCommunicationId") val npCommunicationId: String = "",
    @SerializedName("trophyTitleName") val trophyTitleName: String = "",
    @SerializedName("trophyTitleIconUrl") val trophyTitleIconUrl: String = "",
    @SerializedName("trophyTitlePlatform") val trophyTitlePlatform: String = "",
    @SerializedName("hasTrophyGroups") val hasTrophyGroups: Boolean = false,
    @SerializedName("definedTrophies") val definedTrophies: PsnTrophyCounts = PsnTrophyCounts(),
    @SerializedName("earnedTrophies") val earnedTrophies: PsnTrophyCounts = PsnTrophyCounts(),
    @SerializedName("progress") val progress: Int = 0,
    @SerializedName("lastUpdatedDateTime") val lastUpdatedDateTime: String = ""
)

data class PsnTrophyCounts(
    @SerializedName("bronze") val bronze: Int = 0,
    @SerializedName("silver") val silver: Int = 0,
    @SerializedName("gold") val gold: Int = 0,
    @SerializedName("platinum") val platinum: Int = 0
)

// Trophies
data class PsnUserTrophyListResponse(
    @SerializedName("trophies") val trophies: List<PsnUserTrophy> = emptyList()
)

data class PsnUserTrophy(
    @SerializedName("trophyId") val trophyId: Int = 0,
    @SerializedName("trophyHidden") val trophyHidden: Boolean = false,
    @SerializedName("trophyType") val trophyType: String = "", // "bronze", "silver", "gold", "platinum"
    @SerializedName("trophyName") val trophyName: String? = null,
    @SerializedName("trophyDetail") val trophyDetail: String? = null,
    @SerializedName("trophyIconUrl") val trophyIconUrl: String? = null,
    @SerializedName("trophyEarnedRate") val trophyEarnedRate: String? = null,
    @SerializedName("earned") val earned: Boolean = false,
    @SerializedName("earnedDateTime") val earnedDateTime: String? = null
) {
    val displayType: String
        get() = trophyType.replaceFirstChar { it.uppercase() }
}
