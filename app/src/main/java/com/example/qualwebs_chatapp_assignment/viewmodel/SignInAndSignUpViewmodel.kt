package com.example.qualwebs_chatapp_assignment.viewmodel

import android.content.Intent
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.qualwebs_chatapp_assignment.data.SignUpAndSignInRepository
import com.example.qualwebs_chatapp_assignment.models.UserProfile
import com.example.qualwebs_chatapp_assignment.ui.ForgotPasswordScreen
import com.example.qualwebs_chatapp_assignment.ui.SignInScreen
import com.example.qualwebs_chatapp_assignment.ui.SignUpScreen

class SignInAndSignUpViewmodel: ViewModel() {
    private val signupAndSignInRepo = SignUpAndSignInRepository()


    private val _userLiveData = MutableLiveData<UserProfile?>()
    val userLiveData: LiveData<UserProfile?> = _userLiveData

    private val _errorLiveData = MutableLiveData<String?>()
    val errorLiveData: LiveData<String?> = _errorLiveData

    fun onSignUpTxtClicked(view: View){
        val context= view.context
        val intent = Intent(context, SignUpScreen::class.java)
        context.startActivity(intent)
    }

    fun onSignInTxtClicked(view: View){
        val context= view.context
        val intent = Intent(context, SignInScreen::class.java)
        intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    fun onForgotPasswordTxtClicked(view: View){
        val context= view.context
        val intent = Intent(context, ForgotPasswordScreen::class.java)
        context.startActivity(intent)
    }

    fun signUpUserRepoCall(
        name:String,
        email:String,
        phone:String,
        password:String,
        profilePic:String?,
        onSuccess:()-> Unit,
        onFailure:(String)-> Unit,

    ){
        signupAndSignInRepo.signUpUser(name,email,phone,password,profilePic ?: "",onSuccess,onFailure)

    }

    fun signInUserRepoCall(
        emailPhone:String,
        password:String,
    ){
        signupAndSignInRepo.signInUser(emailPhone, password,onSuccess={
                user ->
            _userLiveData.postValue(user)
        },onFailure={
                error ->
            _errorLiveData.postValue(error)
        }
        )
    }

}