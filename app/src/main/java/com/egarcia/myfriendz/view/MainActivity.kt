package com.egarcia.myfriendz.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.egarcia.myfriendz.R

/**
 * Host activity for the application's fragments such as the friends list and friend details.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}