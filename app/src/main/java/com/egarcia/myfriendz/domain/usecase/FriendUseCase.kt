package com.egarcia.myfriendz.domain.usecase

import com.egarcia.myfriendz.domain.repository.FriendRepository
import com.egarcia.myfriendz.model.Friend
import java.time.LocalDate
import javax.inject.Inject

/**
 * Unified use case for all friend-related operations.
 * This class encapsulates all business logic for friend management,
 * providing a single point of interaction between the presentation and data layers.
 */
class FriendUseCase @Inject constructor(
    private val friendRepository: FriendRepository
) {
    /**
     * Retrieves all friends from the repository.
     * @return List of all friends
     */
    suspend fun getAllFriends(): List<Friend> {
        return friendRepository.getAllFriends()
    }

    /**
     * Retrieves a specific friend by their ID.
     * @param friendId The unique identifier of the friend
     * @return The friend with the specified ID
     */
    suspend fun getFriend(friendId: Int): Friend {
        return friendRepository.getFriend(friendId)
    }

    /**
     * Adds a new friend to the repository.
     * @param friend The friend to add
     */
    suspend fun addFriend(friend: Friend) {
        friendRepository.addFriend(friend)
    }

    /**
     * Updates a friend and marks them as contacted.
     * This updates the lastContacted date to the current date.
     * Use this when recording an interaction with the friend.
     *
     * @param friend The friend to update
     */
    suspend fun updateFriendAsContacted(friend: Friend) {
        val updatedFriend = friend.copy(lastContacted = LocalDate.now())
        friendRepository.updateFriend(updatedFriend)
    }

    /**
     * Updates a friend's details without changing the lastContacted date.
     * Use this when editing friend information (name, phone, etc.)
     *
     * @param friend The friend to update
     */
    suspend fun updateFriendDetails(friend: Friend) {
        friendRepository.updateFriend(friend)
    }

    /**
     * Deletes a friend from the repository.
     * @param friendId The unique identifier of the friend to delete
     */
    suspend fun deleteFriend(friendId: Int) {
        friendRepository.deleteFriend(friendId)
    }
}
