package com.andikrue.tauri.a3lmessaging

import android.app.Notification
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalNotificationPriorityTest {
    @Test
    fun attentionUsesFireTvHeadsUpPriority() {
        assertEquals(
            Notification.PRIORITY_HIGH,
            LocalNotificationRouter.priorityFor("attention"),
        )
    }

    @Test
    fun urgentUsesFireTvHeadsUpPriority() {
        assertEquals(
            Notification.PRIORITY_HIGH,
            LocalNotificationRouter.priorityFor("urgent"),
        )
    }

    @Test
    fun lowDoesNotInterruptCurrentViewing() {
        assertEquals(
            Notification.PRIORITY_LOW,
            LocalNotificationRouter.priorityFor("low"),
        )
    }
}
