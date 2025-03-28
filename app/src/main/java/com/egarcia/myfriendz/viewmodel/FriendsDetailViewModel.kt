package com.egarcia.myfriendz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.model.FriendDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsDetailViewModel @Inject constructor(
    private val friendDao: FriendDao
) : ViewModel() {
    val friend = MutableLiveData<Friend>()

    fun fetch(friendUuid: Int) {
        viewModelScope.launch {
            val friendFromDatabase = friendDao.getFriend(friendUuid)
            friend.value = friendFromDatabase
        }
    }
}