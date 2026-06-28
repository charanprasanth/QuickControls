package com.poc.quickcontrols.ringer

import android.content.Context
import com.poc.quickcontrols.action.ActionHandler
import com.poc.quickcontrols.action.QuickAction

object RingerActionHandler : ActionHandler {

    override val supported = setOf(
        QuickAction.RINGER_SILENT, QuickAction.RINGER_VIBRATE, QuickAction.RINGER_NORMAL
    )

    override fun handle(context: Context, action: QuickAction): ActionHandler.Result {
        val target = when (action) {
            QuickAction.RINGER_SILENT -> RingerController.Mode.SILENT
            QuickAction.RINGER_VIBRATE -> RingerController.Mode.VIBRATE
            QuickAction.RINGER_NORMAL -> RingerController.Mode.NORMAL
            else -> return ActionHandler.Result(false, "Unsupported")
        }
        val (ok, msg) = RingerController.set(context, target)
        return ActionHandler.Result(ok, msg)
    }
}