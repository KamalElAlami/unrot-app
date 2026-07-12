package com.focusreset.app.ui

enum class FiniteSessionPhase { INTRO, PLAYING, INTERRUPTED }

object FiniteSessionPolicy {
    fun onAppPaused(phase: FiniteSessionPhase): FiniteSessionPhase =
        if (phase == FiniteSessionPhase.PLAYING) FiniteSessionPhase.INTERRUPTED else phase
}
