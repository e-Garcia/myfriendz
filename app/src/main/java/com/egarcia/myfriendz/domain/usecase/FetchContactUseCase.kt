@file:Suppress("unused")

package com.egarcia.myfriendz.domain.usecase

import com.egarcia.myfriendz.domain.contacts.ContactsRepository
import com.egarcia.myfriendz.domain.contacts.ContactData
import com.egarcia.myfriendz.domain.permission.PermissionGateway
import javax.inject.Inject

/**
 * Use case that wraps contact fetching logic. Presentation layer should call this instead
 * of depending on platform-specific repositories directly.
 */
class FetchContactUseCase @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val permissionGateway: PermissionGateway
) {
    sealed class Result {
        data class Success(val contact: ContactData) : Result()
        object PermissionRequired : Result()
        object NotFound : Result()
        data class Error(val throwable: Throwable) : Result()
    }

    suspend fun fetchContactDetails(contactUriString: String, permission: String): Result {
        return if (!permissionGateway.hasPermission(permission)) {
            Result.PermissionRequired
        } else {
            try {
                val contact = contactsRepository.fetchContactDetails(contactUriString)
                if (contact != null) Result.Success(contact) else Result.NotFound
            } catch (t: Throwable) {
                Result.Error(t)
            }
        }
    }
}
