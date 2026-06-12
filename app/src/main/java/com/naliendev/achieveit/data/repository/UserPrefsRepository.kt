package com.naliendev.achieveit.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class RaCredentials(val username: String, val apiKey: String)

/**
 * Reads and writes the user's RetroAchievements credentials
 * to Firebase Realtime Database under /users/{uid}/integrations/retroachievements/
 */
class UserPrefsRepository {

    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    private val uid: String?
        get() = auth.currentUser?.uid

    /** Observe RA credentials as a Flow. Emits null if not set. */
    fun raCredentialsFlow(): Flow<RaCredentials?> = callbackFlow {
        val uid = uid ?: run { trySend(null); close(); return@callbackFlow }
        val ref = db.child("users").child(uid).child("integrations").child("retroachievements")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.child("username").getValue(String::class.java)
                val apiKey = snapshot.child("apiKey").getValue(String::class.java)
                trySend(
                    if (!username.isNullOrBlank() && !apiKey.isNullOrBlank())
                        RaCredentials(username, apiKey)
                    else null
                )
            }
            override fun onCancelled(error: DatabaseError) { trySend(null) }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    /** Save RA credentials to Firebase. */
    suspend fun saveRaCredentials(username: String, apiKey: String) {
        val uid = uid ?: return
        db.child("users").child(uid).child("integrations").child("retroachievements")
            .updateChildren(mapOf("username" to username, "apiKey" to apiKey))
            .await()
    }

    /** Remove RA credentials from Firebase. */
    suspend fun clearRaCredentials() {
        val uid = uid ?: return
        db.child("users").child(uid).child("integrations").child("retroachievements")
            .removeValue()
            .await()
    }

    /** Observe PSN credentials as a Flow. Emits null if not set. */
    fun psnCredentialsFlow(): Flow<PsnCredentials?> = callbackFlow {
        val uid = uid ?: run { trySend(null); close(); return@callbackFlow }
        val ref = db.child("users").child(uid).child("integrations").child("playstation")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val npsso = snapshot.child("npsso").getValue(String::class.java)
                val accessToken = snapshot.child("accessToken").getValue(String::class.java)
                val refreshToken = snapshot.child("refreshToken").getValue(String::class.java)
                trySend(
                    if (!npsso.isNullOrBlank())
                        PsnCredentials(npsso, accessToken, refreshToken)
                    else null
                )
            }
            override fun onCancelled(error: DatabaseError) { trySend(null) }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    /** Save PSN npsso token to Firebase. */
    suspend fun savePsnNpsso(npsso: String) {
        val uid = uid ?: return
        db.child("users").child(uid).child("integrations").child("playstation")
            .setValue(mapOf(
                "npsso" to npsso,
                "accessToken" to null,
                "refreshToken" to null
            ))
            .await()
    }

    /** Update PSN OAuth tokens in Firebase. */
    suspend fun updatePsnTokens(accessToken: String, refreshToken: String) {
        val uid = uid ?: return
        db.child("users").child(uid).child("integrations").child("playstation")
            .updateChildren(mapOf("accessToken" to accessToken, "refreshToken" to refreshToken))
            .await()
    }

    /** Remove PSN credentials from Firebase. */
    suspend fun clearPsnCredentials() {
        val uid = uid ?: return
        db.child("users").child(uid).child("integrations").child("playstation")
            .removeValue()
            .await()
    }
}

data class PsnCredentials(
    val npsso: String,
    val accessToken: String? = null,
    val refreshToken: String? = null
)
