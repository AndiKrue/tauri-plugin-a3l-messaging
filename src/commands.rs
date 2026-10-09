use crate::{PendingEventsResult, PlatformResult, TokenResult, TopicResult};
use tauri::{AppHandle, Runtime};

#[tauri::command]
pub(crate) async fn get_token<R: Runtime>(app: AppHandle<R>) -> TokenResult {
    #[cfg(target_os = "android")]
    return crate::android::get_token(&app);

    #[cfg(not(target_os = "android"))]
    {
        let _ = app;
        crate::desktop::get_token()
    }
}

#[tauri::command]
pub(crate) async fn get_current_platform<R: Runtime>(app: AppHandle<R>) -> PlatformResult {
    #[cfg(target_os = "android")]
    return crate::android::get_current_platform(&app);

    #[cfg(not(target_os = "android"))]
    {
        let _ = app;
        crate::desktop::get_current_platform()
    }
}

#[tauri::command(rename_all = "camelCase")]
pub(crate) async fn subscribe_to_topic<R: Runtime>(
    app: AppHandle<R>,
    topic: String,
) -> TopicResult {
    #[cfg(target_os = "android")]
    return crate::android::subscribe_to_topic(&app, topic);

    #[cfg(not(target_os = "android"))]
    {
        let _ = (app, topic);
        crate::desktop::topic()
    }
}

#[tauri::command(rename_all = "camelCase")]
pub(crate) async fn unsubscribe_from_topic<R: Runtime>(
    app: AppHandle<R>,
    topic: String,
) -> TopicResult {
    #[cfg(target_os = "android")]
    return crate::android::unsubscribe_from_topic(&app, topic);

    #[cfg(not(target_os = "android"))]
    {
        let _ = (app, topic);
        crate::desktop::topic()
    }
}

#[tauri::command]
pub(crate) async fn drain_pending_events<R: Runtime>(app: AppHandle<R>) -> PendingEventsResult {
    #[cfg(target_os = "android")]
    return crate::android::drain_pending_events(&app);

    #[cfg(not(target_os = "android"))]
    {
        let _ = app;
        crate::desktop::drain_pending_events()
    }
}
