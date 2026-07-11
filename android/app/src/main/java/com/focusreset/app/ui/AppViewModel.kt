package com.focusreset.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusreset.app.FocusResetApplication
import com.focusreset.app.data.AppPreferences
import com.focusreset.app.data.ChallengeProgressEntity
import com.focusreset.app.data.FocusRunEntity
import com.focusreset.app.data.UsageStatsRepository
import com.focusreset.app.domain.ChallengeEngine
import com.focusreset.app.domain.ChallengeProgram
import com.focusreset.app.domain.ClarityScore
import com.focusreset.app.domain.DailyGameSeed
import com.focusreset.app.domain.DayOutcome
import com.focusreset.app.domain.Entitlement
import com.focusreset.app.domain.GameResult
import com.focusreset.app.domain.GameSeeds
import com.focusreset.app.domain.GameType
import com.focusreset.app.domain.ProgramCatalog
import com.focusreset.app.domain.Scoring
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Screen { ONBOARDING, HOME, PROGRAM, RUN, RESULT, PRACTICE, SQUADS, SETTINGS }

data class AppUiState(
    val initialized: Boolean = false,
    val screen: Screen = Screen.ONBOARDING,
    val selectedProgram: ChallengeProgram = ProgramCatalog.programs.first(),
    val activeProgram: ChallengeProgram? = null,
    val challengeDay: Int = 1,
    val completedChallengeDays: Int = 0,
    val challengeStreak: Int = 0,
    val currentDayOutcome: DayOutcome = DayOutcome.PENDING,
    val recoveryRequired: Boolean = false,
    val recoveryRun: Boolean = false,
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
    private val preferences = AppPreferences(application)
    private val usage = UsageStatsRepository(application)
    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        val today = LocalDate.now().toEpochDay()
        val onboardingComplete = preferences.onboardingComplete.first()
        val activeProgramId = preferences.activeProgram.first()
        val startedEpochDay = preferences.challengeStartedEpochDay.first()
        val activeProgram = ProgramCatalog.programs.firstOrNull { it.id == activeProgramId }
        val challengeDay = if (activeProgram != null && startedEpochDay != null) {
            ChallengeEngine.currentDay(startedEpochDay, today, activeProgram.length)
        } else 1
        val progress = activeProgram?.let { dao.progressForProgram(it.id) }.orEmpty()
        val outcomes = progress.mapNotNull { runCatching { DayOutcome.valueOf(it.outcome) }.getOrNull() }
        val currentOutcome = progress.firstOrNull { it.day == challengeDay }
            ?.let { runCatching { DayOutcome.valueOf(it.outcome) }.getOrDefault(DayOutcome.PENDING) }
            ?: DayOutcome.PENDING
        val minutes = usage.minutesToday(TRACKED_PACKAGES).values.sum()
        val dailyCompleted = dao.dailyRun(today) != null
        val practiceUsedMs = dao.practiceDuration(today)
        val reducedMotion = preferences.reducedMotion.first()
        _state.update {
            it.copy(
                initialized = true,
                screen = if (onboardingComplete) {
                    if (it.screen == Screen.ONBOARDING) Screen.HOME else it.screen
                } else Screen.ONBOARDING,
                activeProgram = activeProgram,
                challengeDay = challengeDay,
                completedChallengeDays = ChallengeEngine.completionCount(outcomes),
                challengeStreak = ChallengeEngine.streak(outcomes),
                currentDayOutcome = currentOutcome,
                dailyCompleted = dailyCompleted,
                practiceUsedMs = practiceUsedMs,
                usageAccess = usage.hasAccess(),
                selectedUsageMinutes = minutes,
                reducedMotion = reducedMotion
            )
        }
    }

    fun completeOnboarding() = viewModelScope.launch {
        preferences.setOnboardingComplete(true)
        _state.update { it.copy(screen = Screen.HOME) }
    }

    fun navigate(screen: Screen) { _state.update { it.copy(screen = screen) } }

    fun selectProgram(program: ChallengeProgram) {
        _state.update { it.copy(selectedProgram = program, screen = Screen.PROGRAM) }
    }

    fun activateSelectedProgram() {
        val program = _state.value.selectedProgram
        if (program.entitlement != Entitlement.FREE) return
        viewModelScope.launch {
            preferences.startProgram(program.id, LocalDate.now().toEpochDay())
            _state.update {
                it.copy(
                    activeProgram = program,
                    challengeDay = 1,
                    completedChallengeDays = 0,
                    challengeStreak = 0,
                    currentDayOutcome = DayOutcome.PENDING,
                    screen = Screen.HOME
                )
            }
        }
    }

    fun toggleReducedMotion() = viewModelScope.launch {
        val value = !_state.value.reducedMotion
        preferences.setReducedMotion(value)
        _state.update { it.copy(reducedMotion = value) }
    }

    fun startRun(practice: Boolean = false) {
        val current = _state.value
        if (!practice && current.dailyCompleted) return
        if (practice && current.practiceUsedMs >= PRACTICE_LIMIT_MS) return
        val seed = if (practice) GameSeeds.daily().copy(seed = System.nanoTime()) else GameSeeds.daily()
        _state.update {
            it.copy(
                screen = Screen.RUN,
                seed = seed,
                gameIndex = 0,
                results = emptyList(),
                latestScore = null,
                practice = practice,
                recoveryRun = false
            )
        }
    }

    fun startPractice(type: GameType) {
        val current = _state.value
        if (current.practiceUsedMs >= PRACTICE_LIMIT_MS) return
        val daily = GameSeeds.daily()
        _state.update {
            it.copy(
                screen = Screen.RUN,
                seed = daily.copy(seed = System.nanoTime(), games = listOf(type)),
                gameIndex = 0,
                results = emptyList(),
                latestScore = null,
                practice = true,
                recoveryRun = false
            )
        }
    }

    fun submitCheckIn(metTarget: Boolean) {
        val current = _state.value
        if (!current.dailyCompleted || current.activeProgram == null || current.currentDayOutcome != DayOutcome.PENDING) return
        if (metTarget) saveOutcome(DayOutcome.PERFECT)
        else _state.update { it.copy(recoveryRequired = true) }
    }

    fun startRecovery() {
        val current = _state.value
        if (!current.recoveryRequired || current.activeProgram == null) return
        val daily = GameSeeds.daily()
        _state.update {
            it.copy(
                screen = Screen.RUN,
                seed = daily.copy(seed = daily.seed + 7_919L, games = listOf(GameType.RULE_SHIFT)),
                gameIndex = 0,
                results = emptyList(),
                latestScore = null,
                practice = false,
                recoveryRun = true
            )
        }
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
        viewModelScope.launch {
            if (current.recoveryRun) {
                saveOutcomeNow(DayOutcome.RECOVERY)
            } else {
                dao.saveRun(
                    FocusRunEntity(
                        UUID.randomUUID().toString(),
                        LocalDate.now().toEpochDay(),
                        current.seed.seed,
                        score.total,
                        score.accuracy,
                        score.consistency,
                        score.impulseControl,
                        duration,
                        current.practice
                    )
                )
            }
            _state.update {
                it.copy(
                    screen = Screen.RESULT,
                    results = nextResults,
                    latestScore = score,
                    dailyCompleted = it.dailyCompleted || (!current.practice && !current.recoveryRun),
                    practiceUsedMs = it.practiceUsedMs + if (current.practice) duration else 0,
                    recoveryRequired = false
                )
            }
        }
    }

    private fun saveOutcome(outcome: DayOutcome) = viewModelScope.launch { saveOutcomeNow(outcome) }

    private suspend fun saveOutcomeNow(outcome: DayOutcome) {
        val current = _state.value
        val program = current.activeProgram ?: return
        dao.saveProgress(
            ChallengeProgressEntity(
                programId = program.id,
                day = current.challengeDay,
                outcome = outcome.name,
                completedAt = System.currentTimeMillis()
            )
        )
        val progress = dao.progressForProgram(program.id)
        val outcomes = progress.mapNotNull { runCatching { DayOutcome.valueOf(it.outcome) }.getOrNull() }
        _state.update {
            it.copy(
                currentDayOutcome = outcome,
                completedChallengeDays = ChallengeEngine.completionCount(outcomes),
                challengeStreak = ChallengeEngine.streak(outcomes),
                recoveryRequired = false
            )
        }
    }

    companion object {
        private const val PRACTICE_LIMIT_MS = 15 * 60_000L
        private val TRACKED_PACKAGES = setOf(
            "com.instagram.android",
            "com.zhiliaoapp.musically",
            "com.google.android.youtube"
        )
    }
}
