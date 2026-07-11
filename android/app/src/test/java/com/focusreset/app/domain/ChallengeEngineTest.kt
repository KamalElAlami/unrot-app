package com.focusreset.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChallengeEngineTest {
    @Test fun currentDayStartsAtOneAndStopsAtProgramLength() {
        assertEquals(1, ChallengeEngine.currentDay(100, 100, ProgramLength.SEVEN))
        assertEquals(4, ChallengeEngine.currentDay(100, 103, ProgramLength.SEVEN))
        assertEquals(7, ChallengeEngine.currentDay(100, 120, ProgramLength.SEVEN))
    }

    @Test fun completionAndRecoveryBothPreserveProgress() {
        val outcomes = listOf(DayOutcome.PERFECT, DayOutcome.RECOVERY, DayOutcome.PENDING)
        assertEquals(2, ChallengeEngine.completionCount(outcomes))
        assertEquals(0, ChallengeEngine.streak(outcomes))
        assertEquals(2, ChallengeEngine.streak(outcomes.dropLast(1)))
    }

    @Test fun finishedOnlyAfterAllProgramDaysHaveElapsed() {
        assertFalse(ChallengeEngine.isFinished(100, 106, ProgramLength.SEVEN))
        assertTrue(ChallengeEngine.isFinished(100, 107, ProgramLength.SEVEN))
    }
}
