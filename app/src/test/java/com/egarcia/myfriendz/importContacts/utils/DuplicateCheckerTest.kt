package com.egarcia.myfriendz.importContacts.utils

import com.egarcia.myfriendz.importContacts.model.ImportContactItem
import com.egarcia.myfriendz.model.Friend
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DuplicateCheckerTest {

    private val now = LocalDate.now()
    private val checker = DuplicateChecker(defaultRegion = "US")

    private fun friend(name: String, phone: String = "", email: String = "") = Friend(
        name = name,
        lastContacted = now,
        frequency = "",
        phone = phone,
        email = email,
        comments = ""
    )

    private fun contact(name: String, phone: String? = null, email: String? = null) = ImportContactItem(
        id = name,
        name = name,
        phone = phone,
        email = email
    )

    @Test
    fun `duplicate when phone matches exactly`() {
        val existing = listOf(friend("Alice", "1234567890", ""))
        val c = contact("Alice", "(123) 456-7890", null)
        assertTrue(checker.isDuplicate(c, existing))
    }

    @Test
    fun `duplicate when email matches exactly`() {
        val existing = listOf(friend("Bob", "", "bob@example.com"))
        val c = contact("Bobby", null, "bob@example.com")
        assertTrue(checker.isDuplicate(c, existing))
    }

    @Test
    fun `not duplicate when name matches but phones differ`() {
        val existing = listOf(friend("Carol", "1112223333", ""))
        val c = contact("Carol", "9998887777", null)
        assertFalse(checker.isDuplicate(c, existing))
    }

    @Test
    fun `duplicate when name and phone both match after normalization`() {
        val existing = listOf(friend("Dave", "+1-800-555-0000", ""))
        val c = contact("  dave  ", "(800)5550000", null)
        assertTrue(checker.isDuplicate(c, existing))
    }

    @Test
    fun `not duplicate when no matching identifiers`() {
        val existing = listOf(friend("Eve", "1112223333", "eve@example.com"))
        val c = contact("Frank", "4445556666", "frank@example.com")
        assertFalse(checker.isDuplicate(c, existing))
    }

    @Test
    fun `phone with extension matches base number`() {
        val existing = listOf(friend("Gina", "+1-202-555-0175", ""))
        val c = contact("Gina", "+1 (202) 555-0175 ext. 123", null)
        assertTrue(checker.isDuplicate(c, existing))
    }

    @Test
    fun `different country code but same national number matches when normalized`() {
        val existing = listOf(friend("Hank", "+44 20 7946 0958", ""))
        val c = contact("Hank", "020 7946 0958", null)
        // Using defaultRegion US won't parse UK national number, so this should not be considered duplicate with US default.
        // Create a UK-specific checker to validate matching behavior.
        val ukChecker = DuplicateChecker(defaultRegion = "GB")
        assertFalse(checker.isDuplicate(c, existing))
        assertTrue(ukChecker.isDuplicate(c, existing))
    }
}
