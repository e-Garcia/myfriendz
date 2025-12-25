package com.egarcia.myfriendz.importContacts.viewmodel

import android.util.Log
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egarcia.myfriendz.R
import com.egarcia.myfriendz.core.di.IoDispatcher
import com.egarcia.myfriendz.domain.usecase.FriendUseCase
import com.egarcia.myfriendz.importContacts.model.ImportContactItem
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.showFriend.utils.DEFAULT_MONTHS_LAST_CONTACTED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

data class ImportContactsUiState(
    val contacts: List<ImportContactItem> = emptyList(),
    val permissionDenied: Boolean = false,
    val errorRes: Int? = null,
    val isImportInProgress: Boolean = false
)

sealed class ImportContactsEffect {
    data class ShowMessage(
        @StringRes val messageRes: Int,
        val formatArgs: List<Any> = emptyList()
    ) : ImportContactsEffect()

    data class ShowText(val message: String) : ImportContactsEffect()

    data class ShowPlural(
        @PluralsRes val pluralRes: Int,
        val quantity: Int,
        val formatArgs: List<Any> = emptyList()
    ) : ImportContactsEffect()
}

@HiltViewModel
class ImportContactsViewModel @Inject constructor(
    private val friendUseCase: FriendUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val defaultLastContacted: LocalDate =
        LocalDate.now().minusMonths(DEFAULT_MONTHS_LAST_CONTACTED)

    private val _uiState = MutableStateFlow(ImportContactsUiState())
    val uiState: StateFlow<ImportContactsUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ImportContactsEffect>()
    val effects: SharedFlow<ImportContactsEffect> = _effects.asSharedFlow()

    fun setContacts(contacts: List<ImportContactItem>) {
        _uiState.update {
            it.copy(
                contacts = contacts,
                permissionDenied = false,
                errorRes = null
            )
        }
    }

    fun markPermissionDenied() {
        _uiState.update {
            it.copy(
                contacts = emptyList(),
                permissionDenied = true,
                errorRes = null
            )
        }
    }

    fun markLoadingError(@StringRes errorRes: Int) {
        _uiState.update {
            it.copy(
                contacts = emptyList(),
                errorRes = errorRes,
                permissionDenied = false
            )
        }
    }

    fun updateSelection(contactId: String, selected: Boolean) {
        _uiState.update { state ->
            state.copy(
                contacts = state.contacts.map { contact ->
                    if (contact.id == contactId) {
                        contact.copy(isSelected = selected)
                    } else {
                        contact
                    }
                }
            )
        }
    }

    fun toggleSelectAll() {
        _uiState.update { state ->
            if (state.contacts.isEmpty()) return@update state
            val shouldSelect = state.contacts.any { !it.isSelected }
            state.copy(
                contacts = state.contacts.map { it.copy(isSelected = shouldSelect) }
            )
        }
    }

    fun importSelectedContacts() {
        val selected = _uiState.value.contacts.filter { it.isSelected }
        if (selected.isEmpty()) {
            viewModelScope.launch {
                _effects.emit(
                    ImportContactsEffect.ShowMessage(
                        R.string.select_contacts_to_import
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isImportInProgress = true) }
            val result = runCatching {
                withContext(ioDispatcher) {
                    performImport(selected)
                }
            }.onFailure {
                _uiState.update { it.copy(isImportInProgress = false) }
                _effects.emit(
                    ImportContactsEffect.ShowMessage(
                        R.string.error_importing_contacts
                    )
                )
            }.getOrNull()

            if (result != null) {
                _uiState.update {
                    it.copy(
                        contacts = it.contacts.map { contact -> contact.copy(isSelected = false) },
                        isImportInProgress = false,
                        errorRes = null
                    )
                }
                // Emit plural-aware effect so Fragment can format with resources
                _effects.emit(
                    ImportContactsEffect.ShowPlural(
                        R.plurals.import_contacts_result,
                        result.imported,
                        listOf(result.skipped, result.failed)
                    )
                )
            }
        }
    }

    private suspend fun performImport(selected: List<ImportContactItem>): ImportResult {
        val existingFriends = friendUseCase.getAllFriends()
        val duplicates = selected.filter { isDuplicate(it, existingFriends) }
        val toImport = selected - duplicates

        var imported = 0
        var failed = 0
        toImport.forEach { contact ->
            try {
                friendUseCase.addFriend(contact.toFriend(defaultLastContacted))
                imported++
            } catch (e: Exception) {
                Log.e("ImportContactsViewModel", "Failed to import contact: ${contact.name}", e)
                failed++
            }
        }

        return ImportResult(
            imported = imported,
            skipped = duplicates.size,
            failed = failed
        )
    }

    private fun ImportContactItem.toFriend(lastContacted: LocalDate): Friend {
        return Friend(
            name = name,
            lastContacted = lastContacted,
            frequency = "",
            phone = phone.orEmpty(),
            email = email.orEmpty(),
            comments = ""
        )
    }

    private fun isDuplicate(
        contact: ImportContactItem,
        existingFriends: List<Friend>
    ): Boolean {
        val normalizedPhone = contact.phone.normalizePhone()
        val normalizedEmail = contact.email.normalizeEmail()
        val normalizedName = contact.name.trim().lowercase()

        return existingFriends.any { friend ->
            val friendPhone = friend.phone.normalizePhone()
            val friendEmail = friend.email.normalizeEmail()
            val friendName = friend.name.trim().lowercase()

            (normalizedPhone.isNotBlank() && normalizedPhone == friendPhone) ||
                (normalizedEmail.isNotBlank() && normalizedEmail == friendEmail) ||
                (normalizedName.isNotBlank() && normalizedName == friendName && normalizedPhone.isNotBlank() && friendPhone.isNotBlank())
        }
    }

    private fun String?.normalizePhone(): String {
        return this?.filter { it.isDigit() } ?: ""
    }

    private fun String?.normalizeEmail(): String {
        return this?.trim()?.lowercase() ?: ""
    }

    private data class ImportResult(
        val imported: Int,
        val skipped: Int,
        val failed: Int
    )
}
