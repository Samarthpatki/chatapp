package com.example.qualwebs_chatapp_assignment.data

import android.util.Log
import com.example.qualwebs_chatapp_assignment.models.ChatMessage
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ChatsRepository {
    private  val storage = FirebaseDatabase.getInstance("https://qualwebschatappassignment-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val storageRef= storage.getReference("chats")

    fun getChatMessagesRef(chatId: String): DatabaseReference {
        return FirebaseDatabase.getInstance().reference.child("chats").child(chatId)
    }


    fun sendMessage(chatId: String, message: ChatMessage, onSuccess:()-> Unit, onFailure:(String)->Unit){

        val messageRef= storageRef.child(chatId).push()
        val messageWithId = message.copy(messageId = messageRef.key?: "")


        messageRef.setValue(messageWithId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Message send failed") }



    }


    fun listenForMessages(chatId: String, onMessageReceived: (ChatMessage)->Unit,
                          onMessageDeleted: (String) -> Unit,
                          onLastMessageChanged: (String) -> Unit
    ){
        storageRef.child(chatId).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message= snapshot.getValue(ChatMessage::class.java)
                    message?.let{
                        onMessageReceived(it)
                    }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val updatedMessage = snapshot.getValue(ChatMessage::class.java)
                updatedMessage?.let {
                    onMessageReceived(it)
                    onLastMessageChanged(it.message)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val messageId = snapshot.key // The ID of the deleted message
                messageId?.let { onMessageDeleted(it) }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ChatRepoError", error.message)
            }

        })
    }
    fun editMessage(chatId: String, messageId: String, newMessage: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val messageRef = storageRef.child(chatId).child(messageId)
        messageRef.child("message").setValue(newMessage)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to edit message") }
    }

    fun deleteMessage(chatId: String, messageId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val messageRef = storageRef.child(chatId).child(messageId)
        messageRef.removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Failed to delete message") }
    }

//    fun markAllMessagesAsSeenCall(chatId:String){
//        storageRef.orderByChild("seen").equalTo(false).get().addOnSuccessListener { snapshot ->
//            for (message in snapshot.children) {
//                message.ref.child("seen").setValue(true)
//            }
//        }

//    }
fun markMessagesAsSeen(chatId: String, senderId: String, receiverId: String) {
    // Get a reference to the chat messages
    val chatMessagesRef = FirebaseDatabase.getInstance().reference.child("chats").child(chatId)

    // Query for all unseen messages sent by the receiver (messages that are unread by the sender)
    chatMessagesRef.orderByChild("seen")
        .equalTo(false) // Only retrieve messages that are not seen
        .get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { dataSnapshot ->
                val message = dataSnapshot.getValue(ChatMessage::class.java)
                if (message != null && message.receiverId == senderId) {
                    // Update the message's 'seen' field to true
                    dataSnapshot.ref.child("seen").setValue(true)
                        .addOnSuccessListener {
                            Log.d("SeenUpdate", "Message ${message.messageId} marked as seen.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("SeenUpdate", "Failed to mark message ${message.messageId} as seen: ${e.message}")
                        }
                }
            }
        }
}


}