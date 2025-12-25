package com.egarcia.myfriendz.addFriend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egarcia.myfriendz.domain.usecase.FriendUseCase
import com.egarcia.myfriendz.domain.usecase.FetchContactUseCase
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.showFriend.utils.DEFAULT_MONTHS_LAST_CONTACTED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject


@HiltViewModel
class AddFriendViewModel @Inject constructor(
    private val friendUseCase: FriendUseCase,
    private val fetchContactUseCase: FetchContactUseCase
) : ViewModel() {
    private val lastContacted: LocalDate = LocalDate.now().minusMonths(DEFAULT_MONTHS_LAST_CONTACTED)
    val friend = androidx.lifecycle.MutableLiveData(Friend("", lastContacted, "", "", "", ""))

    // Expose selected date (epoch millis) as a StateFlow so the Fragment can collect it.
    private val _selectedDateMillis = MutableStateFlow<Long?>(
        lastContacted.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    val selectedDateMillis: StateFlow<Long?> = _selectedDateMillis.asStateFlow()

    // Contact fetch state exposed to the UI as StateFlow
    sealed class ContactFetchState {
        object Idle : ContactFetchState()
        object PermissionRequired : ContactFetchState()
        data class Success(val name: String, val phone: String?, val email: String?) : ContactFetchState()
        object NotFound : ContactFetchState()
        data class Error(val throwable: Throwable) : ContactFetchState()
    }

    private val _contactFetchState = MutableStateFlow<ContactFetchState>(ContactFetchState.Idle)
    val contactFetchState: StateFlow<ContactFetchState> = _contactFetchState.asStateFlow()

    // One-shot event to open the contact picker in the Fragment (SharedFlow)
    private val _openContactPicker = MutableSharedFlow<Unit>(replay = 0)
    val openContactPicker: SharedFlow<Unit> = _openContactPicker.asSharedFlow()

    // One-shot event to navigate back after saving (SharedFlow)
    private val _navigateBack = MutableSharedFlow<Unit>(replay = 0)
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    // Pending contact URI stored here so permission results can be retried without Fragment state
    private var pendingContactUri: String? = null

    fun addFriend() {
        viewModelScope.launch {
            friend.value?.let {
                friendUseCase.addFriend(it)
            }
        }
    }

    fun onSaveClicked() {
        viewModelScope.launch {
            friend.value?.let {
                friendUseCase.addFriend(it)
                _navigateBack.emit(Unit)
            }
        }
    }

    fun onImportFromContactsClicked() {
        viewModelScope.launch {
            _openContactPicker.emit(Unit)
        }
    }

    fun populateFromContact(name: String?, phone: String?, email: String?) {
        val current = friend.value ?: return
        friend.value = current.copy(
            name = name?.takeIf { it.isNotBlank() } ?: current.name,
            phone = phone?.takeIf { it.isNotBlank() } ?: current.phone,
            email = email?.takeIf { it.isNotBlank() } ?: current.email
        )
        // Do not change lastContacted here; if the contact provides a date, update via onDateSelectedMillis
    }

    /**
     * Called by the Fragment when the user selects a date from the picker (millis).
     * This updates both the selectedDate state and the friend's lastContacted value.
     */
    fun onDateSelectedMillis(millis: Long) {
        _selectedDateMillis.value = millis
        val selectedLocalDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        val current = friend.value ?: return
        friend.value = current.copy(lastContacted = selectedLocalDate)
    }

    /**
     * Public API for the Fragment to inform the ViewModel that a contact Uri was picked.
     * The ViewModel stores the pending Uri and attempts to fetch; if permission is required
     * the pending Uri remains and can be retried by calling onPermissionGranted().
     */
    fun onContactPickedUri(uriString: String) {
        pendingContactUri = uriString
        // attempt fetch; populateContactFromUri will set PermissionRequired if needed
        populateContactFromUri(uriString)
    }

    /**
     * Call when the system permission has been granted; this will retry any pending contact fetch.
     */
    fun onPermissionGranted() {
        pendingContactUri?.let {
            populateContactFromUri(it)
            pendingContactUri = null
        }
    }

    private fun populateContactFromUri(contactUriString: String) {
        _contactFetchState.value = ContactFetchState.Idle
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                fetchContactUseCase.fetchContactDetails(contactUriString, android.Manifest.permission.READ_CONTACTS)
            }

            when (result) {
                is FetchContactUseCase.Result.PermissionRequired -> _contactFetchState.value = ContactFetchState.PermissionRequired
                is FetchContactUseCase.Result.Success -> _contactFetchState.value = ContactFetchState.Success(result.contact.name, result.contact.phone, result.contact.email)
                is FetchContactUseCase.Result.NotFound -> _contactFetchState.value = ContactFetchState.NotFound
                is FetchContactUseCase.Result.Error -> _contactFetchState.value = ContactFetchState.Error(result.throwable)
            }
        }
    }
}
