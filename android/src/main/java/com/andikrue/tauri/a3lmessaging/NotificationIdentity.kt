package com.andikrue.tauri.a3lmessaging

import java.util.UUID

/**
 * Derive a notification/request code from the actual delivery identity.
 *
 * A3L/ADM can omit messageId, sentTime, and from on a valid data message. The
 * old fallback "0:" always hashed to 1546, updating the same notification and
 * suppressing subsequent heads-up alerts when setOnlyAlertOnce(true).
 *
 * The complete payload is a product-neutral fallback when the SDK does not
 * supply a messageId; all bytes stay in memory and are never logged.
 */
internal object NotificationIdentity {
    fun forMessage(
        messageId: String?,
        sentTime: Long,
        from: String?,
        data: Map<String, String>,
    ): Int {
        val knownId = messageId?.takeIf { it.isNotBlank() }
        val identity =
            when {
                knownId != null -> "message:$knownId"
                data.isNotEmpty() ->
                    buildString {
                        append("data:")
                        for ((key, value) in data.toSortedMap()) {
                            // Length-prefix each field; do not permit separator ambiguity.
                            append(key.length).append(':').append(key)
                            append(value.length).append(':').append(value)
                        }
                    }
                sentTime > 0L || !from.isNullOrBlank() ->
                    "metadata:$sentTime:${from.orEmpty()}"
                else ->
                    // Even a completely metadata-free message needs a new alert.
                    "fallback:${UUID.randomUUID()}"
            }

        // Mask the sign bit: absoluteValue leaves Int.MIN_VALUE negative.
        return (identity.hashCode() and Int.MAX_VALUE).coerceAtLeast(1)
    }
}
