package com.example.harvestlinkapp.ui.theme.settings

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.harvestlinkapp.R
import com.example.harvestlinkapp.databinding.ActivitySettingsBinding
import com.example.harvestlinkapp.ui.LocalizedActivity
import com.example.harvestlinkapp.ui.theme.auth.LoginActivity
import com.example.harvestlinkapp.util.Prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class SettingsActivity : LocalizedActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings_title)

        // Notifications
        binding.switchNotifications.isChecked = Prefs.isNotificationsEnabled(this)
        binding.switchNotifications.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            Prefs.setNotificationsEnabled(this, isChecked)

            if (isChecked) {
                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->

                }
            } else {
                FirebaseMessaging.getInstance().deleteToken()
            }
        }

        // Biometrics
        binding.switchBiometric.isChecked = Prefs.isBiometricsEnabled(this)
        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            Prefs.setBiometricsEnabled(this, isChecked)
        }


        binding.switchDarkMode.isChecked = Prefs.isDarkModeEnabled(this)
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            Prefs.setDarkModeEnabled(this, isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
            recreate()
        }

        setupLanguageSpinner()

        binding.btnLogout.setOnClickListener {
            Prefs.setRole(this, "")
            auth.signOut()
            Toast.makeText(this, getString(R.string.logged_out), Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupLanguageSpinner() {
        val languages = listOf(
            getString(R.string.language_english),
            getString(R.string.language_afrikaans),
            getString(R.string.language_zulu)
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languages
        ).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerLanguage.adapter = adapter

        when (Prefs.getLanguage(this)) {
            "en" -> binding.spinnerLanguage.setSelection(0)
            "af" -> binding.spinnerLanguage.setSelection(1)
            "zu" -> binding.spinnerLanguage.setSelection(2)
            else -> binding.spinnerLanguage.setSelection(0)
        }

        binding.spinnerLanguage.setOnItemSelectedListenerCompat { position ->
            val langCode = when (position) {
                0 -> "en"
                1 -> "af"
                2 -> "zu"
                else -> "en"
            }
            if (langCode != Prefs.getLanguage(this)) {
                Prefs.setLanguage(this, langCode)
                Toast.makeText(
                    this,
                    getString(R.string.language_changed),
                    Toast.LENGTH_SHORT
                ).show()
                recreate()
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

private fun android.widget.Spinner.setOnItemSelectedListenerCompat(
    onSelected: (position: Int) -> Unit
) {
    this.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: android.widget.AdapterView<*>?,
            view: android.view.View?,
            position: Int,
            id: Long
        ) {
            onSelected(position)
        }

        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
    }
}
