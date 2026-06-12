package com.naliendev.achieveit.data.remote

import com.naliendev.achieveit.data.model.RaCompletionProgressResponse
import com.naliendev.achieveit.data.model.RaGameDetailResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface RetroAchievementsApi {

    /**
     * Returns all games the user has ever played, with completion progress.
     *
     * Docs: https://api-docs.retroachievements.org/v1/get-user-completion-progress.html
     *
     * @param callerUsername  Your RA username (auth)
     * @param apiKey          Your RA web API key (auth)
     * @param targetUsername  The RA username whose data to fetch
     * @param count           Max results per page (max 500)
     * @param offset          Pagination offset
     */
    @GET("API_GetUserCompletionProgress.php")
    suspend fun getUserCompletionProgress(
        @Query("z") callerUsername: String,
        @Query("y") apiKey: String,
        @Query("u") targetUsername: String,
        @Query("c") count: Int = 500,
        @Query("o") offset: Int = 0
    ): RaCompletionProgressResponse

    /**
     * Returns extended game info along with a user's achievement progress for that game.
     *
     * Docs: https://api-docs.retroachievements.org/v1/get-game-info-and-user-progress.html
     *
     * @param apiKey          Your RA web API key (auth)
     * @param targetUsername  The RA username whose progress to fetch
     * @param gameId          The target game ID
     */
    @GET("API_GetGameInfoAndUserProgress.php")
    suspend fun getGameInfoAndUserProgress(
        @Query("z") callerUsername: String,
        @Query("y") apiKey: String,
        @Query("u") targetUsername: String,
        @Query("g") gameId: Int
    ): RaGameDetailResponse
}
