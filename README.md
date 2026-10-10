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
