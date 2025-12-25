package com.egarcia.myfriendz.addFriend.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.egarcia.myfriendz.domain.usecase.FetchContactUseCase
import com.egarcia.myfriendz.domain.usecase.FriendUseCase
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

@ExperimentalCoroutinesApi
class AddFriendViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var friendUseCase: FriendUseCase

    @MockK
    private lateinit var fetchContactUseCase: FetchContactUseCase

    private lateinit var viewModel: AddFriendViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        viewModel = AddFriendViewModel(friendUseCase, fetchContactUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onDateSelectedMillis updates selectedDateMillis and friend lastContacted`() = runTest(testDispatcher) {
        // Given
        val date = LocalDate.parse("2020-01-01")
        val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // When
        viewModel.onDateSelectedMillis(millis)
        // ensure any coroutines complete
        testScheduler.advanceUntilIdle()

        // Then
        assertEquals(millis, viewModel.selectedDateMillis.value)
        assertEquals(date, viewModel.friend.value?.lastContacted)
    }
}
