package com.egarcia.myfriendz.editFriend

import android.os.Bundle
import androidx.navigation.NavDirections
import com.egarcia.myfriendz.R
import kotlin.Int

private const val FRIEND_UUID_KEY = "friendUuid"

class FriendDetailsFragmentDirections private constructor() {
    private data class ActionEditFriendFragment(
        val friendUuid: Int
    ) : NavDirections {
        override val actionId: Int = R.id.editFriendFragment

        override val arguments: Bundle
            get() {
                val result = Bundle()
                result.putInt(FRIEND_UUID_KEY, this.friendUuid)
                return result
            }
    }

    companion object {
        fun actionEditFriendFragment(friendUuid: Int): NavDirections =
            ActionEditFriendFragment(friendUuid)
    }
}