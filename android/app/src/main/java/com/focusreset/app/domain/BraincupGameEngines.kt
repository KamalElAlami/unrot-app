/*
 * Adapted from Braincup by Simon Schubert.
 * Original project: https://github.com/SimonSchubert/Braincup
 * Licensed under the Apache License, Version 2.0.
 * Modified for deterministic Focus Reset sessions, scoring, and native Android UI.
 */
package com.focusreset.app.domain

import kotlin.math.sqrt
import kotlin.random.Random

object BraincupGameEngines {
    data class GhostGridRound(val gridSize: Int, val sequence: List<Int>, val flashDurationMs: Long)
    data class Dot(val x: Float, val y: Float, val radius: Float)
    data class FlashCrowdRound(
        val leftCount: Int,
        val rightCount: Int,
        val leftDots: List<Dot>,
        val rightDots: List<Dot>
    )
    data class PathRound(val gridSize: Int, val startIndex: Int, val directions: List<Int>, val destination: Int)
    data class DigitRound(val sequence: String, val problem: String, val answer: String)

    fun ghostGrid(seed: Long, round: Int = 1): GhostGridRound {
        val length = 3 + round
        val gridSize = if (length >= 7) 5 else 4
        val random = Random(seed + round * 101L)
        return GhostGridRound(
            gridSize = gridSize,
            sequence = (0 until gridSize * gridSize).shuffled(random).take(length),
            flashDurationMs = if (length >= 9) 450L else 600L
        )
    }

    fun flashCrowd(seed: Long, round: Int): FlashCrowdRound {
        val random = Random(seed + round * 307L)
        val moreOnLeft = random.nextBoolean()
        val moreCount = random.nextInt(15, 26)
        val ratio = when {
            round <= 1 -> 1.0 / 2.0
            round <= 3 -> 2.0 / 3.0
            round <= 5 -> 3.0 / 4.0
            else -> 4.0 / 5.0
        }
        val fewerCount = (moreCount * ratio).toInt().coerceAtLeast(1)
        val leftCount = if (moreOnLeft) moreCount else fewerCount
        val rightCount = if (moreOnLeft) fewerCount else moreCount
        return FlashCrowdRound(
            leftCount,
            rightCount,
            generateDots(leftCount, Random(seed + round * 307L + 1L)),
            generateDots(rightCount, Random(seed + round * 307L + 2L))
        )
    }

    fun pathFinder(seed: Long, round: Int = 1): PathRound {
        val gridSize = 4
        val random = Random(seed + round * 509L)
        var x = random.nextInt(gridSize)
        var y = 0
        val start = x
        var last = 0
        val directions = buildList {
            while (size < 3 + round) {
                val direction = random.nextInt(4)
                val valid = when (direction) {
                    0 -> y > 0
                    1 -> x < gridSize - 1
                    2 -> y < gridSize - 1
                    else -> x > 0
                }
                if (valid && direction != last) {
                    add(direction)
                    last = direction
                    when (direction) { 0 -> y--; 1 -> x++; 2 -> y++; else -> x-- }
                }
            }
        }
        return PathRound(gridSize, start, directions, y * gridSize + x)
    }

    fun schulteTable(seed: Long, size: Int = 4): List<Int> =
        (1..size * size).shuffled(Random(seed + 701L))

    fun digitMemory(seed: Long, round: Int = 1): DigitRound {
        val random = Random(seed + round * 907L)
        val sequence = buildString { repeat(round + 3) { append(random.nextInt(10)) } }
        val useMultiply = round >= 4 && random.nextBoolean()
        val problem: String
        val answer: Int
        if (useMultiply) {
            val a = random.nextInt(2, 6 + round / 2)
            val b = random.nextInt(2, 6)
            problem = "$a × $b"
            answer = a * b
        } else if (random.nextBoolean()) {
            val a = random.nextInt(2, 9 + round * 2)
            val b = random.nextInt(2, 9 + round * 2)
            problem = "$a + $b"
            answer = a + b
        } else {
            val a = random.nextInt(5, 14 + round * 2)
            val b = random.nextInt(2, a)
            problem = "$a − $b"
            answer = a - b
        }
        return DigitRound(sequence, problem, answer.toString())
    }

    private fun generateDots(count: Int, random: Random): List<Dot> {
        val dots = mutableListOf<Dot>()
        var attempts = 0
        while (dots.size < count && attempts < count * 50) {
            val radius = random.nextFloat() * .035f + .02f
            val x = radius + random.nextFloat() * (1f - 2 * radius)
            val y = radius + random.nextFloat() * (1f - 2 * radius)
            val overlaps = dots.any { existing ->
                val dx = existing.x - x
                val dy = existing.y - y
                sqrt(dx * dx + dy * dy) < (existing.radius + radius) * .8f
            }
            if (!overlaps) dots += Dot(x, y, radius)
            attempts++
        }
        return dots
    }
}
