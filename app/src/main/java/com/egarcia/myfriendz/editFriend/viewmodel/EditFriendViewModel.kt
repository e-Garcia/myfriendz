package com.egarcia.myfriendz.editFriend.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egarcia.myfriendz.domain.usecase.FriendUseCase
import com.egarcia.myfriendz.model.Friend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditFriendViewModel @Inject constructor(
    private val friendUseCase: FriendUseCase
) : ViewModel() {

    val friend = MutableLiveData<Friend>()

    fun fetch(uuid: Int) {
        viewModelScope.launch {
            friend.value = friendUseCase.getFriend(uuid)
        }
    }

    fun update(friend: Friend) {
        viewModelScope.launch {
            friendUseCase.updateFriendDetails(friend)
        }
    }
}