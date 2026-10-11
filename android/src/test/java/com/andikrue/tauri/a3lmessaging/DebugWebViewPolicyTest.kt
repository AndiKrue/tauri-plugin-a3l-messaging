package com.andikrue.tauri.a3lmessaging

import android.content.pm.ApplicationInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DebugWebViewPolicyTest {
    @Test
    fun productionBuildNeverEnablesWebViewDebugging() {
        assertFalse(DebugWebViewPolicy.isAllowed(0))
        assertFalse(DebugWebViewPolicy.isAllowed(ApplicationInfo.FLAG_ALLOW_BACKUP))
    }

    @Test
    fun debuggableAppMayExposeWebViewForLocalAdbAcceptance() {
        assertTrue(DebugWebViewPolicy.isAllowed(ApplicationInfo.FLAG_DEBUGGABLE))
        assertTrue(DebugWebViewPolicy.isAllowed(
            ApplicationInfo.FLAG_DEBUGGABLE or ApplicationInfo.FLAG_ALLOW_BACKUP
        ))
    }
}
