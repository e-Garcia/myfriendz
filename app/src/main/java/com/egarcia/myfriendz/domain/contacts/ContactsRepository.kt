package com.egarcia.myfriendz.domain.contacts

/**
 * Platform-agnostic contact data DTO and repository interface.
 * The repository takes a contact Uri as a String so the domain layer
 * remains free of Android framework types.
 */

data class ContactData(
    val name: String,
    val phone: String?,
    val email: String?
)

interface ContactsRepository {
    /**
     * Fetch contact details for the given contact Uri represented as String (Uri.toString()).
     * Implementations are responsible for handling Android APIs and should run on an IO dispatcher.
     */
    suspend fun fetchContactDetails(contactUriString: String): ContactData?
}
