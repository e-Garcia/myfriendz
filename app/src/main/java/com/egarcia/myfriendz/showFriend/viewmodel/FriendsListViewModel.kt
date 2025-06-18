package com.egarcia.myfriendz.showFriend.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asFlow
import com.egarcia.myfriendz.R
import com.egarcia.myfriendz.data.NetworkMonitor
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.model.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for managing the state of the friends list.
 * Handles fetching, updating, and deleting friends,
 * as well as syncing with the repository.
 */
@HiltViewModel
class FriendsListViewModel @Inject constructor(
    private val repository: FriendRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _friendsState = MutableLiveData<FriendListState>()
    val friendsState: LiveData<FriendListState> = _friendsState

    init {
        syncAll()
        // Observe connectivity changes and sync when online
        viewModelScope.launch {
            networkMonitor.isConnected.asFlow().collect { connected ->
                if (connected) syncAll()
            }
        }
    }

    fun syncAll() {
        viewModelScope.launch {
            try {
                repository.syncAll()
                fetchFromDatabase()
            } catch (e: Exception) {
                // Optionally handle sync error
            }
        }
    }

    private fun fetchFromDatabase() {
        viewModelScope.launch {
            _friendsState.value = FriendListState.Loading
            try {
                val friendsList = withContext(Dispatchers.IO) {
                    repository.getAllFriends()
                }
                _friendsState.postValue(FriendListState.Success(friendsList))
            } catch (e: Exception) {
                _friendsState.postValue(FriendListState.Error(R.string.error_fetching_data))
            }
        }
    }

    fun refresh() {
        fetchFromDatabase()
    }

    fun updateFriend(friend: Friend) {
        viewModelScope.launch {
            _friendsState.value = FriendListState.Loading
            try {
                withContext(Dispatchers.IO) {
                    friend.lastContacted = LocalDate.now()
                    repository.updateFriend(friend)
                }
                fetchFromDatabase()
            } catch (e: Exception) {
                _friendsState.value = FriendListState.Error(R.string.error_updating_data)
            }
        }
    }

    fun deleteFriend(friend: Friend) {
        viewModelScope.launch {
            _friendsState.value = FriendListState.Loading
            try {
                withContext(Dispatchers.IO) {
                    repository.deleteFriend(friend.uuid)
                }
                fetchFromDatabase()
            } catch (e: Exception) {
                _friendsState.value = FriendListState.Error(R.string.error_deleting_data)
            }
        }
    }
}

/**
 * Represents the state of the friends list.
 * Can be in a loading state, success state with a list of friends,
 * or an error state with a message resource ID.
 */
sealed class FriendListState {
    object Loading : FriendListState()
    data class Success(val friends: List<Friend>) : FriendListState()
    data class Error(@StringRes val messageRes: Int) : FriendListState()
}