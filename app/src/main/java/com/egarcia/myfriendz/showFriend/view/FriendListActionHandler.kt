package com.egarcia.myfriendz.showFriend.view

import android.view.View
import com.egarcia.myfriendz.model.Friend

interface FriendListActionHandler {
    fun onFriendLongClicked(view: View, friend: Friend): Boolean
    fun onFriendClicked(view: View, friend: Friend)
    fun onAddFriendClicked(view: View)
}