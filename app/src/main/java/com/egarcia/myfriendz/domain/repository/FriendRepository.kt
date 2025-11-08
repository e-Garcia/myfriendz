package com.egarcia.myfriendz.domain.repository

import com.egarcia.myfriendz.model.Friend

/**
 * Repository interface for Friend data operations.
 * This interface defines the contract for data access, decoupling the domain layer
 * from the data layer implementation details.
 */
interface FriendRepository {
    /**
     * Retrieves all friends from the data source.
     * @return List of all friends
     */
    suspend fun getAllFriends(): List<Friend>

    /**
     * Retrieves a specific friend by their ID.
     * @param friendId The unique identifier of the friend
     * @return The friend with the specified ID
     */
    suspend fun getFriend(friendId: Int): Friend

    /**
     * Updates an existing friend in the data source.
     * @param friend The friend to update
     */
    suspend fun updateFriend(friend: Friend)

    /**
     * Deletes a friend from the data source.
     * @param friendId The unique identifier of the friend to delete
     */
    suspend fun deleteFriend(friendId: Int)

    /**
     * Adds a new friend to the data source.
     * @param friend The friend to add
     */
    suspend fun addFriend(friend: Friend)
}
