package com.egarcia.myfriendz.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Model class, uses room annotations for data storage.
 * @see FriendDao
 */
@Entity(tableName = "friends")
data class Friend(
    var name: String,
    @ColumnInfo(name = "last_contacted")
    var lastContacted: LocalDate,
    var frequency: String,
    var phone: String,
    var email: String,
    var comments: String,
) {
    @PrimaryKey(autoGenerate = true)
    var uuid: Int = 0

    fun getLastContactedFormatted(): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return lastContacted.format(formatter)
    }
}