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
    }
    val onboardingComplete = context.dataStore.data.map { it[Keys.onboardingComplete] ?: false }
    val reducedMotion = context.dataStore.data.map { it[Keys.reducedMotion] ?: false }
    val activeProgram = context.dataStore.data.map { it[Keys.activeProgram] }
    val challengeStartedEpochDay = context.dataStore.data.map { it[Keys.challengeStartedEpochDay] }
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
}
