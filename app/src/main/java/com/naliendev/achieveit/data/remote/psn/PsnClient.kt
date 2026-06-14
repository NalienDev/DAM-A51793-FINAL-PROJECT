package com.naliendev.achieveit.data.remote.psn

import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PsnClient {

    const val CLIENT_ID     = "09515159-7237-4370-9b40-3806e67c0891"
    const val CLIENT_SECRET = "ucPjka5tntB2KqsP"

    val BASIC_AUTH: String by lazy {
        val encoded = Base64.encodeToString(
            "$CLIENT_ID:$CLIENT_SECRET".toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP
        )
        "Basic $encoded"
    }
    // ─────────────────────────────────────────────────────────────────────

    private val authClient = OkHttpClient.Builder()
        .followRedirects(false)
        .build()

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    val api: PsnApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://m.np.playstation.com/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PsnApi::class.java)
    }

    suspend fun getAccessCode(npsso: String): String? = withContext(Dispatchers.IO) {
        val url = "https://ca.account.sony.com/api/authz/v3/oauth/authorize" +
                "?access_type=offline" +
                "&client_id=$CLIENT_ID" +
                "&response_type=code" +
                "&scope=psn:mobile.v2.core%20psn:clientapp" +
                "&redirect_uri=com.scee.psxandroid.scecompcall://redirect"

        val request = Request.Builder()
            .url(url)
            .header("Cookie", "npsso=$npsso")
            .build()

        return@withContext try {
            android.util.Log.d("AchieveIt-PSN", "Requesting access code using NPSSO...")
            val response = authClient.newCall(request).execute()
            val location = response.header("Location")
            android.util.Log.d("AchieveIt-PSN", "Sony authorization response code: ${response.code}, Location: $location")
            if (response.code == 302 && location != null) {
                val uri = android.net.Uri.parse(location)
                val code = uri.getQueryParameter("code")
                android.util.Log.d("AchieveIt-PSN", "Successfully extracted authorization code.")
                code
            } else {
                android.util.Log.e("AchieveIt-PSN", "Failed to get access code: status=${response.code}")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("AchieveIt-PSN", "Exception during Sony authorize request", e)
            null
        }
    }
}