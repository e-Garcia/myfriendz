package com.egarcia.myfriendz.showFriend.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.egarcia.myfriendz.R
import com.egarcia.myfriendz.domain.usecase.FriendUseCase
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.testing.MainDispatcherRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.time.LocalDate

@ExperimentalCoroutinesApi
class FriendsListViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var friendUseCase: FriendUseCase

    private lateinit var viewModel: FriendsListViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    private fun createTestFriends(): List<Friend> {
        return listOf(
            Friend(
                "John Doe",
                LocalDate.parse("2023-01-01"),
                "monthly",
                "123-456-7890",
                "john@example.com",
                "Friend from college"
            ),
            Friend(
                "Jane Smith",
                LocalDate.parse("2023-02-15"),
                "weekly",
                "098-765-4321",
                "jane@example.com",
                "Coworker"
            )
        )
    }

    @Test
    fun `initial state should be loading and then fetch friends on init`() = runTest {
        // Given
        val testFriends = createTestFriends()
        coEvery { friendUseCase.getAllFriends() } returns testFriends

        // When
        viewModel = FriendsListViewModel(friendUseCase, mainDispatcherRule.dispatcher)
        advanceUntilIdle()

        // Then
        val currentState = viewModel.friendsState.value
        assertTrue(currentState is FriendListState.Success)
        assertEquals(testFriends, (currentState as FriendListState.Success).friends)
        coVerify { friendUseCase.getAllFriends() }
    }

    @Test
    fun `refresh should fetch friends from use case`() = runTest {
        // Given
        val testFriends = createTestFriends()
        coEvery { friendUseCase.getAllFriends() } returns testFriends
        viewModel = FriendsListViewModel(friendUseCase, mainDispatcherRule.dispatcher)
        advanceUntilIdle()

        // When
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        val currentState = viewModel.friendsState.value
        assertTrue(currentState is FriendListState.Success)
        assertEquals(testFriends, (currentState as FriendListState.Success).friends)
        coVerify(exactly = 2) { friendUseCase.getAllFriends() } // Once on init, once on refresh
    }

    @Test
    fun `updateFriend should call updateFriendAsContacted and refresh data`() = runTest {
        // Given
        val testFriends = createTestFriends()
        val friendToUpdate = testFriends[0]
        coEvery { friendUseCase.getAllFriends() } returns testFriends
        coEvery { friendUseCase.updateFriendAsContacted(any()) } returns Unit
        viewModel = FriendsListViewModel(friendUseCase, mainDispatcherRule.dispatcher)
        advanceUntilIdle()

        // When
        viewModel.updateFriend(friendToUpdate)
        advanceUntilIdle()

        // Then
        coVerify { friendUseCase.updateFriendAsContacted(friendToUpdate) }
        coVerify(exactly = 2) { friendUseCase.getAllFriends() } // Once on init, once after update
    }

    @Test
    fun `deleteFriend should call deleteFriend and refresh data`() = runTest {
        // Given
        val testFriends = createTestFriends()
        val friendToDelete = testFriends[0]
        coEvery { friendUseCase.getAllFriends() } returns testFriends
        coEvery { friendUseCase.deleteFriend(any()) } returns Unit
        viewModel = FriendsListViewModel(friendUseCase, mainDispatcherRule.dispatcher)
        advanceUntilIdle()

        // When
        viewModel.deleteFriend(friendToDelete)
        advanceUntilIdle()

        // Then
        coVerify { friendUseCase.deleteFriend(friendToDelete.uuid) }
        coVerify(exactly = 2) { friendUseCase.getAllFriends() } // Once on init, once after delete
    }

    @Test
    fun `getFriends error should emit Error state`() = runTest {
        // Given
        coEvery { friendUseCase.getAllFriends() } throws Exception("Database error")

        // When
        viewModel = FriendsListViewModel(friendUseCase, mainDispatcherRule.dispatcher)
        advanceUntilIdle()

        // Then
        val currentState = viewModel.friendsState.value
        assertTrue(currentState is FriendListState.Error)
        assertEquals(R.string.error_fetching_data, (currentState as FriendListState.Error).messageRes)
    }

    @Test
    fun `updateFriend error should emit Error state`() = runTest {
        // Given
        val testFriends = createTestFriends()
        val friendToUpdate = testFriends[0]
        coEvery { friendUseCase.getAllFriends() } returns testFriends
        coEvery { friendUseCase.updateFriendAsContacted(any()) } throws Exception("Update failed")
        viewModel = FriendsListViewModel(friendUseCase, mainDispatcherRule.dispatcher)
        advanceUntilIdle()

        // When
        viewModel.updateFriend(friendToUpdate)
        advanceUntilIdle()

        // Then
        val currentState = viewModel.friendsState.value
        assertTrue(currentState is FriendListState.Error)
        assertEquals(R.string.error_updating_data, (currentState as FriendListState.Error).messageRes)
    }

    @Test
    fun `deleteFriend error should emit Error state`() = runTest {
        // Given
        val testFriends = createTestFriends()
        val friendToDelete = testFriends[0]
        coEvery { friendUseCase.getAllFriends() } returns testFriends
        coEvery { friendUseCase.deleteFriend(any()) } throws Exception("Delete failed")
        viewModel = FriendsListViewModel(friendUseCase, mainDispatcherRule.dispatcher)
        advanceUntilIdle()

        // When
        viewModel.deleteFriend(friendToDelete)
        advanceUntilIdle()

        // Then
        val currentState = viewModel.friendsState.value
        assertTrue(currentState is FriendListState.Error)
        assertEquals(R.string.error_deleting_data, (currentState as FriendListState.Error).messageRes)
    }
}
