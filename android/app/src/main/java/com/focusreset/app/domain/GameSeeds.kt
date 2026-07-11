package com.focusreset.app.domain

import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.random.Random

object GameSeeds {
    fun daily(date: LocalDate = LocalDate.now(ZoneOffset.UTC)): DailyGameSeed {
        val seed = date.toEpochDay() * 1_000_003L + 41L
        val games = GameType.entries.shuffled(Random(seed)).take(3)
        return DailyGameSeed(date, seed, games)
    }

    fun colorRound(seed: Long, index: Int): Pair<String, Int> {
        val words = listOf("RED", "BLUE", "GREEN", "AMBER")
        val random = Random(seed + index * 97L)
        return words[random.nextInt(words.size)] to random.nextInt(words.size)
    }

    fun memoryCells(seed: Long, size: Int = 4, count: Int = 5): Set<Int> =
        (0 until size * size).shuffled(Random(seed)).take(count).toSet()
}
