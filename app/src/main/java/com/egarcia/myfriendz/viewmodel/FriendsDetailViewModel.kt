package com.egarcia.myfriendz.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egarcia.myfriendz.R
import com.egarcia.myfriendz.core.di.IoDispatcher
import com.egarcia.myfriendz.domain.usecase.FriendUseCase
import com.egarcia.myfriendz.model.Friend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class FriendDetailState {
    object Idle : FriendDetailState()
    object Loading : FriendDetailState()
    data class Success(val friend: Friend) : FriendDetailState()
    data class Error(@StringRes val messageRes: Int) : FriendDetailState()
}

@HiltViewModel
class FriendsDetailViewModel @Inject constructor(
    private val friendUseCase: FriendUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    val friend = MutableLiveData<Friend>()

    private val _friendDetailState = MutableStateFlow<FriendDetailState>(FriendDetailState.Idle)
    val friendDetailState: StateFlow<FriendDetailState> = _friendDetailState.asStateFlow()

    fun fetch(friendUuid: Int) {
        viewModelScope.launch {
            _friendDetailState.value = FriendDetailState.Loading
            try {
                val friendFromDatabase = withContext(ioDispatcher) {
                    friendUseCase.getFriend(friendUuid)
                }
                friend.value = friendFromDatabase
                _friendDetailState.value = FriendDetailState.Success(friendFromDatabase)
            } catch (_: Exception) {
                _friendDetailState.value = FriendDetailState.Error(R.string.error_fetching_friend)
            }
        }
    }
}
