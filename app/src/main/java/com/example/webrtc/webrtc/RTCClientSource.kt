package com.example.webrtc.webrtc

import org.webrtc.IceCandidate
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

interface RTCClientSource {

     suspend fun answer(sdpObserver: AppSdpObserver,meetingID: String)

     suspend fun call(sdpObserver: SdpObserver, meetingID: String)

     suspend fun endCall(meetingID: String)

     suspend fun enableAudio(b: Boolean)

     suspend fun enableVideo(b: Boolean)

     fun switchCamera()

     fun onRemoteSessionReceived(description: SessionDescription)

     fun addIceCandidate(iceCandidate: IceCandidate?)

}