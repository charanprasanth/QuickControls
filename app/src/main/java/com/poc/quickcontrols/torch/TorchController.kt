package com.poc.quickcontrols.torch

import android.content.Context
import android.hardware.camera2.CameraManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Process-wide singleton wrapping CameraManager torch control.
 *
 * State lives here (not in a ViewModel) because the torch can be toggled from
 * VoiceActionActivity while MainActivity is also visible — both need the same
 * source of truth. Compose observes [isOn] via collectAsState().
 */
object TorchController {

    private val _isOn = MutableStateFlow(false)
    val isOn: StateFlow<Boolean> = _isOn

    private var torchAvailable: Boolean? = null
    private var cameraId: String? = null

    private fun init(context: Context): Boolean {
        if (torchAvailable != null) return torchAvailable == true
        val cm = context.applicationContext
            .getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cm.cameraIdList.firstOrNull { id ->
            val chars = cm.getCameraCharacteristics(id)
            chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }

        // Listen so we stay in sync if another app / the system toggles the torch.
        cm.registerTorchCallback(object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                if (cameraId == this@TorchController.cameraId) _isOn.value = enabled
            }
            override fun onTorchModeUnavailable(cameraId: String) {
                if (cameraId == this@TorchController.cameraId) _isOn.value = false
            }
        }, null)

        torchAvailable = cameraId != null
        return torchAvailable == true
    }

    fun isAvailable(context: Context): Boolean = init(context)

    /** @return true on success, false if torch is unavailable or the call failed. */
    fun setTorch(context: Context, on: Boolean): Boolean {
        if (!init(context)) return false
        val cm = context.applicationContext
            .getSystemService(Context.CAMERA_SERVICE) as CameraManager
        return try {
            cm.setTorchMode(cameraId!!, on)
            _isOn.value = on
            true
        } catch (t: Throwable) {
            false
        }
    }

    fun toggle(context: Context): Boolean = setTorch(context, !_isOn.value)
}