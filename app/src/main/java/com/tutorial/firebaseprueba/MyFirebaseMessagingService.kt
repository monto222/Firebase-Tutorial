package com.tutorial.firebaseprueba

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {


    override fun onMessageReceived(message: RemoteMessage) {
        Toast.makeText(applicationContext, message.notification?.title, Toast.LENGTH_LONG).show()
    }
}