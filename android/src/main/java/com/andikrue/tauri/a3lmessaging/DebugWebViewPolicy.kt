package com.andikrue.tauri.a3lmessaging

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.webkit.WebView

/**
 * Enable CDP inspection for unattended on-device QA, never for store builds.
 *
 * Fire OS WebView implementations may predate Android WebView 113, where
 * manifest-debuggable apps automatically expose their DevTools socket.
 */
internal object DebugWebViewPolicy {
    fun isAllowed(applicationFlags: Int): Boolean =
        (applicationFlags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    fun configure(activity: Activity) {
        if (isAllowed(activity.applicationInfo.flags)) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
    }
}
