package com.egarcia.myfriendz.showFriend.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.model.FriendDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class FriendsListViewModel @Inject constructor(
    private val friendDao: FriendDao
) : ViewModel() {

    val friends = MutableLiveData<List<Friend>>()

    fun refresh() {
        fetchFromDatabase()
    }

    fun updateFriend(friend: Friend) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                friend.lastContacted = LocalDate.now()
                friendDao.updateFriend(friend)
                fetchFromDatabase()
            }
        }
    }

    private fun fetchFromDatabase() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val friendsList = friendDao.getAllFriends()
                friends.postValue(friendsList)
            }
        }
    }
}