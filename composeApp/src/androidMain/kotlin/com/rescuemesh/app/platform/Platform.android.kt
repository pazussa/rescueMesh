package com.rescuemesh.app.platform

/**
 * Android Platform implementation
 */
actual class Platform actual constructor() {
    actual val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
    actual val isAndroid: Boolean = true
    actual val isIOS: Boolean = false
}

/**
 * Get current time in milliseconds
 */
actual fun currentTimeMillis(): Long = System.currentTimeMillis()

/**
 * Generate a UUID string
 */
actual fun randomUUID(): String = java.util.UUID.randomUUID().toString()
