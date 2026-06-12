package com.naliendev.achieveit.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.naliendev.achieveit.data.local.psn.PsnGameDao
import com.naliendev.achieveit.data.local.psn.PsnGameEntity
import com.naliendev.achieveit.data.model.PsnTitle
import com.naliendev.achieveit.data.model.PsnUserTrophyListResponse
import com.naliendev.achieveit.data.remote.psn.PsnClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "AchieveIt-PSN"

class PsnRepository(
    private val dao: PsnGameDao,
    private val prefsRepo: UserPrefsRepository = UserPrefsRepository()
) {

    private val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /** Observe cached games from Room. */
    fun cachedGamesFlow(): Flow<List<PsnGameEntity>> = dao.getAllForUser(uid)

    /** Clear local cache. */
    suspend fun clearCache() = dao.clearForUser(uid)

    /**
     * Refreshes the token if needed, or exchanges NPSSO for a new token.
     * Then fetches PSN titles and updates the Room cache.
     */
    suspend fun refreshFromNetwork(): PsnCredentials? {
        val creds = ensureValidToken() ?: run {
            Log.e(TAG, "refreshFromNetwork: no credentials or token exchange failed.")
            return null
        }

        Log.d(TAG, "Fetching PSN trophy titles...")
        return try {
            val response = PsnClient.api.getUserTrophyTitles(
                authorization = "Bearer ${creds.accessToken}",
                limit = 200
            )
            Log.d(TAG, "PSN titles fetched: ${response.trophyTitles.size} titles.")
            val entities = response.trophyTitles.map { it.toEntity(uid) }
            dao.upsertAll(entities)
            creds
        } catch (e: HttpException) {
            if (e.code() == 401) {
                Log.w(TAG, "Token expired (401) during title fetch. Attempting refresh...")
                val refreshedCreds = forceTokenRefresh(creds) ?: return null
                // Retry once after refresh
                val response = PsnClient.api.getUserTrophyTitles(
                    authorization = "Bearer ${refreshedCreds.accessToken}",
                    limit = 200
                )
                Log.d(TAG, "PSN titles fetched after refresh: ${response.trophyTitles.size} titles.")
                val entities = response.trophyTitles.map { it.toEntity(uid) }
                dao.upsertAll(entities)
                refreshedCreds
            } else {
                Log.e(TAG, "HTTP error fetching trophy titles: ${e.code()} ${e.message()}")
                throw e
            }
        }
    }

    /**
     * Fetches trophies for a specific game (npCommunicationId) and merges the progress
     * with the static trophy metadata (names, details, icon URLs).
     */
    suspend fun getGameTrophies(npCommunicationId: String, npServiceName: String = "trophy"): PsnUserTrophyListResponse? {
        val creds = ensureValidToken() ?: return null

        Log.d(TAG, "Fetching trophies for $npCommunicationId with service=$npServiceName")
        return try {
            val userTrophies = PsnClient.api.getUserTrophies(
                authorization = "Bearer ${creds.accessToken}",
                npCommunicationId = npCommunicationId,
                npServiceName = npServiceName
            )
            val metadata = PsnClient.api.getTrophyMetadata(
                authorization = "Bearer ${creds.accessToken}",
                npCommunicationId = npCommunicationId,
                npServiceName = npServiceName
            )
            mergeTrophyData(userTrophies, metadata)
        } catch (e: HttpException) {
            if (e.code() == 401) {
                Log.w(TAG, "Token expired (401) during trophy fetch. Attempting refresh...")
                val refreshedCreds = forceTokenRefresh(creds) ?: return null
                val userTrophies = PsnClient.api.getUserTrophies(
                    authorization = "Bearer ${refreshedCreds.accessToken}",
                    npCommunicationId = npCommunicationId,
                    npServiceName = npServiceName
                )
                val metadata = PsnClient.api.getTrophyMetadata(
                    authorization = "Bearer ${refreshedCreds.accessToken}",
                    npCommunicationId = npCommunicationId,
                    npServiceName = npServiceName
                )
                mergeTrophyData(userTrophies, metadata)
            } else {
                Log.e(TAG, "HTTP error fetching trophies: ${e.code()} ${e.message()}")
                throw e
            }
        }
    }

    private fun mergeTrophyData(
        userTrophies: PsnUserTrophyListResponse,
        metadata: PsnUserTrophyListResponse
    ): PsnUserTrophyListResponse {
        val mergedList = userTrophies.trophies.map { userTrophy ->
            val meta = metadata.trophies.firstOrNull { it.trophyId == userTrophy.trophyId }
            userTrophy.copy(
                trophyName = meta?.trophyName ?: userTrophy.trophyName,
                trophyDetail = meta?.trophyDetail ?: userTrophy.trophyDetail,
                trophyIconUrl = meta?.trophyIconUrl ?: userTrophy.trophyIconUrl
            )
        }
        return PsnUserTrophyListResponse(trophies = mergedList)
    }

    /**
     * Ensures we have a valid access token.
     * - If accessToken exists, return immediately (assume valid).
     * - If refreshToken exists, try to use it to get a new access token.
     * - If neither, exchange the npsso for a new pair.
     */
    private suspend fun ensureValidToken(): PsnCredentials? {
        val creds = prefsRepo.psnCredentialsFlow().firstOrNull() ?: run {
            Log.e(TAG, "ensureValidToken: no PSN credentials found in prefs.")
            return null
        }

        if (!creds.accessToken.isNullOrBlank()) {
            Log.d(TAG, "Using existing access token.")
            return creds
        }

        if (!creds.refreshToken.isNullOrBlank()) {
            Log.d(TAG, "Access token missing, trying refresh token...")
            return refreshWithToken(creds)
        }

        Log.d(TAG, "No tokens found, exchanging NPSSO for auth code...")
        return exchangeNpssoForTokens(creds)
    }

    /** Obtain new tokens using the refresh_token grant. */
    private suspend fun refreshWithToken(creds: PsnCredentials): PsnCredentials? {
        return try {
            val tokenResponse = PsnClient.api.getAccessToken(
                auth         = PsnClient.BASIC_AUTH,
                grantType    = "refresh_token",
                refreshToken = creds.refreshToken,
                code         = null,
                redirectUri  = "com.scee.psxandroid.scecompcall://redirect"
            )
            Log.d(TAG, "Token refreshed successfully.")
            prefsRepo.updatePsnTokens(tokenResponse.accessToken, tokenResponse.refreshToken)
            creds.copy(accessToken = tokenResponse.accessToken, refreshToken = tokenResponse.refreshToken)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to refresh token, will try re-exchanging NPSSO.", e)
            exchangeNpssoForTokens(creds)
        }
    }


    /** Force a refresh by trying refresh_token first, then full NPSSO re-exchange. */
    private suspend fun forceTokenRefresh(creds: PsnCredentials): PsnCredentials? {
        if (!creds.refreshToken.isNullOrBlank()) {
            Log.d(TAG, "Force refresh: using refresh_token...")
            val refreshed = refreshWithToken(creds)
            if (refreshed != null) return refreshed
        }
        Log.d(TAG, "Force refresh: falling back to NPSSO exchange...")
        return exchangeNpssoForTokens(creds)
    }

    /** Full NPSSO → code → token exchange. Clears stored tokens first. */
    private suspend fun exchangeNpssoForTokens(creds: PsnCredentials): PsnCredentials? {
        Log.d(TAG, "Exchanging NPSSO for auth code...")
        val code = PsnClient.getAccessCode(creds.npsso) ?: run {
            Log.e(TAG, "NPSSO exchange failed: could not get authorization code.")
            return null
        }

        Log.d(TAG, "Got auth code, exchanging for access token...")
        return try {
            val tokenResponse = PsnClient.api.getAccessToken(
                auth        = PsnClient.BASIC_AUTH,
                grantType   = "authorization_code",
                code        = code,
                refreshToken = null,
                redirectUri = "com.scee.psxandroid.scecompcall://redirect"
            )
            Log.d(TAG, "Token exchange successful.")
            prefsRepo.updatePsnTokens(tokenResponse.accessToken, tokenResponse.refreshToken)
            creds.copy(accessToken = tokenResponse.accessToken, refreshToken = tokenResponse.refreshToken)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to exchange auth code for token.", e)
            null
        }
    }
}

// Map remote PSN Title to Room Entity
fun PsnTitle.toEntity(ownerUid: String) = PsnGameEntity(
    npCommunicationId = npCommunicationId,
    ownerUid = ownerUid,
    title = trophyTitleName,
    imageIcon = trophyTitleIconUrl,
    platform = trophyTitlePlatform,
    npServiceName = npServiceName,
    bronze = definedTrophies.bronze,
    silver = definedTrophies.silver,
    gold = definedTrophies.gold,
    platinum = definedTrophies.platinum,
    earnedBronze = earnedTrophies.bronze,
    earnedSilver = earnedTrophies.silver,
    earnedGold = earnedTrophies.gold,
    earnedPlatinum = earnedTrophies.platinum,
    progress = progress,
    lastUpdated = lastUpdatedDateTime.ifBlank {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
    }
)
