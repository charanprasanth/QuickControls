package com.poc.quickcontrols.dnd

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object DndController {

    private val _isOn = MutableStateFlow(false)
    val isOn: StateFlow<Boolean> = _isOn

    private fun nm(context: Context): NotificationManager =
        context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun hasPolicyAccess(context: Context): Boolean =
        nm(context).isNotificationPolicyAccessGranted

    /** Refresh [isOn] from the live system state. */
    fun refresh(context: Context) {
        if (!hasPolicyAccess(context)) {
            _isOn.value = false; return
        }
        _isOn.value = nm(context).currentInterruptionFilter !=
            NotificationManager.INTERRUPTION_FILTER_ALL
    }

    /**
     * @return Result(true, …) on success; Result(false, …) when policy access
     *         is missing — in which case the user has been deep-linked to the
     *         settings screen.
     */
    fun setEnabled(context: Context, enable: Boolean): Pair<Boolean, String> {
        if (!hasPolicyAccess(context)) {
            launchPolicyAccessSettings(context)
            return false to "Grant DND access, then try again"
        }
        val filter = if (enable) NotificationManager.INTERRUPTION_FILTER_PRIORITY
                     else NotificationManager.INTERRUPTION_FILTER_ALL
        nm(context).setInterruptionFilter(filter)
        _isOn.value = enable
        return true to if (enable) "Do Not Disturb ON" else "Do Not Disturb OFF"
    }

    private fun launchPolicyAccessSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}