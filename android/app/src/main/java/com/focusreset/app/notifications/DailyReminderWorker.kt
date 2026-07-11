package com.focusreset.app.notifications

import android.app.*
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

class DailyReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = "daily_focus"
        manager.createNotificationChannel(NotificationChannel(channel, "Daily focus run", NotificationManager.IMPORTANCE_DEFAULT))
        manager.notify(41, NotificationCompat.Builder(applicationContext, channel).setSmallIcon(com.focusreset.app.R.drawable.ic_launcher).setContentTitle("Today’s Focus Run is ready").setContentText("Five finite minutes. Then leave your phone.").setAutoCancel(true).build())
        return Result.success()
    }

    companion object {
        fun schedule(context: Context) = WorkManager.getInstance(context).enqueueUniquePeriodicWork("daily-focus", ExistingPeriodicWorkPolicy.UPDATE, PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS).build())
    }
}
