package com.poc.quickcontrols.darkmode

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object DarkModeController {

    private const val PREFS = "quickcontrols_prefs"
    private const val KEY_DARK = "dark_mode"

    private val _isDark = MutableStateFlow(false)
    val isDark: StateFlow<Boolean> = _isDark

    fun init(context: Context) {
        val saved = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_DARK, false)
        _isDark.value = saved
        applyDelegate(saved)
    }

    fun setDark(context: Context, dark: Boolean): Pair<Boolean, String> {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_DARK, dark).apply()
        _isDark.value = dark
        applyDelegate(dark)
        return true to if (dark) "Dark mode ON" else "Light mode ON"
    }

    private fun applyDelegate(dark: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (dark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}