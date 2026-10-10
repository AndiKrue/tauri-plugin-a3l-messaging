package com.andikrue.tauri.a3lmessaging

import android.content.Context
import com.amazon.A3L.messaging.A3LMessagingService
import com.amazon.A3L.messaging.RemoteMessage

class A3LTauriMessagingService : A3LMessagingService() {
    override fun onMessageReceived(context: Context, remoteMessage: RemoteMessage) {
        PendingEventStore.addMessage(context, remoteMessage)
        LocalNotificationRouter.maybeNotify(context, remoteMessage)
    }

    override fun onNewToken(context: Context, token: String) {
        PendingEventStore.addToken(context, token)
    }
}
