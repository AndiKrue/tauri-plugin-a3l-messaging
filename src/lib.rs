//! Amazon A3L Messaging integration for Tauri 2.
//!
//! The plugin exposes one typed Tauri API across Android and Fire OS while
//! Amazon A3L selects FCM or ADM underneath. The proprietary Ask First product
//! is intentionally not part of this crate.

mod commands;
mod models;

#[cfg(target_os = "android")]
mod android;

#[cfg(not(target_os = "android"))]
mod desktop;

pub use models::*;

use tauri::{plugin::TauriPlugin, Runtime};

pub fn init<R: Runtime>() -> TauriPlugin<R> {
    let builder = tauri::plugin::Builder::new("a3l-messaging").invoke_handler(
        tauri::generate_handler![
            commands::get_token,
            commands::get_current_platform,
            commands::subscribe_to_topic,
            commands::unsubscribe_from_topic,
            commands::drain_pending_events,
        ],
    );

    #[cfg(target_os = "android")]
    let builder = builder.setup(|app, api| {
        use tauri::Manager;

        let handle =
            api.register_android_plugin("com.andikrue.tauri.a3lmessaging", "A3LMessagingPlugin")?;
        app.manage(android::A3LMessagingHandle(handle));
        Ok(())
    });

    builder.build()
}
