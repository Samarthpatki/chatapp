package com.example.qualwebs_chatapp_assignment.data

import com.google.firebase.database.FirebaseDatabase

class ProfileScreenRepository {

    private  val storage = FirebaseDatabase.getInstance()
    private val storageRef= storage.getReference("profiles")




}