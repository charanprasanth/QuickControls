package com.poc.quickcontrols.torch

import android.content.Context
import com.poc.quickcontrols.action.ActionHandler
import com.poc.quickcontrols.action.QuickAction

object TorchActionHandler : ActionHandler {

    override val supported: Set<QuickAction> =
        setOf(QuickAction.TORCH_ON, QuickAction.TORCH_OFF)

    override fun handle(context: Context, action: QuickAction): ActionHandler.Result {
        if (!TorchController.isAvailable(context)) {
            return ActionHandler.Result(false, "Torch not available on this device")
        }
        val target = action == QuickAction.TORCH_ON
        val ok = TorchController.setTorch(context, target)
        return if (ok) {
            ActionHandler.Result(true, if (target) "Torch turned ON" else "Torch turned OFF")
        } else {
            ActionHandler.Result(false, "Failed to toggle torch")
        }
    }
}