package com.poc.quickcontrols.darkmode

import android.content.Context
import com.poc.quickcontrols.action.ActionHandler
import com.poc.quickcontrols.action.QuickAction

object DarkModeActionHandler : ActionHandler {

    override val supported = setOf(QuickAction.DARK_MODE_ON, QuickAction.DARK_MODE_OFF)

    override fun handle(context: Context, action: QuickAction): ActionHandler.Result {
        val (ok, msg) = DarkModeController.setDark(context, action == QuickAction.DARK_MODE_ON)
        return ActionHandler.Result(ok, msg)
    }
}