package com.focusreset.app.data

import com.focusreset.app.domain.LeaderboardEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class SquadSnapshot(val id: String, val name: String, val inviteCode: String, val leaderboard: List<Map<String, Any?>>)

interface SquadRepository {
    suspend fun create(name: String): SquadSnapshot
    suspend fun join(inviteCode: String): SquadSnapshot
    suspend fun submitScore(squadId: String, runId: String, score: Int, mistakes: Int, durationMs: Long, seed: Long)
    suspend fun react(squadId: String, runId: String, emoji: String)
}

class FirebaseSquadRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : SquadRepository {
    private fun uid() = requireNotNull(auth.currentUser?.uid) { "Google sign-in is required for squads." }

    override suspend fun create(name: String): SquadSnapshot {
        val ref = db.collection("squads").document()
        val code = ref.id.takeLast(8).uppercase()
        ref.set(mapOf("name" to name.trim().take(40), "inviteCode" to code, "ownerUid" to uid(), "memberUids" to listOf(uid()), "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp())).await()
        return SquadSnapshot(ref.id, name, code, emptyList())
    }

    override suspend fun join(inviteCode: String): SquadSnapshot {
        val result = db.collection("squads").whereEqualTo("inviteCode", inviteCode.uppercase()).limit(1).get().await()
        val doc = requireNotNull(result.documents.firstOrNull()) { "Squad not found." }
        doc.reference.update("memberUids", com.google.firebase.firestore.FieldValue.arrayUnion(uid())).await()
        return SquadSnapshot(doc.id, doc.getString("name") ?: "Squad", inviteCode.uppercase(), emptyList())
    }

    override suspend fun submitScore(squadId: String, runId: String, score: Int, mistakes: Int, durationMs: Long, seed: Long) {
        require(score in 0..100 && mistakes >= 0 && durationMs in 1_000..600_000)
        db.collection("scoreSubmissions").document(runId).set(mapOf("uid" to uid(), "squadId" to squadId, "score" to score, "mistakes" to mistakes, "durationMs" to durationMs, "seed" to seed, "status" to "pending", "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp())).await()
    }

    override suspend fun react(squadId: String, runId: String, emoji: String) {
        require(emoji in setOf("👏", "🧠", "🔥", "💚"))
        db.collection("squads").document(squadId).collection("reactions").document("${runId}_${uid()}").set(mapOf("uid" to uid(), "runId" to runId, "emoji" to emoji)).await()
    }
}
