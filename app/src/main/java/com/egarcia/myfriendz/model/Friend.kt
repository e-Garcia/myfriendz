package com.egarcia.myfriendz.model

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.egarcia.myfriendz.showFriend.utils.APP_DATE_FORMAT
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

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
        val formatter = DateTimeFormatter.ofPattern(APP_DATE_FORMAT)
        return lastContacted.format(formatter)
    }

    fun setLastContactedFormatted(date: String) {
        val formatter = DateTimeFormatter.ofPattern(APP_DATE_FORMAT)
        try {
            lastContacted = LocalDate.parse(date, formatter)
        } catch (e: DateTimeParseException) {
            Log.e(TAG, "Failed to parse date: $date with format: $APP_DATE_FORMAT", e)
            return
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error parsing date: $date", e)
            return
        }
    }

    companion object {
        private const val TAG = "Friend"
    }
}