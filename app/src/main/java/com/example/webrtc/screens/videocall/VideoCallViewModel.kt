package com.example.webrtc.screens.videocall

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.webrtc.R
import com.example.webrtc.databinding.VideoCallScreenBinding
import com.example.webrtc.webrtc.*
import io.ktor.util.*
import kotlinx.coroutines.*
import org.webrtc.*

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
@RequiresApi(Build.VERSION_CODES.N)
class VideoCallViewModel(application: Application): AndroidViewModel(application) {


    companion object{
        const val TAG = "VideoCallViewModel"
    }

    private val jobMain = CoroutineScope(Dispatchers.Main + Job())
    private val jobIO = CoroutineScope(Dispatchers.IO + Job())


    private  var rtcClient: RTCClient? = null
    private val audioManager: RTCAudioManager by lazy{
       RTCAudioManager.create(application)
    }



//    private var audioManager: RTCAudioManager = RTCAudioManager.create(application)


    private val _inSpeakerMode = MutableLiveData<Boolean>()
    val inSpeakerMode: LiveData<Boolean> = _inSpeakerMode

    private val _isUnMute = MutableLiveData<Boolean>()
    val isUnMute: LiveData<Boolean> = _isUnMute

    private val _isVideoResumed = MutableLiveData<Boolean>()
    val isVideoResumed: LiveData<Boolean> = _isVideoResumed


    private val sdpObserver = object : AppSdpObserver() {
        override fun onCreateSuccess(p0: SessionDescription?) {
            super.onCreateSuccess(p0)
//            signallingClient.send(p0)
        }
    }




    init {
        _inSpeakerMode.value = true
        _isUnMute.value = true
        _isVideoResumed.value = true
        audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
    }


    fun isSpeakerEnabled(){
        jobMain.launch {
            if (_inSpeakerMode.value == false) {
                _inSpeakerMode.value = true
                audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE)
            } else {
                _inSpeakerMode.value = false
                audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
            }
        }
    }

    fun isMicrophoneEnabled(){
        jobMain.launch {
            _isUnMute.value = _isUnMute.value != true
            rtcClient!!.enableAudio(_isUnMute.value!!)
        }
    }


    fun isVideoResumed(){
        jobMain.launch {
            _isVideoResumed.value = isVideoResumed.value != true
            rtcClient!!.enableVideo(isVideoResumed.value!!)
        }
    }

    fun switchCamera(){
        jobMain.launch {
            rtcClient!!.switchCamera()
        }
    }


    fun onRemoteSessionReceived(description: SessionDescription){
        rtcClient!!.onRemoteSessionReceived(description)
    }

    fun answer(meetingID: String){
        rtcClient!!.answer(sdpObserver,meetingID)
    }

    fun endCall(meetingID: String){
        jobIO.launch {
            rtcClient!!.endCall(meetingID)
            rtcClient!!.enableAudio(false)
            rtcClient!!.enableVideo(false)
//            audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.NONE)
        }
    }

    fun onIceCandidateReceived(iceCandidate: IceCandidate){
        rtcClient!!.addIceCandidate(iceCandidate)
    }


    fun onCameraAndAudioPermissionGranted(binding: VideoCallScreenBinding, isJoin: Boolean , signallingClient: SignalingClient,meetingID: String) {
        rtcClient = RTCClient(
                getApplication(),
                object : PeerConnectionObserver(){
                    override fun onIceCandidate(p0: IceCandidate?) {
                        super.onIceCandidate(p0)
                        jobIO.launch {
                            signallingClient.sendIceCandidate(p0, isJoin)
                        }
                        jobIO.launch {
                            rtcClient!!.addIceCandidate(p0)
                        }
                    }

                    override fun onAddStream(p0: MediaStream?) {
                        super.onAddStream(p0)
                        Log.e(TAG, "onAddStream: $p0")
                        jobMain.launch {
                            p0?.videoTracks?.get(0)?.addSink(binding.remoteView)
                        }

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
        if (!isJoin)
            jobIO.launch {
                rtcClient!!.call(sdpObserver,meetingID)
            }
    }


    override fun onCleared() {
        super.onCleared()

        jobMain.cancel()
        jobIO.cancel()
    }


}