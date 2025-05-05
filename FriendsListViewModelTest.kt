package com.egarcia.myfriendz.showFriend.viewmodel

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.egarcia.myfriendz.MainCoroutineRule
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.model.FriendDao
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@ExperimentalCoroutinesApi
class FriendsListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var friendDao: FriendDao

    private lateinit var friendsObserver: Observer<List<Friend>>

    private lateinit var viewModel: FriendsListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)
        viewModel = FriendsListViewModel(friendDao)
        friendsObserver = mockk(relaxed = true)
        viewModel.friends.observeForever(friendsObserver)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `refresh should fetch friends from database and update LiveData`() = runTest {
        // Given
        val expectedFriends = listOf(
            Friend(uuid = 1, name = "Friend 1", lastContacted = LocalDate.now()),
            Friend(uuid = 2, name = "Friend 2", lastContacted = LocalDate.now())
        )
        coEvery { friendDao.getAllFriends() } returns expectedFriends

        // When
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { friendDao.getAllFriends() }
        verify { friendsObserver.onChanged(expectedFriends) }
    }

    @Test
    fun `updateFriend should update friend in database and refresh LiveData`() = runTest {
        // Given
        val friendToUpdate = Friend(uuid = 1, name = "New Name", lastContacted = LocalDate.now(), imageUri = Uri.parse(""))
        val expectedFriends = listOf(
            Friend(uuid = 1, name = "Friend 1", lastContacted = LocalDate.now()),
            Friend(uuid = 2, name = "Friend 2", lastContacted = LocalDate.now())
        )
        coEvery { friendDao.getAllFriends() } returns expectedFriends
        coEvery { friendDao.updateFriend(any()) } returns Unit
        // When
        viewModel.updateFriend(friendToUpdate)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { friendDao.updateFriend(friendToUpdate) }
        coVerify { friendDao.getAllFriends() }
        verify { friendsObserver.onChanged(expectedFriends) }
    }
}