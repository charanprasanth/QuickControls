package com.poc.quickcontrols.workmode

import android.content.Context
import com.poc.quickcontrols.action.ActionHandler
import com.poc.quickcontrols.action.QuickAction
import com.poc.quickcontrols.darkmode.DarkModeActionHandler
import com.poc.quickcontrols.dnd.DndActionHandler
import com.poc.quickcontrols.volume.VolumeActionHandler
import com.poc.quickcontrols.vpn.VpnActionHandler

/**
 * Composite — fans out one voice command into several others. Demonstrates
 * that handlers can also call into other handlers; the dispatcher stays
 * unchanged.
 */
object WorkModeActionHandler : ActionHandler {

    override val supported = setOf(QuickAction.WORK_MODE_ON, QuickAction.WORK_MODE_OFF)

    override fun handle(context: Context, action: QuickAction): ActionHandler.Result {
        val steps: List<QuickAction> = if (action == QuickAction.WORK_MODE_ON) {
            listOf(
                QuickAction.DND_ON,
                QuickAction.VOLUME_MUTE,
                QuickAction.VPN_CONNECT,
                QuickAction.DARK_MODE_ON,
            )
        } else {
            listOf(
                QuickAction.DND_OFF,
                QuickAction.VOLUME_UNMUTE,
                QuickAction.VPN_DISCONNECT,
                QuickAction.DARK_MODE_OFF,
            )
        }

        var allOk = true
        for (step in steps) {
            val handler: ActionHandler = when (step) {
                in DndActionHandler.supported -> DndActionHandler
                in VolumeActionHandler.supported -> VolumeActionHandler
                in VpnActionHandler.supported -> VpnActionHandler
                in DarkModeActionHandler.supported -> DarkModeActionHandler
                else -> continue
            }
            val r = handler.handle(context, step)
            if (!r.success) allOk = false
        }
        val label = if (action == QuickAction.WORK_MODE_ON) "Work mode ON" else "Work mode OFF"
        return ActionHandler.Result(allOk, label)
    }
}
