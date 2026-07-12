package com.focusreset.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoringTest {
    @Test fun emptyResultsProduceZeroScore() {
        assertEquals(ClarityScore(0, 0, 0, 0), Scoring.clarity(emptyList()))
    }

    @Test fun perfectConsistentResultsProducePerfectScore() {
        val result = GameResult(GameType.GHOST_GRID, 8, 0, 0, 2_400, listOf(300, 300, 300, 300))
        assertEquals(ClarityScore(100, 100, 100, 100), Scoring.clarity(listOf(result)))
    }

    @Test fun mistakesAndMissesReduceTheScore() {
        val clean = GameResult(GameType.GHOST_GRID, 8, 0, 0, 2_400, listOf(300, 300, 300))
        val noisy = GameResult(GameType.GHOST_GRID, 4, 2, 2, 3_000, listOf(200, 500, 900))
        assertTrue(Scoring.clarity(listOf(clean)).total > Scoring.clarity(listOf(noisy)).total)
    }

    @Test fun leaderboardUsesRequiredDeterministicTieBreakers() {
        fun entry(uid: String, score: Int, mistakes: Int, duration: Long) = LeaderboardEntry(
            SquadMember(uid, uid), score, mistakes, duration
        )
        val ranked = Scoring.rank(listOf(
            entry("slow", 80, 1, 5_000),
            entry("lower", 79, 0, 1_000),
            entry("fast", 80, 1, 4_000),
            entry("clean", 80, 0, 6_000)
        ))
        assertEquals(listOf("clean", "fast", "slow", "lower"), ranked.map { it.member.uid })
    }
}
