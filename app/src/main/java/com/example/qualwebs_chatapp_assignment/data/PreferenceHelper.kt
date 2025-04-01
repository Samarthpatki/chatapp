package com.example.qualwebs_chatapp_assignment.data

import android.annotation.SuppressLint
import android.content.Context

class PreferenceHelper(@SuppressLint("RestrictedApi") context : Context) {

    private val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)

    fun saveUserEmail(email: String) {
        sharedPreferences.edit().putString("USER_EMAIL", email).apply()
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString("USER_EMAIL", null)
    }


    fun saveUserName(name: String) {
        sharedPreferences.edit().putString("USER_NAME", name).apply()
    }

    fun getUserName(): String? {
        return sharedPreferences.getString("USER_NAME", null)
    }
    fun saveUserID(uid: String) {
        sharedPreferences.edit().putString("USER_ID", uid).apply()
    }

    fun getUserID(): String? {
        return sharedPreferences.getString("USER_ID", null)
    }

    fun saveUserBio(bio: String) {
        sharedPreferences.edit().putString("USER_Bio", bio).apply()
    }

    fun getUserBio(): String? {
        return sharedPreferences.getString("USER_Bio", null)
    }

    fun saveUserNumber(number: String) {
        sharedPreferences.edit().putString("USER_Number", number).apply()
    }

    fun getUserNumber(): String? {
        return sharedPreferences.getString("USER_Number", null)
    }

    fun saveUserPic(pic: String) {
        sharedPreferences.edit().putString("USER_PIC", pic).apply()
    }

    fun getUserPic(): String? {
        return sharedPreferences.getString("USER_PIC", null)
    }



    fun setLoggedIn(isLoggedIn : Boolean){
        sharedPreferences.edit().putBoolean("LOGIN_STATUS", isLoggedIn).apply()


    }
    fun getUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("LOGIN_STATUS", false)
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

}