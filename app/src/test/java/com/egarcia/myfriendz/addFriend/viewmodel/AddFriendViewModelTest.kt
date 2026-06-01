package com.egarcia.myfriendz.addFriend.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.egarcia.myfriendz.domain.usecase.FriendUseCase
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.showFriend.utils.DEFAULT_MONTHS_LAST_CONTACTED
import com.egarcia.myfriendz.testing.MainDispatcherRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@ExperimentalCoroutinesApi
class AddFriendViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: AddFriendViewModel

    @MockK
    private lateinit var friendUseCase: FriendUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        viewModel = AddFriendViewModel(friendUseCase, mainDispatcherRule.dispatcher)
    }

    private fun createTestFriend(): Friend {
        val date = LocalDate.parse("2023-01-01")
        return Friend(
            "John",
            date,
            "monthly",
            "123-456-7890",
            "john.doe@example.com",
            "some notes"
        )
    }

    @Test
    fun `addFriend calls friendUseCase addFriend`() = runTest {
        // Given
        val friend = createTestFriend()
        coEvery { friendUseCase.addFriend(any()) } returns Unit
        viewModel.friend.value = friend

        // When
        viewModel.addFriend()
        advanceUntilIdle()

        // Then
        coVerify { friendUseCase.addFriend(friend) }
    }

    @Test
    fun `initial friend value is empty`() {
        // When
        val initialFriend = viewModel.friend.value

        // Then
        Assert.assertEquals(
            Friend(
                "",
                LocalDate.now().minusMonths(DEFAULT_MONTHS_LAST_CONTACTED),
                "",
                "",
                "",
                ""
            ), initialFriend
        )
    }
}