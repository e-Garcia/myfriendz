package com.egarcia.myfriendz.addFriend.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.model.FriendDao
import com.egarcia.myfriendz.showFriend.utils.DEFAULT_MONTHS_LAST_CONTACTED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class AddFriendViewModel @Inject constructor(
    private val friendDao: FriendDao
) : ViewModel() {
    private val lastContacted: LocalDate = LocalDate.now().minusMonths(DEFAULT_MONTHS_LAST_CONTACTED)
    val friend = MutableLiveData(Friend("", lastContacted, "", "", "", ""))

    fun addFriend() {
        viewModelScope.launch {
            friend.value?.let {
                friendDao.addFriend(it)
            }
        }
    }
}