package com.example.webrtc.webrtc

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.example.webrtc.R



@BindingAdapter("setMic")
fun setMic(imageView: ImageView, isMuted: Boolean){
    if(isMuted){
        imageView.setImageResource(R.drawable.ic_microphone_on)
    }else{
        imageView.setImageResource(R.drawable.ic_microphone_off)
    }
}


@BindingAdapter("setVideo")
fun setVideo(imageView: ImageView, isVideoResumed: Boolean){
    if(isVideoResumed){
        imageView.setImageResource(R.drawable.ic_videocamera_on)
    }else{
        imageView.setImageResource(R.drawable.ic_videocamera_off)
    }
}


@BindingAdapter("setSpeaker")
fun setSpeaker(imageView: ImageView, isSpeaker: Boolean){
    if(isSpeaker){
        imageView.setImageResource(R.drawable.ic_speaker)
    }else{
        imageView.setImageResource(R.drawable.ic_baseline_hearing)
    }
}