package com.example.webrtc.screens.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class HomeViewModel(): ViewModel() {


    private val db = Firebase.firestore
    private val jobIO = CoroutineScope(Dispatchers.IO + Job())

    private val _meeting = MutableLiveData<String>()
    val meeting: LiveData<String> = _meeting



    fun checkMeetingId(meetingId: String){
        jobIO.launch {
            db.collection("calls")
                    .document(meetingId)
                    .get()
                    .addOnSuccessListener {
                        if (it != null){
                            _meeting.value = it["type"].toString()
                        }
                    }
        }
    }


    override fun onCleared() {
        super.onCleared()

        jobIO.cancel()
    }
}