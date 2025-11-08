package com.egarcia.myfriendz.data.repository

import com.egarcia.myfriendz.domain.repository.FriendRepository
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.model.FriendDao
import javax.inject.Inject

/**
 * Implementation of FriendRepository that uses Room database via FriendDao.
 * This class is part of the data layer and handles the actual data persistence.
 */
class FriendRepositoryImpl @Inject constructor(
    private val friendDao: FriendDao
) : FriendRepository {

    override suspend fun getAllFriends(): List<Friend> {
        return friendDao.getAllFriends()
    }

    override suspend fun getFriend(friendId: Int): Friend {
        return friendDao.getFriend(friendId)
    }

    override suspend fun updateFriend(friend: Friend) {
        friendDao.updateFriend(friend)
    }

    override suspend fun deleteFriend(friendId: Int) {
        friendDao.deleteFriend(friendId)
    }

    override suspend fun addFriend(friend: Friend) {
        friendDao.addFriend(friend)
    }
}
