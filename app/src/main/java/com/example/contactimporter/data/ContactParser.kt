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
            Log.w(TAG, "Invalid JSON input")
            return ParseResult(0, 0, emptyList(), listOf(ImportFailure(null, null, "Invalid JSON format")))
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
            Log.w(TAG, "Invalid JSON input")
            return ParseResult(0, 0, emptyList(), listOf(ImportFailure(null, null, "Invalid JSON format")))
        }

        return parseContacts(root)
    }

    private fun parseContacts(root: JsonElement): ParseResult {
        val validEntries = mutableListOf<ContactImportEntry>()
        val failures = mutableListOf<ImportFailure>()

        val jsonArray = extractContactsArray(root)
            ?: return ParseResult(0, 0, emptyList(), listOf(ImportFailure(null, null, "Unsupported JSON structure")))

        val totalCount = jsonArray.size

        for ((index, element) in jsonArray.withIndex()) {
            val contactIndex = index + 1
            if (element !is JsonObject) {
                failures.add(ImportFailure(null, null, "Contact #$contactIndex is not a JSON object"))
                continue
            }

            // 1. Name Validation
            val nameElement = getFieldIgnoreCase(element, "name")
            val rawName = (nameElement as? JsonPrimitive)?.takeIf { it.isString }?.content
            val trimmedName = rawName?.trim()

            if (trimmedName.isNullOrEmpty()) {
                val reason = when {
                    nameElement == null -> "Missing name"
                    nameElement !is JsonPrimitive || !nameElement.isString -> "Name must be a string"
                    else -> "Name is empty or only whitespace"
                }
                logFailure(contactIndex, null, null, reason)
                failures.add(ImportFailure(null, null, "Contact #$contactIndex: $reason"))
                continue
            }

            // 2. Phone Type Validation
            val numberElement = getFieldIgnoreCase(element, "number") ?: getFieldIgnoreCase(element, "phone")
            
            if (numberElement == null || numberElement is JsonNull) {
                logFailure(contactIndex, trimmedName, null, "Missing phone number")
                failures.add(ImportFailure(trimmedName, null, "Contact #$contactIndex: Missing phone number"))
                continue
            }

            if (numberElement !is JsonPrimitive) {
                val type = when(numberElement) {
                    is JsonObject -> "Object"
                    is JsonArray -> "Array"
                    else -> "Unknown"
                }
                logFailure(contactIndex, trimmedName, numberElement.toString(), "Unsupported JSON type ($type)")
                failures.add(ImportFailure(trimmedName, numberElement.toString(), "Contact #$contactIndex: Unsupported JSON type ($type)"))
                continue
            }

            // Accept strings and unsigned integer literals only. Exponent notation,
            // decimals, booleans and negative JSON numbers are ambiguous phone inputs.
            val rawNumber = numberElement.content
            if (!numberElement.isString && !rawNumber.matches(Regex("[0-9]+"))) {
                logFailure(contactIndex, trimmedName, null, "Unsupported phone number type")
                failures.add(ImportFailure(trimmedName, null, "Contact #$contactIndex: Unsupported phone number type"))
                continue
            }
            
            // 3. Normalize Valid Numbers
            val normalizedNumber = ContactsProviderHelper.normalizePhoneNumber(rawNumber)

            // 4. Validate Normalized Number
            val validationError = validateNormalizedNumber(normalizedNumber)
            if (validationError != null) {
                logFailure(contactIndex, trimmedName, rawNumber, validationError)
                failures.add(ImportFailure(trimmedName, rawNumber, "Contact #$contactIndex: $validationError"))
                continue
            }

            validEntries.add(ContactImportEntry(trimmedName, normalizedNumber))
        }

        Log.d(TAG, "Parsed $totalCount records. Valid: ${validEntries.size}, Failures: ${failures.size}")

        return ParseResult(
            total = totalCount,
            parsed = totalCount,
            validEntries = validEntries,
            failures = failures
        )
    }

    private fun validateNormalizedNumber(number: String): String? {
        if (number.isEmpty()) return "Phone number became empty after normalization"
        
        val digitsOnly = if (number.startsWith("+")) number.substring(1) else number
        
        if (digitsOnly.any { !it.isDigit() }) {
            return "Invalid characters in phone number (only digits and leading + allowed)"
        }
        
        if (digitsOnly.length < 7) return "Phone number too short (minimum 7 digits)"
        if (digitsOnly.length > 15) return "Phone number too long (maximum 15 digits)"
        
        return null
    }

    private fun logFailure(index: Int, name: String?, rawNumber: String?, reason: String) {
        // Never write contact names or phone numbers to Logcat.
        Log.w(TAG, "Skipped contact #$index: $reason")
    }

    private fun getFieldIgnoreCase(jsonObject: JsonObject, key: String): JsonElement? {
        return jsonObject.entries.find { it.key.equals(key, ignoreCase = true) }?.value
    }

    private fun extractContactsArray(root: JsonElement): JsonArray? {
        return when (root) {
            is JsonArray -> root
            is JsonObject -> {
                val wrapperKeys = listOf("contacts", "data", "items")
                for (key in wrapperKeys) {
                    val element = getFieldIgnoreCase(root, key)
                    if (element is JsonArray) return element
                    if (element is JsonObject) return JsonArray(listOf(element))
                }
                JsonArray(listOf(root))
            }
            else -> null
        }
    }
}
