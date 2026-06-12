package com.naliendev.achieveit.data.remote.psn

import com.naliendev.achieveit.data.model.PsnAuthTokenResponse
import com.naliendev.achieveit.data.model.PsnTitleListResponse
import com.naliendev.achieveit.data.model.PsnUserTrophyListResponse
import retrofit2.http.*

interface PsnApi {

    @FormUrlEncoded
    @POST("https://ca.account.sony.com/api/authz/v3/oauth/token")
    suspend fun getAccessToken(
        @Header("Authorization") auth: String,
        @Field("code") code: String?,
        @Field("refresh_token") refreshToken: String?,
        @Field("grant_type") grantType: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("token_format") tokenFormat: String = "jwt",
        @Field("scope") scope: String = "psn:mobile.v2.core psn:clientapp"
    ): PsnAuthTokenResponse

    @GET("https://m.np.playstation.com/api/trophy/v1/users/{accountId}/trophyTitles")
    suspend fun getUserTrophyTitles(
        @Header("Authorization") authorization: String,
        @Path("accountId") accountId: String = "me",
        @Query("limit") limit: Int = 200,
        @Query("offset") offset: Int = 0
    ): PsnTitleListResponse

    @GET("https://m.np.playstation.com/api/trophy/v1/users/{accountId}/npCommunicationIds/{npCommunicationId}/trophyGroups/all/trophies")
    suspend fun getUserTrophies(
        @Header("Authorization") authorization: String,
        @Path("accountId") accountId: String = "me",
        @Path("npCommunicationId") npCommunicationId: String,
        @Query("npServiceName") npServiceName: String = "trophy"
    ): PsnUserTrophyListResponse

    @GET("https://m.np.playstation.com/api/trophy/v1/npCommunicationIds/{npCommunicationId}/trophyGroups/all/trophies")
    suspend fun getTrophyMetadata(
        @Header("Authorization") authorization: String,
        @Path("npCommunicationId") npCommunicationId: String,
        @Query("npServiceName") npServiceName: String = "trophy"
    ): PsnUserTrophyListResponse
}