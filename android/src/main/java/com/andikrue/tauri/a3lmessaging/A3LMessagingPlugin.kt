package com.andikrue.tauri.a3lmessaging

import android.app.Activity
import app.tauri.annotation.Command
import app.tauri.annotation.InvokeArg
import app.tauri.annotation.TauriPlugin
import app.tauri.plugin.Invoke
import app.tauri.plugin.JSObject
import app.tauri.plugin.Plugin
import com.amazon.A3L.messaging.A3LMessaging

@InvokeArg
internal class TopicArgs {
    lateinit var topic: String
}

@TauriPlugin
class A3LMessagingPlugin(private val hostActivity: Activity) : Plugin(hostActivity) {
    @Command
    fun getToken(invoke: Invoke) {
        A3LMessaging.getToken().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                invoke.resolve(
                    failedResult(
                        "token_unavailable",
                        task.exception?.message ?: "A3L token request failed",
                        true,
                    ),
                )
                return@addOnCompleteListener
            }

            val token = task.result
            if (token.isNullOrBlank()) {
                invoke.resolve(failedResult("token_unavailable", "A3L returned an empty token", true))
                return@addOnCompleteListener
            }

            invoke.resolve(statusResult("success").apply { put("token", token) })
        }
    }

    @Command
    fun getCurrentPlatform(invoke: Invoke) {
        try {
            val platform = A3LMessaging.getCurrentPlatform(hostActivity.applicationContext)
            invoke.resolve(
                statusResult("success").apply {
                    put("platform", platform ?: "unknown")
                },
            )
        } catch (error: Exception) {
            invoke.resolve(
                failedResult(
                    "native_error",
                    error.message ?: "A3L platform lookup failed",
                    true,
                ),
            )
        }
    }

    @Command
    fun subscribeToTopic(invoke: Invoke) {
        val topic = parseTopic(invoke) ?: return
        A3LMessaging.subscribeToTopic(topic).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                invoke.resolve(statusResult("subscribed").apply { put("topic", topic) })
            } else {
                invoke.resolve(
                    failedResult(
                        "native_error",
                        task.exception?.message ?: "A3L topic subscription failed",
                        true,
                    ),
                )
            }
        }
    }

    @Command
    fun unsubscribeFromTopic(invoke: Invoke) {
        val topic = parseTopic(invoke) ?: return
        A3LMessaging.unsubscribeFromTopic(topic).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                invoke.resolve(statusResult("unsubscribed").apply { put("topic", topic) })
            } else {
                invoke.resolve(
                    failedResult(
                        "native_error",
                        task.exception?.message ?: "A3L topic unsubscription failed",
                        true,
                    ),
                )
            }
        }
    }

    @Command
    fun drainPendingEvents(invoke: Invoke) {
        invoke.resolve(
            statusResult("success").apply {
                put("events", PendingEventStore.drain())
            },
        )
    }

    private fun parseTopic(invoke: Invoke): String? {
        val args = try {
            invoke.parseArgs(TopicArgs::class.java)
        } catch (error: Exception) {
            invoke.resolve(failedResult("invalid_topic", error.message ?: "Invalid topic", false))
            return null
        }

        val topic = args.topic.trim()
        if (topic.isEmpty()) {
            invoke.resolve(failedResult("invalid_topic", "topic must not be blank", false))
            return null
        }
        return topic
    }

    private fun statusResult(status: String): JSObject =
        JSObject().apply { put("status", status) }

    private fun failedResult(
        code: String,
        message: String,
        recoverable: Boolean,
    ): JSObject =
        statusResult("failed").apply {
            put(
                "error",
                JSObject().apply {
                    put("code", code)
                    put("message", message)
                    put("recoverable", recoverable)
                },
            )
        }
}
