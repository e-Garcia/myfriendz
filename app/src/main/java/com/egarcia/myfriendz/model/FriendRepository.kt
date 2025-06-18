package com.egarcia.myfriendz.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing friends data.
 * Handles interactions with both local Room database and remote Firebase Firestore.
 */
class FriendRepository(
    private val dao: FriendDao,
    private val firebase: FriendFirebaseRepository,
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun getAllFriends(): List<Friend> = withContext(ioDispatcher) {
        dao.getAllFriends()
    }

    suspend fun addFriend(friend: Friend) = withContext(Dispatchers.IO) {
        dao.addFriend(friend)
        firebase.uploadFriend(friend)
    }

    suspend fun updateFriend(friend: Friend) = withContext(Dispatchers.IO) {
        dao.updateFriend(friend)
        firebase.uploadFriend(friend)
    }

    suspend fun deleteFriend(friendId: Int) = withContext(Dispatchers.IO) {
        dao.deleteFriend(friendId)
        firebase.deleteFriend(friendId)
    }

    suspend fun syncAll() = withContext(Dispatchers.IO) {
        // Upload all local friends to Firestore
        val localFriends = dao.getAllFriends()
        localFriends.forEach { firebase.uploadFriend(it) }
        // Download all remote friends and update local DB
        val remoteFriends = firebase.getAllFriends()
        remoteFriends.forEach { dao.addFriend(it) }
    }
}
