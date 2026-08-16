package com.example.contactimporter.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeRepository {
    private val _themeConfig = MutableStateFlow(ThemeConfig())
    val themeConfig: StateFlow<ThemeConfig> = _themeConfig.asStateFlow()

    fun updateConfig(config: ThemeConfig) {
        _themeConfig.value = config
    }

    fun getActiveCharacter(): CharacterTheme? {
        val id = _themeConfig.value.characterId
        return CharacterRegistry.characters.find { it.id == id }
    }
}