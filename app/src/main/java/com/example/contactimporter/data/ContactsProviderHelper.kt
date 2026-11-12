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
     * Normalizes a phone number for comparison and storage.
     * Trim whitespace, removes spaces, dashes, and parentheses.
     * Keeps a leading '+' if present.
     */
    fun normalizePhoneNumber(number: String): String {
        val trimmed = number.trim()
        if (trimmed.isEmpty()) return ""
        
        val startsWithPlus = trimmed.startsWith("+")
        val rest = if (startsWithPlus) trimmed.substring(1) else trimmed
        // Remove spaces, dashes, and parentheses
        val normalizedRest = rest.replace(Regex("[ \\-()]"), "")
        
        return if (startsWithPlus) "+$normalizedRest" else normalizedRest
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
        
        // We use the normalized number for lookup if possible, 
        // but PhoneLookup generally handles various formats.
        // However, to ensure consistency with our requirements, we compare results.
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
                val normalizedTarget = normalizePhoneNumber(number)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)) ?: ""
                    val foundNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.NUMBER)) ?: ""
                    
                    if (normalizePhoneNumber(foundNumber) == normalizedTarget) {
                        return@use ExistingContact(id, name, foundNumber)
                    }
                }
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "PhoneLookup query failed", e)
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
            Log.e(TAG, "Contact insert failed", e)
            false
        }
    }

    /**
     * Updates an existing contact.
     */
    suspend fun performUpdate(context: Context, contactId: Long, newName: String, newNumber: String): Boolean = withContext(Dispatchers.IO) {
        if (contactId <= 0L || newName.isBlank() || newNumber.isBlank()) return@withContext false

        // Resolve the exact phone data row first. A contact may contain multiple
        // phone numbers; updating every Phone row would overwrite unrelated numbers.
        val projection = arrayOf(
            ContactsContract.Data._ID,
            ContactsContract.Data.RAW_CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val selection = "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?" 
        val selectionArgs = arrayOf(
            contactId.toString(),
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
        )

        var phoneDataId: Long? = null
        var rawContactId: Long? = null
        try {
            context.contentResolver.query(
                ContactsContract.Data.CONTENT_URI, projection, selection, selectionArgs, null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data._ID)
                val rawIdColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.RAW_CONTACT_ID)
                val numberColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val target = normalizePhoneNumber(newNumber)
                while (cursor.moveToNext()) {
                    val currentNumber = cursor.getString(numberColumn).orEmpty()
                    if (normalizePhoneNumber(currentNumber) == target) {
                        phoneDataId = cursor.getLong(idColumn)
                        rawContactId = cursor.getLong(rawIdColumn)
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Contact update lookup failed", e)
            return@withContext false
        }

        val dataId = phoneDataId ?: return@withContext false
        val rawId = rawContactId ?: return@withContext false
        val ops = arrayListOf(
            ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                    "${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                    arrayOf(rawId.toString(), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                )
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, newName)
                .build(),
            ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                    "${ContactsContract.Data._ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                    arrayOf(dataId.toString(), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                )
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, newNumber)
                .build()
        )

        try {
            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Contact update failed", e)
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
