package com.egarcia.myfriendz

import android.content.Context
import androidx.room.Room
import com.egarcia.myfriendz.model.FriendDao
import com.egarcia.myfriendz.model.FriendDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFriendDatabase(@ApplicationContext appContext: Context): FriendDatabase {
        return Room.databaseBuilder(
            appContext,
            FriendDatabase::class.java,
            "friend_database"
        ).build()
    }

    @Provides
    fun provideFriendDao(database: FriendDatabase): FriendDao {
        return database.friendDao()
    }
}