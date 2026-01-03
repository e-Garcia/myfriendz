package com.egarcia.myfriendz.importContacts.viewmodel

import com.egarcia.myfriendz.domain.repository.FriendRepository
import com.egarcia.myfriendz.domain.usecase.FriendUseCase
import com.egarcia.myfriendz.importContacts.model.ImportContactItem
import com.egarcia.myfriendz.model.Friend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
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
        // Wait for ConfirmDuplicate effect
        val confirm = withTimeout(1_000) { viewModel.effects.filterIsInstance<ImportContactsEffect.ConfirmDuplicate>().first() }

        // Simulate user choosing Skip for the duplicate so import proceeds
        viewModel.resolveDuplicate("1", ImportContactsViewModel.DuplicateDecision.Skip)
        advanceUntilIdle()

        // no collector job to cancel

        assertEquals(1, repository.addedFriends.size)
        assertEquals("New Friend", repository.addedFriends.first().name)
        assertEquals(2, repository.getAllFriends().size)
    }

    @Test
    fun `duplicate prompts are emitted and merge updates existing friend and imports remaining`() = runTest {
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
                // Ensure this contact will be detected as duplicate by matching phone
                ImportContactItem(
                    id = "1",
                    name = "Existing Friend Updated",
                    phone = "555", // same as existing to trigger duplicate
                    email = "existing_new@example.com",
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
        val confirm = withTimeout(1_000) { viewModel.effects.filterIsInstance<ImportContactsEffect.ConfirmDuplicate>().first() }
        assertEquals("1", confirm.contact.id)

        // Resolve by merging
        viewModel.resolveDuplicate("1", ImportContactsViewModel.DuplicateDecision.Merge())
        advanceUntilIdle()

        // After merge, existing friend should be updated
        val all = repository.getAllFriends()
        val updated = all.first { it.uuid == 1 }
        assertEquals("Existing Friend Updated", updated.name)
        assertEquals("555", updated.phone)
        assertEquals("existing_new@example.com", updated.email)

        // And the remaining new contact should be added
        assertEquals(1, repository.addedFriends.size)
        assertEquals("New Friend", repository.addedFriends.first().name)
    }

    @Test
    fun `create new decision currently will be skipped due to duplicate re-check (ensures no crash)`() = runTest {
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
        val confirm2 = withTimeout(1_000) { viewModel.effects.filterIsInstance<ImportContactsEffect.ConfirmDuplicate>().first() }
        viewModel.resolveDuplicate(confirm2.contact.id, ImportContactsViewModel.DuplicateDecision.CreateNew)
        advanceUntilIdle()

        // The duplicate should not be added because performImport double-checks duplicates
        assertEquals(1, repository.addedFriends.size)
        assertEquals("New Friend", repository.addedFriends.first().name)
    }

    @Test
    fun `skip decision skips duplicate and imports others`() = runTest {
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
        val confirm3 = withTimeout(1_000) { viewModel.effects.filterIsInstance<ImportContactsEffect.ConfirmDuplicate>().first() }
        viewModel.resolveDuplicate(confirm3.contact.id, ImportContactsViewModel.DuplicateDecision.Skip)
        advanceUntilIdle()

        // Only the new friend should be added
        assertEquals(1, repository.addedFriends.size)
        assertEquals("New Friend", repository.addedFriends.first().name)
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
