package com.focusreset.app.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSeedsTest {
    @Test fun dailySeedAndGameOrderAreDeterministic() {
        val date = LocalDate.of(2026, 7, 12)
        assertEquals(GameSeeds.daily(date), GameSeeds.daily(date))
        assertNotEquals(GameSeeds.daily(date).seed, GameSeeds.daily(date.plusDays(1)).seed)
    }

    @Test fun dailyMixContainsThreeDifferentBraincupGames() {
        val games = GameSeeds.daily(LocalDate.of(2026, 7, 12)).games
        assertEquals(3, games.size)
        assertEquals(3, games.distinct().size)
    }

    @Test fun ghostGridUsesBraincupProgressionAndUniqueTiles() {
        val easy = BraincupGameEngines.ghostGrid(42L, round = 1)
        val harder = BraincupGameEngines.ghostGrid(42L, round = 6)
        assertEquals(4, easy.sequence.size)
        assertEquals(easy.sequence.size, easy.sequence.distinct().size)
        assertEquals(9, harder.sequence.size)
        assertEquals(5, harder.gridSize)
        assertEquals(450L, harder.flashDurationMs)
    }

    @Test fun flashCrowdIsDeterministicAndHasOneLargerSide() {
        val round = BraincupGameEngines.flashCrowd(42L, round = 4)
        assertEquals(round, BraincupGameEngines.flashCrowd(42L, round = 4))
        assertNotEquals(round.leftCount, round.rightCount)
        assertTrue(round.leftDots.isNotEmpty() && round.rightDots.isNotEmpty())
    }

    @Test fun pathFinderNeverLeavesItsGrid() {
        repeat(100) { seed ->
            val round = BraincupGameEngines.pathFinder(seed.toLong(), round = 3)
            assertTrue(round.startIndex in 0 until round.gridSize)
            assertTrue(round.destination in 0 until round.gridSize * round.gridSize)
            assertEquals(6, round.directions.size)
        }
    }

    @Test fun schulteContainsEveryNumberExactlyOnce() {
        assertEquals((1..16).toSet(), BraincupGameEngines.schulteTable(42L).toSet())
    }

    @Test fun digitMemoryPreservesLeadingZerosAndValidMath() {
        repeat(100) { seed ->
            val round = BraincupGameEngines.digitMemory(seed.toLong(), round = 2)
            assertEquals(5, round.sequence.length)
            assertTrue(round.sequence.all(Char::isDigit))
            val parts = round.problem.split(" ")
            val a = parts[0].toInt()
            val b = parts[2].toInt()
            val expected = when (parts[1]) { "+" -> a + b; "−" -> a - b; else -> a * b }
            assertEquals(expected.toString(), round.answer)
        }
    }
}
