package com.example.qualwebs_chatapp_assignment.models

data class ChatMessage(

 val messageId: String = "",
 val senderId: String = "",
 val receiverId: String = "",
 var message: String = "",
 val imageMsg: String? = "",
 val timestamp: Long = 0,
 val seen: Boolean = false,
// val isSentByUser: Boolean = false


)
