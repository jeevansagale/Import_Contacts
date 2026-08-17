package com.example.contactimporter.data

import android.content.ContentProviderOperation
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ContactsProviderHelper {

    private const val TAG = "ContactsProviderHelper"

    /**
     * Normalizes a phone number for comparison by removing all non-digit characters.
     */
    fun normalizePhoneNumber(number: String): String {
        return number.replace(Regex("[^0-9]"), "")
    }

    /**
     * Data class to hold existing contact info for conflict resolution.
     */
    data class ExistingContact(
        val id: Long,
        val name: String,
        val number: String
    )

    /**
     * Checks if a contact with the given phone number already exists.
     * Returns existing contact details if found.
     */
    suspend fun getExistingContactByNumber(context: Context, number: String): ExistingContact? = withContext(Dispatchers.IO) {
        if (number.isBlank()) return@withContext null
        
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number)
        )
        
        val projection = arrayOf(
            ContactsContract.PhoneLookup._ID,
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.NUMBER
        )
        
        return@withContext try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)) ?: ""
                    val foundNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.NUMBER)) ?: ""
                    ExistingContact(id, name, foundNumber)
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying PhoneLookup for $number", e)
            null
        }
    }

    /**
     * Inserts a new contact.
     */
    suspend fun performInsert(context: Context, name: String, number: String): Boolean = withContext(Dispatchers.IO) {
        val ops = arrayListOf<ContentProviderOperation>()

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
            .build())

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
            .build())

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
            .build())

        try {
            val results = context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            results.size >= 3 && results[0].uri != null
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting contact: $name", e)
            false
        }
    }

    /**
     * Updates an existing contact. We search for the StructuredName and Phone data rows
     * associated with the contact ID and update them.
     */
    suspend fun performUpdate(context: Context, contactId: Long, newName: String, newNumber: String): Boolean = withContext(Dispatchers.IO) {
        val ops = arrayListOf<ContentProviderOperation>()

        // Update Name
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
            .withSelection(
                "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                arrayOf(contactId.toString(), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            )
            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, newName)
            .build())

        // Update Phone
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
            .withSelection(
                "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                arrayOf(contactId.toString(), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            )
            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, newNumber)
            .build())

        try {
            val results = context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            // One or both updates might apply. If results[0].count > 0 or results[1].count > 0 it means something changed.
            // However, a safer check is whether it succeeded without exception.
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating contact ID: $contactId", e)
            false
        }
    }

    /**
     * Verification check after operation.
     */
    suspend fun verifyContact(context: Context, name: String, number: String): Boolean {
        val existing = getExistingContactByNumber(context, number)
        return existing != null && (name.isBlank() || existing.name.contains(name, ignoreCase = true))
    }
}
