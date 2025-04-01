package com.example.qualwebs_chatapp_assignment.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.qualwebs_chatapp_assignment.R
import com.example.qualwebs_chatapp_assignment.data.PreferenceHelper
import com.example.qualwebs_chatapp_assignment.databinding.ActivityChatScreenBinding
import com.example.qualwebs_chatapp_assignment.models.ChatMessage
import com.example.qualwebs_chatapp_assignment.models.UserProfile
import com.example.qualwebs_chatapp_assignment.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream

class ChatScreen : AppCompatActivity() {
    private lateinit var binding : ActivityChatScreenBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var preferenceHelper: PreferenceHelper
    private var chatMessages = mutableListOf<ChatMessage>()
    private val chatViewModel: ChatViewModel by viewModels()
    private val REQUEST_CAMERA = 100
    private val REQUEST_GALLERY = 101
    private val CAMERA_PERMISSION_CODE = 102
    private var receiverId = ""
    private var receiverName = ""
    private var receiverPic = ""
    private var receiverPicBitmap : Bitmap? = null
    private var chatId=""


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
       binding=ActivityChatScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceHelper = PreferenceHelper(this)
        val recvUser= intent.getSerializableExtra("USER") as? UserProfile
        val currentUser= FirebaseAuth.getInstance().currentUser
        chatId = generateChatId(currentUser?.uid, recvUser?.uid)
         recvUser?.let{
            binding.receiverName.text=recvUser.name
            receiverId = recvUser.uid
             receiverName = recvUser.name
             receiverPic = recvUser.profilePic
            if (receiverPic!=""){
                receiverPicBitmap= base64ToBitmap(receiverPic)
                binding.recvProfileImage.setImageBitmap(receiverPicBitmap)
            }
        }


        binding.bckbtn.setOnClickListener {
            finish()
        }


        chatViewModel.messages.observe(this){
            newMsgs ->
            chatMessages.clear()
            chatMessages.addAll(newMsgs)
            chatAdapter.notifyDataSetChanged()
            binding.recyclerChat.scrollToPosition(chatMessages.size-1)

        }
        chatViewModel.listenForMessage(chatId, receiverId,senderName = preferenceHelper.getUserName() ?: "", senderPic = preferenceHelper.getUserPic() ?: "",receiverName,receiverPic)
        chatAdapter= ChatAdapter(chatMessages , currentUid =currentUser?.uid?:"", ::onEditMessage , ::onDeleteMessage,receiverPicBitmap)
        binding.recyclerChat.apply {
            layoutManager=LinearLayoutManager(this@ChatScreen)
            adapter=chatAdapter

        }
        binding.btnSendImage.setOnClickListener { view ->
            showImagePickerDialog()
        }
        binding.btnSend.setOnClickListener {
            val messageTxt= binding.editMessage.text.toString().trim()
            if(messageTxt.isNotEmpty()){
                val chatMessage =ChatMessage(
                    messageId = "",
                    senderId = currentUser?.uid?:"",
                    receiverId = recvUser?.uid ?: "",
                    message = messageTxt,
                    timestamp = System.currentTimeMillis(),
                    seen = false)
                chatViewModel.sendMessage(chatId,chatMessage, senderName = preferenceHelper.getUserName() ?: "", senderPic = preferenceHelper.getUserPic() ?: "",receiverId,receiverName,receiverPic)

                 binding.editMessage.text.clear()

            }
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

    override fun onResume() {
        super.onResume()
//        markAllMessagesAsSeen(chatId,preferenceHelper.getUserID(),receiverId)
    }


    private fun markAllMessagesAsSeen(chatId: String,senderId: String?,receiverId: String?) {
        if (receiverId != null) {
            if (senderId != null) {
                chatViewModel.markAllMessagesAsSeen(chatId,senderId,receiverId)
            }
        }
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
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }



    private fun generateChatId(senderId:String?, receiverId:String?) :String{
        return if(senderId!=null && receiverId!==null){
            listOf(senderId,receiverId).sorted().joinToString("_")
        }else{
            ""
        }
    }
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            val imageBitmap: Bitmap? = when (requestCode) {
                REQUEST_CAMERA -> data?.extras?.get("data") as? Bitmap
                REQUEST_GALLERY -> {
                    val uri = data?.data
                    uri?.let {
                        getBitmapFromUri(it) }
                }
                else -> null
            }

            imageBitmap?.let { bitmap ->
                val base64String = convertBitmapToBase64(bitmap)
                sendImageMessage(base64String)
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun sendImageMessage(base64Image: String?) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val chatId = generateChatId(currentUser?.uid, receiverId)

        val chatMessage = ChatMessage(
            messageId = "",
            senderId = currentUser?.uid ?: "",
            receiverId = receiverId,
            message = "",
            imageMsg = base64Image ?: "",
            timestamp = System.currentTimeMillis(),
            seen = false
        )

        chatViewModel.sendMessage(chatId, chatMessage, senderName = preferenceHelper.getUserName() ?: "", senderPic = preferenceHelper.getUserPic() ?: "", receiverId, receiverName, receiverPic)
    }


    private fun deleteMessage(messageId: String) {
        chatViewModel.deleteMessage(chatId, messageId, senderName = preferenceHelper.getUserName() ?: "", senderPic = preferenceHelper.getUserPic() ?: "" ,receiverName,receiverPic)
    }

    private fun onEditMessage(message: ChatMessage,position:Int) {
        val input = EditText(this)
        input.setText(message.message)

        AlertDialog.Builder(this)
            .setTitle("Edit Message")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newMessage = input.text.toString().trim()
                if (newMessage.isNotEmpty() && newMessage != message.message) {
                    chatViewModel.editMessage(chatId, message.messageId, newMessage)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()

//        binding.editMessage.setText(message.message)
    }

    private fun onDeleteMessage(message: ChatMessage) {
        AlertDialog.Builder(this)
            .setTitle("Delete Message")
            .setMessage("Are you sure you want to delete this message?")
            .setPositiveButton("Delete") { _, _ -> deleteMessage(message.messageId) }
            .setNegativeButton("Cancel", null)
            .show()
    }




}