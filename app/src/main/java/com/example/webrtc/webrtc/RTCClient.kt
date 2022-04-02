package com.example.webrtc.webrtc

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.webrtc.Call
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.webrtc.*
import java.util.*

@DelicateCoroutinesApi
@RequiresApi(Build.VERSION_CODES.N)
class RTCClient(
        context: Application,
        observer: PeerConnection.Observer
) {

    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_track"

        private const val TAG = "RTCClient"

        private const val CALLS_COLLECTION = "calls"
        private const val CANDIDATES_COLLECTION = "candidates"
        private const val FIELD_TYPE = "type"
        private const val FIELD_ID = "id"
        private const val FIELD_START_TIME = "startTime"
        private const val FIELD_END_TIME = "endTime"

    }

    private val rootEglBase: EglBase = EglBase.create()

    private var localAudioTrack : AudioTrack? = null
    private var localVideoTrack : VideoTrack? = null


    var remoteSessionDescription : SessionDescription? = null

    val db = Firebase.firestore

    init {
        initPeerConnectionFactory(context)
    }

    private val iceServer = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
                    .createIceServer()
    )

    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }
    private val videoCapture by lazy { getVideoCapture(context) }
    private val audioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints())}
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }
    private val peerConnection by lazy { buildPeerConnection(observer) }




    private fun initPeerConnectionFactory(context: Application) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
                .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun buildPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory
                .builder()
                .setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
                .setVideoEncoderFactory(DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true))
                .setOptions(PeerConnectionFactory.Options().apply {
                    disableEncryption = true
                    disableNetworkMonitor = true
                })
                .createPeerConnectionFactory()
    }

    private fun buildPeerConnection(observer: PeerConnection.Observer) = peerConnectionFactory.createPeerConnection(
            iceServer,
            observer
    )

    private fun getVideoCapture(context: Context) =
            Camera2Enumerator(context).run {
                deviceNames.find {
                    isFrontFacing(it)
                }?.let {
                    createCapturer(it, null)
                } ?: throw IllegalStateException()
            }

    fun initSurfaceView(view: SurfaceViewRenderer) = view.run {
        setMirror(true)
        setEnableHardwareScaler(true)
        init(rootEglBase.eglBaseContext, null)
    }

    //todo local camera is hidden after the answer join to call.

    fun startLocalVideoCapture(localVideoOutput: SurfaceViewRenderer) {
        val surfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)
        (videoCapture as VideoCapturer).initialize(surfaceTextureHelper, localVideoOutput.context, localVideoSource.capturerObserver)
        videoCapture.startCapture(320, 240, 60)
        localAudioTrack = peerConnectionFactory.createAudioTrack(LOCAL_TRACK_ID + "_audio", audioSource)
        localVideoTrack = peerConnectionFactory.createVideoTrack(LOCAL_TRACK_ID, localVideoSource)
        localVideoTrack?.addSink(localVideoOutput)
        val localStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID)
        localStream.addTrack(localVideoTrack)
        localStream.addTrack(localAudioTrack)
        peerConnection?.addStream(localStream)
    }

    private suspend fun PeerConnection.call(sdpObserver: SdpObserver, meetingID: String) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }

        createOffer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                setLocalDescription(object : SdpObserver {

                    override fun onSetFailure(p0: String?) {
                        Log.e(TAG, "onSetFailure: $p0")
                    }

                    override fun onSetSuccess() {
                        GlobalScope.launch(Dispatchers.IO) {
                            val call = Call(meetingID, Date(),Date(),desc?.type.toString(),desc?.description.toString())
                            db.collection(CALLS_COLLECTION)
                                .document(meetingID)
                                .set(call).await()
                            Log.e(TAG, "onSetSuccess")
                        }
                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {
                        Log.e(TAG, "onCreateSuccess: Description $p0")
                    }

                    override fun onCreateFailure(p0: String?) {
                        Log.e(TAG, "onCreateFailure: $p0")
                    }
                }, desc)
                sdpObserver.onCreateSuccess(desc)
            }

            override fun onSetFailure(p0: String?) {
                Log.e(TAG, "onSetFailure: $p0")
            }

            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "onCreateFailure: $p0")
            }
        }, constraints)
    }

    private fun PeerConnection.answer(sdpObserver: SdpObserver, meetingID: String) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }

        createAnswer(object : SdpObserver by sdpObserver {

            override fun onCreateSuccess(desc: SessionDescription?) {
                GlobalScope.launch(Dispatchers.IO) {
                    val callRef = db.collection(CALLS_COLLECTION).document(meetingID).get().await()
                    if (callRef != null) {
                        val data = callRef.toObject(Call::class.java)
                        data?.type = desc?.type.toString()
                        data?.sdp = desc?.description.toString()
                        data?.startTime = Date()

                        db.collection(CALLS_COLLECTION).document(data!!.id)
                            .update(data.toHashMap()).await()
                    }
                }


                setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                        Log.e(TAG, "onSetFailure: $p0")
                    }

                    override fun onSetSuccess() {
                        Log.e(TAG, "onSetSuccess")
                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {
                        Log.e(TAG, "onCreateSuccess: Description $p0")
                    }

                    override fun onCreateFailure(p0: String?) {
                        Log.e(TAG, "onCreateFailureLocal: $p0")
                    }
                }, desc)
                sdpObserver.onCreateSuccess(desc)
            }

            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "onCreateFailureRemote: $p0")
            }
        }, constraints)
    }


     suspend fun call(sdpObserver: SdpObserver, meetingID: String) {
        return peerConnection?.call(sdpObserver, meetingID)!!
    }


    fun answer(sdpObserver: SdpObserver, meetingID: String) = peerConnection?.answer(sdpObserver, meetingID)

    fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        remoteSessionDescription = sessionDescription
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetFailure(p0: String?) {
                Log.e(TAG, "onSetFailure: $p0")
            }

            override fun onSetSuccess() {
                Log.e(TAG, "onSetSuccessRemoteSession")
            }

            override fun onCreateSuccess(p0: SessionDescription?) {
                Log.e(TAG, "onCreateSuccessRemoteSession: Description $p0")
            }

            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "onCreateFailure")
            }
        }, sessionDescription)

    }

    fun addIceCandidate(iceCandidate: IceCandidate?) {
        peerConnection?.addIceCandidate(iceCandidate)
    }


    suspend fun endCall(meetingID: String) {
        val candidates = db.collection(CALLS_COLLECTION).document(meetingID).collection(CANDIDATES_COLLECTION).get().await()
        if (candidates != null){
            val iceCandidateArray: MutableList<IceCandidate> = mutableListOf()
            candidates.forEach { dataSnapshot->
                if (dataSnapshot.contains(FIELD_TYPE) && dataSnapshot[FIELD_TYPE]=="offerCandidate") {
                    iceCandidateArray.add(IceCandidate(dataSnapshot["sdpMid"].toString(), Math.toIntExact(dataSnapshot["sdpMLineIndex"] as Long), dataSnapshot["sdp"].toString()))
                } else if (dataSnapshot.contains(FIELD_TYPE) && dataSnapshot["type"]=="answerCandidate") {
                    iceCandidateArray.add(IceCandidate(dataSnapshot["sdpMid"].toString(), Math.toIntExact(dataSnapshot["sdpMLineIndex"] as Long), dataSnapshot["sdp"].toString()))
                }
            }
            peerConnection?.removeIceCandidates(iceCandidateArray.toTypedArray())

            // update type
            val endCall = hashMapOf(FIELD_TYPE to CallState.END_CALL)
            db.collection(CALLS_COLLECTION).document(meetingID).update(endCall.toMap()).await()
            peerConnection?.close()

        }


    }

    suspend fun enableVideo(videoEnabled: Boolean) {
        if (localVideoTrack != null)
            localVideoTrack?.setEnabled(videoEnabled)
    }

    suspend fun enableAudio(audioEnabled: Boolean) {
        if (localAudioTrack != null)
            localAudioTrack?.setEnabled(audioEnabled)
    }

    fun switchCamera() {
        videoCapture.switchCamera(null)
    }
}