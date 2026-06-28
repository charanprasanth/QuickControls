package com.poc.quickcontrols.action

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

/**
 * Invisible (Theme.NoDisplay) entry point for both Google Assistant App Actions
 * and adb-driven local testing. Reads the "feature" extra, dispatches it, shows
 * a Toast, and finishes immediately.
 */
class VoiceActionActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val feature = intent?.getStringExtra(EXTRA_FEATURE)
        val action = QuickAction.fromFeature(feature)

        val message = if (action == null) {
            "Unknown feature: $feature"
        } else {
            ActionDispatcher.dispatch(this, action).message
        }
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        const val EXTRA_FEATURE = "feature"
    }
}