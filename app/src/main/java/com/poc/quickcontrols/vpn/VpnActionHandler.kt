package com.poc.quickcontrols.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.poc.quickcontrols.action.ActionHandler
import com.poc.quickcontrols.action.QuickAction

object VpnActionHandler : ActionHandler {

    override val supported = setOf(QuickAction.VPN_CONNECT, QuickAction.VPN_DISCONNECT)

    override fun handle(context: Context, action: QuickAction): ActionHandler.Result {
        return when (action) {
            QuickAction.VPN_CONNECT -> connect(context)
            QuickAction.VPN_DISCONNECT -> disconnect(context)
            else -> ActionHandler.Result(false, "Unsupported")
        }
    }

    private fun connect(context: Context): ActionHandler.Result {
        // VpnService.prepare() returns null once the user has approved this app
        // as a VPN. The first time it returns a system Intent we must launch.
        val prepare: Intent? = VpnService.prepare(context.applicationContext)
        if (prepare != null) {
            // No activity context here — must be a new task.
            context.startActivity(prepare.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return ActionHandler.Result(false, "Approve VPN, then say it again")
        }
        val intent = Intent(context, LocalVpnService::class.java)
            .setAction(LocalVpnService.ACTION_CONNECT)
        context.startService(intent)
        return ActionHandler.Result(true, "VPN connected")
    }

    private fun disconnect(context: Context): ActionHandler.Result {
        val intent = Intent(context, LocalVpnService::class.java)
            .setAction(LocalVpnService.ACTION_DISCONNECT)
        context.startService(intent)
        return ActionHandler.Result(true, "VPN disconnected")
    }
}
