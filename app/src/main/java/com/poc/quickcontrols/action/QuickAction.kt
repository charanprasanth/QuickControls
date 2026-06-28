package com.poc.quickcontrols.action

/**
 * Every voice-triggered (or programmatic) action the app supports.
 * Adding a feature = add the enum entries here + a matching synonym set
 * in [fromFeature], then implement an [ActionHandler] for them.
 */
enum class QuickAction(val featureKey: String) {
    // Torch
    TORCH_ON("torch_on"),
    TORCH_OFF("torch_off"),

    // Do Not Disturb
    DND_ON("dnd_on"),
    DND_OFF("dnd_off"),

    // Volume (STREAM_MUSIC)
    VOLUME_MUTE("volume_mute"),
    VOLUME_UNMUTE("volume_unmute"),
    VOLUME_UP("volume_up"),
    VOLUME_DOWN("volume_down"),
    VOLUME_MAX("volume_max"),

    // App dark mode
    DARK_MODE_ON("dark_mode_on"),
    DARK_MODE_OFF("dark_mode_off"),

    // Ringer mode
    RINGER_SILENT("ringer_silent"),
    RINGER_VIBRATE("ringer_vibrate"),
    RINGER_NORMAL("ringer_normal"),

    // Local VPN
    VPN_CONNECT("vpn_connect"),
    VPN_DISCONNECT("vpn_disconnect"),

    // Composite ("Work mode")
    WORK_MODE_ON("work_mode_on"),
    WORK_MODE_OFF("work_mode_off");

    companion object {
        // One canonical map: synonym (lowercased) -> action. Keep voice
        // phrases here in sync with the string-arrays in res/values/strings.xml.
        private val synonyms: Map<String, QuickAction> = buildMap {
            fun bind(action: QuickAction, vararg phrases: String) {
                put(action.featureKey, action)
                phrases.forEach { put(it.lowercase(), action) }
            }

            // Custom phrases — deliberately unusual so Google Assistant does
            // NOT intercept them with its own built-in handlers. The user must
            // still say "in QuickControls" to engage App Actions routing.

            bind(TORCH_ON, "light up the window", "open the window",
                "lantern on", "ignite the torch", "fire up the lamp")
            bind(TORCH_OFF, "shut the window", "close the window",
                "lantern off", "kill the lamp", "douse the torch")

            bind(DND_ON, "zen mode", "raise the bubble", "do not bother me",
                "ghost mode on", "go invisible")
            bind(DND_OFF, "open the gates", "drop the bubble",
                "ghost mode off", "i'm back")

            bind(VOLUME_MUTE, "hush the room", "kill the sound", "drop the audio")
            bind(VOLUME_UNMUTE, "wake the speakers", "bring back sound", "audio on")
            bind(VOLUME_UP, "pump it up", "crank it", "more juice")
            bind(VOLUME_DOWN, "ease it down", "cool the sound", "less juice")
            bind(VOLUME_MAX, "blast the speakers", "full blast", "go loud")

            bind(DARK_MODE_ON, "nightfall", "lights out", "go dark")
            bind(DARK_MODE_OFF, "sunrise", "lights on", "go bright")

            bind(RINGER_SILENT, "library mode", "mute the phone", "go silent")
            bind(RINGER_VIBRATE, "buzz mode", "shake mode", "tickle mode")
            bind(RINGER_NORMAL, "loud mode", "bell mode", "go normal")

            bind(VPN_CONNECT, "raise the shield", "tunnel up", "cloak on", "go private")
            bind(VPN_DISCONNECT, "drop the shield", "tunnel down", "cloak off", "go public")

            bind(WORK_MODE_ON, "tunnel vision", "focus zone", "lock me in", "deep work")
            bind(WORK_MODE_OFF, "free me", "exit the zone", "unlock me", "shallow work")
        }

        /**
         * Resolve a raw "feature" string (from an Assistant intent extra or an
         * adb test intent) to a QuickAction. Accepts the canonical key or any
         * declared synonym.
         */
        fun fromFeature(raw: String?): QuickAction? {
            val v = raw?.trim()?.lowercase() ?: return null
            return synonyms[v]
        }
    }
}