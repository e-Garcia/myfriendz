package com.egarcia.myfriendz.addFriend.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.model.FriendDao
import com.egarcia.myfriendz.model.FriendDatabase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
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
        coEvery { FriendDatabase(application) } returns friendDatabase
        viewModel = AddFriendViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createTestFriend(): Friend {
        return Friend(
            "John",
            "Doe",
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
        assertEquals(Friend("", "", "", "", "", ""), initialFriend)
    }
}