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
import io.mockk.every
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

    @MockK
    private lateinit var timeProvider: com.egarcia.myfriendz.util.TimeProvider

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        // Make TimeProvider deterministic for the test (use current date)
        every { timeProvider.today() } returns LocalDate.now()

        viewModel = AddFriendViewModel(friendUseCase, timeProvider)
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
        // Populate the draft via intent-style field updates
        viewModel.onNameChanged(friend.name)
        viewModel.onFrequencyChanged(friend.frequency)
        viewModel.onPhoneChanged(friend.phone)
        viewModel.onEmailChanged(friend.email)
        viewModel.onCommentsChanged(friend.comments)

        // When
        viewModel.onSaveClicked()
        advanceUntilIdle()

        // Then
        coVerify { friendUseCase.addFriend(friend) }
    }

    @Test
    fun `initial friend value is empty`() {
        // When
        val initialState = viewModel.state.value

        // Then
        val expectedFriend = Friend(
            "",
            LocalDate.now().minusMonths(DEFAULT_MONTHS_LAST_CONTACTED),
            "",
            "",
            "",
            ""
        )

        Assert.assertEquals(expectedFriend, initialState.friend)
        Assert.assertFalse(initialState.isSaving)
        Assert.assertNull(initialState.errorMessageRes)
        Assert.assertFalse(initialState.isSaveSuccessful)
    }
}