package com.example.contactimporter.data

import android.content.ContentProviderOperation
import android.content.Context
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

object ContactsProviderHelper {

    private const val TAG = "ContactsProviderHelper"

    /**
     * Loads existing contacts for quick duplicate detection.
     * We create a simple signature for each contact: "name|phoneNumber"
     * For actual usage we normalize phone numbers (e.g., removing spaces).
     */
    suspend fun loadExistingSignatures(context: Context): Set<String> = withContext(Dispatchers.IO) {
        val signatures = mutableSetOf<String>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (cursor.moveToNext()) {
                val name = if (nameIdx >= 0) cursor.getString(nameIdx) else null
                val phone = if (phoneIdx >= 0) cursor.getString(phoneIdx) else null
                
                if (name != null) {
                    val cleanPhone = phone?.replace(Regex("[\\s\\-\\(\\)]"), "") ?: ""
                    signatures.add("${name.trim().lowercase()}|$cleanPhone")
                }
            }
        }
        signatures
    }

    /**
     * Imports a list of contacts via bulk operations.
     */
    suspend fun importContacts(
        context: Context,
        contacts: List<ContactData>,
        onProgress: (Int) -> Unit
    ): ImportResult = withContext(Dispatchers.IO) {
        
        val existingSignatures = loadExistingSignatures(context)
        
        var imported = 0
        var skipped = 0
        var errors = 0
        val operations = ArrayList<ContentProviderOperation>()

        val batchSize = 100
        var currentRawContactIndex = 0
        var countInBatch = 0

        for ((index, contact) in contacts.withIndex()) {
            // Validation
            if (contact.displayName.isNullOrBlank() && contact.phoneNumbers.isEmpty() && contact.emails.isEmpty()) {
                skipped++
                continue
            }

            // Duplicate detection: check if "Name|Phone1" exists
            val mainPhone = contact.phoneNumbers.firstOrNull()?.replace(Regex("[\\s\\-\\(\\)]"), "") ?: ""
            val signature = "${contact.displayName?.trim()?.lowercase() ?: ""}|$mainPhone"
            
            if (existingSignatures.contains(signature)) {
                skipped++
                continue
            }

            val rawContactInsertIndex = operations.size
            // Create new raw contact
            operations.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build()
            )

            // Name
            if (!contact.displayName.isNullOrBlank()) {
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.displayName)
                        .build()
                )
            }

            // Phones
            for (phone in contact.phoneNumbers) {
                if (phone.isNotBlank()) {
                    operations.add(
                        ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                            .build()
                    )
                }
            }
            
            // Emails
            for (email in contact.emails) {
                if (email.isNotBlank()) {
                    operations.add(
                        ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, email)
                            .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME)
                            .build()
                    )
                }
            }

            countInBatch++
            imported++
            
            // Reached batch size, apply batch
            if (countInBatch >= batchSize) {
                try {
                    context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
                } catch (e: Exception) {
                    Log.e(TAG, "Batch import error", e)
                    errors += countInBatch
                    imported -= countInBatch
                }
                operations.clear()
                countInBatch = 0
                
                withContext(Dispatchers.Main) {
                    onProgress(index + 1)
                }
            }
        }

        // Apply remaining
        if (operations.isNotEmpty()) {
            try {
                context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
            } catch (e: Exception) {
                Log.e(TAG, "Final batch import error", e)
                errors += countInBatch
                imported -= countInBatch
            }
            withContext(Dispatchers.Main) {
                onProgress(contacts.size)
            }
        }

        ImportResult(contacts.size, imported, skipped, errors)
    }
}