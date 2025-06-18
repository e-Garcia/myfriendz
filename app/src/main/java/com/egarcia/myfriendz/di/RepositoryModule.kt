package com.egarcia.myfriendz.di

import com.egarcia.myfriendz.model.FriendDao
import com.egarcia.myfriendz.model.FriendFirebaseRepository
import com.egarcia.myfriendz.model.FriendRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideFriendRepository(
        dao: FriendDao,
        firebase: FriendFirebaseRepository
    ): FriendRepository = FriendRepository(dao, firebase)

    @Provides
    @Singleton
    fun provideFriendFirebaseRepository(): FriendFirebaseRepository = FriendFirebaseRepository()
}

