package com.example.contactimporter.data

import android.util.Log
import kotlinx.serialization.json.*
import java.io.InputStream

object ContactParser {
    private const val TAG = "ContactParser"

    data class ParseResult(
        val total: Int,
        val parsed: Int,
        val validEntries: List<ContactImportEntry>,
        val failures: List<ImportFailure>
    )

    fun parseContacts(inputStream: InputStream): ParseResult {
        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        val root = try {
            json.decodeFromStream<JsonElement>(inputStream)
        } catch (e: Exception) {
            Log.e(TAG, "Invalid JSON structure", e)
            return ParseResult(0, 0, emptyList(), listOf(ImportFailure(null, null, "Invalid JSON format: ${e.message}")))
        }
        return parseContacts(root)
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

        return parseContacts(root)
    }

    private fun parseContacts(root: JsonElement): ParseResult {
        val validEntries = mutableListOf<ContactImportEntry>()
        val failures = mutableListOf<ImportFailure>()

        // Extract contacts array from various root structures
        val jsonArray = extractContactsArray(root)
            ?: return ParseResult(0, 0, emptyList(), listOf(ImportFailure(null, null, "Unsupported JSON structure")))

        val totalCount = jsonArray.size

        for ((index, element) in jsonArray.withIndex()) {
            if (element !is JsonObject) {
                failures.add(ImportFailure(null, null, "Contact at index $index is not a JSON object"))
                continue
            }

            // Case-insensitive field lookup
            val name = getFieldIgnoreCase(element, "name")?.jsonPrimitive?.contentOrNull
            
            // Priority: number > phone (case-insensitive)
            val numberElement = getFieldIgnoreCase(element, "number") 
                ?: getFieldIgnoreCase(element, "phone")
            
            val number = when (numberElement) {
                null -> null
                is JsonPrimitive -> numberElement.content
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

    private fun getFieldIgnoreCase(jsonObject: JsonObject, key: String): JsonElement? {
        return jsonObject.entries.find { it.key.equals(key, ignoreCase = true) }?.value
    }

    /**
     * Extracts the contacts array from various supported JSON root structures.
     */
    private fun extractContactsArray(root: JsonElement): JsonArray? {
        return when (root) {
            is JsonArray -> root
            is JsonObject -> {
                // Check for supported wrapper keys (case-insensitive)
                val wrapperKeys = listOf("contacts", "data", "items")
                for (key in wrapperKeys) {
                    val element = getFieldIgnoreCase(root, key)
                    if (element is JsonArray) {
                        return element
                    }
                    if (element is JsonObject) {
                        return JsonArray(listOf(element))
                    }
                }
                // If object has no recognized wrapper, treat as single contact
                JsonArray(listOf(root))
            }
            else -> null
        }
    }
}