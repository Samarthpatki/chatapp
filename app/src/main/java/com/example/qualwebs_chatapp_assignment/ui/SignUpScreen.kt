package com.example.qualwebs_chatapp_assignment.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.qualwebs_chatapp_assignment.R
import com.example.qualwebs_chatapp_assignment.databinding.ActivitySignUpScreenBinding
import com.example.qualwebs_chatapp_assignment.viewmodel.SignInAndSignUpViewmodel
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import kotlin.math.min

class SignUpScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpScreenBinding
    private val REQUEST_CAMERA = 100
    private val REQUEST_GALLERY = 101
    private val CAMERA_PERMISSION_CODE = 102

    private lateinit var profilePic: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up_screen)
        binding.viewmodel = SignInAndSignUpViewmodel()
        binding.lifecycleOwner = this


        binding.addProfileImg.setOnClickListener { view ->
            showImagePickerDialog()
        }

        binding.signUpBtn.setOnClickListener { view ->

            val name = binding.nameEditTxt.text.toString().trim()
            val email = binding.emailSignupEdittext.text.toString().trim()
            val phone = binding.numberSignupEdittext.text.toString().trim()
            val password = binding.passwordSignupEdittext.text.toString().trim()
            val confirmPassword = binding.confirmPassSignupEdittext.text.toString().trim()

            if(password != confirmPassword){
                Toast.makeText(this, "Passwords don't match" , Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            Log.d("CheckClick", "1")

            if (name.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && password.length > 6) {
                Log.d("CheckClick", "1")
                binding.progressBar.visibility= View.VISIBLE
                binding.viewmodel?.signUpUserRepoCall(
                    name, email, phone, password, profilePic ?: "",
                    onSuccess = {
                        binding.progressBar.visibility= View.GONE
                        Toast.makeText(this, "Sign Up successful , Please Sign in", Toast.LENGTH_LONG).show()
                        val intent =Intent(this, SignInScreen::class.java)
                        intent.flags= Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    },
                    onFailure = { error ->
                        binding.progressBar.visibility= View.GONE
                        Toast.makeText(this, "Sign Up Failed $error", Toast.LENGTH_LONG).show()

                    }
                )

            } else {
                Toast.makeText(
                    this,
                    "Please Fill all details properly with password length greater than 6",
                    Toast.LENGTH_LONG
                ).show()

            }

        }


    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Select Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openCamera() {

        if (!checkCameraPermission()) {
            requestCameraPermission()
            return
        }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_CAMERA)
        } else {
            Toast.makeText(this, "No camera app found!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        if (!checkCameraPermission()) {
            requestCameraPermission()
            return
        }

        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_GALLERY -> {
                    val imageUri = data?.data
                    imageUri?.let { cropCircular(it) }
                }
                REQUEST_CAMERA -> {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        val circularBitmap = getCircularBitmap(it) // Directly make circular
                        binding.addProfileImg.setImageBitmap(circularBitmap)
                        profilePic = convertToBase64(circularBitmap)
                    }
                }
            }
        }
    }

    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = min(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
            shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }

        val rect = Rect(0, 0, size, size)
        val rectF = RectF(rect)

        val path = Path().apply {
            addOval(rectF, Path.Direction.CCW)
        }

        canvas.drawPath(path, paint)
        return output
    }

    private fun cropCircular(uri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val circularBitmap = getCircularBitmap(bitmap)
            binding.addProfileImg.setImageBitmap(circularBitmap)
            profilePic = convertToBase64(circularBitmap)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }



    fun convertToBase64(bitmap: Bitmap): String {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true) // Resize to 200x200
        val byteArrayOutputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream) // 50% Quality
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }


     private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
//        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        val permissions = mutableListOf(Manifest.permission.CAMERA)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {  // Android 12 (API 32) and below
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else  {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        }

        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), CAMERA_PERMISSION_CODE)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required!", Toast.LENGTH_SHORT).show()
            }
        }
    }



}












