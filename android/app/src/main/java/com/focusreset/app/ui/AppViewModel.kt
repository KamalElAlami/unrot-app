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
import com.focusreset.app.domain.TrackableAppCatalog
import com.focusreset.app.domain.UsageSummary
import com.focusreset.app.notifications.DailyReminderWorker
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Screen { ONBOARDING, HOME, HISTORY, PROGRAM, RUN, RESULT, PRACTICE, SQUADS, SETTINGS, PRIVACY }

data class AppUiState(
    val initialized: Boolean = false,
    val screen: Screen = Screen.ONBOARDING,
    val selectedProgram: ChallengeProgram = ProgramCatalog.programs.first(),
    val activeProgram: ChallengeProgram? = null,
    val challengeDay: Int = 1,
    val completedChallengeDays: Int = 0,
    val challengeStreak: Int = 0,
    val challengeOutcomes: Map<Int, DayOutcome> = emptyMap(),
    val challengeFinished: Boolean = false,
    val challengeNotes: Map<Int, String> = emptyMap(),
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
    val trackedApps: Set<String> = TrackableAppCatalog.defaultPackages,
    val usageMinutesByApp: Map<String, Int> = emptyMap(),
    val reducedMotion: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 21,
    val reminderMinute: Int = 0
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
        var activeProgramId = preferences.activeProgram.first()
        var startedEpochDay = preferences.challengeStartedEpochDay.first()
        if (onboardingComplete && activeProgramId == null) {
            val starter = ProgramCatalog.programs.first()
            startedEpochDay = today
            activeProgramId = starter.id
            preferences.startProgram(starter.id, today)
        }
        val activeProgram = ProgramCatalog.programs.firstOrNull { it.id == activeProgramId }
        val challengeDay = if (activeProgram != null && startedEpochDay != null) {
            ChallengeEngine.currentDay(startedEpochDay, today, activeProgram.length)
        } else 1
        var progress = activeProgram?.let { dao.progressForProgram(it.id) }.orEmpty()
        if (activeProgram != null && startedEpochDay != null) {
            ChallengeEngine.elapsedMissedDays(startedEpochDay, today, activeProgram.length, progress.map { it.day }.toSet()).forEach { missedDay ->
                dao.saveProgress(ChallengeProgressEntity(activeProgram.id, missedDay, DayOutcome.MISSED.name, null))
            }
            progress = dao.progressForProgram(activeProgram.id)
        }
        val outcomes = progress.mapNotNull { runCatching { DayOutcome.valueOf(it.outcome) }.getOrNull() }
        val outcomesByDay = progress.associate { item ->
            item.day to runCatching { DayOutcome.valueOf(item.outcome) }.getOrDefault(DayOutcome.PENDING)
        }
        val notesByDay = progress.mapNotNull { item -> item.note?.takeIf(String::isNotBlank)?.let { item.day to it } }.toMap()
        val currentOutcome = progress.firstOrNull { it.day == challengeDay }
            ?.let { runCatching { DayOutcome.valueOf(it.outcome) }.getOrDefault(DayOutcome.PENDING) }
            ?: DayOutcome.PENDING
        val trackedApps = preferences.trackedApps.first()
        val usageMinutesByApp = usage.minutesToday(trackedApps)
        val minutes = UsageSummary.selectedTotal(trackedApps, usageMinutesByApp)
        val dailyCompleted = dao.dailyRun(today) != null
        val practiceUsedMs = dao.practiceDuration(today)
        val reducedMotion = preferences.reducedMotion.first()
        val reminderEnabled = preferences.reminderEnabled.first()
        val reminderHour = preferences.reminderHour.first()
        val reminderMinute = preferences.reminderMinute.first()
        if (reminderEnabled) DailyReminderWorker.schedule(getApplication(), reminderHour, reminderMinute)
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
                challengeOutcomes = outcomesByDay,
                currentDayOutcome = currentOutcome,
                challengeFinished = activeProgram?.let { ChallengeEngine.isComplete(challengeDay, it.length, currentOutcome) } ?: false,
                challengeNotes = notesByDay,
                recoveryRequired = currentOutcome == DayOutcome.MISSED,
                dailyCompleted = dailyCompleted,
                practiceUsedMs = practiceUsedMs,
                usageAccess = usage.hasAccess(),
                selectedUsageMinutes = minutes,
                trackedApps = trackedApps,
                usageMinutesByApp = UsageSummary.selectedBreakdown(trackedApps, usageMinutesByApp),
                reducedMotion = reducedMotion,
                reminderEnabled = reminderEnabled,
                reminderHour = reminderHour,
                reminderMinute = reminderMinute
            )
        }
    }

    fun completeOnboarding() = viewModelScope.launch {
        preferences.setOnboardingComplete(true)
        val starter = ProgramCatalog.programs.first()
        if (preferences.activeProgram.first() == null) {
            preferences.startProgram(starter.id, LocalDate.now().toEpochDay())
        }
        _state.update {
            it.copy(
                screen = Screen.HOME,
                activeProgram = it.activeProgram ?: starter,
                selectedProgram = starter,
                challengeDay = 1
            )
        }
    }

    fun navigate(screen: Screen) { _state.update { it.copy(screen = screen) } }

    fun goBack() {
        AppNavigation.backDestination(_state.value)?.let { destination ->
            _state.update { it.copy(screen = destination, results = emptyList(), latestScore = null, recoveryRun = false) }
        }
    }

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
                    challengeOutcomes = emptyMap(),
                    challengeFinished = false,
                    challengeNotes = emptyMap(),
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

    fun setReminderEnabled(enabled: Boolean) = viewModelScope.launch {
        val current = _state.value
        preferences.setReminder(enabled, current.reminderHour, current.reminderMinute)
        if (enabled) DailyReminderWorker.schedule(getApplication(), current.reminderHour, current.reminderMinute)
        else DailyReminderWorker.cancel(getApplication())
        _state.update { it.copy(reminderEnabled = enabled) }
    }

    fun setReminderTime(hour: Int, minute: Int) = viewModelScope.launch {
        val current = _state.value
        preferences.setReminder(current.reminderEnabled, hour, minute)
        if (current.reminderEnabled) DailyReminderWorker.schedule(getApplication(), hour, minute)
        _state.update { it.copy(reminderHour = hour, reminderMinute = minute) }
    }

    fun toggleTrackedApp(packageName: String) = viewModelScope.launch {
        val selected = _state.value.trackedApps.toMutableSet()
        if (!selected.add(packageName)) selected.remove(packageName)
        preferences.setTrackedApps(selected)
        val minutesByApp = usage.minutesToday(selected)
        _state.update {
            it.copy(
                trackedApps = selected,
                usageMinutesByApp = UsageSummary.selectedBreakdown(selected, minutesByApp),
                selectedUsageMinutes = UsageSummary.selectedTotal(selected, minutesByApp)
            )
        }
    }

    fun deleteAllLocalData() = viewModelScope.launch {
        DailyReminderWorker.cancel(getApplication())
        dao.clearRuns()
        dao.clearAllProgress()
        preferences.clearAll()
        _state.value = LocalDataDeletion.clearedUiState()
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
        if (current.activeProgram == null || current.currentDayOutcome != DayOutcome.PENDING) return
        if (metTarget) saveOutcome(DayOutcome.PERFECT)
        else viewModelScope.launch {
            saveOutcomeNow(DayOutcome.MISSED)
            _state.update { it.copy(recoveryRequired = true) }
        }
    }

    fun startRecovery() {
        val current = _state.value
        if ((!current.recoveryRequired && current.currentDayOutcome != DayOutcome.MISSED) || current.activeProgram == null) return
        val daily = GameSeeds.daily()
        _state.update {
            it.copy(
                screen = Screen.RUN,
                seed = daily.copy(seed = daily.seed + 7_919L, games = listOf(GameType.GHOST_GRID)),
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
                completedAt = System.currentTimeMillis(),
                note = dao.progress(program.id, current.challengeDay)?.note
            )
        )
        val progress = dao.progressForProgram(program.id)
        val outcomes = progress.mapNotNull { runCatching { DayOutcome.valueOf(it.outcome) }.getOrNull() }
        val outcomesByDay = progress.associate { item ->
            item.day to runCatching { DayOutcome.valueOf(item.outcome) }.getOrDefault(DayOutcome.PENDING)
        }
        val notesByDay = progress.mapNotNull { item -> item.note?.takeIf(String::isNotBlank)?.let { item.day to it } }.toMap()
        _state.update {
            it.copy(
                currentDayOutcome = outcome,
                completedChallengeDays = ChallengeEngine.completionCount(outcomes),
                challengeStreak = ChallengeEngine.streak(outcomes),
                challengeOutcomes = outcomesByDay,
                challengeFinished = ChallengeEngine.isComplete(current.challengeDay, program.length, outcome),
                challengeNotes = notesByDay,
                recoveryRequired = false
            )
        }
    }

    fun saveJournalNote(note: String) {
        val current = _state.value
        val program = current.activeProgram ?: return
        if (current.currentDayOutcome == DayOutcome.PENDING) return
        viewModelScope.launch {
            val cleaned = note.trim().take(240).ifBlank { null }
            dao.updateNote(program.id, current.challengeDay, cleaned)
            _state.update {
                val notes = it.challengeNotes.toMutableMap()
                if (cleaned == null) notes.remove(it.challengeDay) else notes[it.challengeDay] = cleaned
                it.copy(challengeNotes = notes)
            }
        }
    }

    fun restartChallenge() {
        val program = _state.value.activeProgram ?: return
        viewModelScope.launch {
            dao.clearProgress(program.id)
            preferences.startProgram(program.id, LocalDate.now().toEpochDay())
            _state.update {
                it.copy(
                    challengeDay = 1,
                    completedChallengeDays = 0,
                    challengeStreak = 0,
                    challengeOutcomes = emptyMap(),
                    currentDayOutcome = DayOutcome.PENDING,
                    challengeFinished = false,
                    challengeNotes = emptyMap(),
                    recoveryRequired = false,
                    dailyCompleted = false,
                    screen = Screen.HOME
                )
            }
        }
    }

    companion object {
        private const val PRACTICE_LIMIT_MS = 15 * 60_000L
    }
}
