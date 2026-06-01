package com.egarcia.myfriendz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egarcia.myfriendz.core.di.IoDispatcher
import com.egarcia.myfriendz.domain.usecase.FriendUseCase
import com.egarcia.myfriendz.model.Friend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FriendsDetailViewModel @Inject constructor(
    private val friendUseCase: FriendUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    val friend = MutableLiveData<Friend>()

    fun fetch(friendUuid: Int) {
        viewModelScope.launch {
            val friendFromDatabase = withContext(ioDispatcher) {
                friendUseCase.getFriend(friendUuid)
            }
            friend.value = friendFromDatabase
        }
    }
}