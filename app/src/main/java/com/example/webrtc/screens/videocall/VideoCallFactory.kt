package com.example.webrtc.screens.videocall

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.ktor.http.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

//@RequiresApi(Build.VERSION_CODES.N)
//@ExperimentalCoroutinesApi
//class VideoCallFactory(private val context: Application): ViewModelProvider.Factory {
//    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
//        if(modelClass.isAssignableFrom(VideoCallViewModel::class.java)){
//            return VideoCallViewModel(context) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//
//}
//
