package com.egarcia.myfriendz.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * The main database of the application. Contains an array of Friends. Uses the Room library.
 * @see Friend as it's the model class to be stored in the database.
 * @see FriendDao as it controls how the main table on the database is queried and updated.
 */
@Database(entities = [Friend::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FriendDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendDao
}