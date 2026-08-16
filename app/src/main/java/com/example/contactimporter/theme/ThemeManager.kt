package com.example.contactimporter.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("theme_settings")

data class AppTheme(
    val presetId: String,
    val backgroundUri: String?,
    val logoUri: String?
)

object ThemeManager {
    private val PRESET_ID = stringPreferencesKey("preset_id")
    private val BACKGROUND_URI = stringPreferencesKey("background_uri")
    private val LOGO_URI = stringPreferencesKey("logo_uri")

    fun getThemeSettings(context: Context): Flow<AppTheme> {
        return context.dataStore.data.map { prefs ->
            AppTheme(
                presetId = prefs[PRESET_ID] ?: "default",
                backgroundUri = prefs[BACKGROUND_URI],
                logoUri = prefs[LOGO_URI]
            )
        }
    }

    suspend fun setThemePreset(context: Context, presetId: String) {
        context.dataStore.edit { it[PRESET_ID] = presetId }
    }

    suspend fun setBackgroundUri(context: Context, uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri == null) prefs.remove(BACKGROUND_URI)
            else prefs[BACKGROUND_URI] = uri
        }
    }

    suspend fun setLogoUri(context: Context, uri: String?) {
        context.dataStore.edit { prefs ->
            if (uri == null) prefs.remove(LOGO_URI)
            else prefs[LOGO_URI] = uri
        }
    }
}