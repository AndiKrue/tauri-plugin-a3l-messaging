use serde::{Deserialize, Serialize};
use std::collections::BTreeMap;

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
#[serde(rename_all = "camelCase")]
pub struct PluginError {
    pub code: ErrorCode,
    pub message: String,
    pub recoverable: bool,
}

impl PluginError {
    #[cfg(target_os = "android")]
    pub(crate) fn bridge(message: impl Into<String>) -> Self {
        Self {
            code: ErrorCode::BridgeError,
            message: message.into(),
            recoverable: true,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
#[serde(rename_all = "snake_case")]
pub enum ErrorCode {
    BridgeError,
    NativeError,
    InvalidTopic,
    TokenUnavailable,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
#[serde(tag = "status", rename_all = "snake_case", rename_all_fields = "camelCase")]
pub enum TokenResult {
    Success { token: String },
    Unsupported,
    Failed { error: PluginError },
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
#[serde(tag = "status", rename_all = "snake_case", rename_all_fields = "camelCase")]
pub enum PlatformResult {
    Success { platform: String },
    Unsupported,
    Failed { error: PluginError },
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
#[serde(tag = "status", rename_all = "snake_case", rename_all_fields = "camelCase")]
pub enum TopicResult {
    Subscribed { topic: String },
    Unsubscribed { topic: String },
    Unsupported,
    Failed { error: PluginError },
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
#[serde(tag = "type", rename_all = "snake_case", rename_all_fields = "camelCase")]
pub enum PendingEvent {
    Token {
        token: String,
    },
    Message {
        platform: String,
        from: Option<String>,
        message_id: Option<String>,
        sent_time_ms: i64,
        ttl_seconds: i32,
        data: BTreeMap<String, String>,
        notification_title: Option<String>,
        notification_body: Option<String>,
    },
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
#[serde(tag = "status", rename_all = "snake_case", rename_all_fields = "camelCase")]
pub enum PendingEventsResult {
    Success { events: Vec<PendingEvent> },
    Unsupported,
    Failed { error: PluginError },
}
