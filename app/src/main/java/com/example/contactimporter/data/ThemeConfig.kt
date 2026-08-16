package com.example.contactimporter.data

data class ThemeConfig(
    val characterId: String = "default",
    val customPrimaryColor: Long? = null,
    val customSecondaryColor: Long? = null,
    val customLogoUri: String? = null,
    val customBackgroundUri: String? = null,
    val selectedCardStyle: CardStyle? = null,
    val selectedAnimationStyle: AnimationStyle? = null
)