# tauri-plugin-a3l-messaging

Tauri 2 bridge for **Amazon A3L Messaging** on Android and Fire OS.

The plugin keeps Amazon/Google messaging-provider differences below one Tauri command surface:
A3L selects **FCM on Android** and **ADM on Fire OS**. It does not contain Ask First or any other
product-specific semantics.

## V1 API

- `getToken()`
- `getCurrentPlatform()`
- `subscribeToTopic(topic)`
- `unsubscribeFromTopic(topic)`
- `drainPendingEvents()`

`A3LTauriMessagingService` receives A3L token/message callbacks even if the Tauri plugin has not yet
been initialized. It stores the newest 100 events in app-private persistent storage; `drainPendingEvents()` atomically
returns and clears that queue. This survives normal Android/Fire OS process eviction, including
low-memory kills.

## Optional local notification routing

A consumer may opt a data message into a native local notification by including these reserved data
fields:

- `_a3l_tauri_notify=true`
- `_a3l_tauri_title=<title>`
- `_a3l_tauri_body=<body>`
- `_a3l_tauri_urgency=low|attention|urgent`
- `_a3l_tauri_action=<button label>`

The plugin remains product-neutral: it does not interpret proposal/call semantics. When the host app is
not visible, `attention` and `urgent` use high-importance Android notification channels while `low`
uses a low-importance channel. Selecting the notification/action launches the consumer app; the
persisted message is then available through the existing `drainPendingEvents()` API.

The plugin intentionally never uses a full-screen intent and never force-opens the consumer Activity on
message receipt.

When A3L/ADM omits `messageId`, `sentTime`, and sender on a data push, the
notification ID is derived from the product-neutral canonical payload rather than
the constant legacy `"0:"` fallback (whose Kotlin hash is `1546`). This keeps
distinct deliveries from replacing the same notification and inadvertently
suppressing subsequent alerts under `setOnlyAlertOnce(true)`. True duplicates
with an identical payload still update the same notification; completely
metadata-free messages receive a fresh random identity. SDK message IDs still
take precedence when present.

### Fire TV heads-up compatibility

Amazon's [Fire TV notifications documentation](https://developer.amazon.com/docs/fire-tv/notifications.html)
specifies `Notification.PRIORITY_HIGH` for heads-up overlays. The local
notification router therefore sets this per-notification priority for
`attention` and `urgent` on **every API level**, including Fire OS 8/API 30;
`low` retains `PRIORITY_LOW`. Android 8+ notification channels still use
`IMPORTANCE_HIGH` for attention/urgent. Setting both is a compatibility measure
and **does not guarantee** Fire OS renders a heads-up card when the user/device
suppresses them.

An Ask First Fire TV capture previously confirmed native posting but identified
only an animated Home carousel as the strongest visual candidate, not the
notification banner. Validate the exact physical device's popup separately from
successful ADM HTTP submission, and do not equate a visible entry in the
Notification Center with a heads-up acceptance PASS.

JVM unit coverage lives in `android/src/test/`; executing it requires an
Android Gradle host environment and the Amazon A3L SDK artifact (the SDK is
not included in this repository).

## Unattended debug-APK WebView acceptance

Fire TV QA can use device-scoped ADB plus Chrome DevTools Protocol to drive
**actual WebView buttons**, without bypassing the native notification or sending
approval requests directly to the server. Some older Fire OS WebViews do not
automatically expose their debug socket. The Android plugin therefore calls
`WebView.setWebContentsDebuggingEnabled(true)` **only when**
`ApplicationInfo.FLAG_DEBUGGABLE` is present. This enables debug-APK QA
and does not affect release/store builds.

**Security:** Never enable this feature unconditionally: ADB DevTools can
inspect local authority state, tokens, and WebView content. Production apps
must be non-debuggable. Android unit tests cover both sides of this policy.
The QA harness must also verify that the actively running edge is an isolated
mock backend before clicking Approve or Decline.

## Amazon SDK prerequisite

Amazon currently distributes A3L Messaging as an AAR. **The AAR is not vendored in this repository.**
Download A3L Messaging 1.1.1 from Amazon's official SDK page. Consumer applications should point
the build at the SDK without copying it into this Git repository:

```powershell
$env:A3L_MESSAGING_AAR = 'C:\Tools\amazon-a3l\A3LMessaging-1.1.1.aar'
```

For local plugin development, `android/libs/A3LMessaging-1.1.1.aar` remains the fallback path.

The fallback path is ignored by Git so this repository does not silently redistribute Amazon's SDK.

The Android module follows Amazon's current setup guidance and includes Firebase Messaging `23.0.0`
because A3L uses FCM on Android and ADM on Fire OS.

## Current validation boundary

The JavaScript bridge tests are host-runnable without Amazon credentials or SDK binaries. Rust desktop
compilation is intended to stay independent of the A3L AAR because the Android module is compiled only
for the Android target.

A real Android/Fire OS build is not complete until:
1. Amazon A3L Messaging 1.1.1 is downloaded into `android/libs`;
2. ADM and FCM credentials are configured as required by the target stores;
3. the Android module compiles;
4. a real Fire OS device receives a test message.

Do not claim Fire OS acceptance before those steps pass.
