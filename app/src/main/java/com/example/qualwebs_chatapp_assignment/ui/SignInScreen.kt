package com.example.qualwebs_chatapp_assignment.ui

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.qualwebs_chatapp_assignment.R
import com.example.qualwebs_chatapp_assignment.data.PreferenceHelper
import com.example.qualwebs_chatapp_assignment.databinding.ActivitySignInScreenBinding
import com.example.qualwebs_chatapp_assignment.viewmodel.SignInAndSignUpViewmodel
import com.google.firebase.Firebase
import com.google.firebase.database.database

class SignInScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySignInScreenBinding
    private lateinit var viewmodel: SignInAndSignUpViewmodel
    private lateinit var preferenceHelper: PreferenceHelper
    private val REQUEST_CAMERA = 100
    private val REQUEST_GALLERY = 101
    private val CAMERA_PERMISSION_CODE = 102



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        checkAndRequestPermissions()
        binding= DataBindingUtil.setContentView(this, R.layout.activity_sign_in_screen)

        viewmodel= ViewModelProvider(this).get(SignInAndSignUpViewmodel::class.java)
        binding.lifecycleOwner= this



        binding.viewmodel= viewmodel



        binding.signInBtn.setOnClickListener { view ->
            val emailPhone = binding.emailEditTxt.text.toString().trim()
            val password= binding.passEditTxt.text.toString().trim()

            if(emailPhone.isNotEmpty() && password.isNotEmpty()){
                binding.progressBar.visibility= View.VISIBLE
                 binding.viewmodel?.signInUserRepoCall(
                    emailPhone,password,

//                    onSuccess = {
//
//
//                        val intent= Intent(this, AllChatsScreen::class.java)
//                        startActivity(intent)
//
//                    },
//                    onFailure = {error ->
//                        Toast.makeText(this, "Sign in failed ${error.toString()}",Toast.LENGTH_LONG).show()
//                    }
                )



            }else{

                Toast.makeText(this, "Please fill all fields ",Toast.LENGTH_LONG).show()
            }



        }
        preferenceHelper= PreferenceHelper(this)

        viewmodel.userLiveData.observe(this) { user ->
            user?.let {
                binding.progressBar.visibility= View.VISIBLE
                Toast.makeText(this, "Welcome ${it.name}", Toast.LENGTH_SHORT).show()
                preferenceHelper.setLoggedIn(true)
                preferenceHelper.saveUserName(it.name)
                preferenceHelper.saveUserID(it.uid)
                preferenceHelper.saveUserEmail(it.email)
                preferenceHelper.saveUserNumber(it.phone)
                preferenceHelper.saveUserPic(it.profilePic)
                val intent= Intent(this, AllChatsScreen::class.java)
                intent.flags= Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

//                preferenceHelper.saveUserBio("")

            }
        }
        viewmodel.errorLiveData.observe(this){
            error->
            binding.progressBar.visibility= View.GONE
            Toast.makeText(this, "Sign in failed ${error.toString()}", Toast.LENGTH_SHORT).show()

        }




    }
    private fun checkAndRequestPermissions() {
        // List of permissions to request
        val permissions = mutableListOf(Manifest.permission.CAMERA)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {  // Android 12 (API 32) and below
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {  // For Android 13 and above
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        }

        // Check if all permissions are granted
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        // If there are any permissions to request, ask for them
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), CAMERA_PERMISSION_CODE)
        } else {
            // All permissions granted
//            proceedWithApp()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            // Check if all permissions are granted
            val allPermissionsGranted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (allPermissionsGranted) {
                // Proceed with app functionality
//                proceedWithApp()
            } else {
                // If permissions are not granted, close the app
                Toast.makeText(this, "Permissions are required to use the camera and gallery!", Toast.LENGTH_SHORT).show()
                finish()  // Close the app
            }
        }
    }




}