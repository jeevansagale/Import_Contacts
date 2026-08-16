package com.example.contactimporter.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterThemeTest {

    @Test
    fun `character ids are unique`() {
        val ids = CharacterRegistry.characters.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `each character has usable local assets`() {
        CharacterRegistry.characters.forEach { theme ->
            assertTrue(theme.portraitAsset.isNotBlank())
            assertTrue(theme.heroAsset.isNotBlank())
            assertTrue(theme.franchise.isNotBlank())
        }
    }
}