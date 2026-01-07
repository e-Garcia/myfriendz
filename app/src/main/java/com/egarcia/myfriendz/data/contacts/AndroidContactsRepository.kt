package com.egarcia.myfriendz.data.contacts

import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.core.net.toUri
import com.egarcia.myfriendz.domain.contacts.ContactData
import com.egarcia.myfriendz.domain.contacts.ContactsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Android implementation of [ContactsRepository] that uses the platform ContentResolver.
 * Runs queries on Dispatchers.IO to avoid blocking the main thread.
 */
class AndroidContactsRepository @Inject constructor(
    private val resolver: ContentResolver
) : ContactsRepository {

    override suspend fun fetchContactDetails(contactUriString: String): ContactData? =
        withContext(Dispatchers.IO) {
            val contactUri = contactUriString.toUri()
            val projection = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
            )

            resolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                    val phone = readFirstPhoneNumber(id)
                    val email = readFirstEmail(id)
                    return@withContext ContactData(name ?: "", phone, email)
                }
            }
            return@withContext null
        }

    private fun readFirstPhoneNumber(contactId: String): String? {
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            "${ContactsContract.CommonDataKinds.Phone.TYPE} ASC"
        )

        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
            }
        }
        return null
    }

    private fun readFirstEmail(contactId: String): String? {
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contactId),
            "${ContactsContract.CommonDataKinds.Email.TYPE} ASC"
        )

        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS))
            }
        }
        return null
    }
}
