package com.egarcia.myfriendz.viewmodel

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
class FriendsDetailViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var friendUseCase: FriendUseCase

    private lateinit var viewModel: FriendsDetailViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        viewModel = FriendsDetailViewModel(friendUseCase)
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
        val currentState = viewModel.friendDetailState.value
        assertTrue(currentState is FriendDetailState.Success)
        assertEquals(friend, (currentState as FriendDetailState.Success).friend)
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
        val currentState = viewModel.friendDetailState.value
        assertTrue(currentState is FriendDetailState.Error)
        assertEquals(R.string.error_fetching_friend, (currentState as FriendDetailState.Error).messageRes)
    }
}
