package com.focusreset.app.domain

object ChallengeEngine {
    fun currentDay(startedEpochDay: Long, todayEpochDay: Long, length: ProgramLength): Int =
        (todayEpochDay - startedEpochDay + 1L).coerceIn(1L, length.days.toLong()).toInt()

    fun isFinished(startedEpochDay: Long, todayEpochDay: Long, length: ProgramLength): Boolean =
        todayEpochDay - startedEpochDay >= length.days

    fun completionCount(outcomes: List<DayOutcome>): Int =
        outcomes.count { it == DayOutcome.PERFECT || it == DayOutcome.RECOVERY }

    fun streak(outcomes: List<DayOutcome>): Int {
        var streak = 0
        for (outcome in outcomes.asReversed()) {
            if (outcome == DayOutcome.PERFECT || outcome == DayOutcome.RECOVERY) streak++ else break
        }
        return streak
    }
}
