package com.egarcia.myfriendz.addFriend.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.model.FriendDatabase
import com.egarcia.myfriendz.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

class AddFriendViewModel(application: Application) : BaseViewModel(application) {

    val friend = MutableLiveData(Friend("", "", "", "", "", ""))

    fun addFriend() {
        launch {
            friend.value?.let {
                FriendDatabase(getApplication()).friendDao().addFriend(it)
            }
        }
    }
}