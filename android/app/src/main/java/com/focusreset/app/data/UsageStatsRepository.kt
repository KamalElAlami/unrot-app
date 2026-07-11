package com.focusreset.app.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import java.time.LocalDate
import java.time.ZoneId

class UsageStatsRepository(private val context: Context) {
    fun hasAccess(): Boolean {
        val ops = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName) == AppOpsManager.MODE_ALLOWED
    }

    fun minutesToday(packages: Set<String>): Map<String, Int> {
        if (!hasAccess()) return emptyMap()
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
        val end = System.currentTimeMillis()
        val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        return manager.queryAndAggregateUsageStats(start, end)
            .filterKeys { it in packages }
            .mapValues { (_, stats) -> (stats.totalTimeInForeground / 60_000L).toInt() }
    }
}
