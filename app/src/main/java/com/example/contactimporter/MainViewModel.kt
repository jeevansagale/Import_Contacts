package com.example.contactimporter

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contactimporter.data.ContactConflict
import com.example.contactimporter.data.ContactImportEntry
import com.example.contactimporter.data.ContactParser
import com.example.contactimporter.data.ContactsProviderHelper
import com.example.contactimporter.data.ImportResult
import com.example.contactimporter.data.ImportFailure
import com.example.contactimporter.data.ThemeConfig
import com.example.contactimporter.data.ThemeRepository
import com.example.contactimporter.data.ConflictResolution
import com.example.contactimporter.data.CharacterMetadata
import com.example.contactimporter.data.CharacterMetadataLoader
import kotlinx.coroutines.Dispatchers
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ImportState {
    object Idle : ImportState()
    object Parsing : ImportState()
    data class ReadyToImport(
        val entries: List<ContactImportEntry>,
        val total: Int,
        val validCount: Int,
        val invalidCount: Int
    ) : ImportState()
    data class Importing(val progress: Int, val total: Int) : ImportState()
    data class Conflict(val conflict: ContactConflict) : ImportState()
    data class Finished(val result: ImportResult) : ImportState()
    data class Error(val message: String) : ImportState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val themeRepository = ThemeRepository(application)
    val themeConfig: StateFlow<ThemeConfig> = themeRepository.themeConfig
    
    // Metadata as StateFlow for reactive Compose UI
    val availableCharacters: StateFlow<List<CharacterMetadata>> = themeRepository.availableCharacters
    val characterLoadError: String? = themeRepository.characterLoadError
    
    init {
        Log.d("MainViewModel", "ViewModel created. Characters: ${availableCharacters.value.size}")
    }

    fun getActiveCharacter() = themeRepository.getActiveCharacter()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState

    private var currentEntries: List<ContactImportEntry> = emptyList()
    private var currentIndex = 0
    private var createdCount = 0
    private var updatedCount = 0
    private var skippedCount = 0
    private var failedCount = 0
    private var verifiedCount = 0
    
    private var totalRecords = 0
    private var invalidRecords = 0
    private var parserFailures: List<ImportFailure> = emptyList()
    
    private var applyToAllDecision: ConflictResolution? = null

    // LOCKED: Import Pipeline Logic
    fun parseJsonFile(uri: Uri) {
        _importState.value = ImportState.Parsing
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(uri)?.use {
                        ContactParser.parseContacts(it)
                    }
                }
                handleParseResult(result)
            } catch (e: Exception) {
                _importState.value = ImportState.Error("Failed to read file: ${e.message}")
            }
        }
    }

    fun parseJsonString(json: String) {
        _importState.value = ImportState.Parsing
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    ContactParser.parseContacts(json)

                }
                handleParseResult(result)
            } catch (e: Exception) {
                _importState.value = ImportState.Error("Failed to parse JSON: ${e.message}")
            }
        }
    }

    private fun handleParseResult(result: ContactParser.ParseResult?) {
        if (result == null || (result.validEntries.isEmpty() && result.failures.isNotEmpty())) {
            val errorMsg = result?.failures?.firstOrNull()?.reason ?: "No valid contacts found."
            _importState.value = ImportState.Error(errorMsg)
        } else if (result.validEntries.isEmpty()) {
            _importState.value = ImportState.Error("The input contains no contacts.")
        } else {
            currentEntries = result.validEntries
            totalRecords = result.total
            invalidRecords = result.failures.size
            parserFailures = result.failures
            
            _importState.value = ImportState.ReadyToImport(
                entries = result.validEntries,
                total = result.total,
                validCount = result.validEntries.size,
                invalidCount = result.failures.size
            )
        }
    }

    // LOCKED: Import Pipeline Logic
    fun startImport(entries: List<ContactImportEntry>) {
        currentEntries = entries
        currentIndex = 0
        createdCount = 0
        updatedCount = 0
        skippedCount = 0
        failedCount = 0
        verifiedCount = 0
        applyToAllDecision = null
        
        processNext()
    }

    // LOCKED: Import Pipeline Logic
    private fun processNext() {
        if (currentIndex >= currentEntries.size) {
            finishImport()
            return
        }

        _importState.value = ImportState.Importing(currentIndex + 1, currentEntries.size)
        
        val entry = currentEntries[currentIndex]
        val name = entry.name ?: ""
        val number = entry.number ?: ""
        val normalizedNumber = ContactsProviderHelper.normalizePhoneNumber(number)

        viewModelScope.launch {
            // Try to find existing contact using normalized number
            val existing = ContactsProviderHelper.getExistingContactByNumber(getApplication(), normalizedNumber)
            
            // Verify match after normalization
            val isMatch = existing != null && 
                          ContactsProviderHelper.normalizePhoneNumber(existing.number) == normalizedNumber

            if (isMatch) {
                if (applyToAllDecision != null) {
                    handleResolution(applyToAllDecision!!, existing!!.id)
                } else {
                    _importState.value = ImportState.Conflict(
                        ContactConflict(
                            existingContactId = existing!!.id,
                            existingName = existing.name,
                            existingNumber = existing.number,
                            incomingName = name,
                            incomingNumber = number
                        )
                    )
                }
            } else {
                val success = ContactsProviderHelper.performInsert(getApplication(), name, number)
                if (success) {
                    if (ContactsProviderHelper.verifyContact(getApplication(), name, number)) {
                        createdCount++
                        verifiedCount++
                    } else {
                        failedCount++
                    }
                } else {
                    failedCount++
                }
                currentIndex++
                processNext()
            }
        }
    }

    // LOCKED: Import Pipeline Logic
    fun resolveConflict(resolution: ConflictResolution, applyToAll: Boolean) {
        if (applyToAll) {
            applyToAllDecision = resolution
        }
        
        val state = _importState.value
        if (state is ImportState.Conflict) {
            viewModelScope.launch {
                handleResolution(resolution, state.conflict.existingContactId)
            }
        }
    }

    // LOCKED: Import Pipeline Logic
    private suspend fun handleResolution(resolution: ConflictResolution, contactId: Long) {
        val entry = currentEntries[currentIndex]
        val name = entry.name ?: ""
        val number = entry.number ?: ""

        when (resolution) {
            ConflictResolution.OVERWRITE -> {
                val success = ContactsProviderHelper.performUpdate(getApplication(), contactId, name, number)
                if (success) {
                    if (ContactsProviderHelper.verifyContact(getApplication(), name, number)) {
                        updatedCount++
                        verifiedCount++
                    } else {
                        failedCount++
                    }
                } else {
                    failedCount++
                }
            }
            ConflictResolution.SKIP -> {
                skippedCount++
            }
        }
        
        currentIndex++
        processNext()
    }

    private fun finishImport() {
        _importState.value = ImportState.Finished(
            ImportResult(
                total = totalRecords,
                parsed = totalRecords,
                valid = currentEntries.size,
                invalid = invalidRecords,
                created = createdCount,
                updated = updatedCount,
                skipped = skippedCount,
                failed = failedCount,
                verified = verifiedCount,
                failures = parserFailures
            )
        )
    }

    fun resetState() {
        _importState.value = ImportState.Idle
        currentIndex = 0
        applyToAllDecision = null
    }
    
    // Theme System Updates
    fun setThemePreset(presetId: String) {
        val current = themeConfig.value
        themeRepository.updateConfig(current.copy(characterId = presetId))
    }

    fun toggleTheme(enabled: Boolean) {
        val current = themeConfig.value
        themeRepository.updateConfig(current.copy(themeEnabled = enabled))
    }
}
