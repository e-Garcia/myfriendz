package com.egarcia.myfriendz.importContacts.model

data class ImportContactItem(
    val id: String,
    val name: String,
    val phone: String?,
    val email: String?,
    val isSelected: Boolean = false
)
