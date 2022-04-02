package com.example.webrtc.webrtc

enum class CandidateType{ offerCandidate, answerCandidate}

enum class CallState{ CALL_ENDED, ANSWER, OFFER , CALLING }


class Constants {
    companion object {
        var isCallEnded: Boolean = false
        var isInitiatedNow : Boolean = true
    }
}


