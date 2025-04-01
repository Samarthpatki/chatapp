package com.example.qualwebs_chatapp_assignment.data

import android.util.Log
import com.example.qualwebs_chatapp_assignment.models.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.auth.User

class SignUpAndSignInRepository {
    private  val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseDatabase.getInstance("https://qualwebschatappassignment-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val storageRef = storage.getReference("profiles")

    fun signUpUser(
        name: String,
        email: String,
        phone: String,
        password: String,
        profilePic:String?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ){
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
            task ->

            if(task.isSuccessful){
                val uid= auth.currentUser?.uid ?: return@addOnCompleteListener
                val user= UserProfile(uid,name,email,phone,profilePic ?: "")
                Log.d("InRepo", uid)

                storageRef
                    .child(uid)
                    .setValue(user)

                    .addOnSuccessListener{
                    Log.d("FirebaseDB", "User data stored successfully")

                    onSuccess()  }
                    .addOnFailureListener {

                    error ->
                        onFailure(error.message ?: "Error!")
                }
            }else{
                onFailure(task.exception?.message ?:"Signup failed")
            }

        }
    }


    fun signInUser(emailPhone: String, password: String,onSuccess : (UserProfile)->Unit, onFailure: (String) -> Unit ){

        if(emailPhone.contains("@")){
            auth.signInWithEmailAndPassword(emailPhone,password)
                .addOnSuccessListener {

                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        getUserDetails(userId, onSuccess, onFailure)
                    }

//                    onSuccess()
                }
                .addOnFailureListener {
                    error->
                    onFailure(error.message ?: "Login Failed")
                }
        }else{

            storageRef.orderByChild("phone").equalTo(emailPhone).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Get the first user with the matching phone number
                        for (dataSnapshot in snapshot.children) {
                            val user = dataSnapshot.getValue(UserProfile::class.java)
                            user?.let {
                                val email = it.email

                                // Sign in using email and password
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnSuccessListener {
                                        val userId = auth.currentUser?.uid
                                        if (userId != null) {
                                            getUserDetails(userId, onSuccess, onFailure)
                                        }                                    }
                                    .addOnFailureListener { error ->
                                        onFailure(error.message ?: "Login Failed")
                                    }
                            }
                            break  // Exit loop after the first match
                        }
                    } else {
                        onFailure("Phone number not registered")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onFailure("Database error: ${error.message}")
                }
            })

        }
}

    private fun getUserDetails(userId: String, onSuccess: (UserProfile) -> Unit, onFailure: (String) -> Unit) {
        storageRef.child(userId).get()
            .addOnSuccessListener { snapshot ->
                val userProfile = snapshot.getValue(UserProfile::class.java)
                if (userProfile != null) {
                    onSuccess(userProfile)
                } else {
                    onFailure("User details not found")
                }
            }
            .addOnFailureListener { error ->
                onFailure(error.message ?: "Failed to fetch user details")
            }


    }
}