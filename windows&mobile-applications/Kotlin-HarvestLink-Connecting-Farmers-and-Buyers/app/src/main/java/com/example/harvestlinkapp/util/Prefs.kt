package com.example.harvestlinkapp.util

import android.content.Context

object Prefs {

    private const val PREFS_NAME = "harvestlink_prefs"
    private const val KEY_ROLE = "role"
    private const val KEY_BIOMETRICS_ENABLED = "biometrics_enabled"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_DARK_MODE_ENABLED = "dark_mode_enabled"


    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ---------- Role ----------

    fun setRole(context: Context, role: String) {
        prefs(context).edit()
            .putString(KEY_ROLE, role)
            .apply()
    }

    fun getRole(context: Context): String {
        return prefs(context).getString(KEY_ROLE, "") ?: ""
    }

    // ---------- Biometrics ----------

    fun setBiometricsEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_BIOMETRICS_ENABLED, enabled)
            .apply()
    }

    fun isBiometricsEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_BIOMETRICS_ENABLED, false)
    }

    // ---------- Language ----------

    fun setLanguage(context: Context, langCode: String) {
        prefs(context).edit()
            .putString(KEY_LANGUAGE, langCode)
            .apply()
    }

    fun getLanguage(context: Context): String {
        return prefs(context).getString(KEY_LANGUAGE, "en") ?: "en"
    }

    // ---------- Notifications ----------

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
            .apply()
    }

    fun isNotificationsEnabled(context: Context): Boolean {

        return prefs(context).getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

       // ---------- Dark mode ----------

      fun setDarkModeEnabled(context: Context, enabled: Boolean) {
             prefs(context).edit()
                  .putBoolean(KEY_DARK_MODE_ENABLED, enabled)
                  .apply()
           }
       fun isDarkModeEnabled(context: Context): Boolean {
              return prefs(context).getBoolean(KEY_DARK_MODE_ENABLED, false)
           }
}
