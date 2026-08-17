package com.example.contactimporter.data

import android.content.Context
import android.util.Log
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import java.io.IOException

object CharacterMetadataLoader {
    private const val TAG = "CharacterMetadataLoader"
    private const val ASSET_ROOT = "characters"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = false // Requirement: strict parsing to catch errors
    }

    var lastError: String? = null
        private set

    /**
     * Loads the character library from assets with high fault tolerance and detailed diagnostics.
     */
    fun loadRegistry(context: Context): List<CharacterMetadata> {
        lastError = null
        val characters = mutableListOf<CharacterMetadata>()
        val skipped = mutableListOf<Pair<String, String>>()
        
        Log.d(TAG, "Loading registry...")
        
        val registryPath = "$ASSET_ROOT/registry.json"
        val registryText = try {
            context.assets.open(registryPath).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            val msg = "Registry not found: assets/$registryPath"
            Log.e(TAG, msg, e)
            lastError = msg
            return emptyList()
        }

        Log.d(TAG, "✓ registry.json loaded")

        val registryJson = try {
            json.parseToJsonElement(registryText).jsonObject
        } catch (e: Exception) {
            val msg = "Failed to parse registry.json: ${e.message}"
            Log.e(TAG, msg)
            e.printStackTrace()
            lastError = msg
            return emptyList()
        }

        val characterEntries = registryJson["characters"]?.jsonArray ?: return emptyList()
        val totalInRegistry = characterEntries.size

        characterEntries.forEach { element ->
            val path = element.jsonObject["path"]?.jsonPrimitive?.content ?: "unknown"
            val charId = element.jsonObject["id"]?.jsonPrimitive?.content ?: "unknown"
            
            Log.d(TAG, "-----------------------------------")
            Log.d(TAG, "Loading $path...")
            
            val charFolder = "$ASSET_ROOT/$path"
            val metadataPath = "$charFolder/metadata.json"
            
            try {
                // 1. Metadata fetch
                val metadataText = try {
                    context.assets.open(metadataPath).bufferedReader().use { it.readText() }
                } catch (e: IOException) {
                    Log.e(TAG, "Failed finding metadata: $metadataPath")
                    skipped.add(charId to "Missing metadata.json")
                    return@forEach
                }
                Log.d(TAG, "✓ metadata found")

                // 2. Metadata parse & Validation against CharacterMetadata data class
                val metadata = try {
                    json.decodeFromString<CharacterMetadata>(metadataText)
                } catch (e: SerializationException) {
                    val errorMsg = "Failed parsing:\n$path/metadata.json\n\nReason:\n${e.message}"
                    Log.e(TAG, errorMsg)
                    e.printStackTrace()
                    skipped.add(charId to "Parse Error")
                    return@forEach
                } catch (e: Exception) {
                    Log.e(TAG, "Failed parsing:\n$path/metadata.json")
                    Log.e(TAG, "Reason: ${e.message}")
                    e.printStackTrace()
                    skipped.add(charId to "Parse Error")
                    return@forEach
                }
                Log.d(TAG, "✓ theme parsed")

                // 3. Image validation
                val resolvedMetadata = metadata.copy(
                    images = metadata.images.copy(
                        hero = "file:///android_asset/$charFolder/${metadata.images.hero}",
                        portrait = "file:///android_asset/$charFolder/${metadata.images.portrait}",
                        background = "file:///android_asset/$charFolder/${metadata.images.background}"
                    )
                )

                if (validateImages(context, resolvedMetadata)) {
                    Log.d(TAG, "✓ images verified")
                    characters.add(resolvedMetadata)
                    Log.d(TAG, "Success: ${resolvedMetadata.name}")
                } else {
                    Log.e(TAG, "Validation failed for $charId (Missing images)")
                    skipped.add(resolvedMetadata.name to "Missing Images")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading character at $path", e)
                e.printStackTrace()
                skipped.add(charId to "Unexpected Error")
            }
        }
        
        Log.d(TAG, "===================================")
        Log.d(TAG, "Loaded ${characters.size}/$totalInRegistry characters.")
        if (skipped.isNotEmpty()) {
            Log.d(TAG, "Skipped:")
            skipped.forEach { (name, reason) ->
                val displayName = if (name.contains("-")) {
                    name.split("-").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                } else name
                Log.d(TAG, "- $displayName ($reason)")
            }
        }
        Log.d(TAG, "===================================")
        
        return characters.sortedBy { it.displayOrder }
    }

    private fun validateImages(context: Context, metadata: CharacterMetadata): Boolean {
        var allExist = true
        val images = listOf(
            "hero" to metadata.images.hero,
            "portrait" to metadata.images.portrait,
            "background" to metadata.images.background
        )

        for ((type, uri) in images) {
            val assetPath = uri.removePrefix("file:///android_asset/")
            try {
                context.assets.open(assetPath).use { it.close() }
            } catch (e: IOException) {
                Log.e(TAG, "Missing $type image: $assetPath")
                allExist = false
            }
        }
        return allExist
    }
}
