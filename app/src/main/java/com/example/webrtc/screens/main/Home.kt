package com.example.webrtc.screens.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.webrtc.R
import com.example.webrtc.databinding.HomeBinding
import com.example.webrtc.screens.account.AccountViewModel
import com.example.webrtc.utils.ahmedId
import com.example.webrtc.utils.mohamedId


import com.example.webrtc.webrtc.Constants
import java.util.*


class Home : Fragment() {


    private lateinit var binding: HomeBinding
    private lateinit var viewModel: HomeViewModel
    private val accountViewModel by activityViewModels<AccountViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.home,container,false)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]


        Constants.isInitiatedNow = true
        Constants.isCallEnded = true

        accountViewModel.initData()


        binding.apply {



            // button start meeting
            btnStartMeeting.setOnClickListener {

                val id = etMeetingId.text.toString().trim()

                if (id.isEmpty())
                    etMeetingId.error = "Please enter meeting id"
                else {
                    val data = bundleOf("isJoin" to false , "meetingId" to id)
                    findNavController().navigate(R.id.action_home_to_encouter,data)

                }
            }


            // button join meeting
            btnJoinMeeting.setOnClickListener {
                val meetingId = etMeetingId.text.toString().trim()
                if (meetingId.isEmpty())
                    etMeetingId.error = "Please enter meeting id"
                else {

                    val data = bundleOf("isJoin" to true , "meetingId" to meetingId)
//                    findNavController().navigate(R.id.action_ma,data)
                    findNavController().navigate(R.id.action_home_to_encouter,data)

                }
            }




        }

        return binding.root
    }


    private fun getUserId(name: String): String{
        return if (name != "Ahmed"){
            ahmedId
        }else{
            mohamedId
        }
    }

    private fun navigateToEncounter(callId: String,isJoin: Boolean){

        val userData = accountViewModel.user.value

//        Toast.makeText(context, getReceivedTo(userData!!.name),Toast.LENGTH_SHORT).show()

        if (userData?.name!!.isNotEmpty()){
            val data = bundleOf(
                "callId" to callId,
                "isJoin" to isJoin,
                "cameFrom" to userData.userId,
                "receivedTo" to getUserId(userData.name)
            )
            findNavController().navigate(R.id.action_home_to_encouter,data)
        }else{
            Toast.makeText(context,"please select one of the users..",Toast.LENGTH_SHORT).show()
        }
    }



    private fun getReceivedTo(name: String): String{
        return if (name == "Ahmed"){
            "Mohamed"
        }else{
            "Ahmed"
        }

    }





//package com.example.webrtc.screens.main
//
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.core.os.bundleOf
//import androidx.databinding.DataBindingUtil
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelProviders
//import androidx.navigation.fragment.findNavController
//import com.example.webrtc.R
//
//import com.example.webrtc.databinding.MainScreenBinding
//import com.example.webrtc.webrtc.Constants
//
//
//class Main : Fragment() {
//
//    //    private val db = Firebase.firestore
//    private lateinit var binding: MainScreenBinding
//    private lateinit var viewModel: MainViewModel
//    private var meetingId = ""
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//
//        binding = DataBindingUtil.inflate(inflater, R.layout.main_screen,container,false)
//        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
//
//
//        Constants.isInitiatedNow = true
//        Constants.isCallEnded = true
//
//
//
//        binding.apply {
//
//            // live data metting
//            viewModel.meeting.observe(viewLifecycleOwner, Observer { type ->
//                if (type != null){
//                    progress.visibility = View.GONE
//                    btnStartMeeting.isEnabled = true
//                    if (type == "OFFER" || type == "ANSWER" || type == "END_CALL"){
//                        etMeetingId.error = "Please enter new meeting ID"
//                    }else{
//                        val data = bundleOf("isJoin" to false , "meetingId" to meetingId)
//                        findNavController().navigate(R.id.action_mainScreen_to_videoCallScreen,data)
//                    }
//                }
//            })
//
//
//
//            // button start meeting
//            btnStartMeeting.setOnClickListener {
//                progress.visibility = View.VISIBLE
//                btnStartMeeting.isEnabled = false
//                val id = etMeetingId.text.toString().trim()
//                meetingId = id
//                if (id.isNullOrEmpty())
//                    etMeetingId.error = "Please enter meeting id"
//                else {
//                    viewModel.checkMeetingId(id)
//
//                }
//            }
//
//
//            // button join meeting
//            btnJoinMeeting.setOnClickListener {
//                val meetingId = etMeetingId.text.toString().trim()
//                if (meetingId.isNullOrEmpty())
//                    etMeetingId.error = "Please enter meeting id"
//                else {
//                    val data = bundleOf("isJoin" to true , "meetingId" to meetingId)
//                    findNavController().navigate(R.id.action_mainScreen_to_videoCallScreen,data)
////                    findNavController().navigate(MainScreenDirections
////                            .actionMainScreenToVideoCallScreen(true,meetingId))
//                }
//            }
//
//
//
//
//        }
//
//        return binding.root
//    }
//
//}
}