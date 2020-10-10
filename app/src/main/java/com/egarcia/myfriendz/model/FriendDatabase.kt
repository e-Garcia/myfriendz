package com.egarcia.myfriendz.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The main database of the application. Contains an array of Friends. Uses the Room library.
 * @see Friend as it's the model class to be stored in the database.
 * @see FriendDao as it controls how the main table on the database is queried and updated.
 */
@Database(entities = arrayOf(Friend::class), version = 1)
abstract class FriendDatabase: RoomDatabase() {
    abstract fun friendDao(): FriendDao

    companion object {
        @Volatile private var instance: FriendDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance?: synchronized(LOCK) {
            instance?:buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context,
        FriendDatabase::class.java,
        "frienddatabase"
        ).build()
    }
}