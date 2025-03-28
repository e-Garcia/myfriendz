package com.egarcia.myfriendz.addFriend.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.model.FriendDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddFriendViewModel @Inject constructor(
    private val friendDao: FriendDao
) : ViewModel() {

    val friend = MutableLiveData(Friend("", "", "", "", "", ""))

    fun addFriend() {
        viewModelScope.launch {
            friend.value?.let {
                friendDao.addFriend(it)
            }
        }
    }
}