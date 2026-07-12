package com.focusreset.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class FiniteSessionPolicyTest {
    @Test fun activeRoundBecomesInterruptedWhenAppPauses() {
        assertEquals(FiniteSessionPhase.INTERRUPTED, FiniteSessionPolicy.onAppPaused(FiniteSessionPhase.PLAYING))
    }

    @Test fun introDoesNotBecomeInterruptedByPermissionOrSystemDialogs() {
        assertEquals(FiniteSessionPhase.INTRO, FiniteSessionPolicy.onAppPaused(FiniteSessionPhase.INTRO))
    }

    @Test fun interruptedRoundStaysInterruptedUntilExplicitRestart() {
        assertEquals(FiniteSessionPhase.INTERRUPTED, FiniteSessionPolicy.onAppPaused(FiniteSessionPhase.INTERRUPTED))
    }
}
