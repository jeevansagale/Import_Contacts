package com.example.contactimporter

import com.example.contactimporter.data.ContactParser
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream

class ContactParserTest {

    @Test
    fun testParseMultipleContactsWithMixedData() {
        val jsonString = """
            [
              {"name": "Rahul Sharma", "number": "+919876543210"},
              {"name": "Priya Patil", "number": "+919876543211"},
              {"name": "Amit Shah", "number": "+919876543212"},
              {"name": "Sita Ram", "number": "+919876543213"},
              {"name": "Gopal Das", "number": "+919876543214"},
              {"name": "Laxmi Bai", "number": "+919876543215"},
              {"name": "Vikram Singh", "number": "+919876543216"},
              {"name": "Anjali Devi", "number": "+919876543217"},
              {"name": "Rohan Mehra", "number": "+919876543218"},
              {"name": "Invalid One", "number": ""},
              {"name": "Kiran Rao", "number": "+919876543219"}
            ]
        """.trimIndent()

        val inputStream = ByteArrayInputStream(jsonString.toByteArray())
        val result = ContactParser.parseContacts(inputStream)

        // Verify total records (includes invalid ones)
        assertEquals(11, result.total)
        
        // Verify valid entries (those with both name and number)
        assertEquals(10, result.validEntries.size)
        
        // Verify failures
        assertEquals(1, result.failures.size)
        assertEquals("NUMBER is required", result.failures[0].reason)
        
        // Verify first and last valid entry mapping
        assertEquals("Rahul Sharma", result.validEntries[0].name)
        assertEquals("+919876543210", result.validEntries[0].number)
        assertEquals("Kiran Rao", result.validEntries.last().name)
        assertEquals("+919876543219", result.validEntries.last().number)
    }

    @Test
    fun testParseEmptyJson() {
        val jsonString = "[]"
        val inputStream = ByteArrayInputStream(jsonString.toByteArray())
        val result = ContactParser.parseContacts(inputStream)
        assertEquals(0, result.total)
        assertEquals(0, result.validEntries.size)
    }
}
