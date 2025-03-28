package com.egarcia.myfriendz.showFriend.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.model.FriendDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsListViewModel @Inject constructor(
    private val friendDao: FriendDao
) : ViewModel() {

    val friends = MutableLiveData<List<Friend>>()

    fun refresh() {
        fetchFromDatabase()
    }

    private fun fetchFromDatabase() {
        viewModelScope.launch {
            val friendsList = friendDao.getAllFriends()
            friends.value = friendsList
        }
    }
}