package com.naliendev.achieveit.data.local.psn

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PsnGameDao {
    @Query("SELECT * FROM psn_games WHERE ownerUid = :uid ORDER BY lastUpdated DESC")
    fun getAllForUser(uid: String): Flow<List<PsnGameEntity>>

    @Query("SELECT * FROM psn_games WHERE ownerUid = :uid AND npCommunicationId = :gameId LIMIT 1")
    suspend fun getGame(uid: String, gameId: String): PsnGameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(games: List<PsnGameEntity>)

    @Query("DELETE FROM psn_games WHERE ownerUid = :uid")
    suspend fun clearForUser(uid: String)
}
