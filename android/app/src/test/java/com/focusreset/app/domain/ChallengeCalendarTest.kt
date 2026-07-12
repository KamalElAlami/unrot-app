package com.focusreset.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ChallengeCalendarTest {
    @Test fun sevenDayProgramAlwaysShowsFirstWeek() {
        assertEquals(1..7, ChallengeCalendar.visibleWeek(7, ProgramLength.SEVEN).days)
    }

    @Test fun fourteenDayProgramMovesToSecondWeek() {
        assertEquals(1..7, ChallengeCalendar.visibleWeek(1, ProgramLength.FOURTEEN).days)
        assertEquals(8..14, ChallengeCalendar.visibleWeek(8, ProgramLength.FOURTEEN).days)
        assertEquals(8..14, ChallengeCalendar.visibleWeek(14, ProgramLength.FOURTEEN).days)
    }

    @Test fun thirtyDayProgramHandlesPartialFinalWeek() {
        assertEquals(22..28, ChallengeCalendar.visibleWeek(25, ProgramLength.THIRTY).days)
        assertEquals(29..30, ChallengeCalendar.visibleWeek(30, ProgramLength.THIRTY).days)
    }

    @Test fun invalidDayInputsAreClampedSafely() {
        assertEquals(1..7, ChallengeCalendar.visibleWeek(0, ProgramLength.THIRTY).days)
        assertEquals(29..30, ChallengeCalendar.visibleWeek(99, ProgramLength.THIRTY).days)
    }
}
