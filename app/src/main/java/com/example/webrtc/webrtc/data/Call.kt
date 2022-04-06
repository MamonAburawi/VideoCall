package com.example.webrtc.webrtc.data

import java.util.*


data class Call (
    val callId: String = "",
    var startAt: Date = Date(),
    var endAt: Date = Date(),
    var callStatus: String = "",
    var callType: String = "",
    val cameFrom: String = "",
    val receivedTo: String = "",
    var sdp: String = ""
){
    fun toHashMap(): HashMap<String, Any> {
        return hashMapOf(
            "callId" to callId,
            "startAt" to startAt,
            "endAt" to endAt,
            "callStatus" to callStatus,
            "callType" to callType,
            "cameFrom" to cameFrom,
            "receivedTo" to receivedTo,
            "sdp" to sdp
        )
    }

}
