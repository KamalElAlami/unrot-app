package com.focusreset.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusreset.app.FocusResetApplication
import com.focusreset.app.data.*
import com.focusreset.app.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

enum class Screen { HOME, PROGRAM, RUN, RESULT, PRACTICE, SQUADS, SETTINGS }

data class AppUiState(
    val screen: Screen = Screen.HOME,
    val selectedProgram: ChallengeProgram = ProgramCatalog.programs.first(),
    val seed: DailyGameSeed = GameSeeds.daily(),
    val gameIndex: Int = 0,
    val results: List<GameResult> = emptyList(),
    val latestScore: ClarityScore? = null,
    val dailyCompleted: Boolean = false,
    val practice: Boolean = false,
    val practiceUsedMs: Long = 0,
    val usageAccess: Boolean = false,
    val selectedUsageMinutes: Int = 0,
    val reducedMotion: Boolean = false
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = (application as FocusResetApplication).database.focusDao()
    private val usage = UsageStatsRepository(application)
    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        val day = LocalDate.now().toEpochDay()
        val completed = dao.dailyRun(day) != null
        val practice = dao.practiceDuration(day)
        val minutes = usage.minutesToday(setOf("com.instagram.android", "com.zhiliaoapp.musically", "com.google.android.youtube")).values.sum()
        _state.update { it.copy(dailyCompleted = completed, practiceUsedMs = practice, usageAccess = usage.hasAccess(), selectedUsageMinutes = minutes) }
    }

    fun navigate(screen: Screen) { _state.update { it.copy(screen = screen) } }
    fun selectProgram(program: ChallengeProgram) { _state.update { it.copy(selectedProgram = program, screen = Screen.PROGRAM) } }
    fun toggleReducedMotion() { _state.update { it.copy(reducedMotion = !it.reducedMotion) } }

    fun startRun(practice: Boolean = false) {
        val current = _state.value
        if (!practice && current.dailyCompleted) return
        if (practice && current.practiceUsedMs >= 15 * 60_000L) return
        val seed = if (practice) GameSeeds.daily().copy(seed = System.nanoTime()) else GameSeeds.daily()
        _state.update { it.copy(screen = Screen.RUN, seed = seed, gameIndex = 0, results = emptyList(), latestScore = null, practice = practice) }
    }

    fun finishGame(result: GameResult) {
        val current = _state.value
        val nextResults = current.results + result
        if (current.gameIndex < current.seed.games.lastIndex) {
            _state.update { it.copy(results = nextResults, gameIndex = it.gameIndex + 1) }
            return
        }
        val score = Scoring.clarity(nextResults)
        val duration = nextResults.sumOf { it.durationMs }
        val run = FocusRunEntity(
            UUID.randomUUID().toString(), LocalDate.now().toEpochDay(), current.seed.seed,
            score.total, score.accuracy, score.consistency, score.impulseControl, duration, current.practice
        )
        viewModelScope.launch {
            dao.saveRun(run)
            _state.update { it.copy(screen = Screen.RESULT, results = nextResults, latestScore = score, dailyCompleted = it.dailyCompleted || !current.practice, practiceUsedMs = it.practiceUsedMs + if (current.practice) duration else 0) }
        }
    }
}
