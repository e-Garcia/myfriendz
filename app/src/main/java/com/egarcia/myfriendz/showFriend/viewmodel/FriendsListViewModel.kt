package com.egarcia.myfriendz.showFriend.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egarcia.myfriendz.R
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.model.FriendDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

sealed class FriendListState {
    object Loading : FriendListState()
    data class Success(val friends: List<Friend>) : FriendListState()
    data class Error(@StringRes val messageRes: Int) : FriendListState()
}

@HiltViewModel
class FriendsListViewModel @Inject constructor(
    private val friendDao: FriendDao
) : ViewModel() {

    private val _friendsState = MutableLiveData<FriendListState>()
    val friendsState: LiveData<FriendListState> = _friendsState

    fun refresh() {
        fetchFromDatabase()
    }

    fun updateFriend(friend: Friend) {
        viewModelScope.launch {
            _friendsState.value = FriendListState.Loading
            try {
                withContext(Dispatchers.IO) {
                    friend.lastContacted = LocalDate.now()
                    friendDao.updateFriend(friend)
                    fetchFromDatabase()
                }
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
                    friendDao.deleteFriend(friend.uuid)
                    fetchFromDatabase()
                }
            } catch (e: Exception) {
                _friendsState.value = FriendListState.Error(R.string.error_deleting_data)
            }
        }
    }

    private fun fetchFromDatabase() {
        viewModelScope.launch {
            _friendsState.value = FriendListState.Loading
            try {
                withContext(Dispatchers.IO) {
                    val friendsList = friendDao.getAllFriends()
                    _friendsState.postValue(FriendListState.Success(friendsList))
                }
            } catch (e: Exception) {
                _friendsState.postValue(FriendListState.Error(R.string.error_fetching_data))
            }
        }
    }
}