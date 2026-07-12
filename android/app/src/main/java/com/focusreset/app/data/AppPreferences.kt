package com.focusreset.app.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("focus_reset_preferences")

class AppPreferences(private val context: Context) {
    private object Keys {
        val onboardingComplete = booleanPreferencesKey("onboarding_complete")
        val reducedMotion = booleanPreferencesKey("reduced_motion")
        val activeProgram = stringPreferencesKey("active_program")
        val challengeStartedEpochDay = longPreferencesKey("challenge_started_epoch_day")
        val practiceLimitMinutes = intPreferencesKey("practice_limit_minutes")
        val reminderEnabled = booleanPreferencesKey("reminder_enabled")
        val reminderHour = intPreferencesKey("reminder_hour")
        val reminderMinute = intPreferencesKey("reminder_minute")
        val trackedApps = stringSetPreferencesKey("tracked_apps")
        val soundEnabled = booleanPreferencesKey("sound_enabled")
        val hapticsEnabled = booleanPreferencesKey("haptics_enabled")
    }
    val onboardingComplete = context.dataStore.data.map { it[Keys.onboardingComplete] ?: false }
    val reducedMotion = context.dataStore.data.map { it[Keys.reducedMotion] ?: false }
    val activeProgram = context.dataStore.data.map { it[Keys.activeProgram] }
    val challengeStartedEpochDay = context.dataStore.data.map { it[Keys.challengeStartedEpochDay] }
    val reminderEnabled = context.dataStore.data.map { it[Keys.reminderEnabled] ?: false }
    val reminderHour = context.dataStore.data.map { it[Keys.reminderHour] ?: 21 }
    val reminderMinute = context.dataStore.data.map { it[Keys.reminderMinute] ?: 0 }
    val trackedApps = context.dataStore.data.map { it[Keys.trackedApps] ?: com.focusreset.app.domain.TrackableAppCatalog.defaultPackages }
    val soundEnabled = context.dataStore.data.map { it[Keys.soundEnabled] ?: false }
    val hapticsEnabled = context.dataStore.data.map { it[Keys.hapticsEnabled] ?: true }
    suspend fun setOnboardingComplete(value: Boolean) = context.dataStore.edit { it[Keys.onboardingComplete] = value }
    suspend fun setReducedMotion(value: Boolean) = context.dataStore.edit { it[Keys.reducedMotion] = value }
    suspend fun startProgram(id: String, startedEpochDay: Long) = context.dataStore.edit {
        it[Keys.activeProgram] = id
        it[Keys.challengeStartedEpochDay] = startedEpochDay
    }
    suspend fun clearProgram() = context.dataStore.edit {
        it.remove(Keys.activeProgram)
        it.remove(Keys.challengeStartedEpochDay)
    }
    suspend fun setPracticeLimit(minutes: Int) = context.dataStore.edit { it[Keys.practiceLimitMinutes] = minutes.coerceIn(5, 15) }
    suspend fun setReminder(enabled: Boolean, hour: Int, minute: Int) = context.dataStore.edit {
        it[Keys.reminderEnabled] = enabled
        it[Keys.reminderHour] = hour.coerceIn(0, 23)
        it[Keys.reminderMinute] = minute.coerceIn(0, 59)
    }
    suspend fun clearAll() = context.dataStore.edit { it.clear() }
    suspend fun setTrackedApps(packages: Set<String>) = context.dataStore.edit { it[Keys.trackedApps] = packages }
    suspend fun setSoundEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.soundEnabled] = enabled }
    suspend fun setHapticsEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.hapticsEnabled] = enabled }
}
