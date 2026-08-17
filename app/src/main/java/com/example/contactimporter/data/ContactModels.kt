package com.example.contactimporter.data

/**
 * Domain model for a contact being imported.
 */
data class ContactImportEntry(
    val name: String?,
    val number: String?
)

/**
 * Represents a conflict between an incoming contact and an existing one.
 */
data class ContactConflict(
    val existingContactId: Long,
    val existingName: String,
    val existingNumber: String,
    val incomingName: String,
    val incomingNumber: String
)

/**
 * Decision for resolving a conflict.
 */
enum class ConflictResolution {
    OVERWRITE,
    SKIP
}

/**
 * Detailed result of the import operation.
 */
data class ImportResult(
    val total: Int,           // Total records in the file
    val parsed: Int,          // Total records successfully decoded from JSON
    val valid: Int,           // Records passing validation
    val invalid: Int,         // Records missing name or number
    val created: Int,         // New contacts created
    val updated: Int,         // Existing contacts updated
    val skipped: Int,         // Contacts skipped by user
    val failed: Int,          // Records where insertion or verification failed
    val verified: Int,        // Records confirmed to exist after operation
    val alreadyExists: Int = 0, // Records that already existed in the provider
    val failures: List<ImportFailure> = emptyList()
)

data class ImportFailure(
    val name: String?,
    val number: String?,
    val reason: String
)
