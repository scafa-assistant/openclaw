package de.openclaw.assistant.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property for DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "openclaw_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_TIER = stringPreferencesKey("user_tier")
        val PREFERRED_MODEL = stringPreferencesKey("preferred_model")
        val LANGUAGE = stringPreferencesKey("language")
        val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
    }

    // Auth Token
    val authToken: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[AUTH_TOKEN] }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
        }
    }

    suspend fun clearAuthToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN)
        }
    }

    // User Data
    val userEmail: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[USER_EMAIL] }

    val userTier: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[USER_TIER] }

    suspend fun saveUserData(email: String, tier: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL] = email
            preferences[USER_TIER] = tier
        }
    }

    // Settings
    val preferredModel: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[PREFERRED_MODEL] ?: "gemini-2.5-flash" }

    val language: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[LANGUAGE] ?: "de-DE" }

    val ttsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[TTS_ENABLED] ?: true }

    suspend fun saveSettings(model: String, language: String, tts: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PREFERRED_MODEL] = model
            preferences[LANGUAGE] = language
            preferences[TTS_ENABLED] = tts
        }
    }

    // Clear all
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
