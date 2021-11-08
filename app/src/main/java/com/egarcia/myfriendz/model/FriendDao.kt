package com.egarcia.myfriendz.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Determines how the Friend model class should be stored and retrieved from the Room database.
 * Relies on Kotlin coroutines for handling the asynchronous processing.
 * @see Friend as it's the model class this dao persists.
 */
@Dao
interface FriendDao {

    @Query("SELECT * FROM friend")
    suspend fun getAllFriends(): List<Friend>

    @Query("SELECT * FROM friend WHERE uuid = :friendId")
    suspend fun getFriend(friendId: Int): Friend

    @Query("DElETE FROM friend WHERE uuid = :friendId")
    suspend fun deleteFriend(friendId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFriend(friend: Friend)

    //Implemented for testing purposes only. to be deleted when done using it.
    @Query("DElETE FROM friend")
    suspend fun deleteAllFriends()
}