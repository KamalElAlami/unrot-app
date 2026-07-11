package com.focusreset.app.domain

import kotlin.math.roundToInt
import kotlin.math.sqrt

object Scoring {
    fun clarity(results: List<GameResult>): ClarityScore {
        if (results.isEmpty()) return ClarityScore(0, 0, 0, 0)
        val correct = results.sumOf { it.correct }
        val incorrect = results.sumOf { it.incorrect }
        val missed = results.sumOf { it.missed }
        val total = (correct + incorrect + missed).coerceAtLeast(1)
        val accuracy = (100.0 * correct / total).roundToInt().coerceIn(0, 100)
        val impulse = (100.0 * correct / (correct + incorrect * 1.5 + missed).coerceAtLeast(1.0)).roundToInt().coerceIn(0, 100)
        val times = results.flatMap { it.responseTimesMs }.map { it.toDouble() }
        val consistency = if (times.size < 2) accuracy else {
            val mean = times.average()
            val variance = times.sumOf { (it - mean) * (it - mean) } / times.size
            (100 - (sqrt(variance) / mean.coerceAtLeast(1.0) * 100)).roundToInt().coerceIn(0, 100)
        }
        val totalScore = (accuracy * .55 + consistency * .20 + impulse * .25).roundToInt().coerceIn(0, 100)
        return ClarityScore(totalScore, accuracy, consistency, impulse)
    }

    fun rank(entries: List<LeaderboardEntry>): List<LeaderboardEntry> = entries.sortedWith(
        compareByDescending<LeaderboardEntry> { it.score }
            .thenBy { it.mistakes }
            .thenBy { it.durationMs }
            .thenBy { it.member.uid }
    )
}
