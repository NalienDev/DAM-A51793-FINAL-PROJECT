package com.naliendev.achieveit.data.remote.steam

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SteamClient {
    private const val BASE_URL = "https://api.steampowered.com/"

    val api: SteamApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SteamApi::class.java)
    }
}
