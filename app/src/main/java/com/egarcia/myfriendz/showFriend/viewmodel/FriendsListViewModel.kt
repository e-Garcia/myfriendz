package com.egarcia.myfriendz.showFriend.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.model.FriendDatabase
import com.egarcia.myfriendz.viewmodel.BaseViewModel
import kotlinx.coroutines.launch

class FriendsListViewModel(application: Application) : BaseViewModel(application) {

    val friends = MutableLiveData<List<Friend>>()

    fun refresh() {
        fetchFromDatabase()
    }

    //TODO remove from code as this is only needed for testing purposes
    fun deleteAllFriends() {
        launch {
            FriendDatabase(getApplication()).friendDao().deleteAllFriends()
        }
    }

    private fun fetchFromDatabase() {
        launch {
            val friendsList = FriendDatabase(getApplication()).friendDao().getAllFriends()
            friends.value = friendsList
        }
    }

}