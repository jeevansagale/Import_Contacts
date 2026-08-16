package com.example.contactimporter

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.contactimporter.data.ContactData
import com.example.contactimporter.data.ContactParser
import com.example.contactimporter.data.ContactsProviderHelper
import com.example.contactimporter.data.ImportResult
import com.example.contactimporter.theme.ThemeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ImportState {
    object Idle : ImportState()
    object Parsing : ImportState()
    data class ReadyToImport(val contacts: List<ContactData>) : ImportState()
    data class Importing(val progress: Int, val total: Int) : ImportState()
    data class Finished(val result: ImportResult) : ImportState()
    data class Error(val message: String) : ImportState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val themeSettings = ThemeManager.getThemeSettings(application)

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState

    fun parseJsonFile(uri: Uri) {
        _importState.value = ImportState.Parsing
        viewModelScope.launch {
            try {
                val contacts = withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(uri)?.use {
                        ContactParser.parseContacts(it)
                    } ?: emptyList()
                }
                
                if (contacts.isEmpty()) {
                    _importState.value = ImportState.Error("No valid contacts found in the file.")
                } else {
                    _importState.value = ImportState.ReadyToImport(contacts)
                }
            } catch (e: Exception) {
                _importState.value = ImportState.Error("Failed to parse JSON: ${e.message}")
            }
        }
    }

    fun startImport(contacts: List<ContactData>) {
        _importState.value = ImportState.Importing(0, contacts.size)
        viewModelScope.launch {
            try {
                val result = ContactsProviderHelper.importContacts(
                    context = getApplication(),
                    contacts = contacts,
                    onProgress = { current ->
                        _importState.value = ImportState.Importing(current, contacts.size)
                    }
                )
                _importState.value = ImportState.Finished(result)
            } catch (e: Exception) {
                _importState.value = ImportState.Error("Import failed: ${e.message}")
            }
        }
    }

    fun resetState() {
        _importState.value = ImportState.Idle
    }
    
    fun setThemePreset(presetId: String) = viewModelScope.launch {
        ThemeManager.setThemePreset(getApplication(), presetId)
    }

    fun setBackgroundUri(uri: Uri?) = viewModelScope.launch {
        // Need to take persistable URI permission
        uri?.let {
            getApplication<Application>().contentResolver.takePersistableUriPermission(
                it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        ThemeManager.setBackgroundUri(getApplication(), uri?.toString())
    }

    fun setLogoUri(uri: Uri?) = viewModelScope.launch {
        uri?.let {
            getApplication<Application>().contentResolver.takePersistableUriPermission(
                it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        ThemeManager.setLogoUri(getApplication(), uri?.toString())
    }
}