package com.egarcia.myfriendz.addFriend.viewmodel

import androidx.annotation.StringRes

sealed interface AddFriendEvent {
    data object SaveSuccess : AddFriendEvent
    data class ValidationError(@StringRes val messageRes: Int) : AddFriendEvent
    data class SaveError(@StringRes val messageRes: Int) : AddFriendEvent
}
