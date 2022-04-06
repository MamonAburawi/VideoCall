package com.example.webrtc.screens.account

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.webrtc.data.User
import com.example.webrtc.utils.SelectUser
import com.example.webrtc.utils.WebRTCAppSessionManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class AccountViewModel(application: Application) : AndroidViewModel(application) {

    companion object{
        private const val TAG = "AccountViewModel"
    }
    private val appSession by lazy { WebRTCAppSessionManager(application) }

    private val root = Firebase.firestore

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user


    init {
     initData()
    }


    fun initData(){
        _user.value = appSession.getUserFromSession()
    }



    fun updateUser(select: String){
        viewModelScope.launch {
            try {
                val ref = root.collection("users").get().await()
                if (ref != null){
                    val users = ref.toObjects(User::class.java)
                    val user = users.filter { it.name == select }[0]
                    _user.value = user
                    appSession.updateUser(user)
                    Log.i(TAG,"${user.name} data has been saved")
                }
            }catch (ex: Exception){
                Log.e(TAG,ex.message.toString())
            }

        }
    }
}