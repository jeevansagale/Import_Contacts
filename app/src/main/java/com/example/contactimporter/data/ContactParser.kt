package com.example.contactimporter.data

import android.util.Log
import kotlinx.serialization.json.*
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charsets

object ContactParser {
    private const val TAG = "ContactParser"

    data class ParseResult(
        val total: Int,
        val parsed: Int,
        val validEntries: List<ContactImportEntry>,
        val failures: List<ImportFailure>
    )

    fun parseContacts(inputStream: InputStream): ParseResult {
        val text = try {
            InputStreamReader(inputStream, Charsets.UTF_8).readText()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading stream", e)
            return ParseResult(0, 0, emptyList(), listOf(ImportFailure(null, null, "File unreadable: ${e.message}")))
        }
        return parseContacts(text)
    }

    fun parseContacts(text: String): ParseResult {
        if (text.isBlank()) {
            return ParseResult(0, 0, emptyList(), emptyList())
        }

        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        val root = try {
            json.parseToJsonElement(text)
        } catch (e: Exception) {
            Log.e(TAG, "Invalid JSON structure", e)
            return ParseResult(0, 0, emptyList(), listOf(ImportFailure(null, null, "Invalid JSON format: ${e.message}")))
        }

        val validEntries = mutableListOf<ContactImportEntry>()
        val failures = mutableListOf<ImportFailure>()
        var totalCount = 0

        // Extract contacts array from various root structures
        val jsonArray = extractContactsArray(root)
            ?: return ParseResult(0, 0, emptyList(), listOf(ImportFailure(null, null, "Unsupported JSON structure: expected array or object with 'contacts', 'data', or 'items' key")))

        totalCount = jsonArray.size

        for ((index, element) in jsonArray.withIndex()) {
            if (element !is JsonObject) {
                failures.add(ImportFailure(null, null, "Contact at index $index is not a JSON object"))
                continue
            }

            // Extract NAME (case-insensitive support)
            val name = element["NAME"]?.jsonPrimitive?.contentOrNull
                ?: element["name"]?.jsonPrimitive?.contentOrNull
                ?: element["Name"]?.jsonPrimitive?.contentOrNull
                ?: element["displayName"]?.jsonPrimitive?.contentOrNull

            // Extract NUMBER - support both "number" and "phone" fields
            // Priority: number > phone
            val numberElement = element["NUMBER"] ?: element["number"] ?: element["Number"]
                ?: element["PHONE"] ?: element["phone"] ?: element["Phone"]
            
            val number = when {
                numberElement == null -> null
                numberElement is JsonPrimitive -> numberElement.content // content handles both "123" and 123
                else -> null
            }

            val trimmedName = name?.trim()
            val trimmedNumber = number?.trim()

            if (trimmedName.isNullOrBlank() && trimmedNumber.isNullOrBlank()) {
                failures.add(ImportFailure(trimmedName, trimmedNumber, "Contact at index $index: Empty record"))
            } else if (trimmedName.isNullOrBlank()) {
                failures.add(ImportFailure(trimmedName, trimmedNumber, "Contact at index $index: NAME is required"))
            } else if (trimmedNumber.isNullOrBlank()) {
                failures.add(ImportFailure(trimmedName, trimmedNumber, "Contact at index $index: NUMBER or PHONE is required"))
            } else {
                validEntries.add(ContactImportEntry(trimmedName, trimmedNumber))
            }
        }

        Log.d(TAG, "Parsed $totalCount records. Valid: ${validEntries.size}, Failures: ${failures.size}")

        return ParseResult(
            total = totalCount,
            parsed = totalCount,
            validEntries = validEntries,
            failures = failures
        )
    }

    /**
     * Extracts the contacts array from various supported JSON root structures.
     * Supported formats:
     * - Root array: [ {...}, {...} ]
     * - Object with "contacts" key: { "contacts": [...] }
     * - Object with "data" key: { "data": [...] }
     * - Object with "items" key: { "items": [...] }
     */
    private fun extractContactsArray(root: JsonElement): JsonArray? = when (root) {
        is JsonArray -> root
        is JsonObject -> {
            // Check for supported wrapper keys in order of preference
            val wrapperKeys = listOf("contacts", "data", "items")
            for (key in wrapperKeys) {
                val element = root[key]
                if (element is JsonArray) {
                    return element
                }
            }
            // If object has no recognized wrapper, treat as single contact
            JsonArray(listOf(root))
        }
        else -> null
    }
}
