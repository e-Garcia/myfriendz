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
@Database(entities = [Friend::class], version = 1, exportSchema = false)
abstract class FriendDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendDao

    companion object {
        @Volatile
        private var instance: FriendDatabase? = null

        fun getInstance(context: Context): FriendDatabase {
            // Double-checked locking for thread safety
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): FriendDatabase {
            return Room.databaseBuilder(
                context.applicationContext, // Use application context to avoid memory leaks
                FriendDatabase::class.java,
                "friends_database" // More descriptive database name
            ).build()
        }
    }
}