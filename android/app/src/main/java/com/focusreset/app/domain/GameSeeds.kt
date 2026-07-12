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

    fun ghostSequence(seed: Long, length: Int = 5, cells: Int = 9): List<Int> {
        val random = Random(seed + 2_003L)
        return List(length) { random.nextInt(cells) }
    }

    fun flashCrowd(seed: Long, round: Int): Pair<Int, Int> {
        val random = Random(seed + round * 211L + 4_009L)
        val left = random.nextInt(3, 10)
        var right = random.nextInt(3, 10)
        if (right == left) right = if (right == 9) 8 else right + 1
        return left to right
    }

    fun pathMoves(seed: Long, count: Int = 4): List<Int> {
        val random = Random(seed + 6_013L)
        var row = 2
        var col = 2
        return buildList {
            repeat(count) {
                val valid = listOf(0, 1, 2, 3).filter { direction ->
                    when (direction) {
                        0 -> row > 0
                        1 -> col < 4
                        2 -> row < 4
                        else -> col > 0
                    }
                }
                val direction = valid[random.nextInt(valid.size)]
                add(direction)
                when (direction) { 0 -> row--; 1 -> col++; 2 -> row++; else -> col-- }
            }
        }
    }

    fun pathDestination(moves: List<Int>): Int {
        var row = 2
        var col = 2
        moves.forEach { when (it) { 0 -> row--; 1 -> col++; 2 -> row++; else -> col-- } }
        return row * 5 + col
    }
}
