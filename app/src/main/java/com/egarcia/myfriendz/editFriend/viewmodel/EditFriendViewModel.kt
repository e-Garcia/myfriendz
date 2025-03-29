package com.egarcia.myfriendz.editFriend.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.model.FriendDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditFriendViewModel @Inject constructor(
    private val friendDao: FriendDao
) : ViewModel() {

    val friend = MutableLiveData<Friend>()

    fun fetch(uuid: Int) {
        viewModelScope.launch {
            friend.value = friendDao.getFriend(uuid)
        }
    }

    fun update(friend: Friend) {
        viewModelScope.launch {
            friendDao.updateFriend(friend)
        }
    }
}