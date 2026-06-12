package com.naliendev.achieveit.data.remote.psn

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PsnClient {

    // Simple okhttp client without auth interceptor just for the redirect step
    private val authClient = OkHttpClient.Builder().followRedirects(false).build()

    // Client with logging for Retrofit
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    val api: PsnApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://m.np.playstation.net/") // Base URL doesn't matter much since endpoints have absolute URLs
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PsnApi::class.java)
    }

    /**
     * Exchanges an NPSSO token for an authorization code.
     * Hits the authorize endpoint and extracts the 'code' parameter from the redirect Location header.
     * Returns null if failed or invalid.
     */
    fun getAccessCode(npsso: String): String? {
        val url = "https://ca.account.sony.com/api/authz/v3/oauth/authorize?" +
                "access_type=offline&client_id=09515159-7237-4370-9b40-3806e67c0891" +
                "&response_type=code&scope=psn:mobile.v2.core psn:clientapp" +
                "&redirect_uri=com.scee.psxandroid.scecompati://redirect"

        val request = Request.Builder()
            .url(url)
            .header("Cookie", "npsso=$npsso")
            .build()

        return try {
            android.util.Log.d("AchieveIt-PSN", "Requesting access code using NPSSO...")
            val response = authClient.newCall(request).execute()
            val location = response.header("Location")
            android.util.Log.d("AchieveIt-PSN", "Sony authorization response code: ${response.code}, Location: $location")
            if (response.code == 302 && location != null) {
                // Extract code from redirect URL: com.scee.psxandroid.scecompati://redirect?code=xxxx
                val uri = android.net.Uri.parse(location)
                val code = uri.getQueryParameter("code")
                android.util.Log.d("AchieveIt-PSN", "Successfully extracted authorization code.")
                code
            } else {
                android.util.Log.e("AchieveIt-PSN", "Failed to get access code: status is not 302 redirect or Location is null")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("AchieveIt-PSN", "Exception during Sony authorize API request", e)
            null
        }
    }
}
