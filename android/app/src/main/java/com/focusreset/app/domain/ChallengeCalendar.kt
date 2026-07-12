package com.focusreset.app.domain

data class ChallengeWeek(val startDay: Int, val endDay: Int) {
    val days: IntRange get() = startDay..endDay
}

object ChallengeCalendar {
    fun visibleWeek(currentDay: Int, length: ProgramLength): ChallengeWeek {
        val safeDay = currentDay.coerceIn(1, length.days)
        val start = ((safeDay - 1) / 7) * 7 + 1
        return ChallengeWeek(start, minOf(start + 6, length.days))
    }
}
