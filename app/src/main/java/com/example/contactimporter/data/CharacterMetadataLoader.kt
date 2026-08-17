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
        isLenient = false
    }

    var lastError: String? = null
        private set

    fun loadRegistry(context: Context): List<CharacterMetadata> {
        lastError = null
        val characters = mutableListOf<CharacterMetadata>()
        val skipped = mutableListOf<Pair<String, String>>()

        Log.d(TAG, "=== CHARACTER LIBRARY LOAD START ===")
        Log.d(TAG, "ASSET_ROOT: $ASSET_ROOT")
        
        // Step 6 Check: List root assets to see if 'characters' is even there
        try {
            val rootAssets = context.assets.list("") ?: emptyArray()
            Log.d(TAG, "Root assets found: ${rootAssets.joinToString(", ")}")
            
            // Check if ASSET_ROOT exists in root
            if (!rootAssets.contains(ASSET_ROOT)) {
                Log.e(TAG, "CRITICAL: '$ASSET_ROOT' folder not found in assets root!")
                
                // Debug: List contents of root to see where it might be
                rootAssets.forEach { asset ->
                    val subAssets = context.assets.list(asset)
                    Log.d(TAG, "Asset: $asset, Sub-assets: ${subAssets?.joinToString(", ")}")
                }
            } else {
                Log.d(TAG, "SUCCESS: '$ASSET_ROOT' folder found in assets root.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to list assets: ${e.message}")
        }

        val registryPath = "$ASSET_ROOT/registry.json"
        Log.d(TAG, "Registry path: assets/$registryPath")

        val registryText = try {
            context.assets.open(registryPath).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            val msg = "Registry not found: assets/$registryPath — ${e.message}"
            Log.e(TAG, msg)
            lastError = msg
            Log.e(TAG, "=== CHARACTER LIBRARY LOAD FAILED ===")
            return emptyList()
        }

        Log.d(TAG, "Registry found: YES")
        Log.d(TAG, "Registry content length: ${registryText.length}")

        val registryJson = try {
            json.parseToJsonElement(registryText).jsonObject
        } catch (e: Exception) {
            val msg = "Failed to parse registry.json: ${e.message}"
            Log.e(TAG, msg)
            lastError = msg
            return emptyList()
        }

        val characterEntries = registryJson["characters"]?.jsonArray
        if (characterEntries == null) {
            val msg = "Registry.json missing 'characters' array"
            Log.e(TAG, msg)
            lastError = msg
            return emptyList()
        }

        Log.d(TAG, "Registry entries discovered: ${characterEntries.size}")

        characterEntries.forEach { element ->
            val path = element.jsonObject["path"]?.jsonPrimitive?.content ?: "unknown"
            val charId = element.jsonObject["id"]?.jsonPrimitive?.content ?: "unknown"

            Log.d(TAG, "[Character Pipeline] Processing: $charId")
            
            val charFolder = "$ASSET_ROOT/$path"
            val metadataPath = "$charFolder/metadata.json"
            Log.d(TAG, "  Loading metadata from: assets/$metadataPath")

            try {
                val metadataText = try {
                    context.assets.open(metadataPath).bufferedReader().use { it.readText() }
                } catch (e: IOException) {
                    Log.e(TAG, "  FAILED: metadata.json missing at $metadataPath")
                    skipped.add(charId to "Missing metadata.json")
                    return@forEach
                }
                
                val cleanedMetadataText = if (metadataText.startsWith("\uFEFF")) {
                    Log.d(TAG, "  BOM detected and removed for $charId")
                    metadataText.removePrefix("\uFEFF")
                } else {
                    metadataText
                }

                val metadata = try {
                    json.decodeFromString<CharacterMetadata>(cleanedMetadataText)
                } catch (e: Exception) {
                    Log.e(TAG, "  FAILED: metadata.json parse error: ${e.message}")
                    skipped.add(charId to "Parse error")
                    return@forEach
                }

                if (metadata.id.isBlank()) {
                    Log.e(TAG, "  FAILED: metadata.id is blank")
                    skipped.add(charId to "Invalid ID")
                    return@forEach
                }

                val resolvedMetadata = metadata.copy(
                    images = metadata.images.copy(
                        hero = "file:///android_asset/$charFolder/${metadata.images.hero}",
                        portrait = "file:///android_asset/$charFolder/${metadata.images.portrait}",
                        background = "file:///android_asset/$charFolder/${metadata.images.background}"
                    )
                )

                // Verify Images
                val missing = checkImages(context, resolvedMetadata)
                if (missing.isNotEmpty()) {
                    Log.w(TAG, "  WARNING: Missing assets for ${metadata.name}: ${missing.joinToString(", ")}")
                } else {
                    Log.d(TAG, "  SUCCESS: All assets verified for ${metadata.name}")
                }

                characters.add(resolvedMetadata)
                Log.d(TAG, "  LOADED: ${metadata.name}")

            } catch (e: Exception) {
                Log.e(TAG, "  CRITICAL ERROR loading $charId: ${e.message}")
            }
        }

        Log.d(TAG, "=== CHARACTER LIBRARY SUMMARY ===")
        Log.d(TAG, "Total Loaded: ${characters.size}")
        Log.d(TAG, "Total Skipped: ${skipped.size}")
        Log.d(TAG, "=== LOAD END ===")

        return characters.sortedBy { it.displayOrder }
    }

    private fun checkImages(context: Context, metadata: CharacterMetadata): List<String> {
        val missing = mutableListOf<String>()
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
                missing.add(type)
            }
        }
        return missing
    }
}
