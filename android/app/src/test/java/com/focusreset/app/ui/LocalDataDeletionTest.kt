package com.focusreset.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class LocalDataDeletionTest {
    @Test fun deletionReturnsACompletelyFreshOnboardingState() {
        val state = LocalDataDeletion.clearedUiState()
        assertEquals(Screen.ONBOARDING, state.screen)
        assertNull(state.activeProgram)
        assertEquals(emptyMap<Int, String>(), state.challengeNotes)
        assertEquals(0, state.completedChallengeDays)
        assertFalse(state.reminderEnabled)
    }
}
