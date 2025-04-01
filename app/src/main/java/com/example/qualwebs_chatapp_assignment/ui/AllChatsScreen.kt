package com.example.qualwebs_chatapp_assignment.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.qualwebs_chatapp_assignment.R
import com.example.qualwebs_chatapp_assignment.data.PreferenceHelper
import com.example.qualwebs_chatapp_assignment.databinding.ActivityAllChatsScreenBinding
import com.example.qualwebs_chatapp_assignment.models.ChatPreview
import com.example.qualwebs_chatapp_assignment.models.UserProfile
import com.example.qualwebs_chatapp_assignment.viewmodel.AllChatsViewModel
import com.example.qualwebs_chatapp_assignment.viewmodel.SignInAndSignUpViewmodel

class AllChatsScreen : AppCompatActivity() {
    private lateinit var binding : ActivityAllChatsScreenBinding
    private  lateinit var viewModel: AllChatsViewModel
    private lateinit var signInVM : SignInAndSignUpViewmodel
    private lateinit var preferenceHelper: PreferenceHelper
    private  var processedChatList: List<ChatPreview> ?=null
    private  var   adapter: AllChatsAdapter ?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding= DataBindingUtil.setContentView(this, R.layout.activity_all_chats_screen)
        binding.lifecycleOwner= this

        viewModel=ViewModelProvider(this).get(AllChatsViewModel::class.java)
        signInVM=ViewModelProvider(this).get(SignInAndSignUpViewmodel::class.java)
        binding.viewmodel= viewModel

        preferenceHelper= PreferenceHelper(this)

        preferenceHelper.getUserID()?.let { viewModel.fetchRecentChats(it) }





          adapter= AllChatsAdapter(

            onChatClick = {
                chats ->

                val userProfile = UserProfile(chats.userId,chats
                    .name,"" ,"",chats.profilePic)
                startChatActivity(userProfile)
            },
            emptyList(),
              preferenceHelper.getUserID()
        )

        viewModel.recentChatsLiveData.observe(this){
                chatList ->
                Log.d("RecentChats", "Fetched Chats: $chatList")
             processedChatList = chatList.map { chat ->
                val isCurrentUserSender = chat.senderId == preferenceHelper.getUserID()

                ChatPreview(
                    userId = chat.userId,
                    name = chat.name,
                    profilePic = chat.profilePic,
                    lastMessage = chat.lastMessage,
                    timestamp = chat.timestamp,
                    senderId = chat.senderId,
                    seen = chat.seen
                )
            }

            adapter?.updateChats(processedChatList)

        }

        binding.allChatsRecycler.layoutManager=LinearLayoutManager(this)
        binding.allChatsRecycler.adapter=adapter
        binding.allChatsRecycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        binding.profilepic.setOnClickListener {
            val intent = Intent(this , ProfileScreen :: class.java)
            startActivity(intent)

        }
        if(preferenceHelper.getUserPic()!=""){

        val bitmap = base64ToBitmap(preferenceHelper.getUserPic() ?: "")
        binding.profilepic.setImageBitmap(bitmap) // Display in ImageView
        }


        binding.fabNewMessage.setOnClickListener {
            showSearchDialog()
        }

        viewModel.userLiveData.observe(this){
            user ->
            if(user !=null){
                startChatActivity(user)
            }else {
                Toast.makeText(this, "User not found ! ", Toast.LENGTH_LONG).show()
            }
        }

    }
    private fun startChatActivity(user: UserProfile) {
        val intent = Intent(this, ChatScreen::class.java).apply {
            putExtra("USER", user)
         }
        startActivity(intent)
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


    @SuppressLint("InflateParams")
    private fun showSearchDialog() {
        val dialogView =  LayoutInflater.from(this).inflate(R.layout.search_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        val etUserInput = dialogView.findViewById<EditText>(R.id.etUserInput)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSearch = dialogView.findViewById<Button>(R.id.btnSearch)

        btnCancel.setOnClickListener{dialog.dismiss()}

        btnSearch.setOnClickListener {
            val queryText = etUserInput.text.toString().trim()
            if(queryText.isNotEmpty()){
                dialog.dismiss()
                viewModel.onSearchUserClicked(queryText)


            }else{
                Toast.makeText(this, "Please enter a valid input",Toast.LENGTH_LONG).show()
            }
        }
            dialog.show()
    }


    override fun onResume() {
        super.onResume()
//       if(
//          processedChatList!=null && processedChatList?.isNotEmpty()==true
//       ) {
//
//           if(adapter!=null){
//
//           adapter?.updateChats(processedChatList!!)
//           }
//       }

//        preferenceHelper.getUserID()?.let { viewModel.fetchRecentChats(it) }


    }
}