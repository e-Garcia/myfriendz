package com.egarcia.myfriendz.showFriend.viewmodel

import androidx.annotation.StringRes
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

sealed class FriendListState {
    object Loading : FriendListState()
    data class Success(val friends: List<Friend>) : FriendListState()
    data class Error(@StringRes val messageRes: Int) : FriendListState()
}

@HiltViewModel
class FriendsListViewModel @Inject constructor(
    private val friendUseCase: FriendUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _friendsState = MutableStateFlow<FriendListState>(FriendListState.Loading)
    val friendsState: StateFlow<FriendListState> = _friendsState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        fetchFromDatabase()
    }

    fun updateFriend(friend: Friend) {
        viewModelScope.launch {
            _friendsState.value = FriendListState.Loading
            try {
                withContext(ioDispatcher) {
                    friendUseCase.updateFriendAsContacted(friend)
                }
                fetchFromDatabase()
            } catch (_: Exception) {
                _friendsState.value = FriendListState.Error(R.string.error_updating_data)
            }
        }
    }

    fun deleteFriend(friend: Friend) {
        viewModelScope.launch {
            _friendsState.value = FriendListState.Loading
            try {
                withContext(ioDispatcher) {
                    friendUseCase.deleteFriend(friend.uuid)
                }
                fetchFromDatabase()
            } catch (_: Exception) {
                _friendsState.value = FriendListState.Error(R.string.error_deleting_data)
            }
        }
    }

    private fun fetchFromDatabase() {
        viewModelScope.launch {
            _friendsState.value = FriendListState.Loading
            try {
                val friendsList = withContext(ioDispatcher) {
                    friendUseCase.getAllFriends()
                }
                _friendsState.value = FriendListState.Success(friendsList)
            } catch (_: Exception) {
                _friendsState.value = FriendListState.Error(R.string.error_fetching_data)
            }
        }
    }
}