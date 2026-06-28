package com.poc.quickcontrols.dnd

import android.content.Context
import com.poc.quickcontrols.action.ActionHandler
import com.poc.quickcontrols.action.QuickAction

object DndActionHandler : ActionHandler {

    override val supported = setOf(QuickAction.DND_ON, QuickAction.DND_OFF)

    override fun handle(context: Context, action: QuickAction): ActionHandler.Result {
        val (ok, msg) = DndController.setEnabled(context, action == QuickAction.DND_ON)
        return ActionHandler.Result(ok, msg)
    }
}