package com.example.webrtc

import java.util.*


data class Call (
    val id: String = "",
    var startTime: Date = Date(),
    var endTime: Date = Date(),
    var type: String = "",
    var sdp: String = ""
){
    fun toHashMap(): HashMap<String, Any> {
        return hashMapOf(
            "id" to id,
            "startTime" to startTime,
            "endTime" to endTime,
            "type" to type,
            "sdp" to sdp
        )
    }

}
