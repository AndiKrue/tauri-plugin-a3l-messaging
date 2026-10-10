package com.andikrue.tauri.a3lmessaging

import android.content.Context
import com.amazon.A3L.messaging.RemoteMessage
import org.json.JSONArray
import org.json.JSONObject

internal object PendingEventStore {
    private const val PREFS = "tauri_a3l_messaging"
    private const val KEY_EVENTS = "pending_events_v1"
    private const val MAX_EVENTS = 100
    private val lock = Any()

    fun addToken(context: Context, token: String) {
        add(
            context,
            JSONObject()
                .put("type", "token")
                .put("token", token),
        )
    }

    fun addMessage(context: Context, message: RemoteMessage): JSONObject {
        val notification = message.notification
        val data = JSONObject()
        message.data.entries.sortedBy { it.key }.forEach { (key, value) ->
            data.put(key, value)
        }

        val event =
            JSONObject()
                .put("type", "message")
                .put("platform", message.remoteMessageType ?: "unknown")
                .put("from", message.from)
                .put("messageId", message.messageId)
                .put("sentTimeMs", message.sentTime)
                .put("ttlSeconds", message.ttl)
                .put("data", data)
                .put("notificationTitle", notification?.title)
                .put("notificationBody", notification?.body)

        add(context, event)
        return event
    }

    private fun preferences(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun read(context: Context): MutableList<JSONObject> {
        val encoded = preferences(context).getString(KEY_EVENTS, null) ?: return mutableListOf()
        return try {
            val array = JSONArray(encoded)
            MutableList(array.length()) { index -> array.getJSONObject(index) }
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    private fun write(context: Context, events: List<JSONObject>) {
        val array = JSONArray()
        events.forEach(array::put)
        preferences(context)
            .edit()
            .putString(KEY_EVENTS, array.toString())
            .apply()
    }

    private fun add(context: Context, event: JSONObject) {
        synchronized(lock) {
            val events = read(context)
            events.add(event)
            while (events.size > MAX_EVENTS) {
                events.removeAt(0)
            }
            write(context, events)
        }
    }

    fun drain(context: Context): JSONArray {
        synchronized(lock) {
            val events = read(context)
            preferences(context).edit().remove(KEY_EVENTS).apply()
            val result = JSONArray()
            events.forEach(result::put)
            return result
        }
    }
}
