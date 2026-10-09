import assert from "node:assert/strict";
import test from "node:test";

import {
  drainPendingEvents,
  getCurrentPlatform,
  getToken,
  subscribeToTopic,
  unsubscribeFromTopic,
} from "../src/index.js";

test("typed functions call the expected Tauri plugin commands", async () => {
  const calls = [];
  globalThis.__TAURI_INTERNALS__ = {
    async invoke(command, args) {
      calls.push({ command, args });
      if (command.endsWith("get_token")) return { status: "success", token: "token-1" };
      if (command.endsWith("get_current_platform")) return { status: "success", platform: "ADM" };
      if (command.endsWith("drain_pending_events")) return { status: "success", events: [] };
      return {
        status: command.endsWith("unsubscribe_from_topic") ? "unsubscribed" : "subscribed",
        topic: args.topic,
      };
    },
  };

  assert.deepEqual(await getToken(), { status: "success", token: "token-1" });
  assert.deepEqual(await getCurrentPlatform(), { status: "success", platform: "ADM" });
  assert.deepEqual(await subscribeToTopic("concierge"), { status: "subscribed", topic: "concierge" });
  assert.deepEqual(await unsubscribeFromTopic("concierge"), { status: "unsubscribed", topic: "concierge" });
  assert.deepEqual(await drainPendingEvents(), { status: "success", events: [] });

  assert.deepEqual(
    calls.map((call) => call.command),
    [
      "plugin:a3l-messaging|get_token",
      "plugin:a3l-messaging|get_current_platform",
      "plugin:a3l-messaging|subscribe_to_topic",
      "plugin:a3l-messaging|unsubscribe_from_topic",
      "plugin:a3l-messaging|drain_pending_events",
    ],
  );
  assert.deepEqual(calls[2].args, { topic: "concierge" });
});

test("bridge failure is a typed ordinary result", async () => {
  globalThis.__TAURI_INTERNALS__ = {
    async invoke() {
      throw new Error("bridge down");
    },
  };

  assert.deepEqual(await getToken(), {
    status: "failed",
    error: {
      code: "bridge_error",
      message: "bridge down",
      recoverable: true,
    },
  });
});
