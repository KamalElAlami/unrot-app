package com.focusreset.app.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("focus_reset_preferences")

class AppPreferences(private val context: Context) {
    private object Keys {
        val reducedMotion = booleanPreferencesKey("reduced_motion")
        val activeProgram = stringPreferencesKey("active_program")
        val practiceLimitMinutes = intPreferencesKey("practice_limit_minutes")
    }
    val reducedMotion = context.dataStore.data.map { it[Keys.reducedMotion] ?: false }
    val activeProgram = context.dataStore.data.map { it[Keys.activeProgram] }
    suspend fun setReducedMotion(value: Boolean) = context.dataStore.edit { it[Keys.reducedMotion] = value }
    suspend fun setActiveProgram(id: String?) = context.dataStore.edit { prefs -> if (id == null) prefs.remove(Keys.activeProgram) else prefs[Keys.activeProgram] = id }
    suspend fun setPracticeLimit(minutes: Int) = context.dataStore.edit { it[Keys.practiceLimitMinutes] = minutes.coerceIn(5, 15) }
}
