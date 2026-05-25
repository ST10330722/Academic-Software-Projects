
package com.example.harvestlinkapp.push

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object TokenUtils {

    private const val TAG = "TokenUtils"

    fun saveCurrentUserToken(token: String) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser ?: run {
            Log.d(TAG, "No logged-in user; cannot save token")
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
                Log.d(TAG, "Token saved for uid=${user.uid}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save token", e)
            }
    }
}
