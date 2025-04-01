package com.example.qualwebs_chatapp_assignment.models

data class ChatPreview (
    val userId: String = "",
    val name: String = "",
    val profilePic: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L,
    val senderId: String = "",
    val seen: Boolean = true

)