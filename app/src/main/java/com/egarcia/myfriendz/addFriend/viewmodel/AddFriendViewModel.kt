package com.egarcia.myfriendz.addFriend.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.model.FriendDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

private const val DEFAULT_MONTHS_LAST_CONTACTED = 6L

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