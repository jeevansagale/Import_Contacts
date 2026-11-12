package com.example.contactimporter

import com.example.contactimporter.data.ContactParser
import org.junit.Assert.assertEquals
import org.junit.Test

class ContactParserTest {

    @Test
    fun test1_NormalArray() {
        val json = """[{"name": "Furina", "phone": "+919876543210"}]"""
        val result = ContactParser.parseContacts(json)
        assertEquals(1, result.validEntries.size)
    }

    @Test
    fun test2_SingleObject() {
        val json = """{"name": "Furina", "phone": "+919876543210"}"""
        val result = ContactParser.parseContacts(json)
        assertEquals(1, result.validEntries.size)
    }

    @Test
    fun test3_CapitalizedFields() {
        val json = """{"Name": "Furina", "Phone": "+919876543210"}"""
        val result = ContactParser.parseContacts(json)
        assertEquals(1, result.validEntries.size)
    }

    @Test
    fun test4_UppercaseFields() {
        val json = """{"NAME": "Furina", "NUMBER": "+919876543210"}"""
        val result = ContactParser.parseContacts(json)
        assertEquals(1, result.validEntries.size)
    }

    @Test
    fun test5_NumericPhone() {
        val json = """{"name": "Furina", "phone": 919876543210}"""
        val result = ContactParser.parseContacts(json)
        assertEquals(1, result.validEntries.size)
        assertEquals("919876543210", result.validEntries[0].number)
    }

    @Test
    fun test6_ContactsWrapper() {
        val json = """{"contacts": [{"name": "Furina", "phone": "+919876543210"}]}"""
        val result = ContactParser.parseContacts(json)
        assertEquals(1, result.validEntries.size)
    }

    @Test
    fun test7_ContactsWrapperSingleObject() {
        val json = """{"contacts": {"name": "Furina", "phone": "+919876543210"}}"""
        val result = ContactParser.parseContacts(json)
        assertEquals(1, result.validEntries.size)
    }

    @Test
    fun test8_MixedCaseWrapper() {
        val json = """{"CoNtAcTs": [{"NaMe": "Furina", "PhOnE": "+919876543210"}]}"""
        val result = ContactParser.parseContacts(json)
        assertEquals(1, result.validEntries.size)
    }

    @Test
    fun test9_MultipleContacts() {
        val json = """
            [
              {"Name": "Furina", "Phone": "+919876543210"},
              {"NAME": "Raiden Shogun", "NUMBER": "+919876543211"}
            ]
        """.trimIndent()
        val result = ContactParser.parseContacts(json)
        assertEquals(2, result.validEntries.size)
    }

    @Test
    fun test10_OneInvalidContact() {
        val json = """
            [
              {"name": "Furina", "phone": "+919876543210"},
              {"name": "Invalid"},
              {"Name": "Raiden Shogun", "Phone": "+919876543211"}
            ]
        """.trimIndent()
        val result = ContactParser.parseContacts(json)
        assertEquals(2, result.validEntries.size)
        assertEquals(1, result.failures.size)
    }
    @Test
    fun rejectsNonPrimitiveNamesWithoutThrowing() {
        val result = ContactParser.parseContacts("""[{"name":{"value":"Furina"},"phone":"919876543210"}]""")
        assertEquals(0, result.validEntries.size)
        assertEquals(1, result.failures.size)
    }

    @Test
    fun rejectsBooleanDecimalAndExponentPhoneNumbers() {
        val json = """[
            {"name":"Boolean","phone":true},
            {"name":"Decimal","phone":9198765432.5},
            {"name":"Exponent","phone":9.1987654321e9}
        ]"""
        val result = ContactParser.parseContacts(json)
        assertEquals(0, result.validEntries.size)
        assertEquals(3, result.failures.size)
    }

    @Test
    fun rejectsPhoneNumbersWithMoreThanFifteenDigits() {
        val result = ContactParser.parseContacts("""{"name":"Long","phone":"1234567890123456"}""")
        assertEquals(0, result.validEntries.size)
        assertEquals(1, result.failures.size)
    }
}
