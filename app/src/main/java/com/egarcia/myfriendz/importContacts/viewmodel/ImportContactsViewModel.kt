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
import com.egarcia.myfriendz.importContacts.utils.DuplicateChecker
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

    data class ConfirmDuplicate(val existing: Friend, val contact: ImportContactItem) : ImportContactsEffect()
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

    private val duplicateChecker = DuplicateChecker()

    // Pending state while resolving duplicates interactively
    private val pendingDuplicates = mutableListOf<Pair<ImportContactItem, Friend>>()
    private val pendingToImport = mutableListOf<ImportContactItem>()

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

            // Partition duplicates vs toImport
            val existingFriends = withContext(ioDispatcher) { friendUseCase.getAllFriends() }

            val (duplicatesList, importList) = selected.partition { contact ->
                duplicateChecker.isDuplicate(contact, existingFriends)
            }

            pendingDuplicates.clear()
            pendingToImport.clear()
            pendingToImport.addAll(importList)

            // Map duplicates to (contact, matchedFriend)
            duplicatesList.forEach { contact ->
                // find the first matching friend for this contact
                val matched = existingFriends.firstOrNull { f ->
                    duplicateChecker.isDuplicate(contact, listOf(f))
                }
                matched?.let { pendingDuplicates.add(contact to it) }
            }

            if (pendingDuplicates.isNotEmpty()) {
                // Prompt user for the first duplicate; UI will call resolveDuplicate for decisions
                val (contact, existing) = pendingDuplicates.first()
                _effects.emit(ImportContactsEffect.ConfirmDuplicate(existing, contact))
                return@launch
            }

            // No duplicates to resolve, proceed with import
            val result = withContext(ioDispatcher) { performImport(pendingToImport, existingFriends) }

            handleImportResult(result)
        }
    }

    // Called by UI when user resolves a duplicate prompt.
    // contactId identifies the ImportContactItem.id for which a decision was made.
    fun resolveDuplicate(contactId: String, decision: DuplicateDecision) {
        viewModelScope.launch {
            val pairIndex = pendingDuplicates.indexOfFirst { it.first.id == contactId }
            if (pairIndex == -1) return@launch // nothing to do

            val (contact, existing) = pendingDuplicates.removeAt(pairIndex)

            when (decision) {
                is DuplicateDecision.Merge -> {
                    // Merge contact fields into existing friend (prefer contact non-blank values)
                    val merged = existing.copy(
                        name = contact.name.takeIf { it.isNotBlank() } ?: existing.name,
                        phone = contact.phone?.takeIf { it.isNotBlank() } ?: existing.phone,
                        email = contact.email?.takeIf { it.isNotBlank() } ?: existing.email
                    ).apply { uuid = existing.uuid } // preserve id so repository update matches
                    withContext(ioDispatcher) { friendUseCase.updateFriendDetails(merged) }
                }
                DuplicateDecision.CreateNew -> {
                    pendingToImport.add(contact)
                }
                DuplicateDecision.Skip -> {
                    // Do nothing
                }
            }

            // If more duplicates remain, prompt next; else perform import of accumulated toImport
            if (pendingDuplicates.isNotEmpty()) {
                val (nextContact, nextExisting) = pendingDuplicates.first()
                _effects.emit(ImportContactsEffect.ConfirmDuplicate(nextExisting, nextContact))
            } else {
                // proceed
                val existingFriends = withContext(ioDispatcher) { friendUseCase.getAllFriends() }
                val result = withContext(ioDispatcher) { performImport(pendingToImport, existingFriends) }
                handleImportResult(result)
            }
        }
    }

    private fun handleImportResult(result: ImportResult) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    contacts = it.contacts.map { contact -> contact.copy(isSelected = false) },
                    isImportInProgress = false,
                    errorRes = null
                )
            }
            _effects.emit(
                ImportContactsEffect.ShowPlural(
                    R.plurals.import_contacts_result,
                    result.imported,
                    listOf(result.skipped, result.failed)
                )
            )
        }
    }

    private suspend fun performImport(selected: List<ImportContactItem>, existingFriends: List<Friend>): ImportResult {
        var imported = 0
        var failed = 0
        val duplicatesDetected = mutableListOf<ImportContactItem>()

        selected.forEach { contact ->
            // Double-check duplicates against current repository state to avoid race conditions
            val isDup = duplicateChecker.isDuplicate(contact, existingFriends)
            if (isDup) {
                duplicatesDetected.add(contact)
                return@forEach
            }

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
            skipped = duplicatesDetected.size,
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

    private data class ImportResult(
        val imported: Int,
        val skipped: Int,
        val failed: Int
    )

    sealed class DuplicateDecision {
        object CreateNew : DuplicateDecision()
        object Skip : DuplicateDecision()
        data class Merge(val mergedFriend: Friend? = null) : DuplicateDecision()
    }
}
