package com.focusreset.app.ui

object LocalDataDeletion {
    fun clearedUiState(): AppUiState = AppUiState(initialized = true, screen = Screen.ONBOARDING)
}
