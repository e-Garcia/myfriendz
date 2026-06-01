package com.egarcia.myfriendz.addFriend.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.egarcia.myfriendz.core.di.IoDispatcher
import com.egarcia.myfriendz.domain.usecase.FriendUseCase
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.showFriend.utils.DEFAULT_MONTHS_LAST_CONTACTED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class AddFriendViewModel @Inject constructor(
    private val friendUseCase: FriendUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    private val lastContacted: LocalDate = LocalDate.now().minusMonths(DEFAULT_MONTHS_LAST_CONTACTED)
    val friend = MutableLiveData(Friend("", lastContacted, "", "", "", ""))

    fun addFriend() {
        viewModelScope.launch {
            friend.value?.let {
                withContext(ioDispatcher) {
                    friendUseCase.addFriend(it)
                }
            }
        }
    }
}