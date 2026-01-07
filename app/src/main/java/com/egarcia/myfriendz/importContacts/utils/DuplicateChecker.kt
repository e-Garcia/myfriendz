package com.egarcia.myfriendz.importContacts.utils

import com.egarcia.myfriendz.importContacts.model.ImportContactItem
import com.egarcia.myfriendz.model.Friend
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

class DuplicateChecker(private val defaultRegion: String = "US") {

    private val phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    fun isDuplicate(contact: ImportContactItem, existingFriends: List<Friend>): Boolean {
        val normalizedPhone = normalizePhone(contact.phone)
        val normalizedEmail = contact.email.normalizeEmail()
        val normalizedName = contact.name.trim().lowercase()

        return existingFriends.any { friend ->
            val friendPhone = normalizePhone(friend.phone)
            val friendEmail = friend.email.normalizeEmail()
            val friendName = friend.name.trim().lowercase()

            (normalizedPhone.isNotBlank() && normalizedPhone == friendPhone) ||
                (normalizedEmail.isNotBlank() && normalizedEmail == friendEmail) ||
                (normalizedName.isNotBlank() && normalizedName == friendName && normalizedPhone.isNotBlank() && normalizedPhone == friendPhone)
        }
    }

    fun normalizePhone(input: String?): String {
        if (input.isNullOrBlank()) return ""
        val trimmed = input.trim()
        return try {
            val number: Phonenumber.PhoneNumber = phoneUtil.parse(trimmed, defaultRegion)
            if (phoneUtil.isValidNumber(number)) {
                phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164)
            } else {
                digitsFallback(trimmed)
            }
        } catch (_: NumberParseException) {
            digitsFallback(trimmed)
        }
    }

    private fun digitsFallback(s: String): String {
        val digits = s.filter { it.isDigit() }
        return if (digits.length > 10) digits.takeLast(10) else digits
    }

    private fun String?.normalizeEmail(): String {
        return this?.trim()?.lowercase() ?: ""
    }
}
