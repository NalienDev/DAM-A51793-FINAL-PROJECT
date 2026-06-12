package com.naliendev.achieveit.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RaGameDao {

    /** Observe all cached games for a Firebase user, ordered by most recent activity. */
    @Query("SELECT * FROM ra_games WHERE ownerUid = :uid ORDER BY mostRecentAwardedDate DESC")
    fun getAllForUser(uid: String): Flow<List<RaGameEntity>>

    /** Insert or replace all games (full refresh). */
    @Upsert
    suspend fun upsertAll(games: List<RaGameEntity>)

    /** Clear the cache for a given Firebase user (e.g. on disconnect). */
    @Query("DELETE FROM ra_games WHERE ownerUid = :uid")
    suspend fun clearForUser(uid: String)
}
