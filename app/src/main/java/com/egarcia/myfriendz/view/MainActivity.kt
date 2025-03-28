package com.egarcia.myfriendz.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.egarcia.myfriendz.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Host activity for the application's fragments such as the friends list and friend details.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}