package com.example.contactimporter.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import java.io.InputStream
import java.io.InputStreamReader

object ContactParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Parses JSON file content to a list of ContactData.
     * We use a straightforward read for simplicity. 
     * In a production app with huge files, standard kotlinx.serialization JSON might load it into memory.
     * For 10,000 contacts, string size is approx 2MB, which easily fits in memory.
     */
    fun parseContacts(inputStream: InputStream): List<ContactData> {
        val text = InputStreamReader(inputStream).readText()
        if (text.isBlank()) return emptyList()

        return try {
            json.decodeFromString<List<ContactData>>(text)
        } catch (e: Exception) {
            e.printStackTrace()
            // If it's a single object instead of a list:
            try {
                val single = json.decodeFromString<ContactData>(text)
                listOf(single)
            } catch (e2: Exception) {
                e2.printStackTrace()
                emptyList()
            }
        }
    }
}