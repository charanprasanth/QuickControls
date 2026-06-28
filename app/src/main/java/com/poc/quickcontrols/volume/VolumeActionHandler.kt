package com.poc.quickcontrols.volume

import android.content.Context
import com.poc.quickcontrols.action.ActionHandler
import com.poc.quickcontrols.action.QuickAction

object VolumeActionHandler : ActionHandler {

    override val supported = setOf(
        QuickAction.VOLUME_MUTE, QuickAction.VOLUME_UNMUTE,
        QuickAction.VOLUME_UP, QuickAction.VOLUME_DOWN, QuickAction.VOLUME_MAX
    )

    override fun handle(context: Context, action: QuickAction): ActionHandler.Result {
        val (ok, msg) = when (action) {
            QuickAction.VOLUME_MUTE -> VolumeController.mute(context)
            QuickAction.VOLUME_UNMUTE -> VolumeController.unmute(context)
            QuickAction.VOLUME_UP -> VolumeController.up(context)
            QuickAction.VOLUME_DOWN -> VolumeController.down(context)
            QuickAction.VOLUME_MAX -> VolumeController.max(context)
            else -> false to "Unsupported"
        }
        return ActionHandler.Result(ok, msg)
    }
}