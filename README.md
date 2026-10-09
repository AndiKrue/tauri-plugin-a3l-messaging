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
been initialized. It stores the newest 100 events in process memory; `drainPendingEvents()` atomically
returns and clears that queue.

This is intentionally a baseline. Persistent background delivery and direct JavaScript event emission
can be added without changing the V1 command contract.

## Amazon SDK prerequisite

Amazon currently distributes A3L Messaging as an AAR. **The AAR is not vendored in this repository.**
Download A3L Messaging 1.1.1 from Amazon's official SDK page and copy:

```text
A3LMessaging-1.1.1.aar
```

to:

```text
android/libs/A3LMessaging-1.1.1.aar
```

The path is ignored by Git so this repository does not silently redistribute Amazon's SDK.

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
