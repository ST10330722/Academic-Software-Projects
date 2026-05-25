package com.example.harvestlinkapp.ui.theme.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.harvestlinkapp.R
import com.example.harvestlinkapp.data.FirebaseRepository
import com.example.harvestlinkapp.databinding.ActivityLoginBinding
import com.example.harvestlinkapp.ui.theme.role.RoleSelectActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.example.harvestlinkapp.push.TokenUtils
import com.example.harvestlinkapp.util.Prefs
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val repo = FirebaseRepository()

    companion object {
        private const val RC_SIGN_IN = 1001
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
           val dark = Prefs.isDarkModeEnabled(this)
           androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                   if (dark) AppCompatDelegate.MODE_NIGHT_YES
                           else AppCompatDelegate.MODE_NIGHT_NO
                       )
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnGoogleSignIn.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val intent = googleSignInClient.signInIntent
        startActivityForResult(intent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Sign-in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    lifecycleScope.launch {
                        try {
                            repo.upsertUser(role = "buyer")


                            FirebaseMessaging.getInstance().token
                                .addOnSuccessListener { token ->
                                    Log.d(TAG, "Got FCM token after login: $token")
                                    TokenUtils.saveCurrentUserToken(token)
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Failed to get FCM token", e)
                                }


                            startActivity(
                                Intent(
                                    this@LoginActivity,
                                    RoleSelectActivity::class.java
                                )
                            )
                            finish()
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@LoginActivity,
                                "Error saving user: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Auth failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
