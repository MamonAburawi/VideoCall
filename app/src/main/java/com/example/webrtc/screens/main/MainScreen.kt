package com.example.webrtc.screens.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.example.webrtc.R

import com.example.webrtc.databinding.MainScreenBinding
import com.example.webrtc.webrtc.Constants
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MainScreen : Fragment() {

    private val db = Firebase.firestore
    private lateinit var binding: MainScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.main_screen,container,false)

        Constants.isIntiatedNow = true
        Constants.isCallEnded = true

        binding.apply {
            btnStartMeeting.setOnClickListener {
                val meetingId = etMeetingId.text.toString().trim()
                if (meetingId.isNullOrEmpty())
                    etMeetingId.error = "Please enter meeting id"
                else {
                    db.collection("calls")
                            .document(meetingId)
                            .get()
                            .addOnSuccessListener {
                                if (it["type"]=="OFFER" || it["type"]=="ANSWER" || it["type"]=="END_CALL") {
                                    etMeetingId.error = "Please enter new meeting ID"
                                } else {
                                    findNavController().navigate(MainScreenDirections
                                            .actionMainScreenToVideoCallScreen(false,meetingId))

                                }
                            }
                            .addOnFailureListener {
                                etMeetingId.error = "Please enter new meeting ID"
                            }
                }
            }
            btnJoinMeeting.setOnClickListener {
                val meetingId = etMeetingId.text.toString().trim()
                if (meetingId.isNullOrEmpty())
                    etMeetingId.error = "Please enter meeting id"
                else {
                    findNavController().navigate(MainScreenDirections
                            .actionMainScreenToVideoCallScreen(true,meetingId))
                }
            }


        }

        return binding.root
    }

}