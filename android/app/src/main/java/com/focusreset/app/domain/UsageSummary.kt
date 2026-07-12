package com.focusreset.app.domain

object UsageSummary {
    fun selectedTotal(selectedPackages: Set<String>, usageMinutes: Map<String, Int>): Int =
        selectedPackages.sumOf { usageMinutes[it] ?: 0 }

    fun selectedBreakdown(selectedPackages: Set<String>, usageMinutes: Map<String, Int>): Map<String, Int> =
        selectedPackages.associateWith { usageMinutes[it] ?: 0 }
}
