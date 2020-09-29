package com.egarcia.myfriendz.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Friend(

    var name: String,
    @ColumnInfo(name = "last_contacted")
    var lastContacted: String,
    var frequency: String,
    var phone: String,
    var email: String,
    var comments: String,
) {
    @PrimaryKey(autoGenerate = true)
    var uuid: Int = 0
}