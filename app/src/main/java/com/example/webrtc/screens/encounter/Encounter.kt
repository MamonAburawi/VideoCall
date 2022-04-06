package com.example.webrtc.screens.encounter

import android.Manifest

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.webrtc.R
import com.example.webrtc.databinding.EncounterBinding
import com.example.webrtc.webrtc.*
import com.example.webrtc.webrtc.data.Call
import io.ktor.util.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.webrtc.*
import java.util.*


@DelicateCoroutinesApi
@KtorExperimentalAPI
@ExperimentalCoroutinesApi
@RequiresApi(Build.VERSION_CODES.N)
class Encounter : Fragment() {

    companion object {
        private const val CAMERA_AUDIO_PERMISSION_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO

        private const val TAG = "VideoCall"
    }

    private lateinit var signallingClient: SignalingClient


    private var meetingID : String = ""
    private var isJoin = false

//    private lateinit var data : Call



    private lateinit var binding: EncounterBinding
//    private lateinit var viewModel: EncounterViewModel
    private val viewModel by activityViewModels<EncounterViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.encounter,container,false)


        isJoin = arguments?.get("isJoin") as Boolean
        meetingID = arguments?.get("meetingId") as String

//        val callId = arguments?.get("callId") as String
//        val cameFrom = arguments?.get("cameFrom") as String
//        val receivedTo = arguments?.get("receivedTo") as String
//        val call = Call(callId, Date(), Date(),CallState.CALLING.name,"",cameFrom,receivedTo,"")
//





        setViews()

//        viewModel = ViewModelProviders.of(this).get(EncounterViewModel::class.java)

        checkCameraAndAudioPermission()



        return binding.root


    }

    private fun setViews() {
        binding.apply {

            encounterViewModel = viewModel
            lifecycleOwner = this@Encounter

            btnSwitchCamera.setOnClickListener {
                viewModel.switchCamera()
            }

            btnAudioOutput.setOnClickListener {
                viewModel.isSpeakerEnabled()
            }

            btnVideo.setOnClickListener {
                viewModel.isVideoResumed()
            }

            btnMic.setOnClickListener {
                viewModel.isMicrophoneEnabled()
            }

            btnEndCall.setOnClickListener {
                binding.remoteView.isGone = false
                Constants.isCallEnded = true
                findNavController().navigate(R.id.action_encounter_to_home)
                viewModel.endCall(meetingID)

            }

        }
    }


    private fun checkCameraAndAudioPermission() {
        if ((ContextCompat.checkSelfPermission(requireActivity(), CAMERA_PERMISSION) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(requireActivity(),AUDIO_PERMISSION) != PackageManager.PERMISSION_GRANTED)) {
            requestCameraAndAudioPermission()
        } else {
            onCameraAndAudioPermissionGranted()
        }
    }

    private fun onCameraAndAudioPermissionGranted(){
        signallingClient =  SignalingClient(meetingID,createSignallingClientListener())
        viewModel.initConnection(binding,signallingClient,isJoin,meetingID)
    }


    private fun createSignallingClientListener() = object : SignalingClientListener {

        override fun onConnectionEstablished() {

            Log.i(TAG,"onConnection Stable")
            binding.btnEndCall.isClickable = true
        }


        override fun onOfferReceived(description: SessionDescription) {
            viewModel.onRemoteSessionReceived(description)
            Constants.isInitiatedNow = false
            viewModel.answer(meetingID)
//            binding.remoteViewLoading.isGone = true
            Log.i(TAG,"onOffer Received")
        }


        // when the answer (user is other side) is join to connection.
        override fun onAnswerReceived(description: SessionDescription) {
            viewModel.onRemoteSessionReceived(description)
            Constants.isInitiatedNow = false
//            binding.remoteViewLoading.isGone = true
            Log.i(TAG,"onAnswer Received")
        }

        override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
            viewModel.onIceCandidateReceived(iceCandidate)
        }

        override fun onErrorConnection(error: String) {

            // TODO: catch any error connection during calling
        }


        override fun onCallEnded() {


           try {
               signallingClient.destroy() /// remove this line if there is any error
               findNavController().navigate(R.id.action_encounter_to_home)
           }catch (ex: Exception){
               Log.e(TAG,ex.message.toString())
           }




//                viewModel.endCall(meetingID)
//                Toast.makeText(requireContext(),"Call ended",Toast.LENGTH_SHORT).show()
//                signallingClient.destroy() /// remove this line if there is any error
//               findNavController().navigate(R.id.action_encounter_to_main)



        }
    }


    private fun requestCameraAndAudioPermission(dialogShown: Boolean = false) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), CAMERA_PERMISSION) &&
                ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), AUDIO_PERMISSION) &&
                !dialogShown) {
            showPermissionRationaleDialog()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(CAMERA_PERMISSION, AUDIO_PERMISSION), CAMERA_AUDIO_PERMISSION_REQUEST_CODE)
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireActivity())
                .setTitle("Camera And Audio Permission Required")
                .setMessage("This app need the camera and audio to function")
                .setPositiveButton("Grant") { dialog, _ ->
                    dialog.dismiss()
                    requestCameraAndAudioPermission(true)
                }
                .setNegativeButton("Deny") { dialog, _ ->
                    dialog.dismiss()
                    onCameraPermissionDenied()
                }
                .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_AUDIO_PERMISSION_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            onCameraAndAudioPermissionGranted()
        } else {
            onCameraPermissionDenied()
        }
    }

    private fun onCameraPermissionDenied() {
        Toast.makeText(requireActivity(), "Camera and Audio Permission Denied", Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.initAudios()
    }

    override fun onDestroy() {
        signallingClient.destroy()
        super.onDestroy()
    }
}