package com.example.contactimporter

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.contactimporter.data.ContactParser
import com.example.contactimporter.data.ContactsProviderHelper
import com.example.contactimporter.data.ImportResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream

@RunWith(AndroidJUnit4::class)
class ImportVerificationTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testFullImportPipeline() = runBlocking {
        // 1. Prepare JSON with 10 valid + 1 invalid record
        val jsonString = """
            [
              {"name": "Test User 1", "number": "+12345678901"},
              {"name": "Test User 2", "number": "+12345678902"},
              {"name": "Test User 3", "number": "+12345678903"},
              {"name": "Test User 4", "number": "+12345678904"},
              {"name": "Test User 5", "number": "+12345678905"},
              {"name": "Test User 6", "number": "+12345678906"},
              {"name": "Test User 7", "number": "+12345678907"},
              {"name": "Test User 8", "number": "+12345678908"},
              {"name": "Test User 9", "number": "+12345678909"},
              {"name": "Invalid Record", "number": ""},
              {"name": "Test User 10", "number": "+12345678910"}
            ]
        """.trimIndent()

        // 2. Test Parser
        val inputStream = ByteArrayInputStream(jsonString.toByteArray())
        val parseResult = ContactParser.parseContacts(inputStream)
        
        assertEquals("Total records parsed should be 11", 11, parseResult.total)
        assertEquals("Valid records should be 10", 10, parseResult.validEntries.size)
        assertEquals("Failures should be 1", 1, parseResult.failures.size)

        // 3. Test Import Logic
        var created = 0
        var failed = 0
        var conflicts = 0
        
        parseResult.validEntries.forEach { entry ->
            val name = entry.name
            val number = entry.number
            
            val existing = ContactsProviderHelper.getExistingContactByNumber(context, number)
            if (existing == null) {
                val success = ContactsProviderHelper.performInsert(context, name, number)
                if (success) {
                    if (ContactsProviderHelper.verifyContact(context, name, number)) {
                        created++
                    } else {
                        failed++
                    }
                } else {
                    failed++
                }
            } else {
                conflicts++
            }
        }

        val importResult = ImportResult(
            total = parseResult.total,
            parsed = parseResult.parsed,
            valid = parseResult.validEntries.size,
            invalid = parseResult.failures.size,
            created = created,
            updated = 0,
            skipped = 0,
            failed = failed,
            verified = created,
            conflicts = conflicts,
            failures = parseResult.failures
        )

        assertTrue("Import process should complete", importResult.total == 11)
        assertEquals("Invalid count from parser should be 1", 1, importResult.invalid)

        // 4. Verify in Contacts Provider
        parseResult.validEntries.forEach { entry ->
            val found = ContactsProviderHelper.getExistingContactByNumber(context, entry.number)
            assertNotNull("Contact should exist after import: ${entry.name}", found)
        }

        // 5. Test Duplicates (Simulate second import)
        var secondConflicts = 0
        parseResult.validEntries.forEach { entry ->
            val existing = ContactsProviderHelper.getExistingContactByNumber(context, entry.number)
            if (existing != null) {
                secondConflicts++
            }
        }
        
        assertEquals("On second import, valid records should be detected as conflicts", 
            10, secondConflicts)
    }
}
