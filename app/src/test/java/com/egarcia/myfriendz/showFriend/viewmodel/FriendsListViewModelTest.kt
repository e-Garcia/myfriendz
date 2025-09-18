package com.egarcia.myfriendz.showFriend.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.egarcia.myfriendz.model.Friend
import com.egarcia.myfriendz.model.FriendDao
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import com.egarcia.myfriendz.R
import org.junit.Assert.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class FriendsListViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var friendDao: FriendDao
    private lateinit var viewModel: FriendsListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        friendDao = mockk(relaxed = true)
        viewModel = FriendsListViewModel(friendDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `refresh emits Loading then Success`() = runTest {
        val friends = listOf(Friend("Alice", LocalDate.now(), "Weekly", "123", "a@b.com", ""))
        coEvery { friendDao.getAllFriends() } returns friends
        val states = mutableListOf<FriendListState>()
        val observer = Observer<FriendListState> { states.add(it) }
        viewModel.friendsState.observeForever(observer)

        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        assert(states.first() is FriendListState.Loading)
        assert(states.last() is FriendListState.Success)
        assertEquals(friends, (states.last() as FriendListState.Success).friends)
        viewModel.friendsState.removeObserver(observer)
    }

    @Test
    fun `refresh emits Error on exception`() = runTest {
        coEvery { friendDao.getAllFriends() } throws RuntimeException()
        val states = mutableListOf<FriendListState>()
        val observer = Observer<FriendListState> { states.add(it) }
        viewModel.friendsState.observeForever(observer)

        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        assert(states.first() is FriendListState.Loading)
        assert(states.last() is FriendListState.Error)
        assertEquals(R.string.error_fetching_data, (states.last() as FriendListState.Error).messageRes)
        viewModel.friendsState.removeObserver(observer)
    }

    @Test
    fun `updateFriend emits Loading then Success`() = runTest {
        val friend = Friend("Bob", LocalDate.now(), "Monthly", "456", "b@c.com", "")
        coEvery { friendDao.updateFriend(any()) } just Runs
        coEvery { friendDao.getAllFriends() } returns listOf(friend)
        val states = mutableListOf<FriendListState>()
        val observer = Observer<FriendListState> { states.add(it) }
        viewModel.friendsState.observeForever(observer)

        viewModel.updateFriend(friend)
        testDispatcher.scheduler.advanceUntilIdle()

        assert(states.first() is FriendListState.Loading)
        assert(states.last() is FriendListState.Success)
        viewModel.friendsState.removeObserver(observer)
    }

    @Test
    fun `updateFriend emits Error on exception`() = runTest {
        val friend = Friend("Bob", LocalDate.now(), "Monthly", "456", "b@c.com", "")
        coEvery { friendDao.updateFriend(any()) } throws RuntimeException()
        val states = mutableListOf<FriendListState>()
        val observer = Observer<FriendListState> { states.add(it) }
        viewModel.friendsState.observeForever(observer)

        viewModel.updateFriend(friend)
        testDispatcher.scheduler.advanceUntilIdle()

        assert(states.first() is FriendListState.Loading)
        assert(states.last() is FriendListState.Error)
        assertEquals(R.string.error_updating_data, (states.last() as FriendListState.Error).messageRes)
        viewModel.friendsState.removeObserver(observer)
    }

    @Test
    fun `deleteFriend emits Loading then Success`() = runTest {
        val friend = Friend("Carol", LocalDate.now(), "Yearly", "789", "c@d.com", "")
        friend.uuid = 1
        coEvery { friendDao.deleteFriend(friend.uuid) } just Runs
        coEvery { friendDao.getAllFriends() } returns emptyList()
        val states = mutableListOf<FriendListState>()
        val observer = Observer<FriendListState> { states.add(it) }
        viewModel.friendsState.observeForever(observer)

        viewModel.deleteFriend(friend)
        testDispatcher.scheduler.advanceUntilIdle()

        assert(states.first() is FriendListState.Loading)
        assert(states.last() is FriendListState.Success)
        viewModel.friendsState.removeObserver(observer)
    }

    @Test
    fun `deleteFriend emits Error on exception`() = runTest {
        val friend = Friend("Carol", LocalDate.now(), "Yearly", "789", "c@d.com", "")
        friend.uuid = 1
        coEvery { friendDao.deleteFriend(friend.uuid) } throws RuntimeException()
        val states = mutableListOf<FriendListState>()
        val observer = Observer<FriendListState> { states.add(it) }
        viewModel.friendsState.observeForever(observer)

        viewModel.deleteFriend(friend)
        testDispatcher.scheduler.advanceUntilIdle()

        assert(states.first() is FriendListState.Loading)
        assert(states.last() is FriendListState.Error)
        assertEquals(R.string.error_deleting_data, (states.last() as FriendListState.Error).messageRes)
        viewModel.friendsState.removeObserver(observer)
    }
}
