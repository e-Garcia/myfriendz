package com.egarcia.myfriendz

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyFriendzApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        com.google.firebase.FirebaseApp.initializeApp(this)
    }
}
