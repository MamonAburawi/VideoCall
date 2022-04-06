package com.example.webrtc.webrtc

enum class CandidateType{ offerCandidate, answerCandidate}

enum class CallState{ END_CALL, ANSWER, OFFER , CALLING , NO_ANSWER , DECLINE}


class Constants {
    companion object {
        var isCallEnded: Boolean = false
        var isInitiatedNow : Boolean = true
    }
}


