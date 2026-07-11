package com.focusreset.app.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSeedsTest {
    @Test fun dailySeedAndGameOrderAreDeterministic() {
        val date = LocalDate.of(2026, 7, 11)
        assertEquals(GameSeeds.daily(date), GameSeeds.daily(date))
        assertNotEquals(GameSeeds.daily(date).seed, GameSeeds.daily(date.plusDays(1)).seed)
    }

    @Test fun dailyRunContainsThreeDifferentGames() {
        val games = GameSeeds.daily(LocalDate.of(2026, 7, 11)).games
        assertEquals(3, games.size)
        assertEquals(3, games.distinct().size)
    }

    @Test fun memoryPatternIsBoundedAndRepeatable() {
        val cells = GameSeeds.memoryCells(seed = 42L, size = 4, count = 5)
        assertEquals(cells, GameSeeds.memoryCells(seed = 42L, size = 4, count = 5))
        assertEquals(5, cells.size)
        assertTrue(cells.all { it in 0..15 })
    }
}
