package com.example.harvestlinkapp.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    fun wrapContext(base: Context): Context {
        val lang = Prefs.getLanguage(base)
        if (lang.isBlank()) return base

        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config: Configuration = base.resources.configuration
        config.setLocale(locale)

        return base.createConfigurationContext(config)
    }
}
