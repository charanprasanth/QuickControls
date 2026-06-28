# QuickControls

Proof-of-concept Android app for hands-free Google Assistant voice control via
**App Actions**. Each device-control feature plugs into a modular
`ActionHandler` architecture so adding a new one (or wiring it to a new voice
phrase) is a couple of files of code.

- Kotlin + Jetpack Compose, Material 3
- minSdk 26, targetSdk 34, compileSdk 34
- Package: `com.poc.quickcontrols`

## Features

| Feature   | Manual UI | Voice                                                    |
| --------- | --------- | -------------------------------------------------------- |
| Torch     | tile tap  | "turn on/off torch", "enable/disable flashlight"         |
| DND       | tile tap  | "enable/disable DND", "turn on/off do not disturb"       |
| Volume    | tile tap  | "mute", "unmute", "volume up/down", "max volume"         |
| Dark Mode | tile tap  | "enable dark mode", "light mode"                         |
| Ringer    | tile tap  | "silent mode", "vibrate", "normal mode"                  |
| Local VPN | tile tap  | "connect VPN", "disconnect VPN"                          |
| Work Mode | tile tap  | "start work mode" → DND + mute + VPN + dark, all at once |

## Build & install

### Android Studio
1. **File → Open** → choose the `QuickControls/` folder.
2. Gradle sync (AGP 9.2.1 / Gradle 9.4.1 / Kotlin 2.2.10).
3. Run on a physical device (torch + VPN need real hardware / system services).

### Command line
```bash
cd QuickControls
./gradlew :app:installDebug
adb shell am start -n com.poc.quickcontrols/.ui.MainActivity
```

### One-time permission setup
- **DND / Ringer (silent/vibrate)** — first action launches *Notification policy
  access* settings; grant access to QuickControls, then re-run the action.
- **VPN** — first connect launches the system "*An app wants to set up a VPN
  connection*" dialog; tap **OK**, then re-run.

---

## ADB commands — every action, no Assistant

```bash
ACT=com.poc.quickcontrols/.action.VoiceActionActivity
A=com.poc.quickcontrols.action.VOICE

# --- Torch ---
adb shell am start -a $A -n $ACT --es feature torch_on
adb shell am start -a $A -n $ACT --es feature torch_off

# --- Do Not Disturb ---
adb shell am start -a $A -n $ACT --es feature dnd_on
adb shell am start -a $A -n $ACT --es feature dnd_off

# --- Volume (STREAM_MUSIC) ---
adb shell am start -a $A -n $ACT --es feature volume_mute
adb shell am start -a $A -n $ACT --es feature volume_unmute
adb shell am start -a $A -n $ACT --es feature volume_up
adb shell am start -a $A -n $ACT --es feature volume_down
adb shell am start -a $A -n $ACT --es feature volume_max

# --- Dark mode (in-app) ---
adb shell am start -a $A -n $ACT --es feature dark_mode_on
adb shell am start -a $A -n $ACT --es feature dark_mode_off

# --- Ringer ---
adb shell am start -a $A -n $ACT --es feature ringer_silent
adb shell am start -a $A -n $ACT --es feature ringer_vibrate
adb shell am start -a $A -n $ACT --es feature ringer_normal

# --- Local VPN ---
adb shell am start -a $A -n $ACT --es feature vpn_connect
adb shell am start -a $A -n $ACT --es feature vpn_disconnect

# --- Composite: Work Mode ---
adb shell am start -a $A -n $ACT --es feature work_mode_on
adb shell am start -a $A -n $ACT --es feature work_mode_off
```

Natural-language phrases also work (same strings Assistant matches):

```bash
adb shell am start -n $ACT --es feature "turn on flashlight"
adb shell am start -n $ACT --es feature "mute"
adb shell am start -n $ACT --es feature "enable dark mode"
adb shell am start -n $ACT --es feature "silent mode"
adb shell am start -n $ACT --es feature "connect vpn"
adb shell am start -n $ACT --es feature "start work mode"
```

Every dispatch shows a Toast confirmation and, if MainActivity is open, the
matching tile updates live.

---

## Voice (Hey Google) — every feature

After uploading to Play Console Internal Testing (steps below):

| Phrase                                                | What happens                                |
| ----------------------------------------------------- | ------------------------------------------- |
| "Hey Google, turn on torch in QuickControls"          | Torch ON                                    |
| "Hey Google, turn off torch in QuickControls"         | Torch OFF                                   |
| "Hey Google, enable flashlight in QuickControls"      | Torch ON (synonym)                          |
| "Hey Google, enable DND in QuickControls"             | DND ON                                      |
| "Hey Google, disable DND in QuickControls"            | DND OFF                                     |
| "Hey Google, mute in QuickControls"                   | STREAM_MUSIC → 0                            |
| "Hey Google, unmute in QuickControls"                 | STREAM_MUSIC → 50%                          |
| "Hey Google, volume up / down in QuickControls"       | ±14%                                        |
| "Hey Google, max volume in QuickControls"             | STREAM_MUSIC → 100%                         |
| "Hey Google, enable dark mode in QuickControls"       | App dark theme                              |
| "Hey Google, light mode in QuickControls"             | App light theme                             |
| "Hey Google, silent mode in QuickControls"            | Ringer SILENT                               |
| "Hey Google, vibrate in QuickControls"                | Ringer VIBRATE                              |
| "Hey Google, normal mode in QuickControls"            | Ringer NORMAL                               |
| "Hey Google, connect VPN in QuickControls"            | LocalVpnService up (system VPN icon shows)  |
| "Hey Google, disconnect VPN in QuickControls"         | LocalVpnService down                        |
| "Hey Google, start work mode in QuickControls"        | DND + mute + VPN + dark, in one command     |
| "Hey Google, stop work mode in QuickControls"         | Reverses all four                           |

## Play Console Internal Testing (real Assistant)

1. Bump `versionCode` in `app/build.gradle.kts`.
2. Create a signing config / keystore, then `./gradlew :app:bundleRelease`.
3. Play Console → **Create app → Internal testing** → upload the AAB.
4. Add yourself as a tester; accept the invite link; install from Play.
5. On the device, the first VPN command will trigger the system VPN approval
   dialog; first DND/ringer command will deep-link you to Notification Policy
   Access settings (grant once).
6. Say any phrase from the table above.

For faster iteration without Play uploads, install the **Google Assistant
Plugin for Android Studio** ("App Actions Test Tool") — point it at
`actions.intent.OPEN_APP_FEATURE`, fill in the `feature` parameter, **Run**.

---

## 60-second demo script

> *Phone on a tripod or desk, screen visible. App open on the grid.*

1. **"Hey Google, start work mode in QuickControls."**
   → Tiles flip together: DND ON, Volume 0%, VPN Connected (green dot, status
   bar VPN key icon appears), Dark Mode ON. *"One command, four system
   settings."*
2. **"Hey Google, turn on torch in QuickControls."**
   → Camera flash on, Torch tile glows. *"Hands-free flashlight while my
   hands are full."*
3. **"Hey Google, volume up in QuickControls."** *(repeat once)*
   → Volume tile climbs.
4. **"Hey Google, set vibrate in QuickControls."**
   → Ringer tile switches to Vibrate.
5. **"Hey Google, stop work mode in QuickControls."**
   → Everything reverts in one shot.
6. **"Hey Google, turn off torch in QuickControls."**

That's the whole pitch: every Quick Settings tile, addressable by voice.

---

## Adding a new feature (the ActionHandler pattern)

Say you want **Wi‑Fi toggle** next:

1. **Enum** — add entries in `action/QuickAction.kt`:
   ```kotlin
   WIFI_ON("wifi_on"),
   WIFI_OFF("wifi_off"),
   ```
   …and add their synonym phrases inside `fromFeature()`'s `buildMap { … }`.

2. **Handler** — new file `wifi/WifiActionHandler.kt`:
   ```kotlin
   object WifiActionHandler : ActionHandler {
       override val supported = setOf(QuickAction.WIFI_ON, QuickAction.WIFI_OFF)
       override fun handle(ctx: Context, action: QuickAction): ActionHandler.Result {
           // Settings panel intent (no direct WifiManager toggle since Android 10)
           ...
       }
   }
   ```

3. **Register** — append `WifiActionHandler` to `ActionDispatcher.handlers`.

4. **Voice mapping** — add a `<string-array>` of synonyms in
   `res/values/strings.xml` and one `<shortcut>` in `res/xml/shortcuts.xml`
   that binds it to the existing `actions.intent.OPEN_APP_FEATURE`
   capability.

5. **UI** *(optional)* — drop a `WifiTile()` composable next to the others in
   `MainActivity.kt`.

`VoiceActionActivity`, `ActionDispatcher.dispatch()`, and the capability block
do not change — that's the architectural payoff.

---

## File map

```
app/src/main/
├── AndroidManifest.xml
├── java/com/poc/quickcontrols/
│   ├── action/
│   │   ├── ActionDispatcher.kt     ← central router
│   │   ├── ActionHandler.kt        ← feature contract
│   │   ├── QuickAction.kt          ← every supported action + synonyms
│   │   └── VoiceActionActivity.kt  ← Theme.NoDisplay intent receiver
│   ├── torch/      TorchController + TorchActionHandler
│   ├── dnd/        DndController + DndActionHandler
│   ├── volume/     VolumeController + VolumeActionHandler
│   ├── darkmode/   DarkModeController + DarkModeActionHandler
│   ├── ringer/     RingerController + RingerActionHandler
│   ├── vpn/        LocalVpnService + VpnActionHandler
│   ├── workmode/   WorkModeActionHandler (composite)
│   └── ui/         MainActivity.kt (Compose grid)
└── res/
    ├── values/strings.xml          ← inline-inventory synonyms
    └── xml/shortcuts.xml           ← App Actions capability + bindings
```













``
The QuickControls Android app is installed but long-pressing the app icon 
on the home screen does NOT show any shortcuts (e.g. "Turn on Torch" / 
"Turn off Torch"). They should appear as quick actions.

Please debug and fix this issue. Investigate the following:

1. **Check shortcuts.xml**
 - Confirm res/xml/shortcuts.xml exists with proper <shortcut> entries 
 for torch_on and torch_off
 - Each <shortcut> must have: shortcutId, shortcutShortLabel, 
 shortcutLongLabel, and a valid <intent> block
 - The <intent> must have android:action, android:targetPackage, 
 and android:targetClass set correctly

2. **Check the manifest**
 - The launcher activity (MainActivity) must have this meta-data INSIDE 
 the <activity> tag (not outside):
 ```
 <​meta-data
 android:name="android.app.shortcuts"
 android:resource="@xml/shortcuts"/>
 ```
 - The activity referenced in shortcuts.xml's targetClass must exist 
 and be declared in the manifest
 - VoiceActionActivity must be android:exported="true"

3. **Check string resources**
 - shortcutShortLabel and shortcutLongLabel should reference @string 
 resources, not hardcoded text
 - Confirm those strings exist in strings.xml

4. **Verify with ADB after fixing**
 - Run: adb shell cmd shortcut get-shortcuts com.poc.quickcontrols
 - This should list both torch_on and torch_off shortcuts
 - If it returns empty, shortcuts.xml is not being picked up

5. **Common issues to check:**
 - shortcuts.xml in wrong folder (should be res/xml/, not res/values/)
 - <capability> tags WITHOUT <shortcut> tags — App Actions capabilities 
 alone don't create home-screen shortcuts; you need BOTH
 - meta-data outside the launcher activity tag
 - Wrong activity referenced in targetClass (typo, wrong package)

Please:
1. Show me the current shortcuts.xml and AndroidManifest.xml
2. Identify what's wrong
3. Fix it
4. After fixing, run "adb shell cmd shortcut get-shortcuts com.poc.quickcontrols" 
 and confirm both shortcuts are listed
5. Then verify by long-pressing the app icon on the device — shortcuts 
 should appear
```

---

The most common cause: **`<capability>` tags create voice triggers but NOT home screen shortcuts.** You need separate `<shortcut>` tags for the long-press menu. This prompt forces Claude Code to check that exact thing. 🎯


