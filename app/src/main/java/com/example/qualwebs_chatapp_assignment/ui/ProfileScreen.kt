package com.example.qualwebs_chatapp_assignment.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.qualwebs_chatapp_assignment.R
import com.example.qualwebs_chatapp_assignment.data.PreferenceHelper
import com.example.qualwebs_chatapp_assignment.databinding.ActivityProfileScreenBinding
import com.example.qualwebs_chatapp_assignment.viewmodel.ProfileScreenViewModel

class ProfileScreen : AppCompatActivity() {

    private lateinit var binding : ActivityProfileScreenBinding
    private lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding= DataBindingUtil.setContentView(this, R.layout.activity_profile_screen)
        binding.viewmodel= ProfileScreenViewModel()
        binding.lifecycleOwner= this
        preferenceHelper= PreferenceHelper(this)

        binding.backbtn.setOnClickListener {
            finish()
        }

        binding.userName.text=  preferenceHelper.getUserName()
        binding.email.text=  preferenceHelper.getUserEmail()
        binding.phone.text=  preferenceHelper.getUserNumber()
        preferenceHelper.getUserPic()?.let {
            if(preferenceHelper.getUserPic()!=""){
                binding.profilePic.setImageBitmap(base64ToBitmap(preferenceHelper.getUserPic()))
            }

        }

        binding.logoutBtn.setOnClickListener {
            preferenceHelper.clear()
            val intent = Intent(this, SignInScreen::class.java)
            intent.flags= Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

        }


    }
    fun base64ToBitmap(base64String: String?): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
    }

}