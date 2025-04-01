package com.example.qualwebs_chatapp_assignment.viewmodel

import android.content.Intent
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.qualwebs_chatapp_assignment.data.AllChatsRepository
import com.example.qualwebs_chatapp_assignment.models.ChatPreview
import com.example.qualwebs_chatapp_assignment.models.UserProfile
import com.example.qualwebs_chatapp_assignment.ui.ProfileScreen

class AllChatsViewModel:ViewModel() {

    private val allChatsRepository =AllChatsRepository()


    private val _userLiveData = MutableLiveData<UserProfile?>()
    val userLiveData: LiveData<UserProfile?> get() = _userLiveData

    private val _errorLiveData = MutableLiveData<String>()
    val errorLiveData: LiveData<String> get() = _errorLiveData

    private val _recentChatsLiveData = MutableLiveData<List<ChatPreview>>()
    val recentChatsLiveData: LiveData<List<ChatPreview>> get() = _recentChatsLiveData




    fun onProfileIconClicked(view : View){
        val intent = Intent(view.context , ProfileScreen :: class.java)
        view.context.startActivity(intent)
    }

    fun onSearchUserClicked(queryText : String){
        allChatsRepository.searchUserQueryCall(queryText,onSuccess = {
            user -> _userLiveData.postValue(user)
        },onFailure ={
            error -> _errorLiveData.postValue(error)
        })
    }

    fun fetchRecentChats(userId: String){
        allChatsRepository.getAllRecentChatsCall(userId){
            chats ->
            _recentChatsLiveData.value=chats
        }
    }



}