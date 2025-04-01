package com.example.qualwebs_chatapp_assignment.models

import java.io.Serializable

data class UserProfile(

    val uid : String="",
    val name : String="",
    val email : String="",
    val phone : String="",
//    val password : String="",
    val profilePic : String="",

):Serializable
