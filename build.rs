const COMMANDS: &[&str] = &[
    "get_token",
    "get_current_platform",
    "subscribe_to_topic",
    "unsubscribe_from_topic",
    "drain_pending_events",
];

fn main() {
    tauri_plugin::Builder::new(COMMANDS)
        .android_path("android")
        .build();
}
