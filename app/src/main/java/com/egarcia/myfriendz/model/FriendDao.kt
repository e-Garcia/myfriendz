package com.egarcia.myfriendz.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FriendDao {

    @Query("SELECT * FROM friend")
    suspend fun getAllFriends(): List<Friend>

    @Query("SELECT * FROM friend WHERE uuid = :friendId")
    suspend fun getFriend(friendId: Int): Friend

    @Query("DElETE FROM friend WHERE uuid = :friendId")
    suspend fun deleteFriend(friendId: Int)

    @Insert
    suspend fun addFriend(friend: Friend)

    //Implemented for testing purposes only. to be deleted when done using it.
    @Query("DElETE FROM friend")
    suspend fun deleteAllFriends()
}