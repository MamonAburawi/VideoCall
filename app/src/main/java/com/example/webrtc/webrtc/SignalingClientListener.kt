package com.example.webrtc.webrtc

import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface SignalingClientListener {

    fun onConnectionEstablished()

    fun onOfferReceived(description: SessionDescription)

    fun onAnswerReceived(description: SessionDescription)

    fun onIceCandidateReceived(iceCandidate: IceCandidate)

    fun onErrorConnection(error: String)

    fun onCallEnded()
}