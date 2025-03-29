package com.egarcia.myfriendz

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.egarcia.myfriendz.addFriend.viewmodel.AddFriendViewModel
import com.egarcia.myfriendz.showFriend.utils.DEFAULT_MONTHS_LAST_CONTACTED
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.model.FriendDao
import com.egarcia.myfriendz.model.FriendDatabase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@ExperimentalCoroutinesApi
class AddFriendViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: AddFriendViewModel

    @MockK
    private lateinit var application: Application

    @MockK
    private lateinit var friendDatabase: FriendDatabase

    @MockK
    private lateinit var friendDao: FriendDao

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        coEvery { application.applicationContext } returns application
        coEvery { friendDatabase.friendDao() } returns friendDao

        viewModel = AddFriendViewModel(friendDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createTestFriend(): Friend {
        val date = LocalDate.parse("2023-01-01")
        return Friend(
            "John",
            date,
            "123-456-7890",
            "john.doe@example.com",
            "New York",
            "some notes"
        )
    }

    @Test
    fun `addFriend calls friendDao addFriend`() = runTest {
        // Given
        val friend = createTestFriend()
        coEvery { friendDao.addFriend(any()) } returns Unit
        viewModel.friend.value = friend

        // When
        viewModel.addFriend()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { friendDao.addFriend(friend) }
    }

    @Test
    fun `initial friend value is empty`() {
        // When
        val initialFriend = viewModel.friend.value

        // Then
        assertEquals(Friend("", LocalDate.now().minusMonths(DEFAULT_MONTHS_LAST_CONTACTED), "", "", "", ""), initialFriend)
    }
}