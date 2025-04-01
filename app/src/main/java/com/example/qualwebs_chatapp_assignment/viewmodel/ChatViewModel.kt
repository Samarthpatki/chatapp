package com.example.qualwebs_chatapp_assignment.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.qualwebs_chatapp_assignment.data.AllChatsRepository
import com.example.qualwebs_chatapp_assignment.data.ChatsRepository
import com.example.qualwebs_chatapp_assignment.models.ChatMessage

class ChatViewModel:ViewModel()
{
        private val chatsRepository=ChatsRepository()
        private val allChatsRepository=AllChatsRepository()
        private val _messages= MutableLiveData<List<ChatMessage>>()
        val messages:LiveData<List<ChatMessage>> get() = _messages


        private val chatMessages= mutableListOf<ChatMessage>()

        fun sendMessage(chatId:String, message: ChatMessage,senderName: String,senderPic: String,receiverId: String, receiverName: String, receiverPic: String){
            val messageRef = chatsRepository.getChatMessagesRef(chatId).push()
            val messageWithId = message.copy(messageId = messageRef.key ?: "")

            chatsRepository.sendMessage(chatId,message, onSuccess = {
                Log.d("ChatVM", "Message sent")
                allChatsRepository.updateRecentChats(
                    senderId = messageWithId.senderId,
                    senderName = senderName,
                    senderPic = senderPic,
                    receiverID = receiverId,
                    receiverName = receiverName,
                    receiverPic = receiverPic,
                    lastMessage = message.message
                )

            }, onFailure = {
                Log.d("ChatVM", "Message failed : $it")

            })

        }

    fun listenForMessage(chatId: String, receiverId: String, senderName: String, senderPic: String, receiverName: String, receiverPic: String) {
        chatsRepository.listenForMessages(chatId, { message ->
            val existingIndex = chatMessages.indexOfFirst { it.messageId == message.messageId }

            if (existingIndex != -1) {
                chatMessages[existingIndex] = message
            } else {
                chatMessages.add(message)
            }
            _messages.postValue(chatMessages.toList())

            allChatsRepository.updateRecentChats(
                senderId = message.senderId, // Receiver's ID becomes sender in their chat list
                senderName = if (message.senderId == receiverId) receiverName else senderName,
                senderPic = if (message.senderId == receiverId) receiverPic else senderPic,
                receiverID = message.receiverId,
                receiverName = if (message.senderId == receiverId) senderName else receiverName,
                receiverPic = if (message.senderId == receiverId) senderPic else receiverPic,
                lastMessage = message.message,
             )
        }, { messageId ->
            removeLocalMessage(messageId,senderName,senderPic,receiverName,receiverPic)
        },{


            val lastMessage = chatMessages.lastOrNull()?.message ?: "No messages"
            allChatsRepository.updateRecentChats(
                senderId = receiverId,
                senderName = receiverName,
                senderPic = receiverPic,
                receiverID = receiverId,
                receiverName = senderName,
                receiverPic = senderPic,
                lastMessage = lastMessage// Update last message
            )
        }

        )
    }



    fun editMessage(chatId: String, messageId: String, newMessage: String) {
        chatsRepository.editMessage(chatId, messageId, newMessage, onSuccess = {
            Log.d("ChatVM", "Message edited successfully")
            updateLocalMessage(messageId, newMessage)
        }, onFailure = {
            Log.d("ChatVM", "Failed to edit message: $it")
        })
    }

    fun deleteMessage(chatId: String, messageId: String,senderName: String,senderPic: String,receiverName: String,receiverPic: String) {
        chatsRepository.deleteMessage(chatId, messageId, onSuccess = {
            Log.d("ChatVM", "Message deleted successfully")
            removeLocalMessage(messageId,senderName,senderPic,receiverName,receiverPic)
        }, onFailure = {
            Log.d("ChatVM", "Failed to delete message: $it")
        })
    }

     private fun updateLocalMessage(messageId: String, newMessage: String) {
        chatMessages.find { it.messageId == messageId }?.message = newMessage
        _messages.postValue(chatMessages.toList())
    }

     private fun removeLocalMessage(messageId: String,senderName: String,senderPic: String,receiverName: String,receiverPic: String) {
        val removedMessage = chatMessages.find { it.messageId == messageId }
        chatMessages.removeAll { it.messageId == messageId }

        _messages.postValue(chatMessages.toList())

        if (removedMessage != null) {
            val lastMessage = chatMessages.lastOrNull()?.message ?: "No messages"
            allChatsRepository.updateRecentChats(
                senderId = removedMessage.senderId,
                senderName = senderName,
                senderPic = senderPic,
                receiverID = removedMessage.receiverId,
                receiverName = receiverName,
                receiverPic = receiverPic,
                lastMessage = lastMessage,
             )
        }
    }

    fun markAllMessagesAsSeen(chatId: String,senderId:String, receiverId: String) {
//        chatsRepository.markAllMessagesAsSeenCall(chatId,senderId,receiverId)
//        allChatsRepository.markMessagesAsSeen(chatId,senderId,receiverId)
    }









}