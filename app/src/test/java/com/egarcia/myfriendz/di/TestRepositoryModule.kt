package com.egarcia.myfriendz.di

import com.egarcia.myfriendz.RepositoryModule
import com.egarcia.myfriendz.domain.repository.FriendRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

/**
 * Test-only Hilt module that replaces the production RepositoryModule.
 * Provides a mock FriendRepository for unit testing.
 *
 * This module uses @TestInstallIn to replace the production RepositoryModule
 * automatically during tests, allowing ViewModels to receive mock dependencies.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
object TestRepositoryModule {

    @Provides
    @Singleton
    fun provideMockFriendRepository(): FriendRepository = mockk(relaxed = true)
}
