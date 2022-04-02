package com.example.webrtc.webrtc

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

interface AppSdpObserver : SdpObserver {

    override fun onSetFailure(p0: String?) {}

    override fun onSetSuccess() {}

    override fun onCreateSuccess(p0: SessionDescription?) {}

    override fun onCreateFailure(p0: String?) {}
}