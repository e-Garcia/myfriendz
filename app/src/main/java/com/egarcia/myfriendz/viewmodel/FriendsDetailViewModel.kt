package com.egarcia.myfriendz.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egarcia.myfriendz.R
import com.egarcia.myfriendz.domain.usecase.FriendUseCase
import com.egarcia.myfriendz.model.Friend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FriendDetailState {
    object Loading : FriendDetailState()
    data class Success(val friend: Friend) : FriendDetailState()
    data class Error(@StringRes val messageRes: Int) : FriendDetailState()
}

@HiltViewModel
class FriendsDetailViewModel @Inject constructor(
    private val friendUseCase: FriendUseCase
) : ViewModel() {
    private val _friendState = MutableStateFlow<FriendDetailState>(FriendDetailState.Loading)
    val friendState: StateFlow<FriendDetailState> = _friendState.asStateFlow()

    fun fetch(friendUuid: Int) {
        viewModelScope.launch {
            _friendState.value = FriendDetailState.Loading
            try {
                val friendFromDatabase = friendUseCase.getFriend(friendUuid)
                _friendState.value = FriendDetailState.Success(friendFromDatabase)
            } catch (e: Exception) {
                _friendState.value = FriendDetailState.Error(R.string.error_fetching_data)
            }
        }
    }
}