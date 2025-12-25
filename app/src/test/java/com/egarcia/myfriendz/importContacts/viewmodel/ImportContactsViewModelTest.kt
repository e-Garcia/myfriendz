package com.egarcia.myfriendz.importContacts.viewmodel

import com.egarcia.myfriendz.domain.repository.FriendRepository
import com.egarcia.myfriendz.domain.usecase.FriendUseCase
import com.egarcia.myfriendz.importContacts.model.ImportContactItem
import com.egarcia.myfriendz.model.Friend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class ImportContactsViewModelTest {

    private val repository = FakeFriendRepository()
    private val friendUseCase = FriendUseCase(repository)
    private val testDispatcher = StandardTestDispatcher()
    private val viewModel = ImportContactsViewModel(friendUseCase, testDispatcher)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `importSelectedContacts ignores duplicates`() = runTest {
        repository.seedFriend(
            Friend(
                name = "Existing Friend",
                lastContacted = LocalDate.now(),
                frequency = "",
                phone = "555",
                email = "existing@example.com",
                comments = ""
            ).apply { uuid = 1 }
        )

        viewModel.setContacts(
            listOf(
                ImportContactItem(
                    id = "1",
                    name = "Existing Friend",
                    phone = "555",
                    email = "existing@example.com",
                    isSelected = true
                ),
                ImportContactItem(
                    id = "2",
                    name = "New Friend",
                    phone = "666",
                    email = "new@example.com",
                    isSelected = true
                )
            )
        )

        viewModel.importSelectedContacts()
        advanceUntilIdle()

        assertEquals(1, repository.addedFriends.size)
        assertEquals("New Friend", repository.addedFriends.first().name)
        assertEquals(2, repository.getAllFriends().size)
    }
}

private class FakeFriendRepository : FriendRepository {

    private val storedFriends = mutableListOf<Friend>()
    val addedFriends = mutableListOf<Friend>()

    fun seedFriend(friend: Friend) {
        storedFriends.add(friend)
    }

    override suspend fun getAllFriends(): List<Friend> = storedFriends.toList()

    override suspend fun getFriend(friendId: Int): Friend =
        storedFriends.first { it.uuid == friendId }

    override suspend fun updateFriend(friend: Friend) {
        storedFriends.replaceAll {
            if (it.uuid == friend.uuid) friend else it
        }
    }

    override suspend fun deleteFriend(friendId: Int) {
        storedFriends.removeAll { it.uuid == friendId }
    }

    override suspend fun addFriend(friend: Friend) {
        val copy = friend.copy()
        copy.uuid = (storedFriends.maxOfOrNull { it.uuid } ?: 0) + 1
        storedFriends.add(copy)
        addedFriends.add(copy)
    }
}
