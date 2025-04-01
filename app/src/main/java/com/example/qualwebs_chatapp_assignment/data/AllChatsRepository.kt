package com.example.qualwebs_chatapp_assignment.data

import android.util.Log
import com.example.qualwebs_chatapp_assignment.models.ChatPreview
import com.example.qualwebs_chatapp_assignment.models.UserProfile
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class AllChatsRepository {
    private val storage = FirebaseDatabase.getInstance("https://qualwebschatappassignment-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val storageRef = storage.getReference("profiles")
    private val recentChatsRef = storage.getReference("recent_chats")


    fun searchUserQueryCall(queryText :String , onSuccess : (UserProfile?) -> Unit , onFailure : (String)->Unit ){

        val query : Query =if(queryText.contains("@")){
            storageRef.orderByChild("email").equalTo(queryText)
        }else{
            storageRef.orderByChild("phone").equalTo(queryText)
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(userSnap in snapshot.children){
                        val user = userSnap.getValue(UserProfile::class.java)
                        user?.let {
                            onSuccess(user)
                            return
                        }
                    }
                }else{
                    onSuccess(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure(error.message)
            }

        })
    }

    fun updateRecentChats(senderId:String,senderName:String, senderPic:String ,
                          receiverID: String, receiverName:String, receiverPic:String,lastMessage:String){
        val timestamp = System.currentTimeMillis()
        val updatedLastMessage = lastMessage ?: "No messages" // Handle deletion

        val senderChat= ChatPreview(receiverID,receiverName,receiverPic,updatedLastMessage,timestamp,senderId)
        val receiverChat = ChatPreview(senderId, senderName, senderPic, updatedLastMessage, timestamp,receiverID)

        recentChatsRef.child(senderId).child(receiverID).setValue(senderChat)
        recentChatsRef.child(receiverID).child(senderId).setValue(receiverChat)

    }

    fun getAllRecentChatsCall(userId : String , onResult: (List<ChatPreview>)->Unit){
        recentChatsRef.child(userId).orderByChild("timestamp").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chatList = mutableListOf<ChatPreview>()
                    for (chatSnapshot in snapshot.children){
                        val chat= chatSnapshot.getValue(ChatPreview::class.java)
                        chat?.let {
                            chatList.add(it)

                        }

                    }
                        onResult(chatList)

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ChatREPOERROR",error.message)
                }
            }
        )



    }

    fun markMessagesAsSeen(chatId: String,senderId: String,receiverId: String) {
//        recentChatsRef.child(chatId).orderByChild("seen").equalTo(false).get().addOnSuccessListener { snapshot ->
//            for (message in snapshot.children) {
//                message.ref.child("seen").setValue(true)
//            }
//            updateAllChatsScreen(chatId) // Refresh adapter

        val senderChatRef = recentChatsRef.child(senderId).child(receiverId)
        val receiverChatRef = recentChatsRef.child(receiverId).child(senderId)
        Log.d("SeenUpdate", "SenderREF"+senderId)
        Log.d("SeenUpdate", "recevREF"+receiverId)

        senderChatRef.child("seen").setValue(true)
            .addOnSuccessListener {
                Log.d("SeenUpdate", "Sender chat seen updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("SeenUpdate", "Failed to update sender chat seen: ${e.message}")
            }

        receiverChatRef.child("seen").setValue(true)
            .addOnSuccessListener {
                Log.d("SeenUpdate", "Receiver chat seen updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("SeenUpdate", "Failed to update receiver chat seen: ${e.message}")
            }


    }


}