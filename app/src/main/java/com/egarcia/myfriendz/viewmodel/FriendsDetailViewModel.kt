package com.egarcia.myfriendz.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.model.FriendDatabase
import kotlinx.coroutines.launch

class FriendsDetailViewModel(application: Application) : BaseViewModel(application) {
    val friend = MutableLiveData<Friend>()

    fun fetch(friendUuid: Int) {
        launch {
            val friendFromDatabase = FriendDatabase.getInstance(getApplication()).friendDao().getFriend(friendUuid)
            friend.value = friendFromDatabase
        }
    }
}