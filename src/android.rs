use crate::{PlatformResult, PluginError, PendingEventsResult, TokenResult, TopicResult};
use serde::{de::DeserializeOwned, Serialize};
use tauri::{plugin::PluginHandle, AppHandle, Manager, Runtime};

pub(crate) struct A3LMessagingHandle<R: Runtime>(pub(crate) PluginHandle<R>);

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct TopicArgs {
    topic: String,
}

fn invoke<R, A, T>(app: &AppHandle<R>, command: &str, args: A) -> Result<T, PluginError>
where
    R: Runtime,
    A: Serialize,
    T: DeserializeOwned,
{
    app.state::<A3LMessagingHandle<R>>()
        .0
        .run_mobile_plugin(command, args)
        .map_err(|error| PluginError::bridge(error.to_string()))
}

pub(crate) fn get_token<R: Runtime>(app: &AppHandle<R>) -> TokenResult {
    invoke(app, "getToken", ()).unwrap_or_else(|error| TokenResult::Failed { error })
}

pub(crate) fn get_current_platform<R: Runtime>(app: &AppHandle<R>) -> PlatformResult {
    invoke(app, "getCurrentPlatform", ())
        .unwrap_or_else(|error| PlatformResult::Failed { error })
}

pub(crate) fn subscribe_to_topic<R: Runtime>(app: &AppHandle<R>, topic: String) -> TopicResult {
    invoke(app, "subscribeToTopic", TopicArgs { topic })
        .unwrap_or_else(|error| TopicResult::Failed { error })
}

pub(crate) fn unsubscribe_from_topic<R: Runtime>(app: &AppHandle<R>, topic: String) -> TopicResult {
    invoke(app, "unsubscribeFromTopic", TopicArgs { topic })
        .unwrap_or_else(|error| TopicResult::Failed { error })
}

pub(crate) fn drain_pending_events<R: Runtime>(app: &AppHandle<R>) -> PendingEventsResult {
    invoke(app, "drainPendingEvents", ())
        .unwrap_or_else(|error| PendingEventsResult::Failed { error })
}
