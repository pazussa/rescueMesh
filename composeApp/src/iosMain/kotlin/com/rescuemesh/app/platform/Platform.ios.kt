package com.rescuemesh.app.platform

import platform.Foundation.NSUUID
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIDevice

/**
 * iOS Platform implementation
 */
actual class Platform actual constructor() {
    actual val name: String = UIDevice.currentDevice.systemName + " " + UIDevice.currentDevice.systemVersion
    actual val isAndroid: Boolean = false
    actual val isIOS: Boolean = true
}

/**
 * Get current time in milliseconds
 */
actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

/**
 * Generate a UUID string
 */
actual fun randomUUID(): String = NSUUID().UUIDString
