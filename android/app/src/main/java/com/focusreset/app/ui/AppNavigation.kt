package com.focusreset.app.ui

object AppNavigation {
    fun backDestination(state: AppUiState): Screen? = when (state.screen) {
        Screen.HOME, Screen.ONBOARDING -> null
        Screen.RUN -> if (state.practice) Screen.PRACTICE else Screen.HOME
        Screen.HISTORY, Screen.PROGRAM, Screen.RESULT, Screen.SQUADS, Screen.SETTINGS -> Screen.HOME
        Screen.PRIVACY -> Screen.SETTINGS
        Screen.PRACTICE -> Screen.HOME
    }
}
