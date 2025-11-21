package com.egarcia.myfriendz.editFriend.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egarcia.myfriendz.R
import com.egarcia.myfriendz.domain.usecase.FriendUseCase
import com.egarcia.myfriendz.model.Friend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EditFriendState {
    object Loading : EditFriendState()
    data class Ready(val friend: Friend) : EditFriendState()
    object Saving : EditFriendState()
    object Success : EditFriendState()
    data class Error(@StringRes val messageRes: Int) : EditFriendState()
}

@HiltViewModel
class EditFriendViewModel @Inject constructor(
    private val friendUseCase: FriendUseCase
) : ViewModel() {

    private val _friendState = MutableStateFlow<EditFriendState>(EditFriendState.Loading)
    val friendState: StateFlow<EditFriendState> = _friendState.asStateFlow()

    fun fetch(uuid: Int) {
        viewModelScope.launch {
            _friendState.value = EditFriendState.Loading
            try {
                val friend = friendUseCase.getFriend(uuid)
                _friendState.value = EditFriendState.Ready(friend)
            } catch (e: Exception) {
                _friendState.value = EditFriendState.Error(R.string.error_fetching_data)
            }
        }
    }

    fun updateFriend(friend: Friend) {
        _friendState.value = EditFriendState.Ready(friend)
    }

    fun update(friend: Friend) {
        viewModelScope.launch {
            _friendState.value = EditFriendState.Saving
            try {
                friendUseCase.updateFriendDetails(friend)
                _friendState.value = EditFriendState.Success
            } catch (e: Exception) {
                _friendState.value = EditFriendState.Error(R.string.error_updating_data)
            }
        }
    }
}