package com.example.webrtc

import java.util.HashMap

data class Candidates(
    val sdpCandidate: String = "",
    val sdpMLineIndex: Int = 0,
    val sdpMid: String = "",
    val serverUrl: String = "",
    val type: String = ""
){
    fun toHashMap(): HashMap<String, Any> {
        return hashMapOf(
            "sdpCandidates" to sdpCandidate,
            "sdpMLineIndex" to sdpMLineIndex,
            "sdpMid" to sdpMid,
            "serverUrl" to serverUrl,
            "type" to type
        )
    }
}
