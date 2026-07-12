package com.focusreset.app.domain

object ChallengeEngine {
    fun currentDay(startedEpochDay: Long, todayEpochDay: Long, length: ProgramLength): Int =
        (todayEpochDay - startedEpochDay + 1L).coerceIn(1L, length.days.toLong()).toInt()

    fun isFinished(startedEpochDay: Long, todayEpochDay: Long, length: ProgramLength): Boolean =
        todayEpochDay - startedEpochDay >= length.days

    fun completionCount(outcomes: List<DayOutcome>): Int =
        outcomes.count { it == DayOutcome.PERFECT || it == DayOutcome.RECOVERY }

    fun elapsedMissedDays(startedEpochDay: Long, todayEpochDay: Long, length: ProgramLength, recordedDays: Set<Int>): List<Int> {
        val elapsedBeforeToday = (todayEpochDay - startedEpochDay).coerceIn(0, length.days.toLong()).toInt()
        return (1..elapsedBeforeToday).filterNot { it in recordedDays }
    }

    fun isComplete(currentDay: Int, length: ProgramLength, currentOutcome: DayOutcome): Boolean =
        currentDay >= length.days && currentOutcome != DayOutcome.PENDING

    fun streak(outcomes: List<DayOutcome>): Int {
        var streak = 0
        for (outcome in outcomes.asReversed()) {
            if (outcome == DayOutcome.PERFECT || outcome == DayOutcome.RECOVERY) streak++ else break
        }
        return streak
    }
}
