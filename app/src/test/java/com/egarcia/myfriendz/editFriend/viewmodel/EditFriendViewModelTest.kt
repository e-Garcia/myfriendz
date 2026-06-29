package com.egarcia.myfriendz.editFriend.viewmodel

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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@ExperimentalCoroutinesApi
class EditFriendViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var friendUseCase: FriendUseCase

    private lateinit var viewModel: EditFriendViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        viewModel = EditFriendViewModel(friendUseCase)
    }

    private fun createTestFriend(): Friend {
        return Friend(
            "John",
            LocalDate.parse("2023-01-01"),
            "monthly",
            "123-456-7890",
            "john.doe@example.com",
            "some notes"
        ).apply { uuid = 1 }
    }

    @Test
    fun `fetch success updates friend and emits Success state`() = runTest {
        // Given
        val friend = createTestFriend()
        coEvery { friendUseCase.getFriend(friend.uuid) } returns friend

        // When
        viewModel.fetch(friend.uuid)
        advanceUntilIdle()

        // Then
        assertEquals(friend, viewModel.friend.value)
        assertTrue(viewModel.editFriendState.value is EditFriendState.Success)
        coVerify { friendUseCase.getFriend(friend.uuid) }
    }

    @Test
    fun `fetch failure emits Error state`() = runTest {
        // Given
        coEvery { friendUseCase.getFriend(1) } throws Exception("Missing friend")

        // When
        viewModel.fetch(1)
        advanceUntilIdle()

        // Then
        val currentState = viewModel.editFriendState.value
        assertTrue(currentState is EditFriendState.Error)
        assertEquals(R.string.error_fetching_friend, (currentState as EditFriendState.Error).messageRes)
    }

    @Test
    fun `update success calls use case and emits Success state`() = runTest {
        // Given
        val friend = createTestFriend()
        coEvery { friendUseCase.updateFriendDetails(friend) } returns Unit

        // When
        viewModel.update(friend)
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.editFriendState.value is EditFriendState.Success)
        coVerify { friendUseCase.updateFriendDetails(friend) }
    }

    @Test
    fun `update failure emits Error state`() = runTest {
        // Given
        val friend = createTestFriend()
        coEvery { friendUseCase.updateFriendDetails(friend) } throws Exception("Update failed")

        // When
        viewModel.update(friend)
        advanceUntilIdle()

        // Then
        val currentState = viewModel.editFriendState.value
        assertTrue(currentState is EditFriendState.Error)
        assertEquals(R.string.error_updating_data, (currentState as EditFriendState.Error).messageRes)
    }
}
