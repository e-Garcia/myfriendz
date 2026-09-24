package com.egarcia.myfriendz.domain.repository

import com.egarcia.myfriendz.domain.model.PreferenceCategory
import com.egarcia.myfriendz.model.FriendPreference

/**
 * Repository interface for friend preference (profiling) data operations.
 * Handles storing and retrieving extracted user preferences from dialogue sessions.
 */
interface PreferenceRepository {

    /**
     * Retrieves all stored preferences for a friend.
     */
    suspend fun getPreferencesByFriend(friendId: Int): List<FriendPreference>

    /**
     * Retrieves preferences for a specific category.
     */
    suspend fun getPreferencesByCategory(
        friendId: Int,
        category: PreferenceCategory
    ): List<FriendPreference>

    /**
     * Retrieves the latest (most recent) value for a given category.
     */
    suspend fun getLatestPreference(
        friendId: Int,
        category: PreferenceCategory
    ): FriendPreference?

    /**
     * Adds a new preference entry.
     */
    suspend fun addPreference(preference: FriendPreference)

    /**
     * Deletes all preferences for a specific friend.
     */
    suspend fun deletePreferencesForFriend(friendId: Int)

    /**
     * Gets a count of completed (source = USER_PROVIDED) categories for a friend.
     */
    suspend fun getCompletedCategoryCount(friendId: Int): Int

    /**
     * Gets all available categories.
     */
    fun getAllCategories(): List<PreferenceCategory>

    /**
     * Checks whether all categories have been completed for a friend.
     */
    suspend fun areAllCategoriesComplete(friendId: Int): Boolean
