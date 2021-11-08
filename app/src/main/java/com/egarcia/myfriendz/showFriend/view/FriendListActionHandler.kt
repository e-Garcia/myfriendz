package com.egarcia.myfriendz.showFriend.view

import android.view.View
import com.egarcia.myfriendz.model.Friend

interface FriendListActionHandler {
    fun viewFriend(view: View, friend: Friend)
    fun addFriend(view: View)
}