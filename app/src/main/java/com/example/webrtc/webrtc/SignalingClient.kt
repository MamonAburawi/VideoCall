package com.example.webrtc.webrtc

import android.util.Log
import com.example.webrtc.Call
import com.example.webrtc.Candidates
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.*
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

@DelicateCoroutinesApi
@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class SignalingClient(
    private val meetingID : String,
    private val listener: SignalingClientListener
) : CoroutineScope {

    companion object {
        private const val HOST_ADDRESS = "192.168.0.12"

        private const  val TAG = "SignallingClient"

        private const val CALLS_COLLECTION = "calls"
        private const val CANDIDATES_COLLECTION = "candidates"
        private const val FIELD_TYPE = "type"
        private const val FIELD_ID = "id"
        private const val FIELD_START_TIME = "startTime"
        private const val FIELD_END_TIME = "endTime"
    }

    var jsonObject : JSONObject?= null

    private val job = Job()

    private val root = Firebase.firestore


    private var sdpType : String? = null
    override val coroutineContext = Dispatchers.IO + job

    private val client = HttpClient(CIO) {
        install(WebSockets)
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }


    init {
        connect()
    }

    private fun connect() = launch {
        root.enableNetwork().addOnSuccessListener {
            listener.onConnectionEstablished()
        }

        try {
            GlobalScope.launch(Dispatchers.IO) {
                root.collection(CALLS_COLLECTION)
                    .document(meetingID)
                    .addSnapshotListener { snapshot, e ->
                        if(e == null && snapshot!!.exists()){
                            val candidate = snapshot.toObject(Call::class.java)
                            when (candidate?.type) {
                                CallState.OFFER.name -> { // offer
                                    listener.onOfferReceived(SessionDescription(SessionDescription.Type.OFFER,candidate.sdp))
                                    sdpType = CallState.OFFER.name
                                }
                                CallState.ANSWER.name -> { // answer
                                    listener.onAnswerReceived(SessionDescription(SessionDescription.Type.ANSWER,candidate.sdp))
                                    sdpType = CallState.ANSWER.name
                                }
                                CallState.CALL_ENDED.name -> {
                                    listener.onCallEnded()
                                    sdpType = CallState.CALL_ENDED.name
                                }
                            }
                        }
                    }
            }

            GlobalScope.launch(Dispatchers.IO) {
                root.collection(CALLS_COLLECTION)
                    .document(meetingID)
                    .collection(CANDIDATES_COLLECTION).addSnapshotListener{ snapshot, e->
                        if (e == null){
                            if (!snapshot!!.isEmpty){
                                snapshot.forEach {
                                    val candidate = it.toObject(Candidates::class.java)
                                    if (sdpType == CallState.OFFER.name  && candidate.type == CandidateType.offerCandidate.name) {
                                        listener.onIceCandidateReceived(
                                            IceCandidate(candidate.sdpMid, candidate.sdpMLineIndex, candidate.sdpCandidate))
                                    } else if (sdpType == CallState.ANSWER.name  && candidate.type == CandidateType.answerCandidate.name) {
                                        listener.onIceCandidateReceived(
                                            IceCandidate(candidate.sdpMid, candidate.sdpMLineIndex, candidate.sdpCandidate))
                                    }
                                }
                            }
                        }
                    }
            }



        } catch (exception: Exception) {
            Log.e(TAG, "connectException: $exception")

        }
    }

    fun sendIceCandidate(candidate: IceCandidate?,isJoin : Boolean) = runBlocking {
        val type = when {
            isJoin -> CandidateType.answerCandidate.name
            else -> CandidateType.offerCandidate.name
        }
        if (candidate != null){
            val candi = Candidates(candidate.sdp,candidate.sdpMLineIndex,candidate.sdpMid,candidate.serverUrl,type)
            root.collection(CALLS_COLLECTION)
                .document(meetingID)
                .collection(CANDIDATES_COLLECTION)
                .document(type)
                .set(candi)
        }
    }

    fun destroy() {
        client.close()
        job.complete()
    }
}