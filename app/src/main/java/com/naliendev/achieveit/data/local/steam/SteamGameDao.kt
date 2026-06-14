package com.naliendev.achieveit.data.local.steam

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SteamGameDao {
    @Query("SELECT * FROM steam_games WHERE ownerUid = :uid ORDER BY playtimeMinutes DESC")
    fun getAllForUser(uid: String): Flow<List<SteamGameEntity>>

    @Query("SELECT * FROM steam_games WHERE ownerUid = :uid AND appId = :appId LIMIT 1")
    suspend fun getGame(uid: String, appId: Int): SteamGameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(games: List<SteamGameEntity>)

    @Query("DELETE FROM steam_games WHERE ownerUid = :uid")
    suspend fun clearForUser(uid: String)
}
