package com.focusreset.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.focusreset.app.MainActivity
import com.focusreset.app.R
import com.focusreset.app.data.AppDatabase
import com.focusreset.app.data.AppPreferences
import com.focusreset.app.domain.ChallengeEngine
import com.focusreset.app.domain.ProgramCatalog
import java.time.ZonedDateTime
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

class DailyReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val preferences = AppPreferences(applicationContext)
        val programId = preferences.activeProgram.first() ?: return Result.success()
        val started = preferences.challengeStartedEpochDay.first() ?: return Result.success()
        val program = ProgramCatalog.programs.firstOrNull { it.id == programId } ?: return Result.success()
        val day = ChallengeEngine.currentDay(started, LocalDate.now().toEpochDay(), program.length)
        val database = AppDatabase.create(applicationContext)
        val alreadyCheckedIn = try { database.focusDao().progress(programId, day) != null } finally { database.close() }
        if (alreadyCheckedIn) return Result.success()
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = "no_reels_checkin"
        manager.createNotificationChannel(NotificationChannel(channel, "No-Reels check-in", NotificationManager.IMPORTANCE_DEFAULT))
        val openApp = PendingIntent.getActivity(
            applicationContext,
            41,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        manager.notify(
            41,
            NotificationCompat.Builder(applicationContext, channel)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Ready for today’s No-Reels check-in?")
                .setContentText("Record your day honestly. It takes a few seconds.")
                .setContentIntent(openApp)
                .setAutoCancel(true)
                .build()
        )
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "daily-no-reels-checkin"

        fun schedule(context: Context, hour: Int, minute: Int) {
            val now = ZonedDateTime.now()
            var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
            if (!next.isAfter(now)) next = next.plusDays(1)
            val delay = java.time.Duration.between(now, next).toMillis()
            val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
        }

        fun cancel(context: Context) = WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
