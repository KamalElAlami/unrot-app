package com.focusreset.app.domain

import java.time.LocalDate

enum class ProgramLength(val days: Int) { SEVEN(7), FOURTEEN(14), THIRTY(30) }
enum class ChallengeStatus { ACTIVE, COMPLETED, ABANDONED }
enum class DayOutcome { PENDING, PERFECT, MISSED, RECOVERY }
enum class GameType { GHOST_GRID, FLASH_CROWD, PATH_FINDER, SCHULTE_TABLE, DIGIT_MEMORY }
enum class Entitlement { FREE, PREMIUM }

data class ChallengeProgram(
    val id: String,
    val title: String,
    val summary: String,
    val length: ProgramLength,
    val entitlement: Entitlement,
    val dailyBudgetMinutes: Int
)

data class ChallengeDay(
    val programId: String,
    val day: Int,
    val title: String,
    val instruction: String,
    val outcome: DayOutcome = DayOutcome.PENDING
)

data class UsageBudget(val packageName: String, val displayName: String, val minutes: Int)
data class TrackableApp(val packageName: String, val displayName: String)

object TrackableAppCatalog {
    val apps = listOf(
        TrackableApp("com.instagram.android", "Instagram"),
        TrackableApp("com.zhiliaoapp.musically", "TikTok"),
        TrackableApp("com.google.android.youtube", "YouTube"),
        TrackableApp("com.facebook.katana", "Facebook"),
        TrackableApp("com.snapchat.android", "Snapchat")
    )
    val defaultPackages = apps.take(3).map { it.packageName }.toSet()
    fun label(packageName: String): String = apps.firstOrNull { it.packageName == packageName }?.displayName ?: packageName
}
data class DailyGameSeed(val date: LocalDate, val seed: Long, val games: List<GameType>)

data class GameResult(
    val type: GameType,
    val correct: Int,
    val incorrect: Int,
    val missed: Int,
    val durationMs: Long,
    val responseTimesMs: List<Long> = emptyList()
) {
    val attempts: Int get() = correct + incorrect + missed
}

data class ClarityScore(
    val total: Int,
    val accuracy: Int,
    val consistency: Int,
    val impulseControl: Int
)

data class FocusRun(
    val id: String,
    val date: LocalDate,
    val seed: Long,
    val results: List<GameResult>,
    val score: ClarityScore,
    val practice: Boolean
)

data class Squad(val id: String, val name: String, val inviteCode: String, val streakDays: Int)
data class SquadMember(val uid: String, val displayName: String, val avatarUrl: String? = null)
data class LeaderboardEntry(val member: SquadMember, val score: Int, val mistakes: Int, val durationMs: Long)
data class SubscriptionEntitlement(val tier: Entitlement, val expiresAtEpochMs: Long? = null)

object ProgramCatalog {
    val programs = listOf(
        ChallengeProgram("reset_7", "7 Days Without Reels", "Check in every day you avoid short-video feeds.", ProgramLength.SEVEN, Entitlement.FREE, 0),
        ChallengeProgram("builder_14", "14 Days Without Reels", "Build a stronger no-short-video routine.", ProgramLength.FOURTEEN, Entitlement.PREMIUM, 0),
        ChallengeProgram("reboot_30", "30 Days Without Reels", "Turn a reset into a lasting boundary.", ProgramLength.THIRTY, Entitlement.PREMIUM, 0)
    )

    fun days(program: ChallengeProgram): List<ChallengeDay> = (1..program.length.days).map { day ->
        val phase = when (program.length) {
            ProgramLength.SEVEN -> "Reset"
            ProgramLength.FOURTEEN -> if (day <= 7) "Reduce" else "Build"
            ProgramLength.THIRTY -> when (day) { in 1..7 -> "Observe"; in 8..14 -> "Reduce"; in 15..21 -> "Replace"; else -> "Maintain" }
        }
        ChallengeDay(program.id, day, "$phase · Day $day", "Avoid Reels, Shorts, and TikTok today, then complete your honest check-in.")
    }
}
