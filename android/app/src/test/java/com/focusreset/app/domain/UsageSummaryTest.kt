package com.focusreset.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UsageSummaryTest {
    @Test fun catalogPackagesAreUniqueAndDefaultsAreSupported() {
        val packages = TrackableAppCatalog.apps.map { it.packageName }
        assertEquals(packages.size, packages.distinct().size)
        assertTrue(TrackableAppCatalog.defaultPackages.all { it in packages })
    }

    @Test fun totalIncludesOnlySelectedApps() {
        val selected = setOf("instagram", "youtube")
        val usage = mapOf("instagram" to 12, "youtube" to 8, "ignored" to 99)
        assertEquals(20, UsageSummary.selectedTotal(selected, usage))
    }

    @Test fun breakdownIncludesSelectedAppsWithZeroFallback() {
        assertEquals(
            mapOf("instagram" to 7, "youtube" to 0),
            UsageSummary.selectedBreakdown(setOf("instagram", "youtube"), mapOf("instagram" to 7))
        )
    }

    @Test fun emptySelectionProducesNoUsageContext() {
        assertEquals(0, UsageSummary.selectedTotal(emptySet(), mapOf("instagram" to 50)))
        assertEquals(emptyMap<String, Int>(), UsageSummary.selectedBreakdown(emptySet(), mapOf("instagram" to 50)))
    }
}
