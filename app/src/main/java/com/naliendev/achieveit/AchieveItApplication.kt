package com.naliendev.achieveit

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class AchieveItApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
