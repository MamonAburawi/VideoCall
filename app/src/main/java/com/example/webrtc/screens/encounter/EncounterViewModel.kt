package com.example.webrtc.screens.encounter

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.webrtc.databinding.EncounterBinding
import com.example.webrtc.webrtc.*
import io.ktor.util.*
import kotlinx.coroutines.*
import org.webrtc.*

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
@DelicateCoroutinesApi
@RequiresApi(Build.VERSION_CODES.N)
class EncounterViewModel(application: Application): AndroidViewModel(application) {

    val app = application

    companion object{
        const val TAG = "VideoCallViewModel"
    }

    private val jobMain = CoroutineScope(Dispatchers.Main + Job())
    private val jobIO = CoroutineScope(Dispatchers.IO + Job())

    private val audioManager: RTCAudioManager by lazy{
       RTCAudioManager.create(application)
    }



    private val _inSpeakerMode = MutableLiveData<Boolean>()
    val inSpeakerMode: LiveData<Boolean> = _inSpeakerMode

    private val _isUnMute = MutableLiveData<Boolean>()
    val isUnMute: LiveData<Boolean> = _isUnMute

    private val _isVideoEnable = MutableLiveData<Boolean>()
    val isVideoEnable: LiveData<Boolean> = _isVideoEnable

    private val _navigateToMain = MutableLiveData<Boolean?>()
    val navigateToMain: LiveData<Boolean?> = _navigateToMain


    private lateinit var rtcClientManager : RTCClientManager




    private val sdpObserver = object : AppSdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) {
            super.onCreateSuccess(p0)
        }
    }



    init {
        initAudios()
        audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
    }

    fun initConnection(
        binding: EncounterBinding,
        signallingClient: SignalingClient,
        isJoin: Boolean,
        meetingID: String
    ){
        rtcClientManager = RTCClientManager(app,binding,signallingClient,sdpObserver,isJoin,meetingID)
    }

    fun initAudios(){
        _inSpeakerMode.value = true
        _isUnMute.value = true
        _isVideoEnable.value = true
    }

    fun navigateToMain(){
        _navigateToMain.value = true
    }

    fun navigateToMainDone(){
        _navigateToMain.value = false
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
        viewModelScope.launch {
            _isUnMute.value = _isUnMute.value != true
            rtcClientManager.enableAudio(_isUnMute.value!!)
        }
    }


    fun isVideoResumed(){
        viewModelScope.launch {
            _isVideoEnable.value = isVideoEnable.value != true
            rtcClientManager.enableVideo(isVideoEnable.value!!)
        }
    }

    fun switchCamera(){
        viewModelScope.launch {
            rtcClientManager.switchCamera()
        }
    }


    fun onRemoteSessionReceived(description: SessionDescription){
        rtcClientManager.onRemoteSessionReceived(description)
    }

    fun answer(meetingID: String){
        viewModelScope.launch {
            rtcClientManager.answer(sdpObserver,meetingID)
        }
    }

    fun call(meetingID: String){
        viewModelScope.launch {
            rtcClientManager.call(sdpObserver, meetingID)
        }
    }

    fun endCall(meetingID: String){
        jobMain.launch {

            rtcClientManager.endCall(meetingID)
            rtcClientManager.enableAudio(false)
            rtcClientManager.enableVideo(false)


            audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.NONE)
        }


    }

    fun onIceCandidateReceived(iceCandidate: IceCandidate){
        viewModelScope.launch {
            rtcClientManager.addIceCandidate(iceCandidate)
        }
    }




    override fun onCleared() {
        super.onCleared()

        jobMain.cancel()
        jobIO.cancel()
    }


}