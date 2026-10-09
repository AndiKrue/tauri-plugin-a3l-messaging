const PLUGIN_PREFIX = "plugin:a3l-messaging|";

function bridgeFailure(reason) {
  let message = "The Tauri command bridge rejected the invocation";
  if (reason instanceof Error) message = reason.message;
  else if (typeof reason === "string") message = reason;

  return {
    status: "failed",
    error: { code: "bridge_error", message, recoverable: true },
  };
}

async function invoke(command, args) {
  const tauriInternals = globalThis.__TAURI_INTERNALS__;
  if (!tauriInternals || typeof tauriInternals.invoke !== "function") {
    return bridgeFailure("The Tauri command bridge is unavailable");
  }

  try {
    return await tauriInternals.invoke(`${PLUGIN_PREFIX}${command}`, args);
  } catch (reason) {
    return bridgeFailure(reason);
  }
}

export function getToken() {
  return invoke("get_token");
}

export function getCurrentPlatform() {
  return invoke("get_current_platform");
}

export function subscribeToTopic(topic) {
  return invoke("subscribe_to_topic", { topic });
}

export function unsubscribeFromTopic(topic) {
  return invoke("unsubscribe_from_topic", { topic });
}

export function drainPendingEvents() {
  return invoke("drain_pending_events");
}
