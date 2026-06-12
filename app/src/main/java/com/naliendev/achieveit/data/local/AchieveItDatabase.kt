package com.naliendev.achieveit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.naliendev.achieveit.data.local.psn.PsnGameDao
import com.naliendev.achieveit.data.local.psn.PsnGameEntity

@Database(entities = [RaGameEntity::class, PsnGameEntity::class], version = 3, exportSchema = false)
abstract class AchieveItDatabase : RoomDatabase() {

    abstract fun raGameDao(): RaGameDao
    abstract fun psnGameDao(): PsnGameDao

    companion object {
        @Volatile
        private var INSTANCE: AchieveItDatabase? = null

        fun getInstance(context: Context): AchieveItDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AchieveItDatabase::class.java,
                    "achieveit_cache.db"
                )
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
        }
    }
}
