package com.example.contactimporter

import com.example.contactimporter.data.ContactParser
import org.junit.Assert.assertEquals
import org.junit.Test

class ContactParserTest {

    @Test
    fun testRootArrayNumberString() {
        val json = """[{"name": "Furina", "number": "+919876543210"}]"""
        val result = ContactParser.parseContacts(json)
        assertEquals(1, result.validEntries.size)
        assertEquals("Furina", result.validEntries[0].name)
        assertEquals("+919876543210", result.validEntries[0].number)
    }

    @Test
    fun testRootArrayPhoneString() {
        val json = """[{"name": "Furina", "phone": "+919876543210"}]"""
        val result = ContactParser.parseContacts(json)
        assertEquals(1, result.validEntries.size)
        assertEquals("Furina", result.validEntries[0].name)
        assertEquals("+919876543210", result.validEntries[0].number)
    }

    @Test
    fun testNumberAsJsonInteger() {
        val json = """[{"name": "Furina", "number": 919876543210}]"""
        val result = ContactParser.parseContacts(json)
        assertEquals(1, result.validEntries.size)
        assertEquals("919876543210", result.validEntries[0].number)
    }

    @Test
    fun testPhoneAsJsonInteger() {
        val json = """[{"name": "Furina", "phone": 919876543210}]"""
        val result = ContactParser.parseContacts(json)
        assertEquals(1, result.validEntries.size)
        assertEquals("919876543210", result.validEntries[0].number)
    }

    @Test
    fun testContactsWrapper() {
        val json = """{"contacts": [{"name": "Furina", "number": "+919876543210"}]}"""
        val result = ContactParser.parseContacts(json)
        assertEquals(1, result.validEntries.size)
    }

    @Test
    fun testDataWrapper() {
        val json = """{"data": [{"name": "Furina", "phone": "+919876543210"}]}"""
        val result = ContactParser.parseContacts(json)
        assertEquals(1, result.validEntries.size)
    }

    @Test
    fun testItemsWrapper() {
        val json = """{"items": [{"name": "Furina", "number": "+919876543210"}]}"""
        val result = ContactParser.parseContacts(json)
        assertEquals(1, result.validEntries.size)
    }

    @Test
    fun testMixedFormats() {
        val json = """
            [
              {"name": "Furina", "number": "+919876543210"},
              {"name": "Raiden Shogun", "phone": 919876543211},
              {"name": "Yae Miko", "number": "+919876543212"},
              {"name": "Nahida", "phone": "919876543213"}
            ]
        """.trimIndent()
        val result = ContactParser.parseContacts(json)
        assertEquals(4, result.validEntries.size)
    }

    @Test
    fun testMalformedContact() {
        val json = """
            [
              {"name": "Furina", "number": "+919876543210"},
              {"name": "Invalid Contact"},
              {"name": "Raiden Shogun", "phone": "+919876543211"}
            ]
        """.trimIndent()
        val result = ContactParser.parseContacts(json)
        assertEquals(2, result.validEntries.size)
        assertEquals(1, result.failures.size)
    }

    @Test
    fun testDuplicateAcrossFieldNames() {
        val json = """
            [
              {"name": "Furina", "number": "+919876543210"},
              {"name": "Furina", "phone": "+919876543210"}
            ]
        """.trimIndent()
        val result = ContactParser.parseContacts(json)
        // Parser should return both as valid entries; duplicate detection is handled by ViewModel
        assertEquals(2, result.validEntries.size)
    }
}
