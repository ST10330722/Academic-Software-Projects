package com.example.harvestlinkapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.harvestlinkapp.ui.theme.auth.LoginActivity
import com.example.harvestlinkapp.ui.theme.buyer.BuyerHomeActivity
import com.example.harvestlinkapp.ui.theme.farmer.FarmerHomeActivity
import com.example.harvestlinkapp.ui.theme.role.RoleSelectActivity
import com.example.harvestlinkapp.util.Prefs
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
           val dark = Prefs.isDarkModeEnabled(this)
           AppCompatDelegate.setDefaultNightMode(
                   if (dark) AppCompatDelegate.MODE_NIGHT_YES
                           else AppCompatDelegate.MODE_NIGHT_NO
                       )
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser


        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }


        if (Prefs.isBiometricsEnabled(this) && isBiometricAvailable()) {
            showBiometricPrompt()
        } else {
            routeToHome()
        }
    }

    private fun isBiometricAvailable(): Boolean {
        val bm = BiometricManager.from(this)
        val can = bm.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        return can == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    routeToHome()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If user cancels / fails biometric, exit to avoid bypass
                    finish()
                }
            }
        )

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock HarvestLink")
            .setSubtitle("Use biometrics to continue")
            .setNegativeButtonText("Cancel")
            .build()

        prompt.authenticate(info)
    }

    private fun routeToHome() {
        when (Prefs.getRole(this)) {
            "farmer" -> startActivity(Intent(this, FarmerHomeActivity::class.java))
            "buyer"  -> startActivity(Intent(this, BuyerHomeActivity::class.java))
            else     -> startActivity(Intent(this, RoleSelectActivity::class.java))
        }
        finish()
    }
}
