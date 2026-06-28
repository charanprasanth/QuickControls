package com.poc.quickcontrols.ringer

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import com.poc.quickcontrols.dnd.DndController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object RingerController {

    enum class Mode { NORMAL, VIBRATE, SILENT }

    private val _mode = MutableStateFlow(Mode.NORMAL)
    val mode: StateFlow<Mode> = _mode

    private fun am(context: Context): AudioManager =
        context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun refresh(context: Context) {
        _mode.value = when (am(context).ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> Mode.SILENT
            AudioManager.RINGER_MODE_VIBRATE -> Mode.VIBRATE
            else -> Mode.NORMAL
        }
    }

    fun set(context: Context, target: Mode): Pair<Boolean, String> {
        // SILENT / VIBRATE require DND policy access on most modern Androids.
        if (target != Mode.NORMAL && !DndController.hasPolicyAccess(context)) {
            context.startActivity(
                Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return false to "Grant DND access, then try again"
        }
        val ringer = when (target) {
            Mode.SILENT -> AudioManager.RINGER_MODE_SILENT
            Mode.VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
            Mode.NORMAL -> AudioManager.RINGER_MODE_NORMAL
        }
        am(context).ringerMode = ringer
        _mode.value = target
        return true to "Ringer: ${target.name.lowercase()}"
    }
}