package com.poc.quickcontrols.vpn

import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.poc.quickcontrols.ui.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * "Fake" VpnService — establishes a tunnel, routes nothing, and just sits
 * there so the system shows the persistent VPN status icon. No real traffic
 * is forwarded; this is a PoC for the App Actions integration, not a VPN.
 *
 * Loosely based on Android's ToyVpn sample (the establish() shape is the
 * same — interface address, MTU, single allowed route — but with no socket
 * tunnel attached).
 */
class LocalVpnService : VpnService() {

    private val running = AtomicBoolean(false)
    private var iface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DISCONNECT -> { stopTunnel(); stopSelf(); return START_NOT_STICKY }
            else -> startTunnel()
        }
        return START_STICKY
    }

    private fun startTunnel() {
        if (running.get()) return
        val configure = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        iface = Builder()
            .setSession("QuickControls VPN")
            .addAddress("10.7.7.2", 24)
            .addRoute("10.7.7.0", 24)   // intentionally narrow — we route nothing real
            .setMtu(1500)
            .setConfigureIntent(configure)
            .establish()
        running.set(iface != null)
        _isConnected.value = running.get()
    }

    private fun stopTunnel() {
        try { iface?.close() } catch (_: Throwable) {}
        iface = null
        running.set(false)
        _isConnected.value = false
    }

    override fun onDestroy() {
        stopTunnel()
        super.onDestroy()
    }

    override fun onRevoke() {
        stopTunnel()
        stopSelf()
        super.onRevoke()
    }

    companion object {
        const val ACTION_CONNECT = "com.poc.quickcontrols.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.poc.quickcontrols.vpn.DISCONNECT"

        // Process-wide state observable by the UI.
        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> = _isConnected
    }
}
