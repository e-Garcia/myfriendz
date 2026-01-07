@file:Suppress("unused")

package com.egarcia.myfriendz.di

import android.content.Context
import android.content.ContentResolver
import com.egarcia.myfriendz.data.contacts.AndroidContactsRepository
import com.egarcia.myfriendz.domain.contacts.ContactsRepository
import com.egarcia.myfriendz.data.permission.AndroidPermissionChecker
import com.egarcia.myfriendz.domain.permission.PermissionGateway
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(SingletonComponent::class)
abstract class ContactsModule {

    @Binds
    abstract fun bindContactsRepository(
        impl: AndroidContactsRepository
    ): ContactsRepository

    @Binds
    abstract fun bindPermissionGateway(
        impl: AndroidPermissionChecker
    ): PermissionGateway

    companion object {
        @Provides
        fun provideContentResolver(@ApplicationContext context: Context): ContentResolver =
            context.contentResolver
    }
}
