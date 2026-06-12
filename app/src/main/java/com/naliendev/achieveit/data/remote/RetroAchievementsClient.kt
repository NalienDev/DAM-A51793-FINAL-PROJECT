package com.naliendev.achieveit.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetroAchievementsClient {

    private const val BASE_URL = "https://retroachievements.org/API/"

    val api: RetroAchievementsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetroAchievementsApi::class.java)
    }
}
