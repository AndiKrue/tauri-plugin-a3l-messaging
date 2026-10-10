package com.andikrue.tauri.a3lmessaging

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.concurrent.atomic.AtomicInteger

internal object AppVisibility {
    private val started = AtomicInteger(0)

    @Volatile
    private var installed = false

    @Synchronized
    fun install(activity: Activity) {
        if (installed) return
        installed = true

        activity.application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, state: Bundle?) = Unit

                override fun onActivityStarted(activity: Activity) {
                    started.incrementAndGet()
                }

                override fun onActivityResumed(activity: Activity) = Unit
                override fun onActivityPaused(activity: Activity) = Unit

                override fun onActivityStopped(activity: Activity) {
                    started.updateAndGet { value -> if (value > 0) value - 1 else 0 }
                }

                override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) = Unit
                override fun onActivityDestroyed(activity: Activity) = Unit
            },
        )
    }

    fun isVisible(): Boolean = started.get() > 0
}
