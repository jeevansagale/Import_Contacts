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

    /**
     * Loads the character library from assets with high fault tolerance and detailed diagnostics.
     * One invalid character must NEVER prevent the rest from loading.
     */
    fun loadRegistry(context: Context): List<CharacterMetadata> {
        lastError = null
        val characters = mutableListOf<CharacterMetadata>()
        val skipped = mutableListOf<Pair<String, String>>()

        Log.d(TAG, "=== CHARACTER LIBRARY LOAD START ===")
        Log.d(TAG, "Registry path: assets/$ASSET_ROOT/registry.json")

        // 1. Load registry.json
        val registryPath = "$ASSET_ROOT/registry.json"
        val registryText = try {
            context.assets.open(registryPath).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            val msg = "Registry not found: assets/$registryPath — ${e.message}"
            Log.e(TAG, msg, e)
            lastError = msg
            Log.e(TAG, "=== CHARACTER LIBRARY LOAD FAILED ===")
            return emptyList()
        }

        Log.d(TAG, "Registry found: YES")
        Log.d(TAG, "Registry size: ${registryText.length} bytes")

        // 2. Parse registry.json
        val registryJson = try {
            json.parseToJsonElement(registryText).jsonObject
        } catch (e: Exception) {
            val msg = "Failed to parse registry.json: ${e.message}"
            Log.e(TAG, msg)
            e.printStackTrace()
            lastError = msg
            Log.e(TAG, "=== CHARACTER LIBRARY LOAD FAILED ===")
            return emptyList()
        }

        val characterEntries = registryJson["characters"]?.jsonArray
        if (characterEntries == null) {
            val msg = "Registry.json missing 'characters' array"
            Log.e(TAG, msg)
            lastError = msg
            Log.e(TAG, "=== CHARACTER LIBRARY LOAD FAILED ===")
            return emptyList()
        }

        val totalInRegistry = characterEntries.size
        Log.d(TAG, "Registry entries: $totalInRegistry")
        Log.d(TAG, "")

        // 3. Load each character
        characterEntries.forEach { element ->
            val path = element.jsonObject["path"]?.jsonPrimitive?.content ?: "unknown"
            val charId = element.jsonObject["id"]?.jsonPrimitive?.content ?: "unknown"

            Log.d(TAG, "--- Loading: $charId ---")
            Log.d(TAG, "  path = $path")

            val charFolder = "$ASSET_ROOT/$path"
            val metadataPath = "$charFolder/metadata.json"

            try {
                // 3a. Fetch metadata file
                val metadataText = try {
                    context.assets.open(metadataPath).bufferedReader().use { it.readText() }
                } catch (e: IOException) {
                    Log.e(TAG, "  metadata: MISSING ($metadataPath)")
                    Log.e(TAG, "  Exception: ${e.message}")
                    skipped.add(charId to "Missing metadata.json")
                    return@forEach
                }
                Log.d(TAG, "  metadata: FOUND (${metadataText.length} bytes)")
                Log.d(TAG, "  first 200 chars: ${metadataText.take(200)}")

                // 3b. Parse metadata
                val metadata = try {
                    json.decodeFromString<CharacterMetadata>(metadataText)
                } catch (e: SerializationException) {
                    Log.e(TAG, "  metadata: PARSE FAILED")
                    Log.e(TAG, "  Reason: ${e.message}")
                    e.printStackTrace()
                    skipped.add(charId to "Parse Error")
                    return@forEach
                } catch (e: Exception) {
                    Log.e(TAG, "  metadata: PARSE FAILED (unexpected)")
                    Log.e(TAG, "  Reason: ${e.message}")
                    e.printStackTrace()
                    skipped.add(charId to "Parse Error")
                    return@forEach
                }
                Log.d(TAG, "  metadata: PARSED OK (id=${metadata.id}, name=${metadata.name})")

                // 3c. Validate required fields
                if (metadata.id.isBlank() || metadata.name.isBlank()) {
                    Log.e(TAG, "  metadata: VALIDATION FAILED (empty id or name)")
                    skipped.add(charId to "Validation Error (empty id/name)")
                    return@forEach
                }

                // 3d. Resolve image paths (absolute asset URIs)
                val resolvedMetadata = metadata.copy(
                    images = metadata.images.copy(
                        hero = "file:///android_asset/$charFolder/${metadata.images.hero}",
                        portrait = "file:///android_asset/$charFolder/${metadata.images.portrait}",
                        background = "file:///android_asset/$charFolder/${metadata.images.background}"
                    )
                )

                // 3e. Check images (advisory only — do NOT exclude character)
                val missingImages = checkImages(context, resolvedMetadata)
                if (missingImages.isNotEmpty()) {
                    Log.w(TAG, "  images: MISSING ${missingImages.joinToString(", ")}")
                    Log.w(TAG, "  (character will still be included)")
                } else {
                    Log.d(TAG, "  images: ALL VERIFIED")
                }

                // 3f. Add character to list
                characters.add(resolvedMetadata)
                Log.d(TAG, "  status: LOADED ✓")

            } catch (e: Exception) {
                Log.e(TAG, "  UNEXPECTED ERROR loading $path", e)
                e.printStackTrace()
                skipped.add(charId to "Unexpected Error")
            }
        }

        // 4. Final summary
        Log.d(TAG, "")
        Log.d(TAG, "===================================")
        Log.d(TAG, "Successfully loaded:")
        if (characters.isNotEmpty()) {
            characters.sortedBy { it.displayOrder }.forEach { c ->
                Log.d(TAG, "  ✓ ${c.name} (${c.source})")
            }
        } else {
            Log.d(TAG, "  (none)")
        }
        if (skipped.isNotEmpty()) {
            Log.d(TAG, "Failed:")
            skipped.forEach { (id, reason) ->
                val displayName = id.split("-").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                Log.d(TAG, "  ✗ $displayName ($reason)")
            }
        } else {
            Log.d(TAG, "Failed: <none>")
        }
        Log.d(TAG, "")
        Log.d(TAG, "Final character count: ${characters.size}")
        Log.d(TAG, "===================================")

        return characters.sortedBy { it.displayOrder }
    }

    /**
     * Checks which images are missing but does NOT exclude the character.
     * Returns a list of missing image type names (empty = all present).
     */
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
                Log.w(TAG, "    Missing $type image: $assetPath")
                missing.add(type)
            }
        }
        return missing
    }
}