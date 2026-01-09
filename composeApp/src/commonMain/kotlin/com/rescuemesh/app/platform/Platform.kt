package com.rescuemesh.app.platform

/**
 * Platform-specific information
 */
expect class Platform() {
    val name: String
    val isAndroid: Boolean
    val isIOS: Boolean
}

/**
 * Get current time in milliseconds
 */
expect fun currentTimeMillis(): Long

/**
 * Generate a UUID string
 */
expect fun randomUUID(): String
