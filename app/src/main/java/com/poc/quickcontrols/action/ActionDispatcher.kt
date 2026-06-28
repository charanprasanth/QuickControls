package com.poc.quickcontrols.action

import android.content.Context
import com.poc.quickcontrols.darkmode.DarkModeActionHandler
import com.poc.quickcontrols.dnd.DndActionHandler
import com.poc.quickcontrols.ringer.RingerActionHandler
import com.poc.quickcontrols.torch.TorchActionHandler
import com.poc.quickcontrols.volume.VolumeActionHandler
import com.poc.quickcontrols.vpn.VpnActionHandler
import com.poc.quickcontrols.workmode.WorkModeActionHandler

/**
 * Routes a [QuickAction] to the handler that owns it.
 *
 * Adding a new feature is a single-line change here: append its handler.
 */
object ActionDispatcher {

    private val handlers: List<ActionHandler> = listOf(
        TorchActionHandler,
        DndActionHandler,
        VolumeActionHandler,
        DarkModeActionHandler,
        RingerActionHandler,
        VpnActionHandler,
        WorkModeActionHandler,
        // Future handlers slot in here.
    )

    fun dispatch(context: Context, action: QuickAction): ActionHandler.Result {
        val handler = handlers.firstOrNull { action in it.supported }
            ?: return ActionHandler.Result(false, "No handler for $action")
        return handler.handle(context, action)
    }
}