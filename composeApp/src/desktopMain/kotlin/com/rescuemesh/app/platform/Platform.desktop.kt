package com.rescuemesh.app.platform

import java.util.UUID

actual class Platform actual constructor() {
    actual val name: String = "Desktop JVM (${System.getProperty("os.name")})"
    actual val isAndroid: Boolean = false
    actual val isIOS: Boolean = false
    val isDesktop: Boolean = true
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun randomUUID(): String = UUID.randomUUID().toString()
