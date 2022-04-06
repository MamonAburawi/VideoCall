package com.example.webrtc.webrtc

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.webrtc.databinding.EncounterBinding
import com.example.webrtc.webrtc.data.Call
import io.ktor.util.*
import kotlinx.coroutines.*
import org.webrtc.*

@RequiresApi(Build.VERSION_CODES.N)
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
class RTCClientManager(
    application: Application,
    private val binding: EncounterBinding,
    val signalingClient: SignalingClient,
    sdpObserver: AppSdpObserver,
    isJoin: Boolean,
    val meetingID: String
): RTCClientSource {

    companion object {
        const val TAG = "RTCClientManager"
    }

    private var rtcClient: RTCClient? = null


    override suspend  fun answer(sdpObserver: AppSdpObserver, meetingID: String) {
        rtcClient!!.answer(sdpObserver, meetingID)
    }

    override suspend fun call(sdpObserver: SdpObserver, meetingID: String) {
        rtcClient!!.call(sdpObserver, meetingID)
    }

    override suspend fun endCall(meetingID: String) {
        rtcClient!!.endCall(meetingID)
    }


    override suspend fun enableAudio(b: Boolean) {
        rtcClient!!.enableAudio(b)
    }

    override suspend fun enableVideo(b: Boolean) {
        rtcClient!!.enableVideo(b)
    }

    override  fun switchCamera() {
        rtcClient!!.switchCamera()
    }

    override  fun onRemoteSessionReceived(description: SessionDescription) {
        rtcClient!!.onRemoteSessionReceived(description)
    }


    override  fun addIceCandidate(iceCandidate: IceCandidate?) {
        rtcClient!!.addIceCandidate(iceCandidate)
    }



    init {

        rtcClient = RTCClient(application,
            object : PeerConnectionObserver{

                override  fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    signalingClient.sendIceCandidate(p0, isJoin)
                    addIceCandidate(p0)
                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    Log.e(TAG, "onAddStream: $p0")
                    p0?.videoTracks?.get(0)?.addSink(binding.remoteView)
                }

                override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                    Log.e(TAG, "onIceConnectionChange: $p0")
                }

                override fun onIceConnectionReceivingChange(p0: Boolean) {
                    Log.e(TAG, "onIceConnectionReceivingChange: $p0")
                }

                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                    Log.e(TAG, "onConnectionChange: $newState")

                }

                override fun onDataChannel(p0: DataChannel?) {
                    Log.e(TAG, "onDataChannel: $p0")
                }

                override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                    Log.e(TAG, "onStandardizedIceConnectionChange: $newState")
                }

                override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
                    Log.e(TAG, "onAddTrack: $p0 \n $p1")
                }

                override fun onTrack(transceiver: RtpTransceiver?) {
                    Log.e(TAG, "onTrack: $transceiver" )
                }
            })

        rtcClient!!.initSurfaceView(binding.remoteView)
        rtcClient!!.initSurfaceView(binding.localView)
        rtcClient!!.startLocalVideoCapture(binding.localView)

        if (!isJoin){
            GlobalScope.launch(Dispatchers.IO) {
                rtcClient!!.call(sdpObserver,meetingID)
            }
        }


    }


}