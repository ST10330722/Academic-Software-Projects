package com.example.harvestlinkapp.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.harvestlinkapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "harvestlink_channel"
        private const val TAG = "MyFCM"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d(TAG, "New FCM token: $token")
        TokenUtils.saveCurrentUserToken(token)
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser ?: run {
            Log.d(TAG, "No logged-in user; skipping token save")
            return
        }

        val db = FirebaseFirestore.getInstance()
        val data = mapOf(
            "token" to token,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("users")
            .document(user.uid)
            .collection("tokens")
            .document(token)
            .set(data)
            .addOnSuccessListener {
                Log.d(TAG, "Token saved to Firestore for uid=${user.uid}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save token to Firestore", e)
            }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        android.util.Log.d("MyFCM", "onMessageReceived: $message")

        val title = message.notification?.title ?: "HarvestLink"
        val body = message.notification?.body ?: "New update"

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                "HarvestLink updates",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            nm.createNotificationChannel(ch)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.hl_notification)
            .setAutoCancel(true)
            .build()

        nm.notify(System.currentTimeMillis().toInt(), notification)
    }

}
