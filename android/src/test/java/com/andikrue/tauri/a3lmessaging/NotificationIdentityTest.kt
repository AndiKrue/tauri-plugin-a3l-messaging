package com.andikrue.tauri.a3lmessaging

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationIdentityTest {
    @Test
    fun missingSdkIdentifiersDoNotCollapseDistinctDataMessages() {
        // Reproduces the A3L/ADM Fire TV data messages: messageId=null,
        // sentTime=0 and from=null. Before the fix all used hash("0:")=1546.
        assertEquals(1546, "0:".hashCode())
        val first = NotificationIdentity.forMessage(
            null, 0L, null,
            mapOf("_a3l_tauri_notify" to "true", "proposal_id" to "demo-first"),
        )
        val second = NotificationIdentity.forMessage(
            null, 0L, null,
            mapOf("_a3l_tauri_notify" to "true", "proposal_id" to "demo-second"),
        )
        assertNotEquals(first, second)
        assertNotEquals(1546, first)
        assertNotEquals(1546, second)
        assertTrue(first > 0)
        assertTrue(second > 0)
    }

    @Test
    fun payloadIdentityIsIndependentOfMapInsertionOrder() {
        val first = NotificationIdentity.forMessage(
            null, 0L, null,
            linkedMapOf("one" to "1", "two" to "2"),
        )
        val second = NotificationIdentity.forMessage(
            null, 0L, null,
            linkedMapOf("two" to "2", "one" to "1"),
        )
        assertEquals(first, second)
    }

    @Test
    fun sdkMessageIdentityTakesPrecedenceWhenPresent() {
        val first = NotificationIdentity.forMessage(
            "sdk-id", 0L, null, mapOf("proposal_id" to "first"),
        )
        val second = NotificationIdentity.forMessage(
            "sdk-id", 42L, "sender", mapOf("proposal_id" to "second"),
        )
        assertEquals(first, second)
    }

    @Test
    fun missingAllIdentifiersStillGeneratesValidPositiveId() {
        val notificationId = NotificationIdentity.forMessage(null, 0L, null, emptyMap())
        assertTrue(notificationId > 0)
    }

    @Test
    fun metadataFallbackUsesTimeAndSender() {
        val first = NotificationIdentity.forMessage(null, 123L, "source", emptyMap())
        val second = NotificationIdentity.forMessage(null, 124L, "source", emptyMap())
        assertNotEquals(first, second)
    }
}
