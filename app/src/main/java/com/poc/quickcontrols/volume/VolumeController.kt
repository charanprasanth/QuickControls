package com.poc.quickcontrols.volume

import android.content.Context
import android.media.AudioManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object VolumeController {

    /** 0..1 fraction of STREAM_MUSIC. */
    private val _level = MutableStateFlow(0f)
    val level: StateFlow<Float> = _level

    private fun am(context: Context): AudioManager =
        context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun refresh(context: Context) {
        val a = am(context)
        val max = a.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        _level.value = a.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / max
    }

    private fun set(context: Context, vol: Int): Pair<Boolean, String> {
        val a = am(context)
        val max = a.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val clamped = vol.coerceIn(0, max)
        a.setStreamVolume(AudioManager.STREAM_MUSIC, clamped, AudioManager.FLAG_SHOW_UI)
        _level.value = clamped.toFloat() / max
        return true to "Volume ${(clamped * 100 / max)}%"
    }

    fun mute(context: Context) = set(context, 0).let { it.first to "Muted" }
    fun unmute(context: Context): Pair<Boolean, String> {
        val a = am(context)
        val max = a.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val target = (max / 2).coerceAtLeast(1)
        set(context, target)
        return true to "Unmuted"
    }
    fun max(context: Context): Pair<Boolean, String> {
        val a = am(context)
        set(context, a.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
        return true to "Volume MAX"
    }
    fun up(context: Context): Pair<Boolean, String> {
        val a = am(context); val max = a.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val step = (max / 7).coerceAtLeast(1)
        return set(context, a.getStreamVolume(AudioManager.STREAM_MUSIC) + step)
    }
    fun down(context: Context): Pair<Boolean, String> {
        val a = am(context); val max = a.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val step = (max / 7).coerceAtLeast(1)
        return set(context, a.getStreamVolume(AudioManager.STREAM_MUSIC) - step)
    }
}