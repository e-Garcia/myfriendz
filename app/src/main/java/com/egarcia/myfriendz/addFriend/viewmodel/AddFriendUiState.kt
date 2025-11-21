package com.egarcia.myfriendz.addFriend.viewmodel

import androidx.annotation.StringRes
import com.egarcia.myfriendz.model.Friend

/**
 * Single UI state for the Add Friend screen.
 */
data class AddFriendUiState(
    val friend: Friend,
    val isSaving: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
    val isSaveSuccessful: Boolean = false
)

