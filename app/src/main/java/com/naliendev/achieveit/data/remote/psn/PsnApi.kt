package com.naliendev.achieveit.data.remote.psn

import com.naliendev.achieveit.data.model.PsnAuthTokenResponse
import com.naliendev.achieveit.data.model.PsnTitleListResponse
import com.naliendev.achieveit.data.model.PsnUserTrophyListResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PsnApi {

    // Auth Token Exchange (using basic auth header for client)
    @FormUrlEncoded
    @POST("https://ca.account.sony.com/api/authz/v3/oauth/token")
    suspend fun getAccessToken(
        @Header("Authorization") auth: String = "Basic MDk1MTUxNTktNzIzNy00MzcwLTliNDAtMzgwNmU2N2MwODkxOnVjSWRqaVI2OWRkMWZzckE=",
        @Field("code") code: String? = null,
        @Field("refresh_token") refreshToken: String? = null,
        @Field("grant_type") grantType: String, // "authorization_code" or "refresh_token"
        @Field("redirect_uri") redirectUri: String = "com.scee.psxandroid.scecompati://redirect",
        @Field("token_format") tokenFormat: String = "jwt",
        @Field("scope") scope: String = "psn:mobile.v2.core psn:clientapp"
    ): PsnAuthTokenResponse

    // Game List
    @GET("https://m.np.playstation.net/api/trophy/v1/users/{accountId}/trophyTitles")
    suspend fun getUserTrophyTitles(
        @Header("Authorization") authorization: String,
        @Path("accountId") accountId: String = "me",
        @Query("limit") limit: Int = 200,
        @Query("offset") offset: Int = 0
    ): PsnTitleListResponse

    // Trophies for a game
    @GET("https://m.np.playstation.net/api/trophy/v1/users/{accountId}/npCommunicationIds/{npCommunicationId}/trophyGroups/all/trophies")
    suspend fun getUserTrophies(
        @Header("Authorization") authorization: String,
        @Path("accountId") accountId: String = "me",
        @Path("npCommunicationId") npCommunicationId: String,
        @Query("npServiceName") npServiceName: String = "trophy" // usually "trophy"
    ): PsnUserTrophyListResponse
}
