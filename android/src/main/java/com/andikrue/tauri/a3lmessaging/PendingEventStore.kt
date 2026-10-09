package com.andikrue.tauri.a3lmessaging

import com.amazon.A3L.messaging.RemoteMessage
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayDeque

internal object PendingEventStore {
    private const val MAX_EVENTS = 100
    private val lock = Any()
    private val events = ArrayDeque<JSONObject>()

    fun addToken(token: String) {
        add(
            JSONObject()
                .put("type", "token")
                .put("token", token),
        )
    }

    fun addMessage(message: RemoteMessage) {
        val notification = message.notification
        val data = JSONObject()
        message.data.entries.sortedBy { it.key }.forEach { (key, value) ->
            data.put(key, value)
        }

        add(
            JSONObject()
                .put("type", "message")
                .put("platform", message.remoteMessageType ?: "unknown")
                .put("from", message.from)
                .put("messageId", message.messageId)
                .put("sentTimeMs", message.sentTime)
                .put("ttlSeconds", message.ttl)
                .put("data", data)
                .put("notificationTitle", notification?.title)
                .put("notificationBody", notification?.body),
        )
    }

    private fun add(event: JSONObject) {
        synchronized(lock) {
            while (events.size >= MAX_EVENTS) {
                events.removeFirst()
            }
            events.addLast(event)
        }
    }

    fun drain(): JSONArray {
        synchronized(lock) {
            val result = JSONArray()
            while (events.isNotEmpty()) {
                result.put(events.removeFirst())
            }
            return result
        }
    }
}
