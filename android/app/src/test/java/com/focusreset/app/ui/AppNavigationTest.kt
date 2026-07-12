package com.focusreset.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppNavigationTest {
    @Test fun rootScreensAllowAndroidToExitNormally() {
        assertNull(AppNavigation.backDestination(AppUiState(screen = Screen.HOME)))
        assertNull(AppNavigation.backDestination(AppUiState(screen = Screen.ONBOARDING)))
    }

    @Test fun topLevelSectionsReturnToChallengeDashboard() {
        listOf(Screen.HISTORY, Screen.PROGRAM, Screen.RESULT, Screen.PRACTICE, Screen.SQUADS, Screen.SETTINGS).forEach { screen ->
            assertEquals(Screen.HOME, AppNavigation.backDestination(AppUiState(screen = screen)))
        }
    }

    @Test fun practiceGameReturnsToGameCatalog() {
        assertEquals(Screen.PRACTICE, AppNavigation.backDestination(AppUiState(screen = Screen.RUN, practice = true)))
    }

    @Test fun dailyOrRecoveryGameReturnsToDashboard() {
        assertEquals(Screen.HOME, AppNavigation.backDestination(AppUiState(screen = Screen.RUN, practice = false)))
    }

    @Test fun privacyReturnsToSettingsInsteadOfExiting() {
        assertEquals(Screen.SETTINGS, AppNavigation.backDestination(AppUiState(screen = Screen.PRIVACY)))
    }
}
