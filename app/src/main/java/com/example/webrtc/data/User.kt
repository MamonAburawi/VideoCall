package com.example.webrtc.data

import java.util.*

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val date: Date = Date(),
    val onCall: Boolean = false,
    val phone: Int = 0,

) {
    fun toHashMap(): HashMap<String, Any> {
        return hashMapOf(
            "userId" to userId,
            "name" to name,
            "email" to email,
            "password" to password,
            "date" to date,
            "onCall" to onCall,
            "phone" to phone
        )
    }

}

