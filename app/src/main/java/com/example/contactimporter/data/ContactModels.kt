package com.example.contactimporter.data

import kotlinx.serialization.Serializable

@Serializable
data class ContactData(
    val displayName: String? = null,
    val phoneNumbers: List<String> = emptyList(),
    val emails: List<String> = emptyList()
)

data class ImportResult(
    val totalContacts: Int,
    val importedContacts: Int,
    val duplicatesSkipped: Int,
    val errors: Int
)