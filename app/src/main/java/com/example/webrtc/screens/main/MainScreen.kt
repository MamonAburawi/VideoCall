package com.example.webrtc.screens.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.webrtc.R

import com.example.webrtc.databinding.MainScreenBinding
import com.example.webrtc.webrtc.Constants


class MainScreen : Fragment() {

//    private val db = Firebase.firestore
    private lateinit var binding: MainScreenBinding
    private lateinit var viewModel: MainViewModel
    private var meetingId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.main_screen,container,false)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)


        Constants.isInitiatedNow = true
        Constants.isCallEnded = true



        binding.apply {

            // live data metting
            viewModel.meeting.observe(viewLifecycleOwner, Observer { type ->
                if (type != null){
                    progress.visibility = View.GONE
                    btnStartMeeting.isEnabled = true
                    if (type == "OFFER" || type == "ANSWER" || type == "END_CALL"){
                        etMeetingId.error = "Please enter new meeting ID"
                    }else{
                        findNavController().navigate(MainScreenDirections
                                .actionMainScreenToVideoCallScreen(false,meetingId))
                    }
                }
            })



            // button start meeting
            btnStartMeeting.setOnClickListener {
                progress.visibility = View.VISIBLE
                btnStartMeeting.isEnabled = false
                val id = etMeetingId.text.toString().trim()
                meetingId = id
                if (id.isNullOrEmpty())
                    etMeetingId.error = "Please enter meeting id"
                else {
                    viewModel.checkMeetingId(id)

                }
            }


            // button join meeting
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