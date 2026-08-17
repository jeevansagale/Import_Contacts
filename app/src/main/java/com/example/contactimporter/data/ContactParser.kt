package com.example.contactimporter.data

import android.util.Log
import kotlinx.serialization.json.*
import java.io.InputStream
import java.io.InputStreamReader

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

        val jsonArray = when (root) {
            is JsonArray -> root
            is JsonObject -> JsonArray(listOf(root))
            else -> {
                return ParseResult(0, 0, emptyList(), listOf(ImportFailure(null, null, "Root must be an array or object")))
            }
        }

        totalCount = jsonArray.size

        for (element in jsonArray) {
            if (element !is JsonObject) {
                failures.add(ImportFailure(null, null, "Entry is not a JSON object"))
                continue
            }

            // Extract NAME (case-insensitive support)
            val name = element["NAME"]?.jsonPrimitive?.contentOrNull
                ?: element["name"]?.jsonPrimitive?.contentOrNull
                ?: element["Name"]?.jsonPrimitive?.contentOrNull
                ?: element["displayName"]?.jsonPrimitive?.contentOrNull

            // Extract NUMBER (case-insensitive support, handles String and Number types)
            val numberElement = element["NUMBER"] ?: element["number"] ?: element["Number"]
            val number = when {
                numberElement == null -> null
                numberElement is JsonPrimitive -> numberElement.content // content handles both "123" and 123
                else -> null
            }

            val trimmedName = name?.trim()
            val trimmedNumber = number?.trim()

            if (trimmedName.isNullOrBlank() && trimmedNumber.isNullOrBlank()) {
                failures.add(ImportFailure(trimmedName, trimmedNumber, "Empty record"))
            } else if (trimmedName.isNullOrBlank()) {
                failures.add(ImportFailure(trimmedName, trimmedNumber, "NAME is required"))
            } else if (trimmedNumber.isNullOrBlank()) {
                failures.add(ImportFailure(trimmedName, trimmedNumber, "NUMBER is required"))
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
}
