package com.example.contactimporter.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _themeConfig = MutableStateFlow(loadConfig())
    val themeConfig: StateFlow<ThemeConfig> = _themeConfig.asStateFlow()

    private val _availableCharacters = MutableStateFlow<List<CharacterMetadata>>(emptyList())
    val availableCharacters: StateFlow<List<CharacterMetadata>> = _availableCharacters.asStateFlow()

    val characterLoadError: String? get() = CharacterMetadataLoader.lastError

    init {
        // Load characters asynchronously to avoid blocking main thread
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            Log.d("ThemeRepository", "Loading character library...")
            val characters = CharacterMetadataLoader.loadRegistry(context)
            _availableCharacters.value = characters
            Log.d("ThemeRepository", "Character library loaded: ${characters.size} characters")
        }
    }

    fun updateConfig(config: ThemeConfig) {
        saveConfig(config)
        _themeConfig.value = config
    }

    fun getActiveCharacter(): CharacterMetadata? {
        val id = _themeConfig.value.characterId
        return _availableCharacters.value.find { it.id == id }
    }

    private fun loadConfig(): ThemeConfig {
        return ThemeConfig(
            characterId = prefs.getString("characterId", "default") ?: "default",
            themeEnabled = prefs.getBoolean("themeEnabled", true),
            customPrimaryColor = prefs.getLong("customPrimaryColor", -1L).takeIf { it != -1L },
            customSecondaryColor = prefs.getLong("customSecondaryColor", -1L).takeIf { it != -1L },
            customLogoUri = prefs.getString("customLogoUri", null),
            customBackgroundUri = prefs.getString("customBackgroundUri", null),
            selectedCardStyle = prefs.getString("selectedCardStyle", null)?.let { CardStyle.valueOf(it) },
            selectedAnimationStyle = prefs.getString("selectedAnimationStyle", null)?.let { AnimationStyle.valueOf(it) },
            selectedBackgroundStyle = prefs.getString("selectedBackgroundStyle", null)?.let { BackgroundStyle.valueOf(it) }
        )
    }

    private fun saveConfig(config: ThemeConfig) {
        prefs.edit().apply {
            putString("characterId", config.characterId)
            putBoolean("themeEnabled", config.themeEnabled)
            config.customPrimaryColor?.let { putLong("customPrimaryColor", it) } ?: remove("customPrimaryColor")
            config.customSecondaryColor?.let { putLong("customSecondaryColor", it) } ?: remove("customSecondaryColor")
            config.customLogoUri?.let { putString("customLogoUri", it) } ?: remove("customLogoUri")
            config.customBackgroundUri?.let { putString("customBackgroundUri", it) } ?: remove("customBackgroundUri")
            config.selectedCardStyle?.let { putString("selectedCardStyle", it.name) } ?: remove("selectedCardStyle")
            config.selectedAnimationStyle?.let { putString("selectedAnimationStyle", it.name) } ?: remove("selectedAnimationStyle")
            config.selectedBackgroundStyle?.let { putString("selectedBackgroundStyle", it.name) } ?: remove("selectedBackgroundStyle")
            apply()
        }
    }
}