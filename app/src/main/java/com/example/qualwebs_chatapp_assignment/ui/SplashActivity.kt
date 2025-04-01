package com.example.qualwebs_chatapp_assignment.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.qualwebs_chatapp_assignment.R
import com.example.qualwebs_chatapp_assignment.data.PreferenceHelper

class SplashActivity : AppCompatActivity() {
    private lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_splash)
        preferenceHelper= PreferenceHelper(this)

        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, 3000) // 3 seconds delay


    }

    private fun checkLoginStatus() {
        val isLoggedIn = preferenceHelper.getUserLoggedIn()

        if (isLoggedIn) {
            startActivity(Intent(this, AllChatsScreen::class.java))
        } else {
            startActivity(Intent(this, SignInScreen::class.java))
        }
        finish()

    }
}