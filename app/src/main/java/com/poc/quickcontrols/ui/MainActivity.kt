package com.poc.quickcontrols.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.poc.quickcontrols.action.ActionDispatcher
import com.poc.quickcontrols.action.QuickAction
import com.poc.quickcontrols.darkmode.DarkModeController
import com.poc.quickcontrols.dnd.DndController
import com.poc.quickcontrols.ringer.RingerController
import com.poc.quickcontrols.torch.TorchController
import com.poc.quickcontrols.volume.VolumeController
import com.poc.quickcontrols.vpn.LocalVpnService

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Warm controllers + load persisted state before first composition.
        TorchController.isAvailable(this)
        DarkModeController.init(this)
        DndController.refresh(this)
        VolumeController.refresh(this)
        RingerController.refresh(this)

        setContent { QuickControlsApp() }
    }

    override fun onResume() {
        super.onResume()
        // System state may have changed while we were away (DND toggled in
        // quick settings, ringer changed by hardware buttons, etc).
        DndController.refresh(this)
        VolumeController.refresh(this)
        RingerController.refresh(this)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickControlsApp() {
    val isDark by DarkModeController.isDark.collectAsState()
    val colors = if (isDark) darkColorScheme() else lightColorScheme()

    MaterialTheme(colorScheme = colors) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    TopAppBar(title = {
                        Text("QuickControls", fontWeight = FontWeight.SemiBold)
                    })
                }
            ) { padding ->
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { TorchTile() }
                    item { DndTile() }
                    item { VolumeTile() }
                    item { DarkModeTile() }
                    item { RingerTile() }
                    item { VpnTile() }
                    item { WorkModeTile() }
                }
            }
        }
    }
}

// -- Generic tile ---------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Tile(
    title: String,
    state: String,
    icon: ImageVector,
    active: Boolean,
    enabled: Boolean = true,
    statusDot: Color? = null,
    onClick: () -> Unit,
) {
    val container = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant
        active -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val content = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant
        active -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        onClick = { if (enabled) onClick() }
    ) {
        Box(Modifier.fillMaxSize().padding(14.dp)) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (statusDot != null) {
                        Box(
                            Modifier.size(10.dp).clip(CircleShape)
                                .background(statusDot)
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(state, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -- Per-feature tiles ----------------------------------------------------

@Composable
private fun TorchTile() {
    val context = LocalContext.current
    val on by TorchController.isOn.collectAsState()
    val available = TorchController.isAvailable(context)
    Tile(
        title = "Torch",
        state = if (!available) "N/A" else if (on) "ON" else "OFF",
        icon = if (on) Icons.Filled.FlashlightOn else Icons.Filled.FlashlightOff,
        active = on && available,
        enabled = available,
    ) {
        ActionDispatcher.dispatch(
            context,
            if (on) QuickAction.TORCH_OFF else QuickAction.TORCH_ON
        )
    }
}

@Composable
private fun DndTile() {
    val context = LocalContext.current
    val on by DndController.isOn.collectAsState()
    Tile(
        title = "Do Not Disturb",
        state = if (on) "ON" else "OFF",
        icon = Icons.Filled.DoNotDisturbOn,
        active = on,
    ) {
        ActionDispatcher.dispatch(
            context,
            if (on) QuickAction.DND_OFF else QuickAction.DND_ON
        )
    }
}

@Composable
private fun VolumeTile() {
    val context = LocalContext.current
    val level by VolumeController.level.collectAsState()
    val muted = level <= 0f
    Tile(
        title = "Volume",
        state = "${(level * 100).toInt()}%",
        icon = when {
            muted -> Icons.Filled.VolumeOff
            level >= 0.99f -> Icons.Filled.VolumeUp
            else -> Icons.Filled.VolumeMute
        },
        active = !muted,
    ) {
        // Tap cycles: 0 -> 50% -> 100% -> 0
        val next = when {
            muted -> QuickAction.VOLUME_UNMUTE
            level >= 0.99f -> QuickAction.VOLUME_MUTE
            else -> QuickAction.VOLUME_MAX
        }
        ActionDispatcher.dispatch(context, next)
    }
}

@Composable
private fun DarkModeTile() {
    val context = LocalContext.current
    val dark by DarkModeController.isDark.collectAsState()
    Tile(
        title = "Dark Mode",
        state = if (dark) "DARK" else "LIGHT",
        icon = if (dark) Icons.Filled.Brightness2 else Icons.Filled.WbSunny,
        active = dark,
    ) {
        ActionDispatcher.dispatch(
            context,
            if (dark) QuickAction.DARK_MODE_OFF else QuickAction.DARK_MODE_ON
        )
    }
}

@Composable
private fun RingerTile() {
    val context = LocalContext.current
    val mode by RingerController.mode.collectAsState()
    val label = mode.name.lowercase().replaceFirstChar { it.uppercase() }
    Tile(
        title = "Ringer",
        state = label,
        icon = when (mode) {
            RingerController.Mode.SILENT -> Icons.Filled.VolumeOff
            RingerController.Mode.VIBRATE -> Icons.Filled.Vibration
            RingerController.Mode.NORMAL -> Icons.Filled.NotificationsActive
        },
        active = mode != RingerController.Mode.NORMAL,
    ) {
        // Tap cycles: NORMAL -> VIBRATE -> SILENT -> NORMAL
        val next = when (mode) {
            RingerController.Mode.NORMAL -> QuickAction.RINGER_VIBRATE
            RingerController.Mode.VIBRATE -> QuickAction.RINGER_SILENT
            RingerController.Mode.SILENT -> QuickAction.RINGER_NORMAL
        }
        ActionDispatcher.dispatch(context, next)
    }
}

@Composable
private fun VpnTile() {
    val context = LocalContext.current
    val connected by LocalVpnService.isConnected.collectAsState()
    Tile(
        title = "Local VPN",
        state = if (connected) "Connected" else "Disconnected",
        icon = Icons.Filled.Shield,
        active = connected,
        statusDot = if (connected) Color(0xFF2ECC71) else null,
    ) {
        ActionDispatcher.dispatch(
            context,
            if (connected) QuickAction.VPN_DISCONNECT else QuickAction.VPN_CONNECT
        )
    }
}

@Composable
private fun WorkModeTile() {
    val context = LocalContext.current
    val dnd by DndController.isOn.collectAsState()
    val vpn by LocalVpnService.isConnected.collectAsState()
    val dark by DarkModeController.isDark.collectAsState()
    val level by VolumeController.level.collectAsState()
    val active = dnd && vpn && dark && level <= 0f
    Tile(
        title = "Work Mode",
        state = if (active) "ON" else "OFF",
        icon = Icons.Filled.Work,
        active = active,
    ) {
        ActionDispatcher.dispatch(
            context,
            if (active) QuickAction.WORK_MODE_OFF else QuickAction.WORK_MODE_ON
        )
    }
}
