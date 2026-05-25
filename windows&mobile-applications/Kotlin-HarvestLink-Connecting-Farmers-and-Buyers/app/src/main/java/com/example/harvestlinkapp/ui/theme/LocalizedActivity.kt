package com.example.harvestlinkapp.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.harvestlinkapp.util.Prefs
import java.util.Locale

abstract class LocalizedActivity : AppCompatActivity() {

    private var currentLangCode: String? = null
    private var currentDarkMode: Boolean? = null

    override fun attachBaseContext(newBase: Context) {
        val langCode = Prefs.getLanguage(newBase) ?: "en"
        val wrapped = newBase.updateLocale(langCode)
        super.attachBaseContext(wrapped)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
             currentLangCode = Prefs.getLanguage(this) ?: "en"


               val dark = Prefs.isDarkModeEnabled(this)
               AppCompatDelegate.setDefaultNightMode(
                       if (dark) AppCompatDelegate.MODE_NIGHT_YES
                               else AppCompatDelegate.MODE_NIGHT_NO
                           )

              currentLangCode = Prefs.getLanguage(this) ?: "en"
               currentDarkMode = dark
               super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        val latest = Prefs.getLanguage(this) ?: "en"
               val latestDark = Prefs.isDarkModeEnabled(this)

               if (latest != currentLangCode || latestDark != currentDarkMode) {
                       currentLangCode = latest
                       currentDarkMode = latestDark
                       recreate()
                   }
    }
}


private fun Context.updateLocale(langCode: String): Context {
    val locale = Locale(langCode)
    Locale.setDefault(locale)

    val config = resources.configuration
    config.setLocale(locale)
    config.setLayoutDirection(locale)

    return createConfigurationContext(config)
}
