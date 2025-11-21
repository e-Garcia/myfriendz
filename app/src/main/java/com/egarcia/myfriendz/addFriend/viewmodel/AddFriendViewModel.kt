package com.egarcia.myfriendz.addFriend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egarcia.myfriendz.R
import com.egarcia.myfriendz.domain.usecase.FriendUseCase
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.showFriend.utils.DEFAULT_MONTHS_LAST_CONTACTED
import com.egarcia.myfriendz.util.TimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class AddFriendViewModel @Inject constructor(
    private val friendUseCase: FriendUseCase,
    timeProvider: TimeProvider
) : ViewModel() {
    private val lastContacted: LocalDate =
        timeProvider.today().minusMonths(DEFAULT_MONTHS_LAST_CONTACTED)

    // Use named args for clarity
    private val initialFriend = Friend(
        name = "",
        lastContacted = lastContacted,
        frequency = "",
        phone = "",
        email = "",
        comments = ""
    )

    private val _state = MutableStateFlow(AddFriendUiState(friend = initialFriend))
    val state: StateFlow<AddFriendUiState> = _state.asStateFlow()

    // Configure SharedFlow with a small buffer so one-off UI events are not dropped
    private val _events = MutableSharedFlow<AddFriendEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events: SharedFlow<AddFriendEvent> = _events.asSharedFlow()

    // Helper to emit UI events in a non-suspending way (avoids launching coroutines just to emit)
    private fun sendEvent(event: AddFriendEvent) {
        _events.tryEmit(event)
    }

    // Intent-style API: individual field updates
    fun onNameChanged(name: String) = updateFriend { copy(name = name) }
    fun onPhoneChanged(phone: String) = updateFriend { copy(phone = phone) }
    fun onEmailChanged(email: String) = updateFriend { copy(email = email) }
    fun onFrequencyChanged(frequency: String) = updateFriend { copy(frequency = frequency) }
    fun onCommentsChanged(comments: String) = updateFriend { copy(comments = comments) }
    // Add other field intents here if Friend grows

    // Helper to mutate the current friend in state and reset transient flags
    private fun updateFriend(transform: Friend.() -> Friend) {
        _state.update { current ->
            val updatedFriend = current.friend.transform()
            current.copy(
                friend = updatedFriend,
                isSaveSuccessful = false,
                errorMessageRes = null
            )
        }
    }

    // Simple client-side validation: returns string resource id if invalid, or null if valid
    private fun validate(friend: Friend): Int? {
        if (friend.name.isBlank()) return R.string.error_name_required
        // TODO: Add more validation rules as the domain grows:
        // - Optional: validate email format if not blank
        // - Optional: validate frequency constraints
        // - Optional: validate phone format if not blank
        return null
    }

    // Called by the UI when a displayed error has been acknowledged/dismissed
    fun onErrorShown() {
        _state.update {
            it.copy(errorMessageRes = null)
        }
    }

    // Event-style save trigger (UI clicked Save)
    fun onSaveClicked() {
        val snapshot = _state.value
        // Guard against double-tap: ignore save if a save is already in progress
        if (snapshot.isSaving) return

        val currentFriend = snapshot.friend

        // Client-side validation
        val validationError = validate(currentFriend)
        if (validationError != null) {
            _state.update {
                it.copy(
                    errorMessageRes = validationError,
                    isSaveSuccessful = false
                )
            }
            // Emit validation error synchronously (no coroutine needed thanks to tryEmit)
            sendEvent(AddFriendEvent.ValidationError(validationError))
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(isSaving = true, errorMessageRes = null, isSaveSuccessful = false)
            }
            try {
                friendUseCase.addFriend(currentFriend)

                _state.update { it.copy(isSaving = false, isSaveSuccessful = true) }

                sendEvent(AddFriendEvent.SaveSuccess)
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update {
                    it.copy(
                        isSaving = false,
                        errorMessageRes = R.string.error_updating_data,
                        isSaveSuccessful = false
                    )
                }
                sendEvent(AddFriendEvent.SaveError(R.string.error_updating_data))
            }
        }
    }
}