package com.example.webrtc.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.webrtc.data.User
import java.util.*
import kotlin.collections.HashMap

class WebRTCAppSessionManager(context: Context) {

    private var userSession: SharedPreferences =
        context.getSharedPreferences("userSessionData", Context.MODE_PRIVATE)

    var editor: SharedPreferences.Editor = userSession.edit()


    fun updateUser(user: User) {
        editor.putString(KEY_ID, user.userId)
        editor.putString(KEY_NAME, user.name)
        editor.putString(KEY_EMAIL,user.email)
        editor.putString(KEY_PASSWORD,user.password)
        editor.putBoolean(KEY_ON_CALL,user.onCall)
        editor.putInt(KEY_PHONE, user.phone)
        editor.commit()
    }





    fun getUserFromSession():User{
        val userId = userSession.getString(KEY_ID, "")
        val name = userSession.getString(KEY_NAME, "")
        val phone = userSession.getInt(KEY_PHONE, 0)
        val email = userSession.getString(KEY_EMAIL,"")
        val password = userSession.getString(KEY_PASSWORD,"")
        val onCall = userSession.getBoolean(KEY_ON_CALL,false)
        return User(userId!!, name!!, email!!, password!!, Date(),onCall, phone)
    }




    fun logoutFromSession() {
        editor.clear()
        editor.commit()
    }

    companion object {
        private const val IS_LOGIN = "isLoggedIn"
        private const val KEY_NAME = "userName"
        private const val KEY_PHONE = "userPhone"
        private const val KEY_ID = "userId"
        private const val KEY_REMEMBER_ME = "isRemOn"
        private const val KEY_ON_CALL = "onCall"
        private const val KEY_PASSWORD = "password"
        private const val KEY_SELECT = "select"
        private const val KEY_EMAIL = "email"
    }
}