package com.egarcia.myfriendz.editFriend.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egarcia.myfriendz.R
import com.egarcia.myfriendz.core.di.IoDispatcher
import com.egarcia.myfriendz.domain.usecase.FriendUseCase
import com.egarcia.myfriendz.model.Friend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class EditFriendState {
    object Idle : EditFriendState()
    object Loading : EditFriendState()
    object Success : EditFriendState()
    data class Error(@StringRes val messageRes: Int) : EditFriendState()
}

@HiltViewModel
class EditFriendViewModel @Inject constructor(
    private val friendUseCase: FriendUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    val friend = MutableLiveData<Friend>()

    private val _editFriendState = MutableStateFlow<EditFriendState>(EditFriendState.Idle)
    val editFriendState: StateFlow<EditFriendState> = _editFriendState.asStateFlow()

    fun fetch(uuid: Int) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                friend.value = friendUseCase.getFriend(uuid)
            }
            _editFriendState.value = EditFriendState.Success
        }
    }

    fun update(friend: Friend) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                friendUseCase.updateFriendDetails(friend)
            }
            _editFriendState.value = EditFriendState.Success
        }
    }
}
