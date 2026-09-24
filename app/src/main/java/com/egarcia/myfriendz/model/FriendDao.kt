package com.egarcia.myfriendz.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Determines how the Friend model class should be stored and retrieved from the Room database.
 * Relies on Kotlin coroutines for handling the asynchronous processing.
 * @see Friend as it's the model class this dao persists.
 */
@Dao
interface FriendDao {

    @Query("SELECT * FROM friends")
    suspend fun getAllFriends(): List<Friend>

    @Query("SELECT * FROM friends WHERE uuid = :friendId")
    suspend fun getFriend(friendId: Int): Friend

    @Update(entity = Friend::class)
    suspend fun updateFriend(friend: Friend)

    @Query("DELETE FROM friends WHERE uuid = :friendId")
    suspend fun deleteFriend(friendId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFriend(friend: Friend)

    //Implemented for testing purposes only. to be deleted when done using it.
    @Query("DELETE FROM friends")

    suspend fun deleteAllFriends()

    // ── Preference (profiling) methods ──────────────────────────────────────────

    @Query("SELECT * FROM friend_preferences WHERE friend_id = :friendId")
    suspend fun getPreferencesByFriend(friendId: Int): List<FriendPreference>

    @Query(
        """
        SELECT * FROM friend_preferences 
        WHERE friend_id = :friendId AND category = :category
        ORDER BY id DESC LIMIT 1
        """
    )
    suspend fun getLatestPreference(
        friendId: Int,
        category: String
    ): FriendPreference?

    @Query("SELECT COUNT(DISTINCT category) FROM friend_preferences WHERE friend_id = :friendId AND source = 'USER_PROVIDED'")
    suspend fun getCompletedCategoryCount(friendId: Int): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addPreference(preference: FriendPreference)

    @Query("DELETE FROM friend_preferences WHERE friend_id = :friendId")
    suspend fun deletePreferencesForFriend(friendId: Int)

    @Query("SELECT DISTINCT category FROM friend_preferences WHERE friend_id = :friendId")
    suspend fun getCategoriesForFriend(friendId: Int): List<String>

}